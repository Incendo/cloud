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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.CommandComponent;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A command consists out of a chain of {@link com.intellectualsites.commands.components.CommandComponent command components}.
 */
public class Command {

    private final CommandComponent<?>[] components;

    private Command(@Nonnull final CommandComponent<?>[] commandComponents) {
        this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
        if (this.components.length == 0){
            throw new IllegalArgumentException("At least one command component is required");
        }
        // Enforce ordering of command components
        boolean foundOptional = false;
        for (final CommandComponent<?> component : this.components) {
            if (foundOptional && component.isRequired()) {
                throw new IllegalArgumentException(String.format("Command component '%s' cannot be placed after an optional component", component.getName()));
            } else if (!component.isRequired()) {
                foundOptional = true;
            }
        }
    }

    /**
     * Return a copy of the command component array
     *
     * @return Copy of the command component array
     */
    @Nonnull public CommandComponent<?>[] getComponents() {
        final CommandComponent<?>[] commandComponents = new CommandComponent<?>[this.components.length];
        System.arraycopy(this.components, 0, commandComponents, 0, this.components.length);
        return commandComponents;
    }

    /**
     * Get the longest chain of similar components for
     * two commands
     *
     * @return List containing the longest shared component chain
     */
    public List<CommandComponent<?>> getSharedComponentChain(@Nonnull final Command other) {
        final List<CommandComponent<?>> commandComponents = new LinkedList<>();
        for (int i = 0; i < this.components.length && i < other.components.length; i++) {
            if (this.components[i].equals(other.components[i])) {
                commandComponents.add(this.components[i]);
            } else {
                break;
            }
        }
        return commandComponents;
    }

}
