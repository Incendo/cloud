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
package cloud.commandframework.examples.bungee;

import cloud.commandframework.Description;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.bungee.arguments.PlayerParser;
import cloud.commandframework.bungee.arguments.ServerParser;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.RichDescription;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import static net.kyori.adventure.text.Component.text;

public final class ExamplePlugin extends Plugin {

    private BungeeCommandManager<CommandSender> manager;
    private BungeeAudiences bungeeAudiences;

    @Override
    public void onEnable() {
        this.manager = new BungeeCommandManager<>(
                this,
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        );

        this.bungeeAudiences = BungeeAudiences.create(this);

        MinecraftExceptionHandler.create(this.bungeeAudiences::sender)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .decorator(component -> text()
                        .append(text("[", NamedTextColor.DARK_GRAY))
                        .append(text("Example", NamedTextColor.GOLD))
                        .append(text("] ", NamedTextColor.DARK_GRAY))
                        .append(component).build()
                )
                .registerTo(this.manager);
        this.constructCommands();
    }

    private void constructCommands() {
        //
        // Create a player command
        //
        this.manager.command(
                this.manager.commandBuilder("player")
                        .senderType(ProxiedPlayer.class)
                        .required(
                                "player",
                                PlayerParser.playerParser(),
                                RichDescription.of(text("Player ").append(text("name", NamedTextColor.GOLD)))
                        )
                        .handler(context -> {
                            final ProxiedPlayer player = context.get("player");
                            this.bungeeAudiences.sender(context.sender()).sendMessage(
                                    text("Selected ", NamedTextColor.GOLD)
                                            .append(text(player.getDisplayName(), NamedTextColor.AQUA))
                            );
                        })
        );

        //
        // Create a server command
        //
        this.manager.command(
                this.manager.commandBuilder("server")
                        .senderType(ProxiedPlayer.class)
                        .required("server", ServerParser.serverParser(), Description.of("Server name"))
                        .handler(context -> {
                            final ServerInfo server = context.get("server");
                            this.bungeeAudiences.sender(context.sender()).sendMessage(
                                    text("Selected ", NamedTextColor.GOLD)
                                            .append(text(server.getName(), NamedTextColor.AQUA))
                            );
                        })
        );
    }
}
