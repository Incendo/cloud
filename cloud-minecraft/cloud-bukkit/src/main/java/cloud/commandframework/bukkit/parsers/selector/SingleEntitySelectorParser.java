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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.bukkit.data.SingleEntitySelector;
import org.apiguardian.api.API;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser for {@link SingleEntitySelector}. On Minecraft 1.13+
 * this argument uses Minecraft's built-in entity selector argument for parsing
 * and suggestions. On prior versions, this argument will suggest nothing and
 * always fail parsing with {@link SelectorUnsupportedException}.
 *
 * @param <C> sender type
 */
public final class SingleEntitySelectorParser<C> extends SelectorUtils.EntitySelectorParser<C, SingleEntitySelector> {

    /**
     * Creates a new single entity selector parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, SingleEntitySelector> singleEntitySelectorParser() {
        return ParserDescriptor.of(new SingleEntitySelectorParser<>(), SingleEntitySelector.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #singleEntitySelectorParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, SingleEntitySelector> singleEntitySelectorComponent() {
        return CommandComponent.<C, SingleEntitySelector>builder().parser(singleEntitySelectorParser());
    }

    /**
     * Creates a new {@link SingleEntitySelectorParser}.
     */
    public SingleEntitySelectorParser() {
        super(true);
    }

    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    @Override
    public SingleEntitySelector mapResult(
            final @NonNull String input,
            final SelectorUtils.@NonNull EntitySelectorWrapper wrapper
    ) {
        final Entity entity = wrapper.singleEntity();
        return new SingleEntitySelector() {
            @Override
            public @NonNull Entity single() {
                return entity;
            }

            @Override
            public @NonNull String inputString() {
                return input;
            }
        };
    }
}
