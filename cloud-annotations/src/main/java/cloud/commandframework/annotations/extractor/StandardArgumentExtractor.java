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
package cloud.commandframework.annotations.extractor;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.ArgumentMode;
import cloud.commandframework.annotations.Default;
import cloud.commandframework.annotations.DescriptionMapper;
import cloud.commandframework.annotations.SyntaxFragment;
import cloud.commandframework.annotations.descriptor.ArgumentDescriptor;
import cloud.commandframework.internal.ImmutableBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

/**
 * Utility that extract {@link ArgumentDescriptor arguments} from
 * {@link java.lang.reflect.Method method} {@link java.lang.reflect.Parameter parameters}
 */
@ImmutableBuilder
@Value.Immutable
public abstract class StandardArgumentExtractor implements ArgumentExtractor {

    private static @Nullable String nullIfEmpty(final @NonNull String string) {
        if (string.isEmpty()) {
            return null;
        }
        return string;
    }

    /**
     * Returns a {@link StandardArgumentExtractor} with the default options.
     *
     * @param annotationParser the annotation parser
     * @return the argument extractor
     */
    public static @NonNull StandardArgumentExtractor create(final @NonNull AnnotationParser<?> annotationParser) {
        return builder(annotationParser).build();
    }

    /**
     * Returns a builder that builds {@link StandardArgumentExtractor} instances.
     *
     * @param annotationParser the annotation parser
     * @return the builder
     */
    public static ImmutableStandardArgumentExtractor.@NonNull Builder builder(
            final @NonNull AnnotationParser<?> annotationParser
    ) {
        return ImmutableStandardArgumentExtractor.builder().annotationParser(annotationParser);
    }

    /**
     * Returns a builder that builds {@link StandardArgumentExtractor} instances.
     *
     * @param extractor the extractor to copy from
     * @return the builder
     */
    public static ImmutableStandardArgumentExtractor.@NonNull Builder builder(
            final @NonNull StandardArgumentExtractor extractor
    ) {
        return ImmutableStandardArgumentExtractor.builder().from(extractor);
    }

    /**
     * Returns the annotation parser.
     *
     * @return the annotation parser
     */
    public abstract @NonNull AnnotationParser<?> annotationParser();

    /**
     * Returns the {@link ParameterNameExtractor} which is responsible for mapping parameters to syntax
     * fragments in the case that a named {@literal @Argument} annotation is not present.
     *
     * @return the parameter name extractor
     */
    @Value.Default
    public @NonNull ParameterNameExtractor parameterNameExtractor() {
        return ParameterNameExtractor.simple();
    }

    /**
     * Returns the description mapper.
     *
     * @return the description mapper
     */
    @Value.Default
    public @NonNull DescriptionMapper descriptionMapper() {
        return this.annotationParser()::mapDescription;
    }

    @Override
    public final @NonNull Collection<@NonNull ArgumentDescriptor> extractArguments(
            final @NonNull List<@NonNull SyntaxFragment> syntax,
            final @NonNull Method method
    ) {
        final Map<String, SyntaxFragment> variableFragments = new HashMap<>();
        syntax.stream()
                .filter(fragment -> fragment.argumentMode() != ArgumentMode.LITERAL)
                .forEach(fragment -> variableFragments.put(fragment.major(), fragment));

        final Collection<ArgumentDescriptor> arguments = new ArrayList<>();
        for (final Parameter parameter : method.getParameters()) {
            final String parameterName = this.parameterNameExtractor().extract(parameter);
            if (!parameter.isAnnotationPresent(Argument.class)) {
                final SyntaxFragment fragment = variableFragments.get(parameterName);
                if (fragment != null) {
                    arguments.add(ArgumentDescriptor.builder().parameter(parameter).name(parameterName).build());
                }
                continue;
            }

            final Argument argument = parameter.getAnnotation(Argument.class);

            final String name;
            if (argument.value().equals(AnnotationParser.INFERRED_ARGUMENT_NAME)) {
                name = parameterName;
            } else {
                name = this.annotationParser().processString(argument.value());
            }

            String defaultValue = null;
            if (parameter.isAnnotationPresent(Default.class)) {
                defaultValue = this.annotationParser().processString(parameter.getAnnotation(Default.class).value());
            }

            final ArgumentDescriptor argumentDescriptor = ArgumentDescriptor.builder()
                    .parameter(parameter)
                    .name(name)
                    .parserName(nullIfEmpty(this.annotationParser().processString(argument.parserName())))
                    .defaultValue(defaultValue)
                    .description(this.descriptionMapper().map(argument.description()))
                    .suggestions(nullIfEmpty(this.annotationParser().processString(argument.suggestions())))
                    .build();
            arguments.add(argumentDescriptor);
        }
        return arguments;
    }
}
