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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Exception handler that sends {@link Component} to the sender. All component builders
 * can be overridden and the handled exception types can be configured (see {@link ExceptionType} for types)
 *
 * @param <C> Command sender type
 */
public final class MinecraftExceptionHandler<C> {

    private static final Component NULL = Component.text("null");

    /**
     * Default component builder for {@link InvalidSyntaxException}
     */
    public static final Function<Exception, Component> DEFAULT_INVALID_SYNTAX_FUNCTION =
            e -> Component.text("Invalid command syntax. Correct command syntax is: ", NamedTextColor.RED)
                    .append(ComponentHelper.highlight(
                            Component.text(
                                    String.format("/%s", ((InvalidSyntaxException) e).getCorrectSyntax()),
                                    NamedTextColor.GRAY
                            ),
                            NamedTextColor.WHITE
                    ));
    /**
     * Default component builder for {@link InvalidCommandSenderException}
     */
    public static final Function<Exception, Component> DEFAULT_INVALID_SENDER_FUNCTION =
            e -> Component.text("Invalid command sender. You must be of type ", NamedTextColor.RED)
                    .append(Component.text(
                            ((InvalidCommandSenderException) e).getRequiredSender().getSimpleName(),
                            NamedTextColor.GRAY
                    ));
    /**
     * Default component builder for {@link NoPermissionException}
     */
    public static final Function<Exception, Component> DEFAULT_NO_PERMISSION_FUNCTION =
            e -> Component.text(
                    "I'm sorry, but you do not have permission to perform this command. \n"
                            + "Please contact the server administrators if you believe that this is in error.",
                    NamedTextColor.RED
            );
    /**
     * Default component builder for {@link ArgumentParseException}
     */
    public static final Function<Exception, Component> DEFAULT_ARGUMENT_PARSING_FUNCTION =
            e -> Component.text("Invalid command argument: ", NamedTextColor.RED)
                    .append(getMessage(e.getCause()).colorIfAbsent(NamedTextColor.GRAY));
    /**
     * Default component builder for {@link CommandExecutionException}
     *
     * @since 1.2.0
     */
    public static final Function<Exception, Component> DEFAULT_COMMAND_EXECUTION_FUNCTION =
            e -> {
                final Throwable cause = e.getCause();
                cause.printStackTrace();

                final StringWriter writer = new StringWriter();
                cause.printStackTrace(new PrintWriter(writer));
                final String stackTrace = writer.toString().replaceAll("\t", "    ");
                final HoverEvent<Component> hover = HoverEvent.showText(
                        Component.text()
                                .append(getMessage(cause))
                                .append(Component.newline())
                                .append(Component.text(stackTrace))
                                .append(Component.newline())
                                .append(Component.text(
                                        "    Click to copy",
                                        NamedTextColor.GRAY,
                                        TextDecoration.ITALIC
                                ))
                );
                final ClickEvent click = ClickEvent.copyToClipboard(stackTrace);
                return Component.text()
                        .content("An internal error occurred while attempting to perform this command.")
                        .color(NamedTextColor.RED)
                        .hoverEvent(hover)
                        .clickEvent(click)
                        .build();
            };

    private final Map<ExceptionType, BiFunction<C, Exception, Component>> componentBuilders = new HashMap<>();
    private Function<Component, Component> decorator = Function.identity();

