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
package org.incendo.cloud.annotations.parser;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.injection.ParameterInjectorRegistry;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;

/**
 * Represents a method argument parser annotated with {@link Parser}
 *
 * @param <C> command sender type
 */
public final class MethodArgumentParserFactoryImpl<C> implements MethodArgumentParserFactory<C> {

    @Override
    public @NonNull ParserDescriptor<C, ?> createArgumentParser(
            final @NonNull SuggestionProvider<C> suggestionProvider,
            final @NonNull Object instance,
            final @NonNull Method method,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) {
        return ParserDescriptor.of(
                new MethodArgumentParser<>(suggestionProvider, instance, method, injectorRegistry),
                TypeToken.get(method.getGenericReturnType())
        );
    }
}
