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

import com.google.common.base.Objects;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Command sender that proxies {@link org.bukkit.command.CommandSender}
 */
public abstract class BukkitCommandSender {

    private final org.bukkit.command.CommandSender internalSender;

    /**
     * Create a new command sender from a Bukkit {@link org.bukkit.command.CommandSender}
     *
     * @param internalSender Bukkit command sender
     */
    protected BukkitCommandSender(final org.bukkit.command.@NonNull CommandSender internalSender) {
        this.internalSender = internalSender;
    }

    /**
     * Construct a new {@link BukkitCommandSender} for a {@link Player}
     *
     * @param player Player instance
     * @return Constructed command sender
     */
    public static @NonNull BukkitCommandSender player(final @NonNull Player player) {
        return new BukkitPlayerSender(player);
    }

    /**
     * Construct a new {@link BukkitCommandSender} for the Bukkit console
     *
     * @return Constructed command sender
     */
    public static @NonNull BukkitCommandSender console() {
        return new BukkitConsoleSender();
    }

    /**
     * Construct a new {@link BukkitCommandSender} from a Bukkit {@link org.bukkit.command.CommandSender}
     *
     * @param sender Bukkit command sender
     * @return Constructed command sender
     */
    public static @NonNull BukkitCommandSender of(final org.bukkit.command.@NonNull CommandSender sender) {
        if (sender instanceof Player) {
            return player((Player) sender);
        }
        return console();
    }

    @Override
    public final boolean equals(final Object o) {
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
    public final int hashCode() {
        return Objects.hashCode(internalSender);
    }

    /**
     * Get the proxied {@link org.bukkit.command.CommandSender}
     *
     * @return Proxied command sneder
     */
    public org.bukkit.command.@NonNull CommandSender getInternalSender() {
        return this.internalSender;
    }

    /**
     * Check if this sender represents a player
     *
     * @return {@code true} if this sender represents a player, {@code false} if not
     */
    public abstract boolean isPlayer();

    /**
     * Get this sender as a player. This can only safely be done if {@link #isPlayer()}}
     * returns {@code true}
     *
     * @return Player object
     */
    public abstract @NonNull Player asPlayer();

    /**
     * Send a message to the command sender
     *
     * @param message Message to send
     */
    public void sendMessage(final @NonNull String message) {
        this.internalSender.sendMessage(message);
    }

}
