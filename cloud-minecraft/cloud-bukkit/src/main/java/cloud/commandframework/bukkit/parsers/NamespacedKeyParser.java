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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.BukkitParserParameters;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser for {@link NamespacedKey}.
 *
 * @param <C> sender type
 * @since 1.7.0
 */
public final class NamespacedKeyParser<C> implements ArgumentParser<C, NamespacedKey>,
        BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new namespaced key parser that does not require a specific namespace,
     * and uses {@link NamespacedKey#MINECRAFT} if no namespace is given.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser() {
        return namespacedKeyParser(false /* requireExplicitNamespace */);
    }

    /**
     * Creates a new namespaced key parser that uses {@link NamespacedKey#MINECRAFT} if no namespace is given
     * and {@code requireExplicitNamespace} is {@code false}.
     *
     * @param requireExplicitNamespace whether a namespace is required
     * @param                          <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser(
            final boolean requireExplicitNamespace
    ) {
        return namespacedKeyParser(requireExplicitNamespace, NamespacedKey.MINECRAFT);
    }

    /**
     * Creates a new namespaced key parser.
     *
     * @param requireExplicitNamespace whether a namespace is required
     * @param defaultNamespace         the namespace to use if no namespace is given
     * @param                          <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, NamespacedKey> namespacedKeyParser(
            final boolean requireExplicitNamespace,
            final @NonNull String defaultNamespace
    ) {
        return ParserDescriptor.of(
                new NamespacedKeyParser<>(requireExplicitNamespace, defaultNamespace),
                NamespacedKey.class
        );
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #namespacedKeyParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, NamespacedKey> namespacedKeyComponent() {
        return CommandComponent.<C, NamespacedKey>builder().parser(namespacedKeyParser());
    }

    private final boolean requireExplicitNamespace;
    private final String defaultNamespace;

    /**
     * Create a new {@link NamespacedKeyParser}.
     *
     * @param requireExplicitNamespace whether to require an explicit namespace
     * @param defaultNamespace         default namespace
     * @since 1.7.0
     */
    public NamespacedKeyParser(
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
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
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
                ret = new NamespacedKey(this.defaultNamespace, commandInput.readString());
            } else if (split.length == 2) {
                ret = new NamespacedKey(commandInput.readUntilAndSkip(':'), commandInput.readString());
            } else {
                // Too many parts, ie not:valid:input
                return ArgumentParseResult.failure(new NamespacedKeyParseException(
                        BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext
                ));
            }

            // Success!
            return ArgumentParseResult.success(ret);
        } catch (final IllegalArgumentException ex) {
            final Caption caption = ex.getMessage().contains("namespace") // stupid but works
                    ? BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE
                    // this will also get used if someone puts >256 chars
                    : BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY;
            return ArgumentParseResult.failure(new NamespacedKeyParseException(
                    caption,
                    input,
                    commandContext
            ));
        }
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
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

    /**
     * Called reflectively by {@link BukkitCommandManager}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull BukkitCommandManager<C> commandManager) {
        commandManager.parserRegistry()
                .registerParserSupplier(TypeToken.get(NamespacedKey.class), params -> new NamespacedKeyParser<>(
                        params.has(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE),
                        params.get(BukkitParserParameters.DEFAULT_NAMESPACE, NamespacedKey.MINECRAFT)
                ));
    }


    /**
     * Exception used when {@link NamespacedKeyParser} fails.
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
                    NamespacedKeyParser.class,
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
}
