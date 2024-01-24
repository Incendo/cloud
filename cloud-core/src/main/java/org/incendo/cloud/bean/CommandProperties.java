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
package org.incendo.cloud.bean;

import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.internal.ImmutableImpl;

/**
 * Defines properties used by {@link CommandBean command beans} to construct commands.
 *
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface CommandProperties {

    /**
     * Construct a new instance
     *
     * @param name    the command name
     * @param aliases the command aliases
     * @return the created instance
     */
    static @NonNull CommandProperties of(final @NonNull String name, final @NonNull String @NonNull... aliases) {
        return CommandPropertiesImpl.of(name, Arrays.asList(aliases));
    }

    /**
     * Construct a new instance
     *
     * @param name    the command name
     * @param aliases the command aliases
     * @return the created instance
     */
    static @NonNull CommandProperties commandProperties(
            final @NonNull String name,
            final @NonNull String @NonNull... aliases
    ) {
        return of(name, aliases);
    }

    /**
     * Returns the command name.
     *
     * @return the name
     */
    @NonNull String name();

    /**
     * Returns an unmodifiable view of the command aliases.
     *
     * @return the command aliases
     */
    @NonNull Collection<@NonNull String> aliases();
}
