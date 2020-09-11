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
import com.intellectualsites.commands.components.standard.IntegerComponent;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.NoPermissionException;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.sender.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

class CommandTreeTest {

    private static CommandManager<CommandSender, SimpleCommandMeta> commandManager;

    @BeforeAll
    static void newTree() {
        commandManager = new TestCommandManager();
        commandManager.registerCommand(commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                                                     .withComponent(StaticComponent.required("one")).build())
                      .registerCommand(commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                                                     .withComponent(StaticComponent.required("two")).withPermission("no").build())
                      .registerCommand(commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                                                     .withComponent(StaticComponent.required("opt"))
                                                     .withComponent(IntegerComponent.optional("num", 15)).build());
    }

    @Test
    void parse() {
        final Optional<Command<CommandSender, SimpleCommandMeta>> command = commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                Arrays.asList("test", "one")));
        Assertions.assertTrue(command.isPresent());
        Assertions.assertThrows(NoPermissionException.class, () -> commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                Arrays.asList("test", "two"))));
        commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(Arrays.asList("test", "opt")))
                      .ifPresent(c -> c.getCommandExecutionHandler().execute(new CommandContext<>(new TestCommandSender())));
        commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(Arrays.asList("test", "opt", "12")))
                      .ifPresent(c -> c.getCommandExecutionHandler().execute(new CommandContext<>(new TestCommandSender())));
    }

    @Test
    void getSuggestions() {
        Assertions.assertFalse(commandManager.getCommandTree().getSuggestions(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                Collections.singletonList("test"))).isEmpty());
    }

}
