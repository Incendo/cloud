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

import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * Key-value pair used to replace variables in captions.
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface CaptionVariable {

    /**
     * Creates a new caption variable instance.
     *
     * @param key   the key
     * @param value the value that replaces the placeholder
     * @return the variable instance
     */
    static @NonNull CaptionVariable of(final @NonNull String key, final @NonNull String value) {
        return CaptionVariableImpl.of(key, value);
    }

    /**
     * Returns the variable key.
     *
     * @return the key
     */
    @NonNull String key();

    /**
     * Returns the variable value
     *
     * @return the value
     */
    @NonNull String value();
}
