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

package cloud.commandframework.quilt.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.predicate.NumberRange;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument parsing an unbounded number range, in the form {@code [min]..[max] }, where both lower and upper bounds are
 * optional.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class IntRangeArgument<C> extends CommandArgument<C, NumberRange.IntRange> {

    IntRangeArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                new WrappedBrigadierParser<>(NumberRangeArgumentType.numberRange()),
                defaultValue,
                NumberRange.IntRange.class,
                suggestionsProvider
        );
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> IntRangeArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new IntRangeArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull IntRangeArgument<C> of(final @NonNull String name) {
        return IntRangeArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull IntRangeArgument<C> optional(final @NonNull String name) {
        return IntRangeArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value.
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull IntRangeArgument<C> optional(
            final @NonNull String name,
            final NumberRange.@NonNull IntRange defaultValue
    ) {
        final StringBuilder value = new StringBuilder(6);
        if (defaultValue.getMin() != null) {
            value.append(defaultValue.getMin());
        }
        value.append("..");
        if (defaultValue.getMax() != null) {
            value.append(defaultValue.getMax());
        }

        return IntRangeArgument.<C>newBuilder(name).asOptionalWithDefault(value.toString()).build();
    }


    /**
     * Builder for {@link IntRangeArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, NumberRange.IntRange, Builder<C>> {

        Builder(final @NonNull String name) {
            super(NumberRange.IntRange.class, name);
        }

        /**
         * Build a new int range argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull IntRangeArgument<C> build() {
            return new IntRangeArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider()
            );
        }

    }

}
