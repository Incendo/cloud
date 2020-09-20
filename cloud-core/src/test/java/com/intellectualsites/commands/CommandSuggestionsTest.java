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

import com.intellectualsites.commands.arguments.standard.EnumArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.arguments.standard.StringArgument;
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
        manager.command(manager.commandBuilder("test").literal("one").build());
        manager.command(manager.commandBuilder("test").literal("two").build());
        manager.command(manager.commandBuilder("test")
                               .literal("var")
                               .argument(StringArgument.<TestCommandSender>newBuilder("str")
                                                 .withSuggestionsProvider((c, s) -> Arrays.asList("one", "two"))
                                                 .build())
                               .argument(EnumArgument.required(TestEnum.class, "enum"))
                               .build());
        manager.command(manager.commandBuilder("test")
                               .literal("comb")
                               .argument(StringArgument.<TestCommandSender>newBuilder("str")
                                                 .withSuggestionsProvider((c, s) -> Arrays.asList("one", "two"))
                                                 .build())
                               .argument(IntegerArgument.<TestCommandSender>newBuilder("num")
                                                 .withMin(1).withMax(95).asOptional().build())
                               .build());
        manager.command(manager.commandBuilder("test")
                               .literal("alt")
                               .argument(IntegerArgument.<TestCommandSender>newBuilder("num")
                                                 .withSuggestionsProvider((c, s) -> Arrays.asList("3", "33", "333")).build())
                               .build());
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
        Assertions.assertEquals(Collections.emptyList(), suggestions2);
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
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions2);
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


    public enum TestEnum {
        FOO,
        BAR
    }

}
