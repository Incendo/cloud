//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A command consists out of a chain of {@link CommandArgument command arguments}.
 *
 * @param <C> Command sender type
 */
public class Command<C> {

    private final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> arguments;
    private final CommandExecutionHandler<C> commandExecutionHandler;
    private final Class<? extends C> senderType;
    private final CommandPermission commandPermission;
    private final CommandMeta commandMeta;

    /**
     * Construct a new command
     *
     * @param commandArguments        Command argument and description pairs
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@NonNull final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
                   @NonNull final CommandExecutionHandler<@NonNull C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @NonNull final CommandPermission commandPermission,
                   @NonNull final CommandMeta commandMeta) {
        this.arguments = Objects.requireNonNull(commandArguments, "Command arguments may not be null");
        if (this.arguments.size() == 0) {
            throw new IllegalArgumentException("At least one command argument is required");
        }
        // Enforce ordering of command arguments
        boolean foundOptional = false;
        for (final CommandArgument<C, ?> argument : this.arguments.keySet()) {
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
     * @param commandArguments        Command arguments
     * @param commandExecutionHandler Execution handler
     * @param senderType              Required sender type. May be {@code null}
     * @param commandMeta             Command meta instance
     */
    public Command(@NonNull final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
                   @NonNull final CommandExecutionHandler<@NonNull C> commandExecutionHandler,
                   @Nullable final Class<? extends C> senderType,
                   @NonNull final CommandMeta commandMeta) {
        this(commandArguments, commandExecutionHandler, senderType, Permission.empty(), commandMeta);
    }

