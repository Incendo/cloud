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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Utility that formats chains of {@link CommandArgument command arguments} into syntax strings
 *
 * @param <C> Command sender type
 */
@FunctionalInterface
public interface CommandSyntaxFormatter<C> {

    /**
     * Format the command arguments into a syntax string
     *
     * @param commandArguments Command arguments that have been unambiguously specified up until this point. This
     *                         should include the "current" command, if such a command exists.
     * @param node             The current command node. The children of this node will be appended onto the
     *                         command syntax string, as long as an unambiguous path can be identified. The node
     *                         itself will not be appended onto the syntax string. This can be set to {@code null} if
     *                         no node is relevant at the point of formatting.
     * @return The formatted syntax string
     */
    @NonNull String apply(
            @NonNull List<@NonNull CommandArgument<C, ?>> commandArguments,
            CommandTree.@Nullable Node<@Nullable CommandArgument<C, ?>> node
    );

}
