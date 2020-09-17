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

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.StaticArgument;
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
 * A command consists out of a chain of {@link CommandArgument command arguments}.
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
@SuppressWarnings("unused")
public class Command<C extends CommandSender, M extends CommandMeta> {

    @Nonnull private final List<CommandArgument<C, ?>> arguments;
    @Nonnull private final CommandExecutionHandler<C> commandExecutionHandler;
    @Nullable private final Class<? extends C> senderType;
    @Nonnull private final String commandPermission;
    @Nonnull private final M commandMeta;

    /**
     * Construct a new command
     *
     * @param commandArguments       Command arguments
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandArgument<C, ?>> commandArguments,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @Nonnull final String commandPermission,
                   @Nonnull final M commandMeta) {
        this.arguments = Objects.requireNonNull(commandArguments, "Command arguments may not be null");
        if (this.arguments.size() == 0) {
            throw new IllegalArgumentException("At least one command argument is required");
        }
        // Enforce ordering of command arguments
        boolean foundOptional = false;
        for (final CommandArgument<C, ?> argument : this.arguments) {
            if (argument.getName().isEmpty()) {
                throw new IllegalArgumentException("Argument names may not be empty");
            }
            if (foundOptional && argument.isRequired()) {
                throw new IllegalArgumentException(
                        String.format("Command argument '%s' cannot be placed after an optional argument",
                                      argument.getName()));
            } else if (!argument.isRequired()) {
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
     * @param commandArguments       Command arguments
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandArgument<C, ?>> commandArguments,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @Nonnull final M commandMeta) {
        this(commandArguments, commandExecutionHandler, senderType, "", commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandArguments       Command arguments
     * @param commandExecutionHandler Execution handler
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@Nonnull final List<CommandArgument<C, ?>> commandArguments,
                   @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                   @Nonnull final String commandPermission,
                   @Nonnull final M commandMeta) {
        this(commandArguments, commandExecutionHandler, null, "", commandMeta);
    }

    /**
     * Create a new command builder
     *
     * @param commandName Base command argument
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
        return new Builder<>(null, commandMeta, null,
                             Collections.singletonList(StaticArgument.required(commandName, aliases)),
                             new CommandExecutionHandler.NullCommandExecutionHandler<>(), "");
    }

    /**
     * Return a copy of the command argument array
     *
     * @return Copy of the command argument array
     */
    @Nonnull
    public List<CommandArgument<C, ?>> getArguments() {
        return Collections.unmodifiableList(this.arguments);
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
     * Get the longest chain of similar arguments for
     * two commands
     *
     * @param other Command to compare to
     * @return List containing the longest shared argument chain
     */
    public List<CommandArgument<C, ?>> getSharedArgumentChain(@Nonnull final Command<C, M> other) {
        final List<CommandArgument<C, ?>> commandArguments = new LinkedList<>();
        for (int i = 0; i < this.arguments.size() && i < other.arguments.size(); i++) {
            if (this.arguments.get(i).equals(other.arguments.get(i))) {
                commandArguments.add(this.arguments.get(i));
            } else {
                break;
            }
        }
        return commandArguments;
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
        @Nonnull private final List<CommandArgument<C, ?>> commandArguments;
        @Nonnull private final CommandExecutionHandler<C> commandExecutionHandler;
        @Nullable private final Class<? extends C> senderType;
        @Nonnull private final String commandPermission;
        @Nullable private final CommandManager<C, M> commandManager;

        private Builder(@Nullable final CommandManager<C, M> commandManager,
                        @Nonnull final M commandMeta,
                        @Nullable final Class<? extends C> senderType,
                        @Nonnull final List<CommandArgument<C, ?>> commandArguments,
                        @Nonnull final CommandExecutionHandler<C> commandExecutionHandler,
                        @Nonnull final String commandPermission) {
            this.commandManager = commandManager;
            this.senderType = senderType;
            this.commandArguments = Objects.requireNonNull(commandArguments, "Arguments may not be null");
            this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
            this.commandPermission = Objects.requireNonNull(commandPermission, "Permission may not be null");
            this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
        }

        /**
         * Supply a command manager instance to the builder. This will be used when attempting to
         * retrieve command argument parsers, in the case that they're needed. This
         * is optional
         *
         * @param commandManager Command manager
         * @return New builder instance using the provided command manager
         */
        @Nonnull
        public Builder<C, M> manager(@Nullable final CommandManager<C, M> commandManager) {
            return new Builder<>(commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Add a new command argument to the command
         *
         * @param argument Argument to add
         * @param <T>       Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        @Nonnull
        public <T> Builder<C, M> argument(@Nonnull final CommandArgument<C, T> argument) {
            final List<CommandArgument<C, ?>> commandArguments = new LinkedList<>(this.commandArguments);
            commandArguments.add(argument);
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Add a new command argument by interacting with a constructed command argument builder
         *
         * @param clazz           Argument class
         * @param name            Argument name
         * @param builderConsumer Builder consumer
         * @param <T>             Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        @Nonnull
        public <T> Builder<C, M> argument(@Nonnull final Class<T> clazz,
                                           @Nonnull final String name,
                                           @Nonnull final Consumer<CommandArgument.Builder<C, T>> builderConsumer) {
            final CommandArgument.Builder<C, T> builder = CommandArgument.ofType(clazz, name);
            if (this.commandManager != null) {
                builder.manager(this.commandManager);
            }
            builderConsumer.accept(builder);
            return this.argument(builder.build());
        }

        /**
         * Specify the command execution handler
         *
         * @param commandExecutionHandler New execution handler
         * @return New builder instance using the command execution handler
         */
        @Nonnull
        public Builder<C, M> handler(@Nonnull final CommandExecutionHandler<C> commandExecutionHandler) {
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 commandExecutionHandler, this.commandPermission);
        }

        /**
         * Specify a required sender type
         *
         * @param senderType Required sender type
         * @return New builder instance using the command execution handler
         */
        @Nonnull
        public Builder<C, M> withSenderType(@Nonnull final Class<? extends C> senderType) {
            return new Builder<>(this.commandManager, this.commandMeta, senderType, this.commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        @Nonnull
        public Builder<C, M> withPermission(@Nonnull final String permission) {
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, permission);
        }

        /**
         * Build a command using the builder instance
         *
         * @return Built command
         */
        @Nonnull
        public Command<C, M> build() {
            return new Command<>(Collections.unmodifiableList(this.commandArguments),
                                 this.commandExecutionHandler, this.senderType, this.commandPermission, this.commandMeta);
        }

    }

}
