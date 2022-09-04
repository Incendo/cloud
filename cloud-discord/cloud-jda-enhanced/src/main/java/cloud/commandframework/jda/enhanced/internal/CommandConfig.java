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

package cloud.commandframework.jda.enhanced.internal;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.enhanced.sender.JDACommandSender;
import cloud.commandframework.meta.CommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CommandConfig<C> {

    private @NonNull
    final Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper;
    private @NonNull
    final Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper;
    private final boolean enableDefaultPreprocessors;
    private final boolean registerCommandListener;
    private @NonNull
    final Supplier<CommandMeta> commandMetaSupplier;
    private @Nullable
    final CommandSyntaxFormatter<C> commandSyntaxFormatter;
    private @NonNull
    final BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper;
    private @NonNull
    final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator;
    private @Nullable
    final CaptionRegistry<C> captionRegistry;

    public CommandConfig(
            final @NonNull Function<@NonNull JDACommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull JDACommandSender> backwardsCommandSenderMapper,
            final boolean enableDefaultPreprocessors,
            final boolean registerCommandListener,
            final @NonNull Supplier<CommandMeta> commandMetaSupplier,
            final @Nullable CommandSyntaxFormatter<C> commandSyntaxFormatter,
            final @NonNull BiFunction<@NonNull C, @NonNull String, @NonNull Boolean> permissionMapper,
            final @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @Nullable CaptionRegistry<C> captionRegistry
    ) {
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        this.enableDefaultPreprocessors = enableDefaultPreprocessors;
        this.registerCommandListener = registerCommandListener;
        this.commandMetaSupplier = commandMetaSupplier;
        this.commandSyntaxFormatter = commandSyntaxFormatter;
        this.permissionMapper = permissionMapper;
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        this.captionRegistry = captionRegistry;
    }

    public @Nullable CaptionRegistry<C> getCaptionRegistry() {
        return captionRegistry;
    }

    public @NonNull BiFunction<C, String, Boolean> getPermissionMapper() {
        return permissionMapper;
    }

    public @NonNull Function<JDACommandSender, C> getCommandSenderMapper() {
        return commandSenderMapper;
    }

    public @NonNull Function<C, JDACommandSender> getBackwardsCommandSenderMapper() {
        return backwardsCommandSenderMapper;
    }

    public boolean isEnableDefaultPreprocessors() {
        return enableDefaultPreprocessors;
    }

    public boolean isRegisterCommandListener() {
        return registerCommandListener;
    }

    public @NonNull Supplier<CommandMeta> getCommandMetaSupplier() {
        return commandMetaSupplier;
    }

    public @Nullable CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return commandSyntaxFormatter;
    }

    public @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> getCommandExecutionCoordinator() {
        return commandExecutionCoordinator;
    }
}
