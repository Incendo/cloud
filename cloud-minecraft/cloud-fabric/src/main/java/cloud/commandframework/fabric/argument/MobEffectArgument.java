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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument parsing a status effect from the {@link net.minecraft.core.registries.Registries#MOB_EFFECT status effect registry}.
 *
 * @param <C> the sender type
 * @since 1.5.0
 * @deprecated backing vanilla type was removed in Minecraft 1.19.3. Uses {@link RegistryEntryArgument.Parser}.
 */
@API(status = API.Status.DEPRECATED, since = "1.8.0")
@Deprecated
public final class MobEffectArgument<C> extends CommandArgument<C, MobEffect> {

    MobEffectArgument(
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                name,
                new RegistryEntryArgument.Parser<>(Registries.MOB_EFFECT),
                MobEffect.class,
                suggestionsProvider,
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
        return new MobEffectArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link MobEffectArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull MobEffectArgument<C> of(final @NonNull String name) {
        return MobEffectArgument.<C>builder(name).build();
    }


    /**
     * Builder for {@link MobEffectArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, MobEffect, Builder<C>> {

        Builder(final @NonNull String name) {
            super(MobEffect.class, name);
        }

        /**
         * Build a new {@link MobEffectArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull MobEffectArgument<C> build() {
            return new MobEffectArgument<>(
                    this.getName(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }
}
