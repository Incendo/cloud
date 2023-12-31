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
package cloud.commandframework.execution;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
final class ExecutionCoordinatorBuilderImpl<C> implements ExecutionCoordinator.Builder<C> {

    private @Nullable Executor parsingExecutor;
    private @Nullable Executor suggestionsExecutor;
    private @Nullable Executor executionSchedulingExecutor;
    private boolean synchronizeExecution = false;

    @Override
    public ExecutionCoordinator.@NonNull Builder<C> parsingExecutor(final @NonNull Executor executor) {
        Objects.requireNonNull(executor, "executor");
        this.parsingExecutor = executor;
        return this;
    }

    @Override
    public ExecutionCoordinator.@NonNull Builder<C> suggestionsExecutor(final @NonNull Executor executor) {
        Objects.requireNonNull(executor, "executor");
        this.suggestionsExecutor = executor;
        return this;
    }

    @Override
    public ExecutionCoordinator.@NonNull Builder<C> executionSchedulingExecutor(final @NonNull Executor executor) {
        Objects.requireNonNull(executor, "executor");
        this.executionSchedulingExecutor = executor;
        return this;
    }

    @Override
    public ExecutionCoordinator.@NonNull Builder<C> synchronizeExecution() {
        this.synchronizeExecution = true;
        return this;
    }

    @Override
    public @NonNull ExecutionCoordinator<C> build() {
        return new ExecutionCoordinatorImpl<>(
                this.parsingExecutor,
                this.suggestionsExecutor,
                this.executionSchedulingExecutor,
                this.synchronizeExecution
        );
    }
}
