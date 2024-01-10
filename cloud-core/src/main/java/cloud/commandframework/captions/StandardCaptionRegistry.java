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
package cloud.commandframework.captions;

import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Caption registry that registers constant values for all {@link StandardCaptionKeys}.
 *
 * @param <C> the command sender type
 */
@API(status = API.Status.STABLE)
public class StandardCaptionRegistry<C> implements CaptionRegistry<C> {

    /**
     * Default caption for {@link StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED = "No input was provided";
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

    private final LinkedList<@NonNull CaptionProvider<C>> providers = new LinkedList<>();

    protected StandardCaptionRegistry() {
        this.registerProvider(
                CaptionProvider.<C>constantProvider()
                        .putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED,
                                ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN,
                                ARGUMENT_PARSE_FAILURE_BOOLEAN
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
                                ARGUMENT_PARSE_FAILURE_NUMBER
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR,
                                ARGUMENT_PARSE_FAILURE_CHAR
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
                                ARGUMENT_PARSE_FAILURE_ENUM
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                                ARGUMENT_PARSE_FAILURE_STRING
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
                                ARGUMENT_PARSE_FAILURE_UUID
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX,
                                ARGUMENT_PARSE_FAILURE_REGEX
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                                ARGUMENT_PARSE_FAILURE_COLOR
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                                ARGUMENT_PARSE_FAILURE_DURATION
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG,
                                ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG,
                                ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED,
                                ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT,
                                ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION,
                                ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT,
                                ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT
                        ).putCaptions(
                                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE,
                                ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE
                        ).build()
        );
    }

    @Override
    public final @NonNull String caption(
            final @NonNull Caption caption,
            final @NonNull C sender
    ) {
        for (final CaptionProvider<C> provider : this.providers) {
            final String result = provider.provide(caption, sender);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalArgumentException(String.format("There is no caption stored with key '%s'", caption));
    }

    @Override
    public final @This @NonNull StandardCaptionRegistry<C> registerProvider(
            final @NonNull CaptionProvider<C> provider
    ) {
        this.providers.addFirst(provider);
        return this;
    }
}
