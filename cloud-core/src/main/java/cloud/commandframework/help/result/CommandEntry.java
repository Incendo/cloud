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
package cloud.commandframework.help.result;

import cloud.commandframework.Command;
import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface CommandEntry<C> extends Comparable<CommandEntry<C>> {

    /**
     * Creates a new command entry.
     *
     * @param <C>     the command sender type
     * @param command the command
     * @param syntax  the command syntax
     * @return the created entry
     */
    static <C> @NonNull CommandEntry<C> of(
            final @NonNull Command<C> command,
            final @NonNull String syntax
    ) {
        return CommandEntryImpl.of(command, syntax);
    }

    /**
     * Returns the command.
     *
     * @return the command
     */
    @NonNull Command<C> command();

    /**
     * Returns the command syntax.
     *
     * @return the command syntax
     */
    @NonNull String syntax();

    @Override
    default int compareTo(final @NonNull CommandEntry<C> other) {
        return this.syntax().compareTo(other.syntax());
    }
}
