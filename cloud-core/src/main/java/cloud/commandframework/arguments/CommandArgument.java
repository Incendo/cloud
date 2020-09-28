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
package cloud.commandframework.arguments;

import cloud.commandframework.arguments.parser.ArgumentParser;
import io.leangen.geantyref.TypeToken;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.context.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * A argument that belongs to a command
 *
 * @param <C> Command sender type
 * @param <T> The type that the argument parses into
 */
@SuppressWarnings("unused")
public class CommandArgument<C, T> implements Comparable<CommandArgument<?, ?>> {

    /**
     * Pattern for command argument names
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9]+");

    /**
     * Indicates whether or not the argument is required
     * or not. All arguments prior to any other required
     * argument must also be required, such that the predicate
     * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
     * where {c_0, ..., c_n-1} is the set of command arguments.
     */
    private final boolean required;
    /**
     * The command argument name. This might be exposed
     * to command senders and so should be chosen carefully.
     */
    private final String name;
    /**
     * The parser that is used to parse the command input
     * into the corresponding command type
     */
    private final ArgumentParser<C, T> parser;
    /**
     * Default value, will be empty if none was supplied
     */
    private final String defaultValue;
    /**
     * The type that is produces by the argument's parser
     */
    private final TypeToken<T> valueType;
    /**
     * Suggestion provider
     */
    private final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;

    private Command<C> owningCommand;

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     */
    public CommandArgument(final boolean required,
                           @Nonnull final String name,
                           @Nonnull final ArgumentParser<C, T> parser,
                           @Nonnull final String defaultValue,
                           @Nonnull final TypeToken<T> valueType,
                           @Nullable final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        this.required = required;
        this.name = Objects.requireNonNull(name, "Name may not be null");
        if (!NAME_PATTERN.asPredicate().test(name)) {
            throw new IllegalArgumentException("Name must be alphanumeric");
        }
        this.parser = Objects.requireNonNull(parser, "Parser may not be null");
        this.defaultValue = defaultValue;
        this.valueType = valueType;
        this.suggestionsProvider = suggestionsProvider == null
                                   ? buildDefaultSuggestionsProvider(this)
                                   : suggestionsProvider;
    }

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     */
    public CommandArgument(final boolean required,
                           @Nonnull final String name,
                           @Nonnull final ArgumentParser<C, T> parser,
                           @Nonnull final String defaultValue,
                           @Nonnull final Class<T> valueType,
                           @Nullable final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        this(required, name, parser, defaultValue, TypeToken.of(valueType), suggestionsProvider);
    }

    /**
     * Construct a new command argument
     *
     * @param required  Whether or not the argument is required
     * @param name      The argument name
     * @param parser    The argument parser
     * @param valueType Type produced by the parser
     */
    public CommandArgument(final boolean required,
                           @Nonnull final String name,
                           @Nonnull final ArgumentParser<C, T> parser,
                           @Nonnull final Class<T> valueType) {
        this(required, name, parser, "", valueType, null);
    }

    private static <C> BiFunction<CommandContext<C>, String, List<String>> buildDefaultSuggestionsProvider(
            @Nonnull final CommandArgument<C, ?> argument) {
        return new DelegatingSuggestionsProvider<>(argument.getName(), argument.getParser());
    }

    /**
     * Create a new command argument
     *
     * @param clazz Argument class
     * @param name  Argument name
     * @param <C>   Command sender type
     * @param <T>   Argument Type. Used to make the compiler happy.
     * @return Argument builder
     */
    @Nonnull
    public static <C, T> CommandArgument.Builder<C, T> ofType(@Nonnull final TypeToken<T> clazz,
                                                              @Nonnull final String name) {
        return new Builder<>(clazz, name);
    }

    /**
     * Create a new command argument
     *
     * @param clazz Argument class
     * @param name  Argument name
     * @param <C>   Command sender type
     * @param <T>   Argument Type. Used to make the compiler happy.
     * @return Argument builder
     */
    @Nonnull
    public static <C, T> CommandArgument.Builder<C, T> ofType(@Nonnull final Class<T> clazz,
                                                              @Nonnull final String name) {
        return new Builder<>(TypeToken.get(clazz), name);
    }

    /**
     * Check whether or not the command argument is required
     *
     * @return {@code true} if the argument is required, {@code false} if not
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Get the command argument name;
     *
     * @return Argument name
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Get the parser that is used to parse the command input
     * into the corresponding command type
     *
     * @return Command parser
     */
    @Nonnull
    public ArgumentParser<C, T> getParser() {
        return this.parser;
    }

    @Nonnull
    @Override
    public final String toString() {
        return String.format("%s{name=%s}", this.getClass().getSimpleName(), this.name);
    }

    /**
     * Get the owning command
     *
     * @return Owning command
     */
    @Nullable
    public Command<C> getOwningCommand() {
        return this.owningCommand;
    }

    /**
     * Set the owning command
     *
     * @param owningCommand Owning command
     */
    public void setOwningCommand(@Nonnull final Command<C> owningCommand) {
        if (this.owningCommand != null) {
            throw new IllegalStateException("Cannot replace owning command");
        }
        this.owningCommand = owningCommand;
    }

