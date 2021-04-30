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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import net.minecraft.commands.arguments.blocks.BlockInput;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.schematic.PaletteTypes;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * An argument for parsing {@link BlockState BlockStates} from a {@link BlockType} identifier
 * and optional extra properties.
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code stone}</li>
 *     <li>{@code minecraft:stone}</li>
 *     <li>{@code andesite_stairs[waterlogged=true,facing=east]}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class BlockStateArgument<C> extends CommandArgument<C, BlockState> {

    private BlockStateArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(),
                defaultValue,
                BlockState.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new required {@link BlockStateArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link BlockStateArgument}
     */
    public static <C> @NonNull BlockStateArgument<C> of(final @NonNull String name) {
        return BlockStateArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link BlockStateArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link BlockStateArgument}
     */
    public static <C> @NonNull BlockStateArgument<C> optional(final @NonNull String name) {
        return BlockStateArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link BlockStateArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link BlockStateArgument}
     */
    public static <C> @NonNull BlockStateArgument<C> optional(
            final @NonNull String name,
            final @NonNull BlockState defaultValue
    ) {
        return BlockStateArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link BlockStateArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link BlockStateArgument}
     */
    public static <C> @NonNull BlockStateArgument<C> optional(
            final @NonNull String name,
            final @NonNull BlockType defaultValue
    ) {
        return BlockStateArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link BlockStateArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link BlockStateArgument}
     */
    public static <C> @NonNull BlockStateArgument<C> optional(
            final @NonNull String name,
            final @NonNull DefaultedRegistryReference<BlockType> defaultValue
    ) {
        return BlockStateArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link Builder}
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Parser for {@link BlockState BlockStates}.
     *
     * @param <C> sender type
     */
    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, BlockState> {

        private final ArgumentParser<C, BlockState> mappedParser =
                new WrappedBrigadierParser<C, BlockInput>(net.minecraft.commands.arguments.blocks.BlockStateArgument.block())
                        .map((ctx, blockInput) -> ArgumentParseResult.success((BlockState) blockInput.getState()));

        @Override
        public @NonNull ArgumentParseResult<@NonNull BlockState> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            return this.mappedParser.parse(commandContext, inputQueue);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.mappedParser.suggestions(commandContext, input);
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return ClientCompletionKeys.BLOCK_STATE.get().createNode();
        }

    }

    /**
     * Builder for {@link BlockStateArgument}.
     *
     * @param <C> sender type
     */
    public static final class Builder<C> extends TypedBuilder<C, BlockState, Builder<C>> {

        Builder(final @NonNull String name) {
            super(BlockState.class, name);
        }

        @Override
        public @NonNull BlockStateArgument<C> build() {
            return new BlockStateArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull BlockState defaultValue) {
            return this.asOptionalWithDefault(PaletteTypes.BLOCK_STATE_PALETTE.get().stringifier()
                    .apply(RegistryTypes.BLOCK_TYPE.get(), defaultValue));
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull BlockType defaultValue) {
            return this.asOptionalWithDefault(defaultValue.defaultState());
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull DefaultedRegistryReference<BlockType> defaultValue) {
            return this.asOptionalWithDefault(defaultValue.get());
        }

    }

}
