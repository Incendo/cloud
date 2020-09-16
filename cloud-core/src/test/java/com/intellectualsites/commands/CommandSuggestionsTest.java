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
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.sender.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSuggestionsTest {

    private static CommandManager<CommandSender, SimpleCommandMeta> manager;

    @BeforeAll
    static void setupManager() {
        manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test").component(StaticComponent.required("one")).build());
        manager.command(manager.commandBuilder("test").component(StaticComponent.required("two")).build());
        manager.command(manager.commandBuilder("test")
                               .component(StaticComponent.required("var"))
                               .component(StringComponent.newBuilder("str")
                                                         .withSuggestionsProvider((c, s) -> Arrays.asList("one", "two"))
                                                         .build())
                               .component(EnumComponent.required(TestEnum.class, "enum"))
                               .build());
    }

    @Test
    void testSimple() {
        final String input = "test";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("one", "two", "var"), suggestions);
    }

    @Test
    void testVar() {
        final String input = "test var";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("one", "two"), suggestions);
        final String input2 = "test var one";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("foo", "bar"), suggestions2);
        final String input3 = "test var one f";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.singletonList("foo"), suggestions3);
    }

    @Test
    void testEmpty() {
        final String input = "kenny";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertTrue(suggestions.isEmpty());
    }


    public enum TestEnum {
        FOO, BAR
    }

}
