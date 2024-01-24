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
package cloud.commandframework.help;

import cloud.commandframework.CommandManager;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public interface HelpHandlerFactory<C> {

    /**
     * Returns a factory that produces instances of {@link StandardHelpHandler}.
     *
     * @param <C>            the command sender type
     * @param commandManager the command manager
     * @return the factory instance
     */
    static <C> @NonNull HelpHandlerFactory<C> standard(
            final @NonNull CommandManager<C> commandManager
    ) {
        return new StandardHelpHandlerFactory<>(commandManager);
    }

    /**
     * Creates a new help handler.
     *
     * @param filter filter that determines which commands are applicable to the help handler
     * @return the help handler
     */
    @NonNull HelpHandler<C> createHelpHandler(@NonNull CommandPredicate<C> filter);
}
