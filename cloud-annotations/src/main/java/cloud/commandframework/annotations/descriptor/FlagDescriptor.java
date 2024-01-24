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
package cloud.commandframework.annotations.descriptor;

import cloud.commandframework.Description;
import cloud.commandframework.internal.ImmutableBuilder;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.suggestion.SuggestionProvider;
import java.lang.reflect.Parameter;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

@ImmutableBuilder
@Value.Immutable
@API(status = API.Status.STABLE, since = "2.0.0")
public interface FlagDescriptor extends Descriptor {

    /**
     * Creates a new builder.
     *
     * @return the created builder
     */
    static ImmutableFlagDescriptor.@NonNull Builder builder() {
        return ImmutableFlagDescriptor.builder();
    }

    /**
     * Returns the parameter.
     *
     * @return the parameter
     */
    @NonNull Parameter parameter();

    /**
     * Returns the flag name.
     *
     * @return the flag name
     */
    @Override
    @NonNull String name();

    /**
     * Returns an unmodifiable view of the flag aliases.
     *
     * @return the flag aliases
     */
    @NonNull Collection<@NonNull String> aliases();

    /**
     * Returns the name of the parser to use. If {@code null} the default parser for the parameter type will be used.
     *
     * @return the parser name, or {@code null}
     */
    @Nullable String parserName();

    /**
     * Returns the name of the suggestion provider to use. If the string is {@code null}, the default
     * provider for the argument parser will be used. Otherwise,
     * the {@link cloud.commandframework.parser.ParserRegistry} instance in the
     * {@link cloud.commandframework.CommandManager} will be queried for a matching suggestion provider.
     * <p>
     * For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link cloud.commandframework.parser.ParserRegistry#registerSuggestionProvider(String, SuggestionProvider)}.
     * The registry instance can be retrieved using {@link cloud.commandframework.CommandManager#parserRegistry()}.
     *
     * @return the name of the suggestion provider, or {@code null}
     */
    @Nullable String suggestions();

    /**
     * Returns the permission of the flag.
     *
     * @return the flag permission, or {@code null}
     */
    @Nullable Permission permission();

    /**
     * Returns the description of the flag.
     *
     * @return the flag description, or {@code null}
     */
    @Nullable Description description();

    /**
     * Returns whether the flag is repeatable.
     *
     * @return whether the flag is repeatable
     */
    boolean repeatable();
}
