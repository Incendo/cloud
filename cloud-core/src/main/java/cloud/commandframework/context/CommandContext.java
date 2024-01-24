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
package cloud.commandframework.context;

import cloud.commandframework.CommandManager;
import cloud.commandframework.caption.Caption;
import cloud.commandframework.caption.CaptionFormatter;
import cloud.commandframework.caption.CaptionRegistry;
import cloud.commandframework.caption.CaptionVariable;
import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.injection.ParameterInjectorRegistry;
import cloud.commandframework.key.CloudKey;
import cloud.commandframework.key.MutableCloudKeyContainer;
import cloud.commandframework.parser.flag.FlagContext;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Command context used to assist in the parsing of commands
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public class CommandContext<C> implements MutableCloudKeyContainer {

    private final List<ParsingContext<C>> parsingContexts = new LinkedList<>();
    private final FlagContext flagContext = FlagContext.create();
    private final Map<CloudKey<?>, Object> internalStorage = new HashMap<>();
    private final C commandSender;
    private final boolean suggestions;
    private final CaptionRegistry<C> captionRegistry;
    private final CommandManager<C> commandManager;

    private CommandComponent<C> currentComponent = null;

    /**
     * Creates a new command context instance.
     *
     * @param commandSender  the sender of the command
     * @param commandManager command manager
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public CommandContext(final @NonNull C commandSender, final @NonNull CommandManager<C> commandManager) {
        this(false, commandSender, commandManager);
    }

    /**
     * Creates a new command context instance.
     *
     * @param suggestions    whether the context is created for command suggestions
     * @param commandSender  the sender of the command
     * @param commandManager command manager
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public CommandContext(
            final boolean suggestions,
            final @NonNull C commandSender,
            final @NonNull CommandManager<C> commandManager
    ) {
        this.commandSender = commandSender;
        this.suggestions = suggestions;
        this.commandManager = commandManager;
        this.captionRegistry = commandManager.captionRegistry();
    }

    /**
     * Formats a {@code caption} using the {@link CommandManager#captionFormatter()}.
     *
     * @param caption   the caption key
     * @param variables the variables to use during formatting
     * @return the formatted caption
     */
    public @NonNull String formatCaption(
            final @NonNull Caption caption,
            final @NonNull CaptionVariable @NonNull... variables
    ) {
        return this.formatCaption(this.commandManager.captionFormatter(), caption, variables);
    }

    /**
     * Formats a {@code caption} using the given {@code formatter}.
     *
     * @param <T>       the message type produced by the formatter
     * @param formatter the formatter
     * @param caption   the caption key
     * @param variables the variables to use during formatting
     * @return the formatted caption
     */
    public <T> @NonNull T formatCaption(
            final @NonNull CaptionFormatter<C, T> formatter,
            final @NonNull Caption caption,
            final @NonNull CaptionVariable @NonNull... variables
    ) {
        return formatter.formatCaption(
                caption,
                this.commandSender,
                this.captionRegistry.caption(caption, this.commandSender),
                variables
        );
    }

    /**
     * Returns the sender that executed the command.
     *
     * @return the command sender
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull C sender() {
        return this.commandSender;
    }

    /**
     * Checks whether the sender that executed the command has a permission.
     *
     * @param permission the permission
     * @return {@code true} if the {@link #sender()} has the permission, else {@code false}
     * @since 1.6.0
     */
    @API(status = API.Status.STABLE, since = "1.6.0")
    public boolean hasPermission(final @NonNull Permission permission) {
        return this.commandManager.testPermission(this.commandSender, permission).allowed();
    }

    /**
     * Checks whether the sender that executed the command has a permission.
     *
     * @param permission the permission
     * @return {@code true} if the {@link #sender()} has the permission, else {@code false}
     * @since 1.6.0
     */
    @API(status = API.Status.STABLE, since = "1.6.0")
    public boolean hasPermission(final @NonNull String permission) {
        return this.commandManager.hasPermission(this.commandSender, permission);
    }

    /**
     * Checks if this context was created for tab completion purposes.
     *
     * @return {@code true} if this context is requesting suggestions, else {@code false}
     */
    public boolean isSuggestions() {
        return this.suggestions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends @NonNull Object> void store(final @NonNull String key, final T value) {
        this.internalStorage.put(CloudKey.of(key), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends @NonNull Object> void store(final @NonNull CloudKey<T> key, final T value) {
        this.internalStorage.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final @NonNull CloudKey<?> key) {
        return this.internalStorage.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends @NonNull Object> @NonNull Optional<T> optional(final @NonNull CloudKey<T> key) {
        final Object value = this.internalStorage.get(key);
        if (value != null) {
            @SuppressWarnings("unchecked") final T castedValue = (T) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends @NonNull Object> @NonNull Optional<T> optional(final @NonNull String key) {
        final Object value = this.internalStorage.get(CloudKey.of(key));
        if (value != null) {
            @SuppressWarnings("unchecked") final T castedValue = (T) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final @NonNull CloudKey<?> key) {
        this.internalStorage.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T computeIfAbsent(
            final @NonNull CloudKey<T> key,
            final @NonNull Function<CloudKey<T>, T> defaultFunction
    ) {
        @SuppressWarnings("unchecked")
        final T castedValue = (T) this.internalStorage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<T>) k));
        return castedValue;
    }

    /**
     * Returns a copy of the raw input.
     *
     * @return raw input
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandInput rawInput() {
        return this.getOrDefault("__raw_input__", CommandInput.empty()).copy();
    }

    /**
     * Creates a parsing context instance for the given component.
     *
     * @param component the component
     * @return the created context
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "2.0.0")
    public @NonNull ParsingContext<C> createParsingContext(final @NonNull CommandComponent<C> component) {
        final ParsingContext<C> parsingContext = new ParsingContext<>(component);
        this.parsingContexts.add(parsingContext);
        return parsingContext;
    }

    /**
     * Returns the context for the given component.
     *
     * @param component the component
     * @return the context
     * @param <T> the type of the component
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "2.0.0")
    public <T> @NonNull ParsingContext<C> parsingContext(final @NonNull CommandComponent<C> component) {
        return this.parsingContexts.stream()
                .filter(context -> context.component().equals(component))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns the context for the component at the given position.
     *
     * @param position the position
     * @return the context
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "1.9.0")
    public @NonNull ParsingContext<C> parsingContext(final int position) {
        return this.parsingContexts.get(position);
    }

    /**
     * Returns the context for the component with the given name.
     *
     * @param name the name
     * @return the context
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "1.9.0")
    public @NonNull ParsingContext<C> parsingContext(final String name) {
        return this.parsingContexts.stream()
                .filter(context -> context.component().name().equals(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns an unmodifiable view of the stored parsing contexts.
     *
     * @return the contexts
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "2.0.0")
    public @NonNull List<@NonNull ParsingContext<@NonNull C>> parsingContexts() {
        return Collections.unmodifiableList(this.parsingContexts);
    }

    /**
     * Returns the associated {@link FlagContext} instance.
     *
     * @return flag context
     */
    public @NonNull FlagContext flags() {
        return this.flagContext;
    }

    /**
     * Returns the component that is currently being parsed for this command context.
     * This value will be updated whenever the context is used to provide new
     * suggestions or parse a new command argument.
     *
     * @return the {@link CommandComponent} that is currently being parsed, or {@code null}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @Nullable CommandComponent<C> currentComponent() {
        return this.currentComponent;
    }

    /**
     * Sets the component that is currently being parsed for this command context.
     * This value should be updated whenever the context is used to provide new
     * suggestions or parse a new command argument.
     *
     * @param component the component that is currently being parsed, or {@code null}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public void currentComponent(final @Nullable CommandComponent<C> component) {
        this.currentComponent = component;
    }

    /**
     * Attempts to retrieve a value that has been registered to the associated command manager's
     * {@link ParameterInjectorRegistry}.
     *
     * @param <T>   type to inject
     * @param clazz class of type to inject
     * @return optional that may contain the created value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public <T> @NonNull Optional<T> inject(final @NonNull Class<T> clazz) {
        if (this.commandManager == null) {
            throw new UnsupportedOperationException(
                    "Cannot retrieve injectable values from a command context that is not associated with a command manager"
            );
        }
        return this.commandManager.parameterInjectorRegistry().getInjectable(clazz, this, AnnotationAccessor.empty());
    }

    /**
     * Attempts to retrieve a value that has been registered to the associated command manager's
     * {@link ParameterInjectorRegistry}.
     *
     * @param <T>  type to inject
     * @param type type to inject
     * @return optional that may contain the created value
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public <T> @NonNull Optional<T> inject(final @NonNull TypeToken<T> type) {
        if (this.commandManager == null) {
            throw new UnsupportedOperationException(
                    "Cannot retrieve injectable values from a command context that is not associated with a command manager"
            );
        }
        return this.commandManager.parameterInjectorRegistry().getInjectable(type, this, AnnotationAccessor.empty());
    }

    @Override
    public final @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
        return Collections.unmodifiableMap(this.internalStorage);
    }
}
