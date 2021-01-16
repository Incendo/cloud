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

package cloud.commandframework;

import cloud.commandframework.internal.CommandRegistrationHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandRegistrationStateTest {

    @Test
    void testInitialState() {
        final TestCommandManager manager = new TestCommandManager();
        assertEquals(CommandManager.RegistrationState.BEFORE_REGISTRATION, manager.getRegistrationState());
    }

    @Test
    void testRegistrationChangesState() {
        final TestCommandManager manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        assertEquals(CommandManager.RegistrationState.REGISTERING, manager.getRegistrationState());
        // And a second registration maintains it
        manager.command(manager.commandBuilder("test2").handler(ctx -> {
        }));
        assertEquals(CommandManager.RegistrationState.REGISTERING, manager.getRegistrationState());
    }

    @Test
    void testChangingRegistrationHandlerFails() {
        final TestCommandManager manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        assertThrows(
                IllegalStateException.class,
                () -> manager.setCommandRegistrationHandler(CommandRegistrationHandler.nullCommandRegistrationHandler())
        );
    }

    @Test
    void testRegistrationFailsInAfterRegistrationState() {
        final TestCommandManager manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));

        manager.transitionOrThrow(
                CommandManager.RegistrationState.REGISTERING,
                CommandManager.RegistrationState.AFTER_REGISTRATION
        );
        assertThrows(IllegalStateException.class, () -> manager.command(manager.commandBuilder("test2").handler(ctx -> {
        })));
    }

    @Test
    void testAllowUnsafeRegistration() {
        final TestCommandManager manager = new TestCommandManager();
        manager.setSetting(CommandManager.ManagerSettings.ALLOW_UNSAFE_REGISTRATION, true);
        manager.command(manager.commandBuilder("test").handler(ctx -> {
        }));
        manager.transitionOrThrow(
                CommandManager.RegistrationState.REGISTERING,
                CommandManager.RegistrationState.AFTER_REGISTRATION
        );
        manager.command(manager.commandBuilder("unsafe").handler(ctx -> {
        }));
    }

}
