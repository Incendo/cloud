//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.CommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

/**
 * Creates and registers {@link ExceptionHandler ExceptionHandlers} that send a {@link Component} to the
 * command sender.
 *
 * @param <C> command sender type
 */
public final class MinecraftExceptionHandler<C> {

    private static final Component NULL = text("null");

    /**
     * The default {@link InvalidSyntaxException} handler.
     *
     * @param <C> sender type
     * @return {@link InvalidSyntaxException} handler function
     * @see #withInvalidSyntaxHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, InvalidSyntaxException>, Component> defaultInvalidSyntaxFunction() {
        return ctx -> text("Invalid command syntax. Correct command syntax is: ", NamedTextColor.RED)
                .append(ComponentHelper.highlight(
                        text(
                                String.format("/%s", ctx.exception().getCorrectSyntax()),
                                NamedTextColor.GRAY
                        ),
                        NamedTextColor.WHITE
                ));
    }

    /**
     * The default {@link InvalidCommandSenderException} handler.
     *
     * @param <C> sender type
     * @return {@link InvalidCommandSenderException} handler function
     * @see #withInvalidSenderHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, InvalidCommandSenderException>, Component> defaultInvalidSenderFunction() {
        return ctx -> text("Invalid command sender. You must be of type ", NamedTextColor.RED)
                .append(text(
                        ctx.exception().getRequiredSender().getSimpleName(),
                        NamedTextColor.GRAY
                ));
    }

    /**
     * The default {@link NoPermissionException} handler.
     *
     * @param <C> sender type
     * @return {@link NoPermissionException} handler function
     * @see #withNoPermissionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, NoPermissionException>, Component> defaultNoPermissionFunction() {
        return ctx -> text(
                "I'm sorry, but you do not have permission to perform this command. \n"
                        + "Please contact the server administrators if you believe that this is in error.",
                NamedTextColor.RED
        );
    }

    /**
     * The default {@link ArgumentParseException} handler.
     *
     * @param <C> sender type
     * @return {@link ArgumentParseException} handler function
     * @see #withArgumentParsingHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, ArgumentParseException>, Component> defaultArgumentParsingFunction() {
        return ctx -> text("Invalid command argument: ", NamedTextColor.RED)
                .append(getMessage(ctx.exception().getCause()).colorIfAbsent(NamedTextColor.GRAY));
    }

    /**
     * The default logger for {@link #defaultCommandExecutionFunction()}, using
     * {@link Throwable#printStackTrace()}.
     *
     * @param <C> sender type
     * @return default logger
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Consumer<ExceptionContext<C, CommandExecutionException>> defaultCommandExecutionLogger() {
        return ctx -> ctx.exception().getCause().printStackTrace();
    }

    /**
     * The default {@link CommandExecutionException} handler.
     *
     * @param <C> sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionLogger()
     * @see #withCommandExecutionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, CommandExecutionException>, Component> defaultCommandExecutionFunction() {
        return defaultCommandExecutionFunction(defaultCommandExecutionLogger());
    }

    /**
     * The default {@link CommandExecutionException} handler, with a specific logger.
     *
     * @param logger logger
     * @param <C>    sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionLogger()
     * @see #defaultCommandExecutionFunction()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, CommandExecutionException>, Component> defaultCommandExecutionFunction(
            final Consumer<ExceptionContext<C, CommandExecutionException>> logger
    ) {
        return ctx -> {
            logger.accept(ctx);
            final Throwable cause = ctx.exception().getCause();

            final StringWriter writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(writer));
            final String stackTrace = writer.toString().replaceAll("\t", "    ");
            final HoverEvent<Component> hover = HoverEvent.showText(
                    text()
                            .append(getMessage(cause))
                            .append(newline())
                            .append(text(stackTrace))
                            .append(newline())
                            .append(text(
                                    "    Click to copy",
                                    NamedTextColor.GRAY,
                                    TextDecoration.ITALIC
                            ))
            );
            final ClickEvent click = ClickEvent.copyToClipboard(stackTrace);
            return text()
                    .content("An internal error occurred while attempting to perform this command.")
                    .color(NamedTextColor.RED)
                    .hoverEvent(hover)
                    .clickEvent(click)
                    .build();
        };
    }

    private final Map<Class<? extends Throwable>, Function<ExceptionContext<C, ?>, @Nullable Component>> componentBuilders =
            new HashMap<>();
    private BiFunction<ExceptionContext<C, ?>, Component, Component> decorator = (ctx, msg) -> msg;

    /**
     * Sets the default invalid syntax handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withInvalidSyntaxHandler() {
        return this.withHandler(InvalidSyntaxException.class, defaultInvalidSyntaxFunction());
    }

    /**
     * Sets the default invalid sender handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withInvalidSenderHandler() {
        return this.withHandler(InvalidCommandSenderException.class, defaultInvalidSenderFunction());
    }

    /**
     * Use the default no permission handler
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withNoPermissionHandler() {
        return this.withHandler(NoPermissionException.class, defaultNoPermissionFunction());
    }

    /**
     * Sets the default argument parsing handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withArgumentParsingHandler() {
        return this.withHandler(ArgumentParseException.class, defaultArgumentParsingFunction());
    }

    /**
     * Sets the default {@link CommandExecutionException} handler.
     *
     * @return {@code this}
     * @since 1.2.0
     */
    public @NonNull @This MinecraftExceptionHandler<C> withCommandExecutionHandler() {
        return this.withHandler(CommandExecutionException.class, defaultCommandExecutionFunction());
    }

    /**
     * Sets all the default exception handlers.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withDefaultHandlers() {
        return this
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler()
                .withCommandExecutionHandler();
    }

    /**
     * Sets the exception handler for the given {@code type}. A handler
     * can return {@code null} to indicate no message should be sent.
     *
     * @param type             the exception type to handle
     * @param componentFactory the factory that produces the components
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Throwable> @NonNull @This MinecraftExceptionHandler<C> withHandler(
            final @NonNull Class<T> type,
            final @NonNull Function<@NonNull ExceptionContext<@NonNull C, @NonNull T>, @Nullable Component> componentFactory
    ) {
        this.componentBuilders.put(type, (Function) componentFactory);
        return this;
    }

    /**
     * Sets the decorator that acts on a component before it's sent to the sender.
     *
     * @param decorator the component decorator
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull @This MinecraftExceptionHandler<C> withDecorator(
            final @NonNull BiFunction<@NonNull ExceptionContext<@NonNull C, ?>, @NonNull Component, @NonNull Component> decorator
    ) {
        this.decorator = decorator;
        return this;
    }

    /**
     * Sets the decorator that acts on a component before it's sent to the sender.
     *
     * @param decorator the component decorator
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withDecorator(
            final @NonNull Function<@NonNull Component, @NonNull Component> decorator
    ) {
        return this.withDecorator((ctx, message) -> decorator.apply(message));
    }

    /**
     * Registers the exceptions to the {@link cloud.commandframework.exceptions.handling.ExceptionController}.
     *
     * @param manager        the manager instance
     * @param audienceMapper the mapper that maps command sender to audience instances
     */
    public void apply(
            final @NonNull CommandManager<C> manager,
            final @NonNull Function<@NonNull C, @NonNull Audience> audienceMapper
    ) {
        this.componentBuilders.forEach((type, handler) -> {
            manager.exceptionController().registerHandler(type, ctx -> {
                final @Nullable Component message = handler.apply(ctx);
                if (message != null) {
                    audienceMapper.apply(ctx.context().sender()).sendMessage(this.decorator.apply(ctx, message));
                }
            });
        });
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }
}
