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
package cloud.commandframework.parser;

import cloud.commandframework.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Registry that allows {@link ArgumentParser parsers} to be referenced by the type of the values they produce, or by their names.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public interface ParserRegistry<C> {

    /**
     * Registers the given {@code supplier} as the default parser supplier for the given {@code type}.
     *
     * @param type     type that is parsed by the parser
     * @param supplier function that generates the parser
     * @param <T>      generic type specifying what is produced by the parser
     * @return {@code this}
     */
    <T> @This ParserRegistry<C> registerParserSupplier(
            @NonNull TypeToken<T> type,
            @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
    );

    /**
     * Registers the parser described by the given {@code descriptor}.
     *
     * <p>This does not allow for customization of the parser using parser parameters. If the parser has options then it is
     * recommended to use {@link #registerParserSupplier(TypeToken, Function)} instead.</p>
     *
     * @param descriptor parser descriptor
     * @param <T>        type produced by the parser
     * @return {@code this}
     */
    @API(status = API.Status.STABLE)
    default <T> @This ParserRegistry<C> registerParser(final @NonNull ParserDescriptor<C, T> descriptor) {
        return this.registerParserSupplier(descriptor.valueType(), parameters -> descriptor.parser());
    }

    /**
     * Registers a named parser supplier.
     *
     * <p>The named parser will only be created if referenced by name using {@link #createParser(String, ParserParameters)}.
     * Only unnamed parsers may be referenced by their value types.</p>
     *
     * @param name     parser name
     * @param supplier the function that generates the parser
     * @return {@code this}
     */
    @This ParserRegistry<C> registerNamedParserSupplier(
            @NonNull String name,
            @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
    );

    /**
     * Registers a named parser supplier.
     *
     * <p>This does not allow for customization of the parser using parser parameters. If the parser has options then it is
     * recommended to use {@link #registerNamedParserSupplier(String, Function)} instead.</p>
     *
     * @param name       parser name
     * @param descriptor parser descriptor
     * @return {@code this}
     */
    default @This ParserRegistry<C> registerNamedParser(
            @NonNull String name,
            @NonNull ParserDescriptor<C, ?> descriptor
    ) {
        return this.registerNamedParserSupplier(name, parameters -> descriptor.parser());
    }

    /**
     * Registers a mapper that maps annotation instances to a map of parameter-object pairs.
     *
     * @param annotation annotation class
     * @param mapper     mapper that maps the pair (annotation, type to be parsed) to a map of
     *                   {@link ParserParameter parameter}-{@link Object object} pairs
     * @param <A>        annotation type
     * @return {@code this}
     */
    <A extends Annotation> @This ParserRegistry<C> registerAnnotationMapper(
            @NonNull Class<A> annotation,
            @NonNull AnnotationMapper<A> mapper
    );

    /**
     * Parses the given {@code annotations} into {@link ParserParameters}.
     *
     * @param parsingType the type that is produced by the parser that is requesting the parsing parameters
     * @param annotations annotations to be parsed
     * @return the parsed parameters
     */
    @NonNull ParserParameters parseAnnotations(
            @NonNull TypeToken<?> parsingType,
            @NonNull Collection<? extends @NonNull Annotation> annotations
    );

    /**
     * Attempts to create an instance of the default {@link ArgumentParser} for the given {@code type} using an instance of
     * {@link ParserParameter} to configure the parser settings.
     *
     * @param type             type of values produced by the parser
     * @param parserParameters parser parameters
     * @param <T>              type of values produced by the parser
     * @return the parser, if one could be created
     */
    <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            @NonNull TypeToken<T> type,
            @NonNull ParserParameters parserParameters
    );

    /**
     * Attempts to create an instance of the parser {@link ArgumentParser} with the given {@code name} using an instance of
     * {@link ParserParameter} to configure the parser settings.
     *
     * @param name             name of the parser
     * @param parserParameters parser parameters
     * @param <T>              type of values produced by the parser
     * @return the parser, if one could be created
     */
    <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            @NonNull String name,
            @NonNull ParserParameters parserParameters
    );

    /**
     * Registers a new named suggestion providers.
     *
     * @param name               name of the suggestion provider. The name is case independent.
     * @param suggestionProvider the suggestion provider
     * @see #getSuggestionProvider(String) Get a suggestion provider
     */
    @API(status = API.Status.STABLE)
    void registerSuggestionProvider(@NonNull String name, @NonNull SuggestionProvider<C> suggestionProvider);

    /**
     * Returns a named suggestion provider, if it exists.
     *
     * @param name case-independent suggestion provider name
     * @return optional that either contains the suggestion provider, or is empty if no
     *         suggestion provider is registered with the given name
     * @see #registerSuggestionProvider(String, SuggestionProvider) Register a suggestion provider
     */
    @API(status = API.Status.STABLE)
    @NonNull Optional<SuggestionProvider<C>> getSuggestionProvider(@NonNull String name);


    @FunctionalInterface
    @API(status = API.Status.STABLE)
    interface AnnotationMapper<A extends Annotation> {

        /**
         * Returns the {@link ParserParameters parser parameters} that correspond to the given {@code annotation}.
         *
         * @param annotation annotation instance
         * @param parsedType type produced by the parser that is being constructed
         * @return the parameters
         */
        @NonNull ParserParameters mapAnnotation(@NonNull A annotation, @NonNull TypeToken<?> parsedType);
    }
}
