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
package org.incendo.cloud.bean;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandFactory;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.meta.CommandMeta;

/**
 * An alternative way of registering commands, where each command lives in a separate class.
 * <p>
 * The command bean is a {@link CommandExecutionHandler} and will be set as the handler of the command by
 * default. The {@link #execute(CommandContext)} or {@link #executeFuture(CommandContext)} methods can be overridden
 * to implement your command handler. You may also provide custom handler when {@link #configure(Command.Builder)} is invoked.
 * <p>
 * Information about the command, such as aliases, should be provided by {@link #properties()}. The command bean may be
 * registered to the command manager by using {@link CommandManager#command(CommandFactory)}. This will invoke
 * {@link #configure(Command.Builder)}
 * where you may configure the command. The command meta may be configured by overriding {@link #meta()}.
 *
 * @param <C> the command sender type
 */
@API(status = API.Status.STABLE)
public abstract class CommandBean<C> implements CommandExecutionHandler<C>, CommandFactory<C> {

    protected CommandBean() {
    }

    /**
     * Constructs a command using the given {@code commandManager}.
     * <p>
     * This invokes {@link #configure(Command.Builder)} which allows for configuration of the command.
     *
     * @param commandManager the command manager
     * @return the constructed command
     */
    @Override
    public @NonNull List<@NonNull Command<? extends C>> createCommands(final @NonNull CommandManager<C> commandManager) {
        final Command.Builder<C> builder = commandManager.commandBuilder(
                this.properties().name(),
                this.properties().aliases(),
                this.meta()
        ).handler(this);
        return Collections.singletonList(this.configure(builder).build());
    }

    /**
     * Returns the command meta for the constructed command.
     *
     * @return the command meta
     */
    protected @NonNull CommandMeta meta() {
        return CommandMeta.builder().build();
    }

    /**
     * Returns the properties of the command.
     *
     * @return the command properties
     */
    protected abstract @NonNull CommandProperties properties();

    /**
     * Configures the command before it's registered to the command manager.
     * <p>
     * The command builder has been created by using the {@link #properties()} and {@link #meta()} of this bean.
     * The builder has been pre-configured to use {@code this} instance as the {@link CommandExecutionHandler}.
     *
     * @param builder the command builder
     * @return the updated builder
     */
    protected abstract Command.@NonNull Builder<? extends C> configure(Command.@NonNull Builder<C> builder);

    /**
     * Default command handler for this command bean. Does nothing unless override.
     *
     * @param commandContext Command context
     */
    @Override
    public void execute(final @NonNull CommandContext<C> commandContext) {
    }
}
