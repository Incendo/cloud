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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.compound.ArgumentPair;
import cloud.commandframework.arguments.compound.ArgumentTriplet;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PredicatePermission;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A command consists out of a chain of {@link CommandArgument command arguments}.
 *
 * @param <C> Command sender type
 */
public class Command<C> {

    private final List<@NonNull CommandComponent<C>> components;
    private final List<@NonNull CommandArgument<C, ?>> arguments;
    private final CommandExecutionHandler<C> commandExecutionHandler;
    private final Class<? extends C> senderType;
    private final CommandPermission commandPermission;
    private final CommandMeta commandMeta;

    /**
     * Construct a new command
     *
     * @param commandComponents       Command component argument and description
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     * @since 1.3.0
     */
    public Command(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @Nullable Class<? extends C> senderType,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
        this.arguments = this.components.stream().map(CommandComponent::getArgument).collect(Collectors.toList());
        if (this.components.isEmpty()) {
            throw new IllegalArgumentException("At least one command component is required");
        }
        // Enforce ordering of command arguments
        boolean foundOptional = false;
        for (final CommandArgument<C, ?> argument : this.arguments) {
            if (argument.getName().isEmpty()) {
                throw new IllegalArgumentException("Argument names may not be empty");
            }
            if (foundOptional && argument.isRequired()) {
                throw new IllegalArgumentException(
                        String.format(
                                "Command argument '%s' cannot be placed after an optional argument",
                                argument.getName()
                        ));
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
     * @param commandComponents       Command components
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandMeta             Command meta instance
     * @since 1.3.0
     */
    public Command(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @Nullable Class<? extends C> senderType,
            final @NonNull CommandMeta commandMeta
    ) {
        this(commandComponents, commandExecutionHandler, senderType, Permission.empty(), commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandComponents       Command components
     * @param commandExecutionHandler Execution handler
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     * @since 1.3.0
     */
    public Command(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this(commandComponents, commandExecutionHandler, null, commandPermission, commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandArguments        Command argument and description pairs
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     * @deprecated Map does not allow for the same literal or variable argument name to repeat
     * @see #Command(List, CommandExecutionHandler, Class, CommandPermission, CommandMeta)
     */
    @Deprecated
    public Command(
            final @NonNull Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @Nullable Class<? extends C> senderType,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this(mapToComponents(commandArguments), commandExecutionHandler, senderType, commandPermission, commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandArguments        Command arguments
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandMeta             Command meta instance
     * @deprecated Map does not allow for the same literal or variable argument name to repeat
     * @see #Command(List, CommandExecutionHandler, Class, CommandMeta)
     */
    @Deprecated
    public Command(
            final @NonNull Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @Nullable Class<? extends C> senderType,
            final @NonNull CommandMeta commandMeta
    ) {
        this(mapToComponents(commandArguments), commandExecutionHandler, senderType, commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandArguments        Command arguments
     * @param commandExecutionHandler Execution handler
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     * @deprecated Map does not allow for the same literal or variable argument name to repeat
     * @see #Command(List, CommandExecutionHandler, CommandPermission, CommandMeta)
     */
    @Deprecated
    public Command(
            final @NonNull Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this(mapToComponents(commandArguments), commandExecutionHandler, commandPermission, commandMeta);
    }

    // Converts a map of CommandArgument and Description pairs to a List of CommandComponent
    // Used for backwards-compatibility
    @SuppressWarnings("deprecation")
    private static <C> @NonNull List<@NonNull CommandComponent<C>> mapToComponents(
            final @NonNull Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments
    ) {
        return commandArguments.entrySet().stream()
                .map(e -> CommandComponent.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Create a new command builder. Is recommended to use the builder methods
     * in {@link CommandManager} rather than invoking this method directly.
     *
     * @param commandName Base command argument
     * @param commandMeta Command meta instance
     * @param description Command description
     * @param aliases     Command aliases
     * @param <C>         Command sender type
     * @return Command builder
     * @deprecated for removal since 1.4.0. Use {@link #newBuilder(String, CommandMeta, ArgumentDescription, String...)} instead.
     */
    @Deprecated
    public static <C> @NonNull Builder<C> newBuilder(
            final @NonNull String commandName,
            final @NonNull CommandMeta commandMeta,
            final @NonNull Description description,
            final @NonNull String... aliases
    ) {
        return newBuilder(commandName, commandMeta, (ArgumentDescription) description, aliases);
    }

    /**
     * Create a new command builder. Is recommended to use the builder methods
     * in {@link CommandManager} rather than invoking this method directly.
     *
     * @param commandName Base command argument
     * @param commandMeta Command meta instance
     * @param description Command description
     * @param aliases     Command aliases
     * @param <C>         Command sender type
     * @return Command builder
     * @since 1.4.0
     */
    public static <C> @NonNull Builder<C> newBuilder(
            final @NonNull String commandName,
            final @NonNull CommandMeta commandMeta,
            final @NonNull ArgumentDescription description,
            final @NonNull String... aliases
    ) {
        final List<CommandComponent<C>> commands = new ArrayList<>();
        commands.add(CommandComponent.of(StaticArgument.of(commandName, aliases), description));
        return new Builder<>(
                null,
                commandMeta,
                null,
                commands,
                new CommandExecutionHandler.NullCommandExecutionHandler<>(),
                Permission.empty(),
                Collections.emptyList()
        );
    }

    /**
     * Create a new command builder. Is recommended to use the builder methods
     * in {@link CommandManager} rather than invoking this method directly.
     *
     * @param commandName Base command argument
     * @param commandMeta Command meta instance
     * @param aliases     Command aliases
     * @param <C>         Command sender type
     * @return Command builder
     */
    public static <C> @NonNull Builder<C> newBuilder(
            final @NonNull String commandName,
            final @NonNull CommandMeta commandMeta,
            final @NonNull String... aliases
    ) {
        final List<CommandComponent<C>> commands = new ArrayList<>();
        commands.add(CommandComponent.of(StaticArgument.of(commandName, aliases), ArgumentDescription.empty()));
        return new Builder<>(
                null,
                commandMeta,
                null,
                commands,
                new CommandExecutionHandler.NullCommandExecutionHandler<>(),
                Permission.empty(),
                Collections.emptyList()
        );
    }

    /**
     * Return a copy of the command argument array
     *
     * @return Copy of the command argument array.  This List is mutable.
     */
    public @NonNull List<CommandArgument<@NonNull C, @NonNull ?>> getArguments() {
        return new ArrayList<>(this.arguments);
    }

    /**
     * Returns a copy of the command component array
     *
     * @return Copy of the command component array. This List is mutable
     * @since 1.3.0
     */
    public @NonNull List<CommandComponent<@NonNull C>> getComponents() {
        return new ArrayList<>(this.components);
    }

    /**
     * Get the command execution handler
     *
     * @return Command execution handler
     */
    public CommandExecutionHandler<@NonNull C> getCommandExecutionHandler() {
        return this.commandExecutionHandler;
    }

    /**
     * Get the required sender type, if one has been specified
     *
     * @return Required sender type
     */
    public @NonNull Optional<Class<? extends C>> getSenderType() {
        return Optional.ofNullable(this.senderType);
    }

    /**
     * Get the command permission
     *
     * @return Command permission
     */
    public @NonNull CommandPermission getCommandPermission() {
        return this.commandPermission;
    }

    /**
     * Get the command meta instance
     *
     * @return Command meta
     */
    public @NonNull CommandMeta getCommandMeta() {
        return this.commandMeta;
    }

    /**
     * Get the description for an argument
     *
     * @param argument Argument
     * @return Argument description
     * @throws IllegalArgumentException If the command argument does not exist
     * @deprecated More than one matching command argument may exist per command.
     *             Use {@link #getArguments()} and search in that, instead.
     */
    @Deprecated
    public @NonNull String getArgumentDescription(final @NonNull CommandArgument<C, ?> argument) {
        for (final CommandComponent<C> component : this.components) {
            if (component.getArgument().equals(argument)) {
                return component.getArgumentDescription().getDescription();
            }
        }
        throw new IllegalArgumentException("Command argument not found: " + argument);
    }

    @Override
    public final String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final CommandArgument<C, ?> argument : this.getArguments()) {
            stringBuilder.append(argument.getName()).append(' ');
        }
        final String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }

    /**
     * Check whether or not the command is hidden
     *
     * @return {@code true} if the command is hidden, {@code false} if not
     */
    public boolean isHidden() {
        return this.getCommandMeta().getOrDefault(CommandMeta.HIDDEN, false);
    }


    /**
     * Builder for {@link Command} instances. The builder is immutable, and each
     * setter method will return a new builder instance.
     *
     * @param <C> Command sender type
     */
    public static final class Builder<C> {

        private final CommandMeta commandMeta;
        private final List<CommandComponent<C>> commandComponents;
        private final CommandExecutionHandler<C> commandExecutionHandler;
        private final Class<? extends C> senderType;
        private final CommandPermission commandPermission;
        private final CommandManager<C> commandManager;
        private final Collection<CommandFlag<?>> flags;

        private Builder(
                final @Nullable CommandManager<C> commandManager,
                final @NonNull CommandMeta commandMeta,
                final @Nullable Class<? extends C> senderType,
                final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
                final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
                final @NonNull CommandPermission commandPermission,
                final @NonNull Collection<CommandFlag<?>> flags
        ) {
            this.commandManager = commandManager;
            this.senderType = senderType;
            this.commandComponents = Objects.requireNonNull(commandComponents, "Components may not be null");
            this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
            this.commandPermission = Objects.requireNonNull(commandPermission, "Permission may not be null");
            this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
            this.flags = Objects.requireNonNull(flags, "Flags may not be null");
        }

        /**
         * Get the required sender type for this builder
         * <p>
         * Returns {@code null} when there is not a specific required sender type
         *
         * @return required sender type
         * @since 1.3.0
         */
        public @Nullable Class<? extends C> senderType() {
            return this.senderType;
        }

        /**
         * Get the required command permission for this builder
         * <p>
         * Will return {@link Permission#empty()} if there is no required permission
         *
         * @return required permission
         * @since 1.3.0
         */
        public @NonNull CommandPermission commandPermission() {
            return this.commandPermission;
        }

        /**
         * Add command meta to the internal command meta map
         *
         * @param key   Meta key
         * @param value Meta value
         * @return New builder instance using the inserted meta key-value pair
         * @deprecated for removal since 1.2.0, use the typesafe variant at {@link #meta(CommandMeta.Key, Object)} instead.
         */
        @Deprecated
        public @NonNull Builder<C> meta(final @NonNull String key, final @NonNull String value) {
            final CommandMeta commandMeta = SimpleCommandMeta.builder().with(this.commandMeta).with(key, value).build();
            return new Builder<>(
                    this.commandManager,
                    commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Add command meta to the internal command meta map
         *
         * @param <V>   Meta value type
         * @param key   Meta key
         * @param value Meta value
         * @return New builder instance using the inserted meta key-value pair
         * @since 1.3.0
         */
        public <V> @NonNull Builder<C> meta(final CommandMeta.@NonNull Key<V> key, final @NonNull V value) {
            final CommandMeta commandMeta = SimpleCommandMeta.builder().with(this.commandMeta).with(key, value).build();
            return new Builder<>(
                    this.commandManager,
                    commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Supply a command manager instance to the builder. This will be used when attempting to
         * retrieve command argument parsers, in the case that they're needed. This
         * is optional
         *
         * @param commandManager Command manager
         * @return New builder instance using the provided command manager
         */
        public @NonNull Builder<C> manager(final @Nullable CommandManager<C> commandManager) {
            return new Builder<>(
                    commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Inserts a required {@link StaticArgument} into the command chain
         *
         * @param main    Main argument name
         * @param aliases Argument aliases
         * @return New builder instance with the modified command chain
         */
        public @NonNull Builder<C> literal(
                final @NonNull String main,
                final @NonNull String... aliases
        ) {
            return this.argument(StaticArgument.of(main, aliases));
        }

        /**
         * Inserts a required {@link StaticArgument} into the command chain
         *
         * @param main        Main argument name
         * @param description Literal description
         * @param aliases     Argument aliases
         * @return New builder instance with the modified command chain
         * @deprecated for removal since 1.4.0. Use {@link #literal(String, ArgumentDescription, String...)} instead.
         */
        @Deprecated
        public @NonNull Builder<C> literal(
                final @NonNull String main,
                final @NonNull Description description,
                final @NonNull String... aliases
        ) {
            return this.argument(StaticArgument.of(main, aliases), description);
        }

        /**
         * Inserts a required {@link StaticArgument} into the command chain
         *
         * @param main        Main argument name
         * @param description Literal description
         * @param aliases     Argument aliases
         * @return New builder instance with the modified command chain
         * @since 1.4.0
         */
        public @NonNull Builder<C> literal(
                final @NonNull String main,
                final @NonNull ArgumentDescription description,
                final @NonNull String... aliases
        ) {
            return this.argument(StaticArgument.of(main, aliases), description);
        }

        /**
         * Add a new command argument with an empty description to the command
         *
         * @param builder Argument to add. {@link CommandArgument.Builder#build()} will be invoked
         *                and the result will be registered in the command.
         * @param <T>     Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        public <T> @NonNull Builder<C> argument(final CommandArgument.@NonNull Builder<C, T> builder) {
            return this.argument(builder.build());
        }

        /**
         * Add a new command argument with an empty description to the command
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        public <T> @NonNull Builder<C> argument(final @NonNull CommandArgument<C, T> argument) {
            return this.argument(argument, argument.getDefaultDescription());
        }

        /**
         * Add a new command argument to the command
         *
         * @param argument    Argument to add
         * @param description Argument description
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @deprecated for removal since 1.4.0. Use {@link #argument(CommandArgument, ArgumentDescription)} instead.
         */
        @Deprecated
        public <T> @NonNull Builder<C> argument(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull Description description
        ) {
            return this.argument(argument, (ArgumentDescription) description);
        }

        /**
         * Add a new command argument to the command
         *
         * @param argument    Argument to add
         * @param description Argument description
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 1.4.0
         */
        public <T> @NonNull Builder<C> argument(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull ArgumentDescription description
        ) {
            if (argument.isArgumentRegistered()) {
                throw new IllegalArgumentException("The provided argument has already been associated with a command."
                        + " Use CommandArgument#copy to create a copy of the argument.");
            }
            argument.setArgumentRegistered();
            final List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
            commandComponents.add(CommandComponent.of(argument, description));
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Add a new command argument to the command
         *
         * @param builder     Argument to add. {@link CommandArgument.Builder#build()} will be invoked
         *                    and the result will be registered in the command.
         * @param description Argument description
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @deprecated for removal since 1.4.0. Use {@link #argument(CommandArgument.Builder, ArgumentDescription)} instead.
         */
        @Deprecated
        public <T> @NonNull Builder<C> argument(
                final CommandArgument.@NonNull Builder<C, T> builder,
                final @NonNull Description description
        ) {
            return this.argument(builder, (ArgumentDescription) description);
        }

        /**
         * Add a new command argument to the command
         *
         * @param builder     Argument to add. {@link CommandArgument.Builder#build()} will be invoked
         *                    and the result will be registered in the command.
         * @param description Argument description
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 1.4.0
         */
        public <T> @NonNull Builder<C> argument(
                final CommandArgument.@NonNull Builder<C, T> builder,
                final @NonNull ArgumentDescription description
        ) {
            final List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
            commandComponents.add(CommandComponent.of(builder.build(), description));
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
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
        public <T> @NonNull Builder<C> argument(
                final @NonNull Class<T> clazz,
                final @NonNull String name,
                final @NonNull Consumer<CommandArgument.Builder<C, T>> builderConsumer
        ) {
            final CommandArgument.Builder<C, T> builder = CommandArgument.ofType(clazz, name);
            if (this.commandManager != null) {
                builder.manager(this.commandManager);
            }
            builderConsumer.accept(builder);
            return this.argument(builder.build());
        }

        // Compound helper methods

        /**
         * Create a new argument pair that maps to {@link Pair}
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name        Name of the argument
         * @param names       Pair containing the names of the sub-arguments
         * @param parserPair  Pair containing the types of the sub-arguments. There must be parsers for these types registered
         *                    in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
         *                    {@link CommandManager} attached to this command
         * @param description Description of the argument
         * @param <U>         First type
         * @param <V>         Second type
         * @return Builder instance with the argument inserted
         * @deprecated for removal since 1.4.0. Use {@link #argumentPair(String, Pair, Pair, ArgumentDescription)} instead.
         */
        @Deprecated
        public <U, V> @NonNull Builder<C> argumentPair(
                final @NonNull String name,
                final @NonNull Pair<@NonNull String, @NonNull String> names,
                final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                final @NonNull Description description
        ) {
            return this.argumentPair(name, names, parserPair, (ArgumentDescription) description);
        }

        /**
         * Create a new argument pair that maps to {@link Pair}
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name        Name of the argument
         * @param names       Pair containing the names of the sub-arguments
         * @param parserPair  Pair containing the types of the sub-arguments. There must be parsers for these types registered
         *                    in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
         *                    {@link CommandManager} attached to this command
         * @param description Description of the argument
         * @param <U>         First type
         * @param <V>         Second type
         * @return Builder instance with the argument inserted
         * @since 1.4.0
         */
        public <U, V> @NonNull Builder<C> argumentPair(
                final @NonNull String name,
                final @NonNull Pair<@NonNull String, @NonNull String> names,
                final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(ArgumentPair.of(this.commandManager, name, names, parserPair).simple(), description);
        }

        /**
         * Create a new argument pair that maps to a custom type.
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name        Name of the argument
         * @param outputType  The output type
         * @param names       Pair containing the names of the sub-arguments
         * @param parserPair  Pair containing the types of the sub-arguments. There must be parsers for these types registered
         *                    in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
         *                    {@link CommandManager} attached to this command
         * @param mapper      Mapper that maps from {@link Pair} to the custom type
         * @param description Description of the argument
         * @param <U>         First type
         * @param <V>         Second type
         * @param <O>         Output type
         * @return Builder instance with the argument inserted
         * @deprecated for removal since 1.4.0. Use
         *     {@link #argumentPair(String, TypeToken, Pair, Pair, BiFunction, ArgumentDescription)} instead.
         */
        @Deprecated
        public <U, V, O> @NonNull Builder<C> argumentPair(
                final @NonNull String name,
                final @NonNull TypeToken<O> outputType,
                final @NonNull Pair<String, String> names,
                final @NonNull Pair<Class<U>, Class<V>> parserPair,
                final @NonNull BiFunction<C, Pair<U, V>, O> mapper,
                final @NonNull Description description
        ) {
            return this.argumentPair(name, outputType, names, parserPair, mapper, (ArgumentDescription) description);
        }

        /**
         * Create a new argument pair that maps to a custom type.
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name        Name of the argument
         * @param outputType  The output type
         * @param names       Pair containing the names of the sub-arguments
         * @param parserPair  Pair containing the types of the sub-arguments. There must be parsers for these types registered
         *                    in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by the
         *                    {@link CommandManager} attached to this command
         * @param mapper      Mapper that maps from {@link Pair} to the custom type
         * @param description Description of the argument
         * @param <U>         First type
         * @param <V>         Second type
         * @param <O>         Output type
         * @return Builder instance with the argument inserted
         * @since 1.4.0
         */
        public <U, V, O> @NonNull Builder<C> argumentPair(
                final @NonNull String name,
                final @NonNull TypeToken<O> outputType,
                final @NonNull Pair<String, String> names,
                final @NonNull Pair<Class<U>, Class<V>> parserPair,
                final @NonNull BiFunction<C, Pair<U, V>, O> mapper,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(
                    ArgumentPair.of(this.commandManager, name, names, parserPair).withMapper(outputType, mapper),
                    description
            );
        }

        /**
         * Create a new argument pair that maps to {@link cloud.commandframework.types.tuples.Triplet}
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name          Name of the argument
         * @param names         Triplet containing the names of the sub-arguments
         * @param parserTriplet Triplet containing the types of the sub-arguments. There must be parsers for these types
         *                      registered in the {@link cloud.commandframework.arguments.parser.ParserRegistry}
         *                      used by the {@link CommandManager} attached to this command
         * @param description   Description of the argument
         * @param <U>           First type
         * @param <V>           Second type
         * @param <W>           Third type
         * @return Builder instance with the argument inserted
         * @deprecated for removal since 1.4.0. Use {@link #argumentTriplet(String, Triplet, Triplet, ArgumentDescription)}
         *     instead.
         */
        @Deprecated
        public <U, V, W> @NonNull Builder<C> argumentTriplet(
                final @NonNull String name,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull Description description
        ) {
            return this.argumentTriplet(name, names, parserTriplet, (ArgumentDescription) description);
        }

        /**
         * Create a new argument pair that maps to {@link cloud.commandframework.types.tuples.Triplet}
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name          Name of the argument
         * @param names         Triplet containing the names of the sub-arguments
         * @param parserTriplet Triplet containing the types of the sub-arguments. There must be parsers for these types
         *                      registered in the {@link cloud.commandframework.arguments.parser.ParserRegistry}
         *                      used by the {@link CommandManager} attached to this command
         * @param description   Description of the argument
         * @param <U>           First type
         * @param <V>           Second type
         * @param <W>           Third type
         * @return Builder instance with the argument inserted
         * @since 1.4.0
         */
        public <U, V, W> @NonNull Builder<C> argumentTriplet(
                final @NonNull String name,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(ArgumentTriplet.of(this.commandManager, name, names, parserTriplet).simple(), description);
        }

        /**
         * Create a new argument triplet that maps to a custom type.
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name          Name of the argument
         * @param outputType    The output type
         * @param names         Triplet containing the names of the sub-arguments
         * @param parserTriplet Triplet containing the types of the sub-arguments. There must be parsers for these types
         *                      registered in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by
         *                      the {@link CommandManager} attached to this command
         * @param mapper        Mapper that maps from {@link Triplet} to the custom type
         * @param description   Description of the argument
         * @param <U>           First type
         * @param <V>           Second type
         * @param <W>           Third type
         * @param <O>           Output type
         * @return Builder instance with the argument inserted
         * @deprecated for removal since 1.4.0, use
         *      {@link #argumentTriplet(String, TypeToken, Triplet, Triplet, BiFunction, ArgumentDescription)} instead.
         */
        @Deprecated
        public <U, V, W, O> @NonNull Builder<C> argumentTriplet(
                final @NonNull String name,
                final @NonNull TypeToken<O> outputType,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull BiFunction<C, Triplet<U, V, W>, O> mapper,
                final @NonNull Description description
        ) {
            return this.argumentTriplet(
                    name,
                    outputType,
                    names,
                    parserTriplet,
                    mapper,
                    (ArgumentDescription) description
            );
        }

        /**
         * Create a new argument triplet that maps to a custom type.
         * <p>
         * For this to work, there must be a {@link CommandManager}
         * attached to the command builder. To guarantee this, it is recommended to get the command builder instance
         * using {@link CommandManager#commandBuilder(String, String...)}
         *
         * @param name          Name of the argument
         * @param outputType    The output type
         * @param names         Triplet containing the names of the sub-arguments
         * @param parserTriplet Triplet containing the types of the sub-arguments. There must be parsers for these types
         *                      registered in the {@link cloud.commandframework.arguments.parser.ParserRegistry} used by
         *                      the {@link CommandManager} attached to this command
         * @param mapper        Mapper that maps from {@link Triplet} to the custom type
         * @param description   Description of the argument
         * @param <U>           First type
         * @param <V>           Second type
         * @param <W>           Third type
         * @param <O>           Output type
         * @return Builder instance with the argument inserted
         * @since 1.4.0
         */
        public <U, V, W, O> @NonNull Builder<C> argumentTriplet(
                final @NonNull String name,
                final @NonNull TypeToken<O> outputType,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull BiFunction<C, Triplet<U, V, W>, O> mapper,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(
                    ArgumentTriplet.of(this.commandManager, name, names, parserTriplet).withMapper(outputType, mapper),
                    description
            );
        }

        // End of compound helper methods

        /**
         * Specify the command execution handler
         *
         * @param commandExecutionHandler New execution handler
         * @return New builder instance using the command execution handler
         */
        public @NonNull Builder<C> handler(final @NonNull CommandExecutionHandler<C> commandExecutionHandler) {
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Specify a required sender type
         *
         * @param senderType Required sender type
         * @return New builder instance using the required sender type
         */
        public @NonNull Builder<C> senderType(final @NonNull Class<? extends C> senderType) {
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    this.flags
            );
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        public @NonNull Builder<C> permission(final @NonNull CommandPermission permission) {
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    permission,
                    this.flags
            );
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        public @NonNull Builder<C> permission(final @NonNull PredicatePermission<C> permission) {
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    permission,
                    this.flags
            );
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        public @NonNull Builder<C> permission(final @NonNull String permission) {
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    Permission.of(permission),
                    this.flags
            );
        }

        /**
         * Make the current command be a proxy of the supplied command. This means that
         * all of the proxied commands variable command arguments will be inserted into this
         * builder instance, in the order they are declared in the proxied command. Furthermore,
         * the proxied commands command handler will be showed by the command that is currently
         * being built. If the current command builder does not have a permission node set, this
         * too will be copied.
         *
         * @param command Command to proxy
         * @return New builder that proxies the given command
         */
        public @NonNull Builder<C> proxies(final @NonNull Command<C> command) {
            Builder<C> builder = this;
            for (final CommandComponent<C> component : command.getComponents()) {
                final CommandArgument<C, ?> argument = component.getArgument();
                if (argument instanceof StaticArgument) {
                    continue;
                }
                final CommandArgument<C, ?> builtArgument = argument.copy();
                builder = builder.argument(builtArgument, component.getArgumentDescription());
            }
            if (this.commandPermission.toString().isEmpty()) {
                builder = builder.permission(command.getCommandPermission());
            }
            return builder.handler(command.commandExecutionHandler);
        }

        /**
         * Indicate that the command should be hidden from help menus
         * and other places where commands are exposed to users
         *
         * @return New builder instance that indicates that the constructed command should be hidden
         */
        public @NonNull Builder<C> hidden() {
            return this.meta(CommandMeta.HIDDEN, true);
        }

        /**
         * Register a new command flag
         *
         * @param flag Flag
         * @param <T>  Flag value type
         * @return New builder instance that uses the provided flag
         */
        public @NonNull <T> Builder<C> flag(final @NonNull CommandFlag<T> flag) {
            final List<CommandFlag<?>> flags = new ArrayList<>(this.flags);
            flags.add(flag);
            return new Builder<>(
                    this.commandManager,
                    this.commandMeta,
                    this.senderType,
                    this.commandComponents,
                    this.commandExecutionHandler,
                    this.commandPermission,
                    Collections.unmodifiableList(flags)
            );
        }

        /**
         * Register a new command flag
         *
         * @param builder Flag builder. {@link CommandFlag.Builder#build()} will be invoked.
         * @param <T>     Flag value type
         * @return New builder instance that uses the provided flag
         */
        public @NonNull <T> Builder<C> flag(final CommandFlag.@NonNull Builder<T> builder) {
            return this.flag(builder.build());
        }

        /**
         * Build a command using the builder instance
         *
         * @return Built command
         */
        public @NonNull Command<C> build() {
            final List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
            /* Construct flag node */
            if (!this.flags.isEmpty()) {
                final FlagArgument<C> flagArgument = new FlagArgument<>(this.flags);
                commandComponents.add(CommandComponent.of(flagArgument, ArgumentDescription.of("Command flags")));
            }
            return new Command<>(
                    Collections.unmodifiableList(commandComponents),
                    this.commandExecutionHandler,
                    this.senderType,
                    this.commandPermission,
                    this.commandMeta
            );
        }

    }

}
