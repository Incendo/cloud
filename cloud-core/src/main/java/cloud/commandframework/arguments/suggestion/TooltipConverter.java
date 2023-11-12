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
package cloud.commandframework.arguments.suggestion;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Converter between a platform type {@link T} and an output type {@link U}
 *
 * @param <T> the input type
 * @param <U> the output type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public abstract class TooltipConverter<T, U> {

    private final Class<T> inputType;

    protected TooltipConverter(final @NonNull Class<T> inputType) {
        this.inputType = inputType;
    }

    /**
     * Returns whether this converter can convert the given {@code object}
     *
     * @param object the object
     * @return {@code true} if this converter can convert the {@code object}, else {@code false}
     */
    public boolean canConvert(final @NonNull Object object) {
        return this.inputType.isInstance(object);
    }

    /**
     * Converts the given {@code tooltip} into the platform type
     *
     * @param tooltip the tooltip
     * @return the converted tooltip
     */
    public abstract @NonNull U convert(@NonNull T tooltip);
}
