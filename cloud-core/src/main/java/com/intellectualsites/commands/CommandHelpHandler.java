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

import com.intellectualsites.commands.arguments.CommandArgument;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class CommandHelpHandler<C> {

    private final CommandManager<C> commandManager;

    CommandHelpHandler(@Nonnull final CommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Get exact syntax hints for all commands
     *
     * @return Syntax hints for all registered commands, order in lexicographical order
     */
    @Nonnull
    public List<VerboseHelpEntry<C>> getAllCommands() {
        final List<VerboseHelpEntry<C>> syntaxHints = new ArrayList<>();
        for (final Command<C> command : this.commandManager.getCommands()) {
            final List<CommandArgument<C, ?>> arguments = command.getArguments();
            syntaxHints.add(new VerboseHelpEntry<>(command,
                                                   this.commandManager.getCommandSyntaxFormatter().apply(arguments, null)));
        }
        syntaxHints.sort(Comparator.comparing(VerboseHelpEntry::getSyntaxString));
        return syntaxHints;
    }

    /**
     * Get a list of the longest shared command chains of all commands.
     * If there are two commands "foo bar 1" and "foo bar 2", this would
     * then return "foo bar 1|2"
     *
     * @return Longest shared command chains
     */
    @Nonnull
    public List<String> getLongestSharedChains() {
        final List<String> chains = new ArrayList<>();
        this.commandManager.getCommandTree().getRootNodes().forEach(node ->
           chains.add(node.getValue().getName() + this.commandManager.getCommandSyntaxFormatter()
                                                                     .apply(Collections.emptyList(), node)));
        chains.sort(String::compareTo);
        return chains;
    }


    public static final class VerboseHelpEntry<C> {

        private final Command<C> command;
        private final String syntaxString;

        private VerboseHelpEntry(@Nonnull final Command<C> command, @Nonnull final String syntaxString) {
            this.command = command;
            this.syntaxString = syntaxString;
        }

        /**
         * Get the command
         *
         * @return Command
         */
        @Nonnull
        public Command<C> getCommand() {
            return this.command;
        }

        /**
         * Get the syntax string
         *
         * @return Syntax string
         */
        @Nonnull
        public String getSyntaxString() {
            return this.syntaxString;
        }
    }

}
