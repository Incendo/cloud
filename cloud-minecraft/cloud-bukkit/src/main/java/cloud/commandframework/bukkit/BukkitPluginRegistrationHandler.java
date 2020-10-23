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
package cloud.commandframework.bukkit;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.GenericCommandHelpTopic;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class BukkitPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, org.bukkit.command.Command> registeredCommands = new HashMap<>();
    private final Set<String> recognizedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private Map<String, org.bukkit.command.Command> bukkitCommands;
    private BukkitCommandManager<C> bukkitCommandManager;
    private CommandMap commandMap;

    BukkitPluginRegistrationHandler() {
    }

    final void initialize(final @NonNull BukkitCommandManager<C> bukkitCommandManager) throws Exception {
        final Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
        getCommandMap.setAccessible(true);
        this.commandMap = (CommandMap) getCommandMap.invoke(Bukkit.getServer());
        final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
        @SuppressWarnings("unchecked") final Map<String, org.bukkit.command.Command> bukkitCommands =
                (Map<String, org.bukkit.command.Command>) knownCommands.get(commandMap);
        this.bukkitCommands = bukkitCommands;
        this.bukkitCommandManager = bukkitCommandManager;
        Bukkit.getHelpMap().registerHelpTopicFactory(BukkitCommand.class, GenericCommandHelpTopic::new);
    }

    @Override
    public final boolean registerCommand(final @NonNull Command<?> command) {
        /* We only care about the root command argument */
        final CommandArgument<?, ?> commandArgument = command.getArguments().get(0);
        if (!(this.bukkitCommandManager.getCommandRegistrationHandler() instanceof CloudCommodoreManager)
                && this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }
        final String label = commandArgument.getName();

        @SuppressWarnings("unchecked")
        final List<String> aliases = new ArrayList<>(((StaticArgument<C>) commandArgument).getAlternativeAliases());

        @SuppressWarnings("unchecked") final BukkitCommand<C> bukkitCommand = new BukkitCommand<>(
                label,
                (this.bukkitCommandManager.getSplitAliases() ? Collections.emptyList() : aliases),
                (Command<C>) command,
                (CommandArgument<C, ?>) commandArgument,
                this.bukkitCommandManager
        );
        this.registeredCommands.put(commandArgument, bukkitCommand);
        if (!this.bukkitCommands.containsKey(label)) {
            this.recognizedAliases.add(label);
        }
        this.recognizedAliases.add(getNamespacedLabel(label));
        this.commandMap.register(
                label,
                this.bukkitCommandManager.getOwningPlugin().getName().toLowerCase(),
                bukkitCommand
        );
        this.registerExternal(label, command, bukkitCommand);

        if (this.bukkitCommandManager.getSplitAliases()) {
            for (final String alias : aliases) {
                if (!this.bukkitCommands.containsKey(alias)) {
                    @SuppressWarnings("unchecked") final BukkitCommand<C> aliasCommand = new BukkitCommand<>(
                            alias,
                            Collections.emptyList(),
                            (Command<C>) command,
                            (CommandArgument<C, ?>) commandArgument,
                            this.bukkitCommandManager
                    );
                    if (!this.bukkitCommands.containsKey(alias)) {
                        this.recognizedAliases.add(alias);
                    }
                    this.recognizedAliases.add(getNamespacedLabel(alias));
                    this.commandMap.register(
                            alias,
                            this.bukkitCommandManager.getOwningPlugin().getName().toLowerCase(),
                            bukkitCommand
                    );
                    this.registerExternal(alias, command, aliasCommand);
                }
            }
        }

        return true;
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

}
