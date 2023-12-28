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
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.INTERNAL, since = "2.0.0")
public interface BrigadierMappings<C, S> {

    /**
     * Returns a new instance of the default implementation.
     *
     * @param <C> cloud command sender type
     * @param <S> brigadier command source type
     * @return the mapping instance
     */
    static <C, S> @NonNull BrigadierMappings<C, S> create() {
        return new BrigadierMappingsImpl<>();
    }

    /**
     * Returns the mapper for the given {@code parserType}.
     *
     * @param <T>        the type produced by the parser
     * @param <K>        the parser type
     * @param parserType the parser type
     * @return the mapping, or {@code null}
     */
    <T, K extends ArgumentParser<C, T>> @Nullable BrigadierMapping<C, K, S> mapping(@NonNull Class<K> parserType);

    /**
     * Registers the {@code mapping} for the given {@code parserType}.
     *
     * @param <T>        the type produced by the parser
     * @param <K>        the parser type
     * @param parserType the parser type
     * @param mapping    the mapping
     */
    default <T, K extends ArgumentParser<C, T>> void registerMapping(
            @NonNull Class<K> parserType,
            @NonNull BrigadierMapping<?, K, S> mapping
    ) {
        this.registerMappingUnsafe(parserType, mapping);
    }

    /**
     * Registers the {@code mapping} for the given {@code parserType}.
     *
     * @param <K>        the parser type
     * @param parserType the parser type
     * @param mapping    the mapping
     */
    <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(
            @NonNull Class<K> parserType,
            @NonNull BrigadierMapping<?, ?, S> mapping
    );
}
