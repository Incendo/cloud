//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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

package cloud.commandframework.quilt;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class QuiltExecutor<C, S extends CommandSource> implements Command<S> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Text NEWLINE = new LiteralText("\n");
    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final QuiltCommandManager<C, S> manager;
    private final Function<S, String> getName;
    private final BiConsumer<S, Text> sendError;

    QuiltExecutor(
            final @NonNull QuiltCommandManager<C, S> manager,
            final @NonNull Function<S, String> getName,
            final @NonNull BiConsumer<S, Text> sendError
    ) {
        this.manager = manager;
        this.getName = getName;
        this.sendError = sendError;
    }

    @Override
    public int run(final @NonNull CommandContext<S> ctx) {
        final S source = ctx.getSource();
        final String input = ctx.getInput().substring(ctx.getLastChild().getNodes().get(0).getRange().getStart());
        final C sender = this.manager.getCommandSourceMapper().apply(source);
        this.manager.executeCommand(sender, input).whenComplete(this.getResultConsumer(source, sender));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private @NonNull BiConsumer<@NonNull CommandResult<C>, ? super Throwable> getResultConsumer(
            final @NonNull S source,
            final @NonNull C sender
    ) {
        return (result, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                final Throwable finalThrowable = throwable;
                if (throwable instanceof InvalidSyntaxException) {
                    this.manager.handleException(
                            sender,
                            InvalidSyntaxException.class,
                            (InvalidSyntaxException) throwable,
                            (c, e) ->
                                    this.sendError.accept(
                                            source,
                                            new LiteralText("Invalid Command Syntax. Correct command syntax is: ")
                                                    .append(new LiteralText(String.format("/%s", e.getCorrectSyntax()))
                                                            .styled(style -> style.withColor(Formatting.GRAY))))
                    );
                } else if (throwable instanceof InvalidCommandSenderException) {
                    this.manager.handleException(
                            sender,
                            InvalidCommandSenderException.class,
                            (InvalidCommandSenderException) throwable,
                            (c, e) ->
                                    this.sendError.accept(source, new LiteralText(finalThrowable.getMessage()))
                    );
                } else if (throwable instanceof NoPermissionException) {
                    this.manager.handleException(
                            sender,
                            NoPermissionException.class,
                            (NoPermissionException) throwable,
                            (c, e) -> this.sendError.accept(source, new LiteralText(MESSAGE_NO_PERMS))
                    );
                } else if (throwable instanceof NoSuchCommandException) {
                    this.manager.handleException(
                            sender,
                            NoSuchCommandException.class,
                            (NoSuchCommandException) throwable,
                            (c, e) -> this.sendError.accept(source, new LiteralText(MESSAGE_UNKNOWN_COMMAND))
                    );
                } else if (throwable instanceof ArgumentParseException) {
                    this.manager.handleException(
                            sender,
                            ArgumentParseException.class,
                            (ArgumentParseException) throwable,
                            (c, e) -> {
                                if (finalThrowable.getCause() instanceof CommandSyntaxException) {
                                    this.sendError.accept(source, new LiteralText("Invalid Command Argument: ")
                                        .append(new LiteralText("")
                                            .append(Texts.toText(((CommandSyntaxException) finalThrowable.getCause()).getRawMessage()))
                                            .formatted(Formatting.GRAY)));
                                } else {
                                    this.sendError.accept(source, new LiteralText("Invalid Command Argument: ")
                                        .append(new LiteralText(finalThrowable.getCause().getMessage())
                                            .formatted(Formatting.GRAY)));
                                }
                            }
                    );
                } else if (throwable instanceof CommandExecutionException) {
                    this.manager.handleException(
                            sender,
                            CommandExecutionException.class,
                            (CommandExecutionException) throwable,
                            (c, e) -> {
                                this.sendError.accept(source, this.decorateHoverStacktrace(
                                                new LiteralText(MESSAGE_INTERNAL_ERROR),
                                                finalThrowable.getCause(),
                                                sender
                                        ));
                                LOGGER.warn(
                                        "Error occurred while executing command for user {}:",
                                        this.getName.apply(source),
                                        finalThrowable
                                );
                            }
                    );
                } else {
                    this.sendError.accept(source, this.decorateHoverStacktrace(
                            new LiteralText(MESSAGE_INTERNAL_ERROR),
                            throwable,
                            sender
                    ));
                    LOGGER.warn(
                            "Error occurred while executing command for user {}:",
                            this.getName.apply(source),
                            throwable
                    );
                }
            }
        };
    }

    private MutableText decorateHoverStacktrace(final MutableText input, final Throwable cause, final C sender) {
        if (!this.manager.hasPermission(sender, "cloud.hover-stacktrace")) {
            return input;
        }

        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final String stackTrace = writer.toString().replace("\t", "    ");
        return input.styled(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText(stackTrace)
                                .append(NEWLINE)
                                .append(new LiteralText("    Click to copy")
                                        .styled(s2 -> s2
                                                .withColor(Formatting.GRAY)
                                                .withItalic(true)))
                ))
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        stackTrace
                )));
    }

}
