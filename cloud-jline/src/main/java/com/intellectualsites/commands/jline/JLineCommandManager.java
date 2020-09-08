//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.jline;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.exceptions.InvalidSyntaxException;
import com.intellectualsites.commands.exceptions.NoSuchCommandException;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Command manager for use with JLine
 */
public class JLineCommandManager extends CommandManager<JLineCommandSender> implements Completer {

    public JLineCommandManager(@Nonnull
                               final Function<CommandTree<JLineCommandSender>, CommandExecutionCoordinator<JLineCommandSender>> executionCoordinatorFunction) {
        super(executionCoordinatorFunction, CommandRegistrationHandler.NULL_COMMAND_REGISTRATION_HANDLER);
    }

    public static void main(String[] args) throws Exception {
        // TODO: REMOVE THIS!!!!
        final JLineCommandManager jLineCommandManager = new JLineCommandManager(CommandExecutionCoordinator.simpleCoordinator());
        final Terminal terminal = TerminalBuilder.builder().build();
        LineReader lineReader = LineReaderBuilder.builder()
                                                 .completer(jLineCommandManager)
                                                 .option(LineReader.Option.INSERT_TAB, false)
                                                 .terminal(terminal)
                                                 .appName("Test")
                                                 .build();
        boolean[] shouldStop = new boolean[]{false};
        jLineCommandManager.registerCommand(Command.newBuilder("stop").withHandler(commandContext ->
                                                                                           shouldStop[0] = true).build())
                           .registerCommand(Command.newBuilder("echo")
                                                   .withComponent(
                                                           CommandComponent.ofType(String.class).named("string").asRequired()
                                                                           .withParser(((commandContext, inputQueue) -> {
                                                                               final StringBuilder stringBuilder = new StringBuilder();
                                                                               while (!inputQueue.isEmpty()) {
                                                                                   stringBuilder.append(inputQueue.remove());
                                                                                   if (!inputQueue.isEmpty()) {
                                                                                       stringBuilder.append(" ");
                                                                                   }
                                                                               }
                                                                               return ComponentParseResult.success(
                                                                                       stringBuilder.toString());
                                                                           })).build())
                                                   .withHandler(commandContext -> commandContext.get("string")
                                                                                                .ifPresent(System.out::println))
                                                   .build())
                           .registerCommand(Command.newBuilder("test")
                                                   .withComponent(StaticComponent.required("one"))
                                                   .withHandler(commandContext -> System.out.println("Test (1)"))
                                                   .build())
                           .registerCommand(Command.newBuilder("test")
                                                   .withComponent(StaticComponent.required("two"))
                                                   .withHandler(commandContext -> System.out.println("Test (2)"))
                                                   .build());
        System.out.println("Ready...");
        while (!shouldStop[0]) {
            final String line = lineReader.readLine();
            if (line == null || line.isEmpty() || !line.startsWith("/")) {
                System.out.println("Empty line");
                continue;
            }
            try {
                final List<String> suggestions = jLineCommandManager.suggest(new JLineCommandSender(), line.substring(1));
                for (final String suggestion : suggestions) {
                    System.out.printf("> %s\n", suggestion);
                }
                // jLineCommandManager.executeCommand(new JLineCommandSender(), line.substring(1)).join();
                // System.out.println("Successfully executed " + line);
            } catch (RuntimeException runtimeException) {
                if (runtimeException.getCause() instanceof NoSuchCommandException) {
                    System.out.println("No such command");
                } else if (runtimeException.getCause() instanceof InvalidSyntaxException) {
                    System.out.println(runtimeException.getCause().getMessage());
                } else {
                    System.out.printf("Something went wrong: %s\n", runtimeException.getCause().getMessage());
                    runtimeException.printStackTrace();
                }
            }
            if (shouldStop[0]) {
                System.out.println("Stopping.");
            }
        }
    }

    @Override
    public void complete(@Nonnull final LineReader lineReader,
                         @Nonnull final ParsedLine parsedLine,
                         @Nonnull final List<Candidate> list) {
        final String line = parsedLine.line();
        if (line == null || line.isEmpty() || !line.startsWith("/")) {
            System.out.println("Cannot suggest: empty line");
            return;
        }
        System.out.printf("Trying to complete '%s'\n", line);
    }

}
