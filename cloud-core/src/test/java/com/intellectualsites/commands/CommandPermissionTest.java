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

import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

class CommandPermissionTest {

    private final static CommandManager<TestCommandSender> manager = new PermissionOutputtingCommandManager();

    @BeforeAll
    static void setup() {
        manager.command(manager.commandBuilder("test").literal("foo").withPermission("test.permission.one").build());
        manager.command(manager.commandBuilder("test").literal("bar").withPermission("test.permission.two").build());
        manager.command(manager.commandBuilder("test").literal("fizz").withPermission("test.permission.three").build());
        manager.command(manager.commandBuilder("test").literal("buzz").withPermission("test.permission.four").build());
    }

    @Test
    void testCompoundPermission() {
        Assertions.assertTrue(manager.suggest(new TestCommandSender(), "").isEmpty());
    }

    private static final class PermissionOutputtingCommandManager extends CommandManager<TestCommandSender> {

        public PermissionOutputtingCommandManager() {
            super(CommandExecutionCoordinator.simpleCoordinator(), cmd -> true);
        }

        @Override
        public boolean hasPermission(@Nonnull final TestCommandSender sender,
                                     @Nonnull final String permission) {
            return false;
        }

        @Nonnull
        @Override
        public CommandMeta createDefaultCommandMeta() {
            return SimpleCommandMeta.empty();
        }
    }

}
