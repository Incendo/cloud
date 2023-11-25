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
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.SimpleCloudKey;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument parser that delegates to multiple inner {@link #components()} and transforms the aggregate results into
 * an output using the {@link #mapper()}.
 *
 * @param <C> the command sender type
 * @param <O> the output type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface AggregateCommandParser<C, O> extends ArgumentParser.FutureArgumentParser<C, O> {

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
    default @NonNull CompletableFuture<@NonNull O> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final AggregateCommandContext<C> aggregateCommandContext = AggregateCommandContext.argumentContext(this);
        CompletableFuture<?> future = CompletableFuture.completedFuture(null);
        for (final CommandComponent<C> component : this.components()) {
            future =
                    future.thenCompose(result -> component.parser()
                            .parseFuture(commandContext, commandInput)
                            .whenComplete((value, exception) -> {
                                if (value == null || exception != null) {
                                    return;
                                }
                                aggregateCommandContext.store(SimpleCloudKey.of(component.name(), component.valueType()), value);
                            }));
        }
        return future.thenCompose(result -> this.mapper().map(commandContext, aggregateCommandContext));
    }

    @Override
    default int getRequestedArgumentCount() {
        return this.components().stream().map(CommandComponent::parser).mapToInt(ArgumentParser::getRequestedArgumentCount).sum();
    }
}
