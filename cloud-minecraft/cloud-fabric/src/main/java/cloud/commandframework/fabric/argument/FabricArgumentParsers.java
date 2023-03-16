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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import cloud.commandframework.fabric.FabricCommandManager;
import cloud.commandframework.fabric.data.Coordinates;
import cloud.commandframework.fabric.data.Message;
import cloud.commandframework.fabric.data.MinecraftTime;
import cloud.commandframework.fabric.data.MultipleEntitySelector;
import cloud.commandframework.fabric.data.MultiplePlayerSelector;
import cloud.commandframework.fabric.data.SingleEntitySelector;
import cloud.commandframework.fabric.data.SinglePlayerSelector;
import cloud.commandframework.fabric.internal.EntitySelectorAccess;
import cloud.commandframework.fabric.mixin.MessageArgumentMessageAccess;
import cloud.commandframework.fabric.mixin.MessageArgumentPartAccess;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

/**
 * Parsers for Vanilla command argument types.
 *
 * @since 1.5.0
 */
public final class FabricArgumentParsers {

    private FabricArgumentParsers() {
    }

    /**
     * A parser that wraps Brigadier argument types which need a {@link CommandBuildContext}
     *
     * @param <C>     sender type
     * @param <V>     argument value type
     * @param factory factory that creates these arguments
     * @return the parser
     */
    public static <C, V> @NonNull ArgumentParser<C, V> contextual(final @NonNull Function<CommandBuildContext, ArgumentType<V>> factory) {
        return new WrappedBrigadierParser<>(new ContextualArgumentTypeProvider<>(factory));
    }

    /**
     * A parser for in-game time, in ticks.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, MinecraftTime> time() {
        return new WrappedBrigadierParser<C, Integer>(TimeArgument.time())
                .map((ctx, val) -> ArgumentParseResult.success(MinecraftTime.of(val)));
    }

    /**
     * A parser for block coordinates.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates.BlockCoordinates> blockPos() {
        return new WrappedBrigadierParser<C, net.minecraft.commands.arguments.coordinates.Coordinates>(BlockPosArgument.blockPos())
                .map(FabricArgumentParsers::mapToCoordinates);
    }

    /**
     * A parser for column coordinates.
     *
     * @param <C> sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates.ColumnCoordinates> columnPos() {
        return new WrappedBrigadierParser<C, net.minecraft.commands.arguments.coordinates.Coordinates>(ColumnPosArgument.columnPos())
                .map(FabricArgumentParsers::mapToCoordinates);
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
        return new WrappedBrigadierParser<C, net.minecraft.commands.arguments.coordinates.Coordinates>(new Vec2Argument(
                centerIntegers))
                .map(FabricArgumentParsers::mapToCoordinates);
    }

    /**
     * A parser for coordinates, relative or absolute, from 3 doubles.
     *
     * @param centerIntegers whether to center integers at x.5
     * @param <C>            sender type
     * @return a parser instance
     */
    public static <C> @NonNull ArgumentParser<C, Coordinates> vec3(final boolean centerIntegers) {
        return new WrappedBrigadierParser<C, net.minecraft.commands.arguments.coordinates.Coordinates>(Vec3Argument.vec3(
                centerIntegers))
                .map(FabricArgumentParsers::mapToCoordinates);
    }

