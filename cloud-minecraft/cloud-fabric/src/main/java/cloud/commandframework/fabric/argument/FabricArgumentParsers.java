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

package cloud.commandframework.fabric.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import cloud.commandframework.fabric.data.Message;
import cloud.commandframework.fabric.data.MinecraftTime;
import cloud.commandframework.fabric.data.MultipleEntitySelector;
import cloud.commandframework.fabric.data.MultiplePlayerSelector;
import cloud.commandframework.fabric.data.SingleEntitySelector;
import cloud.commandframework.fabric.data.SinglePlayerSelector;
import cloud.commandframework.fabric.internal.EntitySelectorAccess;
import cloud.commandframework.fabric.mixin.MessageArgumentTypeMessageFormatAccess;
import cloud.commandframework.fabric.mixin.MessageArgumentTypeMessageSelectorAccess;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Parsers for Vanilla command argument types.
 *
 * @since 1.5.0
 */
public final class FabricArgumentParsers {

    private FabricArgumentParsers() {
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
     * A parser for {@link SinglePlayerSelector}.
     *
     * @param <C> sender type
     * @return a parser instance
     * @since 1.5.0
     */
    public static <C> @NonNull ArgumentParser<C, SinglePlayerSelector> singlePlayerSelector() {
        return new WrappedBrigadierParser<C, EntitySelector>(EntityArgumentType.player())
                .map((ctx, entitySelector) -> {
                    final CommandSource either = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
                    if (!(either instanceof ServerCommandSource)) {
                        return ArgumentParseResult.failure(serverOnly());
                    }
                    try {
                        return ArgumentParseResult.success(new SinglePlayerSelector(
                                ((EntitySelectorAccess) entitySelector).inputString(),
                                entitySelector,
                                entitySelector.getPlayer((ServerCommandSource) either)
                        ));
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });
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
                .map((ctx, entitySelector) -> {
                    final CommandSource either = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
                    if (!(either instanceof ServerCommandSource)) {
                        return ArgumentParseResult.failure(serverOnly());
                    }
                    try {
                        return ArgumentParseResult.success(new MultiplePlayerSelector(
                                ((EntitySelectorAccess) entitySelector).inputString(),
                                entitySelector,
                                entitySelector.getPlayers((ServerCommandSource) either)
                        ));
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });
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
                .map((ctx, entitySelector) -> {
                    final CommandSource either = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
                    if (!(either instanceof ServerCommandSource)) {
                        return ArgumentParseResult.failure(serverOnly());
                    }
                    try {
                        return ArgumentParseResult.success(new SingleEntitySelector(
                                ((EntitySelectorAccess) entitySelector).inputString(),
                                entitySelector,
                                entitySelector.getEntity((ServerCommandSource) either)
                        ));
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });
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
                .map((ctx, entitySelector) -> {
                    final CommandSource either = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
                    if (!(either instanceof ServerCommandSource)) {
                        return ArgumentParseResult.failure(serverOnly());
                    }
                    try {
                        return ArgumentParseResult.success(new MultipleEntitySelector(
                                ((EntitySelectorAccess) entitySelector).inputString(),
                                entitySelector,
                                Collections.unmodifiableCollection(entitySelector.getEntities((ServerCommandSource) either))
                        ));
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });
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
                .map((ctx, format) -> {
                    final CommandSource either = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
                    if (!(either instanceof ServerCommandSource)) {
                        return ArgumentParseResult.failure(serverOnly());
                    }
                    try {
                        return ArgumentParseResult.success(MessageImpl.from(
                                (ServerCommandSource) either,
                                format,
                                true
                        ));
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });
    }

    private static @NonNull IllegalStateException serverOnly() {
        return new IllegalStateException("This command argument type is server-only.");
    }

    static final class MessageImpl implements Message {
        private final Collection<Entity> mentionedEntities;
        private final Text contents;

        static MessageImpl from(
                final @NonNull ServerCommandSource source,
                final MessageArgumentType.@NonNull MessageFormat message,
                final boolean useSelectors
        ) throws CommandSyntaxException {
            final Text contents = message.format(source,  useSelectors);
            final MessageArgumentType.MessageSelector[] selectors =
                    ((MessageArgumentTypeMessageFormatAccess) message).accessor$selectors();
            final Collection<Entity> entities;
            if (!useSelectors || selectors.length == 0) {
                entities = Collections.emptySet();
            } else {
                entities = new HashSet<>();
                for (final MessageArgumentType.MessageSelector selector : selectors) {
                    entities.addAll(((MessageArgumentTypeMessageSelectorAccess) selector).accessor$selector().getEntities(source));
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

}
