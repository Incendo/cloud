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
package cloud.commandframework;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines properties used by {@link CommandBean command beans} to construct commands.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class CommandProperties {

    private final String name;
    private final Collection<@NonNull String> aliases;

    /**
     * Construct a new instance
     *
     * @param name    the command name
     * @param aliases the command aliases
     * @return the created instance
     */
    public static @NonNull CommandProperties of(final @NonNull String name, final @NonNull String @NonNull... aliases) {
        return new CommandProperties(name, Arrays.asList(aliases));
    }

    /**
     * Construct a new instance
     *
     * @param name    the command name
     * @param aliases the command aliases
     * @return the created instance
     */
    public static @NonNull CommandProperties commandProperties(
            final @NonNull String name,
            final @NonNull String @NonNull... aliases
    ) {
        return of(name, aliases);
    }

    /**
     * Construct a new instance
     *
     * @param name    the command name
     * @param aliases the command aliases
     */
    protected CommandProperties(final @NonNull String name, final @NonNull Collection<@NonNull String> aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    /**
     * Returns the command name
     *
     * @return the name
     */
    public final @NonNull String name() {
        return this.name;
    }

    /**
     * Returns an unmodifiable view of the command aliases
     *
     * @return the command aliases
     */
    public final @NonNull Collection<@NonNull String> aliases() {
        return Collections.unmodifiableCollection(this.aliases);
    }

    @Override
    @SuppressWarnings("UndefinedEquals")
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandProperties that = (CommandProperties) o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.aliases, that.aliases);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.name, this.aliases);
    }
}
