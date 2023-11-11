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

import cloud.commandframework.arguments.compound.ArgumentTriplet;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.DurationArgument;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.ArgumentTestHelper.suggestionList;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class CommandSuggestionsTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setupManager() {
        manager = createManager();
        manager.command(manager.commandBuilder("test", "testalias").literal("one").build());
        manager.command(manager.commandBuilder("test").literal("two").build());
        manager.command(manager.commandBuilder("test")
                .literal("var")
                .required(StringArgument.<TestCommandSender>builder("str")
                        .withSuggestionProvider((c, s) -> suggestionList("one", "two")))
                .required(EnumArgument.of(TestEnum.class, "enum")));
        manager.command(manager.commandBuilder("test")
                .literal("comb")
                .required(StringArgument.<TestCommandSender>builder("str")
                        .withSuggestionProvider((c, s) -> suggestionList("one", "two")))
                .optional(IntegerArgument.<TestCommandSender>builder("num")
                        .withMin(1).withMax(95)));
        manager.command(manager.commandBuilder("test")
                .literal("alt")
                .required(IntegerArgument.<TestCommandSender>builder("num")
                        .withSuggestionProvider((c, s) -> suggestionList("3", "33", "333"))));

        manager.command(manager.commandBuilder("com")
                .requiredArgumentPair("com", Pair.of("x", "y"), Pair.of(Integer.class, TestEnum.class),
                        ArgumentDescription.empty()
                )
                .required(IntegerArgument.of("int")));

        manager.command(manager.commandBuilder("com2")
                .requiredArgumentPair("com", Pair.of("x", "enum"),
                        Pair.of(Integer.class, TestEnum.class), ArgumentDescription.empty()
                ));

        manager.command(manager.commandBuilder("flags")
                .required(IntegerArgument.of("num"))
                .flag(manager.flagBuilder("enum")
                        .withArgument(EnumArgument.of(TestEnum.class, "enum"))
                        .build())
                .flag(manager.flagBuilder("static")
                        .build())
                .build());

        manager.command(manager.commandBuilder("flags2")
                .flag(manager.flagBuilder("first").withAliases("f"))
                .flag(manager.flagBuilder("second").withAliases("s"))
                .flag(manager.flagBuilder("third").withAliases("t"))
                .build());

        manager.command(manager.commandBuilder("flags3")
                .flag(manager.flagBuilder("compound")
                        .withArgument(
                                ArgumentTriplet.of(manager, "triplet",
                                        Triplet.of("x", "y", "z"),
                                        Triplet.of(int.class, int.class, int.class)
                                ).simple()
                        )
                )
                .flag(manager.flagBuilder("presence").withAliases("p"))
                .flag(manager.flagBuilder("single")
                        .withArgument(IntegerArgument.of("value"))));

        manager.command(manager.commandBuilder("numbers").required(IntegerArgument.of("num")));
        manager.command(manager.commandBuilder("numberswithfollowingargument").required(IntegerArgument.of("num"))
                .required(BooleanArgument.of("another_argument")));
        manager.command(manager.commandBuilder("numberswithmin")
                .required(IntegerArgument.<TestCommandSender>builder("num").withMin(5).withMax(100)));

        manager.command(manager.commandBuilder("duration").required(DurationArgument.of("duration")));

        manager.command(manager.commandBuilder("partial")
                .required(
                        StringArgument.<TestCommandSender>builder("arg")
                                .withSuggestionProvider((contect, input) -> suggestionList("hi", "hey", "heya", "hai", "hello"))
                )
                .literal("literal")
                .build());

        manager.command(manager.commandBuilder("literal_with_variable")
                .required(
                        StringArgument.<TestCommandSender>builder("arg")
                                .withSuggestionProvider((context, input) -> suggestionList("veni", "vidi")).build()
                )
                .literal("now"));
        manager.command(manager.commandBuilder("literal_with_variable")
                .literal("vici")
                .literal("later"));

        manager.command(manager.commandBuilder("cmd_with_multiple_args")
                .required(IntegerArgument.<TestCommandSender>of("number").addPreprocessor((ctx, input) -> {
                    String argument = input.peek();
                    if (argument == null || !argument.equals("1024")) {
                        return ArgumentParseResult.success(true);
                    } else {
                        return ArgumentParseResult.failure(new NullPointerException());
                    }
                }))
                .required(EnumArgument.of(TestEnum.class, "enum"))
                .literal("world"));
    }

    @Test
    void testRootAliases() {
        final String input = "test ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        final String input2 = "testalias ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestions, suggestions2);
    }

    @Test
    void testSimple() {
        final String input = "test";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
        final String input2 = "test ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("alt", "comb", "one", "two", "var"), suggestions2);
        final String input3 = "test a";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("alt"), suggestions3);
    }

    @Test
    void testVar() {
        final String input = "test var";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
        final String input2 = "test var one";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("one"), suggestions2);
        final String input3 = "test var one f";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("foo"), suggestions3);
        final String input4 = "test var one ";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("foo", "bar"), suggestions4);
    }

    @Test
    void testEmpty() {
        final String input = "kenny";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
    }

    @Test
    void testComb() {
        final String input = "test comb ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("one", "two"), suggestions);
        final String input2 = "test comb one ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions2);
        final String input3 = "test comb one 9";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("9", "90", "91", "92", "93", "94", "95"), suggestions3);
    }

    @Test
    void testAltered() {
        final String input = "test alt ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("3", "33", "333"), suggestions);
    }

    @Test
    void testCompound() {
        final String input = "com ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "com 1 ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("foo", "bar"), suggestions2);
        final String input3 = "com 1 foo ";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions3);
        final String input4 = "com2 1 ";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("foo", "bar"), suggestions4);
    }

    @Test
    void testFlags() {
        final String input = "flags 10 ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("--enum", "--static"), suggestions);
        final String input2 = "flags 10 --enum ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("foo", "bar"), suggestions2);
        final String input3 = "flags 10 --enum foo ";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("--static"), suggestions3);
        final String input4 = "flags2 ";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("--first", "--second", "--third", "-f", "-s", "-t"), suggestions4);
        final String input5 = "flags2 -f";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(suggestionList("-fs", "-ft", "-f"), suggestions5);
        final String input6 = "flags2 -f -s";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(suggestionList("-st", "-s"), suggestions6);

        /* When an incorrect flag is specified, should resolve to listing flags */
        final String input7 = "flags2 --invalid ";
        final List<Suggestion> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(suggestionList("--first", "--second", "--third", "-f", "-s", "-t"), suggestions7);
    }

    @Test
    void testCompoundFlags() {
        final String input = "flags3 ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("--compound", "--presence", "--single", "-p"), suggestions);

        final String input2 = "flags3 --c";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("--compound"), suggestions2);

        final String input3 = "flags3 --compound ";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions3);

        final String input4 = "flags3 --compound 1";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions4);

        final String input5 = "flags3 --compound 22 ";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions5);

        final String input6 = "flags3 --compound 22 1";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(suggestionList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions6);

        /* We've typed compound already, so that flag should be omitted from the suggestions */
        final String input7 = "flags3 --compound 22 33 44 ";
        final List<Suggestion> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(suggestionList("--presence", "--single", "-p"), suggestions7);

        final String input8 = "flags3 --compound 22 33 44 --pres";
        final List<Suggestion> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(suggestionList("--presence"), suggestions8);

        final String input9 = "flags3 --compound 22 33 44 --presence ";
        final List<Suggestion> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertEquals(suggestionList("--single"), suggestions9);

        final String input10 = "flags3 --compound 22 33 44 --single ";
        final List<Suggestion> suggestions10 = manager.suggest(new TestCommandSender(), input10);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions10);
    }

    @Test
    void testNumbers() {
        final String input = "numbers ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "numbers 1";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions2);
        final String input3 = "numbers -";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9"), suggestions3);
        final String input4 = "numbers -1";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(
                suggestionList("-1", "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19"),
                suggestions4
        );
        final String input5 = "numberswithmin ";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(suggestionList("5", "6", "7", "8", "9"), suggestions5);

        final String input6 = "numbers 1 ";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Collections.emptyList(), suggestions6);
    }

    @Test
    void testNumbersWithFollowingArguments() {
        final String input = "numberswithfollowingargument ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "numberswithfollowingargument 1";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions2);
        final String input3 = "numberswithfollowingargument -";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9"), suggestions3);
        final String input4 = "numberswithfollowingargument -1";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(
                suggestionList("-1", "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19"),
                suggestions4
        );
    }

    @Test
    void testDurations() {
        final String input = "duration ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "duration 5";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("5d", "5h", "5m", "5s"), suggestions2);
        final String input3 = "duration 5s";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.emptyList(), suggestions3);
        final String input4 = "duration 5s ";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Collections.emptyList(), suggestions4);
    }

    @Test
    void testInvalidLiteralThenSpace() {
        final String input = "test o";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("one"), suggestions);
        final String input2 = "test o ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Collections.emptyList(), suggestions2);
        final String input3 = "test o abc123xyz";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.emptyList(), suggestions3);
    }

    @Test
    void testStringArgumentWithSuggestionProvider() {
        /*
         * [/partial] - should not match anything
         * [/partial ] - should show all possible suggestions unsorted
         * [/partial h] - should show all starting with 'h' (which is all) unsorted
         * [/partial he] - should show only those starting with he, unsorted
         * [/partial hey] - should show 'hey' and 'heya' (matches exactly and starts with)
         * [/partial hi] - should show only 'hi', it is the only one that matches exactly
         * [/partial b] - should show no suggestions, none match
         * [/partial hello ] - should show the literal following the argument (suggested)
         * [/partial bonjour ] - should show the literal following the argument (not suggested)
         */
        final String input = "partial";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Collections.emptyList(), suggestions);
        final String input2 = "partial ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("hi", "hey", "heya", "hai", "hello"), suggestions2);
        final String input3 = "partial h";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("hi", "hey", "heya", "hai", "hello"), suggestions3);
        final String input4 = "partial he";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("hey", "heya", "hello"), suggestions4);
        final String input5 = "partial hey";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(suggestionList("hey", "heya"), suggestions5);
        final String input6 = "partial hi";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(suggestionList("hi"), suggestions6);
        final String input7 = "partial b";
        final List<Suggestion> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Collections.emptyList(), suggestions7);
        final String input8 = "partial hello ";
        final List<Suggestion> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(suggestionList("literal"), suggestions8);
        final String input9 = "partial bonjour ";
        final List<Suggestion> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertEquals(suggestionList("literal"), suggestions9);
    }

    @Test
    void testLiteralWithVariable() {
        final String input = "literal_with_variable ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("vici", "veni", "vidi"), suggestions);
        final String input2 = "literal_with_variable v";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("vici", "veni", "vidi"), suggestions2);
        final String input3 = "literal_with_variable vi";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(suggestionList("vici", "vidi"), suggestions3);
        final String input4 = "literal_with_variable vidi";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("vidi"), suggestions4);
        final String input5 = "literal_with_variable vidi ";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(suggestionList("now"), suggestions5);
        final String input6 = "literal_with_variable vici ";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(suggestionList("later"), suggestions6);
    }

    @Test
    void testInvalidArgumentShouldNotCauseFurtherCompletion() {
        // pass preprocess
        final String input = "cmd_with_multiple_args 512 ";
        final List<Suggestion> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(suggestionList("foo", "bar"), suggestions);
        final String input2 = "cmd_with_multiple_args 512 BAR ";
        final List<Suggestion> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestionList("world"), suggestions2);
        final String input3 = "cmd_with_multiple_args test ";
        final List<Suggestion> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.emptyList(), suggestions3);
        final String input4 = "cmd_with_multiple_args 512 f";
        final List<Suggestion> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(suggestionList("foo"), suggestions4);
        final String input5 = "cmd_with_multiple_args world f";
        final List<Suggestion> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Collections.emptyList(), suggestions5);
        // trigger preprocess fail
        final String input6 = "cmd_with_multiple_args 1024";
        final List<Suggestion> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(11, suggestions6.size());
        final String input7 = "cmd_with_multiple_args 1024 ";
        final List<Suggestion> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Collections.emptyList(), suggestions7);
        final String input8 = "cmd_with_multiple_args 1024 f";
        final List<Suggestion> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(Collections.emptyList(), suggestions8);
        final String input9 = "cmd_with_multiple_args 1024 foo w";
        final List<Suggestion> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertEquals(Collections.emptyList(), suggestions9);
    }

    @Test
    void testFlagYieldingGreedyStringFollowedByFlagArgument() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.command(
                manager.commandBuilder("command")
                        .required(
                                StringArgument.<TestCommandSender>builder("string")
                                        .greedyFlagYielding()
                                        .withSuggestionProvider((context, input) -> suggestionList("hello"))
                                        .build()
                        ).flag(manager.flagBuilder("flag").withAliases("f").build())
                        .flag(manager.flagBuilder("flag2").build())
        );

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command hel");
        final List<Suggestion> suggestions3 = suggest(manager, "command hello --");
        final List<Suggestion> suggestions4 = suggest(manager, "command hello --f");
        final List<Suggestion> suggestions5 = suggest(manager, "command hello -f");
        final List<Suggestion> suggestions6 = suggest(manager, "command hello -");

        // Assert
        assertThat(suggestions1).containsExactlyElementsIn(suggestionList("hello"));
        assertThat(suggestions2).containsExactlyElementsIn(suggestionList("hello"));
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("-f"));
        assertThat(suggestions6).isEmpty();
    }

    @Test
    void testFlagYieldingStringArrayFollowedByFlagArgument() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.command(
                manager.commandBuilder("command")
                        .required(
                                StringArrayArgument.of(
                                        "array",
                                        true,
                                        (context, input) -> Collections.emptyList()
                                )
                        ).flag(manager.flagBuilder("flag").withAliases("f").build())
                        .flag(manager.flagBuilder("flag2").build())
        );

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command hello");
        final List<Suggestion> suggestions3 = suggest(manager, "command hello --");
        final List<Suggestion> suggestions4 = suggest(manager, "command hello --f");
        final List<Suggestion> suggestions5 = suggest(manager, "command hello -f");
        final List<Suggestion> suggestions6 = suggest(manager, "command hello -");

        // Assert
        assertThat(suggestions1).isEmpty();
        assertThat(suggestions2).isEmpty();
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("-f"));
        assertThat(suggestions6).isEmpty();
    }

    @Test
    void testGreedyArgumentSuggestsAfterSpace() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.command(
                manager.commandBuilder("command")
                        .required(
                                StringArgument.<TestCommandSender>builder("string")
                                        .greedy()
                                        .withSuggestionProvider((context, input) -> suggestionList("hello world"))
                                        .build())
        );
        manager.commandSuggestionProcessor(
                new FilteringCommandSuggestionProcessor<>(
                        FilteringCommandSuggestionProcessor.Filter.<TestCommandSender>startsWith(true).andTrimBeforeLastSpace()));

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command hello");
        final List<Suggestion> suggestions3 = suggest(manager, "command hello ");
        final List<Suggestion> suggestions4 = suggest(manager, "command hello wo");
        final List<Suggestion> suggestions5 = suggest(manager, "command hello world");
        final List<Suggestion> suggestions6 = suggest(manager, "command hello world ");

        // Assert
        assertThat(suggestions1).containsExactlyElementsIn(suggestionList("hello world"));
        assertThat(suggestions2).containsExactlyElementsIn(suggestionList("hello world"));
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("world"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("world"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("world"));
        assertThat(suggestions6).isEmpty();
    }

    @Test
    void testFlagYieldingGreedyStringWithLiberalFlagArgument() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.setSetting(CommandManager.ManagerSettings.LIBERAL_FLAG_PARSING, true);
        manager.command(
                manager.commandBuilder("command")
                        .required(
                                StringArgument.<TestCommandSender>builder("string")
                                        .greedyFlagYielding()
                                        .withSuggestionProvider((context, input) -> suggestionList("hello"))
                                        .build()
                        ).flag(manager.flagBuilder("flag").withAliases("f").build())
                        .flag(manager.flagBuilder("flag2").build())
        );

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command hel");
        final List<Suggestion> suggestions3 = suggest(manager, "command hello --");
        final List<Suggestion> suggestions4 = suggest(manager, "command hello --f");
        final List<Suggestion> suggestions5 = suggest(manager, "command hello -f");
        final List<Suggestion> suggestions6 = suggest(manager, "command hello -");

        // Assert
        assertThat(suggestions1).containsExactlyElementsIn(suggestionList("hello", "--flag", "--flag2", "-f"));
        assertThat(suggestions2).containsExactlyElementsIn(suggestionList("hello"));
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("-f"));
        assertThat(suggestions6).isEmpty();
    }

    @Test
    void testFlagYieldingStringArrayWithLiberalFlagArgument() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.setSetting(CommandManager.ManagerSettings.LIBERAL_FLAG_PARSING, true);
        manager.command(
                manager.commandBuilder("command")
                        .required(
                                StringArrayArgument.of(
                                        "array",
                                        true,
                                        (context, input) -> Collections.emptyList()
                                )
                        ).flag(manager.flagBuilder("flag").withAliases("f").build())
                        .flag(manager.flagBuilder("flag2").build())
        );

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command hello");
        final List<Suggestion> suggestions3 = suggest(manager, "command hello --");
        final List<Suggestion> suggestions4 = suggest(manager, "command hello --f");
        final List<Suggestion> suggestions5 = suggest(manager, "command hello -f");
        final List<Suggestion> suggestions6 = suggest(manager, "command hello -");

        // Assert
        assertThat(suggestions1).containsExactlyElementsIn(suggestionList("--flag", "--flag2", "-f"));
        assertThat(suggestions2).isEmpty();
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("--flag", "--flag2"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("-f"));
        assertThat(suggestions6).isEmpty();
    }

    @Test
    void testTextFlagCompletion() {
        // Arrange
        final CommandManager<TestCommandSender> manager = createManager();
        manager.setSetting(CommandManager.ManagerSettings.LIBERAL_FLAG_PARSING, true);
        manager.command(
                manager.commandBuilder("command")
                        .flag(manager.flagBuilder("flag").withAliases("f")
                                .withArgument(EnumArgument.of(TestEnum.class, "test")).build())
                        .flag(manager.flagBuilder("flog").build())
        );

        // Act
        final List<Suggestion> suggestions1 = suggest(manager, "command ");
        final List<Suggestion> suggestions2 = suggest(manager, "command --");
        final List<Suggestion> suggestions3 = suggest(manager, "command --f");
        final List<Suggestion> suggestions4 = suggest(manager, "command --fla");
        final List<Suggestion> suggestions5 = suggest(manager, "command -f");
        final List<Suggestion> suggestions6 = suggest(manager, "command -");

        final List<Suggestion> suggestions7 = suggest(manager, "command -f ");
        final List<Suggestion> suggestions8 = suggest(manager, "command -f b");

        // Assert
        assertThat(suggestions1).containsExactlyElementsIn(suggestionList("--flag", "--flog", "-f"));
        assertThat(suggestions2).containsExactlyElementsIn(suggestionList("--flag", "--flog"));
        assertThat(suggestions3).containsExactlyElementsIn(suggestionList("--flag", "--flog"));
        assertThat(suggestions4).containsExactlyElementsIn(suggestionList("--flag"));
        assertThat(suggestions5).containsExactlyElementsIn(suggestionList("-f"));
        assertThat(suggestions6).containsExactlyElementsIn(suggestionList("--flag", "--flog", "-f"));
        assertThat(suggestions7).containsExactlyElementsIn(suggestionList("foo", "bar"));
        assertThat(suggestions8).containsExactlyElementsIn(suggestionList("bar"));
    }


    private List<Suggestion> suggest(CommandManager<TestCommandSender> manager, String command) {
        return manager.suggest(new TestCommandSender(), command);
    }

    public enum TestEnum {
        FOO,
        BAR
    }
}
