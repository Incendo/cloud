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
package cloud.commandframework.arguments.aggregate;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.CloudKey;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument parser that delegates to multiple inner {@link #components()} and transforms the aggregate results into
 * an output using the {@link #mapper()}.
 * <p>
 * You may either implement this interface to create a new parser type, or create an aggregate parser by using a
 * {@link #builder()}.
 * <p>
 * The parsers {@link #components()} will be invoked in the order of the returned collection.
 * When parsing, each parser will be invoked and the result will be stored in a {@link AggregateCommandContext}.
 * After parsing, the {@link #mapper()} will be invoked, turning the intermediate results into the output type which is then
 * returned by this parser.
 * <p>
 * When evaluating the suggestions for this parser, some {@link #components() component} parsers will be invoked, which allows
 * the suggestion providers to rely on the results from the preceding components.
 *
 * @param <C> the command sender type
 * @param <O> the output type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface AggregateCommandParser<C, O> extends ArgumentParser.FutureArgumentParser<C, O>, ParserDescriptor<C, O> {

    /**
     * Returns a new aggregate command parser builder. The builder is immutable, and each method returns
     * a new builder instance.
     *
     * @param <C> the command sender type
     * @return the builder
     */
    static <C> @NonNull AggregateCommandParserBuilder<C> builder() {
        return new AggregateCommandParserBuilder<>();
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
        final AggregateCommandContext<C> aggregateCommandContext = AggregateCommandContext.argumentContext(this);
        CompletableFuture<ArgumentParseResult<Object>> future = CompletableFuture.completedFuture(null);
        for (final CommandComponent<C> component : this.components()) {
            future =
                    future.thenCompose(result -> {
                        if (result != null && result.failure().isPresent()) {
                            return ArgumentParseResult.failureFuture(result.failure().get());
                        }
                        return component.parser()
                                .parseFuture(commandContext, commandInput.skipWhitespace(1))
                                .thenApply(value -> {
                                    if (value.parsedValue().isPresent()) {
                                        final CloudKey key = CloudKey.of(component.name(), component.valueType());
                                        aggregateCommandContext.store(key, value.parsedValue().get());
                                    }
                                    return (ArgumentParseResult<Object>) value;
                                });
                    });
        }
        return future.thenCompose(result -> {
            if (result != null && result.failure().isPresent()) {
                return ((ArgumentParseResult<O>) result).asFuture();
            }
            return this.mapper().map(commandContext, aggregateCommandContext);
        });
    }

    @Override
    default @NonNull SuggestionProvider<C> suggestionProvider() {
        return new AggregateSuggestionProvider<>(this);
    }

    @Override
    default int getRequestedArgumentCount() {
        return this.components().stream().map(CommandComponent::parser).mapToInt(ArgumentParser::getRequestedArgumentCount).sum();
    }

    @Override
    default @NonNull ArgumentParser<C, O> parser() {
        return this;
    }
}
