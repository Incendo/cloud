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

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * {@link CommandManager} implementation that allows you to lock command registrations.
 * This should be used when the platform limits command registration to a certain point in time.
 * <p>
 * To lock writes, use {@link #lockWrites()}. To check if writing is allowed, use {@link #isCommandRegistrationAllowed()}.
 * If {@link #isCommandRegistrationAllowed()} is {@code false} then {@link #command(Command)} will throw
 * {@link IllegalStateException}.
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public abstract class LockableCommandManager<C> extends CommandManager<C> {

    private final Object writeLock = new Object();
    private volatile boolean writeLocked = false;

    /**
     * Create a new command manager instance
     *
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link AsynchronousCommandExecutionCoordinator}
     * @param commandRegistrationHandler  Command registration handler. This will get called every time a new command is
     *                                    registered to the command manager. This may be used to forward command registration
     */
    public LockableCommandManager(
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull CommandRegistrationHandler commandRegistrationHandler
    ) {
        super(commandExecutionCoordinator, commandRegistrationHandler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This should only be called when {@link #isCommandRegistrationAllowed()} is {@code false},
     * else {@link IllegalStateException} will be called
     *
     * @param command Command to register
     * @return
     */
    @Override
    public final @NonNull CommandManager<C> command(final @NonNull Command<C> command) {
        synchronized (this.writeLock) {
            if (!isCommandRegistrationAllowed()) {
                throw new IllegalStateException(
                        "Command registration is not allowed. The command manager has been locked."
                );
            }
            return super.command(command);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This should only be called when {@link #isCommandRegistrationAllowed()} is {@code false},
     * else {@link IllegalStateException} will be called
     *
     * @param command Command to register. {@link Command.Builder#build()}} will be invoked.
     * @return
     */
    @Override
    public final @NonNull CommandManager<C> command(final Command.@NonNull Builder<C> command) {
        return super.command(command);
    }

    /**
     * Lock writing. After this, {@link #isCommandRegistrationAllowed()} will return {@code false}
     */
    protected final void lockWrites() {
        synchronized (this.writeLock) {
            this.writeLocked = true;
        }
    }

    /**
     * Check if command registration is allowed
     *
     * @return {@code true} if the registration is allowed, else {@code false}
     */
    public final boolean isCommandRegistrationAllowed() {
        return !this.writeLocked;
    }

}
