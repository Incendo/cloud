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
import cloud.commandframework.bukkit.data.ItemStackPredicate;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.internal.MinecraftArgumentTypes;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import io.leangen.geantyref.TypeToken;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Argument type for parsing an {@link ItemStackPredicate}.
 *
 * <p>This argument type is only usable on Minecraft 1.13+, as it depends on Minecraft internals added in that version.</p>
 *
 * <p>This argument type only provides basic suggestions by default. Enabling Brigadier compatibility through
 * {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public final class ItemStackPredicateArgument<C> extends CommandArgument<C, ItemStackPredicate> {

    private ItemStackPredicateArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new Parser<>(), defaultValue, ItemStackPredicate.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> ItemStackPredicateArgument.@NonNull Builder<C> builder(final @NonNull String name) {
        return new ItemStackPredicateArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link ItemStackPredicateArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ItemStackPredicateArgument<C> of(final @NonNull String name) {
        return ItemStackPredicateArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link ItemStackPredicateArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ItemStackPredicateArgument<C> optional(final @NonNull String name) {
        return ItemStackPredicateArgument.<C>builder(name).asOptional().build();
    }


    /**
     * Builder for {@link ItemStackPredicateArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, ItemStackPredicate, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(ItemStackPredicate.class, name);
        }

        @Override
        public @NonNull ItemStackPredicateArgument<C> build() {
            return new ItemStackPredicateArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    /**
     * Parser for {@link ItemStackPredicateArgument}. Only supported on Minecraft 1.13 and newer CraftBukkit based servers.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Parser<C> implements ArgumentParser<C, ItemStackPredicate> {

        private static final Class<?> CRAFT_ITEM_STACK_CLASS =
                CraftBukkitReflection.needOBCClass("inventory.CraftItemStack");
        private static final Class<?> ARGUMENT_ITEM_PREDICATE_CLASS =
                MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("item_predicate"));
        private static final Class<?> ARGUMENT_ITEM_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find ItemPredicateArgument$Result class",
                CraftBukkitReflection.findNMSClass("ArgumentItemPredicate$b"),
                CraftBukkitReflection.findMCClass("commands.arguments.item.ArgumentItemPredicate$b"),
                CraftBukkitReflection.findMCClass("commands.arguments.item.ItemPredicateArgument$Result")
        );
        private static final Method CREATE_PREDICATE_METHOD = CraftBukkitReflection.needMethod(
                ARGUMENT_ITEM_PREDICATE_RESULT_CLASS,
                "create",
                com.mojang.brigadier.context.CommandContext.class
        );
        private static final Method AS_NMS_COPY_METHOD =
                CraftBukkitReflection.needMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);

        private final ArgumentParser<C, ItemStackPredicate> parser;

        /**
         * Create a new {@link Parser}.
         *
         * @since 1.5.0
         */
        public Parser() {
            try {
                this.parser = this.createParser();
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to initialize ItemPredicate parser.", ex);
            }
        }

        @SuppressWarnings("unchecked")
        private ArgumentParser<C, ItemStackPredicate> createParser() throws ReflectiveOperationException {
            return new WrappedBrigadierParser<C, Object>(
                    (ArgumentType<Object>) ARGUMENT_ITEM_PREDICATE_CLASS.getConstructor().newInstance()
            ).map((ctx, result) -> {
                final Object commandSourceStack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
                final com.mojang.brigadier.context.CommandContext<Object> dummy = createDummyContext(ctx, commandSourceStack);
                try {
                    final Predicate<Object> predicate = (Predicate<Object>) CREATE_PREDICATE_METHOD.invoke(result, dummy);
                    return ArgumentParseResult.success(new ItemStackPredicateImpl(predicate));
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        private static <C> com.mojang.brigadier.context.@NonNull CommandContext<Object> createDummyContext(
                final @NonNull CommandContext<C> ctx,
                final @NonNull Object commandSourceStack
        ) {
            return new com.mojang.brigadier.context.CommandContext<>(
                    commandSourceStack,
                    ctx.getRawInputJoined(),
                    Collections.emptyMap(),
                    null,
                    null,
                    Collections.emptyList(),
                    StringRange.at(0),
                    null,
                    null,
                    false
            );
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull ItemStackPredicate> parse(
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

        private static final class ItemStackPredicateImpl implements ItemStackPredicate {

            private final Predicate<Object> predicate;

            ItemStackPredicateImpl(final @NonNull Predicate<Object> predicate) {
                this.predicate = predicate;
            }

            @Override
            public boolean test(final @NonNull ItemStack itemStack) {
                try {
                    return this.predicate.test(AS_NMS_COPY_METHOD.invoke(null, itemStack));
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }

    }

    /**
     * Called reflectively by {@link BukkitCommandManager}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull BukkitCommandManager<C> commandManager) {
        commandManager.getParserRegistry().registerParserSupplier(
                TypeToken.get(ItemStackPredicate.class),
                params -> new ItemStackPredicateArgument.Parser<>()
        );
    }

}
