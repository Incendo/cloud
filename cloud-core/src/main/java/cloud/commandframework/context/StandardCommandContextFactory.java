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

@SuppressWarnings("deprecation")
public final class StandardCommandContextFactory<C> implements CommandContextFactory<C> {

    @Override
    public @NonNull CommandContext<C> create(
            final boolean suggestions,
            final @NonNull C sender,
            final @NonNull CaptionRegistry<C> captionRegistry
    ) {
        return new CommandContext<>(
                suggestions,
                sender,
                captionRegistry
        );
    }

    @Override
    public @NonNull CommandContext<C> create(
            final boolean suggestions,
            final @NonNull C sender,
            final @NonNull CommandManager<C> commandManager
    ) {
        return new CommandContext<C>(
                suggestions,
                sender,
                commandManager
        );
    }

}
