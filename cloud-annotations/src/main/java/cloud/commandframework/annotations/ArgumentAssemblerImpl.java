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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.annotations.specifier.Completions;
import cloud.commandframework.arguments.ComponentPreprocessor;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unchecked")
final class ArgumentAssemblerImpl<C> implements ArgumentAssembler<C> {

    private final AnnotationParser<C> annotationParser;

    ArgumentAssemblerImpl(final @NonNull AnnotationParser<C> annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public @NonNull CommandComponent<C> assembleArgument(
            @NonNull final SyntaxFragment syntaxFragment,
            @NonNull final ArgumentDescriptor descriptor
    ) {
        final Parameter parameter = descriptor.parameter();
        final Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
        final TypeToken<?> token = TypeToken.get(parameter.getParameterizedType());
        final ParserParameters parameters = this.annotationParser.manager().parserRegistry()
                .parseAnnotations(token, annotations);
        /* Create the argument parser */
        final ArgumentParser<C, ?> parser;
        if (descriptor.parserName() == null) {
            parser = this.annotationParser.manager().parserRegistry()
                    .createParser(token, parameters)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(
                                    "Parameter '%s' "
                                            + "has parser '%s' but no parser exists "
                                            + "for that type",
                                    parameter.getName(),
                                    token.getType().getTypeName()
                            )));
        } else {
            parser = this.annotationParser.manager().parserRegistry()
                    .createParser(this.annotationParser.processString(descriptor.parserName()), parameters)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(
                                    "Parameter '%s' "
                                            + "has parser '%s' but no parser exists "
                                            + "for that type",
                                    parameter.getName(),
                                    token.getType().getTypeName()
                            )));
        }
        /* Check whether the corresponding method parameter actually exists */
        final String argumentName = this.annotationParser.processString(descriptor.name());
        if (syntaxFragment.getArgumentMode() == ArgumentMode.LITERAL) {
            throw new IllegalArgumentException(String.format(
                    "Invalid command argument '%s': Missing syntax mapping", argumentName));
        }

        @SuppressWarnings("rawtypes") final CommandComponent.Builder componentBuilder = CommandComponent.builder();
        componentBuilder.commandManager(this.annotationParser.manager())
                .valueType(parameter.getType())
                .name(argumentName)
                .parser(parser)
                .required(syntaxFragment.getArgumentMode() == ArgumentMode.REQUIRED);

        /* Check for Completions annotation */
        final Completions completions = parameter.getDeclaredAnnotation(Completions.class);
        if (completions != null) {
            final List<Suggestion> suggestions = Arrays.stream(
                    completions.value().replace(" ", "").split(",")
            ).map(Suggestion::simple).collect(Collectors.toList());
            componentBuilder.suggestionProvider(SuggestionProvider.suggesting(suggestions));
        } else if (descriptor.suggestions() != null) {
            final String suggestionProviderName = this.annotationParser.processString(descriptor.suggestions());
            final Optional<SuggestionProvider<C>> suggestionsFunction =
                    this.annotationParser.manager().parserRegistry().getSuggestionProvider(suggestionProviderName);
            componentBuilder.suggestionProvider(
                    suggestionsFunction.orElseThrow(() ->
                            new IllegalArgumentException(String.format(
                                    "There is no suggestion provider with name '%s'. Did you forget to register it?",
                                    suggestionProviderName
                            )))
            );
        }

        if (descriptor.description() != null) {
            componentBuilder.description(descriptor.description());
        }

        if (syntaxFragment.getArgumentMode() == ArgumentMode.OPTIONAL && descriptor.defaultValue() != null) {
            componentBuilder.defaultValue(DefaultValue.parsed(this.annotationParser.processString(descriptor.defaultValue())));
        }

        for (final Annotation annotation : annotations) {
            @SuppressWarnings("rawtypes") final PreprocessorMapper preprocessorMapper =
                    this.annotationParser.preprocessorMappers().get(annotation.annotationType());
            if (preprocessorMapper != null) {
                final ComponentPreprocessor<C> preprocessor = (ComponentPreprocessor<C>) preprocessorMapper.mapAnnotation(
                        annotation
                );
                componentBuilder.preprocessor(preprocessor);
            }
        }

        return componentBuilder.build();
    }
}
