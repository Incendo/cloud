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
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Command manager for use with JLine
 */
public class JLineCommandManager extends CommandManager<JLineCommandSender> implements Completer {

    public static void main(String[] args) throws Exception {
        // TODO: REMOVE THIS!!!!
        final JLineCommandManager jLineCommandManager = new JLineCommandManager(CommandExecutionCoordinator.simpleCoordinator());
        final Terminal terminal = TerminalBuilder.builder().dumb(true).build();
        LineReader lineReader = LineReaderBuilder.builder()
                .completer(jLineCommandManager).terminal(terminal).appName("Test").build();
        boolean[] shouldStop = new boolean[] { false };
        jLineCommandManager.registerCommand(Command.newBuilder("stop").withHandler(commandContext ->
                shouldStop[0] = true).build());
        while (!shouldStop[0]) {
            final String line = lineReader.readLine();
            if (line == null || line.isEmpty() || !line.startsWith("/")) {
                continue;
            }
            jLineCommandManager.executeCommand(new JLineCommandSender(), line.substring(1)).join();
            if (shouldStop[0]) {
                System.out.println("Stopping.");
            }
        }
    }

    public JLineCommandManager(@Nonnull final Function<CommandTree<JLineCommandSender>, CommandExecutionCoordinator<JLineCommandSender>> executionCoordinatorFunction) {
        super(executionCoordinatorFunction, CommandRegistrationHandler.NULL_COMMAND_REGISTRATION_HANDLER);
    }

    @Override
    public void complete(@Nonnull final LineReader lineReader, @Nonnull final ParsedLine parsedLine, @Nonnull final List<Candidate> list) {
        // TODO: Implement
    }

}