    /**
     * Get the argument suggestions provider
     *
     * @return Suggestions provider
     */
    @Nonnull
    public final BiFunction<CommandContext<C>, String, List<String>> getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandArgument<?, ?> that = (CommandArgument<?, ?>) o;
        return isRequired() == that.isRequired() && Objects.equals(getName(), that.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(isRequired(), getName());
    }

    @Override
    public final int compareTo(@Nonnull final CommandArgument<?, ?> o) {
        if (this instanceof StaticArgument) {
            if (o instanceof StaticArgument) {
                return (this.getName().compareTo(o.getName()));
            } else {
                return -1;
            }
        } else {
            if (o instanceof StaticArgument) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Get the default value
     *
     * @return Default value
     */
    @Nonnull
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Check if the argument has a default value
     *
     * @return {@code true} if the argument has a default value, {@code false} if not
     */
    public boolean hasDefaultValue() {
        return !this.isRequired()
                && !this.getDefaultValue().isEmpty();
    }

    /**
     * Get the type of this argument's value
     *
     * @return Value type
     */
    @Nonnull
    public TypeToken<T> getValueType() {
        return this.valueType;
    }

    /**
     * Create a copy of the command argument
     *
     * @return Copied argument
     */
    @Nonnull
    public CommandArgument<C, T> copy() {
        CommandArgument.Builder<C, T> builder = ofType(this.valueType, this.name);
        builder = builder.withSuggestionsProvider(this.suggestionsProvider);
        builder = builder.withParser(this.parser);
        if (this.isRequired()) {
            builder = builder.asRequired();
        } else if (this.defaultValue.isEmpty()) {
            builder = builder.asOptional();
        } else {
            builder = builder.asOptionalWithDefault(this.defaultValue);
        }
        return builder.build();
    }


    /**
     * Mutable builder for {@link CommandArgument} instances
     *
     * @param <C> Command sender type
     * @param <T> Argument value type
     */
    public static class Builder<C, T> {

        private final TypeToken<T> valueType;
        private final String name;

        private CommandManager<C> manager;
        private boolean required = true;
        private ArgumentParser<C, T> parser;
        private String defaultValue = "";
        private BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;

        protected Builder(@Nonnull final TypeToken<T> valueType,
                          @Nonnull final String name) {
            this.valueType = valueType;
            this.name = name;
        }

        protected Builder(@Nonnull final Class<T> valueType,
                          @Nonnull final String name) {
            this(TypeToken.of(valueType), name);
        }

        /**
         * Set the command manager. Will be used to create a default parser
         * if none was provided
         *
         * @param manager Command manager
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> manager(@Nonnull final CommandManager<C> manager) {
            this.manager = manager;
            return this;
        }

        /**
         * Indicates that the argument is required.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> asRequired() {
            this.required = true;
            return this;
        }

        /**
         * Indicates that the argument is optional.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> asOptional() {
            this.required = false;
            return this;
        }

        /**
         * Indicates that the argument is optional.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @param defaultValue Default value that will be used if none was supplied
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> asOptionalWithDefault(@Nonnull final String defaultValue) {
            this.defaultValue = defaultValue;
            this.required = false;
            return this;
        }

        /**
         * Set the argument parser
         *
         * @param parser Argument parser
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> withParser(@Nonnull final ArgumentParser<C, T> parser) {
            this.parser = Objects.requireNonNull(parser, "Parser may not be null");
            return this;
        }

        /**
         * Set the suggestions provider
         *
         * @param suggestionsProvider Suggestions provider
         * @return Builder instance
         */
        @Nonnull
        public Builder<C, T> withSuggestionsProvider(
                @Nonnull final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
            this.suggestionsProvider = suggestionsProvider;
            return this;
        }

        /**
         * Construct a command argument from the builder settings
         *
         * @return Constructed argument
         */
        @Nonnull
        public CommandArgument<C, T> build() {
            if (this.parser == null && this.manager != null) {
                this.parser = this.manager.getParserRegistry().createParser(valueType, ParserParameters.empty())
                                          .orElse(null);
            }
            if (this.parser == null) {
                this.parser = (c, i) -> ArgumentParseResult
                        .failure(new UnsupportedOperationException("No parser was specified"));
            }
            if (this.suggestionsProvider == null) {
                this.suggestionsProvider = new DelegatingSuggestionsProvider<>(this.name, this.parser);
            }
            return new CommandArgument<>(this.required, this.name, this.parser,
                                         this.defaultValue, this.valueType, this.suggestionsProvider);
        }

        @Nonnull
        protected final String getName() {
            return this.name;
        }

        protected final boolean isRequired() {
            return this.required;
        }

        @Nonnull
        protected final ArgumentParser<C, T> getParser() {
            return this.parser;
        }

        @Nonnull
        protected final String getDefaultValue() {
            return this.defaultValue;
        }

        @Nonnull
        protected final BiFunction<CommandContext<C>, String, List<String>> getSuggestionsProvider() {
            return this.suggestionsProvider;
        }

    }

}
