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
package cloud.commandframework.annotations;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;

/**
 * Managed access for {@link java.lang.annotation.Annotation} instances
 *
 * @since 1.2.0
 */
public interface AnnotationAccessor {

    /**
     * Get a {@link AnnotationAccessor} that cannot access any annotations
     *
     * @return Empty annotation accessor
     * @since 1.3.0
     */
    static @NonNull AnnotationAccessor empty() {
        return new NullAnnotationAccessor();
    }

    /**
     * Get a {@link AnnotationAccessor} instance for a {@link AnnotatedElement}, such as
     * a {@link Class} or a {@link java.lang.reflect.Method}. This instance can then be
     * used as a proxy for retrieving the element's annotations
     *
     * @param element Annotated element that will be proxied by the accessor
     * @return Annotation accessor proxying the given annotated element
     */
    static @NonNull AnnotationAccessor of(final @NonNull AnnotatedElement element) {
        return new AnnotatedElementAccessor(element);
    }

    /**
     * Get a {@link AnnotationAccessor} instance that delegates to multiple {@link AnnotatedElement} instances.
     * The first accessor that provides a requested annotation will always be used
     *
     * @param accessors The accessor to delegate to
     * @return Annotation accessor that delegates to the given accessors (using their natural ordering)
     * @since 1.4.0
     */
    static @NonNull AnnotationAccessor of(final @NonNull AnnotationAccessor@NonNull... accessors) {
        return new MultiDelegateAnnotationAccessor(accessors);
    }

    /**
     * Get an annotation instance, if it's present. If the annotation
     * isn't available, this will return {@code null}
     *
     * @param clazz Annotation class
     * @param <A>   Annotation type
     * @return Annotation instance, or {@code null}
     */
    <A extends Annotation> @Nullable A annotation(@NonNull Class<A> clazz);

    /**
     * Get an immutable collection containing all of the annotations that
     * are accessible using the annotation accessor
     *
     * @return Immutable collection of annotations
     */
    @NonNull Collection<@NonNull Annotation> annotations();


    /**
     * Annotation accessor that cannot access any annotations
     *
     * @since 1.3.0
     */
    final class NullAnnotationAccessor implements AnnotationAccessor {

        @Override
        public <A extends Annotation> @Nullable A annotation(final @NonNull Class<A> clazz) {
           return null;
        }

        @Override
        public @NonNull Collection<@NonNull Annotation> annotations() {
            return Collections.emptyList();
        }

    }

}
