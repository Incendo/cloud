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
package cloud.commandframework;

import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.parser.StandardParserRegistry;
import cloud.commandframework.arguments.standard.IntegerArgument;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class ParserRegistryTest {

    private static final int RANGE_MIN = 10;
    private static final int RANGE_MAX = 100;
    private static final Range RANGE = new Range() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Range.class;
        }

        @Override
        public @NonNull String min() {
            return Integer.toString(RANGE_MIN);
        }

        @Override
        public @NonNull String max() {
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

    @Test
    void parsing_range_annotation_results_in_correct_parser_parameters() {
        // Given
        final ParserRegistry<TestCommandSender> parserRegistry = new StandardParserRegistry<>();
        final TypeToken<?> parsedType = TypeToken.get(int.class);

        // When
        final ParserParameters parserParameters = parserRegistry.parseAnnotations(parsedType, Collections.singleton(RANGE));

        // Then
        assertThat(parserParameters.has(StandardParameters.RANGE_MIN)).isTrue();
        assertThat(parserParameters.has(StandardParameters.RANGE_MAX)).isTrue();
    }

    @Test
    void creating_integer_parser_from_parameters_results_in_correct_parser() {
        final ParserRegistry<TestCommandSender> parserRegistry = new StandardParserRegistry<>();
        final TypeToken<?> parsedType = TypeToken.get(int.class);

        final ParserParameters parserParameters = ParserParameters.empty();
        parserParameters.store(StandardParameters.RANGE_MIN, RANGE_MIN);
        parserParameters.store(StandardParameters.RANGE_MAX, RANGE_MAX);

        final ArgumentParser<TestCommandSender, ?> parser = parserRegistry.createParser(
                parsedType,
                parserParameters
        ).orElseThrow(() -> new NullPointerException("No parser found"));

        assertThat(parser).isInstanceOf(IntegerArgument.IntegerParser.class);

        @SuppressWarnings("unchecked") final IntegerArgument.IntegerParser<TestCommandSender> integerParser =
                (IntegerArgument.IntegerParser<TestCommandSender>) parser;

        assertThat(integerParser.getMin()).isEqualTo(RANGE_MIN);
        assertThat(integerParser.getMax()).isEqualTo(RANGE_MAX);
    }

    @Test
    void retrieving_integer_parser_from_parser_registry() {
        // Given
        final ParserRegistry<TestCommandSender> parserRegistry = new StandardParserRegistry<>();

        // When
        final Optional<?> parserOptional = parserRegistry.createParser(
                TypeToken.get(int.class),
                ParserParameters.empty()
        );

        // Then
        assertThat(parserOptional).isPresent();
    }

    @Test
    void retrieving_enum_parser_from_registry() {
        // Given
        final ParserRegistry<TestCommandSender> parserRegistry = new StandardParserRegistry<>();

        // When
        final Optional<?> parserOptional = parserRegistry.createParser(
                TypeToken.get(CommandManager.ManagerSettings.class),
                ParserParameters.empty()
        );

        // Then
        assertThat(parserOptional).isPresent();
    }
}