    /**
     * Construct a new command
     *
     * @param commandArguments        Command arguments
     * @param commandExecutionHandler Execution handler
     * @param commandPermission       Command permission
     * @param commandMeta             Command meta instance
     */
    public Command(@NonNull final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
                   @NonNull final CommandExecutionHandler<@NonNull C> commandExecutionHandler,
                   @NonNull final CommandPermission commandPermission,
                   @NonNull final CommandMeta commandMeta) {
        this(commandArguments, commandExecutionHandler, null, commandPermission, commandMeta);
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
     */
    public static <C> @NonNull Builder<C> newBuilder(@NonNull final String commandName,
                                                     @NonNull final CommandMeta commandMeta,
                                                     @NonNull final Description description,
                                                     @NonNull final String... aliases) {
        final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> map = new LinkedHashMap<>();
        map.put(StaticArgument.required(commandName, aliases), description);
        return new Builder<>(null, commandMeta, null, map,
                             new CommandExecutionHandler.NullCommandExecutionHandler<>(), Permission.empty());
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
    public static <C> @NonNull Builder<C> newBuilder(@NonNull final String commandName,
                                                     @NonNull final CommandMeta commandMeta,
                                                     @NonNull final String... aliases) {
        final Map<CommandArgument<C, ?>, Description> map = new LinkedHashMap<>();
        map.put(StaticArgument.required(commandName, aliases), Description.empty());
        return new Builder<>(null, commandMeta, null, map,
                             new CommandExecutionHandler.NullCommandExecutionHandler<>(), Permission.empty());
    }

    /**
     * Return a copy of the command argument array
     *
     * @return Copy of the command argument array
     */
    public @NonNull List<CommandArgument<@NonNull C, @NonNull ?>> getArguments() {
        return new ArrayList<>(this.arguments.keySet());
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
     */
    public @NonNull String getArgumentDescription(@NonNull final CommandArgument<C, ?> argument) {
        return this.arguments.get(argument).getDescription();
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
        return this.getCommandMeta().getOrDefault("hidden", "true").equals("true");
    }


    /**
     * Builder for {@link Command} instances. The builder is immutable, and each
     * setter method will return a new builder instance.
     *
     * @param <C> Command sender type
     */
    public static final class Builder<C> {

        private final CommandMeta commandMeta;
        private final Map<CommandArgument<C, ?>, Description> commandArguments;
        private final CommandExecutionHandler<C> commandExecutionHandler;
        private final Class<? extends C> senderType;
        private final CommandPermission commandPermission;
        private final CommandManager<C> commandManager;

        private Builder(@Nullable final CommandManager<C> commandManager,
                        @NonNull final CommandMeta commandMeta,
                        @Nullable final Class<? extends C> senderType,
                        @NonNull final Map<@NonNull CommandArgument<C, ?>, @NonNull Description> commandArguments,
                        @NonNull final CommandExecutionHandler<@NonNull C> commandExecutionHandler,
                        @NonNull final CommandPermission commandPermission) {
            this.commandManager = commandManager;
            this.senderType = senderType;
            this.commandArguments = Objects.requireNonNull(commandArguments, "Arguments may not be null");
            this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
            this.commandPermission = Objects.requireNonNull(commandPermission, "Permission may not be null");
            this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
        }

        /**
         * Add command meta to the internal command meta map
         *
         * @param key   Meta key
         * @param value Meta value
         * @return New builder instance using the inserted meta key-value pair
         */
        public @NonNull Builder<C> meta(@NonNull final String key, @NonNull final String value) {
            final CommandMeta commandMeta = SimpleCommandMeta.builder().with(this.commandMeta).with(key, value).build();
            return new Builder<>(this.commandManager, commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Supply a command manager instance to the builder. This will be used when attempting to
         * retrieve command argument parsers, in the case that they're needed. This
         * is optional
         *
         * @param commandManager Command manager
         * @return New builder instance using the provided command manager
         */
        public @NonNull Builder<C> manager(@Nullable final CommandManager<C> commandManager) {
            return new Builder<>(commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Inserts a required {@link StaticArgument} into the command chain
         *
         * @param main    Main argument name
         * @param aliases Argument aliases
         * @return New builder instance with the modified command chain
         */
        public @NonNull Builder<C> literal(@NonNull final String main,
                                           @NonNull final String... aliases) {
            return this.argument(StaticArgument.required(main, aliases));
        }

        /**
         * Inserts a required {@link StaticArgument} into the command chain
         *
         * @param main        Main argument name
         * @param description Literal description
         * @param aliases     Argument aliases
         * @return New builder instance with the modified command chain
         */
        public @NonNull Builder<C> literal(@NonNull final String main,
                                           @NonNull final Description description,
                                           @NonNull final String... aliases) {
            return this.argument(StaticArgument.required(main, aliases), description);
        }

        /**
         * Add a new command argument with an empty description to the command
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        public <T> @NonNull Builder<C> argument(@NonNull final CommandArgument<C, T> argument) {
            return this.argument(argument, Description.empty());
        }

        /**
         * Add a new command argument to the command
         *
         * @param argument    Argument to add
         * @param description Argument description
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         */
        public <T> @NonNull Builder<C> argument(@NonNull final CommandArgument<C, T> argument,
                                                @NonNull final Description description) {
            final Map<CommandArgument<C, ?>, Description> commandArgumentMap = new LinkedHashMap<>(this.commandArguments);
            commandArgumentMap.put(argument, description);
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, commandArgumentMap,
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
        public <T> @NonNull Builder<C> argument(@NonNull final Class<T> clazz,
                                                @NonNull final String name,
                                                @NonNull final Consumer<CommandArgument.Builder<C, T>> builderConsumer) {
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
         */
        public <U, V> @NonNull Builder<C> argumentPair(@NonNull final String name,
                                                       @NonNull final Pair<@NonNull String, @NonNull String> names,
                                                       @NonNull final Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                                                       @NonNull final Description description) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(ArgumentPair.required(this.commandManager, name, names, parserPair).simple(), description);
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
         */
        public <U, V, O> @NonNull Builder<C> argumentPair(@NonNull final String name,
                                                          @NonNull final TypeToken<O> outputType,
                                                          @NonNull final Pair<@NonNull String, @NonNull String> names,
                                                          @NonNull final Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                                                          @NonNull final Function<Pair<@NonNull U, @NonNull V>,
                                                                  @NonNull O> mapper,
                                                          @NonNull final Description description) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(
                    ArgumentPair.required(this.commandManager, name, names, parserPair).withMapper(outputType, mapper),
                    description);
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
         */
        public <U, V, W> @NonNull Builder<C> argumentTriplet(@NonNull final String name,
                                                             @NonNull final Triplet<@NonNull String,
                                                                     @NonNull String, @NonNull String> names,
                                                             @NonNull final Triplet<@NonNull Class<U>,
                                                                     @NonNull Class<V>, @NonNull Class<W>> parserTriplet,
                                                             @NonNull final Description description) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(ArgumentTriplet.required(this.commandManager, name, names, parserTriplet).simple(), description);
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
         */
        public <U, V, W, O> @NonNull Builder<C> argumentTriplet(@NonNull final String name,
                                                                @NonNull final TypeToken<O> outputType,
                                                                @NonNull final Triplet<@NonNull String,
                                                                        @NonNull String, @NonNull String> names,
                                                                @NonNull final Triplet<@NonNull Class<U>,
                                                                        @NonNull Class<V>, @NonNull Class<W>> parserTriplet,
                                                                @NonNull final Function<@NonNull Triplet<@NonNull U,
                                                                        @NonNull V, @NonNull W>, @NonNull O> mapper,
                                                                @NonNull final Description description) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.argument(
                    ArgumentTriplet.required(this.commandManager, name, names, parserTriplet).withMapper(outputType, mapper),
                    description);
        }

        // End of compound helper methods

        /**
         * Specify the command execution handler
         *
         * @param commandExecutionHandler New execution handler
         * @return New builder instance using the command execution handler
         */
        public @NonNull Builder<C> handler(@NonNull final CommandExecutionHandler<C> commandExecutionHandler) {
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 commandExecutionHandler, this.commandPermission);
        }

        /**
         * Specify a required sender type
         *
         * @param senderType Required sender type
         * @return New builder instance using the command execution handler
         */
        public @NonNull Builder<C> withSenderType(@NonNull final Class<? extends C> senderType) {
            return new Builder<>(this.commandManager, this.commandMeta, senderType, this.commandArguments,
                                 this.commandExecutionHandler, this.commandPermission);
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        public @NonNull Builder<C> withPermission(@NonNull final CommandPermission permission) {
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, permission);
        }

        /**
         * Specify a command permission
         *
         * @param permission Command permission
         * @return New builder instance using the command permission
         */
        public @NonNull Builder<C> withPermission(@NonNull final String permission) {
            return new Builder<>(this.commandManager, this.commandMeta, this.senderType, this.commandArguments,
                                 this.commandExecutionHandler, Permission.of(permission));
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
        public @NonNull Builder<C> proxies(@NonNull final Command<C> command) {
            Builder<C> builder = this;
            for (final CommandArgument<C, ?> argument : command.getArguments()) {
                if (argument instanceof StaticArgument) {
                    continue;
                }
                final CommandArgument<C, ?> builtArgument = argument.copy();
                builder = builder.argument(builtArgument, Description.of(command.getArgumentDescription(argument)));
            }
            if (this.commandPermission.toString().isEmpty()) {
                builder = builder.withPermission(command.getCommandPermission());
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
            return this.meta("hidden", "true");
        }

        /**
         * Build a command using the builder instance
         *
         * @return Built command
         */
        public @NonNull Command<C> build() {
            return new Command<>(Collections.unmodifiableMap(this.commandArguments),
                                 this.commandExecutionHandler,
                                 this.senderType,
                                 this.commandPermission,
                                 this.commandMeta);
        }

    }

}
