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

import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Registry that allows for messages to be configurable per-sender. Delegates to registered {@link CaptionProvider
 * CaptionProviders}.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public interface CaptionRegistry<C> {

    /**
     * Returns the value of the given {@code caption} for the given {@code sender}.
     *
     * @param caption the caption key
     * @param sender  the sender to get the caption for
     * @return the caption value
     */
    @NonNull String caption(@NonNull Caption caption, @NonNull C sender);

    /**
     * Registers the given {@code provider}.
     * <p>
     * When {@link #caption(Caption, Object)} is invoked, all providers will be iterated over (with the
     * last registered provider getting priority) until a provider returns a non-{@code null} value for the caption.
     * <p>
     * You may use {@link CaptionProvider#forCaption(Caption, Function)} to register per-caption providers, or
     * {@link CaptionProvider#constantProvider(Caption, String)} to register constant values.
     *
     * @param provider the provider
     * @return {@code this}
     */
    @This @NonNull CaptionRegistry<C> registerProvider(@NonNull CaptionProvider<C> provider);

    /**
     * Creates a new caption registry with no providers registered.
     *
     * @param <C> command sender type
     * @return new caption registry
     */
    static <C> CaptionRegistry<C> captionRegistry() {
        return new CaptionRegistryImpl<>();
    }
}
