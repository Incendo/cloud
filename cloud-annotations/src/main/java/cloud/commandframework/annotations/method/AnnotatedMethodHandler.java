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
package cloud.commandframework.annotations.method;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.annotations.injection.ParameterInjectorRegistry;
import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handler that invokes an annotated method.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@API(status = API.Status.INTERNAL, since = "2.0.0")
public abstract class AnnotatedMethodHandler<C> {

    private final Parameter[] parameters;
    private final MethodHandle methodHandle;
    private final AnnotationAccessor annotationAccessor;
    private final ParameterInjectorRegistry<C> injectorRegistry;

    protected AnnotatedMethodHandler(
            final @NonNull Method method,
            final @NonNull Object instance,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) {
        try {
            this.parameters = method.getParameters();
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
            this.annotationAccessor = AnnotationAccessor.of(method);
            this.injectorRegistry = injectorRegistry;
        } catch (final Exception exception) {
            throw new AnnotatedMethodHandlerInitiationException(exception);
        }
    }

    /**
     * Returns the method parameters.
     *
     * @return the parameters
     */
    public @NonNull Parameter @NonNull[] parameters() {
        return this.parameters;
    }

    /**
     * Returns the method handle.
     *
     * @return the method handle
     */
    public @NonNull MethodHandle methodHandle() {
        return this.methodHandle;
    }

    /**
     * Returns the annotation accessor.
     *
     * @return the annotation accessor
     */
    public @NonNull AnnotationAccessor annotationAccessor() {
        return this.annotationAccessor;
    }

    /**
     * Returns the parameter injector registry.
     *
     * @return the injector registry
     */
    public @NonNull ParameterInjectorRegistry<C> injectorRegistry() {
        return this.injectorRegistry;
    }

    /**
     * Returns the value for the given {@code parameter}, if possible.
     *
     * @param parameter parameter to get value for
     * @param context   the command context
     * @return the value, or {@code null}
     */
    protected @Nullable ParameterValue getParameterValue(
            final @NonNull Parameter parameter,
            final @NonNull CommandContext<C> context
    ) {
        return null;
    }

    /**
     * Returns the injected value for the given {@code parameter}, if possible.
     *
     * @param parameter parameter to get value for
     * @param context   the command context
     * @return the value, or {@code null}
     */
    protected @Nullable ParameterValue getInjectedValue(
            final @NonNull Parameter parameter,
            final @NonNull CommandContext<C> context
    ) {
        final Optional<?> value = this.injectorRegistry.getInjectable(
                TypeToken.get(parameter.getParameterizedType()),
                context,
                AnnotationAccessor.of(AnnotationAccessor.of(parameter), this.annotationAccessor())
        );
        if (value.isPresent()) {
            return ParameterValue.of(parameter, value.get());
        }
        if (parameter.getType() == String.class) {
            return ParameterValue.of(parameter, parameter.getName());
        }
        return null;
    }

    /**
     * Creates a list of values for the method {@link #parameters()}.
     *
     * @param context command context
     * @return the parameter values
     */
    public @NonNull List<@NonNull ParameterValue> createParameterValues(final @NonNull CommandContext<C> context) {
        return this.createParameterValues(context, this.parameters());
    }

    /**
     * Creates a list of values for the given method {@code parameters}.
     *
     * @param context    command context
     * @param parameters parameters to get values for
     * @return the parameter values
     */
    public @NonNull List<@NonNull ParameterValue> createParameterValues(
            final @NonNull CommandContext<C> context,
            final @NonNull Parameter @NonNull[] parameters
    ) {
        return this.createParameterValues(context, parameters, Collections.emptyList());
    }

    /**
     * Creates a list of values for the given method {@code parameters}.
     *
     * @param context             command context
     * @param parameters          parameters to get values for
     * @param preDeterminedValues values that are already known
     * @return the parameter values
     */
    public @NonNull List<@NonNull ParameterValue> createParameterValues(
            final @NonNull CommandContext<C> context,
            final @NonNull Parameter @NonNull[] parameters,
            final @NonNull Collection<Object> preDeterminedValues
            ) {
        final List<ParameterValue> values = new ArrayList<>(parameters.length);
        outer: for (final Parameter parameter : parameters) {
            for (final Object preDeterminedValue : preDeterminedValues) {
               if (parameter.getType().isInstance(preDeterminedValue)) {
                   values.add(ParameterValue.of(parameter, preDeterminedValue));
                   continue outer;
               }
            }

            final ParameterValue contextualValue = this.getParameterValue(parameter, context);
            if (contextualValue != null) {
                values.add(contextualValue);
                continue;
            }

            if (parameter.getType().isAssignableFrom(context.sender().getClass())) {
                values.add(ParameterValue.of(parameter, context.sender()));
                continue;
            }

            final ParameterValue injectedValue = this.getInjectedValue(parameter, context);
            if (injectedValue != null) {
                values.add(injectedValue);
                continue;
            }

            throw new IllegalArgumentException(String.format(
                    "Could not create value for parameter '%s' of type '%s' in method '%s'",
                    parameter.getName(),
                    parameter.getType().getTypeName(),
                    this.methodHandle().toString()
            ));
        }
        return Collections.unmodifiableList(values);
    }
}
