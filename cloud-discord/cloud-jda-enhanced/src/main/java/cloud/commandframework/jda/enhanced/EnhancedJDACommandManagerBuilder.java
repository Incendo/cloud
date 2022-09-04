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

    private final @NonNull JDA jda;
    private final @NonNull List<Function<@NonNull C, @NonNull List<String>>> prefixMappers = new ArrayList<>();
    private final @NonNull List<Function<@NonNull C, @NonNull String>> singlePrefixMappers = new ArrayList<>();
    private final @NonNull Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    private final @NonNull Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private boolean botMentionPrefixEnabled = false;
    private boolean messageCommandsEnabled = true;
    private boolean defaultPreprocessorsEnabled = true;
    private boolean defaultParsersEnabled = true;
    private boolean registerCommandListener = true;
    private @Nullable CommandSyntaxFormatter<C> commandSyntaxFormatter = null;
    private @Nullable CaptionRegistry<C> captionRegistry = null;
    private @NonNull BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper = (C c, String permission) -> false;
    private @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator =
            AsynchronousCommandExecutionCoordinator
                    .<C>newBuilder()
                    .withAsynchronousParsing()
                    .build();
    private @NonNull Supplier<CommandMeta> commandMetaSupplier = SimpleCommandMeta::empty;

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

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final @NonNull EnhancedJDACommandManagerBuilder<C> addPrefixMappers(
            @NonNull final Function<C, @NonNull List<String>>... prefixMappers
    ) {
        Collections.addAll(this.prefixMappers, prefixMappers);

        return this;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final @NonNull EnhancedJDACommandManagerBuilder<C> addSinglePrefixMappers(
            @NonNull final Function<C,
                    @NonNull String>... singlePrefixMappers
    ) {
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

    public @NonNull JDA getJda() {
        return jda;
    }

    public @NonNull List<Function<C, List<String>>> getPrefixMappers() {
        return prefixMappers;
    }

    public @NonNull List<Function<C, String>> getSinglePrefixMappers() {
        return singlePrefixMappers;
    }

    public @NonNull Function<JDACommandSender, C> getCommandSenderMapper() {
        return commandSenderMapper;
    }

    public @NonNull Function<C, JDACommandSender> getBackwardsCommandSenderMapper() {
        return backwardsCommandSenderMapper;
    }

    public boolean isBotMentionPrefixEnabled() {
        return botMentionPrefixEnabled;
    }

    public boolean isDefaultPreprocessorsEnabled() {
        return defaultPreprocessorsEnabled;
    }

    public @NonNull EnhancedJDACommandManager<C> build() throws InterruptedException {
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
                messageCommandsEnabled,
                defaultParsersEnabled
        );

        return new EnhancedJDACommandManager<>(jda, commandConfig, parserConfig);
    }

    public @Nullable CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return commandSyntaxFormatter;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setCommandSyntaxFormatter(
            @Nullable final CommandSyntaxFormatter<C> commandSyntaxFormatter
    ) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;
        return this;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setDefaultPreprocessorsEnabled(final boolean defaultPreprocessorsEnabled) {
        this.defaultPreprocessorsEnabled = defaultPreprocessorsEnabled;
        return this;
    }

    public boolean isRegisterCommandListener() {
        return registerCommandListener;
    }

    public boolean isDefaultParsersEnabled() {
        return defaultParsersEnabled;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setRegisterCommandListener(final boolean registerCommandListener) {
        this.registerCommandListener = registerCommandListener;
        return this;
    }

    public @Nullable CaptionRegistry<C> getCaptionRegistry() {
        return captionRegistry;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setCaptionRegistry(@Nullable final CaptionRegistry<C> captionRegistry) {
        this.captionRegistry = captionRegistry;
        return this;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setBotMentionPrefixEnabled(final boolean botMentionPrefixEnabled) {
        this.botMentionPrefixEnabled = botMentionPrefixEnabled;
        return this;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setDefaultParsersEnabled(final boolean defaultParsersEnabled) {
        this.defaultParsersEnabled = defaultParsersEnabled;
        return this;
    }

    public @NonNull BiFunction<C, String, Boolean> getPermissionMapper() {
        return permissionMapper;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setPermissionMapper(
            @NonNull final BiFunction<C, String, Boolean> permissionMapper
    ) {
        this.permissionMapper = permissionMapper;
        return this;
    }

    public @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> getCommandExecutionCoordinator() {
        return commandExecutionCoordinator;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setCommandExecutionCoordinator(
            @NonNull final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator
    ) {
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        return this;
    }

    public @NonNull Supplier<CommandMeta> getCommandMetaSupplier() {
        return commandMetaSupplier;
    }

    public @NonNull EnhancedJDACommandManagerBuilder<C> setCommandMetaSupplier(
            @NonNull final Supplier<CommandMeta> commandMetaSupplier
    ) {
        this.commandMetaSupplier = commandMetaSupplier;
        return this;
    }

    /**
     * Sets the command execution coordinator to use {@link AsynchronousCommandExecutionCoordinator}.
     *
     * @return The builder. Useful for chaining.
     */
    public @NonNull EnhancedJDACommandManagerBuilder<C> withAsynchronousCoordinator() {
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
    public @NonNull EnhancedJDACommandManagerBuilder<C> withSynchronousCoordinator() {
        commandExecutionCoordinator = CommandExecutionCoordinator.simpleCoordinator();

        return this;
    }
}
