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
package cloud.commandframework.parser.aggregate;

import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.key.CloudKey;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public class AggregateParserBuilder<C> {

    private final List<CommandComponent<C>> components;

    AggregateParserBuilder(final @NonNull List<CommandComponent<C>> components) {
        this.components = Collections.unmodifiableList(components);
    }

    AggregateParserBuilder() {
        this.components = Collections.emptyList();
    }

    /**
     * Returns a new builder with the given {@code mapper}.
     *
     * @param <O>       the type produced by the mapper
     * @param valueType the type produced by the mapper
     * @param mapper    the mapper
     * @return the new builder
     */
    public final <O> @NonNull MappedAggregateParserBuilder<C, O> withMapper(
            final @NonNull TypeToken<O> valueType,
            final @NonNull AggregateResultMapper<C, O> mapper
    ) {
        return new MappedAggregateParserBuilder<>(this.components(), valueType, mapper);
    }

    /**
     * Returns a new builder with the given {@code mapper}.
     * <p>
     * Use {@link #withDirectMapper(Class, AggregateResultMapper.DirectAggregateResultMapper)} if you do not want to wrap
     * the result in a completable future.
     *
     * @param <O>       the type produced by the mapper
     * @param valueType the type produced by the mapper
     * @param mapper    the mapper
     * @return the new builder
     */
    public final <O> @NonNull MappedAggregateParserBuilder<C, O> withMapper(
            final @NonNull Class<O> valueType,
            final @NonNull AggregateResultMapper<C, O> mapper
    ) {
        return new MappedAggregateParserBuilder<>(this.components(), TypeToken.get(valueType), mapper);
    }

    /**
     * Returns a new builder with the given {@code mapper}.
     * <p>
     * This version does not need to wrap the result in a {@link java.util.concurrent.CompletableFuture}.
     *
     * @param <O>       the type produced by the mapper
     * @param valueType the type produced by the mapper
     * @param mapper    the mapper
     * @return the new builder
     */
    public final <O> @NonNull MappedAggregateParserBuilder<C, O> withDirectMapper(
            final @NonNull Class<O> valueType,
            final AggregateResultMapper.@NonNull DirectAggregateResultMapper<C, O> mapper
    ) {
        return new MappedAggregateParserBuilder<>(this.components(), TypeToken.get(valueType), mapper);
    }

    /**
     * Returns a new builder with the given {@code mapper}.
     * <p>
     * This version does not need to wrap the result in a {@link java.util.concurrent.CompletableFuture}.
     *
     * @param <O>       the type produced by the mapper
     * @param valueType the type produced by the mapper
     * @param mapper    the mapper
     * @return the new builder
     */
    public final <O> @NonNull MappedAggregateParserBuilder<C, O> withDirectMapper(
            final @NonNull TypeToken<O> valueType,
            final AggregateResultMapper.@NonNull DirectAggregateResultMapper<C, O> mapper
    ) {
        return new MappedAggregateParserBuilder<>(this.components(), valueType, mapper);
    }

    /**
     * Returns a new builder with the given {@code component} inserted into the component list.
     *
     * @param component the component
     * @return the new builder
     */
    public @NonNull AggregateParserBuilder<C> withComponent(
            final @NonNull CommandComponent<C> component
    ) {
        final List<CommandComponent<C>> components = new ArrayList<>(this.components());
        components.add(component);
        return new AggregateParserBuilder<>(components);
    }

    /**
     * Returns a new builder with a new component using the given {@code parserDescriptor} into the component list.
     *
     * @param <T>              the type of the parser
     * @param name             the name of the component
     * @param parserDescriptor the parser
     * @return the new builder
     */
    public <T> @NonNull AggregateParserBuilder<C> withComponent(
            final @NonNull String name,
            final @NonNull ParserDescriptor<C, T> parserDescriptor
    ) {
        return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).build());
    }

    /**
     * Returns a new builder with a new component using the given {@code parserDescriptor} into the component list.
     *
     * @param <T>                the type of the parser
     * @param name               the name of the component
     * @param parserDescriptor   the parser
     * @param suggestionProvider custom suggestion provider
     * @return the new builder
     */
    public <T> @NonNull AggregateParserBuilder<C> withComponent(
            final @NonNull String name,
            final @NonNull ParserDescriptor<C, T> parserDescriptor,
            final @NonNull SuggestionProvider<C> suggestionProvider
            ) {
        return this.withComponent(
                CommandComponent.<C, T>builder()
                        .name(name)
                        .parser(parserDescriptor)
                        .suggestionProvider(suggestionProvider)
                        .build()
        );
    }

    /**
     * Returns a new builder with a new component using the given {@code parserDescriptor} into the component list.
     *
     * @param <T>              the type of the parser
     * @param name             the name of the component
     * @param parserDescriptor the parser
     * @return the new builder
     */
    public <T> @NonNull AggregateParserBuilder<C> withComponent(
            final @NonNull CloudKey<T> name,
            final @NonNull ParserDescriptor<C, T> parserDescriptor
    ) {
        return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).build());
    }

    /**
     * Returns a new builder with a new component using the given {@code parserDescriptor} into the component list.
     *
     * @param <T>                the type of the parser
     * @param name               the name of the component
     * @param parserDescriptor   the parser
     * @param suggestionProvider custom suggestion provider
     * @return the new builder
     */
    public <T> @NonNull AggregateParserBuilder<C> withComponent(
            final @NonNull CloudKey<T> name,
            final @NonNull ParserDescriptor<C, T> parserDescriptor,
            final @NonNull SuggestionProvider<C> suggestionProvider
    ) {
        return this.withComponent(
                CommandComponent.<C, T>builder()
                        .key(name)
                        .parser(parserDescriptor)
                        .suggestionProvider(suggestionProvider)
                        .build()
        );
    }

    final @NonNull List<@NonNull CommandComponent<C>> components() {
        return this.components;
    }


    public static final class MappedAggregateParserBuilder<C, O> extends AggregateParserBuilder<C> {

        private final AggregateResultMapper<C, O> mapper;
        private final TypeToken<O> valueType;

        MappedAggregateParserBuilder(
                final @NonNull List<CommandComponent<C>> components,
                final @NonNull TypeToken<O> valueType,
                final @NonNull AggregateResultMapper<C, O> mapper
        ) {
            super(components);
            this.valueType = valueType;
            this.mapper = mapper;
        }

        @Override
        public @NonNull MappedAggregateParserBuilder<C, O> withComponent(
                final @NonNull CommandComponent<C> component
        ) {
            final List<CommandComponent<C>> components = new ArrayList<>(this.components());
            components.add(component);
            return new MappedAggregateParserBuilder<>(components, this.valueType, this.mapper);
        }

        @Override
        public @NonNull <T> MappedAggregateParserBuilder<C, O> withComponent(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parserDescriptor
        ) {
            return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).build());
        }

        @Override
        public <T> @NonNull MappedAggregateParserBuilder<C, O> withComponent(
                final @NonNull String name,
                final @NonNull ParserDescriptor<C, T> parserDescriptor,
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            return this.withComponent(
                    CommandComponent.<C, T>builder()
                            .name(name)
                            .parser(parserDescriptor)
                            .suggestionProvider(suggestionProvider)
                            .build()
            );
        }

        @Override
        public @NonNull <T> MappedAggregateParserBuilder<C, O> withComponent(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parserDescriptor
        ) {
            return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).build());
        }

        @Override
        public @NonNull <T> MappedAggregateParserBuilder<C, O> withComponent(
                final @NonNull CloudKey<T> name,
                final @NonNull ParserDescriptor<C, T> parserDescriptor,
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            return this.withComponent(
                    CommandComponent.<C, T>builder()
                            .key(name)
                            .parser(parserDescriptor)
                            .suggestionProvider(suggestionProvider)
                            .build()
            );
        }

        /**
         * Builds the parser described by this builder.
         *
         * @return the parser
         */
        public @NonNull AggregateParser<C, O> build() {
            return new AggregateParserImpl<>(this.components(), this.valueType, this.mapper);
        }
    }
}
