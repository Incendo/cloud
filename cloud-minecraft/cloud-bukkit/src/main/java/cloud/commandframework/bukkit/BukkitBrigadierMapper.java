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

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.parsers.BlockDataArgument;
import cloud.commandframework.bukkit.parsers.BlockPredicateArgument;
import cloud.commandframework.bukkit.parsers.EnchantmentArgument;
import cloud.commandframework.bukkit.parsers.ItemStackArgument;
import cloud.commandframework.bukkit.parsers.ItemStackPredicateArgument;
import cloud.commandframework.bukkit.parsers.location.Location2DArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Class which handles mapping argument types to their NMS Brigadier counterpart on Bukkit platforms.
 *
 * @param <C> Command sender type
 */
public final class BukkitBrigadierMapper<C> {

    private static final int UUID_ARGUMENT_VERSION = 16;

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager<C, ?> brigadierManager;


    /**
     * Class that handles mapping argument types to Brigadier for Bukkit (Commodore) and Paper.
     *
     * @param commandManager   The {@link BukkitCommandManager} to use for mapping
     * @param brigadierManager The {@link CloudBrigadierManager} to use for mapping
     */
    public BukkitBrigadierMapper(
            final @NonNull BukkitCommandManager<C> commandManager,
            final @NonNull CloudBrigadierManager<C, ?> brigadierManager
    ) {
        this.commandManager = commandManager;
        this.brigadierManager = brigadierManager;

        if (!CraftBukkitReflection.craftBukkit()) {
            this.commandManager.getOwningPlugin().getLogger().warning(
                    "Could not detect relocated CraftBukkit package, NMS brigadier mappings will not be enabled.");
            return;
        }

        this.registerMappings();
    }

    private void registerMappings() {
        /* UUID nms argument is a 1.16+ feature */
        if (CraftBukkitReflection.MAJOR_REVISION >= UUID_ARGUMENT_VERSION) {
            /* Map UUID */
            this.mapSimpleNMS(new TypeToken<UUIDArgument.UUIDParser<C>>() {
            }, "UUID");
        }
        /* Map Enchantment */
        this.mapSimpleNMS(new TypeToken<EnchantmentArgument.EnchantmentParser<C>>() {
        }, "Enchantment");
        /* Map Item arguments */
        this.mapSimpleNMS(new TypeToken<ItemStackArgument.Parser<C>>() {
        }, "ItemStack");
        this.mapSimpleNMS(new TypeToken<ItemStackPredicateArgument.Parser<C>>() {
        }, "ItemPredicate");
        /* Map Block arguments */
        this.mapSimpleNMS(new TypeToken<BlockDataArgument.Parser<C>>() {
        }, "Tile");
        this.mapSimpleNMS(new TypeToken<BlockPredicateArgument.Parser<C>>() {
        }, "BlockPredicate");
        /* Map Entity Selectors */
        this.mapNMS(new TypeToken<SingleEntitySelectorArgument.SingleEntitySelectorParser<C>>() {
        }, this.entitySelectorArgumentSupplier(true, false));
        this.mapNMS(new TypeToken<SinglePlayerSelectorArgument.SinglePlayerSelectorParser<C>>() {
        }, this.entitySelectorArgumentSupplier(true, true));
        this.mapNMS(new TypeToken<MultipleEntitySelectorArgument.MultipleEntitySelectorParser<C>>() {
        }, this.entitySelectorArgumentSupplier(false, false));
        this.mapNMS(new TypeToken<MultiplePlayerSelectorArgument.MultiplePlayerSelectorParser<C>>() {
        }, this.entitySelectorArgumentSupplier(false, true));
        /* Map Vec3 */
        this.mapNMS(new TypeToken<LocationArgument.LocationParser<C>>() {
        }, this::argumentVec3);
        /* Map Vec2 */
        this.mapNMS(new TypeToken<Location2DArgument.Location2DParser<C>>() {
        }, this::argumentVec2);
    }

    /**
     * @param single      Whether the selector is for a single entity only (true), or for multiple entities (false)
     * @param playersOnly Whether the selector is for players only (true), or for all entities (false)
     * @return The NMS ArgumentType
     */
    private @NonNull Supplier<ArgumentType<?>> entitySelectorArgumentSupplier(
            final boolean single,
            final boolean playersOnly
    ) {
        return () -> {
            try {
                final Constructor<?> constructor = getNMSArgument("Entity").getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                return (ArgumentType<?>) constructor.newInstance(single, playersOnly);
            } catch (final Exception e) {
                this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Selector Argument", e);
                return fallbackType();
            }
        };
    }

