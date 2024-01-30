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
package org.incendo.cloud.annotations.descriptor;

import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.internal.ImmutableBuilder;
import org.incendo.cloud.suggestion.SuggestionProvider;

@ImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE)
public interface ArgumentDescriptor extends Descriptor {

    /**
     * Creates a new builder.
     *
     * @return the created builder
     */
    static ImmutableArgumentDescriptor.@NonNull Builder builder() {
        return ImmutableArgumentDescriptor.builder();
    }

    /**
     * Returns the parameter.
     *
     * @return the parameter
     */
    @NonNull Parameter parameter();

    /**
     * Returns the argument name
     *
     * @return the argument name
     */
    @Override
    @NonNull String name();

    /**
     * Returns the name of the parser to use. If {@code null} the default parser for the parameter type will be used.
     *
     * @return the parser name, or {@code null}
     */
    @Nullable String parserName();

    /**
     * Returns the name of the suggestion provider to use. If the string is {@code null}, the default
     * provider for the argument parser will be used. Otherwise,
     * the {@link org.incendo.cloud.parser.ParserRegistry} instance in the
     * {@link org.incendo.cloud.CommandManager} will be queried for a matching suggestion provider.
     * <p>
     * For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link org.incendo.cloud.parser.ParserRegistry#registerSuggestionProvider(String, SuggestionProvider)}.
     * The registry instance can be retrieved using {@link org.incendo.cloud.CommandManager#parserRegistry()}.
     *
     * @return the name of the suggestion provider, or {@code null}
     */
    @Nullable String suggestions();

    /**
     * Returns the default value.
     *
     * @return the default value, or {@code null}
     */
    @Nullable DefaultValue<?, ?> defaultValue();

    /**
     * Returns the description of the argument.
     *
     * @return the argument description, or {@code null}
     */
    @Nullable Description description();
}
