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
package cloud.commandframework.annotations;

import cloud.commandframework.annotations.descriptor.ArgumentDescriptor;
import cloud.commandframework.annotations.descriptor.FlagDescriptor;
import cloud.commandframework.annotations.method.AnnotatedMethodHandler;
import cloud.commandframework.annotations.method.ParameterValue;
import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exception.CommandExecutionException;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.injection.ParameterInjectorRegistry;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A command execution handler that invokes a method.
 *
 * @param <C> command sender type.
 * @since 1.6.0 (Was made public in 1.6.0)
 */
public class MethodCommandExecutionHandler<C> extends AnnotatedMethodHandler<C> implements
        CommandExecutionHandler.FutureCommandExecutionHandler<C> {

    private final CommandMethodContext<C> context;
    private final AnnotationParser<C> annotationParser;
    private final boolean returnsFuture;

    /**
     * Constructs a new method command execution handler
     *
     * @param context The context
     */
    public MethodCommandExecutionHandler(final @NonNull CommandMethodContext<C> context) {
        super(context.method, context.instance, context.annotationParser.manager().parameterInjectorRegistry());
        this.context = context;
        this.annotationParser = context.annotationParser();
        this.returnsFuture = context.method().getReturnType().equals(CompletableFuture.class);
    }

    /**
     * Returns the command method context
     *
     * @return The context
     */
    public @NonNull CommandMethodContext<C> context() {
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Void> executeFuture(final @NonNull CommandContext<C> commandContext) {
        /* Invoke the command method */
        try {
            final Object result = this.methodHandle().invokeWithArguments(
                    this.createParameterValues(commandContext).stream()
                            .map(ParameterValue::value)
                            .collect(Collectors.toList())
            );
            if (this.returnsFuture) {
                return (CompletableFuture<Void>) result;
            }
            return CompletableFuture.completedFuture(null);
        } catch (final Error e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new CommandExecutionException(throwable, commandContext);
        }
    }

    @Override
    protected final @Nullable ParameterValue getParameterValue(
            final @NonNull Parameter parameter,
            final @NonNull CommandContext<C> context
    ) {
        final ArgumentDescriptor argumentDescriptor = this.context.argumentDescriptors.stream()
                .filter(descriptor -> descriptor.parameter().equals(parameter))
                .findFirst()
                .orElse(null);
        if (argumentDescriptor != null) {
            final String argumentName;
            if (argumentDescriptor.name().equals(AnnotationParser.INFERRED_ARGUMENT_NAME)) {
                argumentName = parameter.getName();
            } else {
                argumentName = this.annotationParser.processString(argumentDescriptor.name());
            }

            final CommandComponent<C> commandComponent = this.context.commandComponents.get(argumentName);
            if (commandComponent.required()) {
                return ParameterValue.of(parameter, context.get(argumentName), argumentDescriptor);
            }

            final Object optional = context.optional(argumentName).orElse(null);
            return ParameterValue.of(parameter, optional, argumentDescriptor);
        }

        final FlagDescriptor flagDescriptor = this.context.flagDescriptors.stream()
                .filter(descriptor -> descriptor.parameter().equals(parameter))
                .findFirst()
                .orElse(null);
        if (flagDescriptor != null) {
            if (parameter.getType().equals(boolean.class)) {
                return ParameterValue.of(
                        parameter,
                        context.flags().isPresent(flagDescriptor.name()),
                        flagDescriptor
                );
            } else if (flagDescriptor.repeatable() && parameter.getType().isAssignableFrom(List.class)) {
                return ParameterValue.of(
                        parameter,
                        context.flags().getAll(flagDescriptor.name()),
                        flagDescriptor
                );
            }
            return ParameterValue.of(
                    parameter,
                    context.flags().getValue(flagDescriptor.name(), null),
                    flagDescriptor
            );
        }

        return null;
    }

    /**
     * Context for command methods
     *
     * @param <C> command sender type
     */
    public static class CommandMethodContext<C> {

        private final Object instance;
        private final Map<String, CommandComponent<C>> commandComponents;
        private final Method method;
        private final ParameterInjectorRegistry<C> injectorRegistry;
        private final AnnotationParser<C> annotationParser;
        private final Collection<@NonNull ArgumentDescriptor> argumentDescriptors;
        private final Collection<@NonNull FlagDescriptor> flagDescriptors;

        CommandMethodContext(
                final @NonNull Object instance,
                final @NonNull Map<@NonNull String, @NonNull CommandComponent<C>> commandComponents,
                final @NonNull Collection<@NonNull ArgumentDescriptor> argumentDescriptors,
                final @NonNull Collection<@NonNull FlagDescriptor> flagDescriptors,
                final @NonNull Method method,
                final @NonNull AnnotationParser<C> annotationParser
        ) {
            this.instance = instance;
            this.commandComponents = commandComponents;
            this.method = method;
            this.method.setAccessible(true);
            this.injectorRegistry = annotationParser.manager().parameterInjectorRegistry();
            this.annotationParser = annotationParser;
            this.argumentDescriptors = argumentDescriptors;
            this.flagDescriptors = flagDescriptors;
        }

        /**
         * The instance that owns the command method
         *
         * @return The instance
         */
        public @NonNull Object instance() {
            return this.instance;
        }

        /**
         * The command method
         *
         * @return The method
         */
        public final @NonNull Method method() {
            return this.method;
        }

        /**
         * The compiled command arguments
         *
         * @return Compiled command arguments
         */
        public final @NonNull Map<@NonNull String, @NonNull CommandComponent<C>> commandComponents() {
            return this.commandComponents;
        }

        /**
         * The injector registry
         *
         * @return Injector registry
         */
        public final @NonNull ParameterInjectorRegistry<C> injectorRegistry() {
            return this.injectorRegistry;
        }

        /**
         * The annotation parser
         *
         * @return Annotation parser
         */
        public @NonNull AnnotationParser<C> annotationParser() {
            return this.annotationParser;
        }

        /**
         * Returns the argument descriptors
         *
         * @return the argument descriptors
         */
        @API(status = API.Status.STABLE)
        public @NonNull Collection<@NonNull ArgumentDescriptor> argumentDescriptors() {
            return Collections.unmodifiableCollection(this.argumentDescriptors);
        }

        /**
         * Returns the flag descriptors
         *
         * @return the flag descriptors
         */
        @API(status = API.Status.STABLE)
        public @NonNull Collection<@NonNull FlagDescriptor> flagDescriptors() {
            return Collections.unmodifiableCollection(this.flagDescriptors);
        }
    }
}
