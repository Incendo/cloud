//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import com.google.common.reflect.TypeToken;
import cloud.commandframework.services.types.Service;

import javax.annotation.Nonnull;

/**
 * Class that forwards a context to a service type that consumes said context
 *
 * @param <Context> Context to pump
 */
public final class ServicePump<Context> {

    private final ServicePipeline servicePipeline;
    private final Context context;

    ServicePump(final ServicePipeline servicePipeline, final Context context) {
        this.servicePipeline = servicePipeline;
        this.context = context;
    }

    /**
     * Specify the service type that the context will be pumped through
     *
     * @param type     Service type
     * @param <Result> Result type
     * @return Service spigot instance
     */
    @Nonnull
    public <Result> ServiceSpigot<Context, Result> through(
            @Nonnull final TypeToken<? extends Service<Context, Result>> type) {
        return new ServiceSpigot<>(this.servicePipeline, this.context, type);
    }

    /**
     * Specify the service type that the context will be pumped through
     *
     * @param clazz    Service type
     * @param <Result> Result type
     * @return Service spigot instance
     */
    @Nonnull
    public <Result> ServiceSpigot<Context, Result> through(
            @Nonnull final Class<? extends Service<Context, Result>> clazz) {
        return this.through(TypeToken.of(clazz));
    }

}
