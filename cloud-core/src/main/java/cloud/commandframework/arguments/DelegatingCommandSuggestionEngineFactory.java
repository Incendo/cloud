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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Factory that produces {@link DelegatingCommandSuggestionEngine} instances
 *
 * @param <C> Command sender type
 */
public final class DelegatingCommandSuggestionEngineFactory<C> {

    private final CommandManager<C> commandManager;
    private final CommandTree<C> commandTree;

    /**
     * Create a new delegating command suggestion engine factory
     *
     * @param commandManager Command manager
     */
    public DelegatingCommandSuggestionEngineFactory(final @NonNull CommandManager<C> commandManager) {
        this.commandManager = commandManager;
        this.commandTree = commandManager.getCommandTree();
    }

    /**
     * Create the engine instance
     *
     * @return New engine instance
     */
    public @NonNull DelegatingCommandSuggestionEngine<C> create() {
        return new DelegatingCommandSuggestionEngine<>(
                this.commandManager,
                this.commandTree
        );
    }

}
