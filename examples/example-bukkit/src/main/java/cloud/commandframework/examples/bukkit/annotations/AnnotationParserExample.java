//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.examples.bukkit.annotations;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.feature.BuilderModifierExample;
import cloud.commandframework.examples.bukkit.annotations.feature.CommandContainerExample;
import cloud.commandframework.examples.bukkit.annotations.feature.ConfirmationExample;
import cloud.commandframework.examples.bukkit.annotations.feature.EnumExample;
import cloud.commandframework.examples.bukkit.annotations.feature.FlagExample;
import cloud.commandframework.examples.bukkit.annotations.feature.HelpExample;
import cloud.commandframework.examples.bukkit.annotations.feature.PermissionExample;
import cloud.commandframework.examples.bukkit.annotations.feature.RegexExample;
import cloud.commandframework.examples.bukkit.annotations.feature.RootCommandDeletionExample;
import cloud.commandframework.examples.bukkit.annotations.feature.TaskRecipeExample;
import cloud.commandframework.examples.bukkit.annotations.feature.minecraft.LocationExample;
import cloud.commandframework.examples.bukkit.annotations.feature.minecraft.SelectorExample;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.tasks.TaskConsumer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Main entrypoint for all annotation related examples. This class is responsible for creating and configuring
 * the annotation parser. It then registers a bunch of {@link #FEATURES features} that showcase specific cloud concepts.
 */
public final class AnnotationParserExample {

    private static final List<AnnotationFeature> FEATURES = Arrays.asList(
            new BuilderModifierExample(),
            new CommandContainerExample(),
            new ConfirmationExample(),
            new EnumExample(),
            new FlagExample(),
            new HelpExample(),
            new PermissionExample(),
            new RegexExample(),
            new RootCommandDeletionExample(),
            new TaskRecipeExample(),
            // Minecraft-specific features
            new LocationExample(),
            new SelectorExample()
    );

    private final ExamplePlugin examplePlugin;
    private final AnnotationParser<CommandSender> annotationParser;

    public AnnotationParserExample(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull CommandManager<CommandSender> manager
    ) {
        this.examplePlugin = examplePlugin;

        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();
        this.annotationParser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                commandMetaFunction
        );

        this.setupSynchronization();

        // Set up the example modules.
        this.setupExamples();

        //
        // Parse all @CommandMethod-annotated methods
        //
        this.annotationParser.parse(this);
    }

    /**
     * Adds support for the {@link Synchronized} annotation by running the command handler through a synchronous
     * task recipe step.
     */
    private void setupSynchronization() {
        final BukkitCommandManager<CommandSender> commandManager =
                (BukkitCommandManager<CommandSender>) this.annotationParser.manager();
        this.annotationParser.registerBuilderModifier(
                Synchronized.class,
                (annotation, builder) -> builder.handler(
                        commandContext -> commandManager.taskRecipe()
                                .begin(commandContext)
                                .synchronous((TaskConsumer<CommandContext<CommandSender>>) context -> builder.handler()
                                        .execute(commandContext))
                                .execute()
                )
        );
    }

    private void setupExamples() {
        FEATURES.forEach(feature -> feature.registerFeature(this.examplePlugin, this.annotationParser));
    }
}
