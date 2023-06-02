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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Argument type for adventure {@link Component Components}.
 *
 * @param <C> sender type
 * @param <O> output component type
 */
@SuppressWarnings({"NonExtendableApiUsage", "unused"})
// For some reason intellij reports using `O extends Component` as invalid api usage
public final class ComponentArgument<C, O extends Component> extends CommandArgument<C, O> {

    private ComponentArgument(
            final boolean required,
            final @NonNull String name,
            final StringArgument.@NonNull StringMode stringMode,
            final ComponentSerializer<? extends Component, O, String> serializer,
            final TypeToken<O> outputComponentType,
            final @NonNull String defaultValue,
            final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription description
    ) {
        super(
                required,
                name,
                new Parser<>(stringMode, new StringArgument.StringParser<C>(stringMode, suggestionsProvider)
                        .map(ComponentArgument.createParserMapper(serializer))),
                defaultValue,
                outputComponentType,
                suggestionsProvider,
                description,
                new LinkedList<>()
        );
    }

    /**
     * Creates a {@link ComponentArgument} builder.
     *
     * @param name argument name
     * @param outputComponentType output component type
     * @param serializer component serializer
     * @return a new builder
     * @param <C> sender type
     * @param <O> output component type
     */
    public static <C, O extends Component> @NonNull Builder<C, O> builder(
            final @NonNull String name,
            final @NonNull Class<O> outputComponentType,
            final @NonNull ComponentSerializer<? extends Component, O, String> serializer
    ) {
        return new Builder<>(name, outputComponentType, serializer);
    }

    /**
     * Creates a {@link ComponentArgument} builder with the {@link PlainTextComponentSerializer}.
     *
     * @param name argument name
     * @return a new builder
     * @param <C> sender type
     */
    public static <C> @NonNull Builder<C, TextComponent> plainBuilder(final @NonNull String name) {
        return ComponentArgument.builder(name, TextComponent.class, PlainTextComponentSerializer.plainText());
    }

    /**
     * Creates a {@link ComponentArgument}.
     *
     * @param name argument name
     * @param outputComponentType output component type
     * @param serializer component serializer
     * @return a new ComponentArgument
     * @param <C> sender type
     * @param <O> output component type
     */
    public static <C, O extends Component> @NonNull ComponentArgument<C, O> of(
            final @NonNull String name,
            final @NonNull Class<O> outputComponentType,
            final @NonNull ComponentSerializer<? extends Component, O, String> serializer
    ) {
        return ComponentArgument.<C, O>builder(name, outputComponentType, serializer).build();
    }

    /**
     * Creates a {@link ComponentArgument}.
     *
     * @param name argument name
     * @param outputComponentType output component type
     * @param serializer component serializer
     * @param stringMode the string mode to parse with
     * @return a new ComponentArgument
     * @param <C> sender type
     * @param <O> output component type
     */
    public static <C, O extends Component> @NonNull ComponentArgument<C, O> of(
            final @NonNull String name,
            final @NonNull Class<O> outputComponentType,
            final @NonNull ComponentSerializer<? extends Component, O, String> serializer,
            final StringArgument.@NonNull StringMode stringMode
    ) {
        return ComponentArgument.<C, O>builder(name, outputComponentType, serializer).withMode(stringMode).build();
    }

    public static final class Builder<C, O extends Component> extends CommandArgument.Builder<C, O> {

        private StringArgument.StringMode stringMode = StringArgument.StringMode.SINGLE;
        private final ComponentSerializer<? extends Component, O, String> serializer;

        private Builder(
                final @NonNull String name,
                final @NonNull Class<O> outputComponentType,
                final ComponentSerializer<? extends Component, O, String> serializer
        ) {
            super(outputComponentType, name);
            this.serializer = serializer;
        }

        /**
         * Set the String mode
         *
         * @param stringMode String mode to parse with
         * @return Builder instance
         */
        public @This @NonNull Builder<C, O> withMode(final StringArgument.@NonNull StringMode stringMode) {
            this.stringMode = stringMode;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C, O> greedy() {
            this.stringMode = StringArgument.StringMode.GREEDY;
            return this;
        }

        /**
         * Greedy string that will consume the input until a flag is present.
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C, O> greedyFlagYielding() {
            this.stringMode = StringArgument.StringMode.GREEDY_FLAG_YIELDING;
            return this;
        }

        /**
         * Set the string mode to single
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C, O> single() {
            this.stringMode = StringArgument.StringMode.SINGLE;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C, O> quoted() {
            this.stringMode = StringArgument.StringMode.QUOTED;
            return this;
        }

        /**
         * Builder a new string argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull ComponentArgument<C, O> build() {
            return new ComponentArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.stringMode,
                    this.serializer,
                    this.getValueType(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    // You need a parser type in order to register a brigadier mapping
    public static final class Parser<C, O extends Component> implements ArgumentParser<C, O> {

        private final StringArgument.StringMode stringMode;
        private final ArgumentParser<C, O> delegate;

        /**
         * Creates a parser for component arguments with a delegate parser.
         *
         * @param stringMode the string mode
         * @param delegate the delegate parser
         */
        public Parser(final StringArgument.@NonNull StringMode stringMode, final @NonNull ArgumentParser<C, O> delegate) {
            this.stringMode = stringMode;
            this.delegate = delegate;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull O> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            return this.delegate.parse(commandContext, inputQueue);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.delegate.suggestions(commandContext, input);
        }

        @Override
        public boolean isContextFree() {
            return this.delegate.isContextFree();
        }

        @Override
        public int getRequestedArgumentCount() {
            return this.delegate.getRequestedArgumentCount();
        }

        /**
         * Get the string mode used for this parser.
         *
         * @return the string mode
         */
        public StringArgument.@NonNull StringMode getStringMode() {
            return this.stringMode;
        }
    }

    private static <C, O extends Component> BiFunction<CommandContext<C>, String, ArgumentParseResult<O>> createParserMapper(
            final ComponentSerializer<? extends Component, O, String> serializer
    ) {
        return (context, s) -> {
            try {
                return ArgumentParseResult.success(serializer.deserialize(s));
            } catch (final Exception exception) {
                return ArgumentParseResult.failure(exception);
            }
        };
    }
}
