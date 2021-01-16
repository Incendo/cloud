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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class MultiDelegateAnnotationAccessor implements AnnotationAccessor {

    private final AnnotationAccessor[] accessors;

    MultiDelegateAnnotationAccessor(final @NonNull AnnotationAccessor@NonNull... accessors) {
        this.accessors = accessors;
    }

    @Override
    public <A extends Annotation> @Nullable A annotation(@NonNull final Class<A> clazz) {
        A instance = null;
        for (final AnnotationAccessor annotationAccessor : accessors) {
            instance = annotationAccessor.annotation(clazz);
            if (instance != null) {
                break;
            }
        }
        return instance;
    }

    @Override
    public @NonNull Collection<@NonNull Annotation> annotations() {
        final List<Annotation> annotationList = new LinkedList<>();
        for (final AnnotationAccessor annotationAccessor : accessors) {
            annotationList.addAll(annotationAccessor.annotations());
        }
        return Collections.unmodifiableCollection(annotationList);
    }

}
