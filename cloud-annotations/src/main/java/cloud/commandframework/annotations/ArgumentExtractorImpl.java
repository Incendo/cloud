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
package cloud.commandframework.annotations;

import cloud.commandframework.Description;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility that extract {@link Argument arguments} from
 * {@link java.lang.reflect.Method method} {@link java.lang.reflect.Parameter parameters}
 */
class ArgumentExtractorImpl implements ArgumentExtractor {

    private final Function<@NonNull Argument, @Nullable Description> descriptionMapper;

    ArgumentExtractorImpl() {
        this.descriptionMapper = argument -> Description.of(argument.description());
    }

    @Override
    public @NonNull Collection<@NonNull ArgumentDescriptor> extractArguments(
            final @NonNull List<@NonNull SyntaxFragment> syntax,
            final @NonNull Method method
    ) {
        final Collection<ArgumentDescriptor> arguments = new ArrayList<>();
        for (final Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Argument.class)) {
                continue;
            }
            final Argument argument = parameter.getAnnotation(Argument.class);
            final ArgumentDescriptor argumentDescriptor = ArgumentDescriptor.builder()
                    .parameter(parameter)
                    .name(argument.value())
                    .parserName(argument.parserName())
                    .defaultValue(argument.defaultValue())
                    .description(this.descriptionMapper.apply(argument))
                    .suggestions(argument.suggestions())
                    .build();
            arguments.add(argumentDescriptor);
        }
        return arguments;
    }
}
