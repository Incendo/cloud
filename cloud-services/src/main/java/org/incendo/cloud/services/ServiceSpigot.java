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
package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.type.ConsumerService;
import org.incendo.cloud.services.type.Service;
import org.incendo.cloud.services.type.SideEffectService;

/**
 * Class that outputs results from the given context, using the specified service type
 *
 * @param <Context> Context type
 * @param <Result>  Result type
 */
public final class ServiceSpigot<Context, Result> {

    private final Context context;
    private final ServicePipeline pipeline;
    private final ServiceRepository<Context, Result> repository;

    ServiceSpigot(
            final @NonNull ServicePipeline pipeline,
            final @NonNull Context context,
            final @NonNull TypeToken<? extends Service<@NonNull Context, @NonNull Result>> type
    ) {
        this.context = context;
        this.pipeline = pipeline;
        this.repository = pipeline.getRepository(type);
    }

    /**
     * Returns the first result that is generated for the given context. This cannot return {@code null}.
     *
     * <p>If nothing manages to produce a result, an exception will be thrown. If the pipeline has been
     * constructed properly, this will never happen.</p>
     *
     * @return generated result
     * @throws IllegalStateException If no result was found. This only happens if the pipeline has not
     *                               been constructed properly. The most likely cause is a faulty
     *                               default implementation
     * @throws IllegalStateException If a {@link SideEffectService} returns {@code null}
     * @throws PipelineException     Any exceptions thrown during result retrieval from the
     *                               implementations will be wrapped by {@link PipelineException}. Use
     *                               {@link PipelineException#getCause()} to get the exception that
     *                               was thrown.
     * @throws PipelineException     Any exceptions thrown during filtering will be wrapped by {@link
     *                               PipelineException}. Use {@link PipelineException#getCause()} to
     *                               get the exception that was thrown.
     * @see PipelineException PipelineException wraps exceptions thrown during filtering and result
     *         retrieval
     */
    @SuppressWarnings("unchecked")
    public @NonNull Result complete()
            throws IllegalStateException, PipelineException {
        final LinkedList<? extends ServiceRepository<@NonNull Context, @NonNull Result>
                .ServiceWrapper<? extends Service<@NonNull Context, @NonNull Result>>>
                queue = this.repository.queue();
        queue.sort(null); // Sort using the built in comparator method
        ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>
                wrapper;
        boolean consumerService = false;
        while ((wrapper = queue.pollLast()) != null) {
            consumerService = wrapper.implementation() instanceof ConsumerService;
            if (!ServiceFilterHandler.INSTANCE.passes(wrapper, this.context)) {
                continue;
            }
            final Result result;
            try {
                result = wrapper.implementation().handle(this.context);
            } catch (final Exception e) {
                throw new PipelineException(String.format("Failed to retrieve result from %s", wrapper), e);
            }
            if (wrapper.implementation() instanceof SideEffectService) {
                if (result == null) {
                    throw new IllegalStateException(String.format("SideEffectService '%s' returned null", wrapper));
                } else if (result == State.ACCEPTED) {
                    return result;
                }
            } else if (result != null) {
                return result;
            }
        }
        // This is hack to make it so that the default
        // consumer implementation does not have to call #interrupt
        if (consumerService) {
            return (Result) State.ACCEPTED;
        }
        throw new IllegalStateException(
                "No service consumed the context. This means that the pipeline was not constructed properly.");
    }

    /**
     * Returns the first result that is generated for the given context.
     *
     * <p>If nothing manages to produce a result, an exception will be thrown. If the pipeline has been constructed properly,
     * this will never happen. The exception passed to the consumer will be unwrapped, in the case that it's a
     * {@link PipelineException}. Thus, the actual exception will be given instead of the wrapper.</p>
     *
     * @param consumer result consumer. If an exception was wrong, the result will be {@code null},
     *                 otherwise the exception will be non-null and the exception will be {@code
     *                 null}.
     * @throws IllegalStateException If no result was found. This only happens if the pipeline has not
     *                               been constructed properly. The most likely cause is a faulty
     *                               default implementation
     * @throws IllegalStateException If a {@link SideEffectService} returns {@code null}
     */
    public void complete(final @NonNull BiConsumer<Result, Throwable> consumer) {
        try {
            consumer.accept(this.complete(), null);
        } catch (final PipelineException pipelineException) {
            consumer.accept(null, pipelineException.getCause());
        } catch (final Exception e) {
            consumer.accept(null, e);
        }
    }

    /**
     * Returns the first result that is generated for the given context. This cannot return {@code null}.
     *
     * <p>If nothing manages to produce a result, an exception will be thrown. If the pipeline has been
     * constructed properly, this will never happen.</p>
     *
     * @return generated result
     */
    public @NonNull CompletableFuture<Result> completeAsynchronously() {
        return CompletableFuture.supplyAsync(this::complete, this.pipeline.executor());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    public @NonNull ServicePump<Result> forward() {
        return this.pipeline.pump(this.complete());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    public @NonNull CompletableFuture<ServicePump<Result>> forwardAsynchronously() {
        return this.completeAsynchronously().thenApply(this.pipeline::pump);
    }
}
