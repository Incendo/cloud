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
package cloud.commandframework.bukkit.util;

import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.StandardCommandContextFactory;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class ServerTest {

    @Mock
    private Server server;

    @Mock
    private CommandSender commandSender;

    @Mock
    private BukkitCommandManager<CommandSender> commandManager;

    private CommandContext<CommandSender> commandContext;

    @BeforeEach
    void setupServer() throws Exception {
        final Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, this.server);
    }

    @BeforeEach
    void setupCommandContext() {
        this.commandContext = new StandardCommandContextFactory<CommandSender>(this.commandManager).create(
                false /* suggestions */,
                this.commandSender
        );
        this.commandContext.set(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER, this.commandSender);
    }

    /**
     * Returns the mocked server
     *
     * @return mocked server
     */
    protected @NonNull Server server() {
        return this.server;
    }

    /**
     * Returns a command context that can be used in tests
     *
     * @return the command context
     */
    protected @NonNull CommandContext<CommandSender> commandContext() {
        return this.commandContext;
    }
}
