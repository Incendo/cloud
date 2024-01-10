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

import cloud.commandframework.Command;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandPredicate<C> extends Predicate<Command<C>> {

    /**
     * Returns a new predicate which accepts all commands.
     *
     * @param <C> the command sender type
     * @return the filter
     */
    static <C> @NonNull CommandPredicate<C> acceptAll() {
        return cmd -> true;
    }

    /**
     * Returns {@code true} if the command should be kept and
     * {@code false} if the command should be filtered out.
     *
     * @param command the command to test
     * @return whether to keep the command
     */
    @Override
    boolean test(@NonNull Command<C> command);
}
