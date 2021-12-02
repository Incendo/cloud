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
import cloud.commandframework.bukkit.data.BlockPredicate;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.internal.MinecraftArgumentTypes;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.arguments.ArgumentType;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument type for parsing a {@link BlockPredicate}.
 *
 * <p>This argument type is only usable on Minecraft 1.13+, as it depends on Minecraft internals added in that version.</p>
 *
 * <p>This argument type only provides basic suggestions by default. Enabling Brigadier compatibility through
 * {@link BukkitCommandManager#registerBrigadier()} will allow client side validation and suggestions to be utilized.</p>
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public final class BlockPredicateArgument<C> extends CommandArgument<C, BlockPredicate> {

    private BlockPredicateArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new Parser<>(), defaultValue, BlockPredicate.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> BlockPredicateArgument.@NonNull Builder<C> builder(final @NonNull String name) {
        return new BlockPredicateArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link BlockPredicateArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockPredicateArgument<C> of(final @NonNull String name) {
        return BlockPredicateArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link BlockPredicateArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockPredicateArgument<C> optional(final @NonNull String name) {
        return BlockPredicateArgument.<C>builder(name).asOptional().build();
    }


    /**
     * Builder for {@link BlockPredicateArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, BlockPredicate, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(BlockPredicate.class, name);
        }

        @Override
        public @NonNull BlockPredicateArgument<C> build() {
            return new BlockPredicateArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    /**
     * Parser for {@link BlockPredicateArgument}. Only supported on Minecraft 1.13 and newer CraftBukkit based servers.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Parser<C> implements ArgumentParser<C, BlockPredicate> {

        private static final Class<?> TAG_CONTAINER_CLASS;

        static {
            if (CraftBukkitReflection.MAJOR_REVISION > 12 && CraftBukkitReflection.MAJOR_REVISION < 16) {
                TAG_CONTAINER_CLASS = CraftBukkitReflection.needNMSClass("TagRegistry");
            } else {
                TAG_CONTAINER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                        () -> "Couldn't find TagContainer class",
                        CraftBukkitReflection.findNMSClass("ITagRegistry"),
                        CraftBukkitReflection.findMCClass("tags.ITagRegistry"),
                        CraftBukkitReflection.findMCClass("tags.TagContainer")
                );
            }
        }

        private static final Class<?> CRAFT_WORLD_CLASS = CraftBukkitReflection.needOBCClass("CraftWorld");
        private static final Class<?> MINECRAFT_SERVER_CLASS = CraftBukkitReflection.needNMSClassOrElse(
                "MinecraftServer",
                "net.minecraft.server.MinecraftServer"
        );
        private static final Class<?> COMMAND_LISTENER_WRAPPER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find CommandSourceStack class",
                CraftBukkitReflection.findNMSClass("CommandListenerWrapper"),
                CraftBukkitReflection.findMCClass("commands.CommandListenerWrapper"),
                CraftBukkitReflection.findMCClass("commands.CommandSourceStack")
        );
        private static final Class<?> ARGUMENT_BLOCK_PREDICATE_CLASS =
                MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("block_predicate"));
        private static final Class<?> ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find BlockPredicateArgument$Result class",
                CraftBukkitReflection.findNMSClass("ArgumentBlockPredicate$b"),
                CraftBukkitReflection.findMCClass("commands.arguments.blocks.ArgumentBlockPredicate$b"),
                CraftBukkitReflection.findMCClass("commands.arguments.blocks.BlockPredicateArgument$Result")
        );
        private static final Class<?> SHAPE_DETECTOR_BLOCK_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find BlockInWorld class",
                CraftBukkitReflection.findNMSClass("ShapeDetectorBlock"),
                CraftBukkitReflection.findMCClass("world.level.block.state.pattern.ShapeDetectorBlock"),
                CraftBukkitReflection.findMCClass("world.level.block.state.pattern.BlockInWorld")
        );
        private static final Class<?> LEVEL_READER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find LevelReader class",
                CraftBukkitReflection.findNMSClass("IWorldReader"),
                CraftBukkitReflection.findMCClass("world.level.IWorldReader"),
                CraftBukkitReflection.findMCClass("world.level.LevelReader")
        );
        private static final Class<?> BLOCK_POSITION_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "Couldn't find BlockPos class",
                CraftBukkitReflection.findNMSClass("BlockPosition"),
                CraftBukkitReflection.findMCClass("core.BlockPosition"),
                CraftBukkitReflection.findMCClass("core.BlockPos")
        );
        private static final Constructor<?> BLOCK_POSITION_CTR =
                CraftBukkitReflection.needConstructor(BLOCK_POSITION_CLASS, int.class, int.class, int.class);
        private static final Constructor<?> SHAPE_DETECTOR_BLOCK_CTR = CraftBukkitReflection
                .needConstructor(SHAPE_DETECTOR_BLOCK_CLASS, LEVEL_READER_CLASS, BLOCK_POSITION_CLASS, boolean.class);
        private static final Method GET_HANDLE_METHOD = CraftBukkitReflection.needMethod(CRAFT_WORLD_CLASS, "getHandle");
        private static final Method CREATE_PREDICATE_METHOD = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "create on BlockPredicateArgument$Result",
                CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "create", TAG_CONTAINER_CLASS),
                CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "a", TAG_CONTAINER_CLASS)
        );
        private static final Method GET_SERVER_METHOD = CraftBukkitReflection.streamMethods(COMMAND_LISTENER_WRAPPER_CLASS)
                .filter(it -> it.getReturnType().equals(MINECRAFT_SERVER_CLASS) && it.getParameterCount() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find CommandSourceStack#getServer."));
        private static final Method GET_TAG_REGISTRY_METHOD = CraftBukkitReflection.firstNonNullOrThrow(
                () -> "getTags method on MinecraftServer",
                CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTagRegistry"),
                CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTags"),
                CraftBukkitReflection.streamMethods(MINECRAFT_SERVER_CLASS)
                        .filter(it -> it.getReturnType().equals(TAG_CONTAINER_CLASS) && it.getParameterCount() == 0)
                        .findFirst()
                        .orElse(null)
        );

        private final ArgumentParser<C, BlockPredicate> parser;

        /**
         * Create a new {@link Parser}.
         *
         * @since 1.5.0
         */
        public Parser() {
            try {
                this.parser = this.createParser();
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to initialize BlockPredicate parser.", ex);
            }
        }

        @SuppressWarnings("unchecked")
        private ArgumentParser<C, BlockPredicate> createParser() throws ReflectiveOperationException {
            return new WrappedBrigadierParser<C, Object>(
                    (ArgumentType<Object>) ARGUMENT_BLOCK_PREDICATE_CLASS.getConstructor().newInstance()
            ).map((ctx, result) -> {
                final Object commandSourceStack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
                try {
                    final Object server = GET_SERVER_METHOD.invoke(commandSourceStack);
                    final Object tagRegistry = GET_TAG_REGISTRY_METHOD.invoke(server);
                    final Predicate<Object> predicate = (Predicate<Object>) CREATE_PREDICATE_METHOD.invoke(result, tagRegistry);
                    return ArgumentParseResult.success(new BlockPredicateImpl(predicate));
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull BlockPredicate> parse(
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

        private static final class BlockPredicateImpl implements BlockPredicate {

            private final Predicate<Object> predicate;

            BlockPredicateImpl(final @NonNull Predicate<Object> predicate) {
                this.predicate = predicate;
            }

            private boolean testImpl(final @NonNull Block block, final boolean loadChunks) {
                try {
                    final Object blockInWorld = SHAPE_DETECTOR_BLOCK_CTR.newInstance(
                            GET_HANDLE_METHOD.invoke(block.getWorld()),
                            BLOCK_POSITION_CTR.newInstance(block.getX(), block.getY(), block.getZ()),
                            loadChunks
                    );
                    return this.predicate.test(blockInWorld);
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public boolean test(final @NonNull Block block) {
                return this.testImpl(block, false);
            }

            @Override
            public @NonNull BlockPredicate loadChunks() {
                return new BlockPredicate() {
                    @Override
                    public @NonNull BlockPredicate loadChunks() {
                        return this;
                    }

                    @Override
                    public boolean test(final Block block) {
                        return BlockPredicateImpl.this.testImpl(block, true);
                    }
                };
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
        commandManager.getParserRegistry()
                .registerParserSupplier(TypeToken.get(BlockPredicate.class), params -> new BlockPredicateArgument.Parser<>());
    }

}
