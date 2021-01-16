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
package cloud.commandframework.exceptions.parsing;

import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An exception which is thrown when an argument's input is unexpectedly null or empty
 *
 * @since 1.1.0
 */
public class NoInputProvidedException extends ParserException {

    private static final long serialVersionUID = 3485754951593709559L;

    /**
     * Construct a new NoInputProvidedException
     *
     * @param argumentParser Argument parser class
     * @param context        Command context
     */
    public NoInputProvidedException(
            final Class<?> argumentParser,
            final @NonNull CommandContext<?> context
    ) {
        super(
                argumentParser,
                context,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED
        );
    }

}
