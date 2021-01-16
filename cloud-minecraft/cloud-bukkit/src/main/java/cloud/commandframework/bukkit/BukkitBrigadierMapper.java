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

import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.location.Location2D;
import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Class which handles mapping argument types to their NMS Brigadier counterpart on Bukkit platforms.
 *
 * @param <C> Command sender type
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class BukkitBrigadierMapper<C> {

    private static final int UUID_ARGUMENT_VERSION = 16;

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager brigadierManager;
    private final String nmsVersion;


    /**
     * Class that handles mapping argument types to Brigadier for Bukkit (Commodore) and Paper.
     *
     * @param commandManager   The {@link BukkitCommandManager} to use for mapping
     * @param brigadierManager The {@link CloudBrigadierManager} to use for mapping
     */
    public BukkitBrigadierMapper(
            final @NonNull BukkitCommandManager<C> commandManager,
            final @NonNull CloudBrigadierManager brigadierManager
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;

        /* Detect Minecraft Version Metadata */
        final String version = Bukkit.getServer().getClass().getPackage().getName();
        this.nmsVersion = version.substring(version.lastIndexOf(".") + 1);
        final int majorMinecraftVersion = Integer.parseInt(nmsVersion.split("_")[1]);

        try {
            /* UUID nms argument is a 1.16+ feature */
            if (majorMinecraftVersion >= UUID_ARGUMENT_VERSION) {
                /* Map UUID */
                this.mapSimpleNMS(UUID.class, this.getNMSArgument("UUID").getConstructor());
            }
            /* Map Enchantment */
            this.mapSimpleNMS(Enchantment.class, this.getNMSArgument("Enchantment").getConstructor());
            /* Map Entity Selectors */
            this.mapComplexNMS(SingleEntitySelector.class, this.getEntitySelectorArgument(true, false));
            this.mapComplexNMS(SinglePlayerSelector.class, this.getEntitySelectorArgument(true, true));
            this.mapComplexNMS(MultipleEntitySelector.class, this.getEntitySelectorArgument(false, false));
            this.mapComplexNMS(MultiplePlayerSelector.class, this.getEntitySelectorArgument(false, true));
            /* Map Vec3 */
            this.mapComplexNMS(Location.class, this.getArgumentVec3());
            /* Map Vec2I */
            this.mapComplexNMS(Location2D.class, this.getArgumentVec2I());
        } catch (final Exception e) {
            this.commandManager.getOwningPlugin()
                    .getLogger()
                    .log(Level.WARNING, "Failed to map Bukkit types to NMS argument types", e);
        }
    }

    /**
     * @param single      Whether the selector is for a single entity only (true), or for multiple entities (false)
     * @param playersOnly Whether the selector is for players only (true), or for all entities (false)
     * @return The NMS ArgumentType
     */
    private @NonNull Supplier<ArgumentType<?>> getEntitySelectorArgument(
            final boolean single,
            final boolean playersOnly
    ) {
        return () -> {
            try {
                final Constructor<?> constructor = this.getNMSArgument("Entity").getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                return (ArgumentType<?>) constructor.newInstance(single, playersOnly);
            } catch (final Exception e) {
                this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Selector Argument", e);
                return null;
            }
        };
    }

    @SuppressWarnings("UnnecessaryLambda")
    private @NonNull Supplier<ArgumentType<?>> getArgumentVec3() {
        return () -> {
            try {
                return (ArgumentType<?>) this.getNMSArgument("Vec3").getDeclaredConstructor(boolean.class)
                        .newInstance(true);
            } catch (final Exception e) {
                this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Vec3D argument", e);
                return null;
            }
        };
    }

    @SuppressWarnings("UnnecessaryLambda")
    private @NonNull Supplier<ArgumentType<?>> getArgumentVec2I() {
        return () -> {
            try {
                return (ArgumentType<?>) this.getNMSArgument("Vec2I").getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Vec2I argument", e);
                return null;
            }
        };
    }

    /**
     * Attempt to retrieve an NMS argument type
     *
     * @param argument Argument type name
     * @return Argument class
     * @throws Exception If the type cannot be retrieved
     */
    @NonNull
    private Class<?> getNMSArgument(final @NonNull String argument) throws Exception {
        return Class.forName(String.format("net.minecraft.server.%s.Argument%s", this.nmsVersion, argument));
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type        Type to map
     * @param constructor Constructor that construct the NMS argument type
     */
    public void mapSimpleNMS(
            final @NonNull Class<?> type,
            final @NonNull Constructor<?> constructor
    ) {
        try {
            this.brigadierManager.registerDefaultArgumentTypeSupplier(type, () -> {
                try {
                    return constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (final Exception e) {
            this.commandManager.getOwningPlugin()
                    .getLogger()
                    .warning(String.format(
                            "Failed to map '%s' to a Mojang serializable argument type",
                            type.getCanonicalName()
                    ));
        }
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type                 Type to map
     * @param argumentTypeSupplier Supplier of the NMS argument type
     */
    public void mapComplexNMS(
            final @NonNull Class<?> type,
            final @NonNull Supplier<ArgumentType<?>> argumentTypeSupplier
    ) {
        try {
            this.brigadierManager.registerDefaultArgumentTypeSupplier(type, argumentTypeSupplier);
        } catch (final Exception e) {
            this.commandManager.getOwningPlugin()
                    .getLogger()
                    .warning(String.format(
                            "Failed to map '%s' to a Mojang serializable argument type",
                            type.getCanonicalName()
                    ));
        }
    }

}
