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

import java.util.Objects;

/**
 * This is a reference to a caption and does not contain any message itself
 */
public final class Caption {

    private final String key;

    private Caption(final @NonNull String key) {
        this.key = key;
    }

    /**
     * Create a new caption with a given key
     *
     * @param key Caption key
     * @return Created caption
     */
    public static @NonNull Caption of(final @NonNull String key) {
        return new Caption(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Caption caption = (Caption) o;
        return Objects.equals(key, caption.key);
    }

    /**
     * Get the caption key
     *
     * @return Caption key
     */
    public @NonNull String getKey() {
        return this.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return String.format(
                "Caption{key='%s'}",
                this.key
        );
    }

}
