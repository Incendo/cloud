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
package cloud.commandframework.examples.bukkit.builder;

import cloud.commandframework.CommandManager;
import cloud.commandframework.Description;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.feature.AggregateCommandExample;
import cloud.commandframework.examples.bukkit.builder.feature.CommandBeanExample;
import cloud.commandframework.examples.bukkit.builder.feature.CompoundArgumentExample;
import cloud.commandframework.examples.bukkit.builder.feature.ConfirmationExample;
import cloud.commandframework.examples.bukkit.builder.feature.EnumExample;
import cloud.commandframework.examples.bukkit.builder.feature.FlagExample;
import cloud.commandframework.examples.bukkit.builder.feature.HelpExample;
import cloud.commandframework.examples.bukkit.builder.feature.PermissionExample;
import cloud.commandframework.examples.bukkit.builder.feature.RegexExample;
import cloud.commandframework.examples.bukkit.builder.feature.StringArrayExample;
import cloud.commandframework.examples.bukkit.builder.feature.TaskRecipeExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.BlockPredicateExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.ItemStackExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.ItemStackPredicateExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.NamespacedKeyExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.SelectorExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.TextColorExample;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Main entrypoint for all builder-related examples. It registers a bunch of {@link #FEATURES features}
 * that showcase specific cloud concepts.
 */
public final class BuilderExample {

    private static final List<BuilderFeature> FEATURES = Arrays.asList(
            new AggregateCommandExample(),
            new CommandBeanExample(),
            new CompoundArgumentExample(),
            new ConfirmationExample(),
            new EnumExample(),
            new FlagExample(),
            new HelpExample(),
            new PermissionExample(),
            new RegexExample(),
            new StringArrayExample(),
            new TaskRecipeExample(),
            // Minecraft-specific features
            new ItemStackExample(),
            new SelectorExample(),
            new TextColorExample()
    );

    private static @NonNull List<@NonNull BuilderFeature> conditionalFeatures(final @NonNull CommandManager<CommandSender> manager) {
        final List<BuilderFeature> features = new ArrayList<>();
        if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            features.add(new BlockPredicateExample());
            features.add(new ItemStackPredicateExample());
        }
        try {
            Class.forName("org.bukkit.NamespacedKey");
            features.add(new NamespacedKeyExample());
        } catch (final ClassNotFoundException ignored) {
            // We do not care.
        }
        return features;
    }

    private final ExamplePlugin examplePlugin;
    private final BukkitCommandManager<CommandSender> manager;

    public BuilderExample(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        this.examplePlugin = examplePlugin;
        this.manager = manager;

        // Creates the root node.
        this.manager.command(
                this.manager.commandBuilder("builder",  Description.of("Builder examples"), "b")
        );

        // Set up the example modules.
        this.setupExamples();
    }

    private void setupExamples() {
        FEATURES.forEach(feature -> feature.registerFeature(this.examplePlugin, this.manager));
        conditionalFeatures(this.manager).forEach(feature -> feature.registerFeature(this.examplePlugin, this.manager));
    }
}
