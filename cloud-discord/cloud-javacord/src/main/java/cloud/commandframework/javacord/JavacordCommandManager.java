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
package cloud.commandframework.javacord;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.javacord.sender.JavacordCommandSender;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.DiscordApi;

import java.util.function.Function;

public class JavacordCommandManager<C> extends CommandManager<C> {

    private final DiscordApi discordApi;
    private final Function<JavacordCommandSender, C> commandSenderMapper;
    private final Function<C, JavacordCommandSender> backwardsCommandSenderMapper;

    private final Function<C, String> commandPrefixMapper;
    private final Function<C, Boolean> commandPermissionMapper;

    /**
     * Construct a new Javacord command manager
     *
     * @param discordApi                   Instance of {@link DiscordApi} used to register listeners
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link Object} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link Object}
     * @param commandPrefixMapper          Function that maps the command sender type to the command prefix
     * @param commandPermissionMapper      Function used to check if a command sender has the permission to execute a command
     * @throws Exception If the construction of the manager fails
     */
    public JavacordCommandManager(final @NonNull DiscordApi discordApi,
                                  final @NonNull Function<@NonNull CommandTree<C>,
                                          @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                                  final @NonNull Function<@NonNull JavacordCommandSender, @NonNull C> commandSenderMapper,
                                  @NonNull
                                  final Function<@NonNull C, @NonNull JavacordCommandSender> backwardsCommandSenderMapper,
                                  final @NonNull Function<@NonNull C, @NonNull String> commandPrefixMapper,
                                  final @NonNull Function<@NonNull C, @NonNull Boolean> commandPermissionMapper)
            throws Exception {
        super(commandExecutionCoordinator, new JavacordRegistrationHandler<>());
        ((JavacordRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.discordApi = discordApi;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        this.commandPrefixMapper = commandPrefixMapper;
        this.commandPermissionMapper = commandPermissionMapper;
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.commandPermissionMapper.apply(sender);
    }

    @Override
    public final @NonNull SimpleCommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    final @NonNull Function<@NonNull JavacordCommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Gets the current command prefix
     *
     * @param sender Sender used to get the prefix (probably won't used anyways)
     * @return the command prefix
     */
    public @NonNull String getCommandPrefix(final @NonNull C sender) {
        return this.commandPrefixMapper.apply(sender);
    }

    /**
     * Gets the DiscordApi instance
     *
     * @return Current DiscordApi instance
     */
    public @NonNull DiscordApi getDiscordApi() {
        return this.discordApi;
    }
}
