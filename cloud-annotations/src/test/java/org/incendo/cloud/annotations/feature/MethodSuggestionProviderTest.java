//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package org.incendo.cloud.annotations.feature;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.suggestion.Suggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Named.named;

class MethodSuggestionProviderTest {

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;
    private CommandContextFactory<TestCommandSender> commandContextFactory;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.commandContextFactory = new StandardCommandContextFactory<>(this.commandManager);
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class
        );
        this.commandManager.parameterInjectorRegistry().registerInjector(
                InjectedValue.class,
                ParameterInjector.constantInjector(new InjectedValue("foo"))
        );
    }

    @ParameterizedTest
    @MethodSource("testSuggestionsSource")
    void testSuggestions(final @NonNull Object instance) {
        // Arrange
        this.annotationParser.parse(instance);
        final CommandContext<TestCommandSender> context = this.commandContextFactory.create(
                true,
                new TestCommandSender()
        );

        // Act
        final Iterable<? extends Suggestion> suggestions =
                this.commandManager.parserRegistry()
                        .getSuggestionProvider("suggestions")
                        .orElseThrow(NullPointerException::new)
                        .suggestionsFuture(context, CommandInput.empty())
                        .join();

        // Assert
        assertThat(suggestions).containsExactly(Suggestion.suggestion("foo"));
    }

    static @NonNull Stream<@NonNull Object> testSuggestionsSource() {
        return Stream.of(
                named("list source", new TestClassList()),
                named("set source", new TestClassSet()),
                named("stream source", new TestClassStream()),
                named("iterable source", new TestClassIterable()),
                named("string list source", new TestClassListString()),
                named("source with CommandInput injected", new TestClassCommandInput()),
                named("source with injected value", new TestInjectedValue())
        );
    }


    public static final class TestClassList {

        @Suggestions("suggestions")
        public @NonNull Iterable<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singletonList(Suggestion.suggestion("foo"));
        }
    }

    public static final class TestClassSet {

        @Suggestions("suggestions")
        public @NonNull Set<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singleton(Suggestion.suggestion("foo"));
        }
    }

    public static final class TestClassStream {

        @Suggestions("suggestions")
        public @NonNull Stream<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Stream.of(Suggestion.suggestion("foo"));
        }
    }

    public static final class TestClassIterable {

        @Suggestions("suggestions")
        public @NonNull Iterable<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull String input
        ) {
            return Collections.singleton(Suggestion.suggestion("foo"));
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

    public static final class TestClassCommandInput {

        @Suggestions("suggestions")
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull CommandInput input
        ) {
            return Collections.singletonList("foo");
        }
    }

    public static final class TestInjectedValue {

        @Suggestions("suggestions")
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull InjectedValue injectedValue
        ) {
            return Collections.singletonList(injectedValue.value());
        }
    }

    public static class InjectedValue {

        private final String value;

        public InjectedValue(final String value) {
            this.value = value;
        }

        public @NonNull String value() {
            return this.value;
        }
    }
}
