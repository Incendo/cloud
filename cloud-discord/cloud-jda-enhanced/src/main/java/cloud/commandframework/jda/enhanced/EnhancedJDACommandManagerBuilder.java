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

package cloud.commandframework.jda.enhanced;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.enhanced.internal.CommandConfig;
import cloud.commandframework.jda.enhanced.internal.ComplexPrefixMatcher;
import cloud.commandframework.jda.enhanced.internal.ParserConfig;
import cloud.commandframework.jda.enhanced.sender.JDACommandSender;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dv8tion.jda.api.JDA;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EnhancedJDACommandManagerBuilder<C> {

    @NonNull
    private final JDA jda;
    @NonNull
    private final List<Function<@NonNull C, @NonNull List<String>>> prefixMappers = new ArrayList<>();
    @NonNull
    private final List<Function<@NonNull C, @NonNull String>> singlePrefixMappers = new ArrayList<>();
    @NonNull
    private final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    @NonNull
    private final Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private boolean botMentionPrefixEnabled = false;
    private boolean slashCommandsEnabled = true;
    private boolean messageCommandsEnabled = true;
    private boolean defaultPreprocessorsEnabled = true;
    private boolean defaultParsersEnabled = true;
    private boolean registerCommandListener = true;
    @Nullable
    private CommandSyntaxFormatter<C> commandSyntaxFormatter = null;
    @Nullable
    private CaptionRegistry<C> captionRegistry = null;
    @NonNull
    private BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper = (C c, String permission) -> false;
    @NonNull
    private Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator =
            AsynchronousCommandExecutionCoordinator
                    .<C>newBuilder()
                    .withAsynchronousParsing()
                    .build();
    @NonNull
    private Supplier<CommandMeta> commandMetaSupplier = SimpleCommandMeta::empty;

    private EnhancedJDACommandManagerBuilder(
            @NonNull final JDA jda,
            @NonNull final Function<JDACommandSender, C> commandSenderMapper,
            @NonNull final Function<C, JDACommandSender> backwardsCommandSenderMapper
    ) {
        this.jda = jda;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static <C> EnhancedJDACommandManagerBuilder<C> Builder(
            @NonNull final JDA jda,
            @NonNull final Function<JDACommandSender, C> commandSenderMapper,
            @NonNull final Function<C, JDACommandSender> backwardsCommandSenderMapper
    ) {
        return new EnhancedJDACommandManagerBuilder<>(jda, commandSenderMapper, backwardsCommandSenderMapper);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static EnhancedJDACommandManagerBuilder<JDACommandSender> Builder(@NonNull final JDA jda) {
        return new EnhancedJDACommandManagerBuilder<>(jda, Function.identity(), Function.identity());
    }

    @NonNull
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final EnhancedJDACommandManagerBuilder<C> addPrefixMappers(@NonNull final Function<C, @NonNull List<String>>... prefixMappers) {
        Collections.addAll(this.prefixMappers, prefixMappers);

        return this;
    }

    @NonNull
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final EnhancedJDACommandManagerBuilder<C> addSinglePrefixMappers(@NonNull final Function<C, @NonNull String>... singlePrefixMappers) {
        Collections.addAll(this.singlePrefixMappers, singlePrefixMappers);

        return this;
    }

    public boolean isMessageCommandsEnabled() {
        return messageCommandsEnabled;
    }

    public EnhancedJDACommandManagerBuilder<C> setMessageCommandsEnabled(final boolean messageCommandsEnabled) {
        this.messageCommandsEnabled = messageCommandsEnabled;
        return this;
    }

    @NonNull
    public JDA getJda() {
        return jda;
    }

    @NonNull
    public List<Function<C, List<String>>> getPrefixMappers() {
        return prefixMappers;
    }

    @NonNull
    public List<Function<C, String>> getSinglePrefixMappers() {
        return singlePrefixMappers;
    }

    @NonNull
    public Function<JDACommandSender, C> getCommandSenderMapper() {
        return commandSenderMapper;
    }

    @NonNull
    public Function<C, JDACommandSender> getBackwardsCommandSenderMapper() {
        return backwardsCommandSenderMapper;
    }

    public boolean isBotMentionPrefixEnabled() {
        return botMentionPrefixEnabled;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setBotMentionPrefixEnabled(final boolean botMentionPrefixEnabled) {
        this.botMentionPrefixEnabled = botMentionPrefixEnabled;
        return this;
    }

    public boolean isSlashCommandsEnabled() {
        return slashCommandsEnabled;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setSlashCommandsEnabled(final boolean slashCommandsEnabled) {
        this.slashCommandsEnabled = slashCommandsEnabled;
        return this;
    }

    public boolean isDefaultPreprocessorsEnabled() {
        return defaultPreprocessorsEnabled;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setDefaultPreprocessorsEnabled(final boolean defaultPreprocessorsEnabled) {
        this.defaultPreprocessorsEnabled = defaultPreprocessorsEnabled;
        return this;
    }

    public boolean isDefaultParsersEnabled() {
        return defaultParsersEnabled;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setDefaultParsersEnabled(final boolean defaultParsersEnabled) {
        this.defaultParsersEnabled = defaultParsersEnabled;
        return this;
    }

    public boolean isRegisterCommandListener() {
        return registerCommandListener;
    }

    @NonNull
    public EnhancedJDACommandManager<C> build() throws InterruptedException {
        jda.awaitReady();

        final CommandConfig<C> commandConfig = new CommandConfig<>(
                commandSenderMapper,
                backwardsCommandSenderMapper,
                defaultPreprocessorsEnabled,
                registerCommandListener,
                commandMetaSupplier,
                commandSyntaxFormatter,
                permissionMapper,
                commandExecutionCoordinator,
                captionRegistry
        );

        final ParserConfig<C> parserConfig = new ParserConfig<>(
                new ComplexPrefixMatcher<>(
                        prefixMappers,
                        singlePrefixMappers,
                        botMentionPrefixEnabled,
                        jda.getSelfUser().getId()
                ),
                slashCommandsEnabled,
                messageCommandsEnabled,
                defaultParsersEnabled
        );

        return new EnhancedJDACommandManager<>(jda, commandConfig, parserConfig);
    }

    @Nullable
    public CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return commandSyntaxFormatter;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setCaptionRegistry(@Nullable final CaptionRegistry<C> captionRegistry) {
        this.captionRegistry = captionRegistry;
        return this;
    }

    @Nullable
    public CaptionRegistry<C> getCaptionRegistry() {
        return captionRegistry;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setCommandExecutionCoordinator(
            @NonNull final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator
    ) {
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        return this;
    }

    @NonNull
    public BiFunction<C, String, Boolean> getPermissionMapper() {
        return permissionMapper;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setCommandMetaSupplier(
            @NonNull final Supplier<CommandMeta> commandMetaSupplier
    ) {
        this.commandMetaSupplier = commandMetaSupplier;
        return this;
    }

    @NonNull
    public Function<CommandTree<C>, CommandExecutionCoordinator<C>> getCommandExecutionCoordinator() {
        return commandExecutionCoordinator;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setCommandSyntaxFormatter(
            @Nullable final CommandSyntaxFormatter<C> commandSyntaxFormatter
    ) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;
        return this;
    }

    @NonNull
    public Supplier<CommandMeta> getCommandMetaSupplier() {
        return commandMetaSupplier;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setPermissionMapper(
            @NonNull final BiFunction<C, String, Boolean> permissionMapper
    ) {
        this.permissionMapper = permissionMapper;
        return this;
    }

    @NonNull
    public EnhancedJDACommandManagerBuilder<C> setRegisterCommandListener(final boolean registerCommandListener) {
        this.registerCommandListener = registerCommandListener;
        return this;
    }

    /**
     * Sets the command execution coordinator to use {@link AsynchronousCommandExecutionCoordinator}.
     *
     * @return The builder. Useful for chaining.
     */
    @NonNull
    public EnhancedJDACommandManagerBuilder<C> withAsynchronousCoordinator() {
        commandExecutionCoordinator = AsynchronousCommandExecutionCoordinator
                .<C>newBuilder()
                .withAsynchronousParsing()
                .build();

        return this;
    }

    /**
     * Sets the command execution coordinator to use {@link CommandExecutionCoordinator.SimpleCoordinator}.
     *
     * @return The builder. Useful for chaining.
     */
    @NonNull
    public EnhancedJDACommandManagerBuilder<C> withSynchronousCoordinator() {
        commandExecutionCoordinator = CommandExecutionCoordinator.simpleCoordinator();

        return this;
    }
}
