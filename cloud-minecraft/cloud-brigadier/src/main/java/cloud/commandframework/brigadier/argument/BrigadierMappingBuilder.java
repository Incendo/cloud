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
package cloud.commandframework.brigadier.argument;

import cloud.commandframework.arguments.parser.ArgumentParser;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

import static java.util.Objects.requireNonNull;

/**
 * A builder for a mapping between a Cloud parser and a Brigadier {@link com.mojang.brigadier.arguments.ArgumentType}
 *
 * @param <K> the Cloud argument parser type
 * @param <S> the brigadier-native sender type
 * @since 1.5.0
 */
public interface BrigadierMappingBuilder<K extends ArgumentParser<?, ?>, S> {

    /**
     * Map the argument type parser to a constant Brigadier argument type instance.
     *
     * @param constant the argument type
     * @return this builder
     * @since 1.5.0
     */
    @This @NonNull BrigadierMappingBuilder<K, S> toConstant(ArgumentType<?> constant);

    /**
     * Map the cloud argument parser to a variable Brigadier {@link ArgumentType}.
     *
     * @param mapper the mapper
     * @return this builder
     * @since 1.5.0
     */
    @This @NonNull BrigadierMappingBuilder<K, S> to(Function<K, ? extends ArgumentType<?>> mapper);

    /**
     * Use the default brigadier suggestions for this argument type.
     *
     * <p>This is the default option if a mapped type is specified.</p>
     *
     * @return this builder
     * @since 1.5.0
     */
    @This @NonNull BrigadierMappingBuilder<K, S> nativeSuggestions();

    /**
     * Use the suggestions from Cloud's parser for this argument type.
     *
     * <p>This is not the default suggestions configuration.</p>
     *
     * <p>Any previously set suggestion provider suppliers will not be used.</p>
     *
     * @return this builder
     * @since 1.5.0
     */
    @This @NonNull BrigadierMappingBuilder<K, S> cloudSuggestions();

    /**
     * Use a custom Brigadier suggestion provider for this parser.
     *
     * @param provider the suggestion provider
     * @return this builder
     * @since 1.5.0
     */
    default @This @NonNull BrigadierMappingBuilder<K, S> suggestedByConstant(final SuggestionProvider<S> provider) {
        requireNonNull(provider, "provider");
        return this.suggestedBy((argument, useCloud) -> provider);
    }

    /**
     * Use a custom Brigadier suggestion provider for this parser.
     *
     * @param provider the suggestion provider
     * @return this builder
     * @since 1.5.0
     */
    @This @NonNull BrigadierMappingBuilder<K, S> suggestedBy(SuggestionProviderSupplier<K, S> provider);

    /**
     * Builds the mapping.
     *
     * @return the built mapping
     * @since 2.0.0
     */
    @NonNull BrigadierMapping<?, K, S> build();


    @FunctionalInterface
    interface SuggestionProviderSupplier<K extends ArgumentParser<?, ?>, S> {

        /**
         * Create a new suggestion provider based on the provided argument.
         *
         * @param argument Argument to create a specialized provider for
         * @param useCloud A provider that can be returned to ask the server to use cloud suggestions
         * @return A new provider, or {@code null} to use the default value for the mapped argument type
         * @since 1.5.0
         */
        @Nullable SuggestionProvider<? super S> provide(@NonNull K argument, SuggestionProvider<S> useCloud);
    }
}
