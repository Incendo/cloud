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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Tuple;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Compound argument
 *
 * @param <T> Tuple type
 * @param <C> Command sender type
 * @param <O> Output type
 */
public class CompoundArgument<T extends Tuple, C, O> extends CommandArgument<C, O> {

    private final Tuple types;
    private final Tuple names;
    private final Tuple parserTuple;

    /**
     * Construct a Compound Argument
     *
     * @param required     Whether or not the argument is required
     * @param name         The argument name
     * @param names        Names of the sub-arguments (in order)
     * @param parserTuple  The sub arguments
     * @param types        Types of the sub-arguments (in order)
     * @param mapper       Mapper that maps the sub-arguments to the output type
     * @param tupleFactory Function to use when creating tuple
     * @param valueType    The output type
     */
    public CompoundArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull Tuple names,
            final @NonNull Tuple parserTuple,
            final @NonNull Tuple types,
            final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
            final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory,
            final @NonNull TypeToken<O> valueType
    ) {
        super(
                required,
                name,
                new CompoundParser<>(parserTuple, mapper, tupleFactory),
                "",
                valueType,
                null
        );
        this.parserTuple = parserTuple;
        this.names = names;
        this.types = types;
    }

    /**
     * Get the tuple containing the internal parsers
     *
     * @return Internal parsers
     */
    public @NonNull Tuple getParserTuple() {
        return this.parserTuple;
    }

    /**
     * Get the argument names
     *
     * @return Argument names
     */
    public @NonNull Tuple getNames() {
        return this.names;
    }

    /**
     * Get the parser types
     *
     * @return Parser types
     */
    public @NonNull Tuple getTypes() {
        return this.types;
    }


    private static final class CompoundParser<T extends Tuple, C, O> implements ArgumentParser<C, O> {

        private final Object[] parsers;
        private final BiFunction<C, T, O> mapper;
        private final Function<Object[], T> tupleFactory;

        private CompoundParser(
                final @NonNull Tuple parserTuple,
                final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
                final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory
        ) {
            this.parsers = parserTuple.toArray();
            this.mapper = mapper;
            this.tupleFactory = tupleFactory;
        }

        @Override
        public @NonNull ArgumentParseResult<O> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final Object[] output = new Object[this.parsers.length];
            for (int i = 0; i < this.parsers.length; i++) {
                @SuppressWarnings("unchecked") final ArgumentParser<C, ?> parser = (ArgumentParser<C, ?>) this.parsers[i];
                final ArgumentParseResult<?> result = parser.parse(commandContext, inputQueue);
                if (result.getFailure().isPresent()) {
                    /* Return the failure */
                    return ArgumentParseResult.failure(result.getFailure().get());
                }
                /* Store the parsed value */
                output[i] = result.getParsedValue().orElse(null);
            }
            /*
             * We now know that we have complete output, as none of the parsers returned a failure.
             * Now check if the mapper threw any exceptions
             */
            try {
                return ArgumentParseResult.success(this.mapper.apply(
                        commandContext.getSender(),
                        this.tupleFactory.apply(output)
                ));
            } catch (final Exception e) {
                return ArgumentParseResult.failure(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            /*
            This method will be called n times, each time for each of the internal types.
            The problem is that we need to then know which of the parsers to forward the
            suggestion request to. This is done by storing the number of parsed subtypes
            in the context, so we can then extract that number and forward the request
             */
            final int argument = commandContext.getOrDefault("__parsing_argument__", 1) - 1;
            return ((ArgumentParser<C, ?>) this.parsers[argument]).suggestions(commandContext, input);
        }

    }

}
