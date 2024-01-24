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
package cloud.commandframework;

import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PermissionResult;
import cloud.commandframework.permission.PredicatePermission;
import cloud.commandframework.suggestion.Suggestion;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.parser.standard.IntegerParser.integerParser;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionTest {

    @Mock(strictness = Mock.Strictness.LENIENT) private Function<String, Boolean> permissionFunction;

    private CommandManager<TestCommandSender> manager;

    @BeforeEach
    void setup() {
        this.manager = new MockPermissionManager(permissionFunction);
        when(this.permissionFunction.apply(anyString())).thenReturn(false);
    }

    @Test
    void testRootCommandPermissionLackingPermission() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("test").literal("foo").permission("test.permission.one").build());
        this.manager.command(this.manager.commandBuilder("test").literal("bar").permission("test.permission.two").build());
        this.manager.command(this.manager.commandBuilder("test").literal("fizz").permission("test.permission.three").build());
        this.manager.command(this.manager.commandBuilder("test").literal("buzz").permission("test.permission.four").build());

        // Act
        final List<? extends Suggestion> suggestions = this.manager.suggestionFactory()
                .suggestImmediately(new TestCommandSender(), "t").list();

        // Assert
        assertThat(suggestions).isEmpty();
    }

    @Test
    void testRootCommandPermission() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("test").literal("foo").permission("test.permission.one").build());
        this.manager.command(this.manager.commandBuilder("test").literal("bar").permission("test.permission.two").build());
        this.manager.command(this.manager.commandBuilder("test").literal("fizz").permission("test.permission.three").build());
        this.manager.command(this.manager.commandBuilder("test").literal("buzz").permission("test.permission.four").build());

        when(this.permissionFunction.apply("test.permission.four")).thenReturn(true);

        // Act
        final List<? extends Suggestion> suggestions = this.manager.suggestionFactory()
                .suggestImmediately(new TestCommandSender(), "t").list();

        // Assert
        assertThat(suggestions).isNotEmpty();
    }

    @Test
    void testSubCommandPermission() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("first").permission("first"));
        this.manager.command(this.manager.commandBuilder("first").required("second", integerParser()).permission("second"));

        when(this.permissionFunction.apply("first")).thenReturn(true);

        // Act
        this.manager.commandExecutor().executeCommand(new TestCommandSender(), "first").join();
        final CompletionException exception = assertThrows(
                CompletionException.class,
                () -> this.manager.commandExecutor().executeCommand(new TestCommandSender(), "first 10").join()
        );

        // Assert
        assertThat(exception).hasCauseThat().isInstanceOf(NoPermissionException.class);
    }

    @Test
    void testAndPermissionsMissingOne() {
        // Arrange
        final Permission test = Permission.of("one").and(Permission.of("two"));

        when(this.permissionFunction.apply("two")).thenReturn(true);

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), test);

        // Assert
        assertThat(result.allowed()).isFalse();
    }


    @Test
    void testAndPermissionsHasAll() {
        // Arrange
        final Permission test = Permission.of("one").and(Permission.of("two"));

        when(this.permissionFunction.apply("one")).thenReturn(true);
        when(this.permissionFunction.apply("two")).thenReturn(true);

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), test);

        // Assert
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void testOrPermissionHasNone() {
        // Arrange
        final Permission test = Permission.of("one").or(Permission.of("two"));

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), test);

        // Assert
        assertThat(result.allowed()).isFalse();
    }

    @Test
    void testOrPermissionHasOne() {
        // Arrange
        final Permission test = Permission.of("one").or(Permission.of("two"));

        when(this.permissionFunction.apply("two")).thenReturn(true);

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), test);

        // Assert
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void testComplexAndPermissionsMissingOne() {
        // Arrange
        final Permission orOne = Permission.anyOf(
                Permission.of("perm.one"),
                new PredicatePermission<TestCommandSender>() {

                    @Override
                    public @NonNull PermissionResult testPermission(final @NonNull TestCommandSender sender) {
                        return PermissionResult.denied(this);
                    }
                }
        );
        final Permission orTwo = Permission.anyOf(
                Permission.of("perm.two"),
                Permission.of("perm.three")
        );

        final Permission andPermission = Permission.allOf(Arrays.asList(orOne, orTwo));

        when(this.permissionFunction.apply("perm.one")).thenReturn(true);

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), andPermission);

        // Assert
        assertThat(result.allowed()).isFalse();
    }

    @Test
    void testComplexAndPermissions() {
        // Arrange
        final Permission orOne = Permission.anyOf(
                Permission.of("perm.one"),
                new PredicatePermission<TestCommandSender>() {

                    @Override
                    public @NonNull PermissionResult testPermission(final @NonNull TestCommandSender sender) {
                        return PermissionResult.denied(this);
                    }
                }
        );
        final Permission orTwo = Permission.anyOf(
                Permission.of("perm.two"),
                Permission.of("perm.three")
        );

        final Permission andPermission = Permission.allOf(orOne, orTwo);

        when(this.permissionFunction.apply("perm.one")).thenReturn(true);
        when(this.permissionFunction.apply("perm.three")).thenReturn(true);

        // Act
        final PermissionResult result = this.manager.testPermission(new TestCommandSender(), andPermission);

        // Assert
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void testPredicatePermissions() {
        final AtomicBoolean condition = new AtomicBoolean(true);
        manager.command(manager.commandBuilder("predicate").permission(PredicatePermission.of(
                CloudKey.of("boolean"), $ -> condition.get()
        )));
        // First time should succeed
        manager.commandExecutor().executeCommand(new TestCommandSender(), "predicate").join();
        // Now we force it to fail
        condition.set(false);
        assertThrows(
                CompletionException.class,
                () -> manager.commandExecutor().executeCommand(new TestCommandSender(), "predicate").join()
        );
    }


    private static final class MockPermissionManager extends CommandManager<TestCommandSender> {

        private final Function<String, Boolean> permissionFunction;

        private MockPermissionManager(final @NonNull Function<String, Boolean> permissionFunction) {
            super(ExecutionCoordinator.simpleCoordinator(), cmd -> true);
            this.permissionFunction = permissionFunction;
        }

        @Override
        public boolean hasPermission(
                final @NonNull TestCommandSender sender,
                final @NonNull String permission
        ) {
            return this.permissionFunction.apply(permission);
        }
    }
}
