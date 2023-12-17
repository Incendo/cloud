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
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.bukkit.internal.CommandBuildContextSupplier;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.internal.MinecraftArgumentTypes;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Parser for a {@link Material} and optional extra NBT data into a {@link ProtoItemStack}.
 *
 * <p>This parser only provides basic suggestions by default. On Minecraft 1.13 and newer, enabling Brigadier
 * compatibility through {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and
 * suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public class ItemStackParser<C> implements ArgumentParser<C, ProtoItemStack> {

    /**
     * Creates a new item stack parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, ProtoItemStack> itemStackParser() {
        return ParserDescriptor.of(new ItemStackParser<>(), ProtoItemStack.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #itemStackParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, ProtoItemStack> itemStackComponent() {
        return CommandComponent.<C, ProtoItemStack>builder().parser(itemStackParser());
    }

    private final ArgumentParser<C, ProtoItemStack> parser;

    private static @Nullable Class<?> findItemInputClass() {
        final Class<?>[] classes = new Class<?>[]{
                CraftBukkitReflection.findNMSClass("ArgumentPredicateItemStack"),
                CraftBukkitReflection.findMCClass("commands.arguments.item.ArgumentPredicateItemStack"),
                CraftBukkitReflection.findMCClass("commands.arguments.item.ItemInput")
        };
        for (final Class<?> clazz : classes) {
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Create a new {@link ItemStackParser}.
     *
     * @since 1.5.0
     */
    public ItemStackParser() {
        if (findItemInputClass() != null) {
            this.parser = new ModernParser<>();
        } else {
            this.parser = new LegacyParser<>();
        }
    }

    @Override
    public final @NonNull ArgumentParseResult<ProtoItemStack> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return this.parser.parse(commandContext, commandInput);
    }

    @Override
    public final @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.parser.suggestionProvider();
    }


    private static final class ModernParser<C> implements ArgumentParser<C, ProtoItemStack> {

        private static final Class<?> NMS_ITEM_STACK_CLASS = CraftBukkitReflection.needNMSClassOrElse(
                "ItemStack",
                "net.minecraft.world.item.ItemStack"
        );
        private static final Class<?> CRAFT_ITEM_STACK_CLASS =
                CraftBukkitReflection.needOBCClass("inventory.CraftItemStack");
        private static final Class<?> ARGUMENT_ITEM_STACK_CLASS =
                MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("item_stack"));
        private static final Class<?> ITEM_INPUT_CLASS = requireNonNull(findItemInputClass(), "ItemInput class");
        private static final Class<?> NMS_ITEM_CLASS = CraftBukkitReflection.needNMSClassOrElse(
                "Item",
                "net.minecraft.world.item.Item"
        );
        private static final Class<?> CRAFT_MAGIC_NUMBERS_CLASS =
                CraftBukkitReflection.needOBCClass("util.CraftMagicNumbers");
        private static final Method GET_MATERIAL_METHOD = CraftBukkitReflection
                .needMethod(CRAFT_MAGIC_NUMBERS_CLASS, "getMaterial", NMS_ITEM_CLASS);
        private static final Method CREATE_ITEM_STACK_METHOD = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find createItemStack method on ItemInput",
                CraftBukkitReflection.findMethod(ITEM_INPUT_CLASS, "a", int.class, boolean.class),
                CraftBukkitReflection.findMethod(ITEM_INPUT_CLASS, "createItemStack", int.class, boolean.class)
        );
        private static final Method AS_BUKKIT_COPY_METHOD = CraftBukkitReflection
                .needMethod(CRAFT_ITEM_STACK_CLASS, "asBukkitCopy", NMS_ITEM_STACK_CLASS);
        private static final Field ITEM_FIELD = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find item field on ItemInput",
                CraftBukkitReflection.findField(ITEM_INPUT_CLASS, "b"),
                CraftBukkitReflection.findField(ITEM_INPUT_CLASS, "item")
        );
        private static final Field COMPOUND_TAG_FIELD = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find tag field on ItemInput",
                CraftBukkitReflection.findField(ITEM_INPUT_CLASS, "c"),
                CraftBukkitReflection.findField(ITEM_INPUT_CLASS, "tag")
        );
        private static final Class<?> HOLDER_CLASS = CraftBukkitReflection.findMCClass("core.Holder");
        private static final @Nullable Method VALUE_METHOD = HOLDER_CLASS == null
                ? null
                : CraftBukkitReflection.firstNonNullOrThrow(
                        () -> "Couldn't find Holder#value",
                        CraftBukkitReflection.findMethod(HOLDER_CLASS, "value"),
                        CraftBukkitReflection.findMethod(HOLDER_CLASS, "a")
                );

        private final ArgumentParser<C, ProtoItemStack> parser;

        ModernParser() {
            try {
                this.parser = this.createParser();
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to initialize modern ItemStack parser.", ex);
            }
        }

        @SuppressWarnings("unchecked")
        private ArgumentParser<C, ProtoItemStack> createParser() throws ReflectiveOperationException {
            final Constructor<?> ctr = ARGUMENT_ITEM_STACK_CLASS.getDeclaredConstructors()[0];
            final ArgumentType<Object> inst;
            if (ctr.getParameterCount() == 0) {
                inst = (ArgumentType<Object>) ctr.newInstance();
            } else {
                // 1.19+
                inst = (ArgumentType<Object>) ctr.newInstance(CommandBuildContextSupplier.commandBuildContext());
            }
            return new WrappedBrigadierParser<C, Object>(inst)
                    .map((ctx, itemInput) -> CompletableFuture.completedFuture(new ModernProtoItemStack(itemInput)));
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull ProtoItemStack> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            // Minecraft has a parser for this - just use it
            return this.parser.parse(commandContext, commandInput);
        }

        @Override
        public @NonNull SuggestionProvider<C> suggestionProvider() {
            return this.parser.suggestionProvider();
        }


        private static final class ModernProtoItemStack implements ProtoItemStack {

            private final Object itemInput;
            private final Material material;
            private final @Nullable String snbt;

            ModernProtoItemStack(final @NonNull Object itemInput) {
                this.itemInput = itemInput;
                try {
                    Object item = ITEM_FIELD.get(itemInput);
                    if (HOLDER_CLASS != null && HOLDER_CLASS.isInstance(item)) {
                        item = VALUE_METHOD.invoke(item);
                    }
                    this.material = (Material) GET_MATERIAL_METHOD.invoke(null, item);
                    final Object compoundTag = COMPOUND_TAG_FIELD.get(itemInput);
                    if (compoundTag != null) {
                        this.snbt = compoundTag.toString();
                    } else {
                        this.snbt = null;
                    }
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public @NonNull Material material() {
                return this.material;
            }

            @Override
            public boolean hasExtraData() {
                return this.snbt != null;
            }

            @Override
            public @NonNull ItemStack createItemStack(final int stackSize, final boolean respectMaximumStackSize) {
                try {
                    return (ItemStack) AS_BUKKIT_COPY_METHOD.invoke(
                            null,
                            CREATE_ITEM_STACK_METHOD.invoke(this.itemInput, stackSize, respectMaximumStackSize)
                    );
                } catch (final InvocationTargetException ex) {
                    final Throwable cause = ex.getCause();
                    if (cause instanceof CommandSyntaxException) {
                        throw new IllegalArgumentException(cause.getMessage(), cause);
                    }
                    throw new RuntimeException(ex);
                } catch (final ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class LegacyParser<C> implements ArgumentParser<C, ProtoItemStack>,
            BlockingSuggestionProvider.Strings<C> {

        private final ArgumentParser<C, ProtoItemStack> parser = new MaterialParser<C>()
                .map((ctx, material) -> CompletableFuture.completedFuture(new LegacyProtoItemStack(material)));

        @Override
        public @NonNull ArgumentParseResult<@NonNull ProtoItemStack> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            return this.parser.parse(commandContext, commandInput);
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .map(value -> value.name().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());
        }

        private static final class LegacyProtoItemStack implements ProtoItemStack {

            private final Material material;

            private LegacyProtoItemStack(final @NonNull Material material) {
                this.material = material;
            }

            @Override
            public @NonNull Material material() {
                return this.material;
            }

            @Override
            public boolean hasExtraData() {
                return false;
            }

            @Override
            public @NonNull ItemStack createItemStack(final int stackSize, final boolean respectMaximumStackSize)
                    throws IllegalArgumentException {
                if (respectMaximumStackSize && stackSize > this.material.getMaxStackSize()) {
                    throw new IllegalArgumentException(String.format(
                            "The maximum stack size for %s is %d",
                            this.material,
                            this.material.getMaxStackSize()
                    ));
                }
                return new ItemStack(this.material, stackSize);
            }
        }
    }
}