    private @NonNull ArgumentType<?> argumentVec3() {
        try {
            return (ArgumentType<?>) getNMSArgument("Vec3").getDeclaredConstructor(boolean.class)
                    .newInstance(true);
        } catch (final Exception e) {
            this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Vec3D argument", e);
            return fallbackType();
        }
    }

    private @NonNull ArgumentType<?> argumentVec2() {
        try {
            return (ArgumentType<?>) getNMSArgument("Vec2").getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            this.commandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Vec2 argument", e);
            return fallbackType();
        }
    }

    /**
     * Attempt to retrieve an NMS argument type
     *
     * @param argument Argument type name
     * @return Argument class
     * @throws RuntimeException when the class is not found
     */
    private static @NonNull Class<?> getNMSArgument(final @NonNull String argument) throws RuntimeException {
        return CraftBukkitReflection.needNMSClass("Argument" + argument);
    }

    /**
     * Attempt to register a mapping between a cloud argument parser type and an NMS brigadier argument type which
     * has a no-args constructor.
     *
     * @param type         Type to map
     * @param <T>          argument parser type
     * @param argumentName NMS Argument class name (without 'Argument' prefix)
     * @since 1.5.0
     */
    public <T extends ArgumentParser<C, ?>> void mapSimpleNMS(
            final @NonNull TypeToken<T> type,
            final @NonNull String argumentName
    ) {
        final Constructor<?> constructor;
        try {
            final Class<?> nmsArgument = getNMSArgument(argumentName);
            constructor = nmsArgument.getConstructor();
        } catch (final RuntimeException | ReflectiveOperationException e) {
            this.commandManager.getOwningPlugin().getLogger().log(
                    Level.WARNING,
                    String.format("Failed to create mapping for NMS brigadier argument type '%s'.", argumentName),
                    e
            );
            return;
        }
        this.brigadierManager.registerMapping(type, builder -> builder.to(argument -> {
            try {
                return (ArgumentType<?>) constructor.newInstance();
            } catch (final ReflectiveOperationException e) {
                this.commandManager.getOwningPlugin().getLogger().log(
                        Level.WARNING,
                        String.format(
                                "Failed to create instance of brigadier argument type '%s'.",
                                GenericTypeReflector.erase(type.getType()).getCanonicalName()
                        ),
                        e
                );
                return fallbackType();
            }
        }));
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type                 Type to map
     * @param argumentTypeSupplier Supplier of the NMS argument type
     * @param <T>                  argument parser type
     * @since 1.5.0
     */
    public <T extends ArgumentParser<C, ?>> void mapNMS(
            final @NonNull TypeToken<T> type,
            final @NonNull Supplier<ArgumentType<?>> argumentTypeSupplier
    ) {
        this.brigadierManager.registerMapping(type, builder ->
                builder.to(argument -> argumentTypeSupplier.get())
        );
    }

    private static @NonNull StringArgumentType fallbackType() {
        return StringArgumentType.word();
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type        Type to map
     * @param constructor Constructor that construct the NMS argument type
     * @deprecated use {@link #mapSimpleNMS(TypeToken, String)} instead
     */
    @Deprecated
    public void mapSimpleNMS(
            final @NonNull Class<?> type,
            final @NonNull Constructor<?> constructor
    ) {
        this.brigadierManager.registerDefaultArgumentTypeSupplier(type, () -> {
            try {
                return (ArgumentType<?>) constructor.newInstance();
            } catch (final ReflectiveOperationException e) {
                this.commandManager.getOwningPlugin().getLogger().log(
                        Level.WARNING,
                        String.format("Failed to map brigadier argument type '%s'", type.getCanonicalName()),
                        e
                );
                return fallbackType();
            }
        });
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type                 Type to map
     * @param argumentTypeSupplier Supplier of the NMS argument type
     * @deprecated use {@link #mapNMS(TypeToken, Supplier)} instead
     */
    @Deprecated
    public void mapComplexNMS(
            final @NonNull Class<?> type,
            final @NonNull Supplier<ArgumentType<?>> argumentTypeSupplier
    ) {
        this.brigadierManager.registerDefaultArgumentTypeSupplier(type, argumentTypeSupplier);
    }

}
