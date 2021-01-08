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

package cloud.commandframework.fabric.argument.server;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.fabric.FabricCaptionKeys;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.leangen.geantyref.TypeToken;
import net.minecraft.command.CommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

/**
 * Get a value from a registry.
 *
 * <p>Both static and dynamic registries are supported.</p>
 *
 * @param <C> the command sender type
 * @since 1.4.0
 */
public class CommandFunctionArgument<C> extends CommandArgument<C, CommandFunction> {

    CommandFunctionArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull RegistryKey<? extends Registry<V>> registry,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<V> valueType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(required, name, new RegistryEntryParser<>(registry), defaultValue, valueType, suggestionsProvider);
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param type The type of registry entry
     * @param registry A key for the registry to get values from
     * @param <C>  Command sender type
     * @param <V> Registry entry type
     * @return Created builder
     */
    public static <C, V> CommandFunctionArgument.@NonNull Builder<C, V> newBuilder(
            final @NonNull String name,
            final @NonNull Class<V> type,
            final @NonNull RegistryKey<? extends Registry<V>> registry) {
        return new CommandFunctionArgument.Builder<>(registry, type, name);
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param type The type of registry entry
     * @param registry A key for the registry to get values from
     * @param <C>  Command sender type
     * @param <V> Registry entry type
     * @return Created builder
     */
    public static <C, V> CommandFunctionArgument.@NonNull Builder<C, V> newBuilder(
            final @NonNull String name,
            final @NonNull TypeToken<V> type,
            final @NonNull RegistryKey<? extends Registry<V>> registry) {
        return new CommandFunctionArgument.Builder<>(registry, type, name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Argument name
     * @param type The type of registry entry
     * @param registry A key for the registry to get values from
     * @param <C>  Command sender type
     * @param <V> Registry entry type
     * @return Created argument
     */
    public static <C, V> @NonNull CommandFunctionArgument<C, V> of(
            final @NonNull String name,
            final @NonNull Class<V> type,
            final @NonNull RegistryKey<? extends Registry<V>> registry
    ) {
        return CommandFunctionArgument.<C, V>newBuilder(name, type, registry).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param type The type of registry entry
     * @param registry A key for the registry to get values from
     * @param <C>  Command sender type
     * @param <V> Registry entry type
     * @return     Created argument
     */
    public static <C, V> @NonNull CommandFunctionArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull Class<V> type,
            final @NonNull RegistryKey<? extends Registry<V>> registry
    ) {
        return CommandFunctionArgument.<C, V>newBuilder(name, type, registry).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value
     *
     * @param name        Argument name
     * @param type The type of registry entry
     * @param registry A key for the registry to get values from
     * @param defaultValue Default value
     * @param <C>         Command sender type
     * @param <V> Registry entry type
     * @return Created argument
     */
    public static <C, V> @NonNull CommandFunctionArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull Class<V> type,
            final @NonNull RegistryKey<? extends Registry<V>> registry,
            final @NonNull RegistryKey<V> defaultValue
    ) {
        return CommandFunctionArgument.<C, V>newBuilder(name, type, registry)
                .asOptionalWithDefault(defaultValue.getValue().toString())
                .build();
    }

    /**
     * A parser for values stored in a {@link Registry}
     *
     * @param <C> Command sender type
     * @param <V> Registry entry type
     */
    public static final class RegistryEntryParser<C, V> implements ArgumentParser<C, V> {
        private final RegistryKey<? extends Registry<V>> registryIdent;

        /**
         * Create a new parser for registry entries.
         *
         * @param registryIdent the registry identifier
         */
        public RegistryEntryParser(final RegistryKey<? extends Registry<V>> registryIdent) {
            this.registryIdent = requireNonNull(registryIdent, "registryIdent");
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull V> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String possibleIdentifier = inputQueue.peek();
            if (possibleIdentifier == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        CommandFunctionArgument.class,
                        commandContext
                ));
            }

            final Identifier key;
            try {
                key = Identifier.fromCommandInput(new StringReader(possibleIdentifier));
            } catch (final CommandSyntaxException ex) {
                return ArgumentParseResult.failure(ex);
            }
            inputQueue.poll();

            final Registry<V> registry = this.getRegistry(commandContext);
            if (registry == null) {
                return ArgumentParseResult.failure(new IllegalArgumentException("Unknown registry " + this.registryIdent));
            }

            final V entry = registry.get(key);
            if (entry == null) {
                return ArgumentParseResult.failure(new UnknownEntryException(commandContext, key, this.registryIdent));
            }

            return ArgumentParseResult.success(entry);
        }

        @SuppressWarnings("unchecked")
        Registry<V> getRegistry(final CommandContext<C> ctx) {
            final CommandSource reverseMapped = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
            // First try dynamic registries (for things loaded from data-packs)
            Registry<V> registry = reverseMapped.getRegistryManager().getOptional(this.registryIdent).orElse(null);
            if (registry == null) {
                // And then static registries
                registry = (Registry<V>) Registry.REGISTRIES.get(this.registryIdent.getValue());
            }
            return registry;
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final Set<Identifier> ids = this.getRegistry(commandContext).getIds();
            final List<String> results = new ArrayList<>(ids.size());
            for (final Identifier entry : ids) {
                if (entry.getNamespace().equals(NAMESPACE_MINECRAFT)) {
                    results.add(entry.getPath());
                }
                results.add(entry.toString());
            }

            return results;
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        /**
         * Get the registry associated with this parser
         * @return the registry
         */
        public RegistryKey<? extends Registry<?>> getRegistry() {
            return this.registryIdent;
        }

    }

    /**
     * A builder for registry entry arguments.
     *
     * @param <C> The sender type
     * @param <V> The registry value type
     */
    public static final class Builder<C, V> extends TypedBuilder<C, V, Builder<C, V>> {

        Builder(
                final @NonNull String name
        ) {
            super(valueType, name);
        }

        @Override
        public @NonNull CommandFunctionArgument<@NonNull C, @NonNull V> build() {
            return new CommandFunctionArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getValueType(),
                    this.getSuggestionsProvider()
            );
        }
    }

    /**
     * An exception thrown when an entry in a registry could not be found.
     */
    private static final class UnknownEntryException extends ParserException {

        private static final long serialVersionUID = 7694424294461849903L;

        UnknownEntryException(
                final CommandContext<?> context,
                final Identifier key,
                final RegistryKey<? extends Registry<?>> registry
        ) {
            super(
                    CommandFunctionArgument.class,
                    context,
                    FabricCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
                    CaptionVariable.of("id", key.toString()),
                    CaptionVariable.of("registry", registry.toString())
            );
        }

    }

}
