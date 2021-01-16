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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiFunction;

/**
 * A compound argument consisting of two inner arguments
 *
 * @param <C> Command sender type
 * @param <U> First argument type
 * @param <V> Second argument type
 * @param <O> Output type
 */
public class ArgumentPair<C, U, V, O> extends CompoundArgument<Pair<U, V>, C, O> {

    /**
     * Create a new argument pair.
     *
     * @param required   Whether or not the argument is required
     * @param name       The argument name
     * @param names      Names of the sub-arguments (in order)
     * @param types      Types of the sub-arguments (in order)
     * @param parserPair The sub arguments
     * @param mapper     Mapper that maps the sub-arguments to the output type
     * @param valueType  The output type
     */
    @SuppressWarnings("unchecked")
    protected ArgumentPair(
            final boolean required,
            final @NonNull String name,
            final @NonNull Pair<@NonNull String, @NonNull String> names,
            final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> types,
            final @NonNull Pair<@NonNull ArgumentParser<C, U>, @NonNull ArgumentParser<C, V>> parserPair,
            final @NonNull BiFunction<@NonNull C, @NonNull Pair<@NonNull U, @NonNull V>, @NonNull O> mapper,
            final @NonNull TypeToken<O> valueType
    ) {
        super(required, name, names, parserPair, types, mapper, o -> Pair.of((U) o[0], (V) o[1]), valueType);
    }

    /**
     * Construct a builder for an argument pair
     *
     * @param manager Command manager
     * @param name    Argument name
     * @param names   Sub-argument names
     * @param types   Pair containing the types of the sub-arguments. There must be parsers for these types registered
     *                in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
     *                {@link CommandManager}
     * @param <C>     Command sender type
     * @param <U>     First parsed type
     * @param <V>     Second parsed type
     * @return Intermediary builder
     */
    public static <C, U, V> @NonNull ArgumentPairIntermediaryBuilder<C, U, V> of(
            final @NonNull CommandManager<C> manager,
            final @NonNull String name,
            final @NonNull Pair<@NonNull String,
                    @NonNull String> names,
            final @NonNull Pair<@NonNull Class<U>,
                    @NonNull Class<V>> types
    ) {
        final ParserRegistry<C> parserRegistry = manager.getParserRegistry();
        final ArgumentParser<C, U> firstParser = parserRegistry.createParser(
                TypeToken.get(types.getFirst()),
                ParserParameters.empty()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Could not create parser for primary type"));
        final ArgumentParser<C, V> secondaryParser = parserRegistry.createParser(
                TypeToken.get(types.getSecond()),
                ParserParameters.empty()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Could not create parser for secondary type"));
        return new ArgumentPairIntermediaryBuilder<>(true, name, names, Pair.of(firstParser, secondaryParser), types);
    }

    @SuppressWarnings("ALL")
    public static final class ArgumentPairIntermediaryBuilder<C, U, V> {

        private final boolean required;
        private final String name;
        private final Pair<ArgumentParser<C, U>, ArgumentParser<C, V>> parserPair;
        private final Pair<String, String> names;
        private final Pair<Class<U>, Class<V>> types;

        private ArgumentPairIntermediaryBuilder(
                final boolean required,
                final @NonNull String name,
                final @NonNull Pair<@NonNull String, @NonNull String> names,
                final @NonNull Pair<@NonNull ArgumentParser<@NonNull C, @NonNull U>,
                        @NonNull ArgumentParser<@NonNull C, @NonNull V>> parserPair,
                final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> types
        ) {
            this.required = required;
            this.name = name;
            this.names = names;
            this.parserPair = parserPair;
            this.types = types;
        }

        /**
         * Create a simple argument pair that maps to a pair
         *
         * @return Argument pair
         */
        public @NonNull ArgumentPair<@NonNull C, @NonNull U, @NonNull V, @NonNull Pair<@NonNull U, @NonNull V>> simple() {
            return new ArgumentPair<C, U, V, Pair<U, V>>(
                    this.required,
                    this.name,
                    this.names,
                    this.types,
                    this.parserPair,
                    (sender, pair) -> pair,
                    new TypeToken<Pair<U, V>>() {
                    }
            );
        }

        /**
         * Create an argument pair that maps to a specific type
         *
         * @param clazz  Output class
         * @param mapper Output mapper
         * @param <O>    Output type
         * @return Created pair
         */
        public <O> @NonNull ArgumentPair<C, U, V, O> withMapper(
                final @NonNull TypeToken<O> clazz,
                final @NonNull BiFunction<@NonNull C, @NonNull Pair<@NonNull U,
                        @NonNull V>, @NonNull O> mapper
        ) {
            return new ArgumentPair<C, U, V, O>(this.required, this.name, this.names, this.types, this.parserPair, mapper, clazz);
        }

        /**
         * Create an argument pair that maps to a specific type
         *
         * @param clazz  Output class
         * @param mapper Output mapper
         * @param <O>    Output type
         * @return Created pair
         */
        public <O> @NonNull ArgumentPair<@NonNull C, @NonNull U, @NonNull V, @NonNull O> withMapper(
                final @NonNull Class<O> clazz,
                final @NonNull BiFunction<@NonNull C, @NonNull Pair<@NonNull U, @NonNull V>, @NonNull O> mapper
        ) {
            return this.withMapper(TypeToken.get(clazz), mapper);
        }

    }

}
