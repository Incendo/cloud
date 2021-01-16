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
package cloud.commandframework.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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
            addSynchronous(function);
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
            addAsynchronous(function);
            return new TaskRecipeComponentOutputting<>(this.initialInput);
        }

        /**
         * Add a new synchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<O> synchronous(final @NonNull TaskConsumer<O> consumer) {
            addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(initialInput);
        }

        /**
         * Add a new asynchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<O> asynchronous(final @NonNull TaskConsumer<O> consumer) {
            addAsynchronous(consumer);
            return new TaskRecipeComponentVoid<>(initialInput);
        }

        /**
         * Execute the recipe
         *
         * @param callback Callback function
         */
        public void execute(final @NonNull Runnable callback) {
            TaskRecipe.this.execute(initialInput, callback);
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
            addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(initialInput);
        }

        /**
         * Add a new asynchronous step, consuming the input of the earlier step
         *
         * @param consumer Consumer that consumes the input
         * @return New task recipe component
         */
        public TaskRecipeComponentVoid<I> asynchronous(final @NonNull TaskConsumer<I> consumer) {
            addSynchronous(consumer);
            return new TaskRecipeComponentVoid<>(initialInput);
        }

        /**
         * Execute the recipe
         *
         * @param callback Callback function
         */
        public void execute(final @NonNull Runnable callback) {
            TaskRecipe.this.execute(initialInput, callback);
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
