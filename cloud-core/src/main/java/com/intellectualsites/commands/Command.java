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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.execution.CommandExecutionHandler;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A command consists out of a chain of {@link com.intellectualsites.commands.components.CommandComponent command components}.
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
@SuppressWarnings("unused")
public class Command<C extends CommandSender, M extends CommandMeta> {

    @Nonnull private final List<CommandComponent<C, ?>> components;
    @Nonnull private final CommandExecutionHandler<C> commandExecutionHandler;
    @Nullable private final Class<? extends C> senderType;
    @Nonnull private final String commandPermission;
    @Nonnull private final M commandMeta;

    /**
     * Construct a new command
     *
     * @param commandComponents       Command components
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandComponent<C, ?>> commandComponents,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @Nonnull final String commandPermission,
                   @Nonnull final M commandMeta) {
        this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
        if (this.components.size() == 0) {
            throw new IllegalArgumentException("At least one command component is required");
        }
        // Enforce ordering of command components
        boolean foundOptional = false;
        for (final CommandComponent<C, ?> component : this.components) {
            if (component.getName().isEmpty()) {
                throw new IllegalArgumentException("Component names may not be empty");
            }
            if (foundOptional && component.isRequired()) {
                throw new IllegalArgumentException(
                        String.format("Command component '%s' cannot be placed after an optional component",
                                      component.getName()));
            } else if (!component.isRequired()) {
                foundOptional = true;
            }
        }
        this.commandExecutionHandler = commandExecutionHandler;
        this.senderType = senderType;
        this.commandPermission = commandPermission;
        this.commandMeta = commandMeta;
    }

    /**
     * Construct a new command
     *
     * @param commandComponents       Command components
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandComponent<C, ?>> commandComponents,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @Nonnull final M commandMeta) {
        this(commandComponents, commandExecutionHandler, senderType, "", commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandComponents       Command components
     * @param commandExecutionHandler Execution handler
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandComponent<C, ?>> commandComponents,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nonnull final String commandPermission,
                   @Nonnull final M commandMeta) {
        this(commandComponents, commandExecutionHandler, null, "", commandMeta);
    }

    /**
     * Create a new command builder
     *
     * @param commandName Base command component
     * @param commandMeta Command meta instance
     * @param aliases     Command aliases
     * @param <C>         Command sender type
     * @param <M>         Command meta type
     * @return Command builder
     */
    @Nonnull
    public static <C extends CommandSender, M extends CommandMeta> Builder<C, M> newBuilder(@Nonnull final String commandName,
                                                                                            @Nonnull final M commandMeta,
                                                                                            @Nonnull final String... aliases) {
        return new Builder<>(commandMeta, null, Collections.singletonList(StaticComponent.required(commandName, aliases)),
                             new CommandExecutionHandler.NullCommandExecutionHandler<>(), "");
    }

    /**
     * Return a copy of the command component array
     *
     * @return Copy of the command component array
     */
    @Nonnull
    public List<CommandComponent<C, ?>> getComponents() {
        return Collections.unmodifiableList(this.components);
    }

    /**
     * Get the command execution handler
     *
     * @return Command execution handler
     */
    @Nonnull
    public CommandExecutionHandler<C> getCommandExecutionHandler() {
        return this.commandExecutionHandler;
    }

    /**
     * Get the required sender type, if one has been specified
     *
     * @return Required sender type
     */
    @Nonnull
    public Optional<Class<? extends C>> getSenderType() {
        return Optional.ofNullable(this.senderType);
    }

    /**
     * Get the command permission
     *
     * @return Command permission
     */
    @Nonnull
    public String getCommandPermission() {
        return this.commandPermission;
    }

    /**
     * Get the command meta instance
     *
     * @return Command meta
     */
    @Nonnull
    public M getCommandMeta() {
        return this.commandMeta;
    }

