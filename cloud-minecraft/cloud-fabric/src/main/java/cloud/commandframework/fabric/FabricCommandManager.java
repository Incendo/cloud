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
package cloud.commandframework.fabric;

import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.arguments.standard.UUIDParser;
import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.fabric.argument.FabricVanillaArgumentParsers;
import cloud.commandframework.fabric.argument.RegistryEntryParser;
import cloud.commandframework.fabric.argument.TeamParser;
import cloud.commandframework.fabric.data.MinecraftTime;
import cloud.commandframework.permission.PredicatePermission;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
 * @since 1.5.0
 */
public abstract class FabricCommandManager<C, S extends SharedSuggestionProvider> extends CommandManager<C> implements
        BrigadierManagerHolder<C, S>, SenderMapperHolder<S, C> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MOD_PUBLIC_STATIC_FINAL = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

    private static final Component NEWLINE = Component.literal("\n");
    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";


    private final SenderMapper<S, C> senderMapper;
    private final CloudBrigadierManager<C, S> brigadierManager;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;
    private final FabricExceptionHandlerFactory<C, S> exceptionHandlerFactory = new FabricExceptionHandlerFactory<>(this);


    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution coordinator
     *                                     for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in that particular platform are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#asyncCoordinator()}
     * @param senderMapper                 Function that maps {@link SharedSuggestionProvider} to the command sender type
     * @param registrationHandler          the handler accepting command registrations
     * @param dummyCommandSourceProvider   a provider of a dummy command source, for use with brigadier registration
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings("unchecked")
    FabricCommandManager(
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<S, C> senderMapper,
            final @NonNull FabricCommandRegistrationHandler<C, S> registrationHandler,
            final @NonNull Supplier<S> dummyCommandSourceProvider
    ) {
        super(commandExecutionCoordinator, registrationHandler);
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);

        // We're always brigadier
        this.brigadierManager = new CloudBrigadierManager<>(
                this,
                () -> new CommandContext<>(
                        // This looks ugly, but it's what the server does when loading datapack functions in 1.16+
                        // See net.minecraft.server.function.FunctionLoader.reload for reference
                        this.senderMapper.map(dummyCommandSourceProvider.get()),
                        this
                ),
                this.suggestionFactory(),
                this.senderMapper
        );

        this.registerNativeBrigadierMappings(this.brigadierManager);
        this.captionRegistry(new FabricCaptionRegistry<>());
        this.registerCommandPreProcessor(new FabricCommandPreprocessor<>(this));
        this.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<C>startsWith(true).andTrimBeforeLastSpace()
        ));

        ((FabricCommandRegistrationHandler<C, S>) this.commandRegistrationHandler()).initialize(this);
    }

    private void registerNativeBrigadierMappings(final @NonNull CloudBrigadierManager<C, S> brigadier) {
        /* Cloud-native argument types */
        brigadier.registerMapping(new TypeToken<UUIDParser<C>>() {
        }, builder -> builder.toConstant(UuidArgument.uuid()));
        this.registerRegistryEntryMappings();
        brigadier.registerMapping(new TypeToken<TeamParser<C>>() {
        }, builder -> builder.toConstant(net.minecraft.commands.arguments.TeamArgument.team()));
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(PlayerTeam.class),
                params -> new TeamParser<>()
        );

        /* Wrapped/Constant Brigadier types, native value type */
        this.registerConstantNativeParserSupplier(ChatFormatting.class, ColorArgument.color());
        this.registerConstantNativeParserSupplier(CompoundTag.class, CompoundTagArgument.compoundTag());
        this.registerConstantNativeParserSupplier(Tag.class, NbtTagArgument.nbtTag());
        this.registerConstantNativeParserSupplier(NbtPathArgument.NbtPath.class, NbtPathArgument.nbtPath());
        this.registerConstantNativeParserSupplier(ObjectiveCriteria.class, ObjectiveCriteriaArgument.criteria());
        this.registerConstantNativeParserSupplier(OperationArgument.Operation.class, OperationArgument.operation());
        this.registerConstantNativeParserSupplier(AngleArgument.SingleAngle.class, AngleArgument.angle());
        this.registerConstantNativeParserSupplier(new TypeToken<EnumSet<Direction.Axis>>() {
        }, SwizzleArgument.swizzle());
        this.registerConstantNativeParserSupplier(ResourceLocation.class, ResourceLocationArgument.id());
        this.registerConstantNativeParserSupplier(EntityAnchorArgument.Anchor.class, EntityAnchorArgument.anchor());
        this.registerConstantNativeParserSupplier(MinMaxBounds.Ints.class, RangeArgument.intRange());
        this.registerConstantNativeParserSupplier(MinMaxBounds.Doubles.class, RangeArgument.floatRange());
        this.registerContextualNativeParserSupplier(ParticleOptions.class, ParticleArgument::particle);
        this.registerContextualNativeParserSupplier(ItemInput.class, ItemArgument::item);
        this.registerContextualNativeParserSupplier(BlockPredicateArgument.Result.class, BlockPredicateArgument::blockPredicate);

        /* Wrapped/Constant Brigadier types, mapped value type */
        this.registerConstantNativeParserSupplier(MessageArgument.Message.class, MessageArgument.message());
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MinecraftTime.class),
                params -> FabricVanillaArgumentParsers.<C>timeParser().parser()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerRegistryEntryMappings() {
        this.brigadierManager.registerMapping(
                new TypeToken<RegistryEntryParser<C, ?>>() {
                },
                builder -> {
                    builder.to(argument -> ResourceKeyArgument.key((ResourceKey) argument.registryKey()));
                }
        );

        /* Find all fields of RegistryKey<? extends Registry<?>> and register those */
        /* This only works for vanilla registries really, we'll have to do other things for non-vanilla ones */
        final Set<Class<?>> seenClasses = new HashSet<>();
        /* Some registries have types that are too generic... we'll skip those for now.
         * Eventually, these could be resolved by using ParserParameters in some way? */
        seenClasses.add(ResourceLocation.class);
        seenClasses.add(Codec.class);
        seenClasses.add(String.class); // avoid pottery pattern registry overriding default string parser
        for (final Field field : Registries.class.getDeclaredFields()) {
            if ((field.getModifiers() & MOD_PUBLIC_STATIC_FINAL) != MOD_PUBLIC_STATIC_FINAL) {
                continue;
            }
            if (!field.getType().equals(ResourceKey.class)) {
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

            final ResourceKey<?> key;
            try {
                key = (ResourceKey<?>) field.get(null);
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
            this.parserRegistry().registerParserSupplier(
                    TypeToken.get(valueType),
                    params -> new RegistryEntryParser(key)
            );
        }
    }

    /**
     * Register a parser supplier for a brigadier type that has no options and whose output can be directly used.
     *
     * @param type     the Java type to map
     * @param argument a function providing the Brigadier parser given a build context
     * @param <T>      value type
     * @since 1.7.0
     */
    final <T> void registerContextualNativeParserSupplier(
            final @NonNull Class<T> type,
            final @NonNull Function<CommandBuildContext, @NonNull ArgumentType<T>> argument
    ) {
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(type),
                params -> FabricVanillaArgumentParsers.<C, T>contextualParser(argument, type).parser()
        );
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
        this.parserRegistry().registerParserSupplier(type, params -> new WrappedBrigadierParser<>(argument));
    }

    @Override
    public final @NonNull SenderMapper<S, C> senderMapper() {
        return this.senderMapper;
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will always return true for {@link FabricCommandManager}s.</p>
     *
     * @return {@code true}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link FabricCommandManager}s always use Brigadier for registration, so the aforementioned check is not needed.</p>
     *
     * @return {@inheritDoc}
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
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
        return sender -> this.senderMapper()
                .reverse(sender)
                .hasPermission(permissionLevel);
    }

    protected final void registerDefaultExceptionHandlers(
            final @NonNull BiConsumer<@NonNull S, @NonNull Component> sendError,
            final @NonNull Function<@NonNull S, @NonNull String> getName
    ) {
        this.exceptionController().registerHandler(
                Throwable.class,
                this.exceptionHandlerFactory.createHandler((source, sender, throwable) -> {
                    sendError.accept(
                            source,
                            this.decorateHoverStacktrace(
                                    Component.literal(MESSAGE_INTERNAL_ERROR),
                                    throwable,
                                    sender
                            )
                    );
                    LOGGER.warn("Error occurred while executing command for user {}:", getName.apply(source), throwable);
                })
        ).registerHandler(
                CommandExecutionException.class,
                this.exceptionHandlerFactory.createHandler((source, sender, throwable) -> {
                    sendError.accept(
                            source,
                            this.decorateHoverStacktrace(
                                    Component.literal(MESSAGE_INTERNAL_ERROR),
                                    throwable.getCause(),
                                    sender
                            )
                    );
                    LOGGER.warn("Error occurred while executing command for user {}:", getName.apply(source), throwable);
                })
        ).registerHandler(
                ArgumentParseException.class,
                this.exceptionHandlerFactory.createHandler((source, sender, throwable) -> {
                    if (throwable.getCause() instanceof CommandSyntaxException) {
                        sendError.accept(source, Component.literal("Invalid Command Argument: ")
                                .append(Component.literal("")
                                        .append(ComponentUtils
                                                .fromMessage(((CommandSyntaxException) throwable.getCause()).getRawMessage()))
                                        .withStyle(ChatFormatting.GRAY)));
                    } else {
                        sendError.accept(source, Component.literal("Invalid Command Argument: ")
                                .append(Component.literal(throwable.getCause().getMessage())
                                        .withStyle(ChatFormatting.GRAY)));
                    }
                })
        ).registerHandler(NoSuchCommandException.class, this.exceptionHandlerFactory.createHandler(
                (source, sender, throwable) -> sendError.accept(source, Component.literal(MESSAGE_UNKNOWN_COMMAND))
        )).registerHandler(NoPermissionException.class, this.exceptionHandlerFactory.createHandler(
                (source, sender, throwable) -> sendError.accept(source, Component.literal(MESSAGE_NO_PERMS))
        )).registerHandler(InvalidCommandSenderException.class, this.exceptionHandlerFactory.createHandler(
                (source, sender, throwable) -> sendError.accept(source, Component.literal(throwable.getMessage()))
        )).registerHandler(InvalidSyntaxException.class, this.exceptionHandlerFactory.createHandler(
                (source, sender, throwable) -> sendError.accept(
                        source,
                        Component.literal("Invalid Command Syntax. Correct command syntax is: ")
                                .append(Component.literal(String.format("/%s", throwable.getCorrectSyntax()))
                                        .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                )
        ));
    }

    private @NonNull MutableComponent decorateHoverStacktrace(
            final @NonNull MutableComponent input,
            final @NonNull Throwable cause,
            final @NonNull C sender
    ) {
        if (!this.hasPermission(sender, "cloud.hover-stacktrace")) {
            return input;
        }

        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final String stackTrace = writer.toString().replace("\t", "    ");
        return input.withStyle(style -> style
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal(stackTrace)
                                .append(NEWLINE)
                                .append(Component.literal("    Click to copy")
                                        .withStyle(s2 -> s2.withColor(ChatFormatting.GRAY).withItalic(true)))
                ))
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        stackTrace
                )));
    }
}
