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
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.StaticArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.arguments.standard.BooleanArgument;
import com.intellectualsites.commands.arguments.standard.ByteArgument;
import com.intellectualsites.commands.arguments.standard.DoubleArgument;
import com.intellectualsites.commands.arguments.standard.FloatArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.arguments.standard.ShortArgument;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessingContext;
import com.intellectualsites.commands.permission.CommandPermission;
import com.intellectualsites.commands.permission.Permission;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Manager used to map cloud {@link Command}
 * <p>
 * The structure of this class is largely inspired by
 * <a href="https://github.com/aikar/commands/blob/master/brigadier/src/main/java/co.aikar.commands/ACFBrigadierManager.java">
 * ACFBrigadiermanager</a> in the ACF project, which was originally written by MiniDigger and licensed under the MIT license.
 *
 * @param <C> Command sender type
 * @param <S> Brigadier sender type
 */
public final class CloudBrigadierManager<C, S> {

    private final Map<Class<?>, Function<? extends ArgumentParser<C, ?>,
            ? extends ArgumentType<?>>> mappers;
    private final Map<Class<?>, Supplier<ArgumentType<?>>> defaultArgumentTypeSuppliers;
    private final Supplier<CommandContext<C>> dummyContextProvider;
    private final CommandManager<C> commandManager;

    /**
     * Create a new cloud brigadier manager
     *
     * @param commandManager       Command manager
     * @param dummyContextProvider Provider of dummy context for completions
     */
    public CloudBrigadierManager(@Nonnull final CommandManager<C> commandManager,
                                 @Nonnull final Supplier<CommandContext<C>>
                                         dummyContextProvider) {
        this.mappers = Maps.newHashMap();
        this.defaultArgumentTypeSuppliers = Maps.newHashMap();
        this.commandManager = commandManager;
        this.dummyContextProvider = dummyContextProvider;
        this.registerInternalMappings();
    }

