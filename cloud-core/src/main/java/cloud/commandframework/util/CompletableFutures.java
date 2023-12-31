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
package cloud.commandframework.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link CompletableFuture} extensions.
 *
 * @since 2.0.0
 */
@API(status = API.Status.INTERNAL, since = "2.0.0")
public final class CompletableFutures {

    private CompletableFutures() {
    }

    /**
     * Returns a failed future that has been exceptionally completed with the given {@code throwable}.
     *
     * <p>This is equivalent to {@code CompletableFuture.failedFuture(Throwable)} that was introduced in Java 9.</p>
     *
     * @param <T>       future type
     * @param throwable throwable to complete future with
     * @return the future
     */
    public static <T> @NonNull CompletableFuture<T> failedFuture(final @NonNull Throwable throwable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    /**
     * Creates a future that schedules a future on the provided executor.
     *
     * @param executor       executor
     * @param futureSupplier future factory
     * @param <T>            future result type
     * @return future
     */
    public static <T> CompletableFuture<T> scheduleOn(
            final Executor executor,
            final Supplier<CompletableFuture<T>> futureSupplier
    ) {
        return CompletableFuture.supplyAsync(futureSupplier, executor).thenCompose(Function.identity());
    }
}
