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
package cloud.commandframework.brigadier.node;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.MappedArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.argument.ArgumentTypeFactory;
import cloud.commandframework.brigadier.argument.BrigadierMapping;
import cloud.commandframework.brigadier.permission.BrigadierPermissionChecker;
import cloud.commandframework.brigadier.permission.BrigadierPermissionPredicate;
import cloud.commandframework.brigadier.suggestion.BrigadierSuggestionFactory;
import cloud.commandframework.brigadier.suggestion.CloudDelegatingSuggestionProvider;
import cloud.commandframework.brigadier.suggestion.SuggestionsType;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings({"unchecked", "rawtypes"})
@API(status = API.Status.STABLE, since = "2.0.0")
public final class LiteralBrigadierNodeFactory<C, S> implements BrigadierNodeFactory<C, S, LiteralCommandNode<S>> {

    private final CloudBrigadierManager<C, S> cloudBrigadierManager;
    private final CommandManager<C> commandManager;
    private final BrigadierSuggestionFactory<C, S> brigadierSuggestionFactory;

    /**
     * Creates a new factory that produces literal command nodes.
     *
     * @param cloudBrigadierManager the brigadier manager
     * @param commandManager        the command manager
     * @param dummyContextProvider  creates the context provided when retrieving suggestions
     * @param suggestionFactory     the suggestion factory-producing tooltip suggestions
     */
    public LiteralBrigadierNodeFactory(
           final @NonNull CloudBrigadierManager<C, S> cloudBrigadierManager,
           final @NonNull CommandManager<C> commandManager,
           final @NonNull Supplier<CommandContext<C>> dummyContextProvider,
           final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
    ) {
        this.cloudBrigadierManager = cloudBrigadierManager;
        this.commandManager = commandManager;
        this.brigadierSuggestionFactory = new BrigadierSuggestionFactory<>(
                cloudBrigadierManager,
                commandManager,
                dummyContextProvider,
                suggestionFactory
        );
    }

