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
package cloud.commandframework.captions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link Caption} instances for messages in cloud-core
 */
@API(status = API.Status.STABLE)
public final class StandardCaptionKeys {

    private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();

    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED = of("argument.parse.failure.no_input_was_provided");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_BOOLEAN = of("argument.parse.failure.boolean");
    /**
     * Variables: {@code <input>}, {@code <min>}, {@code <max>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_NUMBER = of("argument.parse.failure.number");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_CHAR = of("argument.parse.failure.char");
    /**
     * Variables: {@code <input>}, {@code <stringMode>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_STRING = of("argument.parse.failure.string");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_UUID = of("argument.parse.failure.uuid");
    /**
     * Variables: {@code <input>}, {@code <acceptableValues>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_ENUM = of("argument.parse.failure.enum");
    /**
     * Variables: {@code <input>}, {@code <pattern>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_REGEX = of("argument.parse.failure.regex");
    /**
     * Variables: {@code <flag>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG = of("argument.parse.failure.flag.unknown");
    /**
     * Variables: {@code <flag>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG = of("argument.parse.failure.flag.duplicate_flag");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED = of("argument.parse.failure.flag.no_flag_started");
    /**
     * Variables: {@code <flag>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT = of("argument.parse.failure.flag.missing_argument");
    /**
     * Variables: {@code <flag>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION = of("argument.parse.failure.flag.no_permission");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_COLOR = of("argument.parse.failure.color");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_DURATION = of("argument.parse.failure.duration");

    private StandardCaptionKeys() {
    }

    private static @NonNull Caption of(final @NonNull String key) {
        final Caption caption = Caption.of(key);
        RECOGNIZED_CAPTIONS.add(caption);
        return caption;
    }

    /**
     * Get an immutable collection containing all standard caption keys
     *
     * @return Immutable collection of keys
     */
    public static @NonNull Collection<@NonNull Caption> standardCaptionKeys() {
        return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
    }
}
