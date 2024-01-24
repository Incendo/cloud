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
package cloud.commandframework.annotations.parsers;

import cloud.commandframework.annotations.method.AnnotatedMethodHandler;
import cloud.commandframework.annotations.method.ParameterValue;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.injection.ParameterInjectorRegistry;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a method annotated with {@link Parser}
 *
 * @param <C> Command sender type
 * @param <T> Argument type
 * @since 1.3.0
 */
public final class MethodArgumentParser<C, T> extends AnnotatedMethodHandler<C> implements ArgumentParser<C, T> {

    private final SuggestionProvider<C> suggestionProvider;

    /**
     * Creates a new parser.
     *
     * @param suggestionProvider suggestion provider
     * @param instance           instance that owns the method
     * @param method             the annotated method
     * @param injectorRegistry   injector registry
     */
    public MethodArgumentParser(
            final @NonNull SuggestionProvider<C> suggestionProvider,
            final @NonNull Object instance,
            final @NonNull Method method,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) {
        super(method, instance, injectorRegistry);
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ArgumentParseResult<@NonNull T> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final List<Object> arguments = this.createParameterValues(
                commandContext,
                this.parameters(),
                Collections.singletonList(commandInput)
        ).stream().map(ParameterValue::value).collect(Collectors.toList());
        try {
            return ArgumentParseResult.success(
                    (T) this.methodHandle().invokeWithArguments(arguments)
            );
        } catch (final Throwable t) {
            return ArgumentParseResult.failure(t);
        }
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }
}
