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
package cloud.commandframework.examples.bukkit.builder.feature.minecraft;

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.RichDescription;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.CommandDescription.commandDescription;
import static cloud.commandframework.minecraft.extras.TextColorParser.textColorParser;
import static net.kyori.adventure.text.Component.text;

/**
 * Example showcasing the text color parser from cloud-minecraft-extras.
 */
public final class TextColorExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .commandDescription(
                                commandDescription(
                                        RichDescription.of(text(
                                                "Sets the color scheme for '/example help'",
                                                NamedTextColor.GREEN
                                        ))
                                )
                        )
                        .literal("helpcolors")
                        .required(
                                "primary", textColorParser(),
                                RichDescription.of(text("The primary color for the color scheme", Style.style(TextDecoration.ITALIC)))
                        )
                        .required(
                                "highlight", textColorParser(),
                                RichDescription.of(text("The primary color used to highlight commands and queries"))
                        )
                        .required(
                                "alternate_highlight",
                                textColorParser(),
                                RichDescription.of(text("The secondary color used to highlight commands and queries"))
                        ).required("text", textColorParser(), RichDescription.of(text("The color used for description text")))
                        .required("accent", textColorParser(), RichDescription.of(text("The color used for accents and symbols")))
                        .handler(c -> examplePlugin.minecraftHelp().setHelpColors(MinecraftHelp.HelpColors.of(
                            c.get("primary"),
                            c.get("highlight"),
                            c.get("alternate_highlight"),
                            c.get("text"),
                            c.get("accent")
                        )))
        );
    }
}