    @Override
    public @NonNull LiteralCommandNode<S> createNode(
            final cloud.commandframework.internal.@NonNull CommandNode<C> cloudCommand,
            final @NonNull LiteralCommandNode<S> root,
            final @NonNull SuggestionProvider<S> suggestionProvider,
            final @NonNull Command<S> executor,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker
    ) {
        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder.<S>literal(root.getLiteral())
                .requires(new BrigadierPermissionPredicate<>(permissionChecker, cloudCommand));
        if (cloudCommand.component() != null && cloudCommand.component().owningCommand() != null) {
            literalArgumentBuilder.executes(executor);
        }
        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final cloud.commandframework.internal.CommandNode<C> child : cloudCommand.children()) {
            constructedRoot.addChild(this.constructCommandNode(
                    true,
                    child,
                    permissionChecker,
                    executor,
                    suggestionProvider
            ).build());
        }
        return constructedRoot;
    }

    @Override
    public @NonNull LiteralCommandNode<S> createNode(
            final @NonNull String label,
            final cloud.commandframework.@NonNull Command<C> cloudCommand,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker,
            final boolean forceRegister,
            final @NonNull Command<S> executor
    ) {
        final cloud.commandframework.internal.CommandNode<C> node = this.commandManager
                .commandTree().getNamedNode(cloudCommand.rootComponent().name());
        final SuggestionProvider<S> provider = (context, builder) -> this.brigadierSuggestionFactory.buildSuggestions(
                context,
                null, /* parent node, null for the literal command node root */
                node.component(),
                builder
        );

        final LiteralArgumentBuilder<S> literalArgumentBuilder = LiteralArgumentBuilder
                .<S>literal(label)
                .requires(new BrigadierPermissionPredicate<>(permissionChecker, node));
        if (forceRegister || (node.component() != null && node.component().owningCommand() != null)) {
            literalArgumentBuilder.executes(executor);
        }
        literalArgumentBuilder.executes(executor);
        final LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();
        for (final cloud.commandframework.internal.CommandNode<C> child : node.children()) {
            constructedRoot.addChild(this.constructCommandNode(forceRegister, child,
                    permissionChecker, executor, provider
            ).build());
        }
        return constructedRoot;
    }

    private @NonNull ArgumentBuilder<S, ?> constructCommandNode(
            final boolean forceExecutor,
            final cloud.commandframework.internal.@NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker,
            final com.mojang.brigadier.@NonNull Command<S> executor,
            final SuggestionProvider<S> suggestionProvider
    ) {
        if (root.component().parser() instanceof AggregateCommandParser) {
            final AggregateCommandParser<C, ?> aggregateParser = (AggregateCommandParser<C, ?>) root.component().parser();
            return this.constructAggregateNode(
                    aggregateParser,
                    forceExecutor,
                    root,
                    permissionChecker,
                    executor,
                    suggestionProvider
            );
        }

        final ArgumentBuilder<S, ?> argumentBuilder;
        if (root.component().type() == CommandComponent.ComponentType.LITERAL) {
            argumentBuilder = this.createLiteralArgumentBuilder(root.component(), root, permissionChecker, executor);
        } else {
            argumentBuilder = this.createVariableArgumentBuilder(root.component(), root, permissionChecker);
        }
        if (forceExecutor || root.isLeaf() || root.component().optional()) {
            argumentBuilder.executes(executor);
        }
        if (root.children().stream().noneMatch(node -> node.component().required())) {
            argumentBuilder.executes(executor);
        }
        for (final cloud.commandframework.internal.CommandNode<C> node : root.children()) {
            argumentBuilder.then(this.constructCommandNode(forceExecutor, node, permissionChecker, executor, suggestionProvider));
        }
        return argumentBuilder;
    }

    private @NonNull ArgumentBuilder<S, ?> createLiteralArgumentBuilder(
            final @NonNull CommandComponent<C> component,
            final cloud.commandframework.internal.@NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker,
            final com.mojang.brigadier.@NonNull Command<S> executor
    ) {
        return LiteralArgumentBuilder.<S>literal(component.name())
                .requires(new BrigadierPermissionPredicate<>(permissionChecker, root))
                .executes(executor);
    }

    private @NonNull ArgumentBuilder<S, ?> createVariableArgumentBuilder(
            final @NonNull CommandComponent<C> component,
            final cloud.commandframework.internal.@NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker
    ) {
        final ArgumentMapping<S> argumentMapping = this.getArgument(
                component.valueType(),
                component.parser()
        );

        final SuggestionProvider<S> provider;
        if (argumentMapping.suggestionsType() == SuggestionsType.CLOUD_SUGGESTIONS) {
            provider = new CloudDelegatingSuggestionProvider<>(this.brigadierSuggestionFactory, root);
        } else {
            provider = argumentMapping.suggestionProvider();
        }

        return RequiredArgumentBuilder
                .<S, Object>argument(component.name(), (ArgumentType<Object>) argumentMapping.argumentType())
                .suggests(provider)
                .requires(new BrigadierPermissionPredicate<>(permissionChecker, root));
    }

    private @NonNull ArgumentBuilder<S, ?> constructAggregateNode(
            final @NonNull AggregateCommandParser<C, ?> aggregateParser,
            final boolean forceExecutor,
            final cloud.commandframework.internal.@NonNull CommandNode<C> root,
            final @NonNull BrigadierPermissionChecker<S> permissionChecker,
            final com.mojang.brigadier.@NonNull Command<S> executor,
            final SuggestionProvider<S> suggestionProvider
    ) {
        final List<? extends CommandComponent<?>> components = aggregateParser.components();

        /* Build nodes backwards */
        final ArgumentBuilder<S, ?>[] argumentBuilders = new ArgumentBuilder[components.size()];

        for (int i = components.size() - 1; i >= 0; i--) {
            final CommandComponent<C> component = (CommandComponent<C>) components.get(i);

            final ArgumentParser<C, ?> parser = component.parser();
            final ArgumentMapping<S> argumentMapping = this.getArgument(component.valueType(), parser);
            final SuggestionProvider<S> provider = argumentMapping.suggestionsType() == SuggestionsType.CLOUD_SUGGESTIONS
                    ? suggestionProvider
                    : argumentMapping.suggestionProvider();

            final ArgumentBuilder<S, ?> fragmentBuilder = RequiredArgumentBuilder
                    .<S, Object>argument(component.name(), (ArgumentType<Object>) argumentMapping.argumentType())
                    .suggests(provider)
                    .requires(new BrigadierPermissionPredicate<>(permissionChecker, root));
            argumentBuilders[i] = fragmentBuilder;

            if (forceExecutor || ((i == components.size() - 1) && (root.isLeaf() || !root.component().required()))) {
                fragmentBuilder.executes(executor);
            }

            /* Link all previous builders to this one */
            if ((i + 1) < components.size()) {
                fragmentBuilder.then(argumentBuilders[i + 1]);
            }
        }

        for (final cloud.commandframework.internal.CommandNode<C> node : root.children()) {
            argumentBuilders[components.size() - 1]
                    .then(this.constructCommandNode(forceExecutor, node, permissionChecker, executor, suggestionProvider));
        }

        return argumentBuilders[0];
    }

    /**
     * Returns a mapping to a Brigadier argument for the given {@code argumentParser} that produces values of the given
     * {@code valueType}.
     *
     * @param <K>            the parser type
     * @param valueType      the types of values produced by the parser
     * @param argumentParser the parser
     * @return the argument mapping
     */
    private <K extends ArgumentParser<C, ?>> @NonNull ArgumentMapping<S> getArgument(
            final @NonNull TypeToken<?> valueType,
            final @NonNull K argumentParser
    ) {
       if (argumentParser instanceof MappedArgumentParser) {
           return this.getArgument(valueType, ((MappedArgumentParser<C, ?, ?>) argumentParser).getBaseParser());
        }

        final BrigadierMapping<C, K, S> mapping = this.cloudBrigadierManager.mappings().mapping(argumentParser.getClass());
        if (mapping == null || mapping.mapper() == null) {
            return this.getDefaultMapping(valueType);
        }

        final SuggestionProvider<S> suggestionProvider = mapping.makeSuggestionProvider(argumentParser);
        if (suggestionProvider == BrigadierMapping.delegateSuggestions()) {
            return new ArgumentMapping<>(
                    (ArgumentType) ((Function) mapping.mapper()).apply(argumentParser),
                    SuggestionsType.CLOUD_SUGGESTIONS
            );
        }
        return new ArgumentMapping<>(
                (ArgumentType) ((Function) mapping.mapper()).apply(argumentParser),
                suggestionProvider
        );
    }

    /**
     * Returns a mapping to a Brigadier argument type from the registered default argument type suppliers.
     * If no mapping can be found, a {@link StringArgumentType#word()} is returned.
     *
     * @param type the argument type
     * @return the argument mapping
     */
    private @NonNull ArgumentMapping<S> getDefaultMapping(final @NonNull TypeToken<?> type) {
        final ArgumentTypeFactory<?> argumentTypeSupplier = this.cloudBrigadierManager.defaultArgumentTypeFactories()
                .get(GenericTypeReflector.erase(type.getType()));
        if (argumentTypeSupplier != null) {
            final ArgumentType<?> argumentType = argumentTypeSupplier.create();
            if (argumentType != null) {
                return new ArgumentMapping<>(argumentType);
            }
        }
        return new ArgumentMapping<>(StringArgumentType.word(), SuggestionsType.CLOUD_SUGGESTIONS);
    }
}
