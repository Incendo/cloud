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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * EntitySelector parse exception
 */
public final class SelectorParseException extends ParserException {

    private static final long serialVersionUID = 1900826717897819065L;
    private final String input;
    private final FailureReason reason;

    /**
     * Construct a new EntitySelector parse exception
     *
     * @param input   String input
     * @param context Command context
     * @param reason  Reason for parse failure
     * @param parser  The parser class
     */
    public SelectorParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context,
            final @NonNull FailureReason reason,
            final @NonNull Class<?> parser
    ) {
        super(
                parser,
                context,
                reason.getCaption(),
                CaptionVariable.of("input", input)
        );
        this.reason = reason;
        this.input = input;
    }

    /**
     * Get the supplied input
     *
     * @return String value
     */
    public @NonNull String getInput() {
        return input;
    }

    /**
     * Get the reason of failure for the selector parser
     *
     * @return Failure reason
     * @since 1.2.0
     */
    public @NonNull FailureReason getFailureReason() {
        return this.reason;
    }

    /**
     * Reasons for which selector parsing may fail
     *
     * @since 1.1.0
     */
    public enum FailureReason {

        UNSUPPORTED_VERSION(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED),
        MALFORMED_SELECTOR(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED),
        TOO_MANY_PLAYERS(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS),
        TOO_MANY_ENTITIES(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES),
        NON_PLAYER_IN_PLAYER_SELECTOR(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER);


        private final Caption caption;

        FailureReason(final @NonNull Caption caption) {
            this.caption = caption;
        }

        /**
         * Get the caption used for this failure reason
         *
         * @return The caption
         */
        public @NonNull Caption getCaption() {
            return this.caption;
        }

    }

}
