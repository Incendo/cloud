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
package com.intellectualsites.commands.annotations;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.annotations.specifier.Range;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionException;

class AnnotationParserTest {

    private static CommandManager<TestCommandSender> manager;
    private static AnnotationParser<TestCommandSender> annotationParser;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();
        annotationParser = new AnnotationParser<>(manager, TestCommandSender.class, p -> SimpleCommandMeta.empty());
        manager.getParserRegistry().registerNamedParserSupplier("potato", p -> new StringArgument.StringParser<>(
                StringArgument.StringMode.SINGLE, (c, s) -> Collections.singletonList("potato")));
    }

    @Test
    void testMethodConstruction() {
        final Collection<Command<TestCommandSender>> commands = annotationParser.parse(this);
        Assertions.assertFalse(commands.isEmpty());
        manager.executeCommand(new TestCommandSender(), "test 10").join();
        manager.executeCommand(new TestCommandSender(), "t 10").join();
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "test 101").join());
    }

    @CommandMethod("test|t <int> [string]")
    public void testCommand(@Nonnull final TestCommandSender sender,
                            @Argument("int") @Range(max = "100") final int argument,
                            @Nonnull @Argument(value = "string", defaultValue = "potato", parserName = "potato")
                                final String string) {
        System.out.printf("Received int: %d and string '%s'\n", argument, string);
    }

}
