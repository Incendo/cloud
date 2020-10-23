//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.javacord;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

final class JavacordRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, JavacordCommand<C>> registeredCommands = new HashMap<>();

    private JavacordCommandManager<C> javacordCommandManager;

    JavacordRegistrationHandler() {
    }

    void initialize(final @NonNull JavacordCommandManager<C> javacordCommandManager) {
        this.javacordCommandManager = javacordCommandManager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<?> command) {
        /* We only care about the root command argument */
        final CommandArgument<?, ?> commandArgument = command.getArguments().get(0);
        if (this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }
        @SuppressWarnings("unchecked") final JavacordCommand<C> javacordCommand = new JavacordCommand<>(
                (CommandArgument<C, ?>) commandArgument,
                this.javacordCommandManager
        );
        this.registeredCommands.put(commandArgument, javacordCommand);
        this.javacordCommandManager.getDiscordApi().addMessageCreateListener(javacordCommand);
        return true;
    }

}

