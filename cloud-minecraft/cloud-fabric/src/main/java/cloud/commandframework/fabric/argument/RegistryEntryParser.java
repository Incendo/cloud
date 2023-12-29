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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.fabric.FabricCaptionKeys;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Argument for getting a values from a {@link Registry}.
 *
 * <p>Both static and dynamic registries are supported.</p>
 *
 * @param <C> the command sender type
 * @param <V> the registry entry type
 * @since 2.0.0
 */
public final class RegistryEntryParser<C, V> implements ArgumentParser<C, V>, BlockingSuggestionProvider.Strings<C> {

    private static final String NAMESPACE_MINECRAFT = "minecraft";

    /**
     * Creates a new registry entry parser.
     *
     * @param <C> command sender type
     * @param <V> the registry entry type
     * @param registry the registry key to use
     * @param valueType the value type of output
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
            final @NonNull ResourceKey<? extends Registry<V>> registry,
            final @NonNull TypeToken<V> valueType
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(registry), valueType);
    }

    /**
     * Creates a new registry entry parser.
     *
     * @param <C> command sender type
     * @param <V> the registry entry type
     * @param registry the registry key to use
     * @param valueType the value type of output
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, V> @NonNull ParserDescriptor<C, V> registryEntryParser(
            final @NonNull ResourceKey<? extends Registry<V>> registry,
            final @NonNull Class<V> valueType
    ) {
        return ParserDescriptor.of(new RegistryEntryParser<>(registry), TypeToken.get(valueType));
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #registryEntryParser} as the parser.
     *
     * @param <C> the command sender type
     * @param <V> the registry entry type
     * @param registry the registry key to use
     * @param valueType the value type of output
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, V> CommandComponent.@NonNull Builder<C, V> registryEntryComponent(
            final @NonNull ResourceKey<? extends Registry<V>> registry,
            final @NonNull TypeToken<V> valueType
    ) {
        return CommandComponent.<C, V>builder().parser(registryEntryParser(registry, valueType));
    }

    private final ResourceKey<? extends Registry<V>> registryIdent;

    /**
     * Create a registry entry parser.
     *
     * @param registry registry key to use in parser
     */
    public RegistryEntryParser(
            final @NonNull ResourceKey<? extends Registry<V>> registry
    ) {
        this.registryIdent = registry;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull V> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final ResourceLocation key;
        try {
            key = ResourceLocation.read(new StringReader(commandInput.readString()));
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failure(ex);
        }

        final Registry<V> registry = this.resolveRegistry(commandContext);
        if (registry == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Unknown registry " + this.registryIdent));
        }

        final V entry = registry.get(key);
        if (entry == null) {
            return ArgumentParseResult.failure(new UnknownEntryException(commandContext, key, this.registryIdent));
        }

        return ArgumentParseResult.success(entry);
    }

    private Registry<V> resolveRegistry(final CommandContext<C> ctx) {
        final SharedSuggestionProvider reverseMapped = ctx.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
        return reverseMapped.registryAccess().registry(this.registryIdent).orElse(null);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final Set<ResourceLocation> ids = this.resolveRegistry(commandContext).keySet();
        final List<String> results = new ArrayList<>(ids.size());
        for (final ResourceLocation entry : ids) {
            if (entry.getNamespace().equals(NAMESPACE_MINECRAFT)) {
                results.add(entry.getPath());
            }
            results.add(entry.toString());
        }

        return results;
    }

    /**
     * Get the registry key the parser is using.
     *
     * @return the associated registry key
     */
    public ResourceKey<? extends Registry<?>> registryKey() {
        return this.registryIdent;
    }

    /**
     * An exception thrown when an entry in a registry could not be found.
     *
     * @since 1.5.0
     */
    private static final class UnknownEntryException extends ParserException {

        private static final long serialVersionUID = 7694424294461849903L;

        UnknownEntryException(
                final CommandContext<?> context,
                final ResourceLocation key,
                final ResourceKey<? extends Registry<?>> registry
        ) {
            super(
                    RegistryEntryParser.class,
                    context,
                    FabricCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
                    CaptionVariable.of("id", key.toString()),
                    CaptionVariable.of("registry", registry.toString())
            );
        }
    }
}
