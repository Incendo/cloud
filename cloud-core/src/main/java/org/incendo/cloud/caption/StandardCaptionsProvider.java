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
package org.incendo.cloud.caption;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Provides default captions for all {@link StandardCaptionKeys}.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public final class StandardCaptionsProvider<C> extends DelegatingCaptionProvider<C> {

    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_BOOLEAN}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_BOOLEAN = "Could not parse boolean from '<input>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_NUMBER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_NUMBER = "'<input>' is not a valid number in the range <min> to <max>";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_CHAR}
     */
    public static final String ARGUMENT_PARSE_FAILURE_CHAR = "'<input>' is not a valid character";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_ENUM}
     */
    public static final String ARGUMENT_PARSE_FAILURE_ENUM = "'<input>' is not one of the following: <acceptableValues>";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_STRING}
     */
    public static final String ARGUMENT_PARSE_FAILURE_STRING = "'<input>' is not a valid string of type <stringMode>";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_UUID}
     */
    public static final String ARGUMENT_PARSE_FAILURE_UUID = "'<input>' is not a valid UUID";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_REGEX}
     */
    public static final String ARGUMENT_PARSE_FAILURE_REGEX = "'<input>' does not match '<pattern>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG}
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG = "Unknown flag '<flag>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG}
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG = "Duplicate flag '<flag>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED}
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED = "No flag started. Don't know what to do with '<input>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT = "Missing argument for '<flag>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION}
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION = "You don't have permission to use '<flag>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_COLOR}
     */
    public static final String ARGUMENT_PARSE_FAILURE_COLOR = "'<input>' is not a valid color";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_DURATION}
     */
    public static final String ARGUMENT_PARSE_FAILURE_DURATION = "'<input>' is not a duration format";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT = "Missing component '<component>'";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE = "Invalid component '<component>': <failure>";
    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_EITHER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_EITHER = "Could not resolve <primary> or <fallback> from '<input>'";

    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_UNEXPECTED}
     */
    public static final String EXCEPTION_UNEXPECTED = "An internal error occurred while attempting to perform this command.";
    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_INVALID_ARGUMENT}
     */
    public static final String EXCEPTION_INVALID_ARGUMENT = "Invalid Command Argument: <cause>.";
    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_NO_SUCH_COMMAND}
     */
    public static final String EXCEPTION_NO_SUCH_COMMAND = "Unknown Command.";
    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_NO_PERMISSION}
     */
    public static final String EXCEPTION_NO_PERMISSION = "I'm sorry, but you do not have permission to perform this command.";
    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_INVALID_SENDER}
     */
    public static final String EXCEPTION_INVALID_SENDER =
            "<actual> is not allowed to execute that command. Must be of type <expected>";
    /**
     * Default caption for {@link StandardCaptionKeys#EXCEPTION_INVALID_SYNTAX}
     */
    public static final String EXCEPTION_INVALID_SYNTAX = "Invalid Command Syntax. Correct command syntax is: <syntax>.";

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
            .putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN,
                    ARGUMENT_PARSE_FAILURE_BOOLEAN
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
                    ARGUMENT_PARSE_FAILURE_NUMBER
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR,
                    ARGUMENT_PARSE_FAILURE_CHAR
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
                    ARGUMENT_PARSE_FAILURE_ENUM
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                    ARGUMENT_PARSE_FAILURE_STRING
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
                    ARGUMENT_PARSE_FAILURE_UUID
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX,
                    ARGUMENT_PARSE_FAILURE_REGEX
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                    ARGUMENT_PARSE_FAILURE_COLOR
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                    ARGUMENT_PARSE_FAILURE_DURATION
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG,
                    ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG,
                    ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED,
                    ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT,
                    ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION,
                    ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT,
                    ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE,
                    ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE
            ).putCaption(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_EITHER,
                    ARGUMENT_PARSE_FAILURE_EITHER
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_UNEXPECTED,
                    EXCEPTION_UNEXPECTED
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                    EXCEPTION_INVALID_ARGUMENT
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND,
                    EXCEPTION_NO_SUCH_COMMAND
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
                    EXCEPTION_NO_PERMISSION
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                    EXCEPTION_INVALID_SENDER
            ).putCaption(
                    StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                    EXCEPTION_INVALID_SYNTAX
            ).build();

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
