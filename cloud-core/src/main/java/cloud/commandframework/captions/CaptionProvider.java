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

import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface CaptionProvider<C> {

    /**
     * Returns a builder for a {@link ConstantCaptionProvider}.
     *
     * @param <C> the command sender type
     * @return the builder
     */
    static <C> ImmutableConstantCaptionProvider.@NonNull Builder<C> constantProvider() {
        return ImmutableConstantCaptionProvider.builder();
    }

    /**
     * Returns a {@link ConstantCaptionProvider} that always returns {@code value} for the given {@code caption}.
     *
     * @param <C>     the command sender type
     * @param caption the caption key
     * @param value   the value
     * @return the provider
     */
    static <C> @NonNull CaptionProvider<C> constantProvider(final @NonNull Caption caption, final @NonNull String value) {
        return CaptionProvider.<C>constantProvider().putCaptions(caption, value).build();
    }

    /**
     * Returns a {@link CaptionProvider} that only returns values for the given {@code caption}, and returns
     * {@code null} for all other captions.
     *
     * @param <C>      the command sender type
     * @param caption  the caption key
     * @param provider the provider of the value
     * @return the provider
     */
    static <C> @NonNull CaptionProvider<C> forCaption(
            final @NonNull Caption caption,
            final @NonNull Function<@NonNull C, @Nullable String> provider
    ) {
        return (key, recipient) -> {
            if (key.equals(caption)) {
                return provider.apply(recipient);
            }
            return null;
        };
    }

    /**
     * Returns the value of the given {@code caption} for the given {@code recipient}, if it recognized by this provider.
     *
     * @param caption   the caption key
     * @param recipient the recipient
     * @return the caption, or {@code null}
     */
    @Nullable String provide(@NonNull Caption caption, @NonNull C recipient);
}
