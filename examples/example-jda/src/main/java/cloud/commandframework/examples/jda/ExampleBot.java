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
package cloud.commandframework.examples.jda;

import cloud.commandframework.Command;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.JDA4CommandManager;
import cloud.commandframework.jda.JDAGuildSender;
import cloud.commandframework.jda.JDAPrivateSender;
import cloud.commandframework.jda.parsers.UserParser;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.StringParser.stringParser;

public final class ExampleBot {

    // :)
    private static final Set<UserParser.ParserMode> USER_PARSER_MODES = Stream.of(UserParser.ParserMode.MENTION)
            .collect(Collectors.toSet());

    private ExampleBot() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts the bot
     *
     * @param args Arguments to start the bot
     * @throws InterruptedException When the jda instance does not ready correctly
     * @throws LoginException       If the bots token isn't correct
     */
    public static void main(final @NonNull String[] args) throws InterruptedException, LoginException {
        final JDA jda = JDABuilder.createDefault(System.getProperty("token"))
                .setAutoReconnect(true)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setActivity(Activity.playing("GAMES"))
                .build();

        final PermissionRegistry permissionRegistry = new PermissionRegistry();

        final JDA4CommandManager<CustomUser> commandManager = new JDA4CommandManager<>(
                jda,
                message -> "!",
                (sender, permission) -> permissionRegistry.hasPermission(sender.getUser().getIdLong(), permission),
                CommandExecutionCoordinator.simpleCoordinator(),
                sender -> {
                    MessageReceivedEvent event = sender.getEvent().orElse(null);

                    if (sender instanceof JDAPrivateSender) {
                        JDAPrivateSender jdaPrivateSender = (JDAPrivateSender) sender;
                        return new PrivateUser(event, jdaPrivateSender.getUser(), jdaPrivateSender.getPrivateChannel());
                    }

                    if (sender instanceof JDAGuildSender) {
                        JDAGuildSender jdaGuildSender = (JDAGuildSender) sender;
                        return new GuildUser(event, jdaGuildSender.getMember(), jdaGuildSender.getTextChannel());
                    }

                    throw new UnsupportedOperationException();
                },
                user -> {
                    MessageReceivedEvent event = user.getEvent().orElse(null);
                    if (user instanceof PrivateUser) {
                        PrivateUser privateUser = (PrivateUser) user;
                        return new JDAPrivateSender(event, privateUser.getUser(), privateUser.getPrivateChannel());
                    }

                    if (user instanceof GuildUser) {
                        GuildUser guildUser = (GuildUser) user;
                        return new JDAGuildSender(event, guildUser.getMember(), guildUser.getTextChannel());
                    }

                    throw new UnsupportedOperationException();
                }
        );

        commandManager.command(commandManager
                .commandBuilder("ping")
                .handler(context -> {
                    context.getSender().getChannel().sendMessage("pong").complete();
                }));

        final Command.Builder<CustomUser> builder = commandManager.commandBuilder("permission");

        commandManager.command(builder
                .literal("add")
                .required("user", UserParser.userParser(USER_PARSER_MODES, UserParser.Isolation.GUILD))
                .required("perm", stringParser())
                .handler(context -> {
                    final User user = context.get("user");
                    final String perm = context.get("perm");

                    permissionRegistry.add(user.getIdLong(), perm);
                    context.getSender().getChannel().sendMessage("permission added").complete();
                }));

        commandManager.command(builder
                .literal("remove")
                .required("user", UserParser.userParser(USER_PARSER_MODES, UserParser.Isolation.GUILD))
                .required("perm", stringParser())
                .handler(context -> {
                    final User user = context.get("user");
                    final String perm = context.get("perm");

                    permissionRegistry.remove(user.getIdLong(), perm);
                    context.getSender().getChannel().sendMessage("permission removed").complete();
                }));

        commandManager.command(commandManager
                .commandBuilder("kick")
                .senderType(GuildUser.class)
                .permission("kick")
                .required("user", UserParser.userParser(USER_PARSER_MODES, UserParser.Isolation.GUILD))
                .handler(context -> {
                    final GuildUser guildUser = context.getSender();
                    final TextChannel textChannel = guildUser.getTextChannel();
                    final User user = context.get("user");

                    textChannel.getGuild().kick(user.getId()).complete();
                    textChannel.sendMessage(user.getName() + " kicked").complete();
                }));
    }
}
