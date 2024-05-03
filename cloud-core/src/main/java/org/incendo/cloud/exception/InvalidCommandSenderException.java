//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package org.incendo.cloud.exception;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.util.TypeUtils;

/**
 * Exception thrown when an invalid command sender tries to execute a command
 */
@SuppressWarnings("serial")
@API(status = API.Status.STABLE)
public final class InvalidCommandSenderException extends CommandParseException {

    private final Set<Type> requiredSenderTypes;
    private final @Nullable Command<?> command;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender       Sender who executed the command
     * @param requiredSenderTypes The sender type that is required
     * @param currentChain        Chain leading up to the exception
     * @param command             Command
     */
    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    public InvalidCommandSenderException(
            final @NonNull Object commandSender,
            final @NonNull Type requiredSenderTypes,
            final @NonNull List<@NonNull CommandComponent<?>> currentChain,
            final @Nullable Command<?> command
    ) {
        this(commandSender, new HashSet<>(Collections.singletonList(requiredSenderTypes)), currentChain, command);
    }

    /**
     * Construct a new command parse exception
     *
     * @param commandSender       Sender who executed the command
     * @param requiredSenderTypes The sender type that is required
     * @param currentChain        Chain leading up to the exception
     * @param command             Command
     */
    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    public InvalidCommandSenderException(
            final @NonNull Object commandSender,
            final @NonNull Set<Type> requiredSenderTypes,
            final @NonNull List<@NonNull CommandComponent<?>> currentChain,
            final @Nullable Command<?> command
    ) {
        super(commandSender, currentChain);
        this.requiredSenderTypes = Collections.unmodifiableSet(requiredSenderTypes);
        this.command = command;
    }

    /**
     * Returns the required sender types, a sender must satisfy one.
     *
     * @return required sender types
     */
    public @NonNull Set<Type> requiredSenderTypes() {
        return this.requiredSenderTypes;
    }

    @Override
    public String getMessage() {
        if (this.requiredSenderTypes.size() == 1) {
            return String.format(
                    "%s is not allowed to execute that command. Must be of type %s",
                    commandSender().getClass().getSimpleName(),
                    TypeUtils.simpleName(this.requiredSenderTypes.iterator().next())
            );
        }
        return String.format(
                "%s is not allowed to execute that command. Must be one of %s",
                commandSender().getClass().getSimpleName(),
                this.requiredSenderTypes.stream().map(TypeUtils::simpleName).collect(Collectors.joining(", "))
        );
    }

    /**
     * Returns the Command which the sender is invalid for.
     *
     * @return command
     */
    @API(status = API.Status.STABLE)
    public @Nullable Command<?> command() {
        return this.command;
    }
}
