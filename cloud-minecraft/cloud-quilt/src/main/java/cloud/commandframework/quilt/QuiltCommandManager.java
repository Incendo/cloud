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

package cloud.commandframework.quilt;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.quilt.argument.QuiltArgumentParsers;
import cloud.commandframework.quilt.argument.RegistryEntryArgument;
import cloud.commandframework.quilt.argument.TeamArgument;
import cloud.commandframework.quilt.data.MinecraftTime;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.permission.PredicatePermission;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemEnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.MobEffectArgumentType;
import net.minecraft.command.argument.NbtCompoundTagArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtTagArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.ObjectiveCriteriaArgumentType;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.command.argument.ParticleArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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
 * @see QuiltServerCommandManager for server commands
 * @since 1.5.0
 */
public abstract class QuiltCommandManager<C, S extends CommandSource> extends CommandManager<C> implements
        BrigadierManagerHolder<C> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MOD_PUBLIC_STATIC_FINAL = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

    private final Function<S, C> commandSourceMapper;
    private final Function<C, S> backwardsCommandSourceMapper;
    private final CloudBrigadierManager<C, S> brigadierManager;


    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link AsynchronousCommandExecutionCoordinator}
     * @param commandSourceMapper          Function that maps {@link CommandSource} to the command sender type
     * @param backwardsCommandSourceMapper Function that maps the command sender type to {@link CommandSource}
     * @param registrationHandler          the handler accepting command registrations
     * @param dummyCommandSourceProvider   a provider of a dummy command source, for use with brigadier registration
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    QuiltCommandManager(
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<S, C> commandSourceMapper,
            final @NonNull Function<C, S> backwardsCommandSourceMapper,
            final @NonNull QuiltCommandRegistrationHandler<C, S> registrationHandler,
            final @NonNull Supplier<S> dummyCommandSourceProvider
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
        this.brigadierManager.brigadierSenderMapper(this.commandSourceMapper);
        this.registerNativeBrigadierMappings(this.brigadierManager);
        this.setCaptionRegistry(new QuiltCaptionRegistry<>());
        this.registerCommandPreProcessor(new QuiltCommandPreprocessor<>(this));

        ((QuiltCommandRegistrationHandler<C, S>) this.getCommandRegistrationHandler()).initialize(this);
    }

    private void registerNativeBrigadierMappings(final @NonNull CloudBrigadierManager<C, S> brigadier) {
        /* Cloud-native argument types */
        brigadier.registerMapping(new TypeToken<UUIDArgument.UUIDParser<C>>() {
        }, builder -> builder.toConstant(UuidArgumentType.uuid()));
        this.registerRegistryEntryMappings();
        brigadier.registerMapping(new TypeToken<TeamArgument.TeamParser<C>>() {
        }, builder -> builder.toConstant(TeamArgumentType.team()));
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Team.class), params -> new TeamArgument.TeamParser<>());

        /* Wrapped/Constant Brigadier types, native value type */
        this.registerConstantNativeParserSupplier(Formatting.class, ColorArgumentType.color());
        this.registerConstantNativeParserSupplier(CompoundTag.class, NbtCompoundTagArgumentType.nbtCompound());
        this.registerConstantNativeParserSupplier(Tag.class, NbtTagArgumentType.nbtTag());
        this.registerConstantNativeParserSupplier(NbtPathArgumentType.NbtPath.class, NbtPathArgumentType.nbtPath());
        this.registerConstantNativeParserSupplier(ScoreboardCriterion.class, ObjectiveCriteriaArgumentType.objectiveCriteria());
        this.registerConstantNativeParserSupplier(OperationArgumentType.Operation.class, OperationArgumentType.operation());
        this.registerConstantNativeParserSupplier(ParticleEffect.class, ParticleArgumentType.particle());
        this.registerConstantNativeParserSupplier(AngleArgumentType.Angle.class, AngleArgumentType.angle());
        this.registerConstantNativeParserSupplier(new TypeToken<EnumSet<Direction.Axis>>() {
        }, SwizzleArgumentType.swizzle());
        this.registerConstantNativeParserSupplier(Identifier.class, IdentifierArgumentType.identifier());
        this.registerConstantNativeParserSupplier(
                EntityAnchorArgumentType.EntityAnchor.class,
                EntityAnchorArgumentType.entityAnchor()
        );
        this.registerConstantNativeParserSupplier(NumberRange.IntRange.class, NumberRangeArgumentType.numberRange());
        this.registerConstantNativeParserSupplier(NumberRange.FloatRange.class, NumberRangeArgumentType.method_30918());
        this.registerConstantNativeParserSupplier(ItemStackArgument.class, ItemStackArgumentType.itemStack());

        /* Wrapped/Constant Brigadier types, mapped value type */
        this.registerConstantNativeParserSupplier(
                BlockPredicateArgumentType.BlockPredicate.class,
                BlockPredicateArgumentType.blockPredicate()
        );
        this.registerConstantNativeParserSupplier(MessageArgumentType.MessageFormat.class, MessageArgumentType.message());
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(MinecraftTime.class),
                params -> QuiltArgumentParsers.time()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerRegistryEntryMappings() {
        this.brigadierManager.registerMapping(
                new TypeToken<RegistryEntryArgument.RegistryEntryParser<C, ?>>() {
                },
                builder -> builder.to(argument -> {
                            /* several registries have specialized argument types, so let's use those where possible */
                            final RegistryKey<? extends Registry<?>> registry = argument.getRegistry();
                            if (registry.equals(Registry.ENTITY_TYPE_KEY)) {
                                return EntitySummonArgumentType.entitySummon();
                            } else if (registry.equals(Registry.ENCHANTMENT_KEY)) {
                                return ItemEnchantmentArgumentType.itemEnchantment();
                            } else if (registry.equals(Registry.MOB_EFFECT_KEY)) { // yarn wai
                                return MobEffectArgumentType.mobEffect();
                            } else if (registry.equals(Registry.DIMENSION)) {
                                return DimensionArgumentType.dimension();
                            }
                            return IdentifierArgumentType.identifier();
                        }
                ).suggestedBy((argument, useCloud) -> {
                    /* A few other registries have client-side suggestion providers but no argument type */
                    /* Type parameters are messed up here for some reason */
                    final RegistryKey<? extends Registry<?>> registry = argument.getRegistry();
                    if (registry.equals(Registry.SOUND_EVENT_KEY)) {
                        return (SuggestionProvider<S>) SuggestionProviders.AVAILABLE_SOUNDS;
                    } else if (registry.equals(Registry.BIOME_KEY)) {
                        return (SuggestionProvider<S>) SuggestionProviders.ALL_BIOMES;
                    } else if (registry.equals(Registry.ENTITY_TYPE_KEY)
                            || registry.equals(Registry.ENCHANTMENT_KEY)
                            || registry.equals(Registry.MOB_EFFECT_KEY)
                            || registry.equals(Registry.DIMENSION)) {
                        return null; /* for types with their own argument type, use Brigadier */
                    }
                    return useCloud; /* use cloud suggestions for anything else */
                })
        );

        /* Find all fields of RegistryKey<? extends Registry<?>> and register those */
        /* This only works for vanilla registries really, we'll have to do other things for non-vanilla ones */
        final Set<Class<?>> seenClasses = new HashSet<>();
        /* Some registries have types that are too generic... we'll skip those for now.
         * Eventually, these could be resolved by using ParserParameters in some way? */
        seenClasses.add(Identifier.class);
        seenClasses.add(Codec.class);
        for (final Field field : Registry.class.getDeclaredFields()) {
            if ((field.getModifiers() & MOD_PUBLIC_STATIC_FINAL) != MOD_PUBLIC_STATIC_FINAL) {
                continue;
            }
            if (!field.getType().equals(RegistryKey.class)) {
                continue;
            }

            final Type generic = field.getGenericType(); /* RegistryKey<? extends Registry<?>> */
            if (!(generic instanceof ParameterizedType)) {
                continue;
            }

            Type registryType = ((ParameterizedType) generic).getActualTypeArguments()[0];
            while (registryType instanceof WildcardType) {
                registryType = ((WildcardType) registryType).getUpperBounds()[0];
            }

            if (!(registryType instanceof ParameterizedType)) { /* expected: Registry<V> */
                continue;
            }

            final RegistryKey<?> key;
            try {
                key = (RegistryKey<?>) field.get(null);
            } catch (final IllegalAccessException ex) {
                LOGGER.warn("Failed to access value of registry key in field {} of type {}", field.getName(), generic, ex);
                continue;
            }

            final Type valueType = ((ParameterizedType) registryType).getActualTypeArguments()[0];
            if (seenClasses.contains(GenericTypeReflector.erase(valueType))) {
                LOGGER.debug("Encountered duplicate type in registry {}: type {}", key, valueType);
                continue;
            }
            seenClasses.add(GenericTypeReflector.erase(valueType));

            /* and now, finally, we can register */
            this.getParserRegistry().registerParserSupplier(
                    TypeToken.get(valueType),
                    params -> new RegistryEntryArgument.RegistryEntryParser(key)
            );
        }
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param type     the Java type to map
     * @param argument the Brigadier parser
     * @param <T>      value type
     * @since 1.5.0
     */
    final <T> void registerConstantNativeParserSupplier(final @NonNull Class<T> type, final @NonNull ArgumentType<T> argument) {
        this.registerConstantNativeParserSupplier(TypeToken.get(type), argument);
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param type     the Java type to map
     * @param argument the Brigadier parser
     * @param <T>      value type
     * @since 1.5.0
     */
    final <T> void registerConstantNativeParserSupplier(
            final @NonNull TypeToken<T> type,
            final @NonNull ArgumentType<T> argument
    ) {
        this.getParserRegistry().registerParserSupplier(type, params -> new WrappedBrigadierParser<>(argument));
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Gets the mapper from a game {@link CommandSource} to the manager's {@code C} type.
     *
     * @return Command source mapper
     * @since 1.5.0
     */
    public final @NonNull Function<@NonNull S, @NonNull C> getCommandSourceMapper() {
        return this.commandSourceMapper;
    }

    /**
     * Gets the mapper from the manager's {@code C} type to a game {@link CommandSource}.
     *
     * @return Command source mapper
     * @since 1.5.0
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
        this.lockRegistration();
    }

    /**
     * Get a permission predicate which passes when the sender has the specified permission level.
     *
     * @param permissionLevel permission level to require
     * @return a permission predicate
     * @since 1.5.0
     */
    public @NonNull PredicatePermission<C> permissionLevel(final int permissionLevel) {
        return sender -> this.getBackwardsCommandSourceMapper()
                .apply(sender)
                .hasPermissionLevel(permissionLevel);
    }

}
