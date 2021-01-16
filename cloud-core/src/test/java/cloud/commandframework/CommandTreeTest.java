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
import cloud.commandframework.arguments.compound.ArgumentPair;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.preprocessor.RegexPreprocessor;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.AmbiguousNodeException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionException;

class CommandTreeTest {

    private static final int EXPECTED_INPUT_NUMBER = 15;
    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void newTree() {
        manager = new TestCommandManager();

        /* Build general test commands */
        manager.command(manager.commandBuilder("test", SimpleCommandMeta.empty())
                .literal("one").build())
                .command(manager.commandBuilder("test", SimpleCommandMeta.empty())
                        .literal("two").permission("no").build())
                .command(manager.commandBuilder("test", Collections.singleton("other"),
                        SimpleCommandMeta.empty()
                )
                        .literal("opt", "öpt")
                        .argument(IntegerArgument
                                .optional("num", EXPECTED_INPUT_NUMBER))
                        .build())
                .command(manager.commandBuilder("req").senderType(SpecificCommandSender.class).build());

        /* Build command to test command proxying */
        final Command<TestCommandSender> toProxy = manager.commandBuilder("test")
                .literal("unproxied")
                .argument(StringArgument.of("string"))
                .argument(IntegerArgument.of("int"))
                .literal("anotherliteral")
                .handler(c -> {
                })
                .build();
        manager.command(toProxy);
        manager.command(manager.commandBuilder("proxy").proxies(toProxy).build());

        /* Build command for testing intermediary and final executors */
        manager.command(manager.commandBuilder("command")
                .permission("command.inner")
                .literal("inner")
                .handler(c -> System.out.println("Using inner command")));
        manager.command(manager.commandBuilder("command")
                .permission("command.outer")
                .handler(c -> System.out.println("Using outer command")));

        /* Build command for testing compound types */
        manager.command(manager.commandBuilder("pos")
                .argument(ArgumentPair.of(manager, "pos", Pair.of("x", "y"),
                        Pair.of(Integer.class, Integer.class)
                )
                        .simple())
                .handler(c -> {
                    final Pair<Integer, Integer> pair = c.get("pos");
                    System.out.printf("X: %d | Y: %d\n", pair.getFirst(), pair.getSecond());
                }));
        manager.command(manager.commandBuilder("vec")
                .argument(ArgumentPair.of(manager, "vec", Pair.of("x", "y"),
                        Pair.of(Double.class, Double.class)
                        )
                                .withMapper(
                                        Vector2.class,
                                        (sender, pair) -> new Vector2(pair.getFirst(), pair.getSecond())
                                )
                )
                .handler(c -> {
                    final Vector2 vector2 = c.get("vec");
                    System.out.printf("X: %f | Y: %f\n", vector2.getX(), vector2.getY());
                }));

        /* Build command for testing flags */
        final CommandFlag<Void> test = manager.flagBuilder("test")
                .withAliases("t")
                .build();

        final CommandFlag<Integer> num = manager.flagBuilder("num")
                .withArgument(IntegerArgument.of("num"))
                .build();

        manager.command(manager.commandBuilder("flags")
                .flag(manager.flagBuilder("test")
                        .withAliases("t")
                        .build())
                .flag(manager.flagBuilder("test2")
                        .withAliases("f")
                        .build())
                .flag(num)
                .flag(manager.flagBuilder("enum")
                        .withArgument(EnumArgument.of(FlagEnum.class, "enum")))
                .handler(c -> {
                    System.out.println("Flag present? " + c.flags().isPresent(test));
                    System.out.println("Second flag present? " + c.flags().isPresent("test2"));
                    System.out.println("Numerical flag: " + c.flags().getValue(num, -10));
                    System.out.println("Enum: " + c.flags().getValue("enum", FlagEnum.PROXI));
                })
                .build());

        /* Build command for testing float */
        manager.command(manager.commandBuilder("float")
                .argument(FloatArgument.of("num"))
                .handler(c -> {
                    System.out.printf("%f\n", c.<Float>get("num"));
                }));

        /* Build command for testing preprocessing */
        manager.command(manager.commandBuilder("preprocess")
                .argument(
                        StringArgument.<TestCommandSender>of("argument")
                                .addPreprocessor(RegexPreprocessor.of("[A-Za-z]{3,5}"))
                )
        );

        /* Build command for testing multiple optionals */
        manager.command(
                manager.commandBuilder("optionals")
                        .argument(StringArgument.optional("opt1"))
                        .argument(StringArgument.optional("opt2"))
        );
    }

