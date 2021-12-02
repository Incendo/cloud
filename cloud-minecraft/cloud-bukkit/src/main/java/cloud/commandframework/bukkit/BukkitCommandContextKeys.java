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

import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import io.leangen.geantyref.TypeToken;
import java.util.Set;
import org.bukkit.command.CommandSender;

/**
 * Bukkit related {@link cloud.commandframework.context.CommandContext} keys.
 *
 * @since 1.5.0
 */
public final class BukkitCommandContextKeys {

    /**
     * Key used to store the Bukkit native {@link CommandSender} in the {@link cloud.commandframework.context.CommandContext}.
     *
     * @since 1.5.0
     */
    public static final CloudKey<CommandSender> BUKKIT_COMMAND_SENDER = SimpleCloudKey.of(
            "BukkitCommandSender",
            TypeToken.get(CommandSender.class)
    );

    /**
     * Key used to store the active {@link CloudBukkitCapabilities} in the {@link cloud.commandframework.context.CommandContext}.
     *
     * @since 1.5.0
     */
    public static final CloudKey<Set<CloudBukkitCapabilities>> CLOUD_BUKKIT_CAPABILITIES = SimpleCloudKey.of(
            "CloudBukkitCapabilities",
            new TypeToken<Set<CloudBukkitCapabilities>>() {
            }
    );

    private BukkitCommandContextKeys() {
    }

}
