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
package cloud.commandframework.arguments.parser;

import cloud.commandframework.annotations.specifier.FlagYielding;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Liberal;
import cloud.commandframework.annotations.specifier.Quoted;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.arguments.standard.BooleanParser;
import cloud.commandframework.arguments.standard.ByteParser;
import cloud.commandframework.arguments.standard.CharacterParser;
import cloud.commandframework.arguments.standard.DoubleParser;
import cloud.commandframework.arguments.standard.DurationArgument;
import cloud.commandframework.arguments.standard.EnumParser;
import cloud.commandframework.arguments.standard.FloatParser;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.standard.LongParser;
import cloud.commandframework.arguments.standard.ShortArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import io.leangen.geantyref.AnnotatedTypeMap;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Standard implementation of {@link ParserRegistry}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
@API(status = API.Status.STABLE)
public final class StandardParserRegistry<C> implements ParserRegistry<C> {

    @SuppressWarnings({"DoubleBraceInitialization"})
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPINGS = new HashMap<Class<?>, Class<?>>() {
        private static final long serialVersionUID = 958977651563195489L;

        {
            put(char.class, Character.class);
            put(int.class, Integer.class);
            put(short.class, Short.class);
            put(byte.class, Byte.class);
            put(float.class, Float.class);
            put(double.class, Double.class);
            put(long.class, Long.class);
            put(boolean.class, Boolean.class);
        }
    };

    private final Map<String, Function<ParserParameters, ArgumentParser<C, ?>>> namedParsers = new HashMap<>();
    private final Map<AnnotatedType, Function<ParserParameters, ArgumentParser<C, ?>>> parserSuppliers = new AnnotatedTypeMap<>();
    private final Map<Class<? extends Annotation>, BiFunction<? extends Annotation, TypeToken<?>, ParserParameters>>
            annotationMappers = new HashMap<>();
    private final Map<String, SuggestionProvider<C>> namedSuggestionProviders = new HashMap<>();

