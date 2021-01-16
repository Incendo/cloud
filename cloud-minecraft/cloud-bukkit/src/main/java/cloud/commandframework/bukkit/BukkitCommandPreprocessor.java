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
package cloud.commandframework.bukkit;

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Command preprocessor which decorates incoming {@link cloud.commandframework.context.CommandContext}
 * with Bukkit specific objects
 *
 * @param <C> Command sender type
 */
final class BukkitCommandPreprocessor<C> implements CommandPreprocessor<C> {

    private final BukkitCommandManager<C> mgr;

    /**
     * The Bukkit Command Preprocessor for storing Bukkit-specific contexts in the command contexts
     *
     * @param mgr The BukkitCommandManager
     */
    BukkitCommandPreprocessor(final @NonNull BukkitCommandManager<C> mgr) {
        this.mgr = mgr;
    }

    /**
     * Stores the sender mapped to {@link org.bukkit.command.CommandSender} in the context with the key "BukkitCommandSender",
     * and a {@link java.util.Set} of {@link CloudBukkitCapabilities} with the key "CloudBukkitCapabilities"
     */
    @Override
    public void accept(final @NonNull CommandPreprocessingContext<C> context) {
        context.getCommandContext().store("BukkitCommandSender", mgr.getBackwardsCommandSenderMapper().apply(
                context.getCommandContext().getSender()));
        context.getCommandContext().store("CloudBukkitCapabilities", mgr.queryCapabilities());
    }

}
