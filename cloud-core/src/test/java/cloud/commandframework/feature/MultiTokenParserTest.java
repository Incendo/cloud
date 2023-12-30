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
package cloud.commandframework.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.keys.CloudKey;
import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.BooleanParser.booleanParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

/**
 * Test for parsers that consume multiple tokens.
 */
class MultiTokenParserTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testParsing() {
        // Arrange
        final CloudKey<Monkey> monkeyKey = CloudKey.of("monkey", Monkey.class);
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .required(monkeyKey, ParserDescriptor.of(new MonkeyParser(), TypeToken.get(Monkey.class)))
                        .optional("leader", booleanParser())
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(
                new TestCommandSender(),
                "command Bobo banana 5"
        ).join();

        // Assert
        assertThat(result.commandContext().get(monkeyKey)).isEqualTo(new Monkey("Bobo", Fruit.BANANA, 5));
    }

    @Test
    void testSuggestionsNoInput() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .required("monkey", ParserDescriptor.of(new MonkeyParser(), TypeToken.get(Monkey.class)))
                        .optional("leader", booleanParser())
        );

        // Act
        final Iterable<? extends Suggestion> result = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "command "
        );

        // Assert
        assertThat(result).containsExactly(
                Suggestion.simple("Goofy"),
                Suggestion.simple("Bubbles"),
                Suggestion.simple("Chuckles")
        );
    }

    @Test
    void testSuggestionsAfterName() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .required("monkey", ParserDescriptor.of(new MonkeyParser(), TypeToken.get(Monkey.class)))
                        .optional("leader", booleanParser())
        );

        // Act
        final Iterable<? extends Suggestion> result = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "command Goofy"
        );

        // Assert
        assertThat(result).containsExactly(
                Suggestion.simple("Goofy banana"),
                Suggestion.simple("Goofy apple"),
                Suggestion.simple("Goofy mango")
        );
    }

    @Test
    void testSuggestionsAfterFruit() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .required("monkey", ParserDescriptor.of(new MonkeyParser(), TypeToken.get(Monkey.class)))
                        .optional("leader", booleanParser())
        );

        // Act
        final Iterable<? extends Suggestion> result = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "command Goofy banana "
        );

        // Assert
        assertThat(result).containsExactly(
                Suggestion.simple("Goofy banana 1"),
                Suggestion.simple("Goofy banana 2"),
                Suggestion.simple("Goofy banana 3")
        );
    }

    @Test
    void testSuggestionsAfterMonkey() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("command")
                        .required("monkey", ParserDescriptor.of(new MonkeyParser(), TypeToken.get(Monkey.class)))
                        .optional("leader", booleanParser())
        );

        // Act
        final Iterable<? extends Suggestion> result = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "command Goofy banana 5 "
        );

        // Assert
        assertThat(result).containsExactly(
                Suggestion.simple("true"),
                Suggestion.simple("false")
        );
    }


    static final class MonkeyParser implements ArgumentParser.FutureArgumentParser<TestCommandSender, Monkey>,
            SuggestionProvider<TestCommandSender> {

        private static final List<String> MONKEY_NAMES = Arrays.asList("Goofy", "Bubbles", "Chuckles");

        @Override
        public @NonNull CompletableFuture<ArgumentParseResult<@NonNull Monkey>> parseFuture(
                final @NonNull CommandContext<@NonNull TestCommandSender> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            if (commandInput.remainingTokens() < 3) {
                return ArgumentParseResult.failureFuture(new IllegalArgumentException("Needs: name, fruit, age"));
            }
            final String name = commandInput.readString();
            final Fruit favoriteFruit = Fruit.valueOf(commandInput.readString().toUpperCase(Locale.ROOT));
            if (!commandInput.isValidInteger(0, Integer.MAX_VALUE)) {
                return ArgumentParseResult.failureFuture(new IllegalArgumentException(
                        commandInput.peekString() + " is not a valid age"));
            }
            final int age = commandInput.readInteger();
            return ArgumentParseResult.successFuture(new Monkey(name, favoriteFruit, age));
        }

        @Override
        public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull CommandInput input
        ) {
            final String name;
            if (input.hasRemainingInput()) {
                name = input.readString();
            } else {
                return CompletableFuture.completedFuture(
                        MONKEY_NAMES.stream().map(Suggestion::simple).collect(Collectors.toList())
                );
            }

            final String favoriteFruit;
            if (input.hasRemainingInput()) {
                favoriteFruit = input.readString();
            } else {
                return CompletableFuture.completedFuture(
                        Arrays.stream(Fruit.values())
                                .map(Fruit::name)
                                .map(fruit -> fruit.toLowerCase(Locale.ROOT))
                                .map(fruit -> String.format("%s %s", name, fruit))
                                .map(Suggestion::simple)
                                .collect(Collectors.toList())
                );
            }

            final int age;
            if (input.isValidInteger(0, Integer.MAX_VALUE)) {
                age = input.readInteger();
            } else {
                return CompletableFuture.completedFuture(
                        IntStream.range(1, 4)
                                .mapToObj(number -> String.format("%s %s %d", name, favoriteFruit, number))
                                .map(Suggestion::simple)
                                .collect(Collectors.toList())
                );
            }

            return CompletableFuture.completedFuture(
                    Collections.singletonList(
                            Suggestion.simple(String.format("%s %s %d", name, favoriteFruit, age))
                    )
            );
        }
    }

    static final class Monkey {

        private final String name;
        private final Fruit favoriteFruit;
        private final int age;

        Monkey(final @NonNull String name, final @NonNull Fruit favoriteFruit, final int age) {
            this.name = name;
            this.favoriteFruit = favoriteFruit;
            this.age = age;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) return false;
            final Monkey monkey = (Monkey) o;
            return this.age == monkey.age && Objects.equals(this.name, monkey.name) && this.favoriteFruit == monkey.favoriteFruit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.favoriteFruit, this.age);
        }
    }

    enum Fruit {
        BANANA,
        APPLE,
        MANGO
    }
}
