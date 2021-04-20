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

package cloud.commandframework.quilt.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.quilt.QuiltCommandContextKeys;
import cloud.commandframework.quilt.data.Coordinates;
import cloud.commandframework.quilt.data.Message;
import cloud.commandframework.quilt.data.MinecraftTime;
import cloud.commandframework.quilt.data.MultipleEntitySelector;
import cloud.commandframework.quilt.data.MultiplePlayerSelector;
import cloud.commandframework.quilt.data.SingleEntitySelector;
import cloud.commandframework.quilt.data.SinglePlayerSelector;
import cloud.commandframework.quilt.internal.EntitySelectorAccess;
import cloud.commandframework.quilt.mixin.MessageArgumentTypeMessageFormatAccess;
import cloud.commandframework.quilt.mixin.MessageArgumentTypeMessageSelectorAccess;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;

/**
 * Parsers for Vanilla command argument types.
 *
 * @since 1.5.0
 */
public final class QuiltArgumentParsers {

    private QuiltArgumentParsers() {
    }

    /**
     * A parser for in-game time, in ticks.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, MinecraftTime> time() {
        return new WrappedBrigadierParser<C, Integer>(TimeArgumentType.time())
                .map((ctx, val) -> ArgumentParseResult.success(MinecraftTime.of(val)));
    }

    /**
     * A parser for block coordinates.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates.BlockCoordinates> blockPos() {
        return new WrappedBrigadierParser<C, PosArgument>(BlockPosArgumentType.blockPos())
                .map(QuiltArgumentParsers::mapToCoordinates);
    }

    /**
     * A parser for column coordinates.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates.ColumnCoordinates> columnPos() {
        return new WrappedBrigadierParser<C, PosArgument>(ColumnPosArgumentType.columnPos())
                .map(QuiltArgumentParsers::mapToCoordinates);
    }

    /**
     * A parser for coordinates, relative or absolute, from 2 doubles for x and z,
     * with y always defaulting to 0.
     *
     * @param centerIntegers whether to center integers at x.5
     * @param <C>            sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates.CoordinatesXZ> vec2(final boolean centerIntegers) {
        return new WrappedBrigadierParser<C, PosArgument>(new Vec2ArgumentType(centerIntegers))
                .map(QuiltArgumentParsers::mapToCoordinates);
    }

    /**
     * A parser for coordinates, relative or absolute, from 3 doubles.
     *
     * @param centerIntegers whether to center integers at x.5
     * @param <C>            sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates> vec3(final boolean centerIntegers) {
        return new WrappedBrigadierParser<C, PosArgument>(Vec3ArgumentType.vec3(centerIntegers))
                .map(QuiltArgumentParsers::mapToCoordinates);
    }

    @SuppressWarnings("unchecked")
    private static <C, O extends Coordinates> @NonNull ArgumentParseResult<@NonNull O> mapToCoordinates(
            final @NonNull CommandContext<C> ctx,
            final @NonNull PosArgument posArgument
    ) {
        return requireServerCommandSource(
                ctx,
                serverCommandSource -> ArgumentParseResult.success((O) new CoordinatesImpl(
                        serverCommandSource,
                        posArgument
                ))
        );
    }

    /**
     * A parser for {@link SinglePlayerSelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, SinglePlayerSelector> singlePlayerSelector() {
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgumentType.player())
                .map((ctx, entitySelector) -> requireServerCommandSource(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new SinglePlayerSelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.getPlayer(serverCommandSource)
                                ))
                        )
                ));
    }

    /**
     * A parser for {@link MultiplePlayerSelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, MultiplePlayerSelector> multiplePlayerSelector() {
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgumentType.players())
                .map((ctx, entitySelector) -> requireServerCommandSource(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new MultiplePlayerSelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.getPlayers(serverCommandSource)
                                ))
                        )
                ));
    }

    /**
     * A parser for {@link SingleEntitySelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, SingleEntitySelector> singleEntitySelector() {
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgumentType.entity())
                .map((ctx, entitySelector) -> requireServerCommandSource(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new SingleEntitySelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.getEntity(serverCommandSource)
                                ))
                        )
                ));
    }

    /**
     * A parser for {@link MultipleEntitySelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, MultipleEntitySelector> multipleEntitySelector() {
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgumentType.entities())
                .map((ctx, entitySelector) -> requireServerCommandSource(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new MultipleEntitySelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        Collections.unmodifiableCollection(entitySelector.getEntities(serverCommandSource))
                                ))
                        )
                ));
    }

    /**
     * A parser for {@link Message}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, Message> message() {
        return new WrappedBrigadierParser<C, MessageArgumentType.MessageFormat>(MessageArgumentType.message())
                .map((ctx, format) -> requireServerCommandSource(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(MessageImpl.from(
                                        serverCommandSource,
                                        format,
                                        true
                                ))
                        )
                ));
    }

    @FunctionalInterface
    private interface CommandSyntaxExceptionThrowingParseResultSupplier<O> {

        @NonNull ArgumentParseResult<O> result() throws CommandSyntaxException;

    }

    private static <O> @NonNull ArgumentParseResult<O> handleCommandSyntaxExceptionAsFailure(
            final @NonNull CommandSyntaxExceptionThrowingParseResultSupplier<O> resultSupplier
    ) {
        try {
            return resultSupplier.result();
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failure(ex);
        }
    }

    private static @NonNull IllegalStateException serverOnly() {
        return new IllegalStateException("This command argument type is server-only.");
    }

    private static <C, O> @NonNull ArgumentParseResult<O> requireServerCommandSource(
            final @NonNull CommandContext<C> context,
            final @NonNull Function<ServerCommandSource, ArgumentParseResult<O>> resultFunction
    ) {
        final CommandSource nativeSource = context.get(QuiltCommandContextKeys.NATIVE_COMMAND_SOURCE);
        if (!(nativeSource instanceof ServerCommandSource)) {
            return ArgumentParseResult.failure(serverOnly());
        }
        return resultFunction.apply((ServerCommandSource) nativeSource);
    }

    static final class MessageImpl implements Message {

        private final Collection<Entity> mentionedEntities;
        private final Text contents;

        static MessageImpl from(
                final @NonNull ServerCommandSource source,
                final MessageArgumentType.@NonNull MessageFormat message,
                final boolean useSelectors
        ) throws CommandSyntaxException {
            final Text contents = message.format(source, useSelectors);
            final MessageArgumentType.MessageSelector[] selectors =
                    ((MessageArgumentTypeMessageFormatAccess) message).accessor$selectors();
            final Collection<Entity> entities;
            if (!useSelectors || selectors.length == 0) {
                entities = Collections.emptySet();
            } else {
                entities = new HashSet<>();
                for (final MessageArgumentType.MessageSelector selector : selectors) {
                    entities.addAll(((MessageArgumentTypeMessageSelectorAccess) selector)
                            .accessor$selector()
                            .getEntities(source));
                }
            }

            return new MessageImpl(entities, contents);
        }

        MessageImpl(final Collection<Entity> mentionedEntities, final Text contents) {
            this.mentionedEntities = mentionedEntities;
            this.contents = contents;
        }

        @Override
        public @NonNull Collection<Entity> getMentionedEntities() {
            return this.mentionedEntities;
        }

        @Override
        public @NonNull Text getContents() {
            return this.contents;
        }

    }

    static final class CoordinatesImpl implements Coordinates,
            Coordinates.CoordinatesXZ,
            Coordinates.BlockCoordinates,
            Coordinates.ColumnCoordinates {

        private final ServerCommandSource source;
        private final PosArgument posArgument;

        CoordinatesImpl(final @NonNull ServerCommandSource source, final @NonNull PosArgument posArgument) {
            this.source = source;
            this.posArgument = posArgument;
        }

        @Override
        public @NonNull Vec3d position() {
            return this.posArgument.toAbsolutePos(this.source);
        }

        @Override
        public @NonNull BlockPos blockPos() {
            return new BlockPos(this.position());
        }

        @Override
        public boolean isXRelative() {
            return this.posArgument.isXRelative();
        }

        @Override
        public boolean isYRelative() {
            return this.posArgument.isYRelative();
        }

        @Override
        public boolean isZRelative() {
            return this.posArgument.isZRelative();
        }

        @Override
        public @NonNull PosArgument getWrappedCoordinates() {
            return this.posArgument;
        }

    }

    static final class SingleEntitySelectorImpl implements SingleEntitySelector {

        private final String inputString;
        private final EntitySelector entitySelector;
        private final Entity selectedEntity;

        SingleEntitySelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull Entity selectedEntity
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedEntity = selectedEntity;
        }

        @Override
        public @NonNull String getInput() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector getSelector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull Entity getSingle() {
            return this.selectedEntity;
        }

    }

    static final class MultipleEntitySelectorImpl implements MultipleEntitySelector {

        private final String inputString;
        private final EntitySelector entitySelector;
        private final Collection<Entity> selectedEntities;

        MultipleEntitySelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull Collection<Entity> selectedEntities
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedEntities = selectedEntities;
        }

        @Override
        public @NonNull String getInput() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector getSelector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull Collection<Entity> get() {
            return this.selectedEntities;
        }

    }

    static final class SinglePlayerSelectorImpl implements SinglePlayerSelector {

        private final String inputString;
        private final EntitySelector entitySelector;
        private final ServerPlayerEntity selectedPlayer;

        SinglePlayerSelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull ServerPlayerEntity selectedPlayer
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedPlayer = selectedPlayer;
        }

        @Override
        public @NonNull String getInput() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector getSelector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull ServerPlayerEntity getSingle() {
            return this.selectedPlayer;
        }

    }

    static final class MultiplePlayerSelectorImpl implements MultiplePlayerSelector {

        private final String inputString;
        private final EntitySelector entitySelector;
        private final Collection<ServerPlayerEntity> selectedPlayers;

        MultiplePlayerSelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull Collection<ServerPlayerEntity> selectedPlayers
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedPlayers = selectedPlayers;
        }

        @Override
        public @NonNull String getInput() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector getSelector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull Collection<ServerPlayerEntity> get() {
            return this.selectedPlayers;
        }

    }

}
