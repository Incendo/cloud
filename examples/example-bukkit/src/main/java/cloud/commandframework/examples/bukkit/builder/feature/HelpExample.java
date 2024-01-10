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
package cloud.commandframework.examples.bukkit.builder.feature;

import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.help.result.CommandEntry;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;

/**
 * Example that uses an annotated command to query commands.
 * <p>
 * This relies on {@link MinecraftHelp} from cloud-minecraft-extras.
 */
public final class HelpExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .literal("help")
                        .optional(
                                "query",
                                greedyStringParser(),
                                DefaultValue.constant(""),
                                SuggestionProvider.blocking((ctx, in) -> manager.createHelpHandler()
                                        .queryRootIndex(ctx.sender())
                                        .entries()
                                        .stream()
                                        .map(CommandEntry::syntax)
                                        .map(Suggestion::simple)
                                        .collect(Collectors.toList()))
                        )
                        .handler(context -> {
                            examplePlugin.minecraftHelp().queryCommands(context.get("query"), context.sender());
                        })
        );
    }
}
