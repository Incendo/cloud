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
package cloud.commandframework.annotations.injection;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Triplet;
import com.google.inject.BindingAnnotation;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link InjectionService Injection service} that injects using a Guice {@link Injector}
 * <p>
 * You should not put {@link Inject} annotation on your methods.
 * <p>
 * Ignore the warning says 'Binding annotation @YourAnnotation without @Inject declared'.
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
@API(status = API.Status.STABLE, since = "1.4.0")
public final class GuiceInjectionService<C> implements InjectionService<C> {

    private final Injector injector;

    private GuiceInjectionService(final @NonNull Injector injector) {
        this.injector = injector;
    }

    /**
     * Create a new Guice injection service that wraps the given injector
     *
     * @param injector Injector to wrap
     * @param <C>      Command sender type
     * @return the created injection service
     */
    public static <C> GuiceInjectionService<C> create(final @NonNull Injector injector) {
        return new GuiceInjectionService<>(injector);
    }

    @Override
    @SuppressWarnings("EmptyCatch")
    public @Nullable Object handle(final @NonNull Triplet<CommandContext<C>, Class<?>, AnnotationAccessor> triplet) {
        try {
            return this.injector.getInstance(this.createKey(triplet.getSecond(), triplet.getThird()));
        } catch (final ConfigurationException ignored) {}
        return null;
    }

    /**
     * Create a Guice key for the given class and annotation accessor.
     * <p>
     * If the annotation accessor contains a binding annotation, the key will be created with that annotation.
     * Otherwise, the key will be created without a binding annotation.
     * <p>
     * If the annotation accessor contains multiple binding annotations, the first one will be used.
     *
     * @param cls                Class to create key for.
     * @param annotationAccessor Annotation accessor to create key for.
     * @param <T>                Type of the key.
     *
     * @return The created key.
     *
     * @see Key
     * @see BindingAnnotation
     */
    @NonNull
    private <T> Key<T> createKey(@NonNull final Class<T> cls, @NonNull final AnnotationAccessor annotationAccessor) {
        final Annotation bindingAnnotation = annotationAccessor
                .annotations()
                .stream()
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
                .findFirst()
                .orElse(null);
        if (bindingAnnotation == null) {
            return Key.get(cls);
        } else {
            return Key.get(cls, bindingAnnotation);
        }
    }
}
