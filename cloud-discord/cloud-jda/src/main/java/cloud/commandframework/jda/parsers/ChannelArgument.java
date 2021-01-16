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
package cloud.commandframework.jda.parsers;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Command Argument for {@link MessageChannel}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class ChannelArgument<C> extends CommandArgument<C, MessageChannel> {

    private final Set<ParserMode> modes;

    private ChannelArgument(
            final boolean required, final @NonNull String name,
            final @NonNull Set<ParserMode> modes
    ) {
        super(required, name, new MessageParser<>(modes), MessageChannel.class);
        this.modes = modes;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, MessageChannel> of(final @NonNull String name) {
        return ChannelArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, MessageChannel> optional(final @NonNull String name) {
        return ChannelArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Get the modes enabled on the parser
     *
     * @return List of Modes
     */
    public @NotNull Set<ParserMode> getModes() {
        return modes;
    }


    public enum ParserMode {
        MENTION,
        ID,
        NAME
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, MessageChannel> {

        private Set<ParserMode> modes = new HashSet<>();

        private Builder(final @NonNull String name) {
            super(MessageChannel.class, name);
        }

        /**
         * Set the modes for the parsers to use
         *
         * @param modes List of Modes
         * @return Builder instance
         */
        public @NonNull Builder<C> withParsers(final @NonNull Set<ParserMode> modes) {
            this.modes = modes;
            return this;
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull ChannelArgument<C> build() {
            return new ChannelArgument<>(this.isRequired(), this.getName(), modes);
        }

    }


    public static final class MessageParser<C> implements ArgumentParser<C, MessageChannel> {

        private final Set<ParserMode> modes;

        /**
         * Construct a new argument parser for {@link MessageChannel}
         *
         * @param modes List of parsing modes to use when parsing
         * @throws java.lang.IllegalStateException If no parsing modes were provided
         */
        public MessageParser(final @NonNull Set<ParserMode> modes) {
            if (modes.isEmpty()) {
                throw new IllegalArgumentException("At least one parsing mode is required");
            }

            this.modes = modes;
        }

        @Override
        public @NonNull ArgumentParseResult<MessageChannel> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        MessageParser.class,
                        commandContext
                ));
            }

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

            if (modes.contains(ParserMode.MENTION)) {
                if (input.startsWith("<#") && input.endsWith(">")) {
                    final String id = input.substring(2, input.length() - 1);

                    try {
                        final ArgumentParseResult<MessageChannel> channel = this.channelFromId(event, input, id);
                        inputQueue.remove();
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

            if (modes.contains(ParserMode.ID)) {
                try {
                    final ArgumentParseResult<MessageChannel> result = this.channelFromId(event, input, input);
                    inputQueue.remove();
                    return result;
                } catch (final ChannelNotFoundException | NumberFormatException e) {
                    exception = e;
                }
            }

            if (modes.contains(ParserMode.NAME)) {
                final List<TextChannel> channels = event.getGuild().getTextChannelsByName(input, true);

                if (channels.size() == 0) {
                    exception = new ChannelNotFoundException(input);
                } else if (channels.size() > 1) {
                    exception = new TooManyChannelsFoundParseException(input);
                } else {
                    inputQueue.remove();
                    return ArgumentParseResult.success(channels.get(0));
                }
            }

            assert exception != null;
            return ArgumentParseResult.failure(exception);
        }

        @Override
        public boolean isContextFree() {
            return true;
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
            return input;
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
