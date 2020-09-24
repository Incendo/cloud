//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.brigadier.CloudBrigadierManager;
import com.intellectualsites.commands.context.CommandContext;
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
        } catch (final Exception e) {
            this.paperCommandManager.getOwningPlugin()
                                    .getLogger()
                                    .log(Level.WARNING, "Failed to map Bukkit types to NMS argument types", e);
        }
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

    @EventHandler
    public void onCommandRegister(@Nonnull final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        final CommandTree<C> commandTree = this.paperCommandManager.getCommandTree();
        final CommandTree.Node<CommandArgument<C, ?>> node = commandTree.getNamedNode(event.getCommandLabel());
        if (node == null) {
            return;
        }
        event.setLiteral(this.brigadierManager.createLiteralCommandNode(node,
                                                                        event.getLiteral(),
                                                                        event.getBrigadierCommand(),
                                                                        event.getBrigadierCommand(),
                                                                        (s, p) -> p.toString().isEmpty()
                                                                             || s.getBukkitSender().hasPermission(p.toString())));
    }

}
