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
package cloud.commandframework.brigadier;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This interface is implemented by command managers capable of registering commands to Brigadier.
 *
 * @param <C> Command sender type
 * @since 1.2.0
 */
public interface BrigadierManagerHolder<C> {

    /**
     * Get the Brigadier manager instance used by this manager. This method being present
     * in a command manager means the manager has the capability to register commands
     * to Brigadier, but does not necessarily mean that this capability is being used.
     * <p>
     * In the case that Brigadier isn't used, this method should always return {@code null}.
     *
     * @return The Brigadier manager instance, if commands are being registered to Brigadier.
     *         Else, {@code null}
     * @since 1.2.0
     */
    @Nullable CloudBrigadierManager<C, ?> brigadierManager();

}
