//
// MIT License
//
// Copyright (c) 2024 Incendo
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
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.INTERNAL, since = "2.0.0")
public final class BrigadierMapping<C, K extends ArgumentParser<C, ?>, S> {

    private static final SuggestionProvider<?> DELEGATE_TO_CLOUD = (c, b) -> b.buildFuture();

    /**
     * Returns a sentinel value for declaring that suggestions should be delegated to cloud.
     *
     * @param <T> the sender type
     * @return a singleton sentinel suggestion provider
     */
    @SuppressWarnings("unchecked")
    public static <T> SuggestionProvider<T> delegateSuggestions() {
        return (SuggestionProvider<T>) DELEGATE_TO_CLOUD;
    }

    /**
     * Returns a new mapping builder.
     *
     * @param <C> the command sender type
     * @param <K> the argument parser type
     * @param <S> the brigadier sender type
     * @return the mapping builder
     */
    public static <C, K extends ArgumentParser<C, ?>, S> @NonNull BrigadierMappingBuilder<K, S> builder() {
        return new BuilderImpl<>();
    }

    private final boolean cloudSuggestions;
    private final BrigadierMappingBuilder.@Nullable SuggestionProviderSupplier<K, S> suggestionsOverride;
    private final @Nullable Function<K, ? extends ArgumentType<?>> mapper;

    BrigadierMapping(
            final boolean cloudSuggestions,
            final BrigadierMappingBuilder.@Nullable SuggestionProviderSupplier<K, S> suggestionsOverride,
            final @Nullable Function<K, ? extends ArgumentType<?>> mapper
    ) {
        this.cloudSuggestions = cloudSuggestions;
        this.suggestionsOverride = suggestionsOverride;
        this.mapper = mapper;
    }

    /**
     * Returns the mapping function.
     *
     * @return the mapper
     */
    public @Nullable Function<K, ? extends ArgumentType<?>> mapper() {
        return this.mapper;
    }

    /**
     * Returns a new version of this mapping that uses native suggestions if {@code nativeSuggestions} is {@code true},
     * or cloud suggestions if it's {@code false}.
     *
     * @param nativeSuggestions whether to use native suggestions
     * @return the new mapping
     */
    public @NonNull BrigadierMapping<C, K, S> withNativeSuggestions(final boolean nativeSuggestions) {
        if (nativeSuggestions && this.cloudSuggestions) {
            return new BrigadierMapping<>(false, this.suggestionsOverride, this.mapper);
        } else if (!nativeSuggestions && !this.cloudSuggestions) {
            return new BrigadierMapping<>(true, this.suggestionsOverride, this.mapper);
        }
        return this;
    }

    /**
     * Creates a Brigadier suggestion provider for the given {@code commandArgument}.
     *
     * @param commandArgument the argument
     * @return the suggestion provider
     */
    @SuppressWarnings("unchecked")
    public @Nullable SuggestionProvider<S> makeSuggestionProvider(final K commandArgument) {
        if (this.cloudSuggestions) {
            return delegateSuggestions();
        }
        return this.suggestionsOverride == null
                ? null
                : (SuggestionProvider<S>) this.suggestionsOverride.provide(
                        commandArgument,
                        delegateSuggestions()
                );
    }


    private static final class BuilderImpl<C, K extends ArgumentParser<C, ?>, S> implements BrigadierMappingBuilder<K, S> {

        private Function<K, ? extends ArgumentType<?>> mapper;
        private boolean cloudSuggestions = false;
        private SuggestionProviderSupplier<K, S> suggestionsOverride;

        private BuilderImpl() {
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> toConstant(final ArgumentType<?> constant) {
            return this.to(parser -> constant);
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> to(final Function<K, ? extends ArgumentType<?>> mapper) {
            this.mapper = mapper;
            return this;
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> nativeSuggestions() {
            this.cloudSuggestions = false;
            this.suggestionsOverride = null;
            return this;
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> cloudSuggestions() {
            this.cloudSuggestions = true;
            this.suggestionsOverride = null;
            return this;
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> suggestedByConstant(final SuggestionProvider<S> provider) {
            BrigadierMappingBuilder.super.suggestedByConstant(provider);
            this.cloudSuggestions = false;
            return this;
        }

        @Override
        public @This @NonNull BrigadierMappingBuilder<K, S> suggestedBy(final SuggestionProviderSupplier<K, S> provider) {
            this.suggestionsOverride = requireNonNull(provider, "provider");
            this.cloudSuggestions = false;
            return this;
        }

        @Override
        public @NonNull BrigadierMapping<C, K, S> build() {
            return new BrigadierMapping<>(this.cloudSuggestions, this.suggestionsOverride, this.mapper);
        }
    }
}
