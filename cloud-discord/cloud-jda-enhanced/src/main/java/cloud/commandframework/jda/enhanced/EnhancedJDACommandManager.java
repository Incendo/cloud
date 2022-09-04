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
package cloud.commandframework.jda.enhanced;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.jda.enhanced.internal.CommandConfig;
import cloud.commandframework.jda.enhanced.internal.ParserConfig;
import cloud.commandframework.jda.enhanced.parsers.ChannelArgument;
import cloud.commandframework.jda.enhanced.parsers.EmoteArgument;
import cloud.commandframework.jda.enhanced.parsers.MemberArgument;
import cloud.commandframework.jda.enhanced.parsers.RoleArgument;
import cloud.commandframework.jda.enhanced.parsers.UserArgument;
import cloud.commandframework.jda.enhanced.permission.BotPermissionPostProcessor;
import cloud.commandframework.jda.enhanced.permission.UserPermissionPostProcessor;
import cloud.commandframework.jda.enhanced.sender.JDACommandSender;
import cloud.commandframework.jda.enhanced.sender.JDAGuildCommandSender;
import cloud.commandframework.meta.CommandMeta;
import io.leangen.geantyref.TypeToken;
import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;


/**
 * Command manager for use with JDA
 *
 * @param <C> Command sender type
 */
public final class EnhancedJDACommandManager<C> extends CommandManager<C> {

    private final @NonNull JDA jda;
    private final long botId;
    private final @NonNull BiFunction<@NonNull C, @NonNull String, @Nullable String> prefixMatcher;
    private final @NonNull BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper;
    private final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    private final @NonNull Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private final @NonNull Supplier<CommandMeta> commandMetaSupplier;

    /**
     * Construct a new JDA Command Manager
     *
     * @param jda JDA instance to register against
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public EnhancedJDACommandManager(
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
            this.captionRegistry(captionRegistry);
        }

        final CommandSyntaxFormatter<C> commandSyntaxFormatter = commandConfig.getCommandSyntaxFormatter();
        if (commandSyntaxFormatter != null) {
            this.commandSyntaxFormatter(commandSyntaxFormatter);
        }
    }

    public @NotNull BiFunction<C, String, String> getPrefixMatcher() {
        return prefixMatcher;
    }

    /**
     * Get the JDA instance
     *
     * @return JDA instance
     */
    public @NotNull JDA getJDA() {
        return this.jda;
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    public @NotNull Function<@NonNull JDACommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the backwards command sender plugin
     *
     * @return The backwards command sender mapper
     */
    public @NotNull Function<@NonNull C, @NonNull JDACommandSender> getBackwardsCommandSenderMapper() {
        return this.backwardsCommandSenderMapper;
    }

    /**
     * Get the bots discord id
     *
     * @return Bots discord id
     */
    public long getBotId() {
        return this.botId;
    }

    private void registerParsers() {
        /* Register JDA Parsers */
        this.parserRegistry().registerParserSupplier(TypeToken.get(User.class), parserParameters ->
                new UserArgument.UserParser<>(
                        EnumSet.allOf(UserArgument.ParserMode.class),
                        UserArgument.Isolation.GLOBAL
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(Member.class), parserParameters ->
                new MemberArgument.MemberParser<>(
                        EnumSet.allOf(MemberArgument.ParserMode.class)
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(MessageChannel.class), parserParameters ->
                new ChannelArgument.MessageParser<>(
                        EnumSet.allOf(ChannelArgument.ParserMode.class)
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(Role.class), parserParameters ->
                new RoleArgument.RoleParser<>(
                        EnumSet.allOf(RoleArgument.ParserMode.class)
                ));
        this.parserRegistry().registerParserSupplier(TypeToken.get(Emote.class), parserParameters ->
                new EmoteArgument.EmoteParser<>(
                        EnumSet.allOf(EmoteArgument.ParserMode.class)
                ));
    }

    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }

        // TODO: 2022-09-04 Permission Mapper
//        if (this.permissionMapper != null) {
//            return this.permissionMapper.apply(sender, permission);
//        }

        final JDACommandSender jdaSender = this.backwardsCommandSenderMapper.apply(sender);

        if (!(jdaSender instanceof JDAGuildCommandSender)) {
            return true;
        }

        final JDAGuildCommandSender guildSender = (JDAGuildCommandSender) jdaSender;

        // TODO: 2022-09-04 Special permission handling for webhooks (member will be null, but not user. It will be the default
        //  user, because discord is a well designed and good platform)
        return guildSender.getMember().hasPermission(Permission.valueOf(permission));
    }

    @Override
    public @NonNull CommandMeta createDefaultCommandMeta() {
        return commandMetaSupplier.get();
    }
}
