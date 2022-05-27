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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.AmbiguousNodeException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CommandTree} integration tests.
 */
@SuppressWarnings("unchecked")
class CommandTreeTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testMultiLiteralParsing() {
        // Arrange
        final int defaultInputNumber = ThreadLocalRandom.current().nextInt();
        this.commandManager.command(
                this.commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                        .literal("one")
                        .build()
        ).command(
                this.commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                        .literal("two")
                        .permission("no")
                        .build()
        ).command(
                this.commandManager.commandBuilder("test", SimpleCommandMeta.empty())
                        .literal("opt")
                        .argument(IntegerArgument.optional("num", defaultInputNumber))
                        .build()
        );

        // Act
        final Pair<Command<TestCommandSender>, Exception> command1 = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("test", "one"))
        );
        final Pair<Command<TestCommandSender>, Exception> command2 = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("test", "two"))
        );
        final Pair<Command<TestCommandSender>, Exception> command3 = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("test", "opt"))
        );
        final Pair<Command<TestCommandSender>, Exception> command4 = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("test", "opt", "12"))
        );

        // Assert
        assertThat(command1.getFirst()).isNotNull();
        assertThat(command1.getSecond()).isNull();

        assertThat(command2.getFirst()).isNull();
        assertThat(command2.getSecond()).isInstanceOf(NoPermissionException.class);

        assertThat(command3.getFirst()).isNotNull();
        assertThat(command3.getFirst().toString()).isEqualTo("test opt num");
        assertThat(command3.getSecond()).isNull();

        assertThat(command4.getFirst()).isNotNull();
        assertThat(command4.getFirst().toString()).isEqualTo("test opt num");
        assertThat(command4.getSecond()).isNull();
    }

    @Test
    void testAliasedRouting() {
        // Arrange
        final int defaultInputNumber = ThreadLocalRandom.current().nextInt();
        final Command<TestCommandSender> command = this.commandManager.commandBuilder(
                        "test", Collections.singleton("other"), SimpleCommandMeta.empty()
                ).literal("opt", "öpt")
                .argument(IntegerArgument.optional("num", defaultInputNumber))
                .build();
        this.commandManager.command(command);

        // Act
        final Command<TestCommandSender> result = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("other", "öpt", "12"))
        ).getFirst();

        // Assert
        assertThat(result).isEqualTo(command);
    }

    @Test
    void getSuggestions() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .literal("a")
        );
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .literal("b")
        );

        // Act
        final List<String> results = this.commandManager.getCommandTree().getSuggestions(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("test", ""))
        );

        // Assert
        assertThat(results).containsExactly("a", "b");
    }

    @Test
    void testDefaultParser() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        this.commandManager.command(
                this.commandManager.commandBuilder("default")
                        .argument(this.commandManager.argumentBuilder(Integer.class, "int"))
                        .handler(executionHandler)
                        .build()
        );

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "default 5").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.get(SimpleCloudKey.of("int", TypeToken.get(Integer.class)))).isEqualTo(5);
    }

    @Test
    void invalidCommand() {
        assertThrows(CompletionException.class, () -> this.commandManager
                .executeCommand(new TestCommandSender(), "invalid test").join());
    }

    @Test
    void testProxy() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final Command<TestCommandSender> toProxy = this.commandManager.commandBuilder("test")
                .literal("unproxied")
                .argument(StringArgument.of("string"))
                .argument(IntegerArgument.of("int"))
                .literal("anotherliteral")
                .handler(executionHandler)
                .build();
        this.commandManager.command(toProxy);
        this.commandManager.command(this.commandManager.commandBuilder("proxy").proxies(toProxy).build());

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "test unproxied foo 10 anotherliteral").join();
        this.commandManager.executeCommand(new TestCommandSender(), "proxy foo 10").join();

        // Assert
        verify(executionHandler, times(2)).executeFuture(notNull());
    }

    private CommandExecutionHandler<TestCommandSender> setupFlags() {
        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandFlag<Integer> num = this.commandManager.flagBuilder("num")
                .withArgument(IntegerArgument.of("num"))
                .build();

        this.commandManager.command(this.commandManager.commandBuilder("flags")
                .flag(this.commandManager.flagBuilder("test")
                        .withAliases("t")
                        .build())
                .flag(this.commandManager.flagBuilder("test2")
                        .withAliases("f")
                        .build())
                .flag(num)
                .flag(this.commandManager.flagBuilder("enum")
                        .withArgument(EnumArgument.of(FlagEnum.class, "enum")))
                .handler(executionHandler)
                .build());

        return executionHandler;
    }

    @Test
    void testFlags_NoFlags() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().contains("test")).isFalse();
    }

    @Test
    void testFlags_PresenceFlag() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags --test").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().contains("test")).isTrue();
    }

    @Test
    void testFlags_PresenceFlagShortForm() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags -t").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().contains("test")).isTrue();
    }

    @Test
    void testFlags_NonexistentFlag() {
        // Arrange
        this.setupFlags();

        // Act & Assert
        assertThrows(
                CompletionException.class, () ->
                        this.commandManager.executeCommand(new TestCommandSender(), "flags --test --nonexistent").join()
        );
    }

    @Test
    void testFlags_MultiplePresenceFlags() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags --test --test2").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().contains("test")).isTrue();
        assertThat(context.flags().contains("test2")).isTrue();
    }

    @Test
    void testFlags_NonPrefixedPresenceFlag() {
        // Arrange
        this.setupFlags();

        // Act
        assertThrows(
                CompletionException.class, () ->
                this.commandManager.executeCommand(new TestCommandSender(), "flags --test test2").join()
        );
    }

    @Test
    void testFlags_ValueFlag() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags --num 500").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().<Integer>getValue("num")).hasValue(500);
    }

    @Test
    void testFlags_MultipleValueFlagsFollowedByPresence() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags --num 63 --enum potato --test").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().<Integer>getValue("num")).hasValue(63);
        assertThat(context.flags().<FlagEnum>getValue("enum")).hasValue(FlagEnum.POTATO);
    }

    @Test
    void testFlags_ShortFormPresenceFlagsFollowedByMultipleValueFlags() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = this.setupFlags();

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "flags -tf --num 63 --enum potato").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.flags().contains("test")).isTrue();
        assertThat(context.flags().contains("test2")).isTrue();
        assertThat(context.flags().<Integer>getValue("num")).hasValue(63);
        assertThat(context.flags().<FlagEnum>getValue("enum")).hasValue(FlagEnum.POTATO);
    }

    @Test
    void testAmbiguousNodes() {
        // Call setup(); after each time we leave the Tree in an invalid state
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .argument(StringArgument.of("string"))
        );
        assertThrows(AmbiguousNodeException.class, () ->
                this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                        .argument(IntegerArgument.of("integer"))));
        this.setup();

        // Literal and argument can co-exist, not ambiguous
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .argument(StringArgument.of("string"))
        );
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .literal("literal"));
        this.setup();

        // Two literals (different names) and argument can co-exist, not ambiguous
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .literal("literal"));
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .literal("literal2"));

        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .argument(IntegerArgument.of("integer")));
        this.setup();

        // Two literals with the same name can not co-exist, causes 'duplicate command chains' error
        this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                .literal("literal"));
        assertThrows(IllegalStateException.class, () ->
                this.commandManager.command(this.commandManager.commandBuilder("ambiguous")
                        .literal("literal")));
        this.setup();
    }

    @Test
    void testLiteralRepeatingArgument() {
        // Build a command with a literal repeating
        Command<TestCommandSender> command = this.commandManager.commandBuilder("repeatingargscommand")
                .literal("repeat")
                .literal("middle")
                .literal("repeat")
                .build();

        // Verify built command has the repeat argument twice
        List<CommandArgument<TestCommandSender, ?>> args = command.getArguments();
        assertThat(args.size()).isEqualTo(4);;
        assertThat(args.get(0).getName()).isEqualTo("repeatingargscommand");;
        assertThat(args.get(1).getName()).isEqualTo("repeat");;
        assertThat(args.get(2).getName()).isEqualTo("middle");;
        assertThat(args.get(3).getName()).isEqualTo("repeat");;

        // Register
        this.commandManager.command(command);

        // If internally it drops repeating arguments, then it would register:
        // > /repeatingargscommand repeat middle
        // So check that we can register that exact command without an ambiguity exception
        this.commandManager.command(
                this.commandManager.commandBuilder("repeatingargscommand")
                        .literal("repeat")
                        .literal("middle")
        );
    }

    @Test
    void testAmbiguousLiteralOverridingArgument() {
        /* Build two commands for testing literals overriding variable arguments */
        this.commandManager.command(
                this.commandManager.commandBuilder("literalwithvariable")
                        .argument(StringArgument.of("variable"))
        );

        this.commandManager.command(
                this.commandManager.commandBuilder("literalwithvariable")
                        .literal("literal", "literalalias")
        );

        /* Try parsing as a variable, which should match the variable command */
        final Pair<Command<TestCommandSender>, Exception> variableResult = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("literalwithvariable", "argthatdoesnotmatch"))
        );
        assertThat(variableResult.getSecond()).isNull();
        assertThat(variableResult.getFirst().getArguments().get(0).getName()).isEqualTo("literalwithvariable");;
        assertThat(variableResult.getFirst().getArguments().get(1).getName()).isEqualTo("variable");;

        /* Try parsing with the main name literal, which should match the literal command */
        final Pair<Command<TestCommandSender>, Exception> literalResult = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("literalwithvariable", "literal"))
        );
        assertThat(literalResult.getSecond()).isNull();
        assertThat(literalResult.getFirst().getArguments().get(0).getName()).isEqualTo("literalwithvariable");;
        assertThat(literalResult.getFirst().getArguments().get(1).getName()).isEqualTo("literal");;

        /* Try parsing with the alias of the literal, which should match the literal command */
        final Pair<Command<TestCommandSender>, Exception> literalAliasResult = this.commandManager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), this.commandManager),
                new LinkedList<>(Arrays.asList("literalwithvariable", "literalalias"))
        );
        assertThat(literalAliasResult.getSecond()).isNull();
        assertThat(literalAliasResult.getFirst().getArguments().get(0).getName()).isEqualTo("literalwithvariable");;
        assertThat(literalAliasResult.getFirst().getArguments().get(1).getName()).isEqualTo("literal");;
    }

    @Test
    void testDuplicateArgument() {
        // Arrange
        final CommandArgument<TestCommandSender, String> argument = StringArgument.of("test");
        this.commandManager.command(this.commandManager.commandBuilder("one").argument(argument));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> this.commandManager.command(this.commandManager.commandBuilder("two").argument(argument))
        );
    }

    @Test
    void testFloats() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        this.commandManager.command(
                this.commandManager.commandBuilder("float")
                        .argument(FloatArgument.of("num"))
                        .handler(executionHandler)
                        .build()
        );

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "float 0.0").join();
        this.commandManager.executeCommand(new TestCommandSender(), "float 100").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler, times(2)).executeFuture(contextArgumentCaptor.capture());

        final Stream<Float> values = contextArgumentCaptor.getAllValues()
                .stream()
                .map(context -> context.<Float>get("num"));
        assertThat(values).containsExactly(0.0f, 100f);
    }

    @Test
    void testOptionals() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        this.commandManager.command(
                this.commandManager.commandBuilder("optionals")
                        .argument(StringArgument.optional("opt1"))
                        .argument(StringArgument.optional("opt2"))
                        .handler(executionHandler)
                        .build()
        );

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "optionals").join();

        // Assert
        final ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(executionHandler).executeFuture(contextArgumentCaptor.capture());

        final CommandContext<TestCommandSender> context = contextArgumentCaptor.getValue();
        assertThat(context.getOrDefault(SimpleCloudKey.of("opt1", TypeToken.get(String.class)), null)).isNull();
        assertThat(context.getOrDefault(SimpleCloudKey.of("opt2", TypeToken.get(String.class)), null)).isNull();
    }

    enum FlagEnum {
        POTATO,
        CARROT,
        ONION,
        PROXI
    }
}
