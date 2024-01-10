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
package cloud.commandframework.examples.bukkit.annotations;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.processing.CommandContainer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Example of a command container.
 *
 * @see ExampleSuggestionContainer
 */
@CommandContainer
public final class ExampleCommandContainer {

    /**
     * The constructor. {@link AnnotationParser} is an optional parameter.
     *
     * @param parser the parser
     */
    public ExampleCommandContainer(final @NonNull AnnotationParser<CommandSender> parser) {
        // Woo...
    }

    /**
     * This one gets parsed automatically!
     *
     * @param sender the sender
     * @param arg    a string
     */
    @CommandMethod("annotations container [arg]")
    public void containerCommand(
            final CommandSender sender,
            @Argument(suggestions = "container-suggestions") final @Nullable String arg
    ) {
        sender.sendMessage("This is sent from a container!!");
        if (arg != null) {
            sender.sendMessage("You said: " + arg);
        }
    }
}
