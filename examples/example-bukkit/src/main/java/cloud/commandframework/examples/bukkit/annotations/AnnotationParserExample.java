//
// MIT License
//
// Copyright (c) 2024 Incendo
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
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.feature.BuilderModifierExample;
import cloud.commandframework.examples.bukkit.annotations.feature.CommandContainerExample;
import cloud.commandframework.examples.bukkit.annotations.feature.EnumExample;
import cloud.commandframework.examples.bukkit.annotations.feature.FlagExample;
import cloud.commandframework.examples.bukkit.annotations.feature.HelpExample;
import cloud.commandframework.examples.bukkit.annotations.feature.InjectionExample;
import cloud.commandframework.examples.bukkit.annotations.feature.PermissionExample;
import cloud.commandframework.examples.bukkit.annotations.feature.RegexExample;
import cloud.commandframework.examples.bukkit.annotations.feature.RootCommandDeletionExample;
import cloud.commandframework.examples.bukkit.annotations.feature.StringArrayExample;
import cloud.commandframework.examples.bukkit.annotations.feature.minecraft.LocationExample;
import cloud.commandframework.examples.bukkit.annotations.feature.minecraft.NamespacedKeyExample;
import cloud.commandframework.examples.bukkit.annotations.feature.minecraft.SelectorExample;
import java.util.Arrays;
import java.util.List;
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
            new EnumExample(),
            new FlagExample(),
            new HelpExample(),
            new InjectionExample(),
            new PermissionExample(),
            new RegexExample(),
            new RootCommandDeletionExample(),
            new StringArrayExample(),
            // Minecraft-specific features
            new LocationExample(),
            new NamespacedKeyExample(),
            new SelectorExample()
    );

    private final ExamplePlugin examplePlugin;
    private final AnnotationParser<CommandSender> annotationParser;

    public AnnotationParserExample(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull CommandManager<CommandSender> manager
    ) {
        this.examplePlugin = examplePlugin;
        this.annotationParser = new AnnotationParser<>(manager, CommandSender.class);

        // Set up the example modules.
        this.setupExamples();
    }

    private void setupExamples() {
        FEATURES.forEach(feature -> feature.registerFeature(this.examplePlugin, this.annotationParser));
    }
}
