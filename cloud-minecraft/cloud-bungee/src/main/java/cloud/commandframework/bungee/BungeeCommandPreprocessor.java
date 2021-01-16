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
package cloud.commandframework.bungee;

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Command preprocessor which decorates incoming {@link cloud.commandframework.context.CommandContext}
 * with Bungee specific objects
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
final class BungeeCommandPreprocessor<C> implements CommandPreprocessor<C> {

    private final BungeeCommandManager<C> mgr;

    /**
     * The Bungee Command Preprocessor for storing Bungee-specific contexts in the command contexts
     *
     * @param mgr The BungeeCommandManager
     */
    BungeeCommandPreprocessor(final @NonNull BungeeCommandManager<C> mgr) {
        this.mgr = mgr;
    }

    @Override
    public void accept(final @NonNull CommandPreprocessingContext<C> context) {
        context.getCommandContext().store(BungeeContextKeys.PROXY_SERVER_KEY, mgr.getOwningPlugin().getProxy());
    }

}