    /**
     * Construct a new {@link StandardParserRegistry} instance. This will also
     * register all standard annotation mappers and parser suppliers
     */
    public StandardParserRegistry() {
        /* Register standard mappers */
        this.<Range, Number>registerAnnotationMapper(Range.class, new RangeMapper());
        this.<Greedy, String>registerAnnotationMapper(Greedy.class, new GreedyMapper());
        this.<Quoted, String>registerAnnotationMapper(
                Quoted.class,
                (quoted, typeToken) -> ParserParameters.single(StandardParameters.QUOTED, true)
        );
        this.<Liberal, Boolean>registerAnnotationMapper(
                Liberal.class,
                (liberal, typeToken) -> ParserParameters.single(StandardParameters.LIBERAL, true)
        );
        this.<FlagYielding, String>registerAnnotationMapper(
                FlagYielding.class,
                (flagYielding, typeToken) -> ParserParameters.single(StandardParameters.FLAG_YIELDING, true)
        );

        /* Register standard types */
        this.registerParserSupplier(TypeToken.get(Byte.class), options ->
                new ByteParser<>(
                        (byte) options.get(StandardParameters.RANGE_MIN, Byte.MIN_VALUE),
                        (byte) options.get(StandardParameters.RANGE_MAX, Byte.MAX_VALUE)
                ));
        this.registerParserSupplier(TypeToken.get(Short.class), options ->
                new ShortArgument.ShortParser<>(
                        (short) options.get(StandardParameters.RANGE_MIN, Short.MIN_VALUE),
                        (short) options.get(StandardParameters.RANGE_MAX, Short.MAX_VALUE)
                ));
        this.registerParserSupplier(TypeToken.get(Integer.class), options ->
                new IntegerParser<>(
                        (int) options.get(StandardParameters.RANGE_MIN, Integer.MIN_VALUE),
                        (int) options.get(StandardParameters.RANGE_MAX, Integer.MAX_VALUE)
                ));
        this.registerParserSupplier(TypeToken.get(Long.class), options ->
                new LongParser<>(
                        (long) options.get(StandardParameters.RANGE_MIN, Long.MIN_VALUE),
                        (long) options.get(StandardParameters.RANGE_MAX, Long.MAX_VALUE)
                ));
        this.registerParserSupplier(TypeToken.get(Float.class), options ->
                new FloatParser<>(
                        (float) options.get(StandardParameters.RANGE_MIN, Float.NEGATIVE_INFINITY),
                        (float) options.get(StandardParameters.RANGE_MAX, Float.POSITIVE_INFINITY)
                ));
        this.registerParserSupplier(TypeToken.get(Double.class), options ->
                new DoubleParser<>(
                        (double) options.get(StandardParameters.RANGE_MIN, Double.NEGATIVE_INFINITY),
                        (double) options.get(StandardParameters.RANGE_MAX, Double.POSITIVE_INFINITY)
                ));
        this.registerParserSupplier(TypeToken.get(Character.class), options -> new CharacterParser<>());
        this.registerParserSupplier(TypeToken.get(String[].class), options ->
                new StringArrayArgument.StringArrayParser<>(options.get(StandardParameters.FLAG_YIELDING, false))
        );
        /* Make this one less awful */
        this.registerParserSupplier(TypeToken.get(String.class), options -> {
            final boolean greedy = options.get(StandardParameters.GREEDY, false);
            final boolean greedyFlagAware = options.get(StandardParameters.FLAG_YIELDING, false);
            final boolean quoted = options.get(StandardParameters.QUOTED, false);
            if (greedyFlagAware && quoted) {
                throw new IllegalArgumentException(
                        "Don't know whether to create GREEDY_FLAG_YIELDING or QUOTED StringArgument.StringParser, both specified."
                );
            } else if (greedy && quoted) {
                throw new IllegalArgumentException(
                        "Don't know whether to create GREEDY or QUOTED StringArgument.StringParser, both specified."
                );
            }
            final StringArgument.StringMode stringMode;
            // allow @Greedy and @FlagYielding to both be true, give flag yielding priority
            if (greedyFlagAware) {
                stringMode = StringArgument.StringMode.GREEDY_FLAG_YIELDING;
            } else if (greedy) {
                stringMode = StringArgument.StringMode.GREEDY;
            } else if (quoted) {
                stringMode = StringArgument.StringMode.QUOTED;
            } else {
                stringMode = StringArgument.StringMode.SINGLE;
            }
            return new StringArgument.StringParser<>(
                    stringMode,
                    (context, s) -> Arrays.stream(options.get(StandardParameters.COMPLETIONS, new String[0]))
                            .map(Suggestion::simple)
                            .collect(Collectors.toList())
            );
        });
        this.registerParserSupplier(TypeToken.get(Boolean.class), options -> {
            final boolean liberal = options.get(StandardParameters.LIBERAL, false);
            return new BooleanParser<>(liberal);
        });
        this.registerParserSupplier(TypeToken.get(UUID.class), options -> new UUIDArgument.UUIDParser<>());
        this.registerParserSupplier(TypeToken.get(Duration.class), options -> new DurationArgument.Parser<>());
    }

    private static boolean isPrimitive(final @NonNull TypeToken<?> type) {
        return GenericTypeReflector.erase(type.getType()).isPrimitive();
    }

    @Override
    public <T> void registerParserSupplier(
            final @NonNull TypeToken<T> type,
            final @NonNull Function<@NonNull ParserParameters,
                    @NonNull ArgumentParser<C, ?>> supplier
    ) {
        this.parserSuppliers.put(type.getAnnotatedType(), supplier);
    }

    @Override
    public void registerNamedParserSupplier(
            final @NonNull String name,
            final @NonNull Function<@NonNull ParserParameters,
                    @NonNull ArgumentParser<C, ?>> supplier
    ) {
        this.namedParsers.put(name, supplier);
    }

