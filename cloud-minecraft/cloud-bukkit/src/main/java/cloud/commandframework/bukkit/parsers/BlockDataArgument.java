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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.arguments.ArgumentType;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * Argument type for parsing a {@link Material} and optional extra properties into a {@link BlockData}.
 *
 * <p>This argument type is only usable on Minecraft 1.13+, as the {@link BlockData} API did not exist prior.</p>
 *
 * <p>This argument type only provides basic suggestions by default. Enabling Brigadier compatibility through
 * {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public final class BlockDataArgument<C> extends CommandArgument<C, BlockData> {

    private BlockDataArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new Parser<>(), defaultValue, BlockData.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> BlockDataArgument.@NonNull Builder<C> builder(final @NonNull String name) {
        return new BlockDataArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link BlockDataArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockDataArgument<C> of(final @NonNull String name) {
        return BlockDataArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link BlockDataArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockDataArgument<C> optional(final @NonNull String name) {
        return BlockDataArgument.<C>builder(name).asOptional().build();
    }


    /**
     * Builder for {@link BlockDataArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, BlockData, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(BlockData.class, name);
        }

        @Override
        public @NonNull BlockDataArgument<C> build() {
            return new BlockDataArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    /**
     * Parser for {@link BlockDataArgument}. Only supported on Minecraft 1.13 and newer CraftBukkit based servers.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Parser<C> implements ArgumentParser<C, BlockData> {

        private static final Class<?> ARGUMENT_TILE_CLASS = CraftBukkitReflection.needNMSClass("ArgumentTile");
        private static final Class<?> ARGUMENT_TILE_LOCATION_CLASS =
                CraftBukkitReflection.needNMSClass("ArgumentTileLocation"); // BlockInput
        private static final Class<?> CRAFT_BLOCK_DATA_CLASS =
                CraftBukkitReflection.needOBCClass("block.data.CraftBlockData");
        private static final Class<?> I_BLOCK_DATA_CLASS = CraftBukkitReflection.needNMSClass("IBlockData");
        private static final Method FROM_DATA_METHOD =
                CraftBukkitReflection.needMethod(CRAFT_BLOCK_DATA_CLASS, "fromData", I_BLOCK_DATA_CLASS);
        private static final Field BLOCK_STATE_FIELD =
                CraftBukkitReflection.needField(ARGUMENT_TILE_LOCATION_CLASS, "a");

        private final ArgumentParser<C, BlockData> parser;

        /**
         * Create a new {@link Parser}.
         *
         * @since 1.5.0
         */
        public Parser() {
            try {
                this.parser = this.createParser();
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to initialize BlockData parser.", ex);
            }
        }

        @SuppressWarnings("unchecked")
        private ArgumentParser<C, BlockData> createParser() throws ReflectiveOperationException {
            return new WrappedBrigadierParser<C, Object>(
                    (ArgumentType<Object>) ARGUMENT_TILE_CLASS.getConstructor().newInstance()
            ).map((ctx, blockInput) -> {
                try {
                    final Object iBlockData = BLOCK_STATE_FIELD.get(blockInput);
                    final Object craftBlockData = FROM_DATA_METHOD.invoke(null, iBlockData);
                    return ArgumentParseResult.success((BlockData) craftBlockData);
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull BlockData> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            return this.parser.parse(commandContext, inputQueue);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.parser.suggestions(commandContext, input);
        }

    }

    /**
     * Called reflectively by {@link BukkitCommandManager} in order
     * to avoid directly referencing the MC 1.13+ API class {@link BlockData}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull BukkitCommandManager<C> commandManager) {
        commandManager.getParserRegistry()
                .registerParserSupplier(TypeToken.get(BlockData.class), params -> new BlockDataArgument.Parser<>());
    }

}
