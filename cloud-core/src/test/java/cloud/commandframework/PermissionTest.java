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
package cloud.commandframework;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PredicatePermission;
import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionTest {

    private final static CommandManager<TestCommandSender> manager = new PermissionOutputtingCommandManager();

    @BeforeAll
    static void setup() {
        manager.command(manager.commandBuilder("test").literal("foo").permission("test.permission.one").build());
        manager.command(manager.commandBuilder("test").literal("bar").permission("test.permission.two").build());
        manager.command(manager.commandBuilder("test").literal("fizz").permission("test.permission.three").build());
        manager.command(manager.commandBuilder("test").literal("buzz").permission("test.permission.four").build());
    }

    @Test
    void testCompoundPermission() {
        assertThat(manager.suggestionFactory().suggestImmediately(new TestCommandSender(), "t")).isEmpty();
        assertThat(manager.suggestionFactory().suggestImmediately(
                new TestCommandSender("test.permission.four"), "t")
        ).isNotEmpty();
    }

    @Test
    void testComplexPermissions() {
        manager.command(manager.commandBuilder("first").permission("first"));
        manager.command(manager.commandBuilder("first").required("second", integerParser()).permission("second"));

        manager.executeCommand(new TestCommandSender(), "first").join();

        assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "first 10").join()
        );
    }

    @Test
    void testAndPermissions() {
        final Permission test = Permission.of("one").and(Permission.of("two"));
        final TestCommandSender sender = new TestCommandSender("one");

        assertThat(manager.hasPermission(sender, test)).isFalse();
        assertThat(manager.hasPermission(new TestCommandSender("two"), test)).isFalse();

        sender.addPermission("two");
        assertThat(manager.hasPermission(sender, test)).isTrue();
    }

    @Test
    void testOrPermissions() {
        final Permission test = Permission.of("one").or(Permission.of("two"));
        assertFalse(manager.hasPermission(new TestCommandSender(), test));
        assertTrue(manager.hasPermission(new TestCommandSender("one"), test));
        assertTrue(manager.hasPermission(new TestCommandSender("two"), test));
        assertTrue(manager.hasPermission(new TestCommandSender("one", "two"), test));
    }

    @Test
    void testComplexOrPermissions() {
        final Permission andOne = Permission.andPermission(Arrays.asList(
                Permission.of("perm.one"),
                Permission.of("perm.two")
        ));
        final Permission andTwo = Permission.andPermission(Arrays.asList(
                Permission.of("perm.three"),
                (PredicatePermission<?>) (s) -> false
        ));
        final Permission orPermission = Permission.orPermission(Arrays.asList(andOne, andTwo));
        assertFalse(manager.hasPermission(new TestCommandSender("does.have", "also.does.have"), orPermission));
        assertFalse(manager.hasPermission(new TestCommandSender("perm.one", "perm.three"), orPermission));
        assertTrue(manager.hasPermission(new TestCommandSender("perm.one", "perm.two"), orPermission));
    }

    @Test
    void testComplexAndPermissions() {
        final Permission orOne = Permission.orPermission(Arrays.asList(
                Permission.of("perm.one"),
                (PredicatePermission<?>) (s) -> false
        ));
        final Permission orTwo = Permission.orPermission(Arrays.asList(
                Permission.of("perm.two"),
                Permission.of("perm.three")
        ));
        final Permission andPermission = Permission.andPermission(Arrays.asList(orOne, orTwo));
        assertFalse(manager.hasPermission(new TestCommandSender("perm.one"), andPermission));
        assertTrue(manager.hasPermission(new TestCommandSender("perm.one", "perm.two"), andPermission));
        assertTrue(manager.hasPermission(new TestCommandSender("perm.one", "perm.three"), andPermission));
    }

    @Test
    void testPredicatePermissions() {
        final AtomicBoolean condition = new AtomicBoolean(true);
        manager.command(manager.commandBuilder("predicate").permission(PredicatePermission.of(
                CloudKey.of("boolean"), $ -> condition.get()
        )));
        // First time should succeed
        manager.executeCommand(new TestCommandSender(), "predicate").join();
        // Now we force it to fail
        condition.set(false);
        assertThrows(
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
                final @NonNull TestCommandSender sender,
                final @NonNull String permission
        ) {
            if (permission.equalsIgnoreCase("first")) {
                return true;
            }
            if (permission.equalsIgnoreCase("second")) {
                return false;
            }
            return sender.hasPermisison(permission);
        }
    }
}
