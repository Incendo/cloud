//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.cloudvelocitytest;

import com.google.inject.Inject;
import com.intellectualsites.commands.annotations.AnnotationParser;
import com.intellectualsites.commands.annotations.Argument;
import com.intellectualsites.commands.annotations.CommandMethod;
import com.intellectualsites.commands.annotations.specifier.Range;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.velocity.VelocityCommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Function;

@Plugin(
        id = "cloud-velocity-test",
        name = "Cloud Velocity Test",
        version = "0.0.2.0-SNAPSHOT"
)
public class CloudVelocityTest {

    @Inject private ProxyServer server;
    @Inject private Logger logger;

    /**
     * Event listener
     *
     * @param event Event
     */
    @Subscribe
    public final void onProxyInitialization(@Nonnull final ProxyInitializeEvent event) {
        final VelocityCommandManager<CommandSource> m = new VelocityCommandManager<>(
                server,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );
        m.command(
                m.commandBuilder("proxi", "pp")
                 .literal("is")
                 .literal("cute")
                 .handler(c -> c.getSender().sendMessage(TextComponent.of("That's right ;)").color(NamedTextColor.GOLD)))
                 .build()
        );
        final AnnotationParser<CommandSource> annotationParser = new AnnotationParser<>(m, CommandSource.class,
                                                                                        p -> SimpleCommandMeta.empty());
        annotationParser.parse(this);
    }

    @CommandMethod(value = "test", permission = "cloud.root")
    private void testRoot(@Nonnull final CommandSource source) {
        source.sendMessage(TextComponent.builder("Hello from the root!", NamedTextColor.GOLD));
    }

    @CommandMethod("test <num> [str]")
    private void testCommand(@Nonnull @Argument(value = "str", defaultValue = "potato") final String string,
                             @Nonnull final CommandSource source,
                             @Argument("num") @Range(min = "10", max = "33") final int num) {
        source.sendMessage(TextComponent.builder()
                                        .append("You wrote: ", NamedTextColor.GOLD)
                                        .append(string, NamedTextColor.RED)
                                        .append(" and ", NamedTextColor.GOLD)
                                        .append(Integer.toString(num), NamedTextColor.RED)
                                        .append("!", NamedTextColor.GOLD)
        );
    }

}
