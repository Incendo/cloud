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
package cloud.commandframework.annotations.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.meta.SimpleCommandMeta;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;

class MethodSuggestionProviderTest {

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;
    private CommandContextFactory<TestCommandSender> commandContextFactory;

    @BeforeEach
    void setup() {
        this.commandContextFactory = new StandardCommandContextFactory<>();
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class,
                p -> SimpleCommandMeta.empty()
        );
    }

    @ParameterizedTest
    @MethodSource("testSuggestionsSource")
    void testSuggestions(final @NonNull Object instance) {
        // Arrange
        this.annotationParser.parse(instance);
        final CommandContext<TestCommandSender> context = this.commandContextFactory.create(
                true,
                new TestCommandSender(),
                this.commandManager
        );

        // Act
        final List<Suggestion> suggestions =
                this.commandManager.parserRegistry()
                        .getSuggestionProvider("suggestions")
                        .orElseThrow(NullPointerException::new)
                        .suggestions(context, "");

        // Assert
        assertThat(suggestions).containsExactly(Suggestion.simple("foo"));
    }

    static @NonNull Stream<@NonNull Object> testSuggestionsSource() {
        return Stream.of(
                new TestClassList(),
                new TestClassSet(),
                new TestClassStream(),
                new TestClassIterable(),
                new TestClassListString()
        );
    }


    public static final class TestClassList {

        @Suggestions("suggestions")
        public @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singletonList(Suggestion.simple("foo"));
        }
    }

    public static final class TestClassSet {

        @Suggestions("suggestions")
        public @NonNull Set<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singleton(Suggestion.simple("foo"));
        }
    }

    public static final class TestClassStream {

        @Suggestions("suggestions")
        public @NonNull Stream<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Stream.of(Suggestion.simple("foo"));
        }
    }

    public static final class TestClassIterable {

        @Suggestions("suggestions")
        public @NonNull Iterable<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singleton(Suggestion.simple("foo"));
        }
    }

    public static final class TestClassListString {

        @Suggestions("suggestions")
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singletonList("foo");
        }
    }
}
