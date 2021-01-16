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
package cloud.commandframework;

import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.parser.StandardParserRegistry;
import cloud.commandframework.arguments.standard.IntegerArgument;
import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;

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

            @Override
            public String min() {
                return Integer.toString(RANGE_MIN);
            }

            @Override
            public String max() {
                return Integer.toString(RANGE_MAX);
            }

            @Override
            public boolean equals(final Object obj) {
                if (!(obj instanceof Range)) {
                    return false;
                }
                final Range range = (Range) obj;
                return this.min().equals(range.min()) && this.max().equals(range.max());
            }

            @Override
            public int hashCode() {
                return Objects.hash(this.min(), this.max());
            }
        };


        final TypeToken<?> parsedType = TypeToken.get(int.class);
        final ParserParameters parserParameters = parserRegistry.parseAnnotations(parsedType, Collections.singleton(range));
        Assertions.assertTrue(parserParameters.has(StandardParameters.RANGE_MIN));
        Assertions.assertTrue(parserParameters.has(StandardParameters.RANGE_MAX));
        final ArgumentParser<TestCommandSender, ?> parser = parserRegistry.createParser(
                parsedType,
                parserParameters
        )
                .orElseThrow(
                        () -> new NullPointerException(
                                "No parser found"));
        Assertions.assertTrue(parser instanceof IntegerArgument.IntegerParser);
        @SuppressWarnings("unchecked") final IntegerArgument.IntegerParser<TestCommandSender> integerParser =
                (IntegerArgument.IntegerParser<TestCommandSender>) parser;
        Assertions.assertEquals(RANGE_MIN, integerParser.getMin());
        Assertions.assertEquals(RANGE_MAX, integerParser.getMax());

        /* Test integer */
        parserRegistry.createParser(TypeToken.get(int.class), ParserParameters.empty())
                .orElseThrow(() -> new IllegalArgumentException("No parser found for int.class"));

        /* Test Enum */
        parserRegistry.createParser(TypeToken.get(CommandManager.ManagerSettings.class), ParserParameters.empty())
                .orElseThrow(() -> new IllegalArgumentException("No parser found for enum"));
    }

}
