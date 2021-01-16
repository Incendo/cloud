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

import cloud.commandframework.services.annotations.Order;
import cloud.commandframework.services.types.Service;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

class AnnotatedMethodService<Context, Result> implements Service<Context, Result> {

    private final ExecutionOrder executionOrder;
    private final MethodHandle methodHandle;
    private final Method method;
    private final Object instance;

    AnnotatedMethodService(
            final @NonNull Object instance,
            final @NonNull Method method
    )
            throws Exception {
        ExecutionOrder executionOrder = ExecutionOrder.SOON;
        try {
            final Order order = method.getAnnotation(Order.class);
            if (order != null) {
                executionOrder = order.value();
            }
        } catch (final Exception ignored) {
            // This is fine
        }
        this.instance = instance;
        this.executionOrder = executionOrder;
        method.setAccessible(true);
        this.methodHandle = MethodHandles.lookup().unreflect(method);
        this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Result handle(final @NonNull Context context) {
        try {
            return (Result) this.methodHandle.invoke(this.instance, context);
        } catch (final Throwable throwable) {
            new IllegalStateException(String
                    .format("Failed to call method service implementation '%s' in class '%s'",
                            method.getName(), instance.getClass().getCanonicalName()
                    ), throwable)
                    .printStackTrace();
        }
        return null;
    }

    @Override
    public @NonNull ExecutionOrder order() {
        return this.executionOrder;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AnnotatedMethodService<?, ?> that = (AnnotatedMethodService<?, ?>) o;
        return Objects.equals(this.methodHandle, that.methodHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.methodHandle);
    }

}
