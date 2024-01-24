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
package cloud.commandframework.execution;

import cloud.commandframework.context.CommandContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Delegates to other handlers.
 *
 * @param <C> command sender type
 * @see #delegatingExecutionHandler(List)
 */
@API(status = API.Status.INTERNAL)
final class MulticastDelegateFutureCommandExecutionHandler<C> implements
        CommandExecutionHandler.FutureCommandExecutionHandler<C> {

    private final List<CommandExecutionHandler<C>> handlers;

    MulticastDelegateFutureCommandExecutionHandler(
            final @NonNull List<@NonNull CommandExecutionHandler<C>> handlers
    ) {
        final List<CommandExecutionHandler<C>> unwrappedHandlers = new ArrayList<>();
        for (final CommandExecutionHandler<C> handler : handlers) {
            if (handler instanceof MulticastDelegateFutureCommandExecutionHandler) {
                unwrappedHandlers.addAll(((MulticastDelegateFutureCommandExecutionHandler<C>) handler).handlers);
            } else {
                unwrappedHandlers.add(handler);
            }
        }
        this.handlers = Collections.unmodifiableList(unwrappedHandlers);
    }

    @Override
    public CompletableFuture<@Nullable Void> executeFuture(
            final @NonNull CommandContext<C> commandContext
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
