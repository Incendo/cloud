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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.types.tuples.Pair;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;

class CommandHelpHandlerTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = createManager();
        final SimpleCommandMeta meta1 = SimpleCommandMeta
                .builder()
                .with(CommandMeta.DESCRIPTION, "Command with only literals")
                .build();
        manager.command(manager.commandBuilder("test", meta1).literal("this").literal("thing").build());
        final SimpleCommandMeta meta2 = SimpleCommandMeta
                .builder()
                .with(CommandMeta.DESCRIPTION, "Command with variables")
                .build();
        manager.command(manager.commandBuilder("test", meta2).literal("int").
                required(IntegerArgument.of("int"), ArgumentDescription.of("A number")).build());
        manager.command(manager.commandBuilder("test").required(StringArgument.of("potato")));

        manager.command(manager.commandBuilder("vec")
                .meta(CommandMeta.DESCRIPTION, "Takes in a vector")
                .requiredArgumentPair("vec", Pair.of("x", "y"),
                        Pair.of(Double.class, Double.class), ArgumentDescription.of("Vector")
                )
                .build());
    }

    @Test
    void testVerboseHelp() {
        final List<CommandHelpHandler.VerboseHelpEntry<TestCommandSender>> syntaxHints
                = manager.createCommandHelpHandler().getAllCommands();
        final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry0 = syntaxHints.get(0);
        Assertions.assertEquals("test <potato>", entry0.getSyntaxString());
        final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry1 = syntaxHints.get(1);
        Assertions.assertEquals("test int <int>", entry1.getSyntaxString());
        final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry2 = syntaxHints.get(2);
        Assertions.assertEquals("test this thing", entry2.getSyntaxString());
    }

    @Test
    void testLongestChains() {
        final List<String> longestChains = manager.createCommandHelpHandler().getLongestSharedChains();
        Assertions.assertEquals(Arrays.asList("test int|this|<potato>", "vec <<x> <y>>"), longestChains);
    }

    @Test
    void testHelpQuery() {
        final CommandHelpHandler.HelpTopic<TestCommandSender> query1 = manager.createCommandHelpHandler().queryHelp("");
        Assertions.assertTrue(query1 instanceof CommandHelpHandler.IndexHelpTopic);
        this.printTopic("", query1);
        final CommandHelpHandler.HelpTopic<TestCommandSender> query2 = manager.createCommandHelpHandler().queryHelp("test");
        Assertions.assertTrue(query2 instanceof CommandHelpHandler.MultiHelpTopic);
        this.printTopic("test", query2);
        final CommandHelpHandler.HelpTopic<TestCommandSender> query3 = manager.createCommandHelpHandler().queryHelp("test int");
        Assertions.assertTrue(query3 instanceof CommandHelpHandler.VerboseHelpTopic);
        this.printTopic("test int", query3);
        final CommandHelpHandler.HelpTopic<TestCommandSender> query4 = manager.createCommandHelpHandler().queryHelp("vec");
        Assertions.assertTrue(query4 instanceof CommandHelpHandler.VerboseHelpTopic);
        this.printTopic("vec", query4);
    }

    @Test
    void testPredicateFilter() {
        /*
         * This predicate only displays the commands starting with /test
         * The one command ending in 'thing' is excluded as well, for complexity
         */
        final Predicate<Command<TestCommandSender>> predicate = (command) -> command.toString().startsWith("test ")
                && !command.toString().endsWith(" thing");

        /*
         * List all commands from root, which should show only:
         * - /test <potato>
         * - /test int <int>
         */
        final CommandHelpHandler.HelpTopic<TestCommandSender> query1 = manager.createCommandHelpHandler(predicate).queryHelp("");
        Assertions.assertTrue(query1 instanceof CommandHelpHandler.IndexHelpTopic);
        Assertions.assertEquals(Arrays.asList("test <potato>", "test int <int>"), getSortedSyntaxStrings(query1));

        /*
         * List all commands from /test, which should show only:
         * - /test <potato>
         * - /test int <int>
         */
        final CommandHelpHandler.HelpTopic<TestCommandSender> query2 = manager.createCommandHelpHandler(predicate).queryHelp("test");
        Assertions.assertTrue(query2 instanceof CommandHelpHandler.MultiHelpTopic);
        Assertions.assertEquals(Arrays.asList("test <potato>", "test int <int>"), getSortedSyntaxStrings(query2));

        /*
         * List all commands from /test int, which should show only:
         * - /test int <int>
         */
        final CommandHelpHandler.HelpTopic<TestCommandSender> query3 = manager.createCommandHelpHandler(predicate).queryHelp(
                "test int");
        Assertions.assertTrue(query3 instanceof CommandHelpHandler.VerboseHelpTopic);
        Assertions.assertEquals(Collections.singletonList("test int <int>"), getSortedSyntaxStrings(query3));

        /*
         * List all commands from /vec, which should show none
         */
        final CommandHelpHandler.HelpTopic<TestCommandSender> query4 = manager.createCommandHelpHandler(predicate).queryHelp("vec");
        Assertions.assertTrue(query4 instanceof CommandHelpHandler.IndexHelpTopic);
        Assertions.assertEquals(Collections.emptyList(), getSortedSyntaxStrings(query4));
    }

    /* Lists all the syntax strings of the commands displayed in a help topic */
    private List<String> getSortedSyntaxStrings(
            final CommandHelpHandler.HelpTopic<TestCommandSender> helpTopic
    ) {
        if (helpTopic instanceof CommandHelpHandler.IndexHelpTopic) {
            CommandHelpHandler.IndexHelpTopic<TestCommandSender> index =
                    (CommandHelpHandler.IndexHelpTopic<TestCommandSender>) helpTopic;

            return index.getEntries().stream()
                    .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString)
                    .sorted()
                    .collect(Collectors.toList());
        } else if (helpTopic instanceof CommandHelpHandler.MultiHelpTopic) {
            CommandHelpHandler.MultiHelpTopic<TestCommandSender> multi =
                    (CommandHelpHandler.MultiHelpTopic<TestCommandSender>) helpTopic;

            return multi.getChildSuggestions().stream()
                    .sorted()
                    .collect(Collectors.toList());
        } else if (helpTopic instanceof CommandHelpHandler.VerboseHelpTopic) {
            CommandHelpHandler.VerboseHelpTopic<TestCommandSender> verbose =
                    (CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) helpTopic;

            //TODO: Use CommandManager syntax for this
            StringBuilder syntax = new StringBuilder();
            for (CommandComponent<TestCommandSender> component : verbose.getCommand().components()) {
                if (component.argument() instanceof StaticArgument) {
                    syntax.append(component.name());
                } else if (component.required()) {
                    syntax.append('<').append(component.name()).append('>');
                } else {
                    syntax.append('[').append(component.name()).append(']');
                }
                syntax.append(' ');
            }
            syntax.setLength(syntax.length() - 1);
            return Collections.singletonList(syntax.toString());
        }

        /* Dunno */
        return Collections.emptyList();
    }

    private void printTopic(
            final String query,
            final CommandHelpHandler.HelpTopic<TestCommandSender> helpTopic
    ) {
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

    private void printIndexHelpTopic(final CommandHelpHandler.IndexHelpTopic<TestCommandSender> helpTopic) {
        System.out.println("└── Available Commands: ");
        final Iterator<CommandHelpHandler.VerboseHelpEntry<TestCommandSender>> iterator = helpTopic.getEntries().iterator();
        while (iterator.hasNext()) {
            final CommandHelpHandler.VerboseHelpEntry<TestCommandSender> entry = iterator.next();
            final String prefix = iterator.hasNext() ? "├──" : "└──";
            System.out.printf("    %s /%s: %s\n", prefix, entry.getSyntaxString(), entry.getDescription());
        }
    }

    private void printMultiHelpTopic(final CommandHelpHandler.MultiHelpTopic<TestCommandSender> helpTopic) {
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
            System.out.println(printBuilder);
        }
    }

    private void printVerboseHelpTopic(final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> helpTopic) {
        System.out.printf("└── Command: /%s\n", manager.commandSyntaxFormatter()
                .apply(helpTopic.getCommand().components(), null));
        System.out.printf("    ├── Description: %s\n", helpTopic.getDescription());
        System.out.println("    └── Args: ");
        final Iterator<CommandComponent<TestCommandSender>> iterator = helpTopic.getCommand().components().iterator();
        while (iterator.hasNext()) {
            final CommandComponent<TestCommandSender> component = iterator.next();

            String description = component.argumentDescription().getDescription();
            if (!description.isEmpty()) {
                description = ": " + description;
            }

            System.out.printf("        %s %s%s\n", iterator.hasNext() ? "├──" : "└──", manager.commandSyntaxFormatter().apply(
                    Collections.singletonList(component), null), description);
        }
    }
}
