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

import java.util.concurrent.CompletableFuture;

/**
 * Utility responsible for synchronizing {@link TaskRecipeStep task recipe steps}
 */
public interface TaskSynchronizer {

    /**
     * Accept input into the consumer synchronously
     *
     * @param input    Input to pass to the consumer
     * @param consumer Consumer of the input
     * @param <I>      Input type
     * @return Future that completes when the consumer is done
     */
    <I> CompletableFuture<Void> runSynchronous(@NonNull I input, @NonNull TaskConsumer<I> consumer);

    /**
     * Produce output from accepted input synchronously
     *
     * @param input    Input to pass to the function
     * @param function Function that produces the output
     * @param <I>      Input type
     * @param <O>      Output type
     * @return Future that completes with the output
     */
    <I, O> CompletableFuture<O> runSynchronous(@NonNull I input, @NonNull TaskFunction<I, O> function);

    /**
     * Accept input into the consumer asynchronously
     *
     * @param input    Input to pass to the consumer
     * @param consumer Consumer of the input
     * @param <I>      Input type
     * @return Future that completes when the consumer is done
     */
    <I> CompletableFuture<Void> runAsynchronous(@NonNull I input, @NonNull TaskConsumer<I> consumer);

    /**
     * Produce output from accepted input asynchronously
     *
     * @param input    Input to pass to the function
     * @param function Function that produces the output
     * @param <I>      Input type
     * @param <O>      Output type
     * @return Future that completes with the output
     */
    <I, O> CompletableFuture<O> runAsynchronous(@NonNull I input, @NonNull TaskFunction<I, O> function);

}
