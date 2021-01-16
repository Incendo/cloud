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

import cloud.commandframework.services.types.Service;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Class that forwards a context to a service type that consumes said context
 *
 * @param <Context> Context to pump
 */
public final class ServicePump<Context> {

    private final ServicePipeline servicePipeline;
    private final Context context;

    ServicePump(
            final @NonNull ServicePipeline servicePipeline,
            final @NonNull Context context
    ) {
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
    public <Result> @NonNull ServiceSpigot<@NonNull Context, @NonNull Result> through(
            final @NonNull TypeToken<? extends Service<@NonNull Context, @NonNull Result>> type
    ) {
        return new ServiceSpigot<>(this.servicePipeline, this.context, type);
    }

    /**
     * Specify the service type that the context will be pumped through
     *
     * @param clazz    Service type
     * @param <Result> Result type
     * @return Service spigot instance
     */
    public <Result> @NonNull ServiceSpigot<@NonNull Context, @NonNull Result> through(
            final @NonNull Class<? extends Service<@NonNull Context, @NonNull Result>> clazz
    ) {
        return this.through(TypeToken.get(clazz));
    }

}
