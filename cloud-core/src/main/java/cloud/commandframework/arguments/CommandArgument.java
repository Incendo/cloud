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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandManager;
import cloud.commandframework.Description;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.CloudKeyHolder;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * An argument that belongs to a command
 *
 * @param <C> Command sender type
 * @param <T> The type that the argument parses into
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public class CommandArgument<C, T> implements Comparable<CommandArgument<?, ?>>, CloudKeyHolder<T>, ParserDescriptor<C, T>,
        SuggestionProvider<C>, PreprocessorHolder<C> {

    /**
     * Pattern for command argument names
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9\\-_]+");

    /**
     * A typed key representing this argument
     */
    private final CloudKey<T> key;
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
     * The type that is produces by the argument's parser
     */
    private final TypeToken<T> valueType;
    /**
     * Suggestion provider
     */
    private final SuggestionProvider<C> suggestionProvider;
    /**
     * Argument preprocessors that allows for extensions to existing argument types
     * without having to update all parsers
     */
    private final Collection<@NonNull ComponentPreprocessor<C>> argumentPreprocessors;

    /**
     * Construct a new command argument
     *
     * @param name                  The argument name
     * @param parser                The argument parser
     * @param valueType             Type produced by the parser
     * @param suggestionProvider    Suggestion provider
     * @param defaultDescription    Default description to use when registering
     * @param argumentPreprocessors Argument preprocessors
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Description defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        this.name = Objects.requireNonNull(name, "Name may not be null");
        if (!NAME_PATTERN.asPredicate().test(name)) {
            throw new IllegalArgumentException("Name must be alphanumeric");
        }
        this.parser = Objects.requireNonNull(parser, "Parser may not be null");
        this.valueType = valueType;
        this.suggestionProvider = suggestionProvider == null
                ? buildDefaultSuggestionProvider(this)
                : suggestionProvider;
        this.argumentPreprocessors = argumentPreprocessors.stream()
                .map(ComponentPreprocessor::wrap)
                .collect(Collectors.toCollection(LinkedList::new));
        this.key = CloudKey.of(this.name, this.valueType);
    }

    /**
     * Construct a new command argument
     *
     * @param name                  The argument name
     * @param parser                The argument parser
     * @param valueType             Type produced by the parser
     * @param suggestionProvider    Suggestion provider
     * @param argumentPreprocessors Argument preprocessors
     */
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        this(
                name,
                parser,
                valueType,
                suggestionProvider,
                Description.empty(),
                argumentPreprocessors
        );
    }

    /**
     * Construct a new command argument
     *
     * @param name               The argument name
     * @param parser             The argument parser
     * @param valueType          Type produced by the parser
     * @param suggestionProvider Suggestion provider
     */
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType,
            final @Nullable SuggestionProvider<C> suggestionProvider
    ) {
        this(name, parser, valueType, suggestionProvider, Collections.emptyList());
    }

    /**
     * Construct a new command argument
     *
     * @param name                The argument name
     * @param parser              The argument parser
     * @param valueType           Type produced by the parser
     * @param suggestionProvider Suggestion provider
     */
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType,
            final @Nullable SuggestionProvider<C> suggestionProvider
    ) {
        this(name, parser, TypeToken.get(valueType), suggestionProvider);
    }

    /**
     * Construct a new command argument
     *
     * @param name                The argument name
     * @param parser              The argument parser
     * @param valueType           Type produced by the parser
     * @param suggestionProvider Suggestion provider
     * @param defaultDescription  Default description to use when registering
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Description defaultDescription
    ) {
        this(name, parser, TypeToken.get(valueType), suggestionProvider);
    }

    /**
     * Construct a new command argument
     *
     * @param name      The argument name
     * @param parser    The argument parser
     * @param valueType Type produced by the parser
     */
    public CommandArgument(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType
    ) {
        this(name, parser, valueType, null);
    }

    private static <C> @NonNull SuggestionProvider<C> buildDefaultSuggestionProvider(final @NonNull CommandArgument<C, ?> argument) {
        return SuggestionProvider.delegating(argument.getParser());
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
    public static <C, T> CommandArgument.@NonNull Builder<C, T> ofType(
            final @NonNull TypeToken<T> clazz,
            final @NonNull String name
    ) {
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
    public static <C, T> CommandArgument.@NonNull Builder<@NonNull C, @NonNull T> ofType(
            final @NonNull Class<T> clazz,
            final @NonNull String name
    ) {
        return new Builder<>(TypeToken.get(clazz), name);
    }

    @Override
    public final @NonNull CloudKey<T> key() {
        return this.key;
    }

    /**
     * Returns a descriptor that describes this argument.
     *
     * @return the descriptor
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull ParserDescriptor<C, T> parserDescriptor() {
        return ParserDescriptor.of(this.parser, this.valueType);
    }

    @Override
    public final @NonNull Collection<@NonNull ComponentPreprocessor<C>> preprocessors() {
        return Collections.unmodifiableCollection(this.argumentPreprocessors);
    }

    /**
     * Get the command argument name.
     *
     * @return Argument name
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Get the parser that is used to parse the command input
     * into the corresponding command type
     *
     * @return Command parser
     */
    public @NonNull ArgumentParser<C, T> getParser() {
        return this.parser;
    }

    @Override
    public final @NonNull String toString() {
        return String.format("%s{name=%s}", this.getClass().getSimpleName(), this.name);
    }

    /**
     * Registers a new preprocessor. If all preprocessor has succeeding {@link ArgumentParseResult results}
     * that all return {@code true}, the argument will be passed onto the parser.
     * <p>
     * It is important that the preprocessor doesn't pop any input. Instead, it should only peek.
     *
     * @param preprocessor Preprocessor
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull @This CommandArgument<C, T> addPreprocessor(
            final @NonNull ComponentPreprocessor<C> preprocessor
    ) {
        this.argumentPreprocessors.add(preprocessor);
        return this;
    }

    /**
     * Returns the suggestion provider for this argument
     *
     * @return the suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
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
        return Objects.equals(this.getName(), that.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.getName());
    }

    @Override
    public final int compareTo(final @NonNull CommandArgument<?, ?> o) {
        if (this.parser instanceof LiteralParser) {
            if (o.parser instanceof LiteralParser) {
                return this.getName().compareTo(o.getName());
            } else {
                return -1;
            }
        } else {
            if (o.parser instanceof LiteralParser) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Get the type of this argument's value
     *
     * @return Value type
     */
    public @NonNull TypeToken<T> getValueType() {
        return this.valueType;
    }

    @Override
    public final @NonNull ArgumentParser<C, T> parser() {
        return this.parser;
    }

    @Override
    public final @NonNull TypeToken<T> valueType() {
        return this.valueType;
    }

    /**
     * Create a copy of the command argument
     *
     * @return Copied argument
     */
    public @NonNull CommandArgument<C, T> copy() {
        CommandArgument.Builder<C, T> builder = ofType(this.valueType, this.name);
        builder = builder.withSuggestionProvider(this.suggestionProvider);
        builder = builder.withParser(this.parser);
        final CommandArgument<C, T> argument = builder.build();
        this.argumentPreprocessors.forEach(argument::addPreprocessor);
        return argument;
    }

    @Override
    public final @NonNull List<@NonNull Suggestion> suggestions(
            @NonNull final CommandContext<C> context,
            @NonNull final String input
    ) {
        return this.suggestionProvider.suggestions(context, input);
    }

    /**
     * Mutable builder for {@link CommandArgument} instances. Builders should extend {@link TypedBuilder} instead of this class.
     *
     * @param <C> Command sender type
     * @param <T> Argument value type
     */
    @API(status = API.Status.STABLE)
    public static class Builder<C, T> implements ParserDescriptor<C, T>, CloudKeyHolder<T>, SuggestionProvider<C>,
            PreprocessorHolder<C> {

        private final TypeToken<T> valueType;
        private final String name;

        private CommandManager<C> manager;
        private ArgumentParser<C, T> parser;
        private SuggestionProvider<C> suggestionProvider;

        private final Collection<BiFunction<@NonNull CommandContext<C>,
                @NonNull CommandInput, @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors = new LinkedList<>();

        private Builder(
                final @NonNull TypeToken<T> valueType,
                final @NonNull String name
        ) {
            this.valueType = valueType;
            this.name = name;
        }

        private Builder(
                final @NonNull Class<T> valueType,
                final @NonNull String name
        ) {
            this(TypeToken.get(valueType), name);
        }

        /**
         * Set the command manager. Will be used to create a default parser
         * if none was provided
         *
         * @param manager Command manager
         * @return Builder instance
         */
        public @NonNull @This Builder<@NonNull C, @NonNull T> manager(final @NonNull CommandManager<C> manager) {
            this.manager = manager;
            return this;
        }

        /**
         * Set the argument parser
         *
         * @param parser Argument parser
         * @return Builder instance
         */
        public @NonNull @This Builder<@NonNull C, @NonNull T> withParser(final @NonNull ArgumentParser<@NonNull C, @NonNull T> parser) {
            this.parser = Objects.requireNonNull(parser, "Parser may not be null");
            return this;
        }

        /**
         * Set the Suggestion provider
         *
         * @param suggestionProvider Suggestion provider
         * @return Builder instance
         */
        public @NonNull @This Builder<@NonNull C, @NonNull T> withSuggestionProvider(
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            this.suggestionProvider = suggestionProvider;
            return this;
        }

        /**
         * Construct a command argument from the builder settings
         *
         * @return Constructed argument
         */
        public @NonNull CommandArgument<@NonNull C, @NonNull T> build() {
            if (this.parser == null && this.manager != null) {
                this.parser = this.manager.parserRegistry().createParser(this.valueType, ParserParameters.empty())
                        .orElse(null);
            }
            if (this.parser == null) {
                this.parser = (c, i) -> ArgumentParseResult
                        .failure(new UnsupportedOperationException("No parser was specified"));
            }
            if (this.suggestionProvider == null) {
                this.suggestionProvider = SuggestionProvider.delegating(this.parser);
            }
            return new CommandArgument<>(
                    this.name,
                    this.parser,
                    this.valueType,
                    this.suggestionProvider,
                    this.argumentPreprocessors
            );
        }

        protected final @NonNull String getName() {
            return this.name;
        }

        protected final @NonNull ArgumentParser<@NonNull C, @NonNull T> getParser() {
            return this.parser;
        }

        protected final @NonNull SuggestionProvider<C> suggestionProvider() {
            return this.suggestionProvider;
        }

        protected final @NonNull Description getDefaultDescription() {
            return Description.empty();
        }

        protected final @NonNull TypeToken<T> getValueType() {
            return this.valueType;
        }

        @Override
        public @NonNull ArgumentParser<C, T> parser() {
            if (this.parser == null) {
                this.build();
            }
            return this.parser;
        }

        @Override
        public @NonNull TypeToken<T> valueType() {
            return this.valueType;
        }

        @Override
        public @NonNull List<@NonNull Suggestion> suggestions(
                @NonNull final CommandContext<C> context,
                @NonNull final String input
        ) {
            if (this.suggestionProvider == null) {
                this.build();
            }
            return this.suggestionProvider.suggestions(context, input);
        }

        @Override
        public @NonNull CloudKey<T> key() {
            return CloudKey.of(this.name, this.valueType);
        }

        @Override
        public @NonNull Collection<ComponentPreprocessor<C>> preprocessors() {
            return this.argumentPreprocessors.stream().map(ComponentPreprocessor::wrap).collect(Collectors.toList());
        }
    }

    /**
     * A variant of builders designed for subclassing, that returns a self type.
     *
     * @param <C> sender type
     * @param <T> argument value type
     * @param <B> the subclass type
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public abstract static class TypedBuilder<C, T, B extends Builder<C, T>> extends Builder<C, T> {

        protected TypedBuilder(
                final @NonNull TypeToken<T> valueType,
                final @NonNull String name
        ) {
            super(valueType, name);
        }

        protected TypedBuilder(
                final @NonNull Class<T> valueType,
                final @NonNull String name
        ) {
            super(valueType, name);
        }

        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NonNull B manager(final @NonNull CommandManager<C> manager) {
            super.manager(manager);
            return this.self();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NonNull B withParser(final @NonNull ArgumentParser<@NonNull C, @NonNull T> parser) {
            super.withParser(parser);
            return this.self();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NonNull Builder<@NonNull C, @NonNull T> withSuggestionProvider(
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            super.withSuggestionProvider(suggestionProvider);
            return this.self();
        }
    }
}
