//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.execution.CommandExecutionHandler;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A command consists out of a chain of {@link com.intellectualsites.commands.components.CommandComponent command components}.
 *
 * @param <C> Command sender type
 */
public class Command<C extends CommandSender> {

    private final CommandComponent<C, ?>[] components;
    private final CommandExecutionHandler<C> commandExecutionHandler;

    protected Command(@Nonnull final CommandComponent<C, ?>[] commandComponents, @Nonnull final CommandExecutionHandler<C> commandExecutionHandler) {
        this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
        if (this.components.length == 0) {
            throw new IllegalArgumentException("At least one command component is required");
        }
        // Enforce ordering of command components
        boolean foundOptional = false;
        for (final CommandComponent<C, ?> component : this.components) {
            if (foundOptional && component.isRequired()) {
                throw new IllegalArgumentException(String.format("Command component '%s' cannot be placed after an optional component", component.getName()));
            } else if (!component.isRequired()) {
                foundOptional = true;
            }
        }
        this.commandExecutionHandler = commandExecutionHandler;
    }

    /**
     * Create a new command builder
     *
     * @param commandName Base command component
     * @return Command builder
     */
    @Nonnull
    public static <C extends CommandSender> Builder<C> newBuilder(@Nonnull final String commandName) {
        return new Builder<>(Collections.singletonList(StaticComponent.required(commandName)),
                new CommandExecutionHandler.NullCommandExecutionHandler<>());
    }

    /**
     * Return a copy of the command component array
     *
     * @return Copy of the command component array
     */
    @Nonnull @SuppressWarnings("ALL")
    public CommandComponent<C, ?>[] getComponents() {
        return (CommandComponent<C, ?>[]) Arrays.asList(this.components).toArray();
    }

    /**
     * Get the command execution handler
     *
     * @return Command execution handler
     */
    @Nonnull public CommandExecutionHandler<C> getCommandExecutionHandler() {
        return this.commandExecutionHandler;
    }

    /**
     * Get the longest chain of similar components for
     * two commands
     *
     * @return List containing the longest shared component chain
     */
    public List<CommandComponent<C, ?>> getSharedComponentChain(@Nonnull final Command<C> other) {
        final List<CommandComponent<C, ?>> commandComponents = new LinkedList<>();
        for (int i = 0; i < this.components.length && i < other.components.length; i++) {
            if (this.components[i].equals(other.components[i])) {
                commandComponents.add(this.components[i]);
            } else {
                break;
            }
        }
        return commandComponents;
    }


    public static class Builder<C extends CommandSender> {

        private final List<CommandComponent<C, ?>> commandComponents;
        private final CommandExecutionHandler<C> commandExecutionHandler;

        private Builder(@Nonnull final List<CommandComponent<C, ?>> commandComponents, @Nonnull final CommandExecutionHandler<C> commandExecutionHandler) {
            this.commandComponents = commandComponents;
            this.commandExecutionHandler = commandExecutionHandler;
        }

        /**
         * Add a new command component to the command
         *
         * @param component Component to add
         * @param <T>       Component type
         * @return New builder instance with the command component inserted into the component list
         */
        @Nonnull
        public <T> Builder<C> withComponent(@Nonnull final CommandComponent<C, T> component) {
            final List<CommandComponent<C, ?>> commandComponents = new LinkedList<>(this.commandComponents);
            commandComponents.add(component);
            return new Builder<>(commandComponents, this.commandExecutionHandler);
        }

        /**
         * Specify the command execution handler
         *
         * @param commandExecutionHandler New execution handler
         * @return New builder instance using the command execution handler
         */
        @Nonnull
        public Builder<C> withHandler(@Nonnull final CommandExecutionHandler<C> commandExecutionHandler) {
            return new Builder<>(this.commandComponents, commandExecutionHandler);
        }

        /**
         * Build a command using the builder instance
         *
         * @return Built command
         */
        @Nonnull
        public Command<C> build() {
            return new Command<>(this.commandComponents.toArray(new CommandComponent[0]), this.commandExecutionHandler);
        }

    }

}
