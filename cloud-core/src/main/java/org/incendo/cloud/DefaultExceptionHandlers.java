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
package org.incendo.cloud;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;
import org.incendo.cloud.util.TypeUtils;

/**
 * Opinionated default exception handlers.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.INTERNAL)
final class DefaultExceptionHandlers<C> {

    private final Consumer<Triplet<CommandContext<C>, Caption, List<@NonNull CaptionVariable>>> messageSender;
    private final Consumer<Pair<String, Throwable>> logger;
    private final ExceptionController<C> exceptionController;

    DefaultExceptionHandlers(
            final @NonNull Consumer<Triplet<CommandContext<C>, Caption, List<@NonNull CaptionVariable>>> messageSender,
            final @NonNull Consumer<Pair<String, Throwable>> logger,
            final @NonNull ExceptionController<C> exceptionController
    ) {
        this.messageSender = Objects.requireNonNull(messageSender, "messageSender");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.exceptionController = Objects.requireNonNull(exceptionController, "exceptionController");
    }

    /**
     * Registers the exception handlers.
     */
    void register() {
        this.exceptionController.registerHandler(Throwable.class, context -> {
            this.sendMessage(context, StandardCaptionKeys.EXCEPTION_UNEXPECTED);
            this.log("An unhandled exception was thrown during command execution", context.exception());
        });
        this.exceptionController.registerHandler(CommandExecutionException.class, context -> {
            this.sendMessage(context, StandardCaptionKeys.EXCEPTION_UNEXPECTED);
            this.log("Exception executing command handler", context.exception().getCause());
        });
        this.exceptionController.registerHandler(ArgumentParseException.class, context ->
                this.sendMessage(
                        context,
                        StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                        CaptionVariable.of("cause", context.exception().getCause().getMessage())
                )
        );
        this.exceptionController.registerHandler(NoSuchCommandException.class, context ->
                this.sendMessage(
                        context,
                        StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND,
                        CaptionVariable.of("command", context.exception().suppliedCommand())
                )
        );
        this.exceptionController.registerHandler(NoPermissionException.class, context ->
                this.sendMessage(
                        context,
                        StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
                        CaptionVariable.of("permission", context.exception().permissionResult().permission().permissionString())
                )
        );
        this.exceptionController.registerHandler(InvalidCommandSenderException.class, context ->
                this.sendMessage(
                        context,
                        StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                        CaptionVariable.of("actual", context.context().sender().getClass().getSimpleName()),
                        CaptionVariable.of("expected", TypeUtils.simpleName(context.exception().requiredSender()))
                )
        );
        this.exceptionController.registerHandler(InvalidSyntaxException.class, context ->
                this.sendMessage(
                        context,
                        StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                        CaptionVariable.of("syntax", context.exception().correctSyntax())
                )
        );
    }

    private void sendMessage(
            final @NonNull ExceptionContext<C, ?> context,
            final @NonNull Caption caption,
            final @NonNull CaptionVariable @NonNull... variables
    ) {
        this.sendMessage(context.context(), caption, variables);
    }

    private void sendMessage(
            final @NonNull CommandContext<C> context,
            final @NonNull Caption caption,
            final @NonNull CaptionVariable @NonNull... variables
    ) {
        this.messageSender.accept(Triplet.of(context, caption, Arrays.asList(variables)));
    }

    private void log(final @NonNull String message, final @NonNull Throwable exception) {
        this.logger.accept(Pair.of(message, exception));
    }
}
