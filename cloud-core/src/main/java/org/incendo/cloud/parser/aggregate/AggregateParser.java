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
package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

/**
 * An argument parser that delegates to multiple inner {@link #components()} and transforms the aggregate results into
 * an output using the {@link #mapper()}.
 * <p>
 * You may either implement this interface to create a new parser type, or create an aggregate parser by using a
 * {@link #builder()}.
 * <p>
 * The parsers {@link #components()} will be invoked in the order of the returned collection.
 * When parsing, each parser will be invoked and the result will be stored in a {@link AggregateParsingContext}.
 * After parsing, the {@link #mapper()} will be invoked, turning the intermediate results into the output type which is then
 * returned by this parser.
 * <p>
 * When evaluating the suggestions for this parser, some {@link #components() component} parsers will be invoked, which allows
 * the suggestion providers to rely on the results from the preceding components.
 *
 * @param <C> the command sender type
 * @param <O> the output type
 */
@API(status = API.Status.STABLE)
public interface AggregateParser<C, O> extends ArgumentParser.FutureArgumentParser<C, O>, ParserDescriptor<C, O> {

    /**
     * Returns a new aggregate command parser builder. The builder is immutable, and each method returns
     * a new builder instance.
     *
     * @param <C> the command sender type
     * @return the builder
     */
    static <C> @NonNull AggregateParserBuilder<C> builder() {
        return new AggregateParserBuilder<>();
    }

    /**
     * Returns a new aggregate pair command parser builder. The builder is immutable, and each method returns
     * a new builder instance.
     *
     * @param firstName    the name of the first component
     * @param firstParser  the parser for the first component
     * @param secondName   the name of the second component
     * @param secondParser the parser for the second component
     * @param <C>          the command sender type
     * @param <U>          the type of the first component
     * @param <V>          the type of the second component
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    static <C, U, V> @NonNull AggregateParserPairBuilder<C, U, V, Pair<U, V>> pairBuilder(
            final @NonNull String firstName,
            final @NonNull ParserDescriptor<C, U> firstParser,
            final @NonNull String secondName,
            final @NonNull ParserDescriptor<C, V> secondParser
    ) {
        return new AggregateParserPairBuilder<>(
                CommandComponent.builder(firstName, firstParser).build(),
                CommandComponent.builder(secondName, secondParser).build(),
                AggregateParserPairBuilder.defaultMapper(),
                (TypeToken<Pair<U, V>>) TypeToken.get(TypeFactory.parameterizedClass(
                        Pair.class,
                        GenericTypeReflector.box(firstParser.valueType().getType()),
                        GenericTypeReflector.box(secondParser.valueType().getType())
                ))
        );
    }

    /**
     * Returns a new aggregate triplet command parser builder. The builder is immutable, and each method returns
     * a new builder instance.
     *
     * @param firstName    the name of the first component
     * @param firstParser  the parser for the first component
     * @param secondName   the name of the second component
     * @param secondParser the parser for the second component
     * @param thirdName    the name of the third component
     * @param thirdParser  the parser for the third component
     * @param <C>          the command sender type
     * @param <U>          the type of the first component
     * @param <V>          the type of the second component
     * @param <Z>          the type of the third component
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    static <C, U, V, Z> @NonNull AggregateParserTripletBuilder<C, U, V, Z, Triplet<U, V, Z>> tripletBuilder(
            final @NonNull String firstName,
            final @NonNull ParserDescriptor<C, U> firstParser,
            final @NonNull String secondName,
            final @NonNull ParserDescriptor<C, V> secondParser,
            final @NonNull String thirdName,
            final @NonNull ParserDescriptor<C, Z> thirdParser
    ) {
        return new AggregateParserTripletBuilder<>(
                CommandComponent.builder(firstName, firstParser).build(),
                CommandComponent.builder(secondName, secondParser).build(),
                CommandComponent.builder(thirdName, thirdParser).build(),
                AggregateParserTripletBuilder.defaultMapper(),
                (TypeToken<Triplet<U, V, Z>>) TypeToken.get(TypeFactory.parameterizedClass(
                        Triplet.class,
                        GenericTypeReflector.box(firstParser.valueType().getType()),
                        GenericTypeReflector.box(secondParser.valueType().getType()),
                        GenericTypeReflector.box(thirdParser.valueType().getType())
                ))
        );
    }

    /**
     * Returns the inner components of the parser.
     *
     * @return an unmodifiable view of the inner components in the order they were defined in
     */
    @NonNull List<@NonNull CommandComponent<C>> components();

    /**
     * Returns the result mapper. It will be invoked after parsing to map the intermediate results into the output type.
     *
     * @return the result mapper
     */
    @NonNull AggregateResultMapper<C, O> mapper();

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    default @NonNull CompletableFuture<@NonNull ArgumentParseResult<O>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final AggregateParsingContext<C> aggregateParsingContext = AggregateParsingContext.argumentContext(this);
        CompletableFuture<ArgumentParseResult<Object>> future = CompletableFuture.completedFuture(null);
        for (final CommandComponent<C> component : this.components()) {
            future =
                    future.thenCompose(result -> {
                        if (result != null && result.failure().isPresent()) {
                            return ArgumentParseResult.failureFuture(result.failure().get());
                        }
                        // Skip whitespace between arguments.
                        commandInput.skipWhitespace(1);
                        // Fail if there's no remaining input.
                        if (commandInput.isEmpty()) {
                            return ArgumentParseResult.failureFuture(new AggregateParseException(
                                    commandContext,
                                    component
                            ));
                        }
                        return component.parser()
                                .parseFuture(commandContext, commandInput)
                                .thenApply(value -> {
                                    if (value.parsedValue().isPresent()) {
                                        final CloudKey key = CloudKey.of(component.name(), component.valueType());
                                        aggregateParsingContext.store(key, value.parsedValue().get());
                                    } else if (value.failure().isPresent()) {
                                        return ArgumentParseResult.failure(new AggregateParseException(
                                                commandContext,
                                                "",
                                                component,
                                                value.failure().get()
                                        ));
                                    }
                                    return (ArgumentParseResult<Object>) value;
                                });
                    });
        }
        return future.thenCompose(result -> {
            if (result != null && result.failure().isPresent()) {
                return ((ArgumentParseResult<O>) result).asFuture();
            }
            return this.mapper().map(commandContext, aggregateParsingContext);
        });
    }

    @Override
    default @NonNull SuggestionProvider<C> suggestionProvider() {
        return new AggregateSuggestionProvider<>(this);
    }

    @Override
    default @NonNull ArgumentParser<C, O> parser() {
        return this;
    }


    @API(status = API.Status.STABLE)
    final class AggregateParseException extends ParserException {

        private AggregateParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull String input,
                final @NonNull CommandComponent<?> component,
                final @NonNull Throwable cause
        ) {
            super(
                    cause,
                    AggregateParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("component", component.name()),
                    CaptionVariable.of("failure", cause.getMessage())
            );
        }

        private AggregateParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull CommandComponent<?> component
        ) {
            super(
                    AggregateParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT,
                    CaptionVariable.of("component", component.name())
            );
        }
    }
}
