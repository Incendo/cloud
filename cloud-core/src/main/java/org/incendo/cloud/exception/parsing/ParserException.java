//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package org.incendo.cloud.exception.parsing;

import java.util.Arrays;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;

@SuppressWarnings("serial")
@API(status = API.Status.STABLE)
public class ParserException extends IllegalArgumentException {

    private final Class<?> argumentParser;
    private final CommandContext<?> context;
    private final Caption errorCaption;
    private final CaptionVariable[] captionVariables;

    protected ParserException(
            final @Nullable Throwable cause,
            final @NonNull Class<?> argumentParser,
            final @NonNull CommandContext<?> context,
            final @NonNull Caption errorCaption,
            final @NonNull CaptionVariable... captionVariables
    ) {
        super(cause);
        this.argumentParser = argumentParser;
        this.context = context;
        this.errorCaption = errorCaption;
        this.captionVariables = captionVariables;
    }

    protected ParserException(
            final @NonNull Class<?> argumentParser,
            final @NonNull CommandContext<?> context,
            final @NonNull Caption errorCaption,
            final @NonNull CaptionVariable... captionVariables
    ) {
        this(null /* cause */, argumentParser, context, errorCaption, captionVariables);
    }

    @Override
    public final String getMessage() {
        return this.context.formatCaption(this.errorCaption, this.captionVariables);
    }

    /**
     * Formats the error caption using the given {@code formatter}.
     *
     * @param <T>        the type produced by the formatter
     * @param formatter the formatter
     * @return the formatted caption
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @API(status = API.Status.STABLE)
    public final <T> @NonNull T formatCaption(final @NonNull CaptionFormatter<?, T> formatter) {
        return (T) this.context.formatCaption((CaptionFormatter) formatter, this.errorCaption, this.captionVariables());
    }

    /**
     * Get the error caption for this parser exception
     *
     * @return The caption
     */
    @API(status = API.Status.STABLE)
    public @NonNull Caption errorCaption() {
        return this.errorCaption;
    }

    /**
     * Get a copy of the caption variables present in this parser exception.
     * The returned array may be empty if no variables are present.
     *
     * @return The caption variables
     */
    @API(status = API.Status.STABLE)
    public @NonNull CaptionVariable @NonNull [] captionVariables() {
        return Arrays.copyOf(this.captionVariables, this.captionVariables.length);
    }

    /**
     * Returns the argument parser class.
     *
     * @return argument parser class
     */
    public final @NonNull Class<?> argumentParserClass() {
        return this.argumentParser;
    }

    /**
     * Returns the command context.
     *
     * @return command context
     */
    public final @NonNull CommandContext<?> context() {
        return this.context;
    }
}