    /**
     * Sets the default invalid syntax handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withInvalidSyntaxHandler() {
        return this.withDefaultHandler(ExceptionType.INVALID_SYNTAX);
    }

    /**
     * Sets the default invalid sender handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withInvalidSenderHandler() {
        return this.withDefaultHandler(ExceptionType.INVALID_SENDER);
    }

    /**
     * Use the default no permission handler
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withNoPermissionHandler() {
        return this.withDefaultHandler(ExceptionType.NO_PERMISSION);
    }

    /**
     * Sets the default argument parsing handler.
     *
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withArgumentParsingHandler() {
        return this.withDefaultHandler(ExceptionType.ARGUMENT_PARSING);
    }

    /**
     * Sets the default {@link CommandExecutionException} handler.
     *
     * @return {@code this}
     * @since 1.2.0
     */
    public @NonNull @This MinecraftExceptionHandler<C> withCommandExecutionHandler() {
        return this.withDefaultHandler(ExceptionType.COMMAND_EXECUTION);
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
     * Sets the exception handler for the given {@code type}.
     *
     * @param type             the type of exception to handle
     * @param componentFactory the factory that produces the components
     * @return {@code this}
     */
    public @NonNull @This MinecraftExceptionHandler<C> withHandler(
            final @NonNull ExceptionType type,
            final @NonNull Function<@NonNull Exception, @NonNull Component> componentFactory
    ) {
        return this.withHandler(
                type,
                (sender, exception) -> componentFactory.apply(exception)
        );
    }

    /**
     * Sets the exception handler for the given {@code type} to the {@link ExceptionType#defaultHandler()}.
     *
     * @param type the type of exception to handle
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull @This MinecraftExceptionHandler<C> withDefaultHandler(
            final @NonNull ExceptionType type
    ) {
        return this.withHandler(type, type.defaultHandler());
    }

    /**
     * Sets the exception handler for the given {@code type}.
     *
     * @param type             the exception type to handle
     * @param componentFactory the factory that produces the components
     * @return {@code this}
     * @since 1.2.0
     */
    public @NonNull @This MinecraftExceptionHandler<C> withHandler(
            final @NonNull ExceptionType type,
            final @NonNull BiFunction<@NonNull C, @NonNull Exception, @NonNull Component> componentFactory
    ) {
        this.componentBuilders.put(
                type,
                componentFactory
        );
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
        this.decorator = decorator;
        return this;
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
        this.componentBuilders.forEach((type, handler) -> manager.exceptionController().registerHandler(
                type.exceptionClass(),
                ctx -> audienceMapper.apply(ctx.context().sender()).sendMessage(
                        Identity.nil(),
                        this.decorator.apply(handler.apply(ctx.context().sender(), ctx.exception()))
                )
        ));
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

    /**
     * Exception types.
     */
    public enum ExceptionType {
        /**
         * The input does not correspond to any known command ({@link InvalidSyntaxException})
         */
        INVALID_SYNTAX(InvalidSyntaxException.class, DEFAULT_INVALID_SYNTAX_FUNCTION),
        /**
         * The sender is not of the right type ({@link InvalidCommandSenderException})
         */
        INVALID_SENDER(InvalidCommandSenderException.class, DEFAULT_INVALID_SENDER_FUNCTION),
        /**
         * The sender does not have permission to execute the command ({@link NoPermissionException})
         */
        NO_PERMISSION(NoPermissionException.class, DEFAULT_NO_PERMISSION_FUNCTION),
        /**
         * An argument failed to parse ({@link ArgumentParseException})
         */
        ARGUMENT_PARSING(ArgumentParseException.class, DEFAULT_ARGUMENT_PARSING_FUNCTION),
        /**
         * A command handler had an exception ({@link CommandExecutionException})
         *
         * @since 1.2.0
         */
        COMMAND_EXECUTION(CommandExecutionException.class, DEFAULT_COMMAND_EXECUTION_FUNCTION);

        private final Class<? extends Exception> exceptionClass;
        private final Function<Exception, Component> defaultHandler;

        ExceptionType(
                final @NonNull Class<? extends Exception> exceptionClass,
                final @NonNull Function<@NonNull Exception, @NonNull Component> defaultHandler
        ) {
            this.exceptionClass = exceptionClass;
            this.defaultHandler = defaultHandler;
        }

        /**
         * Returns the associated exception class.
         *
         * @return the exception class
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Class<? extends Exception> exceptionClass() {
            return this.exceptionClass;
        }

        /**
         * Returns the default handler for the type.
         *
         * @return the default handler
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Function<@NonNull Exception, @NonNull Component> defaultHandler() {
            return this.defaultHandler;
        }
    }
}
