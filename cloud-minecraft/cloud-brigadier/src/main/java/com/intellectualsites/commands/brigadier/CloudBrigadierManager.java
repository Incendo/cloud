//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.brigadier;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.components.standard.BooleanComponent;
import com.intellectualsites.commands.components.standard.ByteComponent;
import com.intellectualsites.commands.components.standard.DoubleComponent;
import com.intellectualsites.commands.components.standard.FloatComponent;
import com.intellectualsites.commands.components.standard.IntegerComponent;
import com.intellectualsites.commands.components.standard.ShortComponent;
import com.intellectualsites.commands.components.standard.StringComponent;
import com.intellectualsites.commands.sender.CommandSender;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Manager used to map cloud {@link com.intellectualsites.commands.Command}
 * <p>
 * The structure of this class is largely inspired by
 * <a href="https://github.com/aikar/commands/blob/master/brigadier/src/main/java/co.aikar.commands/ACFBrigadierManager.java">
 * ACFBrigadiermanager</a> in the ACF project, which was originally written by MiniDigger and licensed under the MIT license.
 *
 * @param <C> Command sender type
 * @param <S> Brigadier sender type
 */
public final class CloudBrigadierManager<C extends CommandSender, S> {

    private final Map<Class<?>, Function<? extends CommandComponent<C, ?>,
            ? extends ArgumentType<?>>> mappers;

    /**
     * Create a new cloud brigadier manager
     */
    public CloudBrigadierManager() {
        this.mappers = Maps.newHashMap();
        this.registerInternalMappings();
    }

