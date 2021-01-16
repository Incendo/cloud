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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class MethodCommandExecutionHandler<C> implements CommandExecutionHandler<C> {

    private final Parameter[] parameters;
    private final MethodHandle methodHandle;
    private final Map<String, CommandArgument<C, ?>> commandArguments;
    private final ParameterInjectorRegistry<C> injectorRegistry;
    private final AnnotationAccessor annotationAccessor;

    MethodCommandExecutionHandler(
            final @NonNull Object instance,
            final @NonNull Map<@NonNull String, @NonNull CommandArgument<@NonNull C, @NonNull ?>> commandArguments,
            final @NonNull Method method,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) throws Exception {
        this.commandArguments = commandArguments;
        method.setAccessible(true);
        this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
        this.parameters = method.getParameters();
        this.injectorRegistry = injectorRegistry;
        this.annotationAccessor = AnnotationAccessor.of(method);
    }

    @Override
    public void execute(final @NonNull CommandContext<C> commandContext) {
        final List<Object> arguments = new ArrayList<>(this.parameters.length);
        final FlagContext flagContext = commandContext.flags();

        /* Bind parameters to context */
        for (final Parameter parameter : this.parameters) {
            if (parameter.isAnnotationPresent(Argument.class)) {
                final Argument argument = parameter.getAnnotation(Argument.class);
                final CommandArgument<C, ?> commandArgument = this.commandArguments.get(argument.value());
                if (commandArgument.isRequired()) {
                    arguments.add(commandContext.get(argument.value()));
                } else {
                    final Object optional = commandContext.getOptional(argument.value()).orElse(null);
                    arguments.add(optional);
                }
            } else if (parameter.isAnnotationPresent(Flag.class)) {
                final Flag flag = parameter.getAnnotation(Flag.class);
                if (parameter.getType() == boolean.class) {
                    arguments.add(flagContext.isPresent(flag.value()));
                } else {
                    arguments.add(flagContext.getValue(flag.value(), null));
                }
            } else {
                if (parameter.getType().isAssignableFrom(commandContext.getSender().getClass())) {
                    arguments.add(commandContext.getSender());
                } else {
                    final Optional<?> value = this.injectorRegistry.getInjectable(
                            parameter.getType(),
                            commandContext,
                            AnnotationAccessor.of(AnnotationAccessor.of(parameter), this.annotationAccessor)
                    );
                    if (value.isPresent()) {
                        arguments.add(value.get());
                    } else {
                        throw new IllegalArgumentException(String.format(
                                "Unknown command parameter '%s' in method '%s'",
                                parameter.getName(),
                                this.methodHandle.toString()
                        ));
                    }
                }
            }
        }

        /* Invoke the command method */
        try {
            this.methodHandle.invokeWithArguments(arguments);
        } catch (final Error e) {
            throw e;
        } catch (final Throwable throwable) {
            throw new CommandExecutionException(throwable, commandContext);
        }
    }

}
