//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSuggestionsTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setupManager() {
        manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test", "testalias").literal("one").build());
        manager.command(manager.commandBuilder("test").literal("two").build());
        manager.command(manager.commandBuilder("test")
                .literal("var")
                .argument(StringArgument.<TestCommandSender>newBuilder("str")
                        .withSuggestionsProvider((c, s) -> Arrays.asList("one", "two")))
                .argument(EnumArgument.of(TestEnum.class, "enum")));
        manager.command(manager.commandBuilder("test")
                .literal("comb")
                .argument(StringArgument.<TestCommandSender>newBuilder("str")
                        .withSuggestionsProvider((c, s) -> Arrays.asList("one", "two")))
                .argument(IntegerArgument.<TestCommandSender>newBuilder("num")
                        .withMin(1).withMax(95).asOptional()));
        manager.command(manager.commandBuilder("test")
                .literal("alt")
                .argument(IntegerArgument.<TestCommandSender>newBuilder("num")
                        .withSuggestionsProvider((c, s) -> Arrays.asList("3", "33", "333"))));

        manager.command(manager.commandBuilder("com")
                .argumentPair("com", Pair.of("x", "y"), Pair.of(Integer.class, TestEnum.class),
                        ArgumentDescription.empty()
                )
                .argument(IntegerArgument.of("int")));

        manager.command(manager.commandBuilder("com2")
                .argumentPair("com", Pair.of("x", "enum"),
                        Pair.of(Integer.class, TestEnum.class), ArgumentDescription.empty()
                ));

        manager.command(manager.commandBuilder("flags")
                .argument(IntegerArgument.of("num"))
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
                        .withArgument(ArgumentTriplet.of(manager, "triplet",
                                Triplet.of("x", "y", "z"),
                                Triplet.of(int.class, int.class, int.class))
                                .simple()))
                .flag(manager.flagBuilder("presence").withAliases("p"))
                .flag(manager.flagBuilder("single")
                        .withArgument(IntegerArgument.of("value"))));

        manager.command(manager.commandBuilder("numbers").argument(IntegerArgument.of("num")));
        manager.command(manager.commandBuilder("numberswithfollowingargument").argument(IntegerArgument.of("num"))
                .argument(BooleanArgument.of("another_argument")));
        manager.command(manager.commandBuilder("numberswithmin")
                .argument(IntegerArgument.<TestCommandSender>newBuilder("num").withMin(5).withMax(100)));

        manager.command(manager.commandBuilder("partial")
                .argument(StringArgument.<TestCommandSender>newBuilder("arg").withSuggestionsProvider((contect, input) -> {
                    return Arrays.asList("hi", "hey", "heya", "hai", "hello");
                }))
                .literal("literal")
                .build());

        manager.command(manager.commandBuilder("literal_with_variable")
                .argument(StringArgument.<TestCommandSender>newBuilder("arg").withSuggestionsProvider((context, input) -> {
                    return Arrays.asList("veni", "vidi");
                }).build())
                .literal("now"));
        manager.command(manager.commandBuilder("literal_with_variable")
                .literal("vici")
                .literal("later"));
    }

    @Test
    void testRootAliases() {
        final String input = "test ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        final String input2 = "testalias ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(suggestions, suggestions2);
    }

    @Test
    void testSimple() {
        final String input = "test";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
        final String input2 = "test ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("alt", "comb", "one", "two", "var"), suggestions2);
        final String input3 = "test a";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.singletonList("alt"), suggestions3);
    }

    @Test
    void testVar() {
        final String input = "test var";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
        final String input2 = "test var one";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Collections.singletonList("one"), suggestions2);
        final String input3 = "test var one f";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.singletonList("foo"), suggestions3);
        final String input4 = "test var one ";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Arrays.asList("foo", "bar"), suggestions4);
    }

    @Test
    void testEmpty() {
        final String input = "kenny";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
    }

    @Test
    void testComb() {
        final String input = "test comb ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("one", "two"), suggestions);
        final String input2 = "test comb one ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions2);
        final String input3 = "test comb one 9";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("9", "90", "91", "92", "93", "94", "95"), suggestions3);
    }

    @Test
    void testAltered() {
        final String input = "test alt ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("3", "33", "333"), suggestions);
    }

    @Test
    void testCompound() {
        final String input = "com ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "com 1 ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("foo", "bar"), suggestions2);
        final String input3 = "com 1 foo ";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions3);
        final String input4 = "com2 1 ";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Arrays.asList("foo", "bar"), suggestions4);
    }

    @Test
    void testFlags() {
        final String input = "flags 10 ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("--enum", "--static"), suggestions);
        final String input2 = "flags 10 --enum ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("foo", "bar"), suggestions2);
        final String input3 = "flags 10 --enum foo ";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.singletonList("--static"), suggestions3);
        final String input4 = "flags2 ";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Arrays.asList("--first", "--second", "--third", "-f", "-s", "-t"), suggestions4);
        final String input5 = "flags2 -f";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Arrays.asList("-fs", "-ft", "-f"), suggestions5);
        final String input6 = "flags2 -f -s";
        final List<String> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Arrays.asList("-st", "-s"), suggestions6);

        /* When an incorrect flag is specified, should resolve to listing flags */
        final String input7 = "flags2 --invalid ";
        final List<String> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Arrays.asList("--first", "--second", "--third", "-f", "-s", "-t"), suggestions7);
    }

    @Test
    void testCompoundFlags() {
        final String input = "flags3 ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("--compound", "--presence", "--single", "-p"), suggestions);

        final String input2 = "flags3 --c";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("--compound"), suggestions2);

        final String input3 = "flags3 --compound ";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions3);

        final String input4 = "flags3 --compound 1";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Arrays.asList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions4);

        final String input5 = "flags3 --compound 22 ";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions5);

        final String input6 = "flags3 --compound 22 1";
        final List<String> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Arrays.asList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions6);

        /* We've typed compound already, so that flag should be omitted from the suggestions */
        final String input7 = "flags3 --compound 22 33 44 ";
        final List<String> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Arrays.asList("--presence", "--single", "-p"), suggestions7);

        final String input8 = "flags3 --compound 22 33 44 --pres";
        final List<String> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(Arrays.asList("--presence"), suggestions8);

        final String input9 = "flags3 --compound 22 33 44 --presence ";
        final List<String> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertEquals(Arrays.asList("--single"), suggestions9);

        final String input10 = "flags3 --compound 22 33 44 --single ";
        final List<String> suggestions10 = manager.suggest(new TestCommandSender(), input10);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions10);
    }

    @Test
    void testNumbers() {
        final String input = "numbers ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "numbers 1";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions2);
        final String input3 = "numbers -";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9"), suggestions3);
        final String input4 = "numbers -1";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(
                Arrays.asList("-1", "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19"),
                suggestions4
        );
        final String input5 = "numberswithmin ";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Arrays.asList("5", "6", "7", "8", "9"), suggestions5);
    }

    @Test
    void testNumbersWithFollowingArguments() {
        final String input = "numberswithfollowingargument ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);
        final String input2 = "numberswithfollowingargument 1";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("1", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"), suggestions2);
        final String input3 = "numberswithfollowingargument -";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9"), suggestions3);
        final String input4 = "numberswithfollowingargument -1";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(
                Arrays.asList("-1", "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19"),
                suggestions4
        );
    }

    @Test
    void testInvalidLiteralThenSpace() {
        final String input = "test o";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Collections.singletonList("one"), suggestions);
        final String input2 = "test o ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Collections.emptyList(), suggestions2);
        final String input3 = "test o abc123xyz";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
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
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Collections.emptyList(), suggestions);
        final String input2 = "partial ";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("hi", "hey", "heya", "hai", "hello"), suggestions2);
        final String input3 = "partial h";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("hi", "hey", "heya", "hai", "hello"), suggestions3);
        final String input4 = "partial he";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Arrays.asList("hey", "heya", "hello"), suggestions4);
        final String input5 = "partial hey";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Arrays.asList("hey", "heya"), suggestions5);
        final String input6 = "partial hi";
        final List<String> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Collections.singletonList("hi"), suggestions6);
        final String input7 = "partial b";
        final List<String> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Collections.emptyList(), suggestions7);
        final String input8 = "partial hello ";
        final List<String> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(Collections.singletonList("literal"), suggestions8);
        final String input9 = "partial bonjour ";
        final List<String> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertEquals(Collections.singletonList("literal"), suggestions9);
    }

    void testLiteralWithVariable() {
        final String input = "literal_with_variable ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("vici", "veni", "vidi"), suggestions);
        final String input2 = "literal_with_variable v";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("vici", "veni", "vidi"), suggestions2);
        final String input3 = "literal_with_variable vi";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Arrays.asList("vici", "vidi"), suggestions3);
        final String input4 = "literal_with_variable vidi";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertEquals(Collections.emptyList(), suggestions4);
        final String input5 = "literal_with_variable vidi ";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Collections.singletonList("now"), suggestions5);
        final String input6 = "literal_with_variable vici ";
        final List<String> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Collections.singletonList("later"), suggestions6);
    }

    public enum TestEnum {
        FOO,
        BAR
    }

}
