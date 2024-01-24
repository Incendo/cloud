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
package cloud.commandframework.annotations.descriptor;

import cloud.commandframework.annotations.SyntaxFragment;
import cloud.commandframework.internal.ImmutableBuilder;
import java.lang.reflect.Method;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

@ImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE)
public interface CommandDescriptor extends Descriptor {

    /**
     * Returns the command method.
     *
     * @return the command method
     */
    @NonNull Method method();

    @Override
    default @NonNull String name() {
        return this.commandToken();
    }

    /**
     * Returns an unmodifiable view of the syntax fragments.
     *
     * @return the syntax fragments
     */
    @NonNull List<@NonNull SyntaxFragment> syntax();

    /**
     * Returns the root command name.
     *
     * @return the name of the root command
     */
    @NonNull String commandToken();

    /**
     * Returns the required sender type.
     *
     * @return the required sender type
     */
    @NonNull Class<?> requiredSender();
}
