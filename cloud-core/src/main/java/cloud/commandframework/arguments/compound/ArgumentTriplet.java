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
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiFunction;

/**
 * A compound argument consisting of three inner arguments
 *
 * @param <C> Command sender type
 * @param <U> First argument type
 * @param <V> Second argument type
 * @param <W> Third argument type
 * @param <O> Output type
 */
public class ArgumentTriplet<C, U, V, W, O> extends CompoundArgument<Triplet<U, V, W>, C, O> {

    /**
     * Create a new argument triplet.
     *
     * @param required      Whether or not the argument is required
     * @param name          The argument name
     * @param names         Names of the sub-arguments (in order)
     * @param types         Types of the sub-arguments (in order)
     * @param parserTriplet The sub arguments
     * @param mapper        Mapper that maps the sub-arguments to the output type
     * @param valueType     The output type
     */
    @SuppressWarnings("unchecked")
    protected ArgumentTriplet(
            final boolean required,
            final @NonNull String name,
            final @NonNull Triplet<@NonNull String, @NonNull String, @NonNull String> names,
            final @NonNull Triplet<@NonNull Class<U>, @NonNull Class<V>, @NonNull Class<W>> types,
            final @NonNull Triplet<@NonNull ArgumentParser<C, U>, @NonNull ArgumentParser<C, V>,
                    @NonNull ArgumentParser<C, W>> parserTriplet,
            final @NonNull BiFunction<@NonNull C,
                    @NonNull Triplet<U, @NonNull V, @NonNull W>, @NonNull O> mapper,
            final @NonNull TypeToken<O> valueType
    ) {
        super(required, name, names, parserTriplet, types, mapper, o -> Triplet.of((U) o[0], (V) o[1], (W) o[2]), valueType);
    }

    /**
     * Construct a builder for an argument triplet
     *
     * @param manager Command manager
     * @param name    Argument name
     * @param names   Sub-argument names
     * @param types   Triplet containing the types of the sub-arguments. There must be parsers for these types registered
     *                in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
     *                {@link CommandManager}
     * @param <C>     Command sender type
     * @param <U>     First parsed type
     * @param <V>     Second parsed type
     * @param <W>     Third type
     * @return Intermediary builder
     */
    public static <C, U, V, W> @NonNull ArgumentTripletIntermediaryBuilder<@NonNull C, @NonNull U, @NonNull V, @NonNull W>
    of(
            final @NonNull CommandManager<C> manager,
            final @NonNull String name,
            final @NonNull Triplet<@NonNull String, @NonNull String, @NonNull String> names,
            final @NonNull Triplet<@NonNull Class<U>, @NonNull Class<V>, @NonNull Class<W>> types
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
        final ArgumentParser<C, W> tertiaryParser = parserRegistry.createParser(
                TypeToken.get(types.getThird()),
                ParserParameters.empty()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Could not create parser for tertiary type"));
        return new ArgumentTripletIntermediaryBuilder<>(true, name, names,
                Triplet.of(firstParser, secondaryParser, tertiaryParser), types
        );
    }

    @SuppressWarnings("ALL")
    public static final class ArgumentTripletIntermediaryBuilder<C, U, V, W> {

        private final boolean required;
        private final String name;
        private final Triplet<ArgumentParser<C, U>, ArgumentParser<C, V>, ArgumentParser<C, W>> parserTriplet;
        private final Triplet<String, String, String> names;
        private final Triplet<Class<U>, Class<V>, Class<W>> types;

        private ArgumentTripletIntermediaryBuilder(
                final boolean required,
                final @NonNull String name,
                final @NonNull Triplet<@NonNull String, @NonNull String,
                        @NonNull String> names,
                final @NonNull Triplet<@NonNull ArgumentParser<C, U>,
                        @NonNull ArgumentParser<C, V>,
                        @NonNull ArgumentParser<C, W>> parserTriplet,
                final @NonNull Triplet<@NonNull Class<U>,
                        @NonNull Class<V>, @NonNull Class<W>> types
        ) {
            this.required = required;
            this.name = name;
            this.names = names;
            this.parserTriplet = parserTriplet;
            this.types = types;
        }

        /**
         * Create a simple argument triplet that maps to a triplet
         *
         * @return Argument triplet
         */
        public @NonNull ArgumentTriplet<@NonNull C, @NonNull U, @NonNull V,
                @NonNull W, Triplet<U, V, W>> simple() {
            return new ArgumentTriplet<>(
                    this.required,
                    this.name,
                    this.names,
                    this.types,
                    this.parserTriplet,
                    (sender, triplet) -> triplet,
                    new TypeToken<Triplet<U, V, W>>() {
                    }
            );
        }

        /**
         * Create an argument triplet that maps to a specific type
         *
         * @param clazz  Output class
         * @param mapper Output mapper
         * @param <O>    Output type
         * @return Created triplet
         */
        public <O> @NonNull ArgumentTriplet<@NonNull C, @NonNull U, @NonNull V,
                @NonNull W, @NonNull O> withMapper(
                final @NonNull TypeToken<O> clazz,
                final @NonNull BiFunction<@NonNull C, @NonNull Triplet<@NonNull U,
                        @NonNull V, @NonNull W>, @NonNull O> mapper
        ) {
            return new ArgumentTriplet<>(this.required, this.name, this.names, this.types, this.parserTriplet, mapper, clazz);
        }

        /**
         * Create an argument triplet that maps to a specific type
         *
         * @param clazz  Output class
         * @param mapper Output mapper
         * @param <O>    Output type
         * @return Created triplet
         */
        public <O> @NonNull ArgumentTriplet<C, U, V, W, O> withMapper(
                final @NonNull Class<O> clazz,
                final @NonNull BiFunction<@NonNull C, @NonNull Triplet<
                        @NonNull U, @NonNull V, @NonNull W>,
                        @NonNull O> mapper
        ) {
            return new ArgumentTriplet<>(this.required, this.name, this.names, this.types,
                    this.parserTriplet, mapper, TypeToken.get(clazz)
            );
        }

    }

}
