//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Registry of {@link ArgumentParser} that allows these arguments to be
 * referenced by a {@link Class} (or really, a {@link com.google.common.reflect.TypeToken})
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
    <T> void registerParserSupplier(@Nonnull TypeToken<T> type,
                                    @Nonnull Function<ParserParameters, ArgumentParser<C, ?>> supplier);

    /**
     * Register a named parser supplier
     *
     * @param name     Parser name
     * @param supplier The function that generates the parser. The map supplied my contain parameters used
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
     */
    void registerNamedParserSupplier(@Nonnull String name,
                                     @Nonnull Function<ParserParameters, ArgumentParser<C, ?>> supplier);

    /**
     * Register a mapper that maps annotation instances to a map of parameter-object pairs
     *
     * @param annotation Annotation class
     * @param mapper     Mapper that maps the pair (annotation, type to be parsed) to a map of
     *                   {@link ParserParameter parameter}-{@link Object object} pairs
     * @param <A>        Annotation type
     * @param <T>        Type of the object that the parser is retrieved for
     */
    <A extends Annotation, T> void registerAnnotationMapper(@Nonnull Class<A> annotation,
                                                            @Nonnull BiFunction<A, TypeToken<?>,
                                                                    ParserParameters> mapper);

    /**
     * Parse annotations into {@link ParserParameters}
     *
     * @param parsingType The type that is produced by the parser that is requesting the parsing parameters
     * @param annotations The annotations to be parsed
     * @return Parsed parameters
     */
    @Nonnull
    ParserParameters parseAnnotations(@Nonnull TypeToken<?> parsingType, @Nonnull Collection<? extends Annotation> annotations);

    /**
     * Attempt to create a {@link ArgumentParser} for a specified type, using
     * an instance of {@link ParserParameter} to configure the parser settings
     *
     * @param type             Type that should be produced by the parser
     * @param parserParameters Parser parameters
     * @param <T>              Generic type
     * @return Parser, if one can be created
     */
    @Nonnull
    <T> Optional<ArgumentParser<C, T>> createParser(@Nonnull TypeToken<T> type,
                                                    @Nonnull ParserParameters parserParameters);

    /**
     * Attempt to create a {@link ArgumentParser} for a specified type, using
     * an instance of {@link ParserParameter} to configure the parser settings
     *
     * @param name             Parser
     * @param parserParameters Parser parameters
     * @param <T>              Generic type
     * @return Parser, if one can be created
     */
    @Nonnull
    <T> Optional<ArgumentParser<C, T>> createParser(@Nonnull String name,
                                                    @Nonnull ParserParameters parserParameters);

}
