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
package cloud.commandframework.component;

import cloud.commandframework.component.preprocessor.ComponentPreprocessor;
import cloud.commandframework.description.Description;
import cloud.commandframework.key.CloudKey;
import cloud.commandframework.key.CloudKeyHolder;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("unchecked")
@API(status = API.Status.STABLE)
public final class TypedCommandComponent<C, T> extends CommandComponent<C> implements CloudKeyHolder<T>, ParserDescriptor<C, T> {

    TypedCommandComponent(
            final @NonNull String name,
            final @NonNull ArgumentParser<C, ?> parser,
            final @NonNull TypeToken<?> valueType,
            final @NonNull Description description,
            final @NonNull ComponentType componentType,
            final @Nullable DefaultValue<C, ?> defaultValue,
            final @NonNull SuggestionProvider<C> suggestionProvider,
            final @NonNull Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors
    ) {
        super(name, parser, valueType, description, componentType, defaultValue, suggestionProvider, componentPreprocessors);
    }

    @Override
    public @NonNull TypeToken<T> valueType() {
        return (TypeToken<T>) super.valueType();
    }

    @Override
    public @NonNull ArgumentParser<C, T> parser() {
        return (ArgumentParser<C, T>) super.parser();
    }

    @Override
    public @Nullable DefaultValue<C, T> defaultValue() {
        return (DefaultValue<C, T>) super.defaultValue();
    }

    @Override
    public @NonNull CloudKey<T> key() {
        return CloudKey.of(this.name(), this.valueType());
    }
}
