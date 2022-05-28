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
package cloud.commandframework.execution;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handler that is invoked whenever a {@link Command} is executed
 * by a command sender
 *
 * @param <C> Command sender type
 */
@FunctionalInterface
public interface CommandExecutionHandler<C> {

    /**
     * Returns a {@link CommandExecutionHandler} that does nothing (no-op).
     *
     * @param <C> Command sender type
     * @return command execution handler that does nothing
     * @since 1.7.0
     */
    static <C> @NonNull CommandExecutionHandler<C> noOpCommandExecutionHandler() {
        return new NullCommandExecutionHandler<>();
    }

    /**
     * Returns a {@link CommandExecutionHandler} that delegates the given
     * {@code handlers} in sequence.
     * <p>
     * If any handler in the chain throws an exception, then no subsequent
     * handlers will be invoked.
     *
     * @param handlers The handlers to delegate to
     * @param <C>      Command sender type
     * @return multicast-delegate command execution handler
     * @since 1.7.0
     */
    static <C> @NonNull CommandExecutionHandler<C> delegatingExecutionHandler(
            final List<CommandExecutionHandler<C>> handlers
    ) {
        return new MulticastDelegateFutureCommandExecutionHandler<>(handlers);
    }

    /**
     * Handle command execution
     *
     * @param commandContext Command context
     */
    void execute(@NonNull CommandContext<C> commandContext);

    /**
     * Handle command execution
     *
     * @param commandContext Command context
     * @return future that completes when the command has finished execution
     * @since 1.6.0
     */
    default CompletableFuture<@Nullable Void> executeFuture(@NonNull CommandContext<C> commandContext) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            this.execute(commandContext);
            /* The command executed successfully */
            future.complete(null);
        } catch (final Throwable throwable) {
            future.completeExceptionally(throwable);
        }
        return future;
    }

    /**
     * Command execution handler that does nothing
     *
     * @param <C> Command sender type
     */
    class NullCommandExecutionHandler<C> implements CommandExecutionHandler<C> {

        @Override
        public void execute(final @NonNull CommandContext<C> commandContext) {
        }

    }

    /**
     * Handler that is invoked whenever a {@link Command} is executed
     * by a command sender
     *
     * @param <C> Command sender type
     * @since 1.6.0
     */
    @FunctionalInterface
    interface FutureCommandExecutionHandler<C> extends CommandExecutionHandler<C> {

        @Override
        @SuppressWarnings("FunctionalInterfaceMethodChanged")
        default void execute(
                @NonNull CommandContext<C> commandContext
        ) {
        }

        @Override
        CompletableFuture<@Nullable Void> executeFuture(
                @NonNull CommandContext<C> commandContext
        );

    }

    /**
     * Delegates to other handlers.
     *
     * @param <C> Command sender type
     * @see #delegatingExecutionHandler(List)
     * @since 1.7.0
     */
    final class MulticastDelegateFutureCommandExecutionHandler<C> implements FutureCommandExecutionHandler<C> {

        private final List<CommandExecutionHandler<C>> handlers;

        private MulticastDelegateFutureCommandExecutionHandler(
                final @NonNull List<@NonNull CommandExecutionHandler<C>> handlers
        ) {
            this.handlers = Collections.unmodifiableList(handlers);
        }

        @Override
        public CompletableFuture<@Nullable Void> executeFuture(
                @NonNull final CommandContext<C> commandContext
        ) {
            @MonotonicNonNull CompletableFuture<@Nullable Void> composedHandler = null;

            if (this.handlers.isEmpty()) {
                composedHandler = CompletableFuture.completedFuture(null);
            } else {
                for (final CommandExecutionHandler<C> handler : this.handlers) {
                    if (composedHandler == null) {
                        composedHandler = handler.executeFuture(commandContext);
                    } else {
                        composedHandler = composedHandler.thenCompose(
                                ignore -> handler.executeFuture(commandContext)
                        );
                    }
                }
            }

            return composedHandler;
        }

    }

}
