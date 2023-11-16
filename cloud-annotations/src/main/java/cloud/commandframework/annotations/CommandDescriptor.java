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
package cloud.commandframework.annotations;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public final class CommandDescriptor implements Descriptor {

    private final Method method;
    private final List<@NonNull SyntaxFragment> syntax;
    private final String commandToken;
    private final Class<?> requiredSender;

    /**
     * Creates a new command descriptor.
     *
     * @param method         the command method
     * @param syntax         the command syntax
     * @param commandToken   the root command name
     * @param requiredSender the required sender type
     */
    public CommandDescriptor(
            final @NonNull Method method,
            final @NonNull List<@NonNull SyntaxFragment> syntax,
            final @NonNull String commandToken,
            final @NonNull Class<?> requiredSender
    ) {
        this.method = method;
        this.syntax = syntax;
        this.commandToken = commandToken;
        this.requiredSender = requiredSender;
    }

    /**
     * Returns the command method.
     *
     * @return the command method
     */
    public @NonNull Method method() {
        return this.method;
    }

    @Override
    public @NonNull String name() {
        return this.commandToken;
    }

    /**
     * Returns an unmodifiable view of the syntax fragments.
     *
     * @return the syntax fragments
     */
    public @NonNull List<@NonNull SyntaxFragment> syntax() {
        return Collections.unmodifiableList(this.syntax);
    }

    /**
     * Returns the root command name.
     *
     * @return the name of the root command
     */
    public @NonNull String commandToken() {
        return this.commandToken;
    }

    /**
     * Returns the required sender type.
     *
     * @return the required sender type
     */
    public @NonNull Class<?> requiredSender() {
        return this.requiredSender;
    }
}
