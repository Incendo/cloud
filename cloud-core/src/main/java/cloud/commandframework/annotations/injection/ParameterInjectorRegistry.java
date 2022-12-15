//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.services.ServicePipeline;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
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

    private final List<Pair<Predicate<Class<?>>, ParameterInjector<C, ?>>> injectors = new ArrayList<>();
    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();

    /**
     * Create a new parameter injector registry
     */
    public ParameterInjectorRegistry() {
        this.servicePipeline.registerServiceType(new TypeToken<InjectionService<C>>() {
        }, this);
    }

    /**
     * Register an injector for a particular type or any of it's assignable supertypes.
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
        this.registerInjector(cl -> cl.isAssignableFrom(clazz), injector);
    }

    /**
     * Register an injector for a particular type predicate.
     *
     * <p>The predicate should only
     * return true if the injected type is assignable to the tested type. This predicate overload
     * is provided in addition to {@link #registerInjector(Class, ParameterInjector)} to allow
     * for exact, non-exact, or custom predicates, however is still bound by the aforementioned constraint.
     * Failure to adhere to this will result in runtime exceptions.</p>
     *
     * @param predicate A predicate that matches if the injector should be used for a type
     * @param injector The injector that should inject the value into the command method
     * @param <T>      Injected type
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public synchronized <T> void registerInjector(
            final @NonNull Predicate<Class<?>> predicate,
            final @NonNull ParameterInjector<C, T> injector
    ) {
        this.injectors.add(Pair.of(predicate, injector));
    }

    /**
     * Get a collection of all injectors that could potentially inject a value of the given type. This
     * does not include injectors from external injector services, instead it only uses injectors
     * registered using {@link #registerInjector(Class, ParameterInjector)}.
     *
     * @param clazz Type to query for
     * @param <T>   Generic type
     * @return Immutable collection containing all injectors that could potentially inject a value of the given type
     * @deprecated Inject directly instead of relying on this list
     */
    @Deprecated
    public synchronized <T> @NonNull Collection<@NonNull ParameterInjector<C, ?>> injectors(
            final @NonNull Class<T> clazz
    ) {
        return Collections.unmodifiableCollection(this.injectors.stream()
                .filter(pair -> pair.getFirst().test(clazz))
                .map(Pair::getSecond)
                .collect(Collectors.toList()));
    }

    @Override
    public @Nullable Object handle(final @NonNull Triplet<CommandContext<C>, Class<?>, AnnotationAccessor> triplet) {
        for (final ParameterInjector<C, ?> injector : this.injectors(triplet.getSecond())) {
            final Object value = injector.create(triplet.getFirst(), triplet.getThird());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Attempt to get an injectable value for the given context. This will consider all registered
     * {@link InjectionService injection services}, and not just the {@link ParameterInjector injectors}
     * registered using {@link #registerInjector(Class, ParameterInjector)}.
     *
     * @param clazz              Class of the to inject
     * @param context            The command context that requests the injection
     * @param annotationAccessor Annotation accessor for the injection. If the object is requested without access to annotations,
     *                           use {@link AnnotationAccessor#empty()}
     * @param <T>                Type to inject
     * @return The injected value, if an injector was able to provide a value
     * @since 1.4.0
     */
    @SuppressWarnings("EmptyCatch")
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <@NonNull T> @NonNull Optional<T> getInjectable(
            final @NonNull Class<T> clazz,
            final @NonNull CommandContext<C> context,
            final @NonNull AnnotationAccessor annotationAccessor
    ) {
        final Triplet<CommandContext<C>, Class<?>, AnnotationAccessor> triplet = Triplet.of(context, clazz, annotationAccessor);
        try {
            return Optional.of(clazz.cast(this.servicePipeline.pump(triplet).through(new TypeToken<InjectionService<C>>() {
            }).getResult()));
        } catch (final IllegalStateException ignored) {
        }
        return Optional.empty();
    }

    /**
     * Register an injection service that will be able to provide injections using
     * {@link #getInjectable(Class, CommandContext, AnnotationAccessor)}.
     *
     * @param service Service implementation
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public void registerInjectionService(final InjectionService<C> service) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<InjectionService<C>>() {
        }, service, Collections.emptyList());
    }
}
