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
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Repository that contains implementations for a given service type
 *
 * @param <Context>  Service context
 * @param <Response> Service response type
 */
public final class ServiceRepository<Context, Response> {

    private final Object lock = new Object();
    private final TypeToken<? extends Service<Context, Response>> serviceType;
    private final List<ServiceWrapper<? extends Service<Context, Response>>> implementations;

    private int registrationOrder = 0;

    /**
     * Create a new service repository for a given service type
     *
     * @param serviceType Service type
     */
    ServiceRepository(final @NonNull TypeToken<? extends Service<Context, Response>> serviceType) {
        this.serviceType = serviceType;
        this.implementations = new LinkedList<>();
    }

    /**
     * Register a new implementation for the service
     *
     * @param service Implementation
     * @param filters Filters that will be used to determine whether or not the service gets used
     * @param <T>     Type of the implementation
     */
    <T extends Service<Context, Response>> void registerImplementation(
            final @NonNull T service,
            final @NonNull Collection<Predicate<Context>> filters
    ) {
        synchronized (this.lock) {
            this.implementations.add(new ServiceWrapper<>(service, filters));
        }
    }

    /**
     * Get a queue containing all implementations
     *
     * @return Queue containing all implementations
     */
    @NonNull
    LinkedList<ServiceWrapper<? extends Service<Context, Response>>> getQueue() {
        synchronized (this.lock) {
            return new LinkedList<>(this.implementations);
        }
    }


    /**
     * Used to store {@link Service} implementations together with their state
     *
     * @param <T> Service type
     */
    final class ServiceWrapper<T extends Service<Context, Response>>
            implements Comparable<ServiceWrapper<T>> {

        private final boolean defaultImplementation;
        private final T implementation;
        private final Collection<Predicate<Context>> filters;

        private final int registrationOrder = ServiceRepository.this.registrationOrder++;
        private final ExecutionOrder executionOrder;

        private ServiceWrapper(
                final @NonNull T implementation,
                final @NonNull Collection<Predicate<Context>> filters
        ) {
            this.defaultImplementation = implementations.isEmpty();
            this.implementation = implementation;
            this.filters = filters;
            ExecutionOrder executionOrder = implementation.order();
            if (executionOrder == null) {
                final Order order = implementation.getClass().getAnnotation(Order.class);
                if (order != null) {
                    executionOrder = order.value();
                } else {
                    executionOrder = ExecutionOrder.SOON;
                }
            }
            this.executionOrder = executionOrder;
        }

        @NonNull
        T getImplementation() {
            return this.implementation;
        }

        @NonNull
        Collection<Predicate<Context>> getFilters() {
            return Collections.unmodifiableCollection(this.filters);
        }

        boolean isDefaultImplementation() {
            return this.defaultImplementation;
        }

        @Override
        public String toString() {
            return String
                    .format("ServiceWrapper{type=%s,implementation=%s}", serviceType.toString(),
                            TypeToken.get(implementation.getClass()).toString()
                    );
        }

        @Override
        public int compareTo(final @NonNull ServiceWrapper<T> other) {
            return Comparator.<ServiceWrapper<T>>comparingInt(
                    wrapper -> wrapper.isDefaultImplementation()
                            ? Integer.MIN_VALUE
                            : Integer.MAX_VALUE).thenComparingInt(wrapper -> wrapper.executionOrder.ordinal())
                    .thenComparingInt(wrapper -> wrapper.registrationOrder).compare(this, other);
        }

    }

}
