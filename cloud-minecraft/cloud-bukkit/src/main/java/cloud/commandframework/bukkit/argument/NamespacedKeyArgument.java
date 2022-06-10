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
package cloud.commandframework.bukkit.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.BukkitParserParameters;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * cloud argument type that parses {@link NamespacedKey}s
 *
 * @param <C> sender type
 * @since 1.7.0
 */
public final class NamespacedKeyArgument<C> extends CommandArgument<C, NamespacedKey> {

    private NamespacedKeyArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final boolean requireExplicitNamespace,
            final String defaultNamespace
    ) {
        super(
                required,
                name,
                new Parser<>(requireExplicitNamespace, defaultNamespace),
                defaultValue,
                NamespacedKey.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return builder instance
     * @since 1.7.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new NamespacedKeyArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link NamespacedKeyArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return argument instance
     * @since 1.7.0
     */
    public static <C> @NonNull NamespacedKeyArgument<C> of(final @NonNull String name) {
        return NamespacedKeyArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link NamespacedKeyArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return argument instance
     * @since 1.7.0
     */
    public static <C> @NonNull NamespacedKeyArgument<C> optional(final @NonNull String name) {
        return NamespacedKeyArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link NamespacedKeyArgument} using the provided default value.
     *
     * @param name         argument name
     * @param defulatValue default name
     * @param <C>          sender type
     * @return argument instance
     * @since 1.7.0
     */
    public static <C> @NonNull NamespacedKeyArgument<C> optional(
            final @NonNull String name,
            final @NonNull NamespacedKey defulatValue
    ) {
        return NamespacedKeyArgument.<C>builder(name).asOptionalWithDefault(defulatValue).build();
    }


    /**
     * Builder for {@link NamespacedKeyArgument}.
     *
     * @param <C> sender type
     * @since 1.7.0
     */
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, NamespacedKey, Builder<C>> {

        private boolean requireExplicitNamespace = false;
        private String defaultNamespace = NamespacedKey.MINECRAFT;

        private Builder(final @NonNull String name) {
            super(NamespacedKey.class, name);
        }

        /**
         * Set to require explicit namespaces (i.e. 'test' will be rejected but 'test:test' will pass).
         *
         * @return this builder
         * @since 1.7.0
         */
        public Builder<C> requireExplicitNamespace() {
            return this.requireExplicitNamespace(true);
        }

        /**
         * Sets whether to require explicit namespaces (i.e. 'test' will be rejected but 'test:test' will pass).
         *
         * @param requireExplicitNamespace whether to require explicit namespaces
         * @return this builder
         * @since 1.7.0
         */
        public Builder<C> requireExplicitNamespace(final boolean requireExplicitNamespace) {
            this.requireExplicitNamespace = requireExplicitNamespace;
            return this;
        }

        /**
         * Sets a custom default namespace. By default, it is {@link NamespacedKey#MINECRAFT}.
         *
         * @param defaultNamespace default namespace
         * @return this builder
         * @since 1.7.0
         */
        public Builder<C> defaultNamespace(final String defaultNamespace) {
            this.defaultNamespace = defaultNamespace;
            return this;
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.7.0
         */
        public Builder<C> asOptionalWithDefault(final NamespacedKey defaultValue) {
            return this.asOptionalWithDefault(defaultValue.getNamespace() + ':' + defaultValue.getKey());
        }

        @Override
        public @NonNull NamespacedKeyArgument<C> build() {
            return new NamespacedKeyArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription(),
                    this.requireExplicitNamespace,
                    this.defaultNamespace
            );
        }

    }

    /**
     * Parser for {@link NamespacedKey}.
     *
     * @param <C> sender type
     * @since 1.7.0
     */
    public static final class Parser<C> implements ArgumentParser<C, NamespacedKey> {

        private final boolean requireExplicitNamespace;
        private final String defaultNamespace;

        /**
         * Create a new {@link Parser}.
         *
         * @param requireExplicitNamespace whether to require an explicit namespace
         * @param defaultNamespace         default namespace
         * @since 1.7.0
         */
        public Parser(
                final boolean requireExplicitNamespace,
                final String defaultNamespace
        ) {
            this.requireExplicitNamespace = requireExplicitNamespace;
            this.defaultNamespace = defaultNamespace;
        }

        @SuppressWarnings("deprecation")
        @Override
        public @NonNull ArgumentParseResult<NamespacedKey> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            final String[] split = input.split(":");
            final int maxSemi = split.length > 1 ? 1 : 0;
            if (input.length() - input.replace(":", "").length() > maxSemi) {
                // Wrong number of ':'
                return ArgumentParseResult.failure(new NamespacedKeyParseException(
                        BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext
                ));
            }
            try {
                final NamespacedKey ret;
                if (split.length == 1) {
                    if (this.requireExplicitNamespace) {
                        // Did not provide explicit namespace when option was enabled
                        return ArgumentParseResult.failure(new NamespacedKeyParseException(
                                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE, input, commandContext
                        ));
                    }
                    ret = new NamespacedKey(this.defaultNamespace, split[0]);
                } else if (split.length == 2) {
                    ret = new NamespacedKey(split[0], split[1]);
                } else {
                    // Too many parts, ie not:valid:input
                    return ArgumentParseResult.failure(new NamespacedKeyParseException(
                            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext
                    ));
                }

                // Success!
                inputQueue.remove();
                return ArgumentParseResult.success(ret);
            } catch (final IllegalArgumentException ex) {
                final Caption caption = ex.getMessage().contains("namespace") // stupid but works
                        ? BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE
                        // this will also get used if someone puts >256 chars
                        : BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY;
                return ArgumentParseResult.failure(new NamespacedKeyParseException(caption, input, commandContext));
            }
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final List<String> ret = new ArrayList<>();
            ret.add(this.defaultNamespace + ":");
            if (!input.contains(":") && !input.isEmpty()) {
                ret.add(input + ":");
            }
            return ret;
        }

    }


    /**
     * Exception used when {@link Parser} fails.
     *
     * @since 1.7.0
     */
    public static final class NamespacedKeyParseException extends ParserException {

        private static final long serialVersionUID = -482592639358941441L;
        private final String input;

        /**
         * Creates a new {@link NamespacedKeyParseException}.
         *
         * @param caption caption
         * @param input   input
         * @param context command context
         * @since 1.7.0
         */
        public NamespacedKeyParseException(
                final @NonNull Caption caption,
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    Parser.class,
                    context,
                    caption,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Gets the input string.
         *
         * @return input
         * @since 1.7.0
         */
        public @NonNull String getInput() {
            return this.input;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final NamespacedKeyParseException that = (NamespacedKeyParseException) o;
            return this.input.equals(that.input) && this.errorCaption().equals(that.errorCaption());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input, this.errorCaption());
        }

    }

    /**
     * Called reflectively by {@link BukkitCommandManager}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull BukkitCommandManager<C> commandManager) {
        commandManager.getParserRegistry()
                .registerParserSupplier(TypeToken.get(NamespacedKey.class), params -> new Parser<>(
                        params.has(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE),
                        params.get(BukkitParserParameters.DEFAULT_NAMESPACE, NamespacedKey.MINECRAFT)
                ));
    }

}
