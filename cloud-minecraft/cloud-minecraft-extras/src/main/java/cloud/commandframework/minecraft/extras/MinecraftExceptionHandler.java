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
     * @see #defaultInvalidSyntaxHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, InvalidSyntaxException>, Component> createDefaultInvalidSyntaxHandler() {
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
     * @see #defaultInvalidSenderHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, InvalidCommandSenderException>, Component> createDefaultInvalidSenderHandler() {
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
     * @see #defaultNoPermissionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, NoPermissionException>, Component> createDefaultNoPermissionHandler() {
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
     * @see #defaultArgumentParsingHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, ArgumentParseException>, Component> createDefaultArgumentParsingHandler() {
        return ctx -> text("Invalid command argument: ", NamedTextColor.RED)
                .append(getMessage(ctx.exception().getCause()).colorIfAbsent(NamedTextColor.GRAY));
    }

    /**
     * Default logger for {@link #createDefaultCommandExecutionHandler(Consumer)}, using
     * {@link Throwable#printStackTrace()} on the {@link CommandExecutionException}'s cause.
     *
     * @param <C> sender type
     * @return default logger
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Consumer<ExceptionContext<C, CommandExecutionException>> createDefaultCommandExecutionLogger() {
        return ctx -> ctx.exception().getCause().printStackTrace();
    }

    /**
     * The default {@link CommandExecutionException} handler, using {@link #createDefaultCommandExecutionLogger()}.
     *
     * @param <C> sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, CommandExecutionException>, Component> createDefaultCommandExecutionHandler() {
        return createDefaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
    }

    /**
     * The default {@link CommandExecutionException} handler, with a specific logger.
     *
     * @param logger logger
     * @param <C>    sender type
     * @return {@link CommandExecutionException} handler function
     * @see #defaultCommandExecutionHandler(Consumer)
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> Function<ExceptionContext<C, CommandExecutionException>, Component> createDefaultCommandExecutionHandler(
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
    private final AudienceProvider<C> audienceProvider;

    private MinecraftExceptionHandler(final AudienceProvider<C> audienceProvider) {
        this.audienceProvider = audienceProvider;
    }

    /**
     * Create a new {@link MinecraftExceptionHandler} using {@code audienceProvider}.
     *
     * @param audienceProvider audience provider
     * @param <C>              sender type
     * @return new {@link MinecraftExceptionHandler}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> MinecraftExceptionHandler<C> create(final AudienceProvider<C> audienceProvider) {
        return new MinecraftExceptionHandler<>(audienceProvider);
    }

    /**
     * Create a new {@link MinecraftExceptionHandler} using {@link AudienceProvider#nativeAudience()}.
     *
     * @param <C> sender type
     * @return new {@link MinecraftExceptionHandler}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C extends Audience> MinecraftExceptionHandler<C> createNative() {
        return create(AudienceProvider.nativeAudience());
    }

    /**
     * Use the default {@link InvalidSyntaxException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultInvalidSyntaxHandler() {
        return this.handler(InvalidSyntaxException.class, createDefaultInvalidSyntaxHandler());
    }

    /**
     * Use the default {@link InvalidCommandSenderException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultInvalidSenderHandler() {
        return this.handler(InvalidCommandSenderException.class, createDefaultInvalidSenderHandler());
    }

    /**
     * Use the default {@link NoPermissionException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultNoPermissionHandler() {
        return this.handler(NoPermissionException.class, createDefaultNoPermissionHandler());
    }

    /**
     * Use the default {@link ArgumentParseException} handler.
     *
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultArgumentParsingHandler() {
        return this.handler(ArgumentParseException.class, createDefaultArgumentParsingHandler());
    }

    /**
     * Use the default {@link CommandExecutionException} handler with {@link #createDefaultCommandExecutionLogger()}.
     *
     * @return {@code this}
     * @see #defaultCommandExecutionHandler(Consumer)
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultCommandExecutionHandler() {
        return this.defaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
    }

    /**
     * Use the default {@link CommandExecutionException} handler with a custom logger.
     *
     * @param logger logger
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultCommandExecutionHandler(
            final @NonNull Consumer<ExceptionContext<C, CommandExecutionException>> logger
    ) {
        return this.handler(CommandExecutionException.class, createDefaultCommandExecutionHandler(logger));
    }

    /**
     * Use all the default exception handlers.
     *
     * @return {@code this}
     * @see #defaultArgumentParsingHandler()
     * @see #defaultInvalidSenderHandler()
     * @see #defaultInvalidSyntaxHandler()
     * @see #defaultNoPermissionHandler()
     * @see #defaultCommandExecutionHandler()
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> defaultHandlers() {
        return this
                .defaultArgumentParsingHandler()
                .defaultInvalidSenderHandler()
                .defaultInvalidSyntaxHandler()
                .defaultNoPermissionHandler()
                .defaultCommandExecutionHandler();
    }

    /**
     * Sets the exception handler for the given {@code type}. A handler
     * can return {@code null} to indicate no message should be sent.
     *
     * @param <T>              exception type
     * @param type             the exception type to handle
     * @param componentFactory the factory that produces the components
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Throwable> @This @NonNull MinecraftExceptionHandler<C> handler(
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
    public @This @NonNull MinecraftExceptionHandler<C> decorator(
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
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @This @NonNull MinecraftExceptionHandler<C> decorator(
            final @NonNull Function<@NonNull Component, @NonNull Component> decorator
    ) {
        return this.decorator((ctx, message) -> decorator.apply(message));
    }

    /**
     * Registers configured handlers to the {@link cloud.commandframework.exceptions.handling.ExceptionController}.
     *
     * @param manager the manager instance
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public void registerTo(final @NonNull CommandManager<C> manager) {
        this.componentBuilders.forEach((type, handler) -> {
            manager.exceptionController().registerHandler(type, ctx -> {
                final @Nullable Component message = handler.apply(ctx);
                if (message != null) {
                    this.audienceProvider.apply(ctx.context().sender()).sendMessage(this.decorator.apply(ctx, message));
                }
            });
        });
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }
}
