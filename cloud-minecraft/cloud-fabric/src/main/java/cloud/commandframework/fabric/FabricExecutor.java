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
package cloud.commandframework.fabric;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

final class FabricExecutor<C, S extends SharedSuggestionProvider> implements Command<S> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component NEWLINE = Component.literal("\n");
    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final FabricCommandManager<C, S> manager;
    private final Function<S, String> getName;
    private final BiConsumer<S, Component> sendError;

    FabricExecutor(
            final @NonNull FabricCommandManager<C, S> manager,
            final @NonNull Function<S, String> getName,
            final @NonNull BiConsumer<S, Component> sendError
    ) {
        this.manager = manager;
        this.getName = getName;
        this.sendError = sendError;
    }

    @Override
    public int run(final @NonNull CommandContext<S> ctx) {
        final S source = ctx.getSource();
        final String input = ctx.getInput().substring(ctx.getLastChild().getNodes().get(0).getRange().getStart());
        final C sender = this.manager.commandSourceMapper().apply(source);

        this.manager.executeCommand(sender, input).whenComplete((result, throwable) -> {
            if (throwable == null) {
                return;
            }
            if (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }
            this.handleThrowable(source, sender, throwable);
        });
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private void handleThrowable(final @NonNull S source, final @NonNull C sender, final @NonNull Throwable throwable) {
        if (throwable instanceof InvalidSyntaxException) {
            this.manager.handleException(
                    sender,
                    InvalidSyntaxException.class,
                    (InvalidSyntaxException) throwable,
                    (c, e) -> this.sendError.accept(
                            source,
                            Component.literal("Invalid Command Syntax. Correct command syntax is: ")
                                    .append(Component.literal(String.format("/%s", e.getCorrectSyntax()))
                                            .withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                    )
            );
        } else if (throwable instanceof InvalidCommandSenderException) {
            this.manager.handleException(
                    sender,
                    InvalidCommandSenderException.class,
                    (InvalidCommandSenderException) throwable,
                    (c, e) -> this.sendError.accept(source, Component.literal(throwable.getMessage()))
            );
        } else if (throwable instanceof NoPermissionException) {
            this.manager.handleException(
                    sender,
                    NoPermissionException.class,
                    (NoPermissionException) throwable,
                    (c, e) -> this.sendError.accept(source, Component.literal(MESSAGE_NO_PERMS))
            );
        } else if (throwable instanceof NoSuchCommandException) {
            this.manager.handleException(
                    sender,
                    NoSuchCommandException.class,
                    (NoSuchCommandException) throwable,
                    (c, e) -> this.sendError.accept(source, Component.literal(MESSAGE_UNKNOWN_COMMAND))
            );
        } else if (throwable instanceof ArgumentParseException) {
            this.manager.handleException(
                    sender,
                    ArgumentParseException.class,
                    (ArgumentParseException) throwable,
                    (c, e) -> {
                        if (throwable.getCause() instanceof CommandSyntaxException) {
                            this.sendError.accept(source, Component.literal("Invalid Command Argument: ")
                                    .append(Component.literal("")
                                            .append(ComponentUtils
                                                    .fromMessage(((CommandSyntaxException) throwable.getCause()).getRawMessage()))
                                            .withStyle(ChatFormatting.GRAY)));
                        } else {
                            this.sendError.accept(source, Component.literal("Invalid Command Argument: ")
                                    .append(Component.literal(throwable.getCause().getMessage())
                                            .withStyle(ChatFormatting.GRAY)));
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
                                Component.literal(MESSAGE_INTERNAL_ERROR),
                                throwable.getCause(),
                                sender
                        ));
                        LOGGER.warn(
                                "Error occurred while executing command for user {}:",
                                this.getName.apply(source),
                                throwable
                        );
                    }
            );
        } else {
            this.sendError.accept(source, this.decorateHoverStacktrace(
                    Component.literal(MESSAGE_INTERNAL_ERROR),
                    throwable,
                    sender
            ));
            LOGGER.warn("Error occurred while executing command for user {}:", this.getName.apply(source), throwable);
        }
    }

    private MutableComponent decorateHoverStacktrace(final MutableComponent input, final Throwable cause, final C sender) {
        if (!this.manager.hasPermission(sender, "cloud.hover-stacktrace")) {
            return input;
        }

        final StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        final String stackTrace = writer.toString().replace("\t", "    ");
        return input.withStyle(style -> style
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal(stackTrace)
                                .append(NEWLINE)
                                .append(Component.literal("    Click to copy")
                                        .withStyle(s2 -> s2.withColor(ChatFormatting.GRAY).withItalic(true)))
                ))
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        stackTrace
                )));
    }
}
