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
package cloud.commandframework.arguments;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.internal.CommandRegistrationHandler;
import java.util.Collections;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class StandardCommandSyntaxFormatterTest {

    @Test
    void accountsForSenderType() {
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.simpleCoordinator(),
                CommandRegistrationHandler.nullCommandRegistrationHandler()
        ) {
            @Override
            public boolean hasPermission(
                    final @NonNull TestCommandSender sender,
                    final @NonNull String permission
            ) {
                return true;
            }
        };

        final Command.Builder<TestCommandSender> root = commandManager.commandBuilder("root");
        commandManager.command(
                root.literal("all")
                        .handler(ctx -> {})
        );
        commandManager.command(
                root.literal("specific_only")
                        .senderType(SpecificTestCommandSender.class)
                        .handler(ctx -> {})
        );

        final StandardCommandSyntaxFormatter<TestCommandSender> formatter =
                new StandardCommandSyntaxFormatter<>(commandManager);

        final CommandNode<TestCommandSender> rootNode = commandManager.commandTree().getNamedNode("root");

        final String specificFormatted = formatter.apply(
                new SpecificTestCommandSender(), Collections.emptyList(), rootNode);
        assertThat(specificFormatted).isEqualTo(" all|specific_only");

        final String formatted = formatter.apply(
                new TestCommandSender(), Collections.emptyList(), rootNode);
        assertThat(formatted).isEqualTo(" all");
    }

    static final class SpecificTestCommandSender extends TestCommandSender {

    }
}
