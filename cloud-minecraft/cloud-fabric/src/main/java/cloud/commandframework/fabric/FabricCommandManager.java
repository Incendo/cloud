//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

package cloud.commandframework.fabric;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.mojang.brigadier.arguments.ArgumentType;
import io.leangen.geantyref.TypeToken;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemEnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.MobEffectArgumentType;
import net.minecraft.command.argument.NbtCompoundTagArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.ObjectiveCriteriaArgumentType;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.command.argument.ParticleArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A command manager for either the server or client on Fabric.
 *
 * <p>Commands registered with managers of this type will be registered into a Brigadier command tree.</p>
 *
 * <p>Where possible, Vanilla argument types are made available in a cloud-friendly format. In some cases, these argument
 * types may only be available for server commands. Mod-provided argument types can be exposed to Cloud as well, by using
 * {@link WrappedBrigadierParser}.</p>
 *
 * @param <C> the manager's sender type
 * @param <S> the platform sender type
 * @see FabricServerCommandManager for server commands
 */
public abstract class FabricCommandManager<C, S extends CommandSource> extends CommandManager<C> implements BrigadierManagerHolder<C> {

    private final Function<S, C> commandSourceMapper;
    private final Function<C, S> backwardsCommandSourceMapper;
    private final CloudBrigadierManager<C, S> brigadierManager;


    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link AsynchronousCommandExecutionCoordinator}
     * @param commandSourceMapper          Function that maps {@link ServerCommandSource} to the command sender type
     * @param backwardsCommandSourceMapper Function that maps the command sender type to {@link ServerCommandSource}
     * @param registrationHandler the handler accepting command registrations
     * @param dummyCommandSourceProvider a provider of a dummy command source, for use with brigadier registration
     */
    @SuppressWarnings("unchecked")
    FabricCommandManager(
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final Function<S, C> commandSourceMapper,
            final Function<C, S> backwardsCommandSourceMapper,
            final FabricCommandRegistrationHandler<C, S> registrationHandler,
            final Supplier<S> dummyCommandSourceProvider
            ) {
        super(commandExecutionCoordinator, registrationHandler);
        this.commandSourceMapper = commandSourceMapper;
        this.backwardsCommandSourceMapper = backwardsCommandSourceMapper;

        // We're always brigadier
        this.brigadierManager = new CloudBrigadierManager<>(this, () -> new CommandContext<>(
                // This looks ugly, but it's what the server does when loading datapack functions in 1.16+
                // See net.minecraft.server.function.FunctionLoader.reload for reference
                this.commandSourceMapper.apply(dummyCommandSourceProvider.get()),
                this
                ));
        this.brigadierManager.backwardsBrigadierSenderMapper(this.backwardsCommandSourceMapper);
        this.registerNativeBrigadierMappings(this.brigadierManager);

        ((FabricCommandRegistrationHandler<C, S>) this.getCommandRegistrationHandler()).initialize(this);
    }

    private void registerNativeBrigadierMappings(final CloudBrigadierManager<C, S> brigadier) {
        /* Cloud-native argument types */
        brigadier.registerMapping(new TypeToken<UUIDArgument.UUIDParser<C>>() {}, false, cloud -> UuidArgumentType.uuid());

        /* Wrapped/Constant Brigadier types, native value type */
        this.registerConstantNativeParserSupplier(Formatting.class, ColorArgumentType.color());
        this.registerConstantNativeParserSupplier(BlockPredicateArgumentType.BlockPredicate.class,
                BlockPredicateArgumentType.blockPredicate());
        this.registerConstantNativeParserSupplier(MessageArgumentType.MessageFormat.class, MessageArgumentType.message());
        this.registerConstantNativeParserSupplier(CompoundTag.class, NbtCompoundTagArgumentType.nbtCompound());
        this.registerConstantNativeParserSupplier(NbtPathArgumentType.NbtPath.class, NbtPathArgumentType.nbtPath());
        this.registerConstantNativeParserSupplier(ScoreboardCriterion.class, ObjectiveCriteriaArgumentType.objectiveCriteria());
        this.registerConstantNativeParserSupplier(OperationArgumentType.Operation.class, OperationArgumentType.operation());
        this.registerConstantNativeParserSupplier(ParticleEffect.class, ParticleArgumentType.particle());
        this.registerConstantNativeParserSupplier(AngleArgumentType.Angle.class, AngleArgumentType.angle());
        this.registerConstantNativeParserSupplier(new TypeToken<EnumSet<Direction.Axis>>() {}, SwizzleArgumentType.swizzle());
        this.registerConstantNativeParserSupplier(Identifier.class, IdentifierArgumentType.identifier());
        this.registerConstantNativeParserSupplier(StatusEffect.class, MobEffectArgumentType.mobEffect());
        this.registerConstantNativeParserSupplier(EntityAnchorArgumentType.EntityAnchor.class, EntityAnchorArgumentType.entityAnchor());
        this.registerConstantNativeParserSupplier(NumberRange.IntRange.class, NumberRangeArgumentType.numberRange());
        this.registerConstantNativeParserSupplier(NumberRange.FloatRange.class, NumberRangeArgumentType.method_30918());
        this.registerConstantNativeParserSupplier(Enchantment.class, ItemEnchantmentArgumentType.itemEnchantment());
        // todo: can we add a compound argument -- MC `ItemStackArgument` is just type and tag, and count is separate
        this.registerConstantNativeParserSupplier(ItemStackArgument.class, ItemStackArgumentType.itemStack());

        /* Wrapped/Constant Brigadier types, mapped value type */
        /*this.registerConstantNativeParserSupplier(GameProfile.class, GameProfileArgumentType.gameProfile());
        this.registerConstantNativeParserSupplier(BlockPos.class, BlockPosArgumentType.blockPos());
        this.registerConstantNativeParserSupplier(ColumnPos.class, ColumnPosArgumentType.columnPos());
        this.registerConstantNativeParserSupplier(Vec3d.class, Vec3ArgumentType.vec3());
        this.registerConstantNativeParserSupplier(Vec2f.class, Vec2ArgumentType.vec2());
        this.registerConstantNativeParserSupplier(BlockState.class, BlockStateArgumentType.blockState());
        this.registerConstantNativeParserSupplier(ItemPredicate.class, ItemPredicateArgumentType.itemPredicate());
        this.registerConstantNativeParserSupplier(ScoreboardObjective.class, ObjectiveArgumentType.objective());
        this.registerConstantNativeParserSupplier(PosArgument.class, RotationArgumentType.rotation()); // todo: different type
        this.registerConstantNativeParserSupplier(ScoreboardSlotArgumentType.scoreboardSlot());
        this.registerConstantNativeParserSupplier(Team.class, TeamArgumentType.team());
        this.registerConstantNativeParserSupplier(/* slot *, ItemSlotArgumentType.itemSlot());
        this.registerConstantNativeParserSupplier(CommandFunction.class, FunctionArgumentType.function());
        this.registerConstantNativeParserSupplier(EntityType.class, EntitySummonArgumentType.entitySummon()); // entity summon
        this.registerConstantNativeParserSupplier(ServerWorld.class, DimensionArgumentType.dimension());
        this.registerConstantNativeParserSupplier(/* time representation in ticks *, TimeArgumentType.time());*/

        /* Wrapped brigadier requiring parameters */
        // score holder: single vs multiple
        // entity argument type: single or multiple, players or any entity -- returns EntitySelector, but do we want that?
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param type the Java type to map
     * @param argument the Brigadier parser
     * @param <T> value type
     */
    final <T> void registerConstantNativeParserSupplier(final Class<T> type, final ArgumentType<T> argument) {
        this.registerConstantNativeParserSupplier(TypeToken.get(type), argument);
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param type the Java type to map
     * @param argument the Brigadier parser
     * @param <T> value type
     */
    final <T> void registerConstantNativeParserSupplier(final TypeToken<T> type, final ArgumentType<T> argument) {
        this.getParserRegistry().registerParserSupplier(type, params -> new WrappedBrigadierParser<>(argument));
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Gets the mapper from a game {@link ServerCommandSource} to the manager's {@code C} type.
     *
     * @return Command source mapper
     */
    public final @NonNull Function<@NonNull S, @NonNull C> getCommandSourceMapper() {
        return this.commandSourceMapper;
    }

    /**
     * Gets the mapper from the manager's {@code C} type to a game {@link ServerCommandSource}.
     *
     * @return Command source mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull S> getBackwardsCommandSourceMapper() {
        return this.backwardsCommandSourceMapper;
    }

    @Override
    public final @NonNull CloudBrigadierManager<C, S> brigadierManager() {
        return this.brigadierManager;
    }

    /* transition state to prevent further registration */
    final void registrationCalled() {
        this.transitionOrThrow(RegistrationState.REGISTERING, RegistrationState.AFTER_REGISTRATION);
    }

}