    @Override
    public <A extends Annotation, T> void registerAnnotationMapper(
            final @NonNull Class<A> annotation,
            final @NonNull BiFunction<@NonNull A, @NonNull TypeToken<?>,
                    @NonNull ParserParameters> mapper
    ) {
        this.annotationMappers.put(annotation, mapper);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NonNull ParserParameters parseAnnotations(
            final @NonNull TypeToken<?> parsingType,
            final @NonNull Collection<@NonNull ? extends Annotation> annotations
    ) {
        final ParserParameters parserParameters = new ParserParameters();
        annotations.forEach(annotation -> {
            // noinspection all
            final BiFunction mapper = this.annotationMappers.get(annotation.annotationType());
            if (mapper == null) {
                return;
            }
            @SuppressWarnings("unchecked") final ParserParameters parserParametersCasted = (ParserParameters) mapper.apply(
                    annotation, parsingType);
            parserParameters.merge(parserParametersCasted);
        });
        return parserParameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            final @NonNull TypeToken<T> type,
            final @NonNull ParserParameters parserParameters
    ) {
        final TypeToken<?> actualType;
        if (GenericTypeReflector.erase(type.getType()).isPrimitive()) {
            actualType = TypeToken.get(PRIMITIVE_MAPPINGS.get(GenericTypeReflector.erase(type.getType())));
        } else {
            actualType = type;
        }
        final Function<ParserParameters, ArgumentParser<C, ?>> producer = this.parserSuppliers.get(actualType.getAnnotatedType());
        if (producer == null) {
            /* Give enums special treatment */
            if (GenericTypeReflector.isSuperType(Enum.class, actualType.getType())) {
                @SuppressWarnings("rawtypes") final EnumParser enumArgument = new EnumParser(
                        GenericTypeReflector.erase(actualType.getType())
                );
                return Optional.of(enumArgument);
            }
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final ArgumentParser<C, T> parser = (ArgumentParser<C, T>) producer.apply(
                parserParameters);
        return Optional.of(parser);
    }

    @Override
    public <T> @NonNull Optional<ArgumentParser<C, T>> createParser(
            final @NonNull String name,
            final @NonNull ParserParameters parserParameters
    ) {
        final Function<ParserParameters, ArgumentParser<C, ?>> producer = this.namedParsers.get(name);
        if (producer == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final ArgumentParser<C, T> parser = (ArgumentParser<C, T>) producer.apply(
                parserParameters);
        return Optional.of(parser);
    }

    @Override
    public void registerSuggestionProvider(
            final @NonNull String name,
            final @NonNull SuggestionProvider<C> suggestionProvider
    ) {
        this.namedSuggestionProviders.put(name.toLowerCase(Locale.ENGLISH), suggestionProvider);
    }

    @Override
    public @NonNull Optional<SuggestionProvider<C>> getSuggestionProvider(
            final @NonNull String name
    ) {
        final SuggestionProvider<C> suggestionProvider = this.namedSuggestionProviders.get(name.toLowerCase(Locale.ENGLISH));
        return Optional.ofNullable(suggestionProvider);
    }


    private static final class RangeMapper implements BiFunction<@NonNull Range, @NonNull TypeToken<?>,
            @NonNull ParserParameters> {

        @Override
        public @NonNull ParserParameters apply(final @NonNull Range range, final @NonNull TypeToken<?> type) {
            final Class<?> clazz;
            if (isPrimitive(type)) {
                clazz = PRIMITIVE_MAPPINGS.get(GenericTypeReflector.erase(type.getType()));
            } else {
                clazz = GenericTypeReflector.erase(type.getType());
            }
            if (!Number.class.isAssignableFrom(clazz)) {
                return ParserParameters.empty();
            }
            Number min = null;
            Number max = null;
            if (clazz.equals(Byte.class)) {
                if (!range.min().isEmpty()) {
                    min = Byte.parseByte(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Byte.parseByte(range.max());
                }
            } else if (clazz.equals(Short.class)) {
                if (!range.min().isEmpty()) {
                    min = Short.parseShort(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Short.parseShort(range.max());
                }
            } else if (clazz.equals(Integer.class)) {
                if (!range.min().isEmpty()) {
                    min = Integer.parseInt(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Integer.parseInt(range.max());
                }
            } else if (clazz.equals(Long.class)) {
                if (!range.min().isEmpty()) {
                    min = Long.parseLong(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Long.parseLong(range.max());
                }
            } else if (clazz.equals(Float.class)) {
                if (!range.min().isEmpty()) {
                    min = Float.parseFloat(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Float.parseFloat(range.max());
                }
            } else if (clazz.equals(Double.class)) {
                if (!range.min().isEmpty()) {
                    min = Double.parseDouble(range.min());
                }
                if (!range.max().isEmpty()) {
                    max = Double.parseDouble(range.max());
                }
            }
            final ParserParameters parserParameters = new ParserParameters();
            if (min != null) {
                parserParameters.store(StandardParameters.RANGE_MIN, min);
            }
            if (max != null) {
                parserParameters.store(StandardParameters.RANGE_MAX, max);
            }
            return parserParameters;
        }
    }


    private static final class GreedyMapper implements BiFunction<@NonNull Greedy, @NonNull TypeToken<?>,
            @NonNull ParserParameters> {

        @Override
        public @NonNull ParserParameters apply(final @NonNull Greedy greedy, final @NonNull TypeToken<?> typeToken) {
            return ParserParameters.single(StandardParameters.GREEDY, true);
        }
    }
}
