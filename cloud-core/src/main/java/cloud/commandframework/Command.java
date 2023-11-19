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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.LiteralParser;
import cloud.commandframework.arguments.compound.ArgumentPair;
import cloud.commandframework.arguments.compound.ArgumentTriplet;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.flags.CommandFlagParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PredicatePermission;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A command consists out of a chain of {@link CommandArgument command arguments}.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public class Command<C> {

    private final List<@NonNull CommandComponent<C>> components;
    private final @Nullable CommandComponent<C> flagComponent;
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
    @API(status = API.Status.STABLE, since = "1.3.0")
    public Command(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @Nullable Class<? extends C> senderType,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
        if (this.components.isEmpty()) {
            throw new IllegalArgumentException("At least one command component is required");
        }

        this.flagComponent =
                this.components.stream()
                        .filter(ca -> ca.type() == CommandComponent.ComponentType.FLAG)
                        .findFirst()
                        .orElse(null);

        // Enforce ordering of command arguments
        boolean foundOptional = false;
        for (final CommandComponent<C> component : this.components) {
            if (component.name().isEmpty()) {
                throw new IllegalArgumentException("Component names may not be empty");
            }
            if (foundOptional && component.required()) {
                throw new IllegalArgumentException(
                        String.format(
                                "Command component '%s' cannot be placed after an optional argument",
                                component.name()
                        ));
            } else if (!component.required()) {
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
    @API(status = API.Status.STABLE, since = "1.3.0")
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
    @API(status = API.Status.STABLE, since = "1.3.0")
    public Command(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @NonNull CommandExecutionHandler<@NonNull C> commandExecutionHandler,
            final @NonNull CommandPermission commandPermission,
            final @NonNull CommandMeta commandMeta
    ) {
        this(commandComponents, commandExecutionHandler, null, commandPermission, commandMeta);
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
    @API(status = API.Status.STABLE, since = "1.4.0")
    public static <C> @NonNull Builder<C> newBuilder(
            final @NonNull String commandName,
            final @NonNull CommandMeta commandMeta,
            final @NonNull ArgumentDescription description,
            final @NonNull String... aliases
    ) {
        final List<CommandComponent<C>> commands = new ArrayList<>();
        final ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
        commands.add(
                CommandComponent.<C, String>builder()
                        .name(commandName)
                        .parser(staticParser)
                        .description(description)
                        .build()
        );
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
        final ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
        commands.add(
                CommandComponent.<C, String>builder()
                        .name(commandName)
                        .parser(staticParser)
                        .build()
        );
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
     * Returns a copy of the list of the components that make up this command.
     *
     * @return modifiable copy of the component list
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull List<CommandComponent<C>> components() {
        return new ArrayList<>(this.components);
    }

    /**
     * Returns the first command component.
     *
     * @return the root component
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandComponent<C> rootComponent() {
        return this.components.get(0);
    }

    /**
     * Return a mutable copy of the command components, ignoring flag arguments.
     *
     * @return argument list
     * @since 1.8.0
     */
    @API(status = API.Status.EXPERIMENTAL, since = "1.8.0")
    public @NonNull List<CommandComponent<C>> nonFlagArguments() {
        final List<CommandComponent<C>> components = new ArrayList<>(this.components);
        if (this.flagComponent() != null) {
            components.remove(this.flagComponent());
        }
        return components;
    }

    /**
     * Returns the component that contains the flags belonging to the command.
     *
     * @return the flag component, or {@code null} if no flags have been registered
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @Nullable CommandComponent<C> flagComponent() {
        return this.flagComponent;
    }

    /**
     * Returns the flag parser for this command, or null if no flags are supported.
     *
     * @return flag parser or null
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @Nullable CommandFlagParser<@NonNull C> flagParser() {
        final CommandComponent<C> flagComponent = this.flagComponent();
        if (flagComponent == null) {
            return null;
        }
        return (CommandFlagParser<C>) flagComponent.parser();
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

    @Override
    public final String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final CommandComponent<C> component : this.components()) {
            stringBuilder.append(component.name()).append(' ');
        }
        final String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }

    /**
     * Check whether the command is hidden
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
    @API(status = API.Status.STABLE)
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
        @API(status = API.Status.STABLE, since = "1.3.0")
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
        @API(status = API.Status.STABLE, since = "1.3.0")
        public @NonNull CommandPermission commandPermission() {
            return this.commandPermission;
        }

        /**
         * Applies the provided {@link Applicable} to this {@link Builder}, and returns the result.
         *
         * @param applicable operation
         * @return operation result
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        public @NonNull Builder<@NonNull C> apply(
                final @NonNull Applicable<@NonNull C> applicable
        ) {
            return applicable.applyToCommandBuilder(this);
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
        @API(status = API.Status.STABLE, since = "1.3.0")
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
         * Inserts a required literal into the command chain
         *
         * @param main    Main argument name
         * @param aliases Argument aliases
         * @return New builder instance with the modified command chain
         */
        public @NonNull Builder<C> literal(
                final @NonNull String main,
                final @NonNull String... aliases
        ) {
            return this.required(main, LiteralParser.literal(main, aliases));
        }

        /**
         * Inserts a required literal into the command chain
         *
         * @param main        Main argument name
         * @param description Literal description
         * @param aliases     Argument aliases
         * @return New builder instance with the modified command chain
         * @since 1.4.0
         */
        @API(status = API.Status.STABLE, since = "1.4.0")
        public @NonNull Builder<C> literal(
                final @NonNull String main,
                final @NonNull ArgumentDescription description,
                final @NonNull String... aliases
        ) {
            return this.required(main, LiteralParser.literal(main, aliases), description);
        }

        /**
         * Adds the given required {@code argument} to the command
         *
         * @param argument    Argument to add
         * @param description Description of the argument
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(this.argumentToComponent(argument).description(description));
        }

        /**
         * Marks the {@code builder} as required and adds it to the command.
         *
         * @param name    the name that will be inserted into the builder
         * @param builder the component builder
         * @return New builder instance with the command argument inserted into the argument list
         */
        @SuppressWarnings({"rawtypes"})
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> required(
                final @NonNull String name,
                final CommandComponent.@NonNull Builder builder
        ) {
            return this.argument(builder.name(name).required());
        }

        /**
         * Marks the {@code builder} as required and adds it to the command.
         *
         * @param name    the name that will be inserted into the builder
         * @param builder the component builder
         * @return New builder instance with the command argument inserted into the argument list
         */
        @SuppressWarnings({"rawtypes"})
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> optional(
                final @NonNull String name,
                final CommandComponent.@NonNull Builder builder
        ) {
            return this.argument(builder.name(name).optional());
        }

        /**
         * Marks the {@code builder} as required and adds it to the command.
         *
         * @param builder the component builder
         * @return New builder instance with the command argument inserted into the argument list
         */
        @SuppressWarnings({"rawtypes"})
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> required(
                final CommandComponent.@NonNull Builder builder
        ) {
            return this.argument(builder.required());
        }

        /**
         * Marks the {@code builder} as required and adds it to the command.
         *
         * @param builder the component builder
         * @return New builder instance with the command argument inserted into the argument list
         */
        @SuppressWarnings({"rawtypes"})
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> optional(
                final CommandComponent.@NonNull Builder builder
        ) {
            return this.argument(builder.optional());
        }

        /**
         * Adds the given required {@code argument} to the command
         *
         * @param argument    Argument to add
         * @param description Description of the argument
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final CommandArgument.@NonNull Builder<C, T> argument,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(this.argumentToComponent(argument.build()).description(description));
        }

        /**
         * Adds the given optional {@code argument} to the command with no default value
         *
         * @param argument    Argument to add
         * @param description Description of the argument
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(this.argumentToComponent(argument).optional().description(description));
        }

        /**
         * Adds the given optional {@code argument} to the command with no default value
         *
         * @param argument    Argument to add
         * @param description Description of the argument
         * @param <T>         Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final CommandArgument.@NonNull Builder<C, T> argument,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(this.argumentToComponent(argument.build()).optional().description(description));
        }

        /**
         * Adds the given optional {@code argument} to the command
         *
         * @param argument     Argument to add
         * @param description  Description of the argument
         * @param defaultValue The default value that gets used when the argument is omitted
         * @param <T>          Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull ArgumentDescription description,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(this.argumentToComponent(argument).optional(defaultValue).description(description));
        }

        /**
         * Adds the given optional {@code argument} to the command
         *
         * @param argument     Argument to add
         * @param description  Description of the argument
         * @param defaultValue The default value that gets used when the argument is omitted
         * @param <T>          Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final CommandArgument.@NonNull Builder<C, T> argument,
                final @NonNull ArgumentDescription description,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(this.argumentToComponent(argument.build()).optional().description(description));
        }

        /**
         * Adds the given required {@code argument} to the command
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull CommandArgument<C, T> argument
        ) {
            return this.argument(this.argumentToComponent(argument));
        }

        /**
         * Adds the given required argument to the command
         *
         * @param name   the name of the argument
         * @param parser the parser
         * @param <T>    the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser
        ) {
            return this.argument(CommandComponent.<C, T>builder().name(name).parser(parser).build());
        }

        /**
         * Adds the given required argument to the command
         *
         * @param name   the name of the argument
         * @param parser the parser
         * @param <T>    the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser
        ) {
            return this.argument(CommandComponent.<C, T>builder().key(name).parser(parser).build());
        }

        /**
         * Adds the given required argument to the command
         *
         * @param name        the name of the argument
         * @param parser      the parser
         * @param description the description of the argument
         * @param <T>         the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(CommandComponent.<C, T>builder().key(name).parser(parser).description(description).build());
        }

        /**
         * Adds the given required argument to the command
         *
         * @param name        the name of the argument
         * @param parser      the parser
         * @param description the description of the argument
         * @param <T>         the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(CommandComponent.<C, T>builder().name(name).parser(parser).description(description).build());
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name   the name of the argument
         * @param parser the parser
         * @param <T>    the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser
        ) {
            return this.argument(CommandComponent.<C, T>builder().name(name).parser(parser).optional().build());
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name   the name of the argument
         * @param parser the parser
         * @param <T>    the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser
        ) {
            return this.argument(CommandComponent.<C, T>builder().key(name).parser(parser).optional().build());
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name        the name of the argument
         * @param parser      the parser
         * @param description the description of the argument
         * @param <T>         the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .name(name)
                            .parser(parser)
                            .description(description)
                            .optional()
                            .build()
            );
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name        the name of the argument
         * @param parser      the parser
         * @param description the description of the argument
         * @param <T>         the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .key(name)
                            .parser(parser)
                            .description(description)
                            .optional()
                            .build()
            );
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name         the name of the argument
         * @param parser       the parser
         * @param defaultValue the default value
         * @param <T>          the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .name(name)
                            .parser(parser)
                            .optional(defaultValue)
                            .build()
            );
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name         the name of the argument
         * @param parser       the parser
         * @param defaultValue the default value
         * @param <T>          the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .key(name)
                            .parser(parser)
                            .optional(defaultValue)
                            .build()
            );
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name         the name of the argument
         * @param parser       the parser
         * @param defaultValue the default value
         * @param description  the description of the argument
         * @param <T>          the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull DefaultValue<C, T> defaultValue,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .name(name)
                            .parser(parser)
                            .optional(defaultValue)
                            .description(description)
                            .build()
            );
        }

        /**
         * Adds the given optional argument to the command
         *
         * @param name         the name of the argument
         * @param parser       the parser
         * @param defaultValue the default value
         * @param description  the description of the argument
         * @param <T>          the type produced by the parser
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parser,
                final @NonNull DefaultValue<C, T> defaultValue,
                final @NonNull ArgumentDescription description
        ) {
            return this.argument(
                    CommandComponent.<C, T>builder()
                            .key(name)
                            .parser(parser)
                            .optional(defaultValue)
                            .description(description)
                            .build()
            );
        }

        /**
         * Adds the given required {@code argument} to the command
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final CommandArgument.@NonNull Builder<C, T> argument
        ) {
            return this.required(argument.build());
        }

        /**
         * Adds the given optional {@code argument} to the command with no default value
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CommandArgument<C, T> argument
        ) {
            return this.argument(this.argumentToComponent(argument).optional());
        }

        /**
         * Adds the given optional {@code argument} to the command with no default value
         *
         * @param argument Argument to add
         * @param <T>      Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final CommandArgument.@NonNull Builder<C, T> argument
        ) {
            return this.argument(this.argumentToComponent(argument.build()).optional());
        }

        /**
         * Adds the given optional {@code argument} to the command
         *
         * @param argument     Argument to add
         * @param defaultValue The default value that gets used when the argument is omitted
         * @param <T>          Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull CommandArgument<C, T> argument,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(this.argumentToComponent(argument).optional(defaultValue));
        }

        /**
         * Adds the given optional {@code argument} to the command
         *
         * @param argument     Argument to add
         * @param defaultValue The default value that gets used when the argument is omitted
         * @param <T>          Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final CommandArgument.@NonNull Builder<C, T> argument,
                final @NonNull DefaultValue<C, T> defaultValue
        ) {
            return this.argument(this.argumentToComponent(argument.build()).optional(defaultValue));
        }

        /**
         * Adds a new required command argument by interacting with a constructed command argument builder
         *
         * @param clazz           Argument class
         * @param name            Argument name
         * @param builderConsumer Builder consumer
         * @param <T>             Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> required(
                final @NonNull Class<T> clazz,
                final @NonNull String name,
                final @NonNull Consumer<CommandArgument.Builder<C, T>> builderConsumer
        ) {
            final CommandArgument.Builder<C, T> builder = CommandArgument.ofType(clazz, name);
            if (this.commandManager != null) {
                builder.manager(this.commandManager);
            }
            builderConsumer.accept(builder);
            return this.argument(this.argumentToComponent(builder.build()));
        }

        /**
         * Adds a new optional command argument by interacting with a constructed command argument builder
         *
         * @param clazz           Argument class
         * @param name            Argument name
         * @param builderConsumer Builder consumer
         * @param <T>             Argument type
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public <T> @NonNull Builder<C> optional(
                final @NonNull Class<T> clazz,
                final @NonNull String name,
                final @NonNull Consumer<CommandArgument.Builder<C, T>> builderConsumer
        ) {
            final CommandArgument.Builder<C, T> builder = CommandArgument.ofType(clazz, name);
            if (this.commandManager != null) {
                builder.manager(this.commandManager);
            }
            builderConsumer.accept(builder);
            return this.argument(this.argumentToComponent(builder.build()).optional());
        }

        /**
         * Adds the given {@code argument} to the command
         * <p>
         * The component will be copied using {@link CommandComponent#copy()} before being inserted into the command tree.
         *
         * @param argument Argument to add
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> argument(
                final @NonNull CommandComponent<C> argument
        ) {
            final List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
            commandComponents.add(argument.copy());
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
         * Adds the given {@code argument} to the command
         * <p>
         * The component will be copied using {@link CommandComponent#copy()} before being inserted into the command tree.
         *
         * @param builder builder that builds the component to add
         * @return New builder instance with the command argument inserted into the argument list
         * @since 2.0.0
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<C> argument(
                final CommandComponent.Builder<?, ?> builder
        ) {
            return this.argument((CommandComponent) builder.build());
        }

        private <T> CommandComponent.@NonNull Builder<C, T> argumentToComponent(final @NonNull CommandArgument<C, T> argument) {
            return CommandComponent.<C, T>builder()
                    .commandManager(this.commandManager)
                    .key(argument.getKey())
                    .parser(argument.parserDescriptor())
                    .suggestionProvider(argument.suggestionProvider())
                    .preprocessors(argument.argumentPreprocessors());
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
         * @since 1.4.0
         */
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V> @NonNull Builder<C> requiredArgumentPair(
                final @NonNull String name,
                final @NonNull Pair<@NonNull String, @NonNull String> names,
                final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.required(ArgumentPair.of(this.commandManager, name, names, parserPair).simple(), description);
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V> @NonNull Builder<C> optionalArgumentPair(
                final @NonNull String name,
                final @NonNull Pair<@NonNull String, @NonNull String> names,
                final @NonNull Pair<@NonNull Class<U>, @NonNull Class<V>> parserPair,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.optional(ArgumentPair.of(this.commandManager, name, names, parserPair).simple(), description);
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, O> @NonNull Builder<C> requiredArgumentPair(
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
            return this.required(
                    ArgumentPair.of(this.commandManager, name, names, parserPair).withMapper(outputType, mapper),
                    description
            );
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, O> @NonNull Builder<C> optionalArgumentPair(
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
            return this.optional(
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
         * @since 1.4.0
         */
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, W> @NonNull Builder<C> requiredArgumentTriplet(
                final @NonNull String name,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.required(ArgumentTriplet.of(this.commandManager, name, names, parserTriplet).simple(), description);
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, W> @NonNull Builder<C> optionalArgumentTriplet(
                final @NonNull String name,
                final @NonNull Triplet<String, String, String> names,
                final @NonNull Triplet<Class<U>, Class<V>, Class<W>> parserTriplet,
                final @NonNull ArgumentDescription description
        ) {
            if (this.commandManager == null) {
                throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
            }
            return this.optional(ArgumentTriplet.of(this.commandManager, name, names, parserTriplet).simple(), description);
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, W, O> @NonNull Builder<C> requiredArgumentTriplet(
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
            return this.required(
                    ArgumentTriplet.of(this.commandManager, name, names, parserTriplet).withMapper(outputType, mapper),
                    description
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
        @API(status = API.Status.STABLE, since = "1.4.0")
        public <U, V, W, O> @NonNull Builder<C> optionalArgumentTriplet(
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
            return this.optional(
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
         * Returns the current command execution handler.
         *
         * @return the current handler
         * @since 1.7.0
         */
        @API(status = API.Status.STABLE, since = "1.7.0")
        public @NonNull CommandExecutionHandler<C> handler() {
            return this.commandExecutionHandler;
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
         * all of the proxied command's variable command arguments will be inserted into this
         * builder instance, in the order they are declared in the proxied command. Furthermore,
         * the proxied command's command handler will be shown by the command that is currently
         * being built. If the current command builder does not have a permission node set, this
         * too will be copied.
         *
         * @param command Command to proxy
         * @return New builder that proxies the given command
         */
        public @NonNull Builder<C> proxies(final @NonNull Command<C> command) {
            Builder<C> builder = this;
            for (final CommandComponent<C> component : command.components()) {
                if (component.type() == CommandComponent.ComponentType.LITERAL) {
                    continue;
                }
                final CommandComponent<C> componentCopy = component.copy();
                builder = builder.argument(componentCopy);
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
                final CommandFlagParser<C> flagParser = new CommandFlagParser<>(this.flags);
                final CommandComponent<C> flagComponent =
                        CommandComponent.<C, Object>builder()
                                .name("flags")
                                .parser(flagParser)
                                .valueType(Object.class)
                                .description(ArgumentDescription.of("Command flags"))
                                .build();
                commandComponents.add(flagComponent);
            }
            return new Command<>(
                    Collections.unmodifiableList(commandComponents),
                    this.commandExecutionHandler,
                    this.senderType,
                    this.commandPermission,
                    this.commandMeta
            );
        }

        /**
         * Essentially a {@link java.util.function.UnaryOperator} for {@link Builder},
         * but as a separate interface to avoid conflicts.
         *
         * @param <C> sender type
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        @FunctionalInterface
        public interface Applicable<C> {

            /**
             * Accepts a {@link Builder} and returns either the same or a modified {@link Builder} instance.
             *
             * @param builder builder
             * @return possibly modified builder
             * @since 1.8.0
             */
            @API(status = API.Status.STABLE, since = "1.8.0")
            @NonNull Builder<C> applyToCommandBuilder(@NonNull Builder<C> builder);
        }
    }
}
