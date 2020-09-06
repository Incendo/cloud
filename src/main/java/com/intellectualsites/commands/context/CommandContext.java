//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.context;

import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandContext<C extends CommandSender> {

    private final Map<String, Object> internalStorage = new HashMap<>();
    private final C commandSender;

    public CommandContext(@Nonnull final C commandSender) {
        this.commandSender = commandSender;
    }

    /**
     * Get the sender that executed the command
     *
     * @return Command sender
     */
    @Nonnull
    public C getCommandSender() {
        return this.commandSender;
    }

    /**
     * Store a value in the context map
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     */
    public <T> void store(@Nonnull final String key, @Nonnull final T value) {
        this.internalStorage.put(key, value);
    }

    /**
     * Get a value from its key
     *
     * @param key Key
     * @param <T> Value type
     * @return Value
     */
    public <T> Optional<T> get(@Nonnull final String key) {
        final Object value = this.internalStorage.get(key);
        if (value != null) {
            @SuppressWarnings("ALL") final T castedValue = (T) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

}
