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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.aggregate.AggregateParser;
import cloud.commandframework.arguments.aggregate.AggregateResultMapper;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.types.tuples.Tuple;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class CompoundParser<T extends Tuple, C, O> implements AggregateParser<C, O> {

    private final List<CommandComponent<C>> components;
    private final BiFunction<C, T, O> mapper;
    private final Function<Object[], T> tupleFactory;
    private final TypeToken<O> valueType;

    @SuppressWarnings({"unchecked", "rawtypes"})
    CompoundParser(
            final @NonNull Object[] names,
            final @NonNull Object[] types,
            final @NonNull Object[] parsers,
            final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
            final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory,
            final @NonNull TypeToken<O> valueType
    ) {
        this.mapper = mapper;
        this.tupleFactory = tupleFactory;
        this.valueType = valueType;
        this.components = new ArrayList<>(parsers.length);
        for (int i = 0; i < parsers.length; i++) {
            final CommandComponent component = CommandComponent.builder()
                    .parser((ArgumentParser) parsers[i])
                    .name((String) names[i])
                    .valueType((Class) types[i])
                    .build();
            this.components.add(component);
        }
    }

    @Override
    public @NonNull List<@NonNull CommandComponent<C>> components() {
        return Collections.unmodifiableList(this.components);
    }

    @Override
    public @NonNull AggregateResultMapper<C, O> mapper() {
        return (commandContext, context) -> {
            final Object[] values = this.components.stream().map(CommandComponent::name).map(context::get).toArray();
            final T tuple = this.tupleFactory.apply(values);
            return CompletableFuture.completedFuture(
                    ArgumentParseResult.success(this.mapper.apply(commandContext.sender(), tuple))
            );
        };
    }

    @Override
    public @NonNull TypeToken<O> valueType() {
        return this.valueType;
    }
}
