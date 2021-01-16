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
package cloud.commandframework.services;

import cloud.commandframework.services.types.ConsumerService;
import cloud.commandframework.services.types.Service;
import cloud.commandframework.services.types.SideEffectService;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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
     * Get the first result that is generated for the given context. This cannot return {@code null}.
     * If nothing manages to produce a result, an exception will be thrown. If the pipeline has been
     * constructed properly, this will never happen.
     *
     * @return Generated result
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
    public @NonNull Result getResult()
            throws IllegalStateException, PipelineException {
        final LinkedList<? extends ServiceRepository<@NonNull Context, @NonNull Result>
                .ServiceWrapper<? extends Service<@NonNull Context, @NonNull Result>>>
                queue = this.repository.getQueue();
        queue.sort(null); // Sort using the built in comparator method
        ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>
                wrapper;
        boolean consumerService = false;
        while ((wrapper = queue.pollLast()) != null) {
            consumerService = wrapper.getImplementation() instanceof ConsumerService;
            if (!ServiceFilterHandler.INSTANCE.passes(wrapper, this.context)) {
                continue;
            }
            final Result result;
            try {
                result = wrapper.getImplementation().handle(this.context);
            } catch (final Exception e) {
                throw new PipelineException(
                        String.format("Failed to retrieve result from %s", wrapper.toString()), e);
            }
            if (wrapper.getImplementation() instanceof SideEffectService) {
                if (result == null) {
                    throw new IllegalStateException(
                            String.format("SideEffectService '%s' returned null", wrapper.toString()));
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
     * Get the first result that is generated for the given context. If nothing manages to produce a
     * result, an exception will be thrown. If the pipeline has been constructed properly, this will
     * never happen. The exception passed to the consumer will be unwrapped, in the case that it's a
     * {@link PipelineException}. Thus, the actual exception will be given instead of the wrapper.
     *
     * @param consumer Result consumer. If an exception was wrong, the result will be {@code null},
     *                 otherwise the exception will be non-null and the exception will be {@code
     *                 null}.
     * @throws IllegalStateException If no result was found. This only happens if the pipeline has not
     *                               been constructed properly. The most likely cause is a faulty
     *                               default implementation
     * @throws IllegalStateException If a {@link SideEffectService} returns {@code null}
     */
    public void getResult(final @NonNull BiConsumer<Result, Throwable> consumer) {
        try {
            consumer.accept(getResult(), null);
        } catch (final PipelineException pipelineException) {
            consumer.accept(null, pipelineException.getCause());
        } catch (final Exception e) {
            consumer.accept(null, e);
        }
    }

    /**
     * Get the first result that is generated for the given context. This cannot return null. If
     * nothing manages to produce a result, an exception will be thrown. If the pipeline has been
     * constructed properly, this will never happen.
     *
     * @return Generated result
     */
    public @NonNull CompletableFuture<Result> getResultAsynchronously() {
        return CompletableFuture.supplyAsync(this::getResult, this.pipeline.getExecutor());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    public @NonNull ServicePump<Result> forward() {
        return this.pipeline.pump(this.getResult());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    public @NonNull CompletableFuture<ServicePump<Result>> forwardAsynchronously() {
        return this.getResultAsynchronously().thenApply(pipeline::pump);
    }

}
