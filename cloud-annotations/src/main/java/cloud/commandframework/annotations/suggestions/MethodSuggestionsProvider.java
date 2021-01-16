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
package cloud.commandframework.annotations.suggestions;

import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Represents a method annotated with {@link Suggestions}
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
public final class MethodSuggestionsProvider<C> implements BiFunction<CommandContext<C>, String, List<String>> {

    private final MethodHandle methodHandle;

    /**
     * Create a new provider
     *
     * @param instance Instance that owns the method
     * @param method   The annotated method
     * @throws Exception If the method lookup fails
     */
    public MethodSuggestionsProvider(
            final @NonNull Object instance,
            final @NonNull Method method
    ) throws Exception {
        this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> apply(final CommandContext<C> context, final String s) {
        try {
            return (List<String>) this.methodHandle.invokeWithArguments(context, s);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
