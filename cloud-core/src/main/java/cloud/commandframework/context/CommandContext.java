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
package cloud.commandframework.context;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.arguments.flags.FlagContext;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.CaptionVariableReplacementHandler;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.CloudKeyContainer;
import cloud.commandframework.keys.CloudKeyHolder;
import cloud.commandframework.permission.CommandPermission;
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
public class CommandContext<C> implements CloudKeyContainer {

    private final CaptionVariableReplacementHandler captionVariableReplacementHandler;
    private final List<ParsingContext<C>> parsingContexts = new LinkedList<>();
    private final FlagContext flagContext = FlagContext.create();
    private final Map<CloudKey<?>, Object> internalStorage = new HashMap<>();
    private final C commandSender;
    private final boolean suggestions;
    private final CaptionRegistry<C> captionRegistry;
    private final CommandManager<C> commandManager;

    private CommandComponent<C> currentComponent = null;

    /**
     * Create a new command context instance
     *
     * @param commandSender  Sender of the command
     * @param commandManager Command manager
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public CommandContext(final @NonNull C commandSender, final @NonNull CommandManager<C> commandManager) {
        this(false, commandSender, commandManager);
    }

    /**
     * Create a new command context instance
     *
     * @param suggestions    Whether the context is created for command suggestions
     * @param commandSender  Sender of the command
     * @param commandManager Command manager
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
        this.captionVariableReplacementHandler = commandManager.captionVariableReplacementHandler();
    }

    /**
     * Format a caption
     *
     * @param caption   Caption key
     * @param variables Replacements
     * @return Formatted message
     */
    public @NonNull String formatMessage(
            final @NonNull Caption caption,
            final @NonNull CaptionVariable... variables
    ) {
        return this.captionVariableReplacementHandler.replaceVariables(
                this.captionRegistry.getCaption(caption, this.commandSender),
                variables
        );
    }

    /**
     * Get the sender that executed the command
     *
     * @return Command sender
     */
    public @NonNull C getSender() {
        return this.commandSender;
    }

    /**
     * Check whether the sender that executed the command has a permission.
     *
     * @param permission The permission
     * @return Command sender
     * @since 1.6.0
     */
    @API(status = API.Status.STABLE, since = "1.6.0")
    public boolean hasPermission(final @NonNull CommandPermission permission) {
        return this.commandManager.hasPermission(this.commandSender, permission);
    }

    /**
     * Check whether the sender that executed the command has a permission.
     *
     * @param permission The permission
     * @return Command sender
     * @since 1.6.0
     */
    @API(status = API.Status.STABLE, since = "1.6.0")
    public boolean hasPermission(final @NonNull String permission) {
        return this.commandManager.hasPermission(this.commandSender, permission);
    }

    /**
     * Check if this context was created for tab completion purposes
     *
     * @return {@code true} if this context is requesting suggestions, else {@code false}
     */
    public boolean isSuggestions() {
        return this.suggestions;
    }

    /**
     * Store a value in the context map. This will overwrite any existing
     * value stored with the same key
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     */
    public <T extends @NonNull Object> void store(final @NonNull String key, final T value) {
        this.internalStorage.put(CloudKey.of(key), value);
    }

    /**
     * Store a value in the context map. This will overwrite any existing
     * value stored with the same key
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     */
    public <T extends @NonNull Object> void store(final @NonNull CloudKey<T> key, final T value) {
        this.internalStorage.put(key, value);
    }

