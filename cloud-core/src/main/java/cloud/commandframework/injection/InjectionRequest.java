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
package cloud.commandframework.injection;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.ImmutableImpl;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface InjectionRequest<C> {

    /**
     * Creates a new injection request.
     *
     * @param <C>                command sender type
     * @param context            context associated with the request
     * @param injectedType       type that is being injected
     * @param annotationAccessor associated annotation accessor
     * @return the request
     */
    static <C> @NonNull InjectionRequest<C> of(
            final @NonNull CommandContext<C> context,
            final @NonNull TypeToken<?> injectedType,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        return InjectionRequestImpl.of(context, injectedType, annotationAccessor);
    }

    /**
     * Creates a new injection request.
     *
     * @param <C>          command sender type
     * @param context      context associated with the request
     * @param injectedType type that is being injected
     * @return the request
     */
    static <C> @NonNull InjectionRequest<C> of(
            final @NonNull CommandContext<C> context,
            final @NonNull TypeToken<?> injectedType
    ) {
        return InjectionRequestImpl.of(context, injectedType, AnnotationAccessor.empty());
    }

    /**
     * Returns the context associated with the request.
     *
     * @return the context
     */
    @NonNull CommandContext<C> commandContext();

    /**
     * Returns the type that is being injected.
     *
     * @return the injected type
     */
    @NonNull TypeToken<?> injectedType();

    /**
     * Returns the type that is being injected.
     *
     * @return the injected type
     */
    @Value.Derived
    default @NonNull Class<?> injectedClass() {
        return GenericTypeReflector.erase(this.injectedType().getType());
    }

    /**
     * Returns an annotation accessor that can be used to access the annotations of the field that is being
     * injected.
     *
     * <p>If no field is injected then {@link AnnotationAccessor#empty()} will be returned.</p>
     *
     * @return the annotation accessor
     */
    @NonNull AnnotationAccessor annotationAccessor();
}
