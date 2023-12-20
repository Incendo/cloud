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
package cloud.commandframework.state;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A simple state machine.
 *
 * @param <S> the state type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface Stateful<S extends State> {

    /**
     * Returns the current state.
     *
     * @return the current state
     */
    @NonNull S state();

    /**
     * Transitions from the {@code in} state to the {@code out} state, unless the current {@link #state()}
     * is identical to the {@code out} state.
     *
     * @param in  the starting state
     * @param out the ending state
     * @return {@code true} if the state transition was successful, or the current state is identical to the {@code out} state
     */
    boolean transitionIfPossible(@NonNull S in, @NonNull S out);

    /**
     * Requires that the current {@link #state()} is equal to the {@code expected} state, and fails
     * exceptionally if the states are different.
     *
     * @param expected the expected state
     * @throws IllegalStateException if the current {@link #state()} is different from the {@code expected} state
     */
    default void requireState(final @NonNull S expected) {
        if (this.state().equals(expected)) {
            return;
        }
        throw new IllegalStateException(String.format(
                "This operation requires the command manager to be in state '%s', but it is in '%s'",
                expected,
                this.state()
        ));
    }

    /**
     * Transitions from the {@code in} state to the {@code out} state, unless the current {@link #state()}
     * is identical to the {@code out} state.
     *
     * @param in  the starting state
     * @param out the ending state
     * @throws IllegalStateException if state transition is not possible
     */
    default void transitionOrThrow(final @NonNull S in, final @NonNull S out) {
        if (this.transitionIfPossible(in, out)) {
            return;
        }
        throw new IllegalStateException(String.format(
                "The current state is '%s' but we expected a state of '%s' or '%s'",
                this.state(),
                in,
                out
        ));
    }
}
