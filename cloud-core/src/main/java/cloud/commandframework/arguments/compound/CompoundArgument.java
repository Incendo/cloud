//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Queue;
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

    CompoundArgument(final boolean required,
                     @Nonnull final String name,
                     @Nonnull final Tuple names,
                     @Nonnull final Tuple parserTuple,
                     @Nonnull final Tuple types,
                     @Nonnull final Function<T, O> mapper,
                     @Nonnull final Function<Object[], T> tupleFactory,
                     @Nonnull final TypeToken<O> valueType) {
        super(required,
              name,
              new CompoundParser<>(parserTuple, mapper, tupleFactory),
              "",
              valueType,
              null);
        this.parserTuple = parserTuple;
        this.names = names;
        this.types = types;
    }

    /**
     * Get the tuple containing the internal parsers
     *
     * @return Internal parsers
     */
    @Nonnull
    public Tuple getParserTuple() {
        return this.parserTuple;
    }

    /**
     * Get the argument names
     *
     * @return Argument names
     */
    @Nonnull
    public Tuple getNames() {
        return this.names;
    }

    /**
     * Get the parser types
     *
     * @return Parser types
     */
    @Nonnull
    public Tuple getTypes() {
        return this.types;
    }


    private static final class CompoundParser<T extends Tuple, C, O> implements ArgumentParser<C, O> {

        private final Object[] parsers;
        private final Function<T, O> mapper;
        private final Function<Object[], T> tupleFactory;

        private CompoundParser(@Nonnull final Tuple parserTuple,
                               @Nonnull final Function<T, O> mapper,
                               @Nonnull final Function<Object[], T> tupleFactory) {
            this.parsers = parserTuple.toArray();
            this.mapper = mapper;
            this.tupleFactory = tupleFactory;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<O> parse(@Nonnull final CommandContext<C> commandContext,
                                            @Nonnull final Queue<String> inputQueue) {
            final Object[] output = new Object[this.parsers.length];
            for (int i = 0; i < this.parsers.length; i++) {
                @SuppressWarnings("unchecked")
                final ArgumentParser<C, ?> parser = (ArgumentParser<C, ?>) this.parsers[i];
                final ArgumentParseResult<?> result = parser.parse(commandContext, inputQueue);
                if (result.getFailure().isPresent()) {
                    /* Return the failure */
                    return ArgumentParseResult.failure(result.getFailure().get());
                }
                /* Store the parsed value */
                output[i] = result.getParsedValue().orElse(null);
            }
            /* We now know that we have complete output, as none of the parsers returned a failure */
            return ArgumentParseResult.success(this.mapper.apply(this.tupleFactory.apply(output)));
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final String input) {
            /*
            This method will be called n times, each time for each of the internal types.
            The problem is that we need to then know which of the parsers to forward the
            suggestion request to. This is done by storing the number of parsed subtypes
            in the context, so we can then extract that number and forward the request
             */
            final int argument = commandContext.getOrDefault("__parsing_argument__", 1) - 1;
            System.out.printf("Compound argument suggestions: %d | %s\n", argument, input);

            //noinspection all
            return ((ArgumentParser<C, ?>) this.parsers[argument]).suggestions(commandContext, input);
        }
    }

}
