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
package cloud.commandframework.arguments.aggregate;

import cloud.commandframework.CommandComponent;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

final class AggregateCommandParserImpl<C, O> implements AggregateCommandParser<C, O> {

    private final List<CommandComponent<C>> components;
    private final TypeToken<O> valueType;
    private final AggregateResultMapper<C, O> mapper;

    AggregateCommandParserImpl(
            final @NonNull List<CommandComponent<C>> components,
            final @NonNull TypeToken<O> valueType,
            final @NonNull AggregateResultMapper<C, O> mapper
    ) {
        this.components = components;
        this.valueType = valueType;
        this.mapper = mapper;
    }

    @Override
    public @NonNull List<@NonNull CommandComponent<C>> components() {
        return Collections.unmodifiableList(this.components);
    }

    @Override
    public @NonNull AggregateResultMapper<C, O> mapper() {
        return this.mapper;
    }

    @Override
    public @NonNull TypeToken<O> valueType() {
        return this.valueType;
    }
}