    @SuppressWarnings("unchecked")
    private static <C, O extends Coordinates> @NonNull ArgumentParseResult<@NonNull O> mapToCoordinates(
            final @NonNull CommandContext<C> ctx,
            final net.minecraft.commands.arguments.coordinates.@NonNull Coordinates posArgument
    ) {
        return requireCommandSourceStack(
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
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.player())
                .map((ctx, entitySelector) -> requireCommandSourceStack(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new SinglePlayerSelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.findSinglePlayer(serverCommandSource)
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
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.players())
                .map((ctx, entitySelector) -> requireCommandSourceStack(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new MultiplePlayerSelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.findPlayers(serverCommandSource)
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
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.entity())
                .map((ctx, entitySelector) -> requireCommandSourceStack(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new SingleEntitySelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        entitySelector.findSingleEntity(serverCommandSource)
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
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgument.entities())
                .map((ctx, entitySelector) -> requireCommandSourceStack(
                        ctx,
                        serverCommandSource -> handleCommandSyntaxExceptionAsFailure(
                                () -> ArgumentParseResult.success(new MultipleEntitySelectorImpl(
                                        ((EntitySelectorAccess) entitySelector).inputString(),
                                        entitySelector,
                                        Collections.unmodifiableCollection(entitySelector.findEntities(serverCommandSource))
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
        return new WrappedBrigadierParser<C, MessageArgument.Message>(MessageArgument.message())
                .map((ctx, format) -> requireCommandSourceStack(
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

    private static <C, O> @NonNull ArgumentParseResult<O> requireCommandSourceStack(
            final @NonNull CommandContext<C> context,
            final @NonNull Function<CommandSourceStack, ArgumentParseResult<O>> resultFunction
    ) {
        final SharedSuggestionProvider nativeSource = context.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
        if (!(nativeSource instanceof CommandSourceStack)) {
            return ArgumentParseResult.failure(serverOnly());
        }
        return resultFunction.apply((CommandSourceStack) nativeSource);
    }

    static final class MessageImpl implements Message {

        private final Collection<Entity> mentionedEntities;
        private final Component contents;

        static MessageImpl from(
                final @NonNull CommandSourceStack source,
                final MessageArgument.@NonNull Message message,
                final boolean useSelectors
        ) throws CommandSyntaxException {
            final Component contents = message.toComponent(source, useSelectors);
            final MessageArgument.Part[] selectors =
                    ((MessageArgumentMessageAccess) message).accessor$parts();
            final Collection<Entity> entities;
            if (!useSelectors || selectors.length == 0) {
                entities = Collections.emptySet();
            } else {
                entities = new HashSet<>();
                for (final MessageArgument.Part selector : selectors) {
                    entities.addAll(((MessageArgumentPartAccess) selector)
                            .accessor$selector()
                            .findEntities(source));
                }
            }

            return new MessageImpl(entities, contents);
        }

        MessageImpl(final Collection<Entity> mentionedEntities, final Component contents) {
            this.mentionedEntities = mentionedEntities;
            this.contents = contents;
        }

        @Override
        public @NonNull Collection<Entity> mentionedEntities() {
            return this.mentionedEntities;
        }

        @Override
        public @NonNull Component contents() {
            return this.contents;
        }
    }

    static final class CoordinatesImpl implements Coordinates,
            Coordinates.CoordinatesXZ,
            Coordinates.BlockCoordinates,
            Coordinates.ColumnCoordinates {

        private final CommandSourceStack source;
        private final net.minecraft.commands.arguments.coordinates.Coordinates posArgument;

        CoordinatesImpl(
                final @NonNull CommandSourceStack source,
                final net.minecraft.commands.arguments.coordinates.@NonNull Coordinates posArgument
        ) {
            this.source = source;
            this.posArgument = posArgument;
        }

        @Override
        public @NonNull Vec3 position() {
            return this.posArgument.getPosition(this.source);
        }

        @Override
        public @NonNull BlockPos blockPos() {
            return BlockPos.containing(this.position());
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
        public net.minecraft.commands.arguments.coordinates.@NonNull Coordinates wrappedCoordinates() {
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
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector selector() {
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
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector selector() {
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
        private final ServerPlayer selectedPlayer;

        SinglePlayerSelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull ServerPlayer selectedPlayer
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedPlayer = selectedPlayer;
        }

        @Override
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector selector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull ServerPlayer getSingle() {
            return this.selectedPlayer;
        }
    }

    static final class MultiplePlayerSelectorImpl implements MultiplePlayerSelector {

        private final String inputString;
        private final EntitySelector entitySelector;
        private final Collection<ServerPlayer> selectedPlayers;

        MultiplePlayerSelectorImpl(
                final @NonNull String inputString,
                final @NonNull EntitySelector entitySelector,
                final @NonNull Collection<ServerPlayer> selectedPlayers
        ) {
            this.inputString = inputString;
            this.entitySelector = entitySelector;
            this.selectedPlayers = selectedPlayers;
        }

        @Override
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull EntitySelector selector() {
            return this.entitySelector;
        }

        @Override
        public @NonNull Collection<ServerPlayer> get() {
            return this.selectedPlayers;
        }
    }

    @ApiStatus.Internal
    public static final class ContextualArgumentTypeProvider<V> implements Supplier<ArgumentType<V>> {

        private static final ThreadLocal<ThreadLocalContext> CONTEXT = new ThreadLocal<>();
        private static final Map<FabricCommandManager<?, ?>, Set<ContextualArgumentTypeProvider<?>>> INSTANCES =
                new WeakHashMap<>();

        private final Function<CommandBuildContext, ArgumentType<V>> provider;
        private volatile ArgumentType<V> provided;

        /**
         * Temporarily expose a command build context to providers called from this thread.
         *
         * @param ctx            the context
         * @param commandManager command manager to use
         * @param resetExisting  whether to clear cached state from existing provider instances for this command type
         * @param action         an action to perform while the context is exposed
         * @since 1.7.0
         */
        public static void withBuildContext(
                final FabricCommandManager<?, ?> commandManager,
                final CommandBuildContext ctx,
                final boolean resetExisting,
                final Runnable action
        ) {
            final ThreadLocalContext context = new ThreadLocalContext(commandManager, ctx);
            CONTEXT.set(context);

            try {
                if (resetExisting) {
                    synchronized (INSTANCES) {
                        for (final ContextualArgumentTypeProvider<?> contextualArgumentTypeProvider : context.instances()) {
                            contextualArgumentTypeProvider.provided = null;
                        }
                    }
                }

                action.run();
            } finally {
                CONTEXT.remove();
            }
        }

        private static final class ThreadLocalContext {

            private final FabricCommandManager<?, ?> commandManager;
            private final CommandBuildContext commandBuildContext;

            private ThreadLocalContext(
                    final FabricCommandManager<?, ?> commandManager,
                    final CommandBuildContext commandBuildContext
            ) {
                this.commandManager = commandManager;
                this.commandBuildContext = commandBuildContext;
            }

            private Set<ContextualArgumentTypeProvider<?>> instances() {
                return INSTANCES.computeIfAbsent(this.commandManager, $ -> Collections.newSetFromMap(new WeakHashMap<>()));
            }
        }

        ContextualArgumentTypeProvider(final @NonNull Function<CommandBuildContext, ArgumentType<V>> provider) {
            this.provider = provider;
        }

        @Override
        public ArgumentType<V> get() {
            final ThreadLocalContext ctx = CONTEXT.get();

            if (ctx != null) {
                synchronized (INSTANCES) {
                    ctx.instances().add(this);
                }
            }

            ArgumentType<V> provided = this.provided;
            if (provided == null) {
                synchronized (this) {
                    if (this.provided == null) {
                        if (ctx == null) {
                            throw new IllegalStateException(
                                    "No build context was available while trying to compute an argument type");
                        }
                        provided = this.provider.apply(ctx.commandBuildContext);
                        this.provided = provided;
                    }
                }
            }
            return provided;
        }
    }
}
