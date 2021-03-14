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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link CommandArgument} description
 *
 * @param <C> Command sender type
 * @deprecated to become package-private since 1.4.0. Use {@link ArgumentDescription} instead.
 */
@Deprecated
public final class Description<C> implements ArgumentDescription<C> {

    private final String description;

    Description(final @NonNull String description) {
        this.description = description;
    }

    /**
     * Get an empty command description
     *
     * @param <C> Command sender type
     * @return Command description
     * @deprecated for removal since 1.4.0. See {@link ArgumentDescription#empty()}
     */
    @Deprecated
    public static <C> @NonNull Description<C> empty() {
        return new Description<>("");
    }

    /**
     * Create a command description instance
     *
     * @param <C> Command sender type
     * @param string Command description
     * @return Created command description
     * @deprecated for removal since 1.4.0. See {@link ArgumentDescription#of(String)}
     */
    @Deprecated
    public static <C> @NonNull Description<C> of(final @NonNull String string) {
        return new Description<>(string);
    }

    /**
     * Get the command description
     *
     * @param sender Command sender
     * @return Command description
     */
    @Override
    public @NonNull String getDescription(final C sender) {
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
