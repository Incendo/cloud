//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.sponge;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.ByteArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.ShortArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class SpongeParserMapper<C> {

    private static final Class<?> DELEGATING_SUGGESTIONS_PROVIDER; // todo - ugly

    static {
        try {
            DELEGATING_SUGGESTIONS_PROVIDER = Class.forName("cloud.commandframework.arguments.DelegatingSuggestionsProvider");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<Class<?>, Function<Object, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>>> mappers
            = new HashMap<>();

    SpongeParserMapper() {
        this.initStandardMappers();
    }

    @SuppressWarnings("unchecked")
    CommandTreeNode<? extends CommandTreeNode.Argument<?>> toSponge(final CommandArgument<C, ?> value) {
        final CommandTreeNode<? extends CommandTreeNode.Argument<?>> result;
        final ArgumentParser<C, ?> parser = value.getParser();
        if (parser instanceof NodeSupplyingArgumentParser) {
            result = ((NodeSupplyingArgumentParser<C, ?>) parser).node();
        } else {
            final Function<Object, CommandTreeNode.Argument<? extends CommandTreeNode.Argument<?>>> mapper =
                    this.mappers.get(parser.getClass());
            if (mapper != null) {
                result = mapper.apply(parser);
            } else {
                result = ClientCompletionKeys.STRING.get().createNode().word();
            }
        }
        final boolean customSuggestionsProvider = !DELEGATING_SUGGESTIONS_PROVIDER.isInstance(value.getSuggestionsProvider());
        if (customSuggestionsProvider) {
            // ignore intellij, it won't compile without this cast
            return (CommandTreeNode<? extends CommandTreeNode.Argument<?>>) result.customSuggestions();
        }
        return result;
    }

    private void initStandardMappers() {
        this.registerMapper(StringArgument.StringParser.class, stringParser -> {
            final StringArgument.StringMode mode = stringParser.getStringMode();
            if (mode == StringArgument.StringMode.SINGLE) {
                return ClientCompletionKeys.STRING.get().createNode().word();
            } else if (mode == StringArgument.StringMode.QUOTED) {
                return ClientCompletionKeys.STRING.get().createNode();
            } else if (mode == StringArgument.StringMode.GREEDY) {
                return ClientCompletionKeys.STRING.get().createNode().greedy();
            }
            throw new IllegalArgumentException("Unknown string mode '" + mode + "'!");
        });
        this.registerMapper(ByteArgument.ByteParser.class, byteParser -> {
            final CommandTreeNode.Range<Integer> node = ClientCompletionKeys.INTEGER.get().createNode();
            node.min((int) byteParser.getMin());
            node.max((int) byteParser.getMax());
            return node;
        });
        this.registerMapper(ShortArgument.ShortParser.class, shortParser -> {
            final CommandTreeNode.Range<Integer> node = ClientCompletionKeys.INTEGER.get().createNode();
            node.min((int) shortParser.getMin());
            node.max((int) shortParser.getMax());
            return node;
        });
        this.registerMapper(IntegerArgument.IntegerParser.class, integerParser -> {
            final CommandTreeNode.Range<Integer> node = ClientCompletionKeys.INTEGER.get().createNode();
            final boolean hasMin = integerParser.getMin() != Integer.MIN_VALUE;
            final boolean hasMax = integerParser.getMax() != Integer.MAX_VALUE;
            if (hasMin) {
                node.min(integerParser.getMin());
            }
            if (hasMax) {
                node.max(integerParser.getMax());
            }
            return node;
        });
        this.registerMapper(FloatArgument.FloatParser.class, floatParser -> {
            final CommandTreeNode.Range<Float> node = ClientCompletionKeys.FLOAT.get().createNode();
            final boolean hasMin = floatParser.getMin() != Float.NEGATIVE_INFINITY;
            final boolean hasMax = floatParser.getMax() != Float.POSITIVE_INFINITY;
            if (hasMin) {
                node.min(floatParser.getMin());
            }
            if (hasMax) {
                node.max(floatParser.getMax());
            }
            return node;
        });
        this.registerMapper(DoubleArgument.DoubleParser.class, doubleParser -> {
            final CommandTreeNode.Range<Double> node = ClientCompletionKeys.DOUBLE.get().createNode();
            final boolean hasMin = doubleParser.getMin() != Double.NEGATIVE_INFINITY;
            final boolean hasMax = doubleParser.getMax() != Double.POSITIVE_INFINITY;
            if (hasMin) {
                node.min(doubleParser.getMin());
            }
            if (hasMax) {
                node.max(doubleParser.getMax());
            }
            return node;
        });
        this.registerMapper(BooleanArgument.BooleanParser.class, booleanParser -> ClientCompletionKeys.BOOL.get().createNode());
        this.registerMapper(
                FlagArgument.FlagArgumentParser.class,
                flagArgumentParser -> ClientCompletionKeys.STRING.get().createNode().greedy()
        );
        this.registerMapper(
                StringArrayArgument.StringArrayParser.class,
                stringArrayParser -> ClientCompletionKeys.STRING.get().createNode().greedy()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <A extends ArgumentParser<?, ?>> void registerMapper(
            final @NonNull Class<A> cloudType,
            final @NonNull Function<A, CommandTreeNode.Argument<?>> mapper
    ) {
        this.mappers.put(cloudType, (Function) mapper);
    }

}
