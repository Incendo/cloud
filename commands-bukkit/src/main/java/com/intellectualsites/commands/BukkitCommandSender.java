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
package com.intellectualsites.commands;

import com.google.common.base.Objects;
import com.intellectualsites.commands.sender.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class BukkitCommandSender implements CommandSender {

    private final org.bukkit.command.CommandSender internalSender;

    BukkitCommandSender(@Nonnull final org.bukkit.command.CommandSender internalSender) {
        this.internalSender = internalSender;
    }

    @Nonnull
    public static BukkitCommandSender player(@Nonnull final Player player) {
        return new BukkitPlayerSender(player);
    }

    @Nonnull
    public static BukkitCommandSender console() {
        return new BukkitConsoleSender();
    }

    @Nonnull
    public static BukkitCommandSender of(@Nonnull final org.bukkit.command.CommandSender sender) {
        if (sender instanceof Player) {
            return player((Player) sender);
        }
        return console();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BukkitCommandSender that = (BukkitCommandSender) o;
        return Objects.equal(internalSender, that.internalSender);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(internalSender);
    }

    @Nonnull
    public org.bukkit.command.CommandSender getInternalSender() {
        return this.internalSender;
    }

    public abstract boolean isPlayer();

    @Nonnull
    public abstract Player asPlayer();

    @Override
    public boolean hasPermission(@Nonnull final String permission) {
        return this.internalSender.hasPermission(permission);
    }
}
