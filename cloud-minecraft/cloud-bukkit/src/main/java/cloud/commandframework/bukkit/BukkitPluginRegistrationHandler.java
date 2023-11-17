//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitPluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private final Map<CommandComponent<C>, org.bukkit.command.Command> registeredCommands = new HashMap<>();
    private final Set<String> recognizedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private Map<String, org.bukkit.command.Command> bukkitCommands;
    private BukkitCommandManager<C> bukkitCommandManager;
    private CommandMap commandMap;

    protected BukkitPluginRegistrationHandler() {
    }

    final void initialize(final @NonNull BukkitCommandManager<C> bukkitCommandManager) throws Exception {
        final Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
        getCommandMap.setAccessible(true);
        this.commandMap = (CommandMap) getCommandMap.invoke(Bukkit.getServer());
        final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
        @SuppressWarnings("unchecked") final Map<String, org.bukkit.command.Command> bukkitCommands =
                (Map<String, org.bukkit.command.Command>) knownCommands.get(this.commandMap);
        this.bukkitCommands = bukkitCommands;
        this.bukkitCommandManager = bukkitCommandManager;
        Bukkit.getHelpMap().registerHelpTopicFactory(BukkitCommand.class, GenericCommandHelpTopic::new);
    }

    @Override
    public final boolean registerCommand(final @NonNull Command<C> command) {
        /* We only care about the root command argument */
        final CommandComponent<C> component = command.rootComponent();
        if (!(this.bukkitCommandManager.commandRegistrationHandler() instanceof CloudCommodoreManager)
                && this.registeredCommands.containsKey(component)) {
            return false;
        }
        final String label = component.name();
        final String namespacedLabel = this.getNamespacedLabel(label);

        @SuppressWarnings("unchecked") final List<String> aliases = new ArrayList<>(
                ((StaticArgument<C>) component.argument()).getAlternativeAliases()
        );

        @SuppressWarnings("unchecked") final BukkitCommand<C> bukkitCommand = new BukkitCommand<>(
                label,
                aliases,
                command,
                component,
                this.bukkitCommandManager
        );

        if (this.bukkitCommandManager.getSetting(CommandManager.ManagerSettings.OVERRIDE_EXISTING_COMMANDS)) {
            this.bukkitCommands.remove(label);
            aliases.forEach(this.bukkitCommands::remove);
        }

        final Set<String> newAliases = new HashSet<>();

        for (final String alias : aliases) {
            final String namespacedAlias = this.getNamespacedLabel(alias);
            newAliases.add(namespacedAlias);
            if (!this.bukkitCommandOrAliasExists(alias)) {
                newAliases.add(alias);
            }
        }

        if (!this.bukkitCommandExists(label)) {
            newAliases.add(label);
        }
        newAliases.add(namespacedLabel);

        this.commandMap.register(
                label,
                this.bukkitCommandManager.getOwningPlugin().getName().toLowerCase(),
                bukkitCommand
        );

        this.recognizedAliases.addAll(newAliases);
        if (this.bukkitCommandManager.getSplitAliases()) {
            newAliases.forEach(alias -> this.registerExternal(alias, command, bukkitCommand));
        }

        this.registeredCommands.put(component, bukkitCommand);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void unregisterRootCommand(
            final @NonNull CommandComponent<C> component
    ) {
        final org.bukkit.command.Command registeredCommand = this.registeredCommands.get(component);
        if (registeredCommand == null) {
            return;
        }
        ((BukkitCommand<C>) registeredCommand).disable();

        final List<String> aliases = new ArrayList<>(((StaticArgument<C>) component.argument()).getAlternativeAliases());
        final Set<String> registeredAliases = new HashSet<>();

        for (final String alias : aliases) {
            registeredAliases.add(this.getNamespacedLabel(alias));
            if (this.bukkitCommandOrAliasExists(alias)) {
                registeredAliases.add(alias);
            }
        }

        if (this.bukkitCommandExists(component.name())) {
            registeredAliases.add(component.name());
        }
        registeredAliases.add(this.getNamespacedLabel(component.name()));

        this.bukkitCommands.remove(component.name());
        this.bukkitCommands.remove(this.getNamespacedLabel(component.name()));

        this.recognizedAliases.removeAll(registeredAliases);
        if (this.bukkitCommandManager.getSplitAliases()) {
            registeredAliases.forEach(this::unregisterExternal);
        }

        this.registeredCommands.remove(component);

        if (this.bukkitCommandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            // Once the command has been unregistered, we need to refresh the command list for all online players.
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        }
    }

    private @NonNull String getNamespacedLabel(final @NonNull String label) {
        return String.format("%s:%s", this.bukkitCommandManager.getOwningPlugin().getName(), label).toLowerCase();
    }

    /**
     * Check if the given alias is recognizable by this registration handler
     *
     * @param alias Alias
     * @return {@code true} if the alias is recognized, else {@code false}
     */
    public boolean isRecognized(final @NonNull String alias) {
        return this.recognizedAliases.contains(alias);
    }

    protected void registerExternal(
            final @NonNull String label,
            final @NonNull Command<?> command,
            final @NonNull BukkitCommand<C> bukkitCommand
    ) {
    }

    @API(status = API.Status.STABLE, since = "1.7.0")
    protected void unregisterExternal(final @NonNull String label) {
    }

    /**
     * Returns true if a command exists in the Bukkit command map, is not an alias, and is not owned by us.
     *
     * @param commandLabel label to check
     * @return whether the command exists and is not an alias
     */
    private boolean bukkitCommandExists(final String commandLabel) {
        final org.bukkit.command.Command existingCommand = this.bukkitCommands.get(commandLabel);
        if (existingCommand == null) {
            return false;
        }
        if (existingCommand instanceof PluginIdentifiableCommand) {
            return existingCommand.getLabel().equals(commandLabel)
                    && !((PluginIdentifiableCommand) existingCommand).getPlugin().getName()
                    .equalsIgnoreCase(this.bukkitCommandManager.getOwningPlugin().getName());
        }
        return existingCommand.getLabel().equals(commandLabel);
    }

    /**
     * Returns true if a command exists in the Bukkit command map, and it is not owned by us, whether or not it is an alias.
     *
     * @param commandLabel label to check
     * @return whether the command exists
     */
    private boolean bukkitCommandOrAliasExists(final String commandLabel) {
        final org.bukkit.command.Command command = this.bukkitCommands.get(commandLabel);
        if (command instanceof PluginIdentifiableCommand) {
            return !((PluginIdentifiableCommand) command).getPlugin().getName()
                    .equalsIgnoreCase(this.bukkitCommandManager.getOwningPlugin().getName());
        }
        return command != null;
    }
}
