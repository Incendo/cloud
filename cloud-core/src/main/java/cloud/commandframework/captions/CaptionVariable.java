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
package cloud.commandframework.captions;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Key-value pair used to replace variables in captions
 */
public final class CaptionVariable {

    private final String key;
    private final String value;

    private CaptionVariable(final @NonNull String key, final @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Create a new caption variable instance
     *
     * @param key   Key
     * @param value Replacement
     * @return Created instance
     */
    public static @NonNull CaptionVariable of(final @NonNull String key, final @NonNull String value) {
        return new CaptionVariable(key, value);
    }

    /**
     * Get the variable key
     *
     * @return Key
     */
    public @NonNull String getKey() {
        return this.key;
    }

    /**
     * Get the variable value
     *
     * @return Value
     */
    public @NonNull String getValue() {
        return this.value;
    }

}
