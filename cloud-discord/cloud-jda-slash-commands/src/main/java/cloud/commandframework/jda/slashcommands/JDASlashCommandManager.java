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
package cloud.commandframework.jda.slashcommands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.jda.slashcommands.internal.CommandConfig;
import cloud.commandframework.jda.slashcommands.internal.ParserConfig;
import cloud.commandframework.jda.slashcommands.parsers.ChannelArgument;
import cloud.commandframework.jda.slashcommands.parsers.EmoteArgument;
import cloud.commandframework.jda.slashcommands.parsers.MemberArgument;
import cloud.commandframework.jda.slashcommands.parsers.RoleArgument;
import cloud.commandframework.jda.slashcommands.parsers.UserArgument;
import cloud.commandframework.jda.slashcommands.permission.BotPermissionPostProcessor;
import cloud.commandframework.jda.slashcommands.permission.UserPermissionPostProcessor;
import cloud.commandframework.jda.slashcommands.sender.JDACommandSender;
import cloud.commandframework.jda.slashcommands.sender.JDAGuildSender;
import cloud.commandframework.meta.CommandMeta;
import io.leangen.geantyref.TypeToken;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Command manager for use with JDA
 *
 * @param <C> Command sender type
 */
public class JDASlashCommandManager<C> extends CommandManager<C> {

    private final JDA jda;
    private final long botId;
    private final BiFunction<@NonNull C, @NonNull String, @Nullable String> prefixMatcher;
    private final BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper;
    private final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    private final Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private final @NonNull Supplier<CommandMeta> commandMetaSupplier;

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda JDA instance to register against
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDASlashCommandManager(
            @NonNull final JDA jda,
            @NonNull final CommandConfig<C> commandConfig,
            @NonNull final ParserConfig<C> parserConfig
    ) throws InterruptedException {
        super(commandConfig.getCommandExecutionCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.jda = jda;
        this.prefixMatcher = parserConfig.getPrefixMatcher();

        this.permissionMapper = commandConfig.getPermissionMapper();
        this.commandSenderMapper = commandConfig.getCommandSenderMapper();
        this.backwardsCommandSenderMapper = commandConfig.getBackwardsCommandSenderMapper();
        this.commandMetaSupplier = commandConfig.getCommandMetaSupplier();


        if (commandConfig.isRegisterCommandListener()) {
            jda.addEventListener(new JDACommandListener<>(
                    this,
                    parserConfig.isEnableSlashCommands(),
                    parserConfig.isEnableMessageCommands()
            ));
        }

        jda.awaitReady();
        this.botId = jda.getSelfUser().getIdLong();

        if (commandConfig.isEnableDefaultPreprocessors()) {
            /* Register JDA Preprocessor */
            this.registerCommandPreProcessor(new JDACommandPreprocessor<>(this));
        }

        /* Register JDA Command Postprocessors */
        this.registerCommandPostProcessor(new BotPermissionPostProcessor<>());
        this.registerCommandPostProcessor(new UserPermissionPostProcessor<>());

        if (parserConfig.isEnableDefaultParsers()) {
            this.registerParsers();
        }

        final CaptionRegistry<C> captionRegistry = commandConfig.getCaptionRegistry();
        if (captionRegistry != null) {
            this.setCaptionRegistry(captionRegistry);
        }

        final CommandSyntaxFormatter<C> commandSyntaxFormatter = commandConfig.getCommandSyntaxFormatter();
        if (commandSyntaxFormatter != null) {
            this.setCommandSyntaxFormatter(commandSyntaxFormatter);
        }
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
        return commandMetaSupplier.get();
    }

    private void registerParsers() {
        /* Register JDA Parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.get(User.class), parserParameters ->
                new UserArgument.UserParser<>(
                        EnumSet.allOf(UserArgument.ParserMode.class),
                        UserArgument.Isolation.GLOBAL
                ));
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Member.class), parserParameters ->
                new MemberArgument.MemberParser<>(
                        EnumSet.allOf(MemberArgument.ParserMode.class)
                ));
        this.getParserRegistry().registerParserSupplier(TypeToken.get(MessageChannel.class), parserParameters ->
                new ChannelArgument.MessageParser<>(
                        EnumSet.allOf(ChannelArgument.ParserMode.class)
                ));
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Role.class), parserParameters ->
                new RoleArgument.RoleParser<>(
                        EnumSet.allOf(RoleArgument.ParserMode.class)
                ));
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Emote.class), parserParameters ->
                new EmoteArgument.EmoteParser<>(
                        EnumSet.allOf(EmoteArgument.ParserMode.class)
                ));
    }

    public BiFunction<C, String, String> getPrefixMatcher() {
        return prefixMatcher;
    }

    /**
     * Get the JDA instance
     *
     * @return JDA instance
     */
    public final @NonNull JDA getJDA() {
        return this.jda;
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

}
