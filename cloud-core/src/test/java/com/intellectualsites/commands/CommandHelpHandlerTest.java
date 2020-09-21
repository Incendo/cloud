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

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class CommandHelpHandlerTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();
        final SimpleCommandMeta meta1 = SimpleCommandMeta.builder().with("description", "Command with only literals").build();
        manager.command(manager.commandBuilder("test", meta1).literal("this").literal("thing").build());
        final SimpleCommandMeta meta2 = SimpleCommandMeta.builder().with("description", "Command with variables").build();
        manager.command(manager.commandBuilder("test", meta2).literal("int").
                argument(IntegerArgument.required("int"), "A number").build());
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

    @Test
    void testHelpQuery() {
        final CommandHelpHandler.HelpTopic<TestCommandSender> query1 = manager.getCommandHelpHandler().queryHelp("");
        Assertions.assertTrue(query1 instanceof CommandHelpHandler.IndexHelpTopic);
        this.printTopic("", query1);
        final CommandHelpHandler.HelpTopic<TestCommandSender> query2 = manager.getCommandHelpHandler().queryHelp("test");
        Assertions.assertTrue(query2 instanceof CommandHelpHandler.MultiHelpTopic);
        this.printTopic("test", query2);
        final CommandHelpHandler.HelpTopic<TestCommandSender> query3 = manager.getCommandHelpHandler().queryHelp("test int");
        Assertions.assertTrue(query3 instanceof CommandHelpHandler.VerboseHelpTopic);
        this.printTopic("test int", query3);
    }

    private void printTopic(@Nonnull final String query,
                            @Nonnull final CommandHelpHandler.HelpTopic<TestCommandSender> helpTopic) {
        System.out.printf("Showing results for query: \"/%s\"\n", query);
        if (helpTopic instanceof CommandHelpHandler.IndexHelpTopic) {
            this.printIndexHelpTopic((CommandHelpHandler.IndexHelpTopic<TestCommandSender>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.MultiHelpTopic) {
            this.printMultiHelpTopic((CommandHelpHandler.MultiHelpTopic<TestCommandSender>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.VerboseHelpTopic) {
            this.printVerboseHelpTopic((CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) helpTopic);
        } else {
            throw new IllegalArgumentException("Unknown help topic type");
        }
        System.out.println();
    }

    private void printIndexHelpTopic(@Nonnull final CommandHelpHandler.IndexHelpTopic<TestCommandSender> helpTopic) {
        System.out.println("└── Available Commands: ");
        final Iterator<CommandHelpHandler.VerboseHelpEntry<TestCommandSender>> iterator = helpTopic.getEntries().iterator();
        while (iterator.hasNext()) {
            final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry = iterator.next();
            final String prefix = iterator.hasNext() ? "├──" : "└──";
            System.out.printf("    %s /%s: %s\n", prefix, entry.getSyntaxString(), entry.getDescription());
        }
    }

    private void printMultiHelpTopic(@Nonnull final CommandHelpHandler.MultiHelpTopic<TestCommandSender> helpTopic) {
        System.out.printf("└── /%s\n", helpTopic.getLongestPath());
        final int headerIndentation = helpTopic.getLongestPath().length();
        final Iterator<String> iterator = helpTopic.getChildSuggestions().iterator();
        while (iterator.hasNext()) {
            final String suggestion = iterator.next();
            final StringBuilder printBuilder = new StringBuilder();
            for (int i = 0; i < headerIndentation; i++) {
                printBuilder.append(' ');
            }
            if (iterator.hasNext()) {
                printBuilder.append("├── ");
            } else {
                printBuilder.append("└── ");
            }
            printBuilder.append(suggestion);
            System.out.println(printBuilder.toString());
        }
    }

    private void printVerboseHelpTopic(@Nonnull final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> helpTopic) {
        System.out.printf("└── Command: /%s\n", manager.getCommandSyntaxFormatter()
                                                      .apply(helpTopic.getCommand().getArguments(), null));
        System.out.printf("    ├── Description: %s\n", helpTopic.getDescription());
        System.out.println("    └── Args: ");
        final Iterator<CommandArgument<TestCommandSender, ?>> iterator = helpTopic.getCommand().getArguments().iterator();
        while (iterator.hasNext()) {
            final CommandArgument<TestCommandSender, ?> argument = iterator.next();

            String description = helpTopic.getCommand().getArgumentDescription(argument);
            if (!description.isEmpty()) {
                description = ": " + description;
            }

            System.out.printf("        %s %s%s\n", iterator.hasNext() ? "├──" : "└──", manager.getCommandSyntaxFormatter().apply(
                    Collections.singletonList(argument), null), description);
        }
    }

}
