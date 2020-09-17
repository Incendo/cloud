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
package com.intellectualsites.commands;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.annotations.specifier.Range;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.arguments.parser.ParserParameters;
import com.intellectualsites.commands.arguments.parser.ParserRegistry;
import com.intellectualsites.commands.arguments.parser.StandardParameters;
import com.intellectualsites.commands.arguments.parser.StandardParserRegistry;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Collections;

public class ParserRegistryTest {

    public static final int RANGE_MIN = 10;
    public static final int RANGE_MAX = 100;

    @Test
    void testParserRegistry() {
        final ParserRegistry<TestCommandSender> parserRegistry = new StandardParserRegistry<>();
        final Range range = new Range() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Range.class;
            }

            @Nonnull
            @Override
            public String min() {
                return Integer.toString(RANGE_MIN);
            }

            @Nonnull
            @Override
            public String max() {
                return Integer.toString(RANGE_MAX);
            }
        };


        final TypeToken<?> parsedType = TypeToken.of(int.class);
        final ParserParameters parserParameters = parserRegistry.parseAnnotations(parsedType, Collections.singleton(range));
        Assertions.assertTrue(parserParameters.has(StandardParameters.RANGE_MIN));
        Assertions.assertTrue(parserParameters.has(StandardParameters.RANGE_MAX));
        final ArgumentParser<TestCommandSender, ?> parser = parserRegistry.createParser(parsedType,
                                                                                    parserParameters)
                                                                      .orElseThrow(
                                                                               () -> new NullPointerException("No parser found"));
        Assertions.assertTrue(parser instanceof IntegerArgument.IntegerParser);
        @SuppressWarnings("unchecked")
        final IntegerArgument.IntegerParser<TestCommandSender> integerParser =
                (IntegerArgument.IntegerParser<TestCommandSender>) parser;
        Assertions.assertEquals(RANGE_MIN, integerParser.getMin());
        Assertions.assertEquals(RANGE_MAX, integerParser.getMax());
    }

}
