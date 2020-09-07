//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.NoPermissionException;
import com.intellectualsites.commands.sender.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommandTreeTest {

    private static CommandManager<CommandSender> commandManager;

    @BeforeAll
    static void newTree() {
        commandManager = new TestCommandManager();
        commandManager.registerCommand(commandManager.commandBuilder("test")
                                                     .withComponent(StaticComponent.required("one")).build())
                      .registerCommand(commandManager.commandBuilder("test")
                                                     .withComponent(StaticComponent.required("two")).withPermission("no").build());
    }

    @Test
    void parse() {
        final Optional<Command<CommandSender>> command = commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                Arrays.asList("test", "one")));
        Assertions.assertTrue(command.isPresent());
        Assertions.assertThrows(NoPermissionException.class, () -> commandManager.getCommandTree().parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                Arrays.asList("test", "two"))));
    }

    @Test
    void getSuggestions() {
    }

    @Test
    void testGetSuggestions() {
    }

    @Test
    void insertCommand() {
    }

    @Test
    void verifyAndRegister() {
    }

}
