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
package com.intellectualsites.commands;

import com.intellectualsites.commands.arguments.StaticArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.NoPermissionException;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.sender.CommandSender;
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
    private static CommandManager<CommandSender, SimpleCommandMeta> manager;

    @BeforeAll
    static void newTree() {
        manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test", SimpleCommandMeta.empty())
                               .argument(StaticArgument.required("one")).build())
               .command(manager.commandBuilder("test", SimpleCommandMeta.empty())
                               .argument(StaticArgument.required("two")).withPermission("no").build())
               .command(manager.commandBuilder("test", Collections.singleton("other"),
                                               SimpleCommandMeta.empty())
                               .argument(StaticArgument.required("opt", "öpt"))
                               .argument(IntegerArgument
                                                                .optional("num", EXPECTED_INPUT_NUMBER))
                               .build())
               .command(manager.commandBuilder("req").withSenderType(SpecificCommandSender.class).build());
    }

    @Test
    void parse() {
        final Optional<Command<CommandSender, SimpleCommandMeta>> command = manager.getCommandTree()
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


    public static final class SpecificCommandSender extends TestCommandSender {
    }

}
