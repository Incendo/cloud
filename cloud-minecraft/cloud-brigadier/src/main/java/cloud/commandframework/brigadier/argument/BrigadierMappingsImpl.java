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
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("unchecked")
final class BrigadierMappingsImpl<C, S> implements BrigadierMappings<C, S> {

    private final Map<Class<?>, BrigadierMapping<?, ?, S>> mappers = new HashMap<>();

    @Override
    public @Nullable <T, K extends ArgumentParser<C, T>> BrigadierMapping<C, K, S> mapping(final @NonNull Class<K> parserType) {
        final BrigadierMapping<?, ?, S> mapper = this.mappers.get(parserType);
        if (mapper == null) {
            return null;
        }
        return (BrigadierMapping<C, K, S>) mapper;
    }

    @Override
    public <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(
            final @NonNull Class<K> parserType,
            final @NonNull BrigadierMapping<?, ?, S> mapping
    ) {
        this.mappers.put(parserType, mapping);
    }
}
