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

import cloud.commandframework.services.annotations.ServiceImplementation;
import cloud.commandframework.services.types.Service;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

enum AnnotatedMethodServiceFactory {
    INSTANCE;

    @NonNull Map<? extends Service<?, ?>, TypeToken<? extends Service<?, ?>>> lookupServices(
            final @NonNull Object instance
    ) throws Exception {
        final Map<Service<?, ?>, TypeToken<? extends Service<?, ?>>> map = new HashMap<>();
        final Class<?> clazz = instance.getClass();
        for (final Method method : clazz.getDeclaredMethods()) {
            final ServiceImplementation serviceImplementation =
                    method.getAnnotation(ServiceImplementation.class);
            if (serviceImplementation == null) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException(String.format(
                        "Method '%s' in class '%s'" + " has wrong parameter count. Expected 1, got %d",
                        method.getName(), instance.getClass().getCanonicalName(),
                        method.getParameterCount()
                ));

            }
            map.put(
                    new AnnotatedMethodService<>(instance, method),
                    TypeToken.get(serviceImplementation.value())
            );
        }
        return map;
    }

}
