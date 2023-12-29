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
package cloud.commandframework.jda.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Command Argument for {@link MessageChannel}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class ChannelParser<C> implements ArgumentParser<C, MessageChannel> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, MessageChannel> channelParser(final @NonNull Set<ParserMode> modes) {
        return ParserDescriptor.of(new ChannelParser<>(modes), MessageChannel.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #channelParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, MessageChannel> channelComponent(final @NonNull Set<ParserMode> modes) {
        return CommandComponent.<C, MessageChannel>builder().parser(channelParser(modes));
    }

    private final Set<ParserMode> modes;

    /**
     * Construct a new channel parser.
     *
     * @param modes parser modes to use
     */
    public ChannelParser(final @NonNull Set<ParserMode> modes) {
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("At least one parsing mode is required");
        }

        this.modes = modes;
    }

    /**
     * Get the modes enabled on the parser
     *
     * @return Set of Modes
     */
    public @NotNull Set<ParserMode> getModes() {
        return this.modes;
    }


    public enum ParserMode {
        MENTION,
        ID,
        NAME
    }

    @Override
    public @NonNull ArgumentParseResult<MessageChannel> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();

        if (!commandContext.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = commandContext.get("MessageReceivedEvent");
        Exception exception = null;

        if (!event.isFromGuild()) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Channel arguments can only be parsed in guilds"));
        }

        if (this.modes.contains(ParserMode.MENTION)) {
            if (input.startsWith("<#") && input.endsWith(">")) {
                final String id = input.substring(2, input.length() - 1);

                try {
                    final ArgumentParseResult<MessageChannel> channel = this.channelFromId(event, input, id);
                    commandInput.readString();
                    return channel;
                } catch (final ChannelNotFoundException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(
                        String.format("Input '%s' is not a channel mention.", input)
                );
            }
        }

        if (this.modes.contains(ParserMode.ID)) {
            try {
                final ArgumentParseResult<MessageChannel> result = this.channelFromId(event, input, input);
                commandInput.readString();
                return result;
            } catch (final ChannelNotFoundException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes.contains(ParserMode.NAME)) {
            final List<TextChannel> channels = event.getGuild().getTextChannelsByName(input, true);

            if (channels.isEmpty()) {
                exception = new ChannelNotFoundException(input);
            } else if (channels.size() > 1) {
                exception = new TooManyChannelsFoundParseException(input);
            } else {
                commandInput.readString();
                return ArgumentParseResult.success(channels.get(0));
            }
        }

        assert exception != null;
        return ArgumentParseResult.failure(exception);
    }

    private @NonNull ArgumentParseResult<MessageChannel> channelFromId(
            final @NonNull MessageReceivedEvent event,
            final @NonNull String input,
            final @NonNull String id
    )
            throws ChannelNotFoundException, NumberFormatException {
        final MessageChannel channel = event.getGuild().getTextChannelById(id);

        if (channel == null) {
            throw new ChannelNotFoundException(input);
        }

        return ArgumentParseResult.success(channel);
    }


    public static class ChannelParseException extends IllegalArgumentException {

        private static final long serialVersionUID = 2724288304060572202L;
        private final String input;

        /**
         * Construct a new channel parse exception
         *
         * @param input String input
         */
        public ChannelParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the users input
         *
         * @return users input
         */
        public final @NonNull String getInput() {
            return this.input;
        }
    }


    public static final class TooManyChannelsFoundParseException extends ChannelParseException {

        private static final long serialVersionUID = -507783063742841507L;

        /**
         * Construct a new channel parse exception
         *
         * @param input String input
         */
        public TooManyChannelsFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many channels found for '%s'.", getInput());
        }
    }


    public static final class ChannelNotFoundException extends ChannelParseException {

        private static final long serialVersionUID = -8299458048947528494L;

        /**
         * Construct a new channel parse exception
         *
         * @param input String input
         */
        public ChannelNotFoundException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Channel not found for '%s'.", getInput());
        }
    }
}
