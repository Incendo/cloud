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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.types.tuples.Tuple;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class CompoundParser<T extends Tuple, C, O> implements ArgumentParser.FutureArgumentParser<C, O> {

    private final Object[] names;
    private final Object[] types;
    private final Object[] parsers;
    private final BiFunction<C, T, O> mapper;
    private final Function<Object[], T> tupleFactory;

    CompoundParser(
            final @NonNull Tuple names,
            final @NonNull Tuple types,
            final @NonNull Tuple parserTuple,
            final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
            final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory
    ) {
        this.names = names.toArray();
        this.types = types.toArray();
        this.parsers = parserTuple.toArray();
        this.mapper = mapper;
        this.tupleFactory = tupleFactory;
    }

    CompoundParser(
            final @NonNull Object @NonNull [] names,
            final @NonNull Object @NonNull [] types,
            final @NonNull Object @NonNull [] parserTuple,
            final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
            final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory
    ) {
        this.names = names;
        this.types = types;
        this.parsers = parserTuple;
        this.mapper = mapper;
        this.tupleFactory = tupleFactory;
    }

    /**
     * Returns the argument names
     *
     * @return the argument names
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Object @NonNull [] names() {
        return this.names;
    }

    /**
     * Returns the argument parsers
     *
     * @return the argument parsers
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Object @NonNull [] parsers() {
        return this.parsers;
    }

    /**
     * Returns the parser types
     *
     * @return parser types
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Object @NonNull [] types() {
        return this.types;
    }

    @Override
    public @NonNull CompletableFuture<O> parseFuture(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        CompletableFuture<List<Object>> parsingFuture = CompletableFuture.completedFuture(Collections.emptyList());
        for (final Object parser : this.parsers) {
            parsingFuture = parsingFuture.thenCombine(
                    this.parseToList(parser, commandContext, commandInput),
                    (l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())
            );
        }
        return parsingFuture.thenApply(output -> this.mapper
                .apply(commandContext.getSender(), this.tupleFactory.apply(output.toArray(new Object[0]))));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private CompletableFuture<List<Object>> parseToList(
            final @NonNull Object parser,
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        return ((ArgumentParser) parser).parseFuture(context, input).thenApply(Collections::singletonList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull List<@NonNull Suggestion> suggestions(
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
