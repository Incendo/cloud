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
package cloud.commandframework.brigadier;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Interface implemented by {@link cloud.commandframework.CommandManager command managers} that are capable of registering
 * commands to Brigadier using {@link CloudBrigadierManager}.
 *
 * @param <C> cloud command sender type
 * @param <S> brigadier command source type
 * @since 1.2.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface BrigadierManagerHolder<C, S> {

    /**
     * Returns whether the {@link CloudBrigadierManager} is present and active.
     *
     * @return if the {@link CloudBrigadierManager brigadier manager} is  present and active
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    boolean hasBrigadierManager();

    /**
     * Get the {@link CloudBrigadierManager} used by this {@link cloud.commandframework.CommandManager command manager}.
     *
     * <p>Generally, {@link #hasBrigadierManager()} should be checked before calling this method. However, some command managers
     * will always use Brigadier and in those cases the check can be skipped (this will be in the relevant manager's
     * documentation).</p>
     *
     * @return the {@link CloudBrigadierManager}
     * @throws BrigadierManagerNotPresent when {@link #hasBrigadierManager()} is false
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @NonNull CloudBrigadierManager<C, ? extends S> brigadierManager();

    /**
     * Exception thrown when {@link #brigadierManager()} is called and {@link #hasBrigadierManager()}
     * is {@code false}.
     *
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    final class BrigadierManagerNotPresent extends RuntimeException {

        /**
         * Creates a new {@link BrigadierManagerNotPresent} exception.
         *
         * @param message detail message
         */
        public BrigadierManagerNotPresent(final String message) {
            super(message);
        }
    }
}
