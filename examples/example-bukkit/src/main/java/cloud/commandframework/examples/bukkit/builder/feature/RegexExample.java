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
package cloud.commandframework.examples.bukkit.builder.feature;

import cloud.commandframework.arguments.preprocessor.RegexPreprocessor;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionProvider;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example showcasing the regex processing. This also showcases how to register custom captions.
 */
public final class RegexExample implements BuilderFeature {

    private static final String MONEY_PATTERN = "(?=.*?\\d)^\\$?(([1-9]\\d{0,2}(,\\d{3})*)|\\d+)?(\\.\\d{1,2})?$";

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        final Caption moneyCaption = Caption.of("annotations.regex.money");
        manager.captionRegistry().registerProvider(CaptionProvider.constantProvider(
                moneyCaption,
                "'<input>' is not very cash money of you"
        ));

        manager.command(
                manager.commandBuilder("builder")
                        .literal("pay")
                        .required(
                                manager.componentBuilder(String.class, "money")
                                        .preprocessor(RegexPreprocessor.of(MONEY_PATTERN))
                        ).handler(commandContext -> {
                            final String money = commandContext.get("money");
                            examplePlugin.bukkitAudiences()
                                    .sender(commandContext.getSender())
                                    .sendMessage(text().append(text("You have been given ", NamedTextColor.AQUA))
                                                    .append(text(money, NamedTextColor.GOLD)));
                        })
        );
    }
}
