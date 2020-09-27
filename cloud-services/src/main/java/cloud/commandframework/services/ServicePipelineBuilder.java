//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Builder for {@link ServicePipeline}
 */
@SuppressWarnings("unused")
public final class ServicePipelineBuilder {

    private Executor executor = Executors.newSingleThreadExecutor();

    ServicePipelineBuilder() {
    }

    /**
     * Construct a new {@link ServicePipeline} using the options specified in the builder
     *
     * @return New service pipeline
     */
    @Nonnull
    public ServicePipeline build() {
        return new ServicePipeline(this.executor);
    }

    /**
     * Set the executor that will be used by the pipeline when evaluating results asynchronously.
     * Unless specified, the pipeline will use a single threaded executor.
     *
     * @param executor New executor
     * @return Builder instance
     */
    @Nonnull
    public ServicePipelineBuilder withExecutor(@Nonnull final Executor executor) {
        this.executor = Preconditions.checkNotNull(executor, "Executor may not be null");
        return this;
    }

}
