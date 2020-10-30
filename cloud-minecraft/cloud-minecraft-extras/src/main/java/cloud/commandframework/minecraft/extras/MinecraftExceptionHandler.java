//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Exception handler that sends {@link Component} to the sender. All component builders
 * can be overridden and the handled exception types can be configured (see {@link ExceptionType} for types)
 *
 * @param <C> Command sender type
 */
public final class MinecraftExceptionHandler<C> {

    /**
     * Default component builder for {@link InvalidSyntaxException}
     */
    public static final Function<Exception, Component> DEFAULT_INVALID_SYNTAX_FUNCTION =
            e -> Component.text()
                    .append(Component.text("Invalid command syntax. Correct command syntax is: ", NamedTextColor.RED))
                    .append(ComponentHelper.highlight(
                            Component.text(
                                    String.format("/%s", ((InvalidSyntaxException) e).getCorrectSyntax()),
                                    NamedTextColor.GRAY
                            ),
                            NamedTextColor.WHITE
                    ))
                    .build();
    /**
     * Default component builder for {@link InvalidCommandSenderException}
     */
    public static final Function<Exception, Component> DEFAULT_INVALID_SENDER_FUNCTION =
            e -> Component.text()
                    .append(Component.text("Invalid command sender. You must be of type ", NamedTextColor.RED))
                    .append(Component.text(
                            ((InvalidCommandSenderException) e).getRequiredSender().getSimpleName(),
                            NamedTextColor.GRAY
                    ))
                    .build();
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
            e -> Component.text()
                    .append(Component.text("Invalid command argument: ", NamedTextColor.RED))
                    .append(Component.text(e.getCause().getMessage(), NamedTextColor.GRAY))
                    .build();

    private final Map<ExceptionType, BiFunction<C, Exception, Component>> componentBuilders = new HashMap<>();
    private Function<Component, Component> decorator = Function.identity();

    /**
     * Use the default invalid syntax handler
     *
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withInvalidSyntaxHandler() {
        return this.withHandler(ExceptionType.INVALID_SYNTAX, DEFAULT_INVALID_SYNTAX_FUNCTION);
    }

    /**
     * Use the default invalid sender handler
     *
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withInvalidSenderHandler() {
        return this.withHandler(ExceptionType.INVALID_SENDER, DEFAULT_INVALID_SENDER_FUNCTION);
    }

    /**
     * Use the default no permission handler
     *
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withNoPermissionHandler() {
        return this.withHandler(ExceptionType.NO_PERMISSION, DEFAULT_NO_PERMISSION_FUNCTION);
    }

    /**
     * Use the default argument parsing handler
     *
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withArgumentParsingHandler() {
        return this.withHandler(ExceptionType.ARGUMENT_PARSING, DEFAULT_ARGUMENT_PARSING_FUNCTION);
    }

    /**
     * Use all four of the default exception handlers
     *
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withDefaultHandlers() {
        return this
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler();
    }

    /**
     * Specify an exception handler
     *
     * @param type             Exception type
     * @param componentBuilder Component builder
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withHandler(
            final @NonNull ExceptionType type,
            final @NonNull Function<@NonNull Exception, @NonNull Component> componentBuilder
    ) {
        return this.withHandler(
                type,
                (sender, exception) -> componentBuilder.apply(exception)
        );
    }

    /**
     * Specify an exception handler
     *
     * @param type             Exception type
     * @param componentBuilder Component builder
     * @return {@code this}
     * @since 1.2.0
     */
    public @NonNull MinecraftExceptionHandler<C> withHandler(
            final @NonNull ExceptionType type,
            final @NonNull BiFunction<@NonNull C, @NonNull Exception, @NonNull Component> componentBuilder
    ) {
        this.componentBuilders.put(
                type,
                componentBuilder
        );
        return this;
    }

    /**
     * Specify a decorator that acts on a component before it's sent to the sender
     *
     * @param decorator Component decorator
     * @return {@code this}
     */
    public @NonNull MinecraftExceptionHandler<C> withDecorator(
            final @NonNull Function<@NonNull Component, @NonNull Component> decorator
    ) {
        this.decorator = decorator;
        return this;
    }

    /**
     * Register the exception handlers in the manager
     *
     * @param manager        Manager instance
     * @param audienceMapper Mapper that maps command sender to audience instances
     */
    public void apply(
            final @NonNull CommandManager<C> manager,
            final @NonNull Function<@NonNull C, @NonNull Audience> audienceMapper
    ) {
        if (componentBuilders.containsKey(ExceptionType.INVALID_SYNTAX)) {
            manager.registerExceptionHandler(
                    InvalidSyntaxException.class,
                    (c, e) -> audienceMapper.apply(c).sendMessage(
                            Identity.nil(),
                            this.decorator.apply(this.componentBuilders.get(ExceptionType.INVALID_SYNTAX).apply(c, e))
                    )
            );
        }
        if (componentBuilders.containsKey(ExceptionType.INVALID_SENDER)) {
            manager.registerExceptionHandler(
                    InvalidCommandSenderException.class,
                    (c, e) -> audienceMapper.apply(c).sendMessage(
                            Identity.nil(),
                            this.decorator.apply(this.componentBuilders.get(ExceptionType.INVALID_SENDER).apply(c, e))
                    )
            );
        }
        if (componentBuilders.containsKey(ExceptionType.NO_PERMISSION)) {
            manager.registerExceptionHandler(
                    NoPermissionException.class,
                    (c, e) -> audienceMapper.apply(c).sendMessage(
                            Identity.nil(),
                            this.decorator.apply(this.componentBuilders.get(ExceptionType.NO_PERMISSION).apply(c, e))
                    )
            );
        }
        if (componentBuilders.containsKey(ExceptionType.ARGUMENT_PARSING)) {
            manager.registerExceptionHandler(
                    ArgumentParseException.class,
                    (c, e) -> audienceMapper.apply(c).sendMessage(
                            Identity.nil(),
                            this.decorator.apply(this.componentBuilders.get(ExceptionType.ARGUMENT_PARSING).apply(c, e))
                    )
            );
        }
    }


    /**
     * Exception types
     */
    public enum ExceptionType {
        /**
         * The input does not correspond to any known command
         */
        INVALID_SYNTAX,
        /**
         * The sender is not of the right type
         */
        INVALID_SENDER,
        /**
         * The sender does not have permission to execute the command
         */
        NO_PERMISSION,
        /**
         * An argument failed to parse
         */
        ARGUMENT_PARSING
    }

}