    private void registerInternalMappings() {
        /* Map byte, short and int to IntegerArgumentType */
        this.registerMapping(new TypeToken<ByteComponent<C>>() {
        }, component -> {
            final boolean hasMin = component.getMin() != Byte.MIN_VALUE;
            final boolean hasMax = component.getMax() != Byte.MAX_VALUE;
            if (hasMin) {
                return IntegerArgumentType.integer(component.getMin(), component.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Byte.MIN_VALUE, component.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        this.registerMapping(new TypeToken<ShortComponent<C>>() {
        }, component -> {
            final boolean hasMin = component.getMin() != Short.MIN_VALUE;
            final boolean hasMax = component.getMax() != Short.MAX_VALUE;
            if (hasMin) {
                return IntegerArgumentType.integer(component.getMin(), component.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Short.MIN_VALUE, component.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        this.registerMapping(new TypeToken<IntegerComponent<C>>() {
        }, component -> {
            final boolean hasMin = component.getMin() != Integer.MIN_VALUE;
            final boolean hasMax = component.getMax() != Integer.MAX_VALUE;

            System.out.println("Constructing new IntegerArgumentType with min " + hasMin + " | max " + hasMax);

            if (hasMin) {
                return IntegerArgumentType.integer(component.getMin(), component.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Integer.MIN_VALUE, component.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        /* Map float to FloatArgumentType */
        this.registerMapping(new TypeToken<FloatComponent<C>>() {
        }, component -> {
            final boolean hasMin = component.getMin() != Float.MIN_VALUE;
            final boolean hasMax = component.getMax() != Float.MAX_VALUE;
            if (hasMin) {
                return FloatArgumentType.floatArg(component.getMin(), component.getMax());
            } else if (hasMax) {
                return FloatArgumentType.floatArg(Float.MIN_VALUE, component.getMax());
            } else {
                return FloatArgumentType.floatArg();
            }
        });
        /* Map double to DoubleArgumentType */
        this.registerMapping(new TypeToken<DoubleComponent<C>>() {
        }, component -> {
            final boolean hasMin = component.getMin() != Double.MIN_VALUE;
            final boolean hasMax = component.getMax() != Double.MAX_VALUE;
            if (hasMin) {
                return DoubleArgumentType.doubleArg(component.getMin(), component.getMax());
            } else if (hasMax) {
                return DoubleArgumentType.doubleArg(Double.MIN_VALUE, component.getMax());
            } else {
                return DoubleArgumentType.doubleArg();
            }
        });
        /* Map boolean to BoolArgumentType */
        this.registerMapping(new TypeToken<BooleanComponent<C>>() {
        }, component -> BoolArgumentType.bool());
        /* Map String properly to StringArgumentType */
        this.registerMapping(new TypeToken<StringComponent<C>>() {
        }, component -> {
            switch (component.getStringMode()) {
                case SINGLE:
                    return StringArgumentType.word();
                case QUOTED:
                    return StringArgumentType.string();
                case GREEDY:
                    return StringArgumentType.greedyString();
                default:
                    return StringArgumentType.word();
            }
        });
    }

    /**
     * Register a cloud-Brigadier mapping
     *
     * @param componentType cloud component type
     * @param mapper        mapper function
     * @param <T>           cloud component value type
     * @param <K>           cloud component type
     * @param <O>           Brigadier argument type value
     */
    public <T, K extends CommandComponent<C, T>, O> void registerMapping(@Nonnull final TypeToken<K> componentType,
                                                                         @Nonnull final Function<? extends K,
                                                                                 ? extends ArgumentType<O>> mapper) {
        this.mappers.put(componentType.getRawType(), mapper);
    }

    /**
     * Get a Brigadier {@link ArgumentType} from a cloud {@link CommandComponent}
     *
     * @param componentType cloud component type
     * @param component     cloud component
     * @param <T>           cloud component value type (generic)
     * @param <K>           cloud component type (generic)
     * @return Brigadier argument type
     */
    @Nonnull
    @SuppressWarnings("all")
    public <T, K extends CommandComponent<?, ?>> ArgumentType<?> getArgument(@Nonnull final TypeToken<T> componentType,
                                                                             @Nonnull final K component) {
        final CommandComponent<C, ?> commandComponent = (CommandComponent<C, ?>) component;
        final Function function = this.mappers.getOrDefault(componentType.getRawType(), t ->
                createDefaultMapper((CommandComponent<C, T>) component));
        return (ArgumentType<?>) function.apply(commandComponent);
    }

    @Nonnull
    private <T, K extends CommandComponent<C, T>> ArgumentType<?> createDefaultMapper(@Nonnull final CommandComponent<C, T>
                                                                                              component) {
        return StringArgumentType.string();
    }

    /**
     * Create a literal command from Brigadier command info, and a cloud command instance
     *
     * @param cloudCommand       Cloud root command
     * @param root               Brigadier root command
     * @param suggestionProvider Brigadier suggestions provider
     * @param executor           Brigadier command executor
     * @param permissionChecker  Permission checker
     * @return Constructed literal command node
     */
    @Nonnull
    public LiteralCommandNode<S> createLiteralCommandNode(@Nonnull final CommandTree.Node<CommandComponent<C, ?>> cloudCommand,
                                                          @Nonnull final LiteralCommandNode<S> root,
                                                          @Nonnull final SuggestionProvider<S> suggestionProvider,
                                                          @Nonnull final com.mojang.brigadier.Command<S> executor,
                                                          @Nonnull final BiPredicate<S, String> permissionChecker) {
        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(sender -> permissionChecker.test(sender, cloudCommand.getNodeMeta().getOrDefault("permission", "")));
        if (cloudCommand.isLeaf() && cloudCommand.getValue() != null && cloudCommand.getValue().getOwningCommand() != null) {
            literalArgumentBuilder.executes(executor);
        }
        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final CommandTree.Node<CommandComponent<C, ?>> child : cloudCommand.getChildren()) {
            constructedRoot.addChild(this.constructCommandNode(child, permissionChecker, executor, suggestionProvider));
        }
        return constructedRoot;
    }

    private CommandNode<S> constructCommandNode(@Nonnull final CommandTree.Node<CommandComponent<C, ?>> root,
                                                @Nonnull final BiPredicate<S, String> permissionChecker,
                                                @Nonnull final com.mojang.brigadier.Command<S> executor,
                                                @Nonnull final SuggestionProvider<S> suggestionProvider) {
        CommandNode<S> commandNode;
        if (root.getValue() instanceof StaticComponent) {
            final LiteralArgumentBuilder<S> argumentBuilder = LiteralArgumentBuilder.<S>literal(root.getValue().getName())
                    .requires(sender -> permissionChecker.test(sender, root.getNodeMeta().getOrDefault("permission", "")));
            if (root.isLeaf()) {
                argumentBuilder.executes(executor);
            }
            commandNode = argumentBuilder.build();
        } else {
            @SuppressWarnings("unchecked") final RequiredArgumentBuilder<S, Object> builder = RequiredArgumentBuilder
                    .<S, Object>argument(root.getValue().getName(),
                                         (ArgumentType<Object>) getArgument(TypeToken.of(root.getValue().getClass()),
                                                                            root.getValue()))
                    .suggests(suggestionProvider)
                    .requires(sender -> permissionChecker.test(sender, root.getNodeMeta().getOrDefault("permission", "")));
            if (root.isLeaf() || !root.getValue().isRequired()) {
                builder.executes(executor);
            }
            commandNode = builder.build();
        }
        for (final CommandTree.Node<CommandComponent<C, ?>> node : root.getChildren()) {
            commandNode.addChild(constructCommandNode(node, permissionChecker, executor, suggestionProvider));
        }
        return commandNode;
    }

}
