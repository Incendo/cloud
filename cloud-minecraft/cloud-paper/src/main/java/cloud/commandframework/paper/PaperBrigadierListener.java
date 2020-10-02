//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.logging.Level;

class PaperBrigadierListener<C> implements Listener {

    private static final int UUID_ARGUMENT_VERSION = 16;

    private final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager;
    private final PaperCommandManager<C> paperCommandManager;
    private final String nmsVersion;

    PaperBrigadierListener(@Nonnull final PaperCommandManager<C> paperCommandManager) throws Exception {
        this.paperCommandManager = paperCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(this.paperCommandManager,
                                                            () -> new CommandContext<>(
                                                                    this.paperCommandManager.getCommandSenderMapper()
                                                                                            .apply(Bukkit.getConsoleSender())));
        /* Register default mappings */
        final String version = Bukkit.getServer().getClass().getPackage().getName();
        this.nmsVersion = version.substring(version.lastIndexOf(".") + 1);
        final int majorMinecraftVersion = Integer.parseInt(this.nmsVersion.split("_")[1]);
        try {
            /* UUID nms argument is a 1.16+ feature */
            if (majorMinecraftVersion >= UUID_ARGUMENT_VERSION) {
                /* Map UUID */
                this.mapSimpleNMS(UUID.class, this.getNMSArgument("UUID").getConstructor());
            }
            /* Map Enchantment */
            this.mapSimpleNMS(Enchantment.class, this.getNMSArgument("Enchantment").getConstructor());
            /* Map EntityType */
            this.mapSimpleNMS(EntityType.class, this.getNMSArgument("EntitySummon").getConstructor());
            /* Map Material */
            this.mapSimpleNMS(Material.class, this.getNMSArgument("ItemStack").getConstructor());
            /* Map Entity Selectors */
            this.mapComplexNMS(SingleEntitySelector.class, this.getEntitySelectorArgument(true, false));
            this.mapComplexNMS(SinglePlayerSelector.class, this.getEntitySelectorArgument(true, true));
            this.mapComplexNMS(MultipleEntitySelector.class, this.getEntitySelectorArgument(false, false));
            this.mapComplexNMS(MultiplePlayerSelector.class, this.getEntitySelectorArgument(false, true));
        } catch (final Exception e) {
            this.paperCommandManager.getOwningPlugin()
                                    .getLogger()
                                    .log(Level.WARNING, "Failed to map Bukkit types to NMS argument types", e);
        }
    }

    /**
     * @param single      Whether the selector is for a single entity only (true), or for multiple entities (false)
     * @param playersOnly Whether the selector is for players only (true), or for all entities (false)
     * @return The NMS ArgumentType
     */
    private Supplier<ArgumentType<?>> getEntitySelectorArgument(final boolean single, final boolean playersOnly) {
        return () -> {
            try {
                Constructor<?> constructor = this.getNMSArgument("Entity").getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                return (ArgumentType<?>) constructor.newInstance(single, playersOnly);
            } catch (Exception e) {
                this.paperCommandManager.getOwningPlugin().getLogger().log(Level.INFO, "Failed to retrieve Selector Argument", e);
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
    @Nonnull
    private Class<?> getNMSArgument(@Nonnull final String argument) throws Exception {
        return Class.forName(String.format("net.minecraft.server.%s.Argument%s", this.nmsVersion, argument));
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type        Type to map
     * @param constructor Constructor that construct the NMS argument type
     */
    public void mapSimpleNMS(@Nonnull final Class<?> type,
                             @Nonnull final Constructor<?> constructor) {
        try {
            this.brigadierManager.registerDefaultArgumentTypeSupplier(type, () -> {
                try {
                    return (ArgumentType<?>) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (final Exception e) {
            this.paperCommandManager.getOwningPlugin()
                                    .getLogger()
                                    .warning(String.format("Failed to map '%s' to a Mojang serializable argument type",
                                                           type.getCanonicalName()));
        }
    }

    /**
     * Attempt to register a mapping between a type and a NMS argument type
     *
     * @param type                 Type to map
     * @param argumentTypeSupplier Supplier of the NMS argument type
     */
    public void mapComplexNMS(@Nonnull final Class<?> type,
                              @Nonnull final Supplier<ArgumentType<?>> argumentTypeSupplier) {
        try {
            this.brigadierManager.registerDefaultArgumentTypeSupplier(type, argumentTypeSupplier);
        } catch (final Exception e) {
            this.paperCommandManager.getOwningPlugin()
                                    .getLogger()
                                    .warning(String.format("Failed to map '%s' to a Mojang serializable argument type",
                                                           type.getCanonicalName()));
        }
    }

    @EventHandler
    public void onCommandRegister(@Nonnull final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        final CommandTree<C> commandTree = this.paperCommandManager.getCommandTree();
        final CommandTree.Node<CommandArgument<C, ?>> node = commandTree.getNamedNode(event.getCommandLabel());
        if (node == null) {
            return;
        }
        final BiPredicate<BukkitBrigadierCommandSource, CommandPermission> permissionChecker = (s, p) -> {
            final C sender = paperCommandManager.getCommandSenderMapper().apply(s.getBukkitSender());
            return paperCommandManager.hasPermission(sender, p);
        };
        event.setLiteral(this.brigadierManager.createLiteralCommandNode(node,
                                                                        event.getLiteral(),
                                                                        event.getBrigadierCommand(),
                                                                        event.getBrigadierCommand(),
                                                                        permissionChecker));
    }

}
