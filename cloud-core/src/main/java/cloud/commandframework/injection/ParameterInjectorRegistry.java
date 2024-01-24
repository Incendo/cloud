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
import cloud.commandframework.exceptions.InjectionException;
import cloud.commandframework.services.ServicePipeline;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Registry containing mappings between {@link Class} {@link Predicate Predicates}
 * and {@link ParameterInjector injectors}.
 *
 * The order injectors are tested is the same order they were registered in.
 *
 * @param <C> Command sender type
 * @since 1.2.0
 */
@SuppressWarnings("ALL")
@API(status = API.Status.STABLE, since = "1.2.0")
public final class ParameterInjectorRegistry<C> implements InjectionService<C> {

    private final List<Pair<Predicate<TypeToken<?>>, ParameterInjector<C, ?>>> injectors = new ArrayList<>();
    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();

    /**
     * Creates a new parameter injector registry
     */
    public ParameterInjectorRegistry() {
        this.servicePipeline.registerServiceType(new TypeToken<InjectionService<C>>() {
        }, this);
    }

    /**
     * Registers an injector for a particular type or any of it's assignable supertypes.
     *
     * @param <T>      injected type
     * @param clazz    type that the injector should inject for
     * @param injector the injector that should inject the value into the command method
     * @return {@code this}
     */
    public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
            final @NonNull Class<T> clazz,
            final @NonNull ParameterInjector<C, T> injector
    ) {
        return this.registerInjector(TypeToken.get(clazz), injector);
    }

    /**
     * Registers an injector for a particular type or any of it's assignable supertypes.
     *
     * @param <T>      injected type
     * @param type     type that the injector should inject for
     * @param injector the injector that should inject the value into the command method
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
            final @NonNull TypeToken<T> type,
            final @NonNull ParameterInjector<C, T> injector
    ) {
        return this.registerInjector(cl -> GenericTypeReflector.isSuperType(cl.getType(), type.getType()), injector);
    }

    /**
     * Registers an injector for a particular type predicate.
     *
     * <p>The predicate should only
     * return true if the injected type is assignable to the tested type. This predicate overload
     * is provided in addition to {@link #registerInjector(Class, ParameterInjector)} to allow
     * for exact, non-exact, or custom predicates, however is still bound by the aforementioned constraint.
     * Failure to adhere to this will result in runtime exceptions.</p>
     *
     * @param <T>       injected type
     * @param predicate a predicate that matches if the injector should be used for a type
     * @param injector  the injector that should inject the value into the command method
     * @return {@code this}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public synchronized <T> @This @NonNull ParameterInjectorRegistry<C> registerInjector(
            final @NonNull Predicate<TypeToken<?>> predicate,
            final @NonNull ParameterInjector<C, T> injector
    ) {
        this.injectors.add(Pair.of(predicate, injector));
        return this;
    }

    @Override
    public @Nullable Object handle(final @NonNull InjectionRequest<C> request) {
        for (final ParameterInjector<C, ?> injector : this.injectors(request.injectedType())) {
            final Object value = injector.create(request.commandContext(), request.annotationAccessor());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Attempts to get an injectable value for the given context.
     *
     * <p>This will consider all registered {@link InjectionService injection services}, and not just the
     * {@link ParameterInjector injectors} registered using {@link #registerInjector(Class, ParameterInjector)}.</p>
     *
     * @param <T>                type to inject
     * @param clazz              class to inject
     * @param context            the command context that requests the injection
     * @param annotationAccessor annotation accessor for the injection. If the object is requested without access to annotations,
     *                           use {@link AnnotationAccessor#empty()}
     * @return the injected value, if an injector was able to provide a value
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <T> @NonNull Optional<T> getInjectable(
            final @NonNull Class<T> clazz,
            final @NonNull CommandContext<C> context,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        return this.getInjectable(TypeToken.get(clazz), context, annotationAccessor);
    }

    /**
     * Attempts to get an injectable value for the given context.
     *
     * <p>This will consider all registered {@link InjectionService injection services}, and not just the
     * {@link ParameterInjector injectors} registered using {@link #registerInjector(Class, ParameterInjector)}.</p>
     *
     * @param <T>                type to inject
     * @param type               type to inject
     * @param context            the command context that requests the injection
     * @param annotationAccessor annotation accessor for the injection. If the object is requested without access to annotations,
     *                           use {@link AnnotationAccessor#empty()}
     * @return the injected value, if an injector was able to provide a value
     * @throws InjectionException if any of the {@link InjectionService injection services} throws an exception
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public <T> @NonNull Optional<T> getInjectable(
            final @NonNull TypeToken<T> type,
            final @NonNull CommandContext<C> context,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        final InjectionRequest<C> request = InjectionRequest.of(context, type, annotationAccessor);
        try {
            final Object rawResult = this.servicePipeline.pump(request).through(new TypeToken<InjectionService<C>>() {
            }).complete();

            if (!request.injectedClass().isInstance(rawResult)) {
                throw new IllegalStateException(String.format(
                        "Injector returned type %s which is not an instance of %s",
                        rawResult.getClass().getName(),
                        request.injectedClass().getName()
                ));
            }
            @SuppressWarnings("unchecked")
            final T result = (T) rawResult;

            return Optional.of(result);
        } catch (final IllegalStateException ignored) {
            return Optional.empty();
        } catch (final InjectionException injectionException) {
            throw injectionException;
        } catch (final Exception e) {
            throw new InjectionException(
                    String.format("Failed to inject type %s", type.getType().getTypeName()),
                    e
            );
        }
    }

    /**
     * Registers an injection service that will be able to provide injections using
     * {@link #getInjectable(Class, CommandContext, AnnotationAccessor)}.
     *
     * @param service Service implementation
     * @return {@code this}
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public @This @NonNull ParameterInjectorRegistry<C> registerInjectionService(final InjectionService<C> service) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<InjectionService<C>>() {
        }, service, Collections.emptyList());
        return this;
    }

    private synchronized <T> @NonNull Collection<@NonNull ParameterInjector<C, ?>> injectors(final @NonNull TypeToken<T> type) {
        return Collections.unmodifiableCollection(this.injectors.stream()
                .filter(pair -> pair.first().test(type))
                .map(Pair::second)
                .collect(Collectors.toList()));
    }
}
