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
package com.intellectualsites.commands.components;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.parser.ComponentParser;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A component that belongs to a command
 *
 * @param <C> Command sender type
 * @param <T> The type that the component parses into
 */
public class CommandComponent<C extends CommandSender, T> implements Comparable<CommandComponent<?, ?>> {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9]+");

    /**
     * Indicates whether or not the component is required
     * or not. All components prior to any other required
     * component must also be required, such that the predicate
     * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
     * where {c_0, ..., c_n-1} is the set of command components.
     */
    private final boolean required;
    /**
     * The command component name. This might be exposed
     * to command senders and so should be chosen carefully.
     */
    private final String name;
    /**
     * The parser that is used to parse the command input
     * into the corresponding command type
     */
    private final ComponentParser<C, T> parser;

    private Command<C> owningCommand;

    CommandComponent(final boolean required, @Nonnull final String name,
                             @Nonnull final ComponentParser<C, T> parser) {
        this.required = required;
        this.name = Objects.requireNonNull(name, "Name may not be null");
        if (!NAME_PATTERN.asPredicate().test(name)) {
             throw new IllegalArgumentException("Name must be alphanumeric");
        }
        this.parser = Objects.requireNonNull(parser, "Parser may not be null");
    }

    /**
     * Create a new command component
     *
     * @param clazz Argument class
     * @param <C> Command sender type
     * @param <T> Argument Type
     * @return Component builder
     */
    @Nonnull public static <C extends CommandSender, T> CommandComponent.Builder<C, T> ofType(@Nonnull final Class<T> clazz) {
        return new Builder<>();
    }

    /**
     * Check whether or not the command component is required
     *
     * @return {@code true} if the component is required, {@code false} if not
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Get the command component name;
     *
     * @return Component name
     */
    @Nonnull public String getName() {
        return this.name;
    }

    /**
     * Get the parser that is used to parse the command input
     * into the corresponding command type
     *
     * @return Command parser
     */
    @Nonnull public ComponentParser<C, T> getParser() {
        return this.parser;
    }

    @Nonnull @Override public String toString() {
        return String.format("CommandComponent{name=%s}", this.name);
    }

    /**
     * Get the owning command
     *
     * @return Owning command
     */
    @Nullable public Command<C> getOwningCommand() {
        return this.owningCommand;
    }

    /**
     * Set the owning command
     *
     * @param owningCommand Owning command
     */
    public void setOwningCommand(@Nonnull final Command<C> owningCommand) {
        if (this.owningCommand != null) {
            throw new IllegalStateException("Cannot replace owning command");
        }
        this.owningCommand = owningCommand;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandComponent<?, ?> that = (CommandComponent<?, ?>) o;
        return isRequired() == that.isRequired() && com.google.common.base.Objects.equal(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(isRequired(), getName());
    }

    @Override
    public int compareTo(@Nonnull final CommandComponent<?, ?> o) {
        if (this instanceof StaticComponent) {
            if (o instanceof StaticComponent) {
                return (this.getName().compareTo(o.getName()));
            } else {
                return -1;
            }
        } else {
            if (o instanceof StaticComponent) {
                return 1;
            } else {
                return 0;
            }
        }
    }


    public static class Builder<C extends CommandSender, T> {

        private String name;
        private boolean required = true;
        private ComponentParser<C, T> parser;

        private Builder() {
        }

        /**
         * Set the component name. Must be alphanumeric
         *
         * @param name Alphanumeric component name
         * @return Builder instance
         */
        @Nonnull public Builder<C, T> named(@Nonnull final String name) {
            this.name = name;
            return this;
        }

        /**
         * Indicates that the component is required.
         * All components prior to any other required
         * component must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command components.
         *
         * @return Builder instance
         */
        @Nonnull public Builder<C, T> asRequired() {
            this.required = true;
            return this;
        }

        /**
         * Indicates that the component is optional.
         * All components prior to any other required
         * component must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command components.
         *
         * @return Builder instance
         */
        @Nonnull public Builder<C, T> asOptional() {
            this.required = false;
            return this;
        }

        /**
         * Set the component parser
         *
         * @param parser Component parser
         * @return Builder instance
         */
        @Nonnull public Builder<C, T> withParser(@Nonnull final ComponentParser<C, T> parser) {
            this.parser = Objects.requireNonNull(parser, "Parser may not be null");
            return this;
        }

        /**
         * Construct a command component from the builder settings
         *
         * @return Constructed component
         */
        @Nonnull public CommandComponent<C, T> build() {
            return new CommandComponent<>(this.required, this.name, this.parser);
        }

    }

}
