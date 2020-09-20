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

import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class CommandHelpHandlerTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test").literal("this").literal("thing").build());
        manager.command(manager.commandBuilder("test").literal("int").
                argument(IntegerArgument.required("int")).build());
    }

    @Test
    void testVerboseHelp() {
        final List<CommandHelpHandler.VerboseHelpEntry<TestCommandSender>> syntaxHints
                = manager.getCommandHelpHandler().getAllCommands();
        final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry1 = syntaxHints.get(0);
        Assertions.assertEquals("test int <int>", entry1.getSyntaxString());
        final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry2 = syntaxHints.get(1);
        Assertions.assertEquals("test this thing", entry2.getSyntaxString());
    }

    @Test
    void testLongestChains() {
        final List<String> longestChains = manager.getCommandHelpHandler().getLongestSharedChains();
        Assertions.assertEquals(Arrays.asList("test int|this"), longestChains);
    }

}
