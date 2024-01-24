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
package cloud.commandframework.annotations.suggestions;

import cloud.commandframework.annotations.method.AnnotatedMethodHandler;
import cloud.commandframework.annotations.method.ParameterValue;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.injection.ParameterInjectorRegistry;
import cloud.commandframework.suggestion.Suggestion;
import cloud.commandframework.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a method annotated with {@link Suggestions}
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
public final class MethodSuggestionProvider<C> extends AnnotatedMethodHandler<C> implements SuggestionProvider<C> {

    /**
     * Creates a new provider.
     *
     * @param instance         instance that owns the method
     * @param method           the annotated method
     * @param injectorRegistry injector registry
     */
    public MethodSuggestionProvider(
            final @NonNull Object instance,
            final @NonNull Method method,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) {
        super(method, instance, injectorRegistry);
    }

    @Override
    public @NonNull CompletableFuture<Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        try {
            final List<Object> arguments = this.createParameterValues(
                    context, this.parameters(), Arrays.asList(context, input, input.lastRemainingToken())
            ).stream().map(ParameterValue::value).collect(Collectors.toList());
            return mapSuggestions(this.methodHandle().invokeWithArguments(arguments));
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Maps the suggestions to a future of a list of suggestions.
     *
     * @param input the input
     * @return the mapped future
     * @since 2.0.0
     */
    @SuppressWarnings("rawtypes")
    public static @NonNull CompletableFuture<Iterable<@NonNull Suggestion>> mapSuggestions(final @NonNull Object input) {
        if (input instanceof CompletableFuture) {
            return mapSuggestions((CompletableFuture) input);
        }
        return CompletableFuture.completedFuture(mapCompleted(input));
    }

    /**
     * Maps the future to a future of suggestions.
     *
     * @param future the future
     * @return the mapped future
     * @since 2.0.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static @NonNull CompletableFuture<Iterable<@NonNull Suggestion>> mapFuture(final @NonNull CompletableFuture future) {
        return future.thenApply(MethodSuggestionProvider::mapCompleted);
    }

    /**
     * Maps the input to a list of suggestions. The input must be a stream, or an iterable.
     *
     * @param input the input
     * @return the suggestions
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    public static @NonNull Iterable<@NonNull Suggestion> mapCompleted(final @NonNull Object input) {
        final List<?> suggestions;
        if (input instanceof List) {
            suggestions = (List<?>) input;
        } else if (input instanceof Collection) {
            suggestions = new ArrayList<>((Collection<?>) input);
        } else if (input instanceof Iterable) {
            suggestions = new ArrayList<>();
            for (final Object suggestion : ((Iterable<?>) input)) {
                ((List<Object>) suggestions).add(suggestion);
            }
        } else if (input instanceof Stream) {
            suggestions = ((Stream<?>) input).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot handle suggestion input of type %s",
                            input.getClass().getName())
            );
        }

        if (suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        final Object suggestion = suggestions.get(0);
        if (suggestion instanceof Suggestion) {
            return (List<Suggestion>) suggestions;
        } else if (suggestion instanceof String) {
            return suggestions.stream().map(Object::toString).map(Suggestion::simple).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot handle suggestions of type: %s",
                            suggestion.getClass().getName())
            );
        }
    }
}
