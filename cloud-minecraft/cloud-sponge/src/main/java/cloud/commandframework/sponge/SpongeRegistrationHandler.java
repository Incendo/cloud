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
package cloud.commandframework.sponge;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.permission.CommandPermission;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.plugin.PluginContainer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.BiPredicate;

final class SpongeRegistrationHandler<C> implements CommandRegistrationHandler {

    private final MethodHandle commandRegistrationMethodHandle;
    private CloudBrigadierManager<C, ?> brigadierManager;
    private SpongeCommandManager<C> commandManager;

    SpongeRegistrationHandler() {
        try {
            final Class<?> brigadierCommandRegistrar =
                    Class.forName("org.spongepowered.common.command.registrar.BrigadierCommandRegistrar");
            /*
            The reason we're using the private method here is because we need to allow for duplicates, whereas
            the public register method doesn't. This way we can indicate whether or not we do actually allow duplicates.

            The reason we need to allow for duplicates is because the same command is registered every time
            a subcommand is added to it.
             */
            final Method method = brigadierCommandRegistrar.getMethod(
                    "registerInternal",
                    PluginContainer.class,
                    LiteralArgumentBuilder.class,
                    String.class,
                    String[].class,
                    Boolean.class
            );
            method.setAccessible(true);
            commandRegistrationMethodHandle = MethodHandles.lookup().unreflect(method);
        } catch (final Exception e) {
            /* Ugly */
            throw new RuntimeException(e);
        }
    }

    void initialize(
            final @NonNull SpongeCommandManager<C> commandManager,
            final @NonNull SystemSubject subject
            ) {
        this.commandManager = commandManager;
        brigadierManager = new CloudBrigadierManager<>(
                commandManager,
                () -> new CommandContext<>(
                        commandManager.getBackwardsSubjectMapper().apply(
                                CommandCause.create() /* This is bad, fix! */
                        ),
                        commandManager.getCaptionRegistry()
                )
        );
    }

    @Override
    public boolean registerCommand(@NonNull final Command<?> command) {
        final StaticArgument<?> staticArgument = (StaticArgument<?>) command.getArguments().get(0);
        final String primaryAlias = staticArgument.getName();
        final String[] aliases = staticArgument.getAlternativeAliases().toArray(new String[0]);
        @SuppressWarnings("all")
        final LiteralCommandNode<?> literalCommandNode = this.brigadierManager.createLiteralCommandNode(
                command.getArguments().get(0).getName(),
                (Command<C>) command,
                (BiPredicate) (o, permission) -> commandManager.hasPermission(
                        commandManager.getBackwardsSubjectMapper().apply((CommandCause) o),
                        (CommandPermission) permission
                ),
                false,
                (com.mojang.brigadier.Command) context -> {
                    commandManager.executeCommand(
                            commandManager.getBackwardsSubjectMapper().apply((CommandCause) context.getSource()),
                            context.getInput()
                    ).whenComplete(((result, throwable) -> {

                    }));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }
        );
        try {
            this.commandRegistrationMethodHandle.invokeWithArguments(
                    this.commandManager.getOwningPlugin(),
                    literalCommandNode.createBuilder(),
                    primaryAlias,
                    aliases,
                    true
            );
        } catch (final Throwable throwable) {
            throwable.printStackTrace();
        }
        return true;
    }

}
