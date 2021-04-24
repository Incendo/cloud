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
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class WorldArgument<C> extends CommandArgument<C, ServerWorld> {

    private WorldArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(),
                defaultValue,
                ServerWorld.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    public static <C> @NonNull WorldArgument<C> optional(final @NonNull String name) {
        return WorldArgument.<C>builder(name).asOptional().build();
    }

    public static <C> @NonNull WorldArgument<C> of(final @NonNull String name) {
        return WorldArgument.<C>builder(name).build();
    }

    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    public static final class Parser<C> implements ArgumentParser<C, ServerWorld> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull ServerWorld> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            final ResourceKey key = RegistryEntryArgument.resourceKey(input);
            if (key == null) {
                // todo
                return ArgumentParseResult.failure(new IllegalArgumentException("invalid key!"));
            }
            final Optional<ServerWorld> entry = Sponge.server().worldManager().world(key);
            if (entry.isPresent()) {
                inputQueue.remove();
                return ArgumentParseResult.success(entry.get());
            }
            // todo
            return ArgumentParseResult.failure(new IllegalArgumentException("sadge"));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return Sponge.server().worldManager().worlds().stream().map(world -> {
                if (world.key().namespace().equals(ResourceKey.MINECRAFT_NAMESPACE)) {
                    return world.key().value();
                }
                return world.key().asString();
            }).collect(Collectors.toList());
        }

    }

    public static final class Builder<C> extends TypedBuilder<C, ServerWorld, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ServerWorld.class, name);
        }

        @Override
        public @NonNull WorldArgument<C> build() {
            return new WorldArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

}
