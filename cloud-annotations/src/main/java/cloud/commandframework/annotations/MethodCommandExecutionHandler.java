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
package cloud.commandframework.annotations;

import cloud.commandframework.annotations.injection.ParameterInjectorRegistry;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.FlagContext;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.execution.CommandExecutionHandler;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A command execution handler that invokes a method.
 *
 * @param <C> Command sender type.
 * @since 1.6.0 (Was made public in 1.6.0)
 */
public class MethodCommandExecutionHandler<C> implements CommandExecutionHandler<C> {

    private final CommandMethodContext<C> context;
    private final Parameter[] parameters;
    private final MethodHandle methodHandle;
    private final AnnotationAccessor annotationAccessor;
    private final AnnotationParser<C> annotationParser;

    /**
     * Constructs a new method command execution handler
     *
     * @param context The context
     * @since 1.6.0
     */
    public MethodCommandExecutionHandler(
            final @NonNull CommandMethodContext<C> context
    ) throws Exception {
        this.context = context;
        this.methodHandle = MethodHandles.lookup().unreflect(context.method).bindTo(context.instance);
        this.parameters = context.method.getParameters();
        this.annotationAccessor = AnnotationAccessor.of(context.method);
        this.annotationParser = context.annotationParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final @NonNull CommandContext<C> commandContext) {
        /* Invoke the command method */
        try {
            this.methodHandle.invokeWithArguments(
                    this.createParameterValues(
                            commandContext,
                            commandContext.flags(),
                            this.parameters
                    )
            );
        } catch (final Error e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new CommandExecutionException(throwable, commandContext);
        }
    }

    /**
     * Creates a list containing the values for the given method parameters.
     *
     * @param commandContext The context
     * @param flagContext    The flag context
     * @param parameters     The parameters to create values for
     * @return A list containing all parameters, in order
     * @since 1.6.0
     */
    protected final List<Object> createParameterValues(
            final CommandContext<C> commandContext,
            final FlagContext flagContext,
            final Parameter[] parameters
    ) {
        final List<Object> arguments = new ArrayList<>(parameters.length);
        for (final Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Argument.class)) {
                final Argument argument = parameter.getAnnotation(Argument.class);

                final String argumentName;
                if (argument.value().equals(AnnotationParser.INFERRED_ARGUMENT_NAME)) {
                    argumentName = parameter.getName();
                } else {
                    argumentName = argument.value();
                }

                final CommandArgument<C, ?> commandArgument = this.context.commandArguments.get(argumentName);
                if (commandArgument.isRequired()) {
                    arguments.add(commandContext.get(argumentName));
                } else {
                    final Object optional = commandContext.getOptional(argumentName).orElse(null);
                    arguments.add(optional);
                }
            } else if (parameter.isAnnotationPresent(Flag.class)) {
                final Flag flag = parameter.getAnnotation(Flag.class);
                final String flagName = this.annotationParser.processString(flag.value());
                if (parameter.getType() == boolean.class) {
                    arguments.add(flagContext.isPresent(flagName));
                } else {
                    arguments.add(flagContext.getValue(flagName, null));
                }
            } else {
                if (parameter.getType().isAssignableFrom(commandContext.getSender().getClass())) {
                    arguments.add(commandContext.getSender());
                } else {
                    final Optional<?> value = this.context.injectorRegistry.getInjectable(
                            parameter.getType(),
                            commandContext,
                            AnnotationAccessor.of(AnnotationAccessor.of(parameter), this.annotationAccessor)
                    );
                    if (value.isPresent()) {
                        arguments.add(value.get());
                    } else {
                        throw new IllegalArgumentException(String.format(
                                "Could not create value for parameter '%s' of type '%s' in method '%s'",
                                parameter.getName(),
                                parameter.getType().getTypeName(),
                                this.methodHandle.toString()
                        ));
                    }
                }
            }
        }
        return arguments;
    }

    /**
     * Returns the command method context
     *
     * @return The context
     * @since 1.6.0
     */
    public @NonNull CommandMethodContext<C> context() {
        return this.context;
    }

    /**
     * Returns all parameters passed to the method
     *
     * @return The parameters
     * @since 1.6.0
     */
    public final @NonNull Parameter @NonNull [] parameters() {
        return this.parameters;
    }

    /**
     * Returns the compiled method handle for the command method.
     *
     * @return The method handle
     * @since 1.6.0
     */
    public final @NonNull MethodHandle methodHandle() {
        return this.methodHandle;
    }

    /**
     * The annotation accessor for the command method
     *
     * @return Annotation accessor
     * @since 1.6.0
     */
    public final AnnotationAccessor annotationAccessor() {
        return this.annotationAccessor;
    }

    /**
     * Context for command methods
     *
     * @param <C> Command sender type
     * @since 1.6.0
     */
    public static class CommandMethodContext<C> {

        private final Object instance;
        private final Map<String, CommandArgument<C, ?>> commandArguments;
        private final Method method;
        private final ParameterInjectorRegistry<C> injectorRegistry;
        private final AnnotationParser<C> annotationParser;

        CommandMethodContext(
                final @NonNull Object instance,
                final @NonNull Map<@NonNull String, @NonNull CommandArgument<@NonNull C, @NonNull ?>> commandArguments,
                final @NonNull Method method,
                final @NonNull AnnotationParser<C> annotationParser
        ) {
            this.instance = instance;
            this.commandArguments = commandArguments;
            this.method = method;
            this.method.setAccessible(true);
            this.injectorRegistry = annotationParser.getParameterInjectorRegistry();
            this.annotationParser = annotationParser;
        }

        /**
         * The instance that owns the command method
         *
         * @return The instance
         * @since 1.6.0
         */
        public @NonNull Object instance() {
            return this.instance;
        }

        /**
         * The command method
         *
         * @return The method
         * @since 1.6.0
         */
        public final @NonNull Method method() {
            return this.method;
        }

        /**
         * The compiled command arguments
         *
         * @return Compiled command arguments
         * @since 1.6.0
         */
        public final @NonNull Map<@NonNull String, @NonNull CommandArgument<C, ?>> commandArguments() {
            return this.commandArguments;
        }

        /**
         * The injector registry
         *
         * @return Injector registry
         * @since 1.6.0
         */
        public final @NonNull ParameterInjectorRegistry<C> injectorRegistry() {
            return this.injectorRegistry;
        }

        /**
         * The annotation parser
         *
         * @return Annotation parser
         * @since 1.7.0
         */
        public @NonNull AnnotationParser<C> annotationParser() {
            return this.annotationParser;
        }

    }

}
