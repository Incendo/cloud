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

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.types.tuples.Pair;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static com.google.common.truth.Truth.assertThat;

@SuppressWarnings("unchecked")
class LiteralBrigadierNodeFactoryTest {

    private TestCommandManager commandManager;
    private LiteralBrigadierNodeFactory<Object, Object> literalBrigadierNodeFactory;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        final CloudBrigadierManager<Object, Object> cloudBrigadierManager = new CloudBrigadierManager<>(
                this.commandManager,
                () -> this.commandManager.commandContextFactory().create(false, new Object(), this.commandManager),
                this.commandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion)
        );
        this.literalBrigadierNodeFactory = cloudBrigadierManager.literalBrigadierNodeFactory();
    }

    @Test
    void testSimple() {
        // Arrange
        final Command<Object> command = this.commandManager.commandBuilder("command")
                .literal("literal")
                .required("integer", integerParser(0, 10))
                .optional("string", greedyStringParser())
                .build();
        this.commandManager.command(command);
        final com.mojang.brigadier.Command<Object> brigadierCommand = ctx -> 0;

        // Act
        final LiteralCommandNode<Object> commandNode = this.literalBrigadierNodeFactory.createNode(
                "command",
                command,
                (source, permission) -> true,
                false /* forceRegister */,
                brigadierCommand
        );

        // Assert
        assertThat(commandNode).isNotNull();
        assertThat(commandNode.getLiteral()).isEqualTo("command");
        assertThat(commandNode.isValidInput("command")).isTrue();
        assertThat(commandNode.getChildren()).hasSize(1);
        assertThat(commandNode.getCommand()).isEqualTo(brigadierCommand);

        assertThat(commandNode.getChild("literal")).isNotNull();
        assertThat(commandNode.getChild("literal")).isInstanceOf(LiteralCommandNode.class);
        assertThat(commandNode.getChild("literal").getChildren()).hasSize(1);
        assertThat(commandNode.getCommand()).isEqualTo(brigadierCommand);

        assertThat(commandNode.getChild("literal").getChild("integer")).isNotNull();
        assertThat(commandNode.getChild("literal").getChild("integer")).isInstanceOf(ArgumentCommandNode.class);
        final ArgumentCommandNode<Object, Integer> integerArgument = (ArgumentCommandNode<Object, Integer>)
                commandNode.getChild("literal").getChild("integer");
        assertThat(integerArgument.getName()).isEqualTo("integer");
        assertThat(integerArgument.getType()).isInstanceOf(IntegerArgumentType.class);
        assertThat(integerArgument.getType()).isEqualTo(IntegerArgumentType.integer(0, 10));
        assertThat(integerArgument.getChildren()).hasSize(1);
        assertThat(integerArgument.getCommand()).isEqualTo(brigadierCommand);

        assertThat(integerArgument.getChild("string")).isNotNull();
        assertThat(integerArgument.getChild("string")).isInstanceOf(ArgumentCommandNode.class);
        final ArgumentCommandNode<Object, String> stringArgument = (ArgumentCommandNode<Object, String>)
                integerArgument.getChild("string");
        assertThat(stringArgument.getName()).isEqualTo("string");
        assertThat(stringArgument.getType()).isInstanceOf(StringArgumentType.class);
        assertThat(((StringArgumentType) stringArgument.getType()).getType())
                .isEqualTo(StringArgumentType.StringType.GREEDY_PHRASE);
        assertThat(stringArgument.getChildren()).isEmpty();
        assertThat(stringArgument.getCommand()).isEqualTo(brigadierCommand);
    }

    @Test
    void testAggregate() {
        // Arrange
        final Command<Object> command = this.commandManager.commandBuilder("command")
                .literal("literal")
                .required(
                        "aggregate",
                        AggregateCommandParser.builder()
                                .withComponent("integer", integerParser(0, 10))
                                .withComponent("string", greedyStringParser())
                                .withDirectMapper(
                                        new TypeToken<Pair<Integer, String>>() {},
                                        (cmdCtx, ctx) -> Pair.of(ctx.<Integer>get("integer"), ctx.<String>get("string"))
                                ).build()
                )
                .build();
        this.commandManager.command(command);
        final com.mojang.brigadier.Command<Object> brigadierCommand = ctx -> 0;

        // Act
        final LiteralCommandNode<Object> commandNode = this.literalBrigadierNodeFactory.createNode(
                "command",
                command,
                (source, permission) -> true,
                false /* forceRegister */,
                brigadierCommand
        );

        // Assert
        assertThat(commandNode).isNotNull();
        assertThat(commandNode.getLiteral()).isEqualTo("command");
        assertThat(commandNode.isValidInput("command")).isTrue();
        assertThat(commandNode.getChildren()).hasSize(1);
        assertThat(commandNode.getCommand()).isEqualTo(brigadierCommand);

        assertThat(commandNode.getChild("literal")).isNotNull();
        assertThat(commandNode.getChild("literal")).isInstanceOf(LiteralCommandNode.class);
        assertThat(commandNode.getChild("literal").getChildren()).hasSize(1);
        assertThat(commandNode.getCommand()).isEqualTo(brigadierCommand);

        assertThat(commandNode.getChild("literal").getChild("integer")).isNotNull();
        assertThat(commandNode.getChild("literal").getChild("integer")).isInstanceOf(ArgumentCommandNode.class);
        final ArgumentCommandNode<Object, Integer> integerArgument = (ArgumentCommandNode<Object, Integer>)
                commandNode.getChild("literal").getChild("integer");
        assertThat(integerArgument.getName()).isEqualTo("integer");
        assertThat(integerArgument.getType()).isInstanceOf(IntegerArgumentType.class);
        assertThat(integerArgument.getType()).isEqualTo(IntegerArgumentType.integer(0, 10));
        assertThat(integerArgument.getChildren()).hasSize(1);
        assertThat(integerArgument.getCommand()).isNull();

        assertThat(integerArgument.getChild("string")).isNotNull();
        assertThat(integerArgument.getChild("string")).isInstanceOf(ArgumentCommandNode.class);
        final ArgumentCommandNode<Object, String> stringArgument = (ArgumentCommandNode<Object, String>)
                integerArgument.getChild("string");
        assertThat(stringArgument.getName()).isEqualTo("string");
        assertThat(stringArgument.getType()).isInstanceOf(StringArgumentType.class);
        assertThat(((StringArgumentType) stringArgument.getType()).getType())
                .isEqualTo(StringArgumentType.StringType.GREEDY_PHRASE);
        assertThat(stringArgument.getChildren()).isEmpty();
        assertThat(stringArgument.getCommand()).isEqualTo(brigadierCommand);
    }


    private static final class TestCommandManager extends CommandManager<Object> {

        private TestCommandManager() {
            super(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        }

        @Override
        public boolean hasPermission(@NonNull final Object sender, @NonNull final String permission) {
            return true;
        }

        @Override
        public @NonNull CommandMeta createDefaultCommandMeta() {
            return SimpleCommandMeta.empty();
        }
    }
}
