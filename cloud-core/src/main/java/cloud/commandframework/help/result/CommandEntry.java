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
package cloud.commandframework.help.result;

import cloud.commandframework.Command;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
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
        return new CommandEntryImpl<>(command, syntax);
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


    final class CommandEntryImpl<C> implements CommandEntry<C> {

        private final Command<C> command;
        private final String syntax;

        private CommandEntryImpl(
                final @NonNull Command<C> command,
                final @NonNull String syntax
        ) {
            this.command = command;
            this.syntax = syntax;
        }

        @Override
        public @NonNull Command<C> command() {
            return this.command;
        }

        @Override
        public @NonNull String syntax() {
            return this.syntax;
        }

        @Override
        public int compareTo(final CommandEntry<C> other) {
            return this.syntax().compareTo(other.syntax());
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            final CommandEntryImpl<?> that = (CommandEntryImpl<?>) object;
            return Objects.equals(this.command, that.command)
                    && Objects.equals(this.syntax, that.syntax);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.command, this.syntax);
        }
    }
}
