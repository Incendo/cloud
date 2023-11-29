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

import cloud.commandframework.Description;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import net.minecraft.core.particles.ParticleOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument for any {@link net.minecraft.core.particles.ParticleOptions}.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class ParticleArgument<C> extends CommandArgument<C, ParticleOptions> {

    ParticleArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Description defaultDescription
    ) {
        super(
                name,
                FabricArgumentParsers.contextual(net.minecraft.commands.arguments.ParticleArgument::particle),
                ParticleOptions.class,
                suggestionProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link ParticleArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ParticleArgument<C> of(final @NonNull String name) {
        return ParticleArgument.<C>builder(name).build();
    }


    /**
     * Builder for {@link ParticleArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, ParticleOptions, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ParticleOptions.class, name);
        }

        /**
         * Build a new particle effect argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull ParticleArgument<C> build() {
            return new ParticleArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription()
            );
        }
    }
}
