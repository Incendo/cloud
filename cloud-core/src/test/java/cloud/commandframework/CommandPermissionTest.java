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
package cloud.commandframework;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.permission.PredicatePermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

class CommandPermissionTest {

    private final static CommandManager<TestCommandSender> manager = new PermissionOutputtingCommandManager();
    private static boolean acceptOne = false;

    @BeforeAll
    static void setup() {
        manager.command(manager.commandBuilder("test").literal("foo").permission("test.permission.one").build());
        manager.command(manager.commandBuilder("test").literal("bar").permission("test.permission.two").build());
        manager.command(manager.commandBuilder("test").literal("fizz").permission("test.permission.three").build());
        manager.command(manager.commandBuilder("test").literal("buzz").permission("test.permission.four").build());
    }

    @Test
    void testCompoundPermission() {
        Assertions.assertTrue(manager.suggest(new TestCommandSender(), "t").isEmpty());
        acceptOne = true;
        Assertions.assertFalse(manager.suggest(new TestCommandSender(), "t").isEmpty());
    }

    @Test
    void testComplexPermissions() {
        manager.command(manager.commandBuilder("first").permission("first"));
        manager.command(manager.commandBuilder("first").argument(IntegerArgument.of("second")).permission("second"));
        manager.executeCommand(new TestCommandSender(), "first").join();
        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "first 10").join()
        );
    }

    @Test
    void testPredicatePermissions() {
        final AtomicBoolean condition = new AtomicBoolean(true);
        manager.command(manager.commandBuilder("predicate").permission(PredicatePermission.of(
                SimpleCloudKey.of("boolean"), $ -> condition.get()
        )));
        // First time should succeed
        manager.executeCommand(new TestCommandSender(), "predicate").join();
        // Now we force it to fail
        condition.set(false);
        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "predicate").join()
        );
    }


    private static final class PermissionOutputtingCommandManager extends CommandManager<TestCommandSender> {

        private PermissionOutputtingCommandManager() {
            super(CommandExecutionCoordinator.simpleCoordinator(), cmd -> true);
        }

        @Override
        public boolean hasPermission(
                final TestCommandSender sender,
                final String permission
        ) {
            if (permission.equalsIgnoreCase("first")) {
                return true;
            }
            if (permission.equalsIgnoreCase("second")) {
                return false;
            }
            return acceptOne && permission.equalsIgnoreCase("test.permission.four");
        }

        @Override
        public CommandMeta createDefaultCommandMeta() {
            return SimpleCommandMeta.empty();
        }

    }

}