    @Test
    void parse() {
        final Pair<Command<TestCommandSender>, Exception> command = manager.getCommandTree()
                .parse(
                        new CommandContext<>(
                                new TestCommandSender(),
                                manager
                        ),
                        new LinkedList<>(
                                Arrays.asList(
                                        "test",
                                        "one"
                                ))
                );
        Assertions.assertNotNull(command.getFirst());
        Assertions.assertEquals(NoPermissionException.class, manager.getCommandTree()
                .parse(
                        new CommandContext<>(
                                new TestCommandSender(),
                                manager
                        ),
                        new LinkedList<>(
                                Arrays.asList("test", "two"))
                )
                .getSecond().getClass());
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(new TestCommandSender(), manager),
                        new LinkedList<>(Arrays.asList("test", "opt"))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager
        ));
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(new TestCommandSender(), manager),
                        new LinkedList<>(Arrays.asList("test", "opt", "12"))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager
        ));
    }

    @Test
    void testAlias() {
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(
                                new TestCommandSender(),
                                manager
                        ),
                        new LinkedList<>(Arrays.asList(
                                "other",
                                "öpt",
                                "12"
                        ))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager
        ));
    }

    @Test
    void getArgumentsAndComponents() {
        // Create and register a command
        Command<TestCommandSender> command = manager.commandBuilder("component")
                .literal("literal", "literalalias")
                .literal("detail", ArgumentDescription.of("detaildescription"))
                .argument(CommandArgument.ofType(int.class, "argument"),
                          ArgumentDescription.of("argumentdescription"))
                .build();
        manager.command(command);

        // Verify all the details we have configured are present
        List<CommandArgument<TestCommandSender, ?>> arguments = command.getArguments();
        List<CommandComponent<TestCommandSender>> components = command.getComponents();
        Assertions.assertEquals(arguments.size(), components.size());
        Assertions.assertEquals(4, components.size());

        // Arguments should exactly match the component getArgument() result, in same order
        for (int i = 0; i < components.size(); i++) {
            Assertions.assertEquals(components.get(i).getArgument(), arguments.get(i));
        }

        // Argument configuration, we know component has the same argument so no need to test those
        // TODO: Aliases
        Assertions.assertEquals("component", arguments.get(0).getName());
        Assertions.assertEquals("literal", arguments.get(1).getName());
        Assertions.assertEquals("detail", arguments.get(2).getName());
        Assertions.assertEquals("argument", arguments.get(3).getName());

        // Check argument is indeed a command argument
        Assertions.assertEquals(TypeToken.get(int.class), arguments.get(3).getValueType());

        // Check description is set for all components, is empty when not specified
        Assertions.assertEquals("", components.get(0).getArgumentDescription().getDescription());
        Assertions.assertEquals("", components.get(1).getArgumentDescription().getDescription());
        Assertions.assertEquals("detaildescription", components.get(2).getArgumentDescription().getDescription());
        Assertions.assertEquals("argumentdescription", components.get(3).getArgumentDescription().getDescription());
    }

    @Test
    void getSuggestions() {
        Assertions.assertFalse(
                manager.getCommandTree().getSuggestions(
                        new CommandContext<>(new TestCommandSender(), manager),
                        new LinkedList<>(Arrays.asList("test", ""))
                ).isEmpty());
    }

    @Test
    void testRequiredSender() {
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "req").join());
    }

    @Test
    void testDefaultParser() {
        manager.command(
                manager.commandBuilder("default")
                        .argument(manager.argumentBuilder(Integer.class, "int"))
                        .handler(context -> {
                            final int number = context.get("int");
                            System.out.printf("Supplied number is: %d\n", number);
                        })
        );
        manager.executeCommand(new TestCommandSender(), "default 5").join();
    }

    @Test
    void invalidCommand() {
        Assertions.assertThrows(CompletionException.class, () -> manager
                .executeCommand(new TestCommandSender(), "invalid test").join());
    }

    @Test
    void testProxy() {
        manager.executeCommand(new TestCommandSender(), "test unproxied foo 10 anotherliteral").join();
        manager.executeCommand(new TestCommandSender(), "proxy foo 10").join();
    }

    @Test
    void testIntermediary() {
        manager.executeCommand(new TestCommandSender(), "command inner").join();
        manager.executeCommand(new TestCommandSender(), "command").join();
    }

    @Test
    void testCompound() {
        manager.executeCommand(new TestCommandSender(), "pos -3 2").join();
        manager.executeCommand(new TestCommandSender(), "vec 1 1").join();
    }

    @Test
    void testFlags() {
        manager.executeCommand(new TestCommandSender(), "flags").join();
        manager.executeCommand(new TestCommandSender(), "flags --test").join();
        manager.executeCommand(new TestCommandSender(), "flags -t").join();
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "flags --test --nonexistant").join());
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "flags --test --duplicate").join());
        manager.executeCommand(new TestCommandSender(), "flags --test --test2").join();
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "flags --test test2").join());
        manager.executeCommand(new TestCommandSender(), "flags --num 500").join();
        manager.executeCommand(new TestCommandSender(), "flags --num 63 --enum potato --test").join();
        manager.executeCommand(new TestCommandSender(), "flags -tf --num 63 --enum potato").join();
    }

    @Test
    void testAmbiguousNodes() {
        // Call newTree(); after each time we leave the Tree in an invalid state
        manager.command(manager.commandBuilder("ambiguous")
                .argument(StringArgument.of("string"))
        );
        Assertions.assertThrows(AmbiguousNodeException.class, () ->
                manager.command(manager.commandBuilder("ambiguous")
                        .argument(IntegerArgument.of("integer"))));
        newTree();

        // Literal and argument can co-exist, not ambiguous
        manager.command(manager.commandBuilder("ambiguous")
                .argument(StringArgument.of("string"))
        );
        manager.command(manager.commandBuilder("ambiguous")
                .literal("literal"));
        newTree();

        // Two literals (different names) and argument can co-exist, not ambiguous
        manager.command(manager.commandBuilder("ambiguous")
                .literal("literal"));
        manager.command(manager.commandBuilder("ambiguous")
                .literal("literal2"));

        manager.command(manager.commandBuilder("ambiguous")
                .argument(IntegerArgument.of("integer")));
        newTree();

        // Two literals with the same name can not co-exist, causes 'duplicate command chains' error
        manager.command(manager.commandBuilder("ambiguous")
                .literal("literal"));
        Assertions.assertThrows(IllegalStateException.class, () ->
                manager.command(manager.commandBuilder("ambiguous")
                        .literal("literal")));
        newTree();
    }

    @Test
    void testLiteralRepeatingArgument() {
        // Build a command with a literal repeating
        Command<TestCommandSender> command = manager.commandBuilder("repeatingargscommand")
                .literal("repeat")
                .literal("middle")
                .literal("repeat")
                .build();

        // Verify built command has the repeat argument twice
        List<CommandArgument<TestCommandSender, ?>> args = command.getArguments();
        Assertions.assertEquals(4, args.size());
        Assertions.assertEquals("repeatingargscommand", args.get(0).getName());
        Assertions.assertEquals("repeat", args.get(1).getName());
        Assertions.assertEquals("middle", args.get(2).getName());
        Assertions.assertEquals("repeat", args.get(3).getName());

        // Register
        manager.command(command);

        // If internally it drops repeating arguments, then it would register:
        // > /repeatingargscommand repeat middle
        // So check that we can register that exact command without an ambiguity exception
        manager.command(
                manager.commandBuilder("repeatingargscommand")
                       .literal("repeat")
                       .literal("middle")
        );
    }

    @Test
    void testAmbiguousLiteralOverridingArgument() {
        /* Build two commands for testing literals overriding variable arguments */
        manager.command(
                manager.commandBuilder("literalwithvariable")
                       .argument(StringArgument.of("variable"))
        );

        manager.command(
                manager.commandBuilder("literalwithvariable")
                       .literal("literal", "literalalias")
        );

        /* Try parsing as a variable, which should match the variable command */
        final Pair<Command<TestCommandSender>, Exception> variableResult = manager.getCommandTree().parse(
                        new CommandContext<>(new TestCommandSender(), manager),
                        new LinkedList<>(Arrays.asList("literalwithvariable", "argthatdoesnotmatch"))
                );
        Assertions.assertNull(variableResult.getSecond());
        Assertions.assertEquals("literalwithvariable",
                variableResult.getFirst().getArguments().get(0).getName());
        Assertions.assertEquals("variable",
                variableResult.getFirst().getArguments().get(1).getName());

        /* Try parsing with the main name literal, which should match the literal command */
        final Pair<Command<TestCommandSender>, Exception> literalResult = manager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), manager),
                new LinkedList<>(Arrays.asList("literalwithvariable", "literal"))
        );
        Assertions.assertNull(literalResult.getSecond());
        Assertions.assertEquals("literalwithvariable",
                literalResult.getFirst().getArguments().get(0).getName());
        Assertions.assertEquals("literal",
                literalResult.getFirst().getArguments().get(1).getName());

        /* Try parsing with the alias of the literal, which should match the literal command */
        final Pair<Command<TestCommandSender>, Exception> literalAliasResult = manager.getCommandTree().parse(
                new CommandContext<>(new TestCommandSender(), manager),
                new LinkedList<>(Arrays.asList("literalwithvariable", "literalalias"))
        );
        Assertions.assertNull(literalAliasResult.getSecond());
        Assertions.assertEquals("literalwithvariable",
                literalAliasResult.getFirst().getArguments().get(0).getName());
        Assertions.assertEquals("literal",
                literalAliasResult.getFirst().getArguments().get(1).getName());
    }

    @Test
    void testDuplicateArgument() {
        final CommandArgument<TestCommandSender, String> argument = StringArgument.of("test");
        manager.command(manager.commandBuilder("one").argument(argument));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                manager.command(manager.commandBuilder("two").argument(argument)));
    }

    @Test
    void testFloats() {
        manager.executeCommand(new TestCommandSender(), "float 0.0").join();
        manager.executeCommand(new TestCommandSender(), "float 100").join();
    }

    @Test
    void testPreprocessors() {
        manager.executeCommand(new TestCommandSender(), "preprocess abc").join();
        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "preprocess ab").join()
        );
    }

    @Test
    void testOptionals() {
        manager.executeCommand(new TestCommandSender(), "optionals").join();
    }


    public static final class SpecificCommandSender extends TestCommandSender {

    }


    public static final class Vector2 {

        private final double x;
        private final double y;

        private Vector2(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        private double getX() {
            return this.x;
        }

        private double getY() {
            return this.y;
        }

    }


    public enum FlagEnum {
        POTATO,
        CARROT,
        ONION,
        PROXI
    }

}
