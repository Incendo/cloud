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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.internal.MinecraftArgumentTypes;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import com.google.common.base.Suppliers;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class SelectorUtils {

    private SelectorUtils() {
    }

    private static <C, T> @Nullable ArgumentParser<C, T> createModernParser(
            final boolean single,
            final boolean playersOnly,
            final SelectorMapper<T> mapper
    ) {
        if (CraftBukkitReflection.MAJOR_REVISION < 13) {
            return null;
        }
        final WrappedBrigadierParser<C, Object> wrappedBrigParser = new WrappedBrigadierParser<>(
                () -> createEntityArgument(single, playersOnly),
                ArgumentParser.DEFAULT_ARGUMENT_COUNT,
                EntityArgumentParseFunction.INSTANCE
        );
        return new ModernSelectorParser<>(wrappedBrigParser, mapper);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentType<Object> createEntityArgument(final boolean single, final boolean playersOnly) {
        final Constructor<?> constructor =
                MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("entity")).getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            return (ArgumentType<Object>) constructor.newInstance(single, playersOnly);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class EntityArgumentParseFunction implements WrappedBrigadierParser.ParseFunction<Object> {

        static final EntityArgumentParseFunction INSTANCE = new EntityArgumentParseFunction();

        @Override
        public Object apply(
                final ArgumentType<Object> type,
                final StringReader reader
        ) throws CommandSyntaxException {
            final @Nullable Method specialParse = CraftBukkitReflection.findMethod(
                    type.getClass(),
                    "parse",
                    StringReader.class,
                    boolean.class
            );
            if (specialParse == null) {
                return type.parse(reader);
            }
            try {
                return specialParse.invoke(
                        type,
                        reader,
                        true // CraftBukkit overridePermissions param
                );
            } catch (final InvocationTargetException ex) {
                final Throwable cause = ex.getCause();
                if (cause instanceof CommandSyntaxException) {
                    throw (CommandSyntaxException) cause;
                }
                throw new RuntimeException(ex);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @SuppressWarnings("unused") // errorprone false positive
    private abstract static class SelectorParser<C, T> implements ArgumentParser.FutureArgumentParser<C, T>, SelectorMapper<T>,
            SuggestionProvider<C> {

        protected static final Supplier<Object> NO_PLAYERS_EXCEPTION_TYPE =
                Suppliers.memoize(() -> findExceptionType("argument.entity.notfound.player"));
        protected static final Supplier<Object> NO_ENTITIES_EXCEPTION_TYPE =
                Suppliers.memoize(() -> findExceptionType("argument.entity.notfound.entity"));

        private final @Nullable ArgumentParser<C, T> modernParser;

        // Hide brigadier references in inner class
        protected static final class Thrower {

            private final Object type;

            Thrower(final Object simpleCommandExceptionType) {
                this.type = simpleCommandExceptionType;
            }

            void throwIt() {
                throw rethrow(((SimpleCommandExceptionType) this.type).create());
            }
        }

        protected SelectorParser(
                final boolean single,
                final boolean playersOnly
        ) {
            this.modernParser = createModernParser(single, playersOnly, this);
        }

        protected CompletableFuture<ArgumentParseResult<T>> legacyParse(
                final CommandContext<C> commandContext,
                final CommandInput commandInput
        ) {
            return ArgumentParseResult.failureFuture(new SelectorUnsupportedException(
                    commandContext,
                    this.getClass()
            ));
        }

        protected @NonNull Iterable<@NonNull Suggestion> legacySuggestions(
                final CommandContext<C> commandContext,
                final String input
        ) {
            return Collections.emptyList();
        }

        @Override
        public CompletableFuture<ArgumentParseResult<T>> parseFuture(
                final CommandContext<C> commandContext,
                final CommandInput commandInput
        ) {
            if (this.modernParser != null) {
                return this.modernParser.parseFuture(commandContext, commandInput);
            }
            return this.legacyParse(commandContext, commandInput);
        }

        @Override
        public CompletableFuture<Iterable<@NonNull Suggestion>> suggestionsFuture(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            if (this.modernParser != null) {
                this.modernParser.suggestionProvider().suggestionsFuture(commandContext, input);
            }
            return CompletableFuture.completedFuture(this.legacySuggestions(commandContext, input));
        }

        // returns SimpleCommandExceptionType, does not reference in signature for ABI with pre-1.13
        private static Object findExceptionType(final String type) {
            final Field[] fields = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("entity")).getDeclaredFields();
            return Arrays.stream(fields)
                    .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == SimpleCommandExceptionType.class)
                    .map(field -> {
                        try {
                            final @Nullable Object fieldValue = field.get(null);
                            if (fieldValue == null) {
                                return null;
                            }
                            final Field messageField = SimpleCommandExceptionType.class.getDeclaredField("message");
                            messageField.setAccessible(true);
                            if (messageField.get(fieldValue).toString().contains(type)) {
                                return fieldValue;
                            }
                        } catch (final ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Could not find exception type '" + type + "'"));
        }
    }

    abstract static class EntitySelectorParser<C, T> extends SelectorParser<C, T> {

        protected EntitySelectorParser(final boolean single) {
            super(single, false);
        }
    }

    abstract static class PlayerSelectorParser<C, T> extends SelectorParser<C, T> {

        protected PlayerSelectorParser(final boolean single) {
            super(single, true);
        }

        @Override
        protected @NonNull Iterable<@NonNull Suggestion> legacySuggestions(
                final CommandContext<C> commandContext,
                final String input
        ) {
            final List<Suggestion> suggestions = new ArrayList<>();

            for (final Player player : Bukkit.getOnlinePlayers()) {
                final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
                if (bukkit instanceof Player && !((Player) bukkit).canSee(player)) {
                    continue;
                }
                suggestions.add(Suggestion.simple(player.getName()));
            }

            return suggestions;
        }
    }

    private static class ModernSelectorParser<C, T> implements ArgumentParser.FutureArgumentParser<C, T>, SuggestionProvider<C> {

        private final WrappedBrigadierParser<C, Object> wrappedBrigadierParser;
        private final SelectorMapper<T> mapper;

        ModernSelectorParser(
                final WrappedBrigadierParser<C, Object> wrapperBrigParser,
                final SelectorMapper<T> mapper
        ) {
            this.wrappedBrigadierParser = wrapperBrigParser;
            this.mapper = mapper;
        }

        @Override
        @SuppressWarnings("unchecked")
        public CompletableFuture<ArgumentParseResult<T>> parseFuture(
                final CommandContext<C> commandContext,
                final CommandInput commandInput
        ) {
            return CompletableFuture.supplyAsync(() -> {
                final CommandInput originalCommandInput = commandInput.copy();
                final ArgumentParseResult<Object> result = this.wrappedBrigadierParser.parse(
                        commandContext,
                        commandInput
                );
                if (result.failure().isPresent()) {
                    return (ArgumentParseResult<T>) result;
                }
                final String input = originalCommandInput.difference(commandInput);
                try {
                    return ArgumentParseResult.success(
                            this.mapper.mapResult(input, new EntitySelectorWrapper(commandContext, result.parsedValue().get()))
                    );
                } catch (final CommandSyntaxException ex) {
                    return ArgumentParseResult.failure(ex);
                } catch (final Exception ex) {
                    throw rethrow(ex);
                }
            }, commandContext.get(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR));
        }

        @Override
        public CompletableFuture<Iterable<@NonNull Suggestion>> suggestionsFuture(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final Object commandSourceStack = commandContext.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
            final @Nullable Field bypassField =
                    CraftBukkitReflection.findField(commandSourceStack.getClass(), "bypassSelectorPermissions");
            try {
                boolean prev = false;
                try {
                    if (bypassField != null) {
                        prev = bypassField.getBoolean(commandSourceStack);
                        bypassField.setBoolean(commandSourceStack, true);
                    }
                    // stupid hack
                    return CompletableFuture.completedFuture(
                            this.wrappedBrigadierParser.suggestionProvider().suggestionsFuture(commandContext, input).join()
                    );
                } finally {
                    if (bypassField != null) {
                        bypassField.setBoolean(commandSourceStack, prev);
                    }
                }
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    static final class EntitySelectorWrapper {

        private static volatile @MonotonicNonNull Methods methods;

        private final CommandContext<?> commandContext;
        private final Object selector;

        private static final class Methods {

            private @MonotonicNonNull Method getBukkitEntity;
            private @MonotonicNonNull Method entity;
            private @MonotonicNonNull Method player;
            private @MonotonicNonNull Method entities;
            private @MonotonicNonNull Method players;

            Methods(final CommandContext<?> commandContext, final Object selector) {
                final Object nativeSender = commandContext.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
                final Class<?> nativeSenderClass = nativeSender.getClass();
                for (final Method method : selector.getClass().getDeclaredMethods()) {
                    if (method.getParameterCount() != 1
                            || !method.getParameterTypes()[0].equals(nativeSenderClass)
                            || !Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }

                    final Class<?> returnType = method.getReturnType();
                    if (List.class.isAssignableFrom(returnType)) {
                        final ParameterizedType stringListType = (ParameterizedType) method.getGenericReturnType();
                        Type listType = stringListType.getActualTypeArguments()[0];
                        while (listType instanceof WildcardType) {
                            listType = ((WildcardType) listType).getUpperBounds()[0];
                        }
                        final Class<?> clazz = listType instanceof Class
                                ? (Class<?>) listType
                                : GenericTypeReflector.erase(listType);
                        final @Nullable Method getBukkitEntity = findGetBukkitEntityMethod(clazz);
                        if (getBukkitEntity == null) {
                            continue;
                        }
                        final Class<?> bukkitType = getBukkitEntity.getReturnType();
                        if (Player.class.isAssignableFrom(bukkitType)) {
                            if (this.players != null) {
                                throw new IllegalStateException();
                            }
                            this.players = method;
                        } else {
                            if (this.entities != null) {
                                throw new IllegalStateException();
                            }
                            this.entities = method;
                        }
                    } else if (returnType != Void.TYPE) {
                        final @Nullable Method getBukkitEntity = findGetBukkitEntityMethod(returnType);
                        if (getBukkitEntity == null) {
                            continue;
                        }
                        final Class<?> bukkitType = getBukkitEntity.getReturnType();
                        if (Player.class.isAssignableFrom(bukkitType)) {
                            if (this.player != null) {
                                throw new IllegalStateException();
                            }
                            this.player = method;
                        } else {
                            if (this.entity != null || this.getBukkitEntity != null) {
                                throw new IllegalStateException();
                            }
                            this.entity = method;
                            this.getBukkitEntity = getBukkitEntity;
                        }
                    }
                }
                Objects.requireNonNull(this.getBukkitEntity, "Failed to locate getBukkitEntity method");
                Objects.requireNonNull(this.player, "Failed to locate findPlayer method");
                Objects.requireNonNull(this.entity, "Failed to locate findEntity method");
                Objects.requireNonNull(this.players, "Failed to locate findPlayers method");
                Objects.requireNonNull(this.entities, "Failed to locate findEntities method");
            }

            private static @Nullable Method findGetBukkitEntityMethod(final Class<?> returnType) {
                @Nullable Method getBukkitEntity;
                try {
                    getBukkitEntity = returnType.getDeclaredMethod("getBukkitEntity");
                } catch (final ReflectiveOperationException ex) {
                    try {
                        getBukkitEntity = returnType.getMethod("getBukkitEntity");
                    } catch (final ReflectiveOperationException ex0) {
                        getBukkitEntity = null;
                    }
                }
                return getBukkitEntity;
            }
        }

        EntitySelectorWrapper(
                final CommandContext<?> commandContext,
                final Object selector
        ) {
            this.commandContext = commandContext;
            this.selector = selector;
        }

        @SuppressWarnings("LockOnNonEnclosingClassLiteral")
        private static Methods methods(final CommandContext<?> commandContext, final Object selector) {
            if (methods == null) {
                synchronized (Methods.class) {
                    if (methods == null) {
                        methods = new Methods(commandContext, selector);
                    }
                }
            }
            return methods;
        }

        private Methods methods() {
            return methods(this.commandContext, this.selector);
        }

        Entity singleEntity() {
            return reflectiveOperation(() -> (Entity) this.methods().getBukkitEntity.invoke(this.methods().entity.invoke(
                    this.selector,
                    this.commandContext.<Object>get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER)
            )));
        }

        Player singlePlayer() {
            return reflectiveOperation(() -> (Player) this.methods().getBukkitEntity.invoke(this.methods().player.invoke(
                    this.selector,
                    this.commandContext.<Object>get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER)
            )));
        }

        @SuppressWarnings("unchecked")
        List<Entity> entities() {
            final List<Object> internalEntities = reflectiveOperation(() -> ((List<Object>) this.methods().entities.invoke(
                    this.selector,
                    this.commandContext.<Object>get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER)
            )));
            return internalEntities.stream()
                    .map(o -> reflectiveOperation(() -> (Entity) this.methods().getBukkitEntity.invoke(o)))
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        List<Player> players() {
            final List<Object> serverPlayers = reflectiveOperation(() -> ((List<Object>) this.methods().players.invoke(
                    this.selector,
                    this.commandContext.<Object>get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER)
            )));
            return serverPlayers.stream()
                    .map(o -> reflectiveOperation(() -> (Player) this.methods().getBukkitEntity.invoke(o)))
                    .collect(Collectors.toList());
        }

        @FunctionalInterface
        interface ReflectiveOperation<T> {

            T run() throws ReflectiveOperationException;
        }

        private static <T> T reflectiveOperation(final ReflectiveOperation<T> op) {
            try {
                return op.run();
            } catch (final InvocationTargetException ex) {
                if (ex.getCause() instanceof CommandSyntaxException) {
                    throw rethrow(ex.getCause());
                }
                throw new RuntimeException(ex);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @FunctionalInterface
    interface SelectorMapper<T> {

        T mapResult(String input, EntitySelectorWrapper wrapper) throws Exception; // throws CommandSyntaxException
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> RuntimeException rethrow(final Throwable t) throws X {
        throw (X) t;
    }
}
