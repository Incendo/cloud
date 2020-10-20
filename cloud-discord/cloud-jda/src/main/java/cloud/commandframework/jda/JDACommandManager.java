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
package cloud.commandframework.jda;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Command manager for use with JDA
 *
 * @param <C> Command sender type
 */
public class JDACommandManager<C> extends CommandManager<C> {

    private final JDA jda;
    private final long botId;

    private final Function<@NonNull C, @NonNull String> prefixMapper;
    private final BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper;
    private final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    private final Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param permissionMapper             Function used to check if a command sender has the permission to execute a command
     * @param commandExecutionCoordinator  Coordination provider
     * @param commandSenderMapper          Function that maps {@link JDACommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link Member}
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDACommandManager(
            final @NonNull JDA jda,
            final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
            final @Nullable BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper
    )
            throws InterruptedException {
        super(commandExecutionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.jda = jda;
        this.prefixMapper = prefixMapper;
        this.permissionMapper = permissionMapper;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        jda.addEventListener(new JDACommandListener<>(this));
        jda.awaitReady();
        this.botId = jda.getSelfUser().getIdLong();

        /* Register JDA Preprocessor */
        this.registerCommandPreProcessor(new JDACommandPreprocessor<>(this));
    }

    /**
     * Get the JDA instance
     *
     * @return JDA instance
     */
    public final @NonNull JDA getJDA() {
        return jda;
    }

    /**
     * Get the prefix mapper
     *
     * @return Prefix mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull String> getPrefixMapper() {
        return this.prefixMapper;
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    public final @NonNull Function<@NonNull JDACommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the backwards command sender plugin
     *
     * @return The backwards command sender mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull JDACommandSender> getBackwardsCommandSenderMapper() {
        return this.backwardsCommandSenderMapper;
    }

    /**
     * Get the bots discord id
     *
     * @return Bots discord id
     */
    public final long getBotId() {
        return this.botId;
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }

        if (this.permissionMapper != null) {
            return this.permissionMapper.apply(sender, permission);
        }

        final JDACommandSender jdaSender = this.backwardsCommandSenderMapper.apply(sender);

        if (!(jdaSender instanceof JDAGuildSender)) {
            return true;
        }

        final JDAGuildSender guildSender = (JDAGuildSender) jdaSender;

        return guildSender.getMember().hasPermission(Permission.valueOf(permission));
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

}
