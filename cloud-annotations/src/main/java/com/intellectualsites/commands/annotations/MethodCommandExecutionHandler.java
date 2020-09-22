//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.annotations;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.execution.CommandExecutionHandler;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MethodCommandExecutionHandler<C> implements CommandExecutionHandler<C> {

    private final Parameter[] parameters;
    private final MethodHandle methodHandle;
    private final Map<String, CommandArgument<C, ?>> commandArguments;

    MethodCommandExecutionHandler(@Nonnull final Object instance,
                                  @Nonnull final Map<String, CommandArgument<C, ?>> commandArguments,
                                  @Nonnull final Method method) throws Exception {
        this.commandArguments = commandArguments;
        method.setAccessible(true);
        this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
        this.parameters = method.getParameters();
    }

    @Override
    public void execute(@Nonnull final CommandContext<C> commandContext) {
        final List<Object> arguments = new ArrayList<>(this.parameters.length);

        /* Bind parameters to context */
        for (final Parameter parameter : this.parameters) {
            if (parameter.isAnnotationPresent(Argument.class)) {
                final Argument argument = parameter.getAnnotation(Argument.class);
                final CommandArgument<C, ?> commandArgument = this.commandArguments.get(argument.value());
                if (commandArgument.isRequired()) {
                    arguments.add(commandContext.getRequired(argument.value()));
                } else {
                    final Object optional = commandContext.get(argument.value()).orElse(null);
                    arguments.add(optional);
                }
            } else {
                if (parameter.getType().isAssignableFrom(commandContext.getSender().getClass())) {
                    arguments.add(commandContext.getSender());
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Unknown command parameter '%s' in method '%s'",
                            parameter.getName(), this.methodHandle.toString()
                    ));
                }
            }
        }

        /* Invoke the command method */
        try {
            this.methodHandle.invokeWithArguments(arguments);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

}
