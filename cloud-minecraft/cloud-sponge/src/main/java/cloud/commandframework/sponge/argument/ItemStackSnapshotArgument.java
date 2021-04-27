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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.DefaultedRegistryReference;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * An argument for parsing {@link ItemStackSnapshot ItemStackSnapshots} from a {@link ItemType} identifier
 * and optional NBT data.
 *
 * <p>Resulting snapshots will always have a stack size of {@code 1}.</p>
 *
 * <p>Example input strings:</p>
 * <ul>
 *     <li>{@code apple}</li>
 *     <li>{@code minecraft:apple}</li>
 *     <li>{@code diamond_sword{Enchantments:[{id:sharpness,lvl:5}]}}</li>
 * </ul>
 *
 * @param <C> sender type
 */
public final class ItemStackSnapshotArgument<C> extends CommandArgument<C, ItemStackSnapshot> {

    private ItemStackSnapshotArgument(
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
                ItemStackSnapshot.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new required {@link ItemStackSnapshotArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> of(final @NonNull String name) {
        return ItemStackSnapshotArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link ItemStackSnapshotArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> optional(final @NonNull String name) {
        return ItemStackSnapshotArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link ItemStackSnapshotArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> optional(
            final @NonNull String name,
            final @NonNull ItemStackSnapshot defaultValue
    ) {
        return ItemStackSnapshotArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link ItemStackSnapshotArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> optional(
            final @NonNull String name,
            final @NonNull ItemStack defaultValue
    ) {
        return ItemStackSnapshotArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link ItemStackSnapshotArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> optional(
            final @NonNull String name,
            final @NonNull ItemType defaultValue
    ) {
        return ItemStackSnapshotArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link ItemStackSnapshotArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link ItemStackSnapshotArgument}
     */
    public static <C> @NonNull ItemStackSnapshotArgument<C> optional(
            final @NonNull String name,
            final @NonNull DefaultedRegistryReference<ItemType> defaultValue
    ) {
        return ItemStackSnapshotArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
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
     * Parser for {@link ItemStackSnapshot ItemStackSnapshots} from an {@link ItemType} identifier and
     * optional NBT data. The stack size of the resulting snapshot will always be {@code 1}.
     *
     * @param <C> sender type
     */
    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, ItemStackSnapshot> {

        private final ArgumentParser<C, ItemStackSnapshot> mappedParser =
                new WrappedBrigadierParser<C, ItemInput>(ItemArgument.item()).map((ctx, itemInput) -> {
                    try {
                        return ArgumentParseResult.success(
                                ((ItemStack) (Object) itemInput.createItemStack(1, true)).createSnapshot()
                        );
                    } catch (final CommandSyntaxException ex) {
                        return ArgumentParseResult.failure(ex);
                    }
                });

        @Override
        public @NonNull ArgumentParseResult<@NonNull ItemStackSnapshot> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            return this.mappedParser.parse(commandContext, inputQueue);
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return ClientCompletionKeys.ITEM_STACK.get().createNode();
        }

    }

    /**
     * Builder for {@link ItemStackSnapshotArgument}.
     *
     * @param <C> sender type
     */
    public static final class Builder<C> extends TypedBuilder<C, ItemStackSnapshot, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ItemStackSnapshot.class, name);
        }

        @Override
        public @NonNull ItemStackSnapshotArgument<C> build() {
            return new ItemStackSnapshotArgument<>(
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
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull ItemType defaultValue) {
            return this.asOptionalWithDefault(ItemStack.of(defaultValue));
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull DefaultedRegistryReference<ItemType> defaultValue) {
            return this.asOptionalWithDefault(defaultValue.get());
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull ItemStackSnapshot defaultValue) {
            return this.asOptionalWithDefault(defaultValue.createStack());
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        @SuppressWarnings("ConstantConditions")
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull ItemStack defaultValue) {
            final net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack) (Object) defaultValue;
            return this.asOptionalWithDefault(new ItemInput(stack.getItem(), stack.getTag()).serialize());
        }

    }

}
