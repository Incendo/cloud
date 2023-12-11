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
package cloud.commandframework.fabric;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.annotations.specifier.Center;
import cloud.commandframework.fabric.argument.FabricVanillaArgumentParsers;
import cloud.commandframework.fabric.data.Coordinates;
import cloud.commandframework.fabric.data.Message;
import cloud.commandframework.fabric.data.MultipleEntitySelector;
import cloud.commandframework.fabric.data.MultiplePlayerSelector;
import cloud.commandframework.fabric.data.SingleEntitySelector;
import cloud.commandframework.fabric.data.SinglePlayerSelector;
import cloud.commandframework.fabric.internal.LateRegistrationCatcher;
import cloud.commandframework.keys.CloudKey;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A command manager for registering server-side commands.
 *
 * <p>All commands should be registered within mod initializers. Any registrations occurring after the first call to
 * {@link CommandRegistrationCallback} will be considered <em>unsafe</em>, and will only be permitted when the unsafe
 * registration manager option is enabled.</p>
 *
 * @param <C> the command sender type
 * @since 1.5.0
 */
public final class FabricServerCommandManager<C> extends FabricCommandManager<C, CommandSourceStack> {

    /**
     * A meta attribute specifying which environments a command should be registered in.
     *
     * <p>The default value is {@link Commands.CommandSelection#ALL}.</p>
     *
     * @since 1.5.0
     */
    public static final CloudKey<Commands.CommandSelection> META_REGISTRATION_ENVIRONMENT = CloudKey.of(
            "cloud:registration-environment",
            Commands.CommandSelection.class
    );

    /**
     * Create a command manager using native source types.
     *
     * @param execCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #FabricServerCommandManager(Function, Function, Function) for a more thorough explanation
     * @since 1.5.0
     */
    public static @NonNull FabricServerCommandManager<@NonNull CommandSourceStack> createNative(
            final @NonNull Function<@NonNull CommandTree<@NonNull CommandSourceStack>,
                    @NonNull CommandExecutionCoordinator<@NonNull CommandSourceStack>> execCoordinator
    ) {
        return new FabricServerCommandManager<>(execCoordinator, Function.identity(), Function.identity());
    }

    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link AsynchronousCommandExecutionCoordinator}
     * @param commandSourceMapper          Function that maps {@link CommandSourceStack} to the command sender type
     * @param backwardsCommandSourceMapper Function that maps the command sender type to {@link CommandSourceStack}
     * @since 1.5.0
     */
    public FabricServerCommandManager(
            final @NonNull Function<@NonNull CommandTree<@NonNull C>,
                    @NonNull CommandExecutionCoordinator<@NonNull C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSourceStack, @NonNull C> commandSourceMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSourceStack> backwardsCommandSourceMapper
    ) {
        super(
                commandExecutionCoordinator,
                commandSourceMapper,
                backwardsCommandSourceMapper,
                new FabricCommandRegistrationHandler.Server<>(),
                () -> new CommandSourceStack(
                        CommandSource.NULL,
                        Vec3.ZERO,
                        Vec2.ZERO,
                        null,
                        4,
                        "",
                        Component.empty(),
                        null,
                        null
                )
        );

        if (LateRegistrationCatcher.hasServerAlreadyStarted()) {
            throw new IllegalStateException("FabricServerCommandManager was created too late! Because command registration "
                    + "occurs before the server instance is created, commands should be registered in mod initializers.");
        }

        this.registerParsers();
    }

    private void registerParsers() {
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(Message.class),
                params -> FabricVanillaArgumentParsers.<C>messageParser().parser()
        );

        // Location arguments
        this.parserRegistry().registerAnnotationMapper(
                Center.class,
                (annotation, type) -> ParserParameters.single(FabricParserParameters.CENTER_INTEGERS, true)
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(Coordinates.class),
                params -> FabricVanillaArgumentParsers.<C>vec3Parser(params.get(
                        FabricParserParameters.CENTER_INTEGERS,
                        false
                )).parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(Coordinates.CoordinatesXZ.class),
                params -> FabricVanillaArgumentParsers.<C>vec2Parser(params.get(
                        FabricParserParameters.CENTER_INTEGERS,
                        false
                )).parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(Coordinates.BlockCoordinates.class),
                params -> FabricVanillaArgumentParsers.<C>blockPosParser().parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(Coordinates.ColumnCoordinates.class),
                params -> FabricVanillaArgumentParsers.<C>columnPosParser().parser()
        );

        // Entity selectors
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(SinglePlayerSelector.class),
                params -> FabricVanillaArgumentParsers.<C>singlePlayerSelectorParser().parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultiplePlayerSelector.class),
                params -> FabricVanillaArgumentParsers.<C>multiplePlayerSelectorParser().parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(SingleEntitySelector.class),
                params -> FabricVanillaArgumentParsers.<C>singleEntitySelectorParser().parser()
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultipleEntitySelector.class),
                params -> FabricVanillaArgumentParsers.<C>multipleEntitySelectorParser().parser()
        );
    }

    /**
     * Check if a sender has a certain permission.
     *
     * <p>The current implementation checks permissions using {@code fabric-permissions-api-v0},
     * falling back to op level checks.</p>
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return whether the sender has the specified permission
     * @since 1.5.0
     */
    @Override
    public boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        final CommandSourceStack source = this.backwardsCommandSourceMapper().apply(sender);
        return Permissions.check(source, permission, source.getServer().getOperatorUserPermissionLevel());
    }
}
