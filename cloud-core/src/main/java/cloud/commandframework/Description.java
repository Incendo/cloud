//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link CommandArgument} description
 */
public final class Description {

    /**
     * Empty command description
     */
    private static final Description EMPTY = Description.of("");

    private final String description;

    private Description(@NonNull final String description) {
        this.description = description;
    }

    /**
     * Get an empty command description
     *
     * @return Command description
     */
    public static @NonNull Description empty() {
        return EMPTY;
    }

    /**
     * Create a command description instance
     *
     * @param string Command description
     * @return Created command description
     */
    public static @NonNull Description of(@NonNull final String string) {
        return new Description(string);
    }

    /**
     * Get the command description
     *
     * @return Command description
     */
    public @NonNull String getDescription() {
       return this.description;
    }

    /**
     * Get the command description
     *
     * @return Command description
     */
    @Override
    public @NonNull String toString() {
        return this.description;
    }

}
