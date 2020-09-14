//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.components.parser;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.annotations.specifier.Range;
import com.intellectualsites.commands.components.standard.BooleanComponent;
import com.intellectualsites.commands.components.standard.ByteComponent;
import com.intellectualsites.commands.components.standard.CharComponent;
import com.intellectualsites.commands.components.standard.DoubleComponent;
import com.intellectualsites.commands.components.standard.EnumComponent;
import com.intellectualsites.commands.components.standard.FloatComponent;
import com.intellectualsites.commands.components.standard.IntegerComponent;
import com.intellectualsites.commands.components.standard.ShortComponent;
import com.intellectualsites.commands.components.standard.StringComponent;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Standard implementation of {@link ParserRegistry}
 *
 * @param <C> Command sender type
 */
public final class StandardParserRegistry<C extends CommandSender> implements ParserRegistry<C> {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPINGS = ImmutableMap.<Class<?>, Class<?>>builder()
            .put(char.class, Character.class)
            .put(int.class, Integer.class)
            .put(short.class, Short.class)
            .put(byte.class, Byte.class)
            .put(float.class, Float.class)
            .put(double.class, Double.class)
            .put(long.class, Long.class)
            .put(boolean.class, Boolean.class)
            .build();

    private final Map<TypeToken<?>, Function<ParserParameters, ComponentParser<C, ?>>> parserSuppliers = new HashMap<>();
    private final Map<Class<? extends Annotation>, BiFunction<? extends Annotation, TypeToken<?>, ParserParameters>>
            annotationMappers = new HashMap<>();

    /**
     * Construct a new {@link StandardParserRegistry} instance. This will also
     * register all standard annotation mappers and parser suppliers
     */
    public StandardParserRegistry() {
        /* Register standard mappers */
        this.<Range, Number>registerAnnotationMapper(Range.class, new RangeMapper<>());

        /* Register standard types */
        this.registerParserSupplier(TypeToken.of(Byte.class), options ->
                new ByteComponent.ByteParser<C>((byte) options.get(StandardParameters.RANGE_MIN, Byte.MIN_VALUE),
                                                (byte) options.get(StandardParameters.RANGE_MAX, Byte.MAX_VALUE)));
        this.registerParserSupplier(TypeToken.of(Short.class), options ->
                new ShortComponent.ShortParser<C>((short) options.get(StandardParameters.RANGE_MIN, Short.MIN_VALUE),
                                                  (short) options.get(StandardParameters.RANGE_MAX, Short.MAX_VALUE)));
        this.registerParserSupplier(TypeToken.of(Integer.class), options ->
                new IntegerComponent.IntegerParser<C>((int) options.get(StandardParameters.RANGE_MIN, Integer.MIN_VALUE),
                                                      (int) options.get(StandardParameters.RANGE_MAX, Integer.MAX_VALUE)));
        this.registerParserSupplier(TypeToken.of(Float.class), options ->
                new FloatComponent.FloatParser<C>((float) options.get(StandardParameters.RANGE_MIN, Float.MIN_VALUE),
                                                  (float) options.get(StandardParameters.RANGE_MAX, Float.MAX_VALUE)));
        this.registerParserSupplier(TypeToken.of(Double.class), options ->
                new DoubleComponent.DoubleParser<C>((double) options.get(StandardParameters.RANGE_MIN, Double.MIN_VALUE),
                                                    (double) options.get(StandardParameters.RANGE_MAX, Double.MAX_VALUE)));
        this.registerParserSupplier(TypeToken.of(Character.class), options -> new CharComponent.CharacterParser<C>());
        /* Make this one less awful */
        this.registerParserSupplier(TypeToken.of(String.class), options -> new StringComponent.StringParser<C>(
                StringComponent.StringMode.SINGLE, (context, s) -> Collections.emptyList()));
        /* Add options to this */
        this.registerParserSupplier(TypeToken.of(Boolean.class), options -> new BooleanComponent.BooleanParser<>(false));
    }

    @Override
    public <T> void registerParserSupplier(@Nonnull final TypeToken<T> type,
                                           @Nonnull final Function<ParserParameters, ComponentParser<C, ?>> supplier) {
        this.parserSuppliers.put(type, supplier);
    }

    @Override
    public <A extends Annotation, T> void registerAnnotationMapper(@Nonnull final Class<A> annotation,
                                                                   @Nonnull final BiFunction<A, TypeToken<?>,
                                                                           ParserParameters> mapper) {
        this.annotationMappers.put(annotation, mapper);
    }

    @Nonnull
    @Override
    public ParserParameters parseAnnotations(@Nonnull final TypeToken<?> parsingType,
                                             @Nonnull final Collection<? extends Annotation> annotations) {
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

    @Nonnull
    @Override
    public <T> Optional<ComponentParser<C, T>> createParser(@Nonnull final TypeToken<T> type,
                                                            @Nonnull final ParserParameters parserParameters) {
        final TypeToken<?> actualType;
        if (type.isPrimitive()) {
            actualType = TypeToken.of(PRIMITIVE_MAPPINGS.get(type.getRawType()));
        } else {
            actualType = type;
        }
        final Function<ParserParameters, ComponentParser<C, ?>> producer = this.parserSuppliers.get(actualType);
        if (producer == null) {
            /* Give enums special treatment */
            if (actualType.isSubtypeOf(Enum.class)) {
                @SuppressWarnings("all")
                final EnumComponent.EnumParser enumComponent = new EnumComponent.EnumParser((Class<Enum>)
                                                                                            actualType.getRawType());
                // noinspection all
                return Optional.of(enumComponent);
            }
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final ComponentParser<C, T> parser = (ComponentParser<C, T>) producer.apply(
                parserParameters);
        return Optional.of(parser);
    }


    private static final class RangeMapper<T> implements BiFunction<Range, TypeToken<?>, ParserParameters> {

        @Override
        public ParserParameters apply(final Range range, final TypeToken<?> type) {
            final Class<?> clazz;
            if (type.isPrimitive()) {
                clazz = PRIMITIVE_MAPPINGS.get(type.getRawType());
            } else {
                clazz = type.getRawType();
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

}
