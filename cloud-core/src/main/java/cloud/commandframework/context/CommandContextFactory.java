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
package cloud.commandframework.context;

import cloud.commandframework.CommandManager;
import cloud.commandframework.captions.CaptionRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Factory for {@link CommandContext} instances
 *
 * @param <C> Command sender
 */
public interface CommandContextFactory<C> {

    /**
     * Create a new command context
     *
     * @param suggestions     Whether or not the sender is requesting suggestions
     * @param sender          Command sender
     * @param captionRegistry Caption registry
     * @return Command context
     * @deprecated Provide a command manager instead of a caption registry
     */
    @Deprecated
    @NonNull CommandContext<C> create(
            boolean suggestions,
            @NonNull C sender,
            @NonNull CaptionRegistry<C> captionRegistry
    );

    /**
     * Create a new command context
     *
     * @param suggestions    Whether or not the sender is requesting suggestions
     * @param sender         Command sender
     * @param commandManager Command manager
     * @return Command context
     * @since 1.3.0
     */
    @NonNull CommandContext<C> create(
            boolean suggestions,
            @NonNull C sender,
            @NonNull CommandManager<C> commandManager
    );

}
