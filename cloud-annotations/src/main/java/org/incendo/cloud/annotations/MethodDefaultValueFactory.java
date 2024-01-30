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
package org.incendo.cloud.annotations;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.DefaultValue;

@API(status = API.Status.INTERNAL)
final class MethodDefaultValueFactory<C, T> implements DefaultValueFactory<C, T> {

    private final MethodHandle methodHandle;

    MethodDefaultValueFactory(final @NonNull Method method, final @NonNull Object instance) {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
        } catch (final Exception e) {
            throw new RuntimeException(String.format(
                    "Failed to create the default value factory using method %s in class %s",
                    method.getName(),
                    method.getDeclaringClass().getName()
            ), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull DefaultValue<C, T> create(final @NonNull Parameter parameter) {
        try {
            return (DefaultValue<C, T>) this.methodHandle.invoke(parameter);
        } catch (final Throwable throwable) {
            throw new RuntimeException("Failed to create default value instance", throwable);
        }
    }
}
