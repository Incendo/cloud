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

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.arguments.StandardCommandSyntaxFormatter;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandSuggestionProcessor;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import net.dv8tion.jda.api.JDA;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


@SuppressWarnings("unused")
public final class JDASlashCommandManagerBuilder<C> {

    @NonNull
    private final JDA jda;
    @NonNull
    private final List<Function<@NonNull C, @NonNull String>> prefixMatchers = new ArrayList<>();
    @NonNull
    private final List<Function<@NonNull C, @NonNull List<String>>> prefixMappers = new ArrayList<>();
    @NonNull
    private final List<Function<@NonNull C, @NonNull String>> singlePrefixMappers = new ArrayList<>();
    @NonNull
    private final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    @NonNull
    private final Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private boolean enableBotMentionPrefix = false;
    private boolean enableSlashCommands = true;
    private boolean enableDefaultPreprocessors = true;
    private boolean enableDefaultParsers = true;
    private boolean registerCommandListener = true;
    @NonNull
    private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>();
    @NonNull
    private CommandSuggestionProcessor<C> commandSuggestionProcessor = new FilteringCommandSuggestionProcessor<>();
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

    @SuppressWarnings("NewMethodNamingConvention")
    public static <C> JDASlashCommandManagerBuilder<C> Builder(
            @NonNull final JDA jda,
            @NonNull final Function<JDACommandSender, C> commandSenderMapper,
            @NonNull final Function<C, JDACommandSender> backwardsCommandSenderMapper
    ) {
        return new JDASlashCommandManagerBuilder<>(jda, commandSenderMapper, backwardsCommandSenderMapper);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static JDASlashCommandManagerBuilder<JDACommandSender> Builder(@NonNull final JDA jda) {
        return new JDASlashCommandManagerBuilder<>(jda, Function.identity(), Function.identity());
    }

    private JDASlashCommandManagerBuilder(
            @NonNull final JDA jda,
            @NonNull final Function<JDACommandSender, C> commandSenderMapper,
            @NonNull final Function<C, JDACommandSender> backwardsCommandSenderMapper
    ) {
        this.jda = jda;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> withAsynchronousCoordinator() {
        commandExecutionCoordinator = AsynchronousCommandExecutionCoordinator
                .<C>newBuilder()
                .withAsynchronousParsing()
                .build();

        return this;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> withSynchronousCoordinator() {
        commandExecutionCoordinator = CommandExecutionCoordinator.simpleCoordinator();

        return this;
    }

    @NonNull
    @SafeVarargs
    public final JDASlashCommandManagerBuilder<C> addPrefixMatchers(@NonNull final Function<C, @Nullable String>... prefixMatchers) {
        Collections.addAll(this.prefixMatchers, prefixMatchers);

        return this;
    }

    @NonNull
    @SafeVarargs
    public final JDASlashCommandManagerBuilder<C> addPrefixMappers(@NonNull final Function<C, @NonNull List<String>>... prefixMappers) {
        Collections.addAll(this.prefixMappers, prefixMappers);

        return this;
    }

    @NonNull
    @SafeVarargs
    public final JDASlashCommandManagerBuilder<C> addSinglePrefixMappers(@NonNull final Function<C, @NonNull String>... prefixMappers) {
        Collections.addAll(this.singlePrefixMappers, prefixMappers);

        return this;
    }

    public boolean isEnableDefaultPreprocessors() {
        return enableDefaultPreprocessors;
    }

    public JDASlashCommandManagerBuilder<C> setEnableDefaultPreprocessors(final boolean enableDefaultPreprocessors) {
        this.enableDefaultPreprocessors = enableDefaultPreprocessors;
        return this;
    }

    public boolean isEnableDefaultParsers() {
        return enableDefaultParsers;
    }

    public JDASlashCommandManagerBuilder<C> setEnableDefaultParsers(final boolean enableDefaultParsers) {
        this.enableDefaultParsers = enableDefaultParsers;
        return this;
    }

    public boolean isEnableSlashCommands() {
        return enableSlashCommands;
    }

    public JDASlashCommandManagerBuilder<C> setEnableSlashCommands(final boolean enableSlashCommands) {
        this.enableSlashCommands = enableSlashCommands;
        return this;
    }

    public boolean isRegisterCommandListener() {
        return registerCommandListener;
    }

    public JDASlashCommandManagerBuilder<C> setRegisterCommandListener(final boolean registerCommandListener) {
        this.registerCommandListener = registerCommandListener;
        return this;
    }

    public boolean isEnableBotMentionPrefix() {
        return enableBotMentionPrefix;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setEnableBotMentionPrefix(final boolean enableBotMentionPrefix) {
        this.enableBotMentionPrefix = enableBotMentionPrefix;

        return this;
    }

    @NonNull
    public JDA getJda() {
        return jda;
    }

    @NonNull
    public List<Function<C, String>> getPrefixMatchers() {
        return prefixMatchers;
    }

    @NonNull
    public List<Function<C, String>> getSinglePrefixMappers() {
        return singlePrefixMappers;
    }

    @NonNull
    public Function<CommandTree<C>, CommandExecutionCoordinator<C>> getCommandExecutionCoordinator() {
        return commandExecutionCoordinator;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setCommandExecutionCoordinator(
            final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator
    ) {
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        return this;
    }

    @NonNull
    public List<Function<C, List<String>>> getPrefixMappers() {
        return prefixMappers;
    }

    @NonNull
    public Function<JDACommandSender, C> getCommandSenderMapper() {
        return commandSenderMapper;
    }

    @NonNull
    public Function<C, JDACommandSender> getBackwardsCommandSenderMapper() {
        return backwardsCommandSenderMapper;
    }

    @NonNull
    public CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return commandSyntaxFormatter;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setCommandSyntaxFormatter(final CommandSyntaxFormatter<C> commandSyntaxFormatter) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;

        return this;
    }

    @Nullable
    public CaptionRegistry<C> getCaptionRegistry() {
        return captionRegistry;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setCaptionRegistry(final CaptionRegistry<C> captionRegistry) {
        this.captionRegistry = captionRegistry;

        return this;
    }

    @NonNull
    public BiFunction<C, String, Boolean> getPermissionMapper() {
        return permissionMapper;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setPermissionMapper(final BiFunction<C, String, Boolean> permissionMapper) {
        this.permissionMapper = permissionMapper;

        return this;
    }

    @NonNull
    public CommandSuggestionProcessor<C> getCommandSuggestionProcessor() {
        return commandSuggestionProcessor;
    }

    @NonNull
    public JDASlashCommandManagerBuilder<C> setCommandSuggestionProcessor(final CommandSuggestionProcessor<C> commandSuggestionProcessor) {
        this.commandSuggestionProcessor = commandSuggestionProcessor;

        return this;
    }

    @NonNull
    public JDASlashCommandManager<C> build() throws InterruptedException {
        return null; // TODO: 2021-09-12  
    }

}
