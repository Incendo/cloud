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
package cloud.commandframework.arguments.parser;

import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Registry of {@link ArgumentParser} that allows these arguments to be
 * referenced by a {@link Class} (or really, a {@link TypeToken})
 * or a {@link String} key
 *
 * @param <C> Command sender type
 */
public interface ParserRegistry<C> {

    /**
     * Register a parser supplier
     *
     * @param type     The type that is parsed by the parser
     * @param supplier The function that generates the parser. The map supplied my contain parameters used
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
     * @param <T>      Generic type specifying what is produced by the parser
     */
    <T> void registerParserSupplier(
            @NonNull TypeToken<T> type,
            @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
    );

    /**
     * Register a named parser supplier
     *
     * @param name     Parser name
     * @param supplier The function that generates the parser. The map supplied my contain parameters used
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
     */
    void registerNamedParserSupplier(
            @NonNull String name,
            @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
    );

    /**
     * Register a mapper that maps annotation instances to a map of parameter-object pairs
     *
     * @param annotation Annotation class
     * @param mapper     Mapper that maps the pair (annotation, type to be parsed) to a map of
     *                   {@link ParserParameter parameter}-{@link Object object} pairs
     * @param <A>        Annotation type
     * @param <T>        Type of the object that the parser is retrieved for
     */
    <A extends Annotation, T> void registerAnnotationMapper(
            @NonNull Class<A> annotation,
            @NonNull BiFunction<@NonNull A, @NonNull TypeToken<?>,
                    @NonNull ParserParameters> mapper
    );

    /**
     * Parse annotations into {@link ParserParameters}
     *
     * @param parsingType The type that is produced by the parser that is requesting the parsing parameters
     * @param annotations The annotations to be parsed
     * @return Parsed parameters
     */
    @NonNull ParserParameters parseAnnotations(
            @NonNull TypeToken<?> parsingType,
            @NonNull Collection<? extends Annotation> annotations
    );

    /**
     * Attempt to create a {@link ArgumentParser} for a specified type, using
     * an instance of {@link ParserParameter} to configure the parser settings
     *
     * @param type             Type that should be produced by the parser
     * @param parserParameters Parser parameters
     * @param <T>              Generic type
     * @return Parser, if one can be created
     */
    <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            @NonNull TypeToken<T> type,
            @NonNull ParserParameters parserParameters
    );

    /**
     * Attempt to create a {@link ArgumentParser} for a specified type, using
     * an instance of {@link ParserParameter} to configure the parser settings
     *
     * @param name             Parser
     * @param parserParameters Parser parameters
     * @param <T>              Generic type
     * @return Parser, if one can be created
     */
    <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            @NonNull String name,
            @NonNull ParserParameters parserParameters
    );

    /**
     * Register a new named suggestion provider
     *
     * @param name                Name of the suggestions provider. The name is case independent.
     * @param suggestionsProvider The suggestions provider
     * @see #getSuggestionProvider(String) Get a suggestion provider
     * @since 1.1.0
     */
    void registerSuggestionProvider(
            @NonNull String name,
            @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String, @NonNull List<String>> suggestionsProvider
    );

    /**
     * Get a named suggestion provider, if a suggestion provider with the given name exists in the registry
     *
     * @param name Suggestion provider name. The name is case independent.
     * @return Optional that either contains the suggestion provider, or is empty if no
     *         suggestion provider is registered with the given name
     * @see #registerSuggestionProvider(String, BiFunction) Register a suggestion provider
     * @since 1.1.0
     */
    @NonNull Optional<BiFunction<@NonNull CommandContext<C>, @NonNull String, @NonNull List<String>>> getSuggestionProvider(
            @NonNull String name
    );

}
