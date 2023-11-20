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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example of the deletion of root commands. Us showcasing this does NOT mean that we recommend this. Do this at your own risk.
 */
@CommandMethod("annotations")
public final class RootCommandDeletionExample implements AnnotationFeature {

    private CommandManager<CommandSender> manager;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.manager = annotationParser.manager();
        annotationParser.parse(this);
    }

    @CommandMethod("removeall")
    public void removeAll(
            final @NonNull CommandSender sender
    ) {
        this.manager.rootCommands().forEach(this.manager::deleteRootCommand);
        sender.sendMessage("All root commands have been deleted :)");
    }

    @CommandMethod("removesingle <command>")
    public void removeSingle(
            final @NonNull CommandSender sender,
            final @Argument(value = "command", suggestions = "commands") String command
    ) {
        this.manager.deleteRootCommand(command);
        sender.sendMessage("Deleted the root command :)");
    }

    @Suggestions("commands")
    public List<String> commands(
            final @NonNull CommandContext<CommandSender> context,
            final @NonNull String input
    ) {
        return new ArrayList<>(this.manager.rootCommands());
    }
}
