//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;

class AnnotationParserTest {

    private static final List<String> NAMED_SUGGESTIONS = Arrays.asList("Dancing-Queen", "Gimme!-Gimme!-Gimme!", "Waterloo");

    private static CommandManager<TestCommandSender> manager;
    private static AnnotationParser<TestCommandSender> annotationParser;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();
        annotationParser = new AnnotationParser<>(manager, TestCommandSender.class, p -> SimpleCommandMeta.empty());
        manager.getParserRegistry().registerNamedParserSupplier("potato", p -> new StringArgument.StringParser<>(
                StringArgument.StringMode.SINGLE, (c, s) -> Collections.singletonList("potato")));
        /* Register a suggestion provider */
        manager.getParserRegistry().registerSuggestionProvider(
                "some-name",
                (context, input) -> NAMED_SUGGESTIONS
        );
    }

    @Test
    void testMethodConstruction() {
        final Collection<Command<TestCommandSender>> commands = annotationParser.parse(this);
        Assertions.assertFalse(commands.isEmpty());
        manager.executeCommand(new TestCommandSender(), "test literal 10").join();
        manager.executeCommand(new TestCommandSender(), "t literal 10 o").join();
        manager.executeCommand(new TestCommandSender(), "proxycommand 10").join();
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "test 101").join());
        manager.executeCommand(new TestCommandSender(), "flagcommand -p").join();
        manager.executeCommand(new TestCommandSender(), "flagcommand --print --word peanut").join();
    }

    @Test
    void testNamedSuggestionProvider() {
        Assertions.assertEquals(NAMED_SUGGESTIONS, manager.suggest(new TestCommandSender(), "namedsuggestions "));
    }

    @ProxiedBy("proxycommand")
    @CommandMethod("test|t literal <int> [string]")
    public void testCommand(
            final TestCommandSender sender,
            @Argument("int") @Range(max = "100") final int argument,
            @Argument(value = "string", defaultValue = "potato", parserName = "potato") final String string
    ) {
        System.out.printf("Received int: %d and string '%s'\n", argument, string);
    }

    @CommandMethod("flagcommand")
    public void testFlags(
            final TestCommandSender sender,
            @Flag(value = "print", aliases = "p") final boolean print,
            @Flag(value = "word", aliases = "w") final String word
    ) {
        if (print) {
            System.out.println(word);
        }
    }

    @CommandMethod("namedsuggestions <input>")
    public void testNamedSuggestionProviders(
            @Argument(value = "input", suggestions = "some-name") final String argument
    ) {
    }

}