    /**
     * Store a value in the context map. This will overwrite any existing
     * value stored with the same key
     *
     * @param keyHolder Holder of the identifying key
     * @param value     Value
     * @param <T>       Value type
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <T extends @NonNull Object> void store(final @NonNull CloudKeyHolder<T> keyHolder, final T value) {
        this.internalStorage.put(keyHolder.key(), value);
    }

    /**
     * Store or remove a value in the context map. This will overwrite any existing
     * value stored with the same key.
     * <p>
     * If the provided value is {@code null}, any current value stored for the provided key will be removed.
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public <T> void set(final @NonNull String key, final @Nullable T value) {
        if (value != null) {
            this.store(key, value);
        } else {
            this.remove(key);
        }
    }

    /**
     * Store or remove a value in the context map. This will overwrite any existing
     * value stored with the same key.
     * <p>
     * If the provided value is {@code null}, any current value stored for the provided key will be removed.
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <T> void set(final @NonNull CloudKey<T> key, final @Nullable T value) {
        if (value != null) {
            this.store(key, value);
        } else {
            this.remove(key);
        }
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
     * Remove a stored value from the context
     *
     * @param key Key to remove
     */
    public void remove(final @NonNull String key) {
        this.remove(CloudKey.of(key));
    }

    /**
     * Remove a stored value from the context
     *
     * @param key Key to remove
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public void remove(final @NonNull CloudKey<?> key) {
        this.internalStorage.remove(key);
    }

    /**
     * Get a value if it exists, else compute and store the value returned by the function and return it.
     *
     * @param key             Cloud key
     * @param defaultFunction Default value function
     * @param <T>             Value type
     * @return present or computed value
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public <T> T computeIfAbsent(
            final @NonNull CloudKey<T> key,
            final @NonNull Function<CloudKey<T>, T> defaultFunction
    ) {
        @SuppressWarnings("unchecked")
        final T castedValue = (T) this.internalStorage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<T>) k));
        return castedValue;
    }

    /**
     * Returns a copy of the raw input
     *
     * @return raw input
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandInput rawInput() {
        return this.getOrDefault("__raw_input__", CommandInput.empty()).copy();
    }

    /**
     * Get the raw input as a joined string
     *
     * @return {@link #rawInput()} joined with {@code " "} as the delimiter
     * @since 1.1.0
     */
    @API(status = API.Status.STABLE, since = "1.1.0")
    public @NonNull String getRawInputJoined() {
        return this.rawInput().remainingInput();
    }

    /**
     * Create a parsing context instance for the given component
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
     * Returns the context for the given component
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
     * Returns the context for the component at the given position
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
     * Return the context for the component with the given name.
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
     * Return an unmodifiable view of the stored parsing contexts
     *
     * @return the contexts
     * @since 2.0.0
     */
    @API(status = API.Status.MAINTAINED, since = "2.0.0")
    public @NonNull List<@NonNull ParsingContext<@NonNull C>> parsingContexts() {
        return Collections.unmodifiableList(this.parsingContexts);
    }

    /**
     * Get the associated {@link FlagContext} instance
     *
     * @return Flag context
     */
    public @NonNull FlagContext flags() {
        return this.flagContext;
    }

    /**
     * Returns the component that is currently being parsed for this command context.
     * This value will be updated whenever the context is used to provide new
     * suggestions or parse a new command argument
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
     * suggestions or parse a new command argument
     *
     * @param component the component that is currently being parsed, or {@code null}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public void currentComponent(final @Nullable CommandComponent<C> component) {
        this.currentComponent = component;
    }

    /**
     * Attempt to retrieve a value that has been registered to the associated command manager's
     * {@link cloud.commandframework.annotations.injection.ParameterInjectorRegistry}
     *
     * @param clazz Class of type to inject
     * @param <T>   Type to inject
     * @return Optional that may contain the created value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public <@NonNull T> @NonNull Optional<T> inject(final @NonNull Class<T> clazz) {
        if (this.commandManager == null) {
            throw new UnsupportedOperationException(
                    "Cannot retrieve injectable values from a command context that is not associated with a command manager"
            );
        }
        return this.commandManager.parameterInjectorRegistry().getInjectable(clazz, this, AnnotationAccessor.empty());
    }

    @Override
    public final @NonNull Map<@NonNull CloudKey<?>, @NonNull ?> all() {
        return Collections.unmodifiableMap(this.internalStorage);
    }
}
