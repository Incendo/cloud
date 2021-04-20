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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;

public final class NamedTextColorArgument<C> extends CommandArgument<C, NamedTextColor> {

    private NamedTextColorArgument(
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
                NamedTextColor.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    public static <C> @NonNull NamedTextColorArgument<C> optional(final @NonNull String name) {
        return NamedTextColorArgument.<C>builder(name).asOptional().build();
    }

    public static <C> @NonNull NamedTextColorArgument<C> of(final @NonNull String name) {
        return NamedTextColorArgument.<C>builder(name).build();
    }

    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, NamedTextColor> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull NamedTextColor> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek().toLowerCase(Locale.ROOT);
            final Optional<NamedTextColor> color = Sponge.registry()
                    .adventureRegistry()
                    .namedColors()
                    .value(input);
            if (color.isPresent()) {
                inputQueue.remove();
                return ArgumentParseResult.success(color.get());
            }
            // todo
            return ArgumentParseResult.failure(new IllegalArgumentException("sadge"));
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return ClientCompletionKeys.COLOR.get().createNode();
        }

    }

    public static final class Builder<C> extends TypedBuilder<C, NamedTextColor, Builder<C>> {

        Builder(final @NonNull String name) {
            super(NamedTextColor.class, name);
        }

        @Override
        public @NonNull NamedTextColorArgument<C> build() {
            return new NamedTextColorArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

}