    private void registerInternalMappings() {
        /* Map byte, short and int to IntegerArgumentType */
        this.registerMapping(new TypeToken<ByteArgument.ByteParser<C>>() {
        }, argument -> {
            final boolean hasMin = argument.getMin() != Byte.MIN_VALUE;
            final boolean hasMax = argument.getMax() != Byte.MAX_VALUE;
            if (hasMin) {
                return IntegerArgumentType.integer(argument.getMin(), argument.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Byte.MIN_VALUE, argument.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        this.registerMapping(new TypeToken<ShortArgument.ShortParser<C>>() {
        }, argument -> {
            final boolean hasMin = argument.getMin() != Short.MIN_VALUE;
            final boolean hasMax = argument.getMax() != Short.MAX_VALUE;
            if (hasMin) {
                return IntegerArgumentType.integer(argument.getMin(), argument.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Short.MIN_VALUE, argument.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        this.registerMapping(new TypeToken<IntegerArgument.IntegerParser<C>>() {
        }, argument -> {
            final boolean hasMin = argument.getMin() != Integer.MIN_VALUE;
            final boolean hasMax = argument.getMax() != Integer.MAX_VALUE;
            if (hasMin) {
                return IntegerArgumentType.integer(argument.getMin(), argument.getMax());
            } else if (hasMax) {
                return IntegerArgumentType.integer(Integer.MIN_VALUE, argument.getMax());
            } else {
                return IntegerArgumentType.integer();
            }
        });
        /* Map float to FloatArgumentType */
        this.registerMapping(new TypeToken<FloatArgument.FloatParser<C>>() {
        }, argument -> {
            final boolean hasMin = argument.getMin() != Float.MIN_VALUE;
            final boolean hasMax = argument.getMax() != Float.MAX_VALUE;
            if (hasMin) {
                return FloatArgumentType.floatArg(argument.getMin(), argument.getMax());
            } else if (hasMax) {
                return FloatArgumentType.floatArg(Float.MIN_VALUE, argument.getMax());
            } else {
                return FloatArgumentType.floatArg();
            }
        });
        /* Map double to DoubleArgumentType */
        this.registerMapping(new TypeToken<DoubleArgument.DoubleParser<C>>() {
        }, argument -> {
            final boolean hasMin = argument.getMin() != Double.MIN_VALUE;
            final boolean hasMax = argument.getMax() != Double.MAX_VALUE;
            if (hasMin) {
                return DoubleArgumentType.doubleArg(argument.getMin(), argument.getMax());
            } else if (hasMax) {
                return DoubleArgumentType.doubleArg(Double.MIN_VALUE, argument.getMax());
            } else {
                return DoubleArgumentType.doubleArg();
            }
        });
        /* Map boolean to BoolArgumentType */
        this.registerMapping(new TypeToken<BooleanArgument.BooleanParser<C>>() {
        }, argument -> BoolArgumentType.bool());
        /* Map String properly to StringArgumentType */
        this.registerMapping(new TypeToken<StringArgument.StringParser<C>>() {
        }, argument -> {
            switch (argument.getStringMode()) {
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
     * @param argumentType cloud argument type
     * @param mapper       mapper function
     * @param <T>          cloud argument value type
     * @param <K>          cloud argument type
     * @param <O>          Brigadier argument type value
     */
    public <T, K extends ArgumentParser<C, T>, O> void registerMapping(@Nonnull final TypeToken<K> argumentType,
                                                                        @Nonnull final Function<? extends K,
                                                                                ? extends ArgumentType<O>> mapper) {
        this.mappers.put(argumentType.getRawType(), mapper);
    }

    /**
     * Register a default mapping to between a class and a Brigadier argument type
     *
     * @param clazz    Type to map
     * @param supplier Supplier that supplies the argument type
     */
    public void registerDefaultArgumentTypeSupplier(@Nonnull final Class<?> clazz,
                                                    @Nonnull final Supplier<ArgumentType<?>> supplier) {
        this.defaultArgumentTypeSuppliers.put(clazz, supplier);
    }

    @Nullable
    @SuppressWarnings("all")
    private <T, K extends ArgumentParser<?, ?>> Pair<ArgumentType<?>, Boolean> getArgument(
            @Nonnull final Class<?> valueType,
            @Nonnull final TypeToken<T> argumentType,
            @Nonnull final K argument) {
        final ArgumentParser<C, ?> commandArgument = (ArgumentParser<C, ?>) argument;
        Function function = this.mappers.get(argumentType.getRawType());
        if (function == null) {
            return this.createDefaultMapper(valueType, commandArgument);
        }
        return new Pair<>((ArgumentType<?>) function.apply(commandArgument), !(argument instanceof StringArgument.StringParser));
    }

    @Nonnull
    private <T, K extends ArgumentParser<C, T>> Pair<ArgumentType<?>, Boolean> createDefaultMapper(
            @Nonnull final Class<?> clazz,
            @Nonnull final ArgumentParser<C, T> argument) {
        final Supplier<ArgumentType<?>> argumentTypeSupplier = this.defaultArgumentTypeSuppliers.get(clazz);
        if (argumentTypeSupplier != null) {
            return new Pair<>(argumentTypeSupplier.get(), true);
        }
        return new Pair<>(StringArgumentType.string(), false);
    }

    /**
     * Create a new literal command node
     *
     * @param label             Command label
     * @param cloudCommand      Cloud command instance
     * @param permissionChecker Permission checker
     * @param executor          Command executor
     * @return Literal command node
     */
    public LiteralCommandNode<S> createLiteralCommandNode(@Nonnull final String label,
                                                          @Nonnull final Command<C> cloudCommand,
                                                          @Nonnull final BiPredicate<S, CommandPermission> permissionChecker,
                                                          @Nonnull final com.mojang.brigadier.Command<S> executor) {
        final CommandTree.Node<CommandArgument<C, ?>> node = this.commandManager
                .getCommandTree().getNamedNode(cloudCommand.getArguments().get(0).getName());
        final SuggestionProvider<S> provider = (context, builder) -> this.buildSuggestions(node.getValue(), context, builder);
        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder
                .<S>literal(label)
                .requires(sender -> permissionChecker.test(sender, (CommandPermission) node.getNodeMeta()
                                                                           .getOrDefault("permission", Permission.empty())));
        literalArgumentBuilder.executes(executor);
        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final CommandTree.Node<CommandArgument<C, ?>> child : node.getChildren()) {
            constructedRoot.addChild(this.constructCommandNode(true, child,
                                                               permissionChecker, executor, provider).build());
        }
        return constructedRoot;
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
    public LiteralCommandNode<S> createLiteralCommandNode(@Nonnull final CommandTree.Node<CommandArgument<C, ?>> cloudCommand,
                                                          @Nonnull final LiteralCommandNode<S> root,
                                                          @Nonnull final SuggestionProvider<S> suggestionProvider,
                                                          @Nonnull final com.mojang.brigadier.Command<S> executor,
                                                          @Nonnull final BiPredicate<S, CommandPermission> permissionChecker) {
        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(sender -> permissionChecker.test(sender, (CommandPermission) cloudCommand.getNodeMeta()
                                                                            .getOrDefault("permission", Permission.empty())));
        if (cloudCommand.getValue() != null && cloudCommand.getValue().getOwningCommand() != null) {
            literalArgumentBuilder.executes(executor);
        }
        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final CommandTree.Node<CommandArgument<C, ?>> child : cloudCommand.getChildren()) {
            constructedRoot.addChild(this.constructCommandNode(false, child, permissionChecker,
                                                               executor, suggestionProvider).build());
        }
        return constructedRoot;
    }

    private ArgumentBuilder<S, ?> constructCommandNode(final boolean forceExecutor,
                                                       @Nonnull final CommandTree.Node<CommandArgument<C, ?>> root,
                                                       @Nonnull final BiPredicate<S, CommandPermission> permissionChecker,
                                                       @Nonnull final com.mojang.brigadier.Command<S> executor,
                                                       @Nonnull final SuggestionProvider<S> suggestionProvider) {

        ArgumentBuilder<S, ?> argumentBuilder;
        if (root.getValue() instanceof StaticArgument) {
            argumentBuilder = LiteralArgumentBuilder.<S>literal(root.getValue().getName())
                    .requires(sender -> permissionChecker.test(sender, (CommandPermission) root.getNodeMeta()
                                                                             .getOrDefault("permission", Permission.empty())))
                    .executes(executor);
        } else {
            final Pair<ArgumentType<?>, Boolean> pair = this.getArgument(root.getValue().getValueType(),
                                                                         TypeToken.of(root.getValue().getParser().getClass()),
                                                                         root.getValue().getParser());
            final SuggestionProvider<S> provider = pair.getRight() ? null : suggestionProvider;
            argumentBuilder = RequiredArgumentBuilder
                    .<S, Object>argument(root.getValue().getName(), (ArgumentType<Object>) pair.getLeft())
                    .suggests(provider)
                    .requires(sender -> permissionChecker.test(sender, (CommandPermission) root.getNodeMeta()
                                                                            .getOrDefault("permission", Permission.empty())));
        }
        if (forceExecutor || root.isLeaf() || !root.getValue().isRequired()) {
            argumentBuilder.executes(executor);
        }
        for (final CommandTree.Node<CommandArgument<C, ?>> node : root.getChildren()) {
            argumentBuilder.then(constructCommandNode(forceExecutor, node, permissionChecker, executor, suggestionProvider));
        }
        return argumentBuilder;
    }

    @Nonnull
    private CompletableFuture<Suggestions> buildSuggestions(@Nonnull final CommandArgument<C, ?> argument,
                                                            @Nonnull final com.mojang.brigadier.context.CommandContext<S> s,
                                                            @Nonnull final SuggestionsBuilder builder) {
        final CommandContext<C> commandContext = this.dummyContextProvider.get();
        final LinkedList<String> inputQueue = new LinkedList<>(Collections.singletonList(builder.getInput()));
        final CommandPreprocessingContext<C> commandPreprocessingContext =
                new CommandPreprocessingContext<>(commandContext, inputQueue);
        /*
          List<String> results = server.tabComplete(context.getSource().getBukkitSender(), builder.getInput(),
           context.getSource().getWorld(), context.getSource().getPosition(), true);
         */

        String command = builder.getInput();
        if (command.startsWith("/") /* Minecraft specific */) {
            command = command.substring(1);
        }
        final List<String> suggestions = this.commandManager.suggest(commandContext.getSender(), command);

        /*
        System.out.println("Filtering out with: " + builder.getInput());
        final CommandSuggestionProcessor<C> processor = this.commandManager.getCommandSuggestionProcessor();
        final List<String> filteredSuggestions = processor.apply(commandPreprocessingContext, suggestions);
        System.out.println("Current suggestions: ");
        for (final String suggestion : filteredSuggestions) {
            System.out.printf("- %s\n", suggestion);
        }*/
        for (final String suggestion : suggestions) {
            String tooltip = argument.getName();
            if (!(argument instanceof StaticArgument)) {
                if (argument.isRequired()) {
                    tooltip = '<' + tooltip + '>';
                } else {
                    tooltip = '[' + tooltip + ']';
                }
            }
            builder.suggest(suggestion, new LiteralMessage(tooltip));
        }
        return builder.buildFuture();
    }


    private static final class Pair<L, R> {

        private final L left;
        private final R right;

        private Pair(@Nonnull final L left, @Nonnull final R right) {
            this.left = left;
            this.right = right;
        }

        @Nonnull
        private L getLeft() {
            return this.left;
        }

        @Nonnull
        private R getRight() {
            return this.right;
        }

    }

}
