//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.preprocessor.RegexPreprocessor;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.types.tuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
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
        manager.command(manager.commandBuilder("flags")
                .flag(manager.flagBuilder("test")
                        .withAliases("t")
                        .build())
                .flag(manager.flagBuilder("test2")
                        .build())
                .flag(manager.flagBuilder("num")
                        .withArgument(IntegerArgument.of("num")).build())
                .flag(manager.flagBuilder("enum")
                        .withArgument(EnumArgument.of(FlagEnum.class, "enum")))
                .handler(c -> {
                    System.out.println("Flag present? " + c.flags().isPresent("test"));
                    System.out.println("Numerical flag: " + c.flags().getValue("num", -10));
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
    }

    @Test
    void parse() {
        final Pair<Command<TestCommandSender>, Exception> command = manager.getCommandTree()
                .parse(
                        new CommandContext<>(
                                new TestCommandSender(),
                                manager.getCaptionRegistry()
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
                                manager.getCaptionRegistry()
                        ),
                        new LinkedList<>(
                                Arrays.asList("test", "two"))
                )
                .getSecond().getClass());
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(new TestCommandSender(), manager.getCaptionRegistry()),
                        new LinkedList<>(Arrays.asList("test", "opt"))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager.getCaptionRegistry()
        ));
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(new TestCommandSender(), manager.getCaptionRegistry()),
                        new LinkedList<>(Arrays.asList("test", "opt", "12"))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager.getCaptionRegistry()
        ));
    }

    @Test
    void testAlias() {
        manager.getCommandTree()
                .parse(
                        new CommandContext<>(
                                new TestCommandSender(),
                                manager.getCaptionRegistry()
                        ),
                        new LinkedList<>(Arrays.asList("other",
                                "öpt", "12"
                        ))
                )
                .getFirst().getCommandExecutionHandler().execute(new CommandContext<>(
                new TestCommandSender(),
                manager.getCaptionRegistry()
        ));
    }

    @Test
    void getSuggestions() {
        Assertions.assertFalse(
                manager.getCommandTree().getSuggestions(
                        new CommandContext<>(new TestCommandSender(), manager.getCaptionRegistry()),
                        new LinkedList<>(Collections.singletonList("test "))
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
