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
package cloud.commandframework.annotations.suggestions;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a method annotated with {@link Suggestions}
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
public final class MethodSuggestionProvider<C> implements SuggestionProvider<C> {

    private final MethodHandle methodHandle;

    /**
     * Create a new provider
     *
     * @param instance Instance that owns the method
     * @param method   The annotated method
     * @throws Exception If the method lookup fails
     */
    public MethodSuggestionProvider(
            final @NonNull Object instance,
            final @NonNull Method method
    ) throws Exception {
        this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull List<@NonNull Suggestion> suggestions(final @NonNull CommandContext<C> context, final @NonNull String input) {
        try {
            final Object output = this.methodHandle.invokeWithArguments(context, input);

            final List<?> suggestions;
            if (output instanceof List) {
                suggestions = (List<?>) output;
            } else if (output instanceof Collection) {
                suggestions = new ArrayList<>((Collection<?>) output);
            } else if (output instanceof Stream) {
                suggestions = ((Stream<?>) output).collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException(
                        String.format("Cannot handle suggestion output of type %s",
                                output.getClass().getName())
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
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
