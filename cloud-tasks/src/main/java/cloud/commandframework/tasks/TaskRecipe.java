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
package cloud.commandframework.tasks;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A task recipe is a chain of tasks with optional synchronization steps,
 * that can be used to produce some sort of result from some input
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused", "overloads"})
public final class TaskRecipe {

    private final TaskSynchronizer synchronizer;
    private final LinkedHashMap<TaskRecipeStep, Boolean> recipeSteps = new LinkedHashMap<>();

    TaskRecipe(final @NonNull TaskSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    /**
     * Begin the recipe. This step always runs asynchronously.
     *
     * @param input Input
     * @param <I>   Input type
     * @return Function that maps the input to itself
     */
    public <I> @NonNull TaskRecipeComponentOutputting<I, I> begin(final @NonNull I input) {
        this.addAsynchronous(TaskFunction.identity());
        return new TaskRecipeComponentOutputting<>(input);
    }

    /**
     * Begin the recipe. This step always runs asynchronously.
     *
     * @return Function that maps the input to itself
     */
    public @NonNull TaskRecipeComponentOutputting<Object, Object> begin() {
        this.addAsynchronous(TaskFunction.identity());
        return new TaskRecipeComponentOutputting<>(new Object());
    }

    private void addAsynchronous(final TaskRecipeStep taskRecipeStep) {
        this.recipeSteps.put(taskRecipeStep, false);
    }

    private void addSynchronous(final TaskRecipeStep taskRecipeStep) {
        this.recipeSteps.put(taskRecipeStep, true);
    }

    private void execute(final @NonNull Object initialInput, final @NonNull Runnable callback) {
        final Iterator<Map.Entry<TaskRecipeStep, Boolean>> iterator = new LinkedHashMap<>(this.recipeSteps).entrySet().iterator();
        CompletableFuture completableFuture = CompletableFuture.completedFuture(initialInput);
        completableFuture.whenComplete(this.execute(iterator, callback));
    }

    private BiConsumer execute(
            final @NonNull Iterator<Map.Entry<TaskRecipeStep, Boolean>> iterator,
            final @NonNull Runnable callback
    ) {
        return (o, o2) -> {
            if (iterator.hasNext()) {
                final Map.Entry<TaskRecipeStep, Boolean> entry = iterator.next();
                final boolean synchronous = entry.getValue();

                CompletableFuture other;
                if (entry.getKey() instanceof TaskFunction) {
                    final TaskFunction function = (TaskFunction<?, ?>) entry.getKey();
                    if (synchronous) {
                        other = this.synchronizer.runSynchronous(o, function);
                    } else {
                        other = this.synchronizer.runAsynchronous(o, function);
                    }
                } else if (entry.getKey() instanceof TaskRunnable) {
                    final TaskRunnable runnable = (TaskRunnable) entry.getKey();
                    if (synchronous) {
                        other = this.synchronizer.runSynchronous(runnable);
                    } else {
                        other = this.synchronizer.runAsynchronous(runnable);
                    }
                } else {
                    final TaskConsumer consumer = (TaskConsumer<?>) entry.getKey();
                    if (synchronous) {
                        other = this.synchronizer.runSynchronous(o, consumer);
                    } else {
                        other = this.synchronizer.runAsynchronous(o, consumer);
                    }
                }

                other.whenComplete(this.execute(iterator, callback));
            } else {
                callback.run();
            }
        };
    }


    /**
     * Represents a partial recipe
     *
     * @param <I> Input type
     * @param <O> Output type
     */
    @SuppressWarnings("UnusedTypeParameter") // already in public API
    public final class TaskRecipeComponentOutputting<I, O> {

        private final Object initialInput;

        private TaskRecipeComponentOutputting(final @NonNull Object initialInput) {
            this.initialInput = initialInput;
        }

        /**
         * Add a new synchronous step, consuming the input of the earlier step
         *
         * @param function Function mapping the input to some output
         * @param <T>      Output type
         * @return New task recipe component
         */
        public <T> TaskRecipeComponentOutputting<O, T> synchronous(final @NonNull TaskFunction<O, T> function) {
            TaskRecipe.this.addSynchronous(function);
            return new TaskRecipeComponentOutputting<>(this.initialInput);
        }

        /**
         * Add a new asynchronous step, consuming the input of the earlier step
         *
         * @param function Function mapping the input to some output
         * @param <T>      Output type
         * @return New task recipe component
         */
        public <T> TaskRecipeComponentOutputting<O, T> asynchronous(final @NonNull TaskFunction<O, T> function) {
            TaskRecipe.this.addAsynchronous(function);
            return new TaskRecipeComponentOutputting<>(this.initialInput);
        }

        /**
         * Add a new synchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<O> synchronous(final @NonNull TaskConsumer<O> consumer) {
            TaskRecipe.this.addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new asynchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<O> asynchronous(final @NonNull TaskConsumer<O> consumer) {
            TaskRecipe.this.addAsynchronous(consumer);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new synchronous step, which ignores the input and does not produce any output
         *
         * @param runnable The step to run
         * @return New task recipe component
         * @since 2.0.0
         */
        public TaskRecipeComponentVoid<O> synchronous(final @NonNull TaskRunnable runnable) {
            TaskRecipe.this.addSynchronous(runnable);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new asynchronous step, which ignores the input and does not produce any output
         *
         * @param runnable The step to run
         * @return New task recipe component
         * @since 2.0.0
         */
        public TaskRecipeComponentVoid<O> asynchronous(final @NonNull TaskRunnable runnable) {
            TaskRecipe.this.addAsynchronous(runnable);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Execute the recipe
         *
         * @param callback Callback function
         */
        public void execute(final @NonNull Runnable callback) {
            TaskRecipe.this.execute(this.initialInput, callback);
        }

        /**
         * Execute the recipe
         */
        public void execute() {
            this.execute(() -> {
            });
        }
    }

    /**
     * Represents a partial recipe
     *
     * @param <I> Input type
     */
    public final class TaskRecipeComponentVoid<I> {

        private final Object initialInput;

        private TaskRecipeComponentVoid(final @NonNull Object initialInput) {
            this.initialInput = initialInput;
        }

        /**
         * Add a new synchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<I> synchronous(final @NonNull TaskConsumer<I> consumer) {
            TaskRecipe.this.addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new asynchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<I> asynchronous(final @NonNull TaskConsumer<I> consumer) {
            TaskRecipe.this.addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new asynchronous step, which ignores the input and does not produce any output
         *
         * @param runnable The step to run
         * @return New task recipe component
         * @since 2.0.0
         */
        public TaskRecipeComponentVoid<I> asynchronous(final @NonNull TaskRunnable runnable) {
            TaskRecipe.this.addAsynchronous(runnable);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Add a new synchronous step, which ignores the input and does not produce any output
         *
         * @param runnable The step to run
         * @return New task recipe component
         * @since 2.0.0
         */
        public TaskRecipeComponentVoid<I> synchronous(final @NonNull TaskRunnable runnable) {
            TaskRecipe.this.addSynchronous(runnable);
            return new TaskRecipeComponentVoid<>(this.initialInput);
        }

        /**
         * Execute the recipe
         *
         * @param callback Callback function
         */
        public void execute(final @NonNull Runnable callback) {
            TaskRecipe.this.execute(this.initialInput, callback);
        }

        /**
         * Execute the recipe
         */
        public void execute() {
            this.execute(() -> {
            });
        }
    }
}
