//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Registry containing mappings between {@link Class classes} and {@link ParameterInjector injectors}
 *
 * @param <C> Command sender type
 * @since 1.2.0
 */
public final class ParameterInjectorRegistry<C> {

    private volatile int injectorCount = 0;
    private final Map<Class<?>, List<ParameterInjector<C, ?>>> injectors = new HashMap<>();

    /**
     * Register an injector for a particular type
     *
     * @param clazz    Type that the injector should inject for. This type will matched using
     *                 {@link Class#isAssignableFrom(Class)}
     * @param injector The injector that should inject the value into the command method
     * @param <T>      Injected type
     */
    public synchronized <T> void registerInjector(
            final @NonNull Class<T> clazz,
            final @NonNull ParameterInjector<C, T> injector
    ) {
        this.injectors.computeIfAbsent(clazz, missingClass -> new LinkedList<>()).add(injector);
        this.injectorCount++;
    }

    /**
     * Get a collection of all injectors that could potentially inject a value of the given type
     *
     * @param clazz Type to query for
     * @param <T>   Generic type
     * @return Immutable collection containing all injectors that could potentially inject a value of the given type
     */
    public synchronized <T> @NonNull Collection<@NonNull ParameterInjector<C, ?>> injectors(
            final @NonNull Class<T> clazz
    ) {
        final List<@NonNull ParameterInjector<C, ?>> injectors = new ArrayList<>(this.injectorCount);
        for (final Map.Entry<Class<?>, List<ParameterInjector<C, ?>>> entry : this.injectors.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                injectors.addAll(entry.getValue());
            }
        }
        return Collections.unmodifiableCollection(injectors);
    }

}
