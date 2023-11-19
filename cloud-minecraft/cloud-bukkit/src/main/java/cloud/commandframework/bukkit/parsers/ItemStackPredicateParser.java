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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.data.ItemStackPredicate;
import cloud.commandframework.bukkit.internal.CommandBuildContextSupplier;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.internal.MinecraftArgumentTypes;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parser for {@link ItemStackPredicate}.
 *
 * <p>This argument type is only usable on Minecraft 1.13+, as it depends on Minecraft internals added in that version.</p>
 *
 * <p>This argument type only provides basic suggestions by default. Enabling Brigadier compatibility through
 * {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public final class ItemStackPredicateParser<C> implements ArgumentParser<C, ItemStackPredicate> {

    private static final Class<?> CRAFT_ITEM_STACK_CLASS =
            CraftBukkitReflection.needOBCClass("inventory.CraftItemStack");
    private static final Class<?> ARGUMENT_ITEM_PREDICATE_CLASS =
            MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("item_predicate"));
    private static final Class<?> ARGUMENT_ITEM_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrNull(
            CraftBukkitReflection.findNMSClass("ArgumentItemPredicate$b"),
            CraftBukkitReflection.findMCClass("commands.arguments.item.ArgumentItemPredicate$b"),
            CraftBukkitReflection.findMCClass("commands.arguments.item.ItemPredicateArgument$Result")
    );
    private static final @Nullable Method CREATE_PREDICATE_METHOD = ARGUMENT_ITEM_PREDICATE_RESULT_CLASS == null
            ? null
            : CraftBukkitReflection.firstNonNullOrNull(
                    CraftBukkitReflection.findMethod(
                            ARGUMENT_ITEM_PREDICATE_RESULT_CLASS,
                            "create",
                            com.mojang.brigadier.context.CommandContext.class
                    ),
                    CraftBukkitReflection.findMethod(
                            ARGUMENT_ITEM_PREDICATE_RESULT_CLASS,
                            "a",
                            com.mojang.brigadier.context.CommandContext.class
                    )
            );
    private static final Method AS_NMS_COPY_METHOD =
            CraftBukkitReflection.needMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);

    /**
     * Creates a new item stack predicate parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, ItemStackPredicate> itemStackPredicateParser() {
        return ParserDescriptor.of(new ItemStackPredicateParser<>(), ItemStackPredicate.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #itemStackPredicateParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, ItemStackPredicate> itemStackPredicateComponent() {
        return CommandComponent.<C, ItemStackPredicate>builder().parser(itemStackPredicateParser());
    }

    private final ArgumentParser<C, ItemStackPredicate> parser;

    /**
     * Create a new {@link ItemStackPredicateParser}.
     *
     * @since 1.5.0
     */
    public ItemStackPredicateParser() {
        try {
            this.parser = this.createParser();
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to initialize ItemPredicate parser.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private ArgumentParser<C, ItemStackPredicate> createParser() throws ReflectiveOperationException {
        final Constructor<?> ctr = ARGUMENT_ITEM_PREDICATE_CLASS.getDeclaredConstructors()[0];
        final ArgumentType<Object> inst;
        if (ctr.getParameterCount() == 0) {
            inst = (ArgumentType<Object>) ctr.newInstance();
        } else {
            // 1.19+
            inst = (ArgumentType<Object>) ctr.newInstance(CommandBuildContextSupplier.commandBuildContext());
        }
        return new WrappedBrigadierParser<C, Object>(inst).map((ctx, result) -> {
            if (result instanceof Predicate) {
                // 1.19+
                return ArgumentParseResult.success(new ItemStackPredicateImpl((Predicate<Object>) result));
            }
            final Object commandSourceStack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
            final com.mojang.brigadier.context.CommandContext<Object> dummy = createDummyContext(ctx, commandSourceStack);
            Objects.requireNonNull(CREATE_PREDICATE_METHOD, "ItemPredicateArgument$Result#create");
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

    /**
     * Called reflectively by {@link BukkitCommandManager}.
     *
     * @param commandManager command manager
     * @param <C>            sender type
     */
    @SuppressWarnings("unused")
    private static <C> void registerParserSupplier(final @NonNull BukkitCommandManager<C> commandManager) {
        commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(ItemStackPredicate.class),
                params -> new ItemStackPredicateParser<>()
        );
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ItemStackPredicate> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final CommandInput commandInput
    ) {
        return this.parser.parse(commandContext, commandInput);
    }

    @Override
    public @NonNull List<@NonNull Suggestion> suggestions(
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
