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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Selector parsers may fail with this exception when they are not able to work
 * in the current environment. This is usually because the server is older than
 * Minecraft 1.13.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class SelectorUnsupportedException extends ParserException {

    private static final long serialVersionUID = -6649195171043474961L;

    /**
     * Create a new {@link SelectorUnsupportedException}.
     *
     * @param context command context
     * @param parser  parser type
     */
    public SelectorUnsupportedException(
            final @NonNull CommandContext<?> context,
            final @NonNull Class<?> parser
    ) {
        super(
                parser,
                context,
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED
        );
    }
}
