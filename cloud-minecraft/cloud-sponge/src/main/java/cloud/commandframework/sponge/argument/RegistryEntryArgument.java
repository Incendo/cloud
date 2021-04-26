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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import cloud.commandframework.sponge.SpongeCaptionKeys;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.ClientSuggestionProviders;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RegistryEntryArgument<C, V> extends CommandArgument<C, V> { // todo: should we return a RegistryEntry<V>?

    private RegistryEntryArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(holderSupplier, registryType),
                defaultValue,
                valueType,
                suggestionsProvider,
                defaultDescription
        );
    }

    // Start DefaultedRegistryType methods

    public static <C, V> @NonNull RegistryEntryArgument<C, V> of(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return RegistryEntryArgument.<C, V>builder(name, valueType, registryType).build();
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> of(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return of(name, TypeToken.get(valueType), registryType);
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return RegistryEntryArgument.<C, V>builder(name, valueType, registryType).asOptional().build();
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return optional(name, TypeToken.get(valueType), registryType);
    }

    public static <C, V> @NonNull Builder<C, V> builder(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return new Builder<>(name, valueType, ctx -> registryType.defaultHolder().get(), registryType);
    }

    public static <C, V> @NonNull Builder<C, V> builder(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull DefaultedRegistryType<V> registryType
    ) {
        return builder(name, TypeToken.get(valueType), registryType);
    }

    // End DefaultedRegistryType methods

    // Start RegistryType methods

    public static <C, V> @NonNull RegistryEntryArgument<C, V> of(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return RegistryEntryArgument.<C, V>builder(name, valueType, holderSupplier, registryType).build();
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> of(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return of(name, TypeToken.get(valueType), holderSupplier, registryType);
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return builder(name, valueType, holderSupplier, registryType).asOptional().build();
    }

    public static <C, V> @NonNull RegistryEntryArgument<C, V> optional(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return optional(name, TypeToken.get(valueType), holderSupplier, registryType);
    }

    public static <C, V> @NonNull Builder<C, V> builder(
            final @NonNull String name,
            final @NonNull TypeToken<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return new Builder<>(name, valueType, holderSupplier, registryType);
    }

    public static <C, V> @NonNull Builder<C, V> builder(
            final @NonNull String name,
            final @NonNull Class<V> valueType,
            final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
            final @NonNull RegistryType<V> registryType
    ) {
        return builder(name, TypeToken.get(valueType), holderSupplier, registryType);
    }

    // End RegistryType methods

    public static final class Parser<C, V> implements NodeSupplyingArgumentParser<C, V> {


        private final Function<CommandContext<C>, RegistryHolder> holderSupplier;
        private final RegistryType<V> registryType;

        public Parser(
                final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
                final @NonNull RegistryType<V> registryType
        ) {
            this.holderSupplier = holderSupplier;
            this.registryType = registryType;
        }

        public Parser(final @NonNull DefaultedRegistryType<V> registryType) {
            this(ctx -> registryType.defaultHolder().get(), registryType);
        }

        private Registry<V> registry(final @NonNull CommandContext<C> commandContext) {
            return this.holderSupplier.apply(commandContext).registry(this.registryType);
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull V> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            final ResourceKey key = ResourceKeyUtil.resourceKey(input);
            if (key == null) {
                return ResourceKeyUtil.invalidResourceKey();
            }
            final Optional<RegistryEntry<V>> entry = this.registry(commandContext).findEntry(key);
            if (entry.isPresent()) {
                inputQueue.remove();
                return ArgumentParseResult.success(entry.get().value());
            }
            return ArgumentParseResult.failure(new NoSuchEntryException(commandContext, key, this.registryType));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.registry(commandContext).streamEntries().flatMap(entry -> {
                if (entry.key().namespace().equals(ResourceKey.MINECRAFT_NAMESPACE)) {
                    return Stream.of(entry.key().value(), entry.key().asString());
                }
                return Stream.of(entry.key().asString());
            }).collect(Collectors.toList());
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            if (this.registryType.equals(RegistryTypes.SOUND_TYPE)) {
                return ClientCompletionKeys.RESOURCE_LOCATION.get().createNode()
                        .suggestions(ClientSuggestionProviders.AVAILABLE_SOUNDS);
            } else if (this.registryType.equals(RegistryTypes.BIOME)) {
                return ClientCompletionKeys.RESOURCE_LOCATION.get().createNode()
                        .suggestions(ClientSuggestionProviders.AVAILABLE_BIOMES);
            } else if (this.registryType.equals(RegistryTypes.ENTITY_TYPE)) {
                return ClientCompletionKeys.ENTITY_SUMMON.get().createNode()
                        .suggestions(ClientSuggestionProviders.SUMMONABLE_ENTITIES);
            } else if (this.registryType.equals(RegistryTypes.ENCHANTMENT_TYPE)) {
                return ClientCompletionKeys.ITEM_ENCHANTMENT.get().createNode();
            } else if (this.registryType.equals(RegistryTypes.POTION_EFFECT_TYPE)) {
                return ClientCompletionKeys.MOB_EFFECT.get().createNode();
            } else if (this.registryType.equals(RegistryTypes.WORLD_TYPE)) {
                // todo: should we use custom suggestions? sponge seems to include non-vanilla dimension types in it's registry.
                return ClientCompletionKeys.DIMENSION.get().createNode();
            }
            return ClientCompletionKeys.RESOURCE_LOCATION.get().createNode().customSuggestions();
        }

    }

    /**
     * Builder for {@link RegistryEntryArgument}.
     *
     * @param <C> sender type
     * @param <V> value type
     */
    public static final class Builder<C, V> extends TypedBuilder<C, V, Builder<C, V>> {

        private final RegistryType<V> registryType;
        private final Function<CommandContext<C>, RegistryHolder> holderSupplier;

        Builder(
                final @NonNull String name,
                final @NonNull TypeToken<V> valueType,
                final @NonNull Function<CommandContext<C>, RegistryHolder> holderSupplier,
                final @NonNull RegistryType<V> registryType
        ) {
            super(valueType, name);
            this.registryType = registryType;
            this.holderSupplier = holderSupplier;
        }

        @Override
        public @NonNull RegistryEntryArgument<C, V> build() {
            return new RegistryEntryArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getValueType(),
                    this.holderSupplier,
                    this.registryType,
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    /**
     * An exception thrown when there is no entry for the provided {@link ResourceKey} in the resolved registry.
     */
    private static final class NoSuchEntryException extends ParserException {

        private static final long serialVersionUID = 4472876671109079272L;

        NoSuchEntryException(
                final CommandContext<?> context,
                final ResourceKey key,
                final RegistryType<?> registryType
        ) {
            super(
                    Parser.class,
                    context,
                    SpongeCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
                    CaptionVariable.of("id", key.asString()),
                    CaptionVariable.of("registry", registryType.location().asString())
            );
        }

    }

}
