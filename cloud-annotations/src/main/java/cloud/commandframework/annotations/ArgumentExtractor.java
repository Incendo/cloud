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
package cloud.commandframework.annotations;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Utility that extract {@link Argument arguments} from
 * {@link java.lang.reflect.Method method} {@link java.lang.reflect.Parameter parameters}
 */
class ArgumentExtractor implements Function<@NonNull Method, @NonNull Collection<@NonNull ArgumentParameterPair>> {

    @Override
    public @NonNull Collection<@NonNull ArgumentParameterPair> apply(final @NonNull Method method) {
        final Collection<ArgumentParameterPair> arguments = new ArrayList<>();
        for (final Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Argument.class)) {
                continue;
            }
            arguments.add(new ArgumentParameterPair(parameter, parameter.getAnnotation(Argument.class)));
        }
        return arguments;
    }

}
