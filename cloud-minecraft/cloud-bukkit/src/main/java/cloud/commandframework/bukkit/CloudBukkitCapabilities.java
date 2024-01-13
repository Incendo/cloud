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
package cloud.commandframework.bukkit;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Capabilities for the Bukkit module
 */
public enum CloudBukkitCapabilities implements CloudCapability {
    /**
     * Whether Brigadier is present in the current environment. Certain parser types require this, and are able to work
     * even if a capability for Brigadier command registration is not present (as long as this capability is present).
     */
    BRIGADIER(CraftBukkitReflection.classExists("com.mojang.brigadier.tree.CommandNode")
            && CraftBukkitReflection.findOBCClass("command.BukkitCommandWrapper") != null),

    /**
     * Whether support for native Brigadier command registration is available
     * through the Paper API ({@code PaperCommandManager#registerBrigadier} from {@code cloud-paper}).
     */
    NATIVE_BRIGADIER(CraftBukkitReflection.classExists(
            "com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")),

    /**
     * Whether support for Brigadier command registration is available through Commodore
     * ({@link BukkitCommandManager#registerBrigadier}).
     *
     * <p><b>Note:</b> As of 1.19.2, Commodore simply delegates to the same Paper API as cloud is capable of using directly,
     * doing nothing on non-Paper Bukkit implementations. As such, this capability will not be present in 1.19.2+ environments.
     * Users should prefer using {@code PaperCommandManager} from {@code cloud-paper} and checking for
     * {@link #NATIVE_BRIGADIER}.</p>
     */
    COMMODORE_BRIGADIER(BRIGADIER.capable()
            && !NATIVE_BRIGADIER.capable()
            && !CraftBukkitReflection.classExists("org.bukkit.entity.Warden")),

    /**
     * Whether asynchronous command completions are supported through the Paper API.
     */
    ASYNCHRONOUS_COMPLETION(CraftBukkitReflection.classExists(
            "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent"));

    static final Set<CloudBukkitCapabilities> CAPABLE = Arrays.stream(values())
            .filter(CloudBukkitCapabilities::capable)
            .collect(Collectors.toSet());

    private final boolean capable;

    CloudBukkitCapabilities(final boolean capable) {
        this.capable = capable;
    }

    boolean capable() {
        return this.capable;
    }

    @Override
    public @NonNull String toString() {
        return name();
    }
}
