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
package cloud.commandframework.annotations.injection;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Triplet;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link InjectionService Injection service} that injects using a Guice {@link Injector}
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
public final class GuiceInjectionService<C> implements InjectionService<C> {

    private final Injector injector;

    private GuiceInjectionService(final @NonNull Injector injector) {
        this.injector = injector;
    }

    /**
     * Create a new Guice injection service that wraps the given injector
     *
     * @param injector Injector to wrap
     * @param <C> Command sender type
     * @return the created injection service
     */
    public static <C> GuiceInjectionService<C> create(final @NonNull Injector injector) {
        return new GuiceInjectionService<>(injector);
    }

    @Override
    @SuppressWarnings("EmptyCatch")
    public @Nullable Object handle(final @NonNull Triplet<CommandContext<C>, Class<?>, AnnotationAccessor> triplet) {
        try {
            return this.injector.getInstance(triplet.getSecond());
        } catch (final ConfigurationException ignored) {
        }
        return null;
    }

}
