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
package cloud.commandframework.brigadier;

import cloud.commandframework.CommandManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Brigadier {@link Command} implementation that delegates to cloud.
 *
 * @param <C> command sender type
 * @param <S> brigadier command source type
 */
@API(status = API.Status.INTERNAL)
public final class CloudBrigadierCommand<C, S> implements Command<S> {

    private final CommandManager<C> commandManager;
    private final CloudBrigadierManager<C, S> brigadierManager;

    /**
     * Creates a new {@link CloudBrigadierCommand}.
     *
     * @param commandManager   command manager
     * @param brigadierManager brigadier manager
     */
    public CloudBrigadierCommand(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull CloudBrigadierManager<C, S> brigadierManager
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;
    }

    @Override
    public int run(final @NonNull CommandContext<S> ctx) {
        final S source = ctx.getSource();
        final String input = ctx.getInput().substring(ctx.getLastChild().getNodes().get(0).getRange().getStart());
        final C sender = this.brigadierManager.senderMapper().map(source);

        this.commandManager.commandExecutor().executeCommand(sender, input);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
