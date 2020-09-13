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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.components.standard.EnumComponent;
import com.intellectualsites.commands.components.standard.StringComponent;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BukkitTest extends JavaPlugin {

    @Override
    public void onLoad() {
        try {
            final BukkitCommandManager commandManager = new BukkitCommandManager(this,
                                                                                 CommandExecutionCoordinator.simpleCoordinator());
            commandManager.registerCommand(commandManager.commandBuilder("gamemode",
                             Collections.singleton("gajmöde"),
                             BukkitCommandMetaBuilder.builder()
                                                     .withDescription("Your ugli")
                                                     .build())
             .withComponent(EnumComponent.required(GameMode.class, "gamemode"))
             .withComponent(StringComponent.<BukkitCommandSender>newBuilder("player")
                                           .withSuggestionsProvider((v1, v2) -> {
                                               final List<String> suggestions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                                                                          .map(Player::getName).collect(Collectors.toList()));
                                               suggestions.add("dog");
                                               suggestions.add("cat");
                                               return suggestions;
                                           }).build())
             .withHandler(c -> c.getCommandSender()
                                .asPlayer()
                                .setGameMode(c.<GameMode>get("gamemode")
                                              .orElse(GameMode.SURVIVAL)))
             .build())
                          .registerCommand(commandManager.commandBuilder("kenny")
                                                         .withComponent(StaticComponent.required("sux"))
                                                         .build());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