    /**
     * Get the longest chain of similar components for
     * two commands
     *
     * @param other Command to compare to
     * @return List containing the longest shared component chain
     */
    public List<CommandComponent<C, ?>> getSharedComponentChain(@Nonnull final Command<C, M> other) {
        final List<CommandComponent<C, ?>> commandComponents = new LinkedList<>();
        for (int i = 0; i < this.components.size() && i < other.components.size(); i++) {
            if (this.components.get(i).equals(other.components.get(i))) {
                commandComponents.add(this.components.get(i));
            } else {
                break;
            }
        }
        return commandComponents;
    }


    /**
     * Builder for {@link Command} instances. The builder is immutable, and each
     * setter method will return a new builder instance.
     *
     * @param <C> Command sender type
     * @param <M> Command meta type
     */
    public static final class Builder<C extends CommandSender, M extends CommandMeta> {

        @Nonnull private final M commandMeta;
        @Nonnull private final List<CommandComponent<C, ?>> commandComponents;
        @Nonnull private final CommandExecutionHandler<C> commandExecutionHandler;
        @Nullable private final Class<? extends C> senderType;
        @Nonnull private final String commandPermission;

        private Builder(@Nonnull final M commandMeta,
                        @Nullable final Class<? extends C> senderType,
                        @Nonnull final List<CommandComponent<C, ?>> commandComponents,
                        @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                        @Nonnull final String commandPermission) {
            this.senderType = senderType;
            this.commandComponents = Objects.requireNonNull(commandComponents, "Components may not be null");
            this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
            this.commandPermission = Objects.requireNonNull(commandPermission, "Permission may not be null");
            this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
        }

        /**
         * Add a new command component to the command
         *
         * @param component Component to add
         * @param <T>       Component type
         * @return New builder instance with the command component inserted into the component list
         */
        @Nonnull
        public <T> Builder<C, M> component(@Nonnull final CommandComponent<C, T> component) {
            final List<CommandComponent<C, ?>> commandComponents = new LinkedList<>(this.commandComponents);
            commandComponents.add(component);
            return new Builder<>(this.commandMeta, this.senderType, commandComponents, this.commandExecutionHandler,
                                 this.commandPermission);
        }

        /**
         * Add a new command component by interacting with a constructed command component builder
         *
         * @param clazz           Component class
         * @param name            Component name
         * @param builderConsumer Builder consumer
         * @param <T>             Component type
         * @return New builder instance with the command component inserted into the component list
         */
        @Nonnull
        public <T> Builder<C, M> component(@Nonnull final Class<T> clazz,
                                           @Nonnull final String name,
                                           @Nonnull final Consumer<CommandComponent.Builder<C, T>> builderConsumer) {
            final CommandComponent.Builder<C, T> builder = CommandComponent.ofType(clazz, name);
            builderConsumer.accept(builder);
            return this.component(builder.build());
        }

        /**
         * Specify the command execution handler
         *
         * @param commandExecutionHandler New execution handler
         * @return New builder instance using the command execution handler
         */
        @Nonnull
        public Builder<C, M> handler(@Nonnull final CommandExecutionHandler<C> commandExecutionHandler) {
            return new Builder<>(this.commandMeta, this.senderType, this.commandComponents, commandExecutionHandler,
                                 this.commandPermission);
        }

        /**
         * Specify a required sender type
         *
         * @param senderType Required sender type
         * @return New builder instance using the command execution handler
         */
        @Nonnull
        public Builder<C, M> withSenderType(@Nonnull final Class<? extends C> senderType) {
            return new Builder<>(this.commandMeta, senderType, this.commandComponents, this.commandExecutionHandler,
                                 this.commandPermission);
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        @Nonnull
        public Builder<C, M> withPermission(@Nonnull final String permission) {
            return new Builder<>(this.commandMeta, this.senderType, this.commandComponents, this.commandExecutionHandler,
                                 permission);
        }

        /**
         * Build a command using the builder instance
         *
         * @return Built command
         */
        @Nonnull
        public Command<C, M> build() {
            return new Command<>(Collections.unmodifiableList(this.commandComponents), this.commandExecutionHandler,
                                 this.senderType, this.commandPermission, this.commandMeta);
        }

    }

}
