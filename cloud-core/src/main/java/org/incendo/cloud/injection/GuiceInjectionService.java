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
package org.incendo.cloud.injection;

import com.google.inject.BindingAnnotation;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

/**
 * {@link InjectionService Injection service} that injects using a Guice {@link Injector}
 *
 * <p>You should not put {@link com.google.inject.Inject} annotation on your methods.
 * Ignore the warning says 'Binding annotation @YourAnnotation without {@literal @Inject} declared'.</p>
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public final class GuiceInjectionService<C> implements InjectionService<C> {

    /**
     * Creates a Guice key for the given class and annotation accessor.
     *
     * <p>If the annotation accessor contains a binding annotation, the key will be created with that annotation.
     * Otherwise, the key will be created without a binding annotation.</p>
     *
     * <p>If the annotation accessor contains multiple binding annotations, the first one will be used.</p>
     *
     * @param <T>                type of the key
     * @param clazz              class to create key for
     * @param annotationAccessor annotation accessor to create key for
     * @return the created key
     */
    private static <T> @NonNull Key<T> createKey(
            final @NonNull Class<T> clazz,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        final Annotation bindingAnnotation = annotationAccessor
                .annotations()
                .stream()
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
                .findFirst()
                .orElse(null);
        if (bindingAnnotation == null) {
            return Key.get(clazz);
        } else {
            return Key.get(clazz, bindingAnnotation);
        }
    }

    private final Injector injector;

    private GuiceInjectionService(final @NonNull Injector injector) {
        this.injector = injector;
    }

    /**
     * Creates a new Guice injection service that wraps the given injector
     *
     * @param <C>      command sender type
     * @param injector injector to wrap
     * @return the created injection service
     */
    public static <C> @NonNull GuiceInjectionService<C> create(final @NonNull Injector injector) {
        return new GuiceInjectionService<>(injector);
    }

    @Override
    public @Nullable Object handle(final @NonNull InjectionRequest<C> request) {
        try {
            return this.injector.getInstance(createKey(request.injectedClass(), request.annotationAccessor()));
        } catch (final ConfigurationException ignored) {
            return null;
        }
    }
}
