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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument parsing an item identifier and optional extra NBT data into an {@link ItemInput}.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class ItemInputArgument<C> extends CommandArgument<C, ItemInput> {

    ItemInputArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                FabricArgumentParsers.contextual(ItemArgument::item),
                defaultValue,
                ItemInput.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link ItemInputArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ItemInputArgument<C> of(final @NonNull String name) {
        return ItemInputArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link ItemInputArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ItemInputArgument<C> optional(final @NonNull String name) {
        return ItemInputArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link ItemInputArgument} with the specified default value.
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ItemInputArgument<C> optional(final @NonNull String name, final @NonNull ItemStack defaultValue) {
        return ItemInputArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Builder for {@link ItemInputArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, ItemInput, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ItemInput.class, name);
        }

        /**
         * Build a new {@link ItemInputArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull ItemInputArgument<C> build() {
            return new ItemInputArgument<>(
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
         * @since 1.5.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull ItemStack defaultValue) {
            final String serializedDefault;
            if (defaultValue.hasTag()) {
                serializedDefault = Registry.ITEM.getKey(defaultValue.getItem()) + defaultValue.getTag().toString();
            } else {
                serializedDefault = Registry.ITEM.getKey(defaultValue.getItem()).toString();
            }
            return this.asOptionalWithDefault(serializedDefault);
        }
    }
}
