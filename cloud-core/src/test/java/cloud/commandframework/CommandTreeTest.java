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

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
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
                               .literal("two").withPermission("no").build())
               .command(manager.commandBuilder("test", Collections.singleton("other"),
                                               SimpleCommandMeta.empty())
                               .literal("opt", "öpt")
                               .argument(IntegerArgument
                                                 .optional("num", EXPECTED_INPUT_NUMBER))
                               .build())
               .command(manager.commandBuilder("req").withSenderType(SpecificCommandSender.class).build());

        /* Build command to test command proxying */
        final Command<TestCommandSender> toProxy = manager.commandBuilder("test")
                                                          .literal("unproxied")
                                                          .argument(StringArgument.required("string"))
                                                          .argument(IntegerArgument.required("int"))
                                                          .literal("anotherliteral")
                                                          .handler(c -> {})
                                                          .build();
        manager.command(toProxy);
        manager.command(manager.commandBuilder("proxy").proxies(toProxy).build());

        /* Build command for testing intermediary and final executors */
        manager.command(manager.commandBuilder("command")
                               .withPermission("command.inner")
                               .literal("inner")
                               .handler(c -> System.out.println("Using inner command"))
                               .build());
        manager.command(manager.commandBuilder("command")
                               .withPermission("command.outer")
                               .handler(c -> System.out.println("Using outer command"))
                               .build());
    }

    @Test
    void parse() {
        final Optional<Command<TestCommandSender>> command = manager.getCommandTree()
                                                                    .parse(new CommandContext<>(
                                                                                   new TestCommandSender()),
                                                                           new LinkedList<>(
                                                                                   Arrays.asList("test",
                                                                                                 "one")));
        Assertions.assertTrue(command.isPresent());
        Assertions.assertThrows(NoPermissionException.class, () -> manager.getCommandTree()
                                                                          .parse(new CommandContext<>(
                                                                                         new TestCommandSender()),
                                                                                 new LinkedList<>(
                                                                                         Arrays.asList("test", "two"))));
        manager.getCommandTree()
               .parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(Arrays.asList("test", "opt")))
               .ifPresent(c -> c.getCommandExecutionHandler().execute(new CommandContext<>(new TestCommandSender())));
        manager.getCommandTree()
               .parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(Arrays.asList("test", "opt", "12")))
               .ifPresent(c -> c.getCommandExecutionHandler().execute(new CommandContext<>(new TestCommandSender())));
    }

    @Test
    void testAlias() {
        manager.getCommandTree()
               .parse(new CommandContext<>(new TestCommandSender()), new LinkedList<>(Arrays.asList("other", "öpt", "12")))
               .ifPresent(c -> c.getCommandExecutionHandler().execute(new CommandContext<>(new TestCommandSender())));
    }

    @Test
    void getSuggestions() {
        Assertions.assertFalse(
                manager.getCommandTree().getSuggestions(new CommandContext<>(new TestCommandSender()), new LinkedList<>(
                        Collections.singletonList("test "))).isEmpty());
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
                       .argument(manager.argumentBuilder(Integer.class, "int").build())
                       .handler(context -> {
                           final int number = context.getRequired("int");
                           System.out.printf("Supplied number is: %d\n", number);
                       })
                       .build()
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
        manager.executeCommand(new TestCommandSender(),"test unproxied foo 10 anotherliteral").join();
        manager.executeCommand(new TestCommandSender(), "proxy foo 10").join();
    }

    @Test
    void testIntermediary() {
        manager.executeCommand(new TestCommandSender(), "command inner").join();
        manager.executeCommand(new TestCommandSender(), "command").join();
    }


    public static final class SpecificCommandSender extends TestCommandSender {
    }

}
