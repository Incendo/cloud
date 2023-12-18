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

import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.setting.ManagerSetting;
import cloud.commandframework.state.RegistrationState;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandRegistrationStateTest {

    @Test
    void testInitialState() {
        final CommandManager<TestCommandSender> manager = createManager();
        assertEquals(RegistrationState.BEFORE_REGISTRATION, manager.state());
    }

    @Test
    void testRegistrationChangesState() {
        final CommandManager<TestCommandSender> manager = createManager();

        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));

        assertEquals(RegistrationState.REGISTERING, manager.state());
    }

    @Test
    void testDoubleRegistrationPersistsState() {
        final CommandManager<TestCommandSender> manager = createManager();

        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        manager.command(manager.commandBuilder("test2").handler(ctx -> {
        }));

        assertEquals(RegistrationState.REGISTERING, manager.state());
    }

    @Test
    void testChangingRegistrationHandlerFails() {
        final CommandManager<TestCommandSender> manager = createManager();
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        assertThrows(
                IllegalStateException.class,
                () -> manager.commandRegistrationHandler(CommandRegistrationHandler.nullCommandRegistrationHandler())
        );
    }

    @Test
    void testRegistrationFailsInAfterRegistrationState() {
        final CommandManager<TestCommandSender> manager = createManager();
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));

        manager.transitionOrThrow(
                RegistrationState.REGISTERING,
                RegistrationState.AFTER_REGISTRATION
        );
        assertThrows(IllegalStateException.class, () -> manager.command(manager.commandBuilder("test2").handler(ctx -> {
        })));
    }

    @Test
    void testAllowUnsafeRegistration() {
        final CommandManager<TestCommandSender> manager = createManager();
        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        manager.transitionOrThrow(
                RegistrationState.REGISTERING,
                RegistrationState.AFTER_REGISTRATION
        );
        manager.command(manager.commandBuilder("unsafe").handler(ctx -> {
        }));
    }
}
