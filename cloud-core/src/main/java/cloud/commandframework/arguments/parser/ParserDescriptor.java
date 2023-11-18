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

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public final class ParserDescriptor<C, T> {

    private final ArgumentParser<C, T> parser;
    private final TypeToken<T> valueType;

    /**
     * Creates a new parser descriptor.
     *
     * @param <C> the command sender type
     * @param <T> the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    public static <C, T> @NonNull ParserDescriptor<C, T> of(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType
    ) {
        return new ParserDescriptor<>(parser, valueType);
    }

    /**
     * Creates a new parser descriptor.
     *
     * @param <C> the command sender type
     * @param <T> the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    public static <C, T> @NonNull ParserDescriptor<C, T> of(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType
    ) {
        return new ParserDescriptor<>(parser, TypeToken.get(valueType));
    }

    private ParserDescriptor(final @NonNull ArgumentParser<C, T> parser, final @NonNull TypeToken<T> valueType) {
        this.parser = parser;
        this.valueType = valueType;
    }

    /**
     * Returns the parser.
     *
     * @return the parser
     */
    public @NonNull ArgumentParser<C, T> parser() {
        return this.parser;
    }

    /**
     * Returns the type of values produced by the parser.
     *
     * @return the type of values produced by the parser
     */
    public @NonNull TypeToken<T> valueType() {
        return this.valueType;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final ParserDescriptor<?, ?> that = (ParserDescriptor<?, ?>) object;
        return Objects.equals(this.parser, that.parser) && Objects.equals(this.valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parser, this.valueType);
    }
}
