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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.Regex;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionProvider;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example showcasing the regex processing. This also showcases how to register custom captions.
 */
public final class RegexExample implements AnnotationFeature {

    private BukkitAudiences bukkitAudiences;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.bukkitAudiences = examplePlugin.bukkitAudiences();
        annotationParser.parse(this);

        final Caption moneyCaption = Caption.of("annotations.regex.money");
        annotationParser.manager().captionRegistry().registerProvider(CaptionProvider.constantProvider(
                moneyCaption,
                "'<input>' is not very cash money of you"
        ));
    }

    @Command("annotations pay <money>")
    @CommandDescription("Command to test the preprocessing system")
    public void commandPay(
            final @NonNull CommandSender sender,
            final @Argument("money") @Regex(value = "(?=.*?\\d)^\\$?(([1-9]\\d{0,2}(,\\d{3})*)|\\d+)?(\\.\\d{1,2})?$",
                    failureCaption = "annotations.regex.money") String money
    ) {
        this.bukkitAudiences.sender(sender).sendMessage(
                text().append(text("You have been given ", NamedTextColor.AQUA))
                        .append(text(money, NamedTextColor.GOLD))
        );
    }
}
