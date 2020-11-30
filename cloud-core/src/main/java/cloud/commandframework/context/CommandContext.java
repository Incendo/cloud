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
package cloud.commandframework.context;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.FlagContext;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.CaptionVariableReplacementHandler;
import cloud.commandframework.captions.SimpleCaptionVariableReplacementHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Command context used to assist in the parsing of commands
 *
 * @param <C> Command sender type
 */
public final class CommandContext<C> {

    private final CaptionVariableReplacementHandler captionVariableReplacementHandler =
            new SimpleCaptionVariableReplacementHandler();
    private final Map<CommandArgument<C, ?>, ArgumentTiming> argumentTimings = new HashMap<>();
    private final FlagContext flagContext = FlagContext.create();
    private final Map<String, Object> internalStorage = new HashMap<>();
    private final C commandSender;
    private final boolean suggestions;
    private final CaptionRegistry<C> captionRegistry;

    private CommandArgument<C, ?> currentArgument = null;

    /**
     * Create a new command context instance
     *
     * @param commandSender   Sender of the command
     * @param captionRegistry Caption registry
     */
    public CommandContext(final @NonNull C commandSender, final @NonNull CaptionRegistry<C> captionRegistry) {
        this(false, commandSender, captionRegistry);
    }

    /**
     * Create a new command context instance
     *
     * @param suggestions     Whether or not the context is created for command suggestions
     * @param commandSender   Sender of the command
     * @param captionRegistry Caption registry
     */
    public CommandContext(
            final boolean suggestions,
            final @NonNull C commandSender,
            final @NonNull CaptionRegistry<C> captionRegistry
    ) {
        this.commandSender = commandSender;
        this.suggestions = suggestions;
        this.captionRegistry = captionRegistry;
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
    public <T> void store(final @NonNull String key, final @NonNull T value) {
        this.internalStorage.put(key, value);
    }

    /**
     * Get a value from its key. Will return {@link Optional#empty()}
     * if no value is stored with the given key
     *
     * @param key Key
     * @param <T> Value type
     * @return Value
     */
    public <T> @NonNull Optional<T> getOptional(final @NonNull String key) {
        final Object value = this.internalStorage.get(key);
        if (value != null) {
            @SuppressWarnings("unchecked") final T castedValue = (T) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get a value from its key. Will return {@link Optional#empty()}
     * if no value is stored with the given key
     *
     * @param argument Argument
     * @param <T>      Value type
     * @return Value
     */
    @SuppressWarnings("unused")
    public <T> @NonNull Optional<T> getOptional(final @NonNull CommandArgument<C, T> argument) {
        final Object value = this.internalStorage.get(argument.getName());
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
        this.internalStorage.remove(key);
    }

    /**
     * Get a required argument from the context. This will thrown an exception
     * if there's no value associated with the given key
     *
     * @param key Argument key
     * @param <T> Argument type
     * @return Argument
     * @throws NullPointerException If no such argument is stored
     */
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public <T> @NonNull T get(final @NonNull String key) {
        final Object value = this.internalStorage.get(key);
        if (value == null) {
            throw new NullPointerException("No such object stored in the context: " + key);
        }
        return (T) value;
    }

    /**
     * Get a required argument from the context. This will thrown an exception
     * if there's no value associated with the given argument
     *
     * @param argument The argument
     * @param <T>      Argument type
     * @return Stored value
     * @throws NullPointerException If no such value is stored
     */
    public <T> @NonNull T get(final @NonNull CommandArgument<C, T> argument) {
        return this.get(argument.getName());
    }

    /**
     * Get a value if it exists, else return the provided default value
     *
     * @param argument     Argument
     * @param defaultValue Default value
     * @param <T>          Argument type
     * @return Stored value, or supplied default value
     */
    public <T> @Nullable T getOrDefault(
            final @NonNull CommandArgument<C, T> argument,
            final @Nullable T defaultValue
    ) {
        return this.<T>getOptional(argument.getName()).orElse(defaultValue);
    }

    /**
     * Get a value if it exists, else return the provided default value
     *
     * @param key          Argument key
     * @param defaultValue Default value
     * @param <T>          Argument type
     * @return Argument, or supplied default value
     */
    public <T> @Nullable T getOrDefault(
            final @NonNull String key,
            final @Nullable T defaultValue
    ) {
        return this.<T>getOptional(key).orElse(defaultValue);
    }

    /**
     * Get a value if it exists, else return the value supplied by the given supplier
     *
     * @param key             Argument key
     * @param defaultSupplier Supplier of default value
     * @param <T>             Argument type
     * @return Argument, or supplied default value
     * @since 1.2.0
     */
    public <T> @Nullable T getOrSupplyDefault(
            final @NonNull String key,
            final @NonNull Supplier<@Nullable T> defaultSupplier
    ) {
        return this.<T>getOptional(key).orElseGet(defaultSupplier);
    }

    /**
     * Get the raw input. This should only be used when {@link #isSuggestions()} is {@code true}
     *
     * @return Raw input in token form
     */
    public @NonNull LinkedList<@NonNull String> getRawInput() {
        return this.getOrDefault("__raw_input__", new LinkedList<>());
    }

    /**
     * Get the raw input as a joined string
     *
     * @return {@link #getRawInput()} joined with {@code " "} as the delimiter
     * @since 1.1.0
     */
    public @NonNull String getRawInputJoined() {
        return String.join(" ", this.getRawInput());
    }

    /**
     * Create an argument timing for a specific argument
     *
     * @param argument Argument
     * @return Created timing instance
     */
    public @NonNull ArgumentTiming createTiming(final @NonNull CommandArgument<C, ?> argument) {
        final ArgumentTiming argumentTiming = new ArgumentTiming();
        this.argumentTimings.put(argument, argumentTiming);
        return argumentTiming;
    }

    /**
     * Get an immutable view of the argument timings map
     *
     * @return Argument timings
     */
    public @NonNull Map<CommandArgument<@NonNull C, @NonNull ?>, ArgumentTiming> getArgumentTimings() {
        return Collections.unmodifiableMap(this.argumentTimings);
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
     * Get the argument that is currently being parsed for this command context.
     * This value will be updated whenever the context is used to provide new
     * suggestions or parse a new command argument
     *
     * @return Currently parsing {@link CommandArgument} or {@code null}
     * @since 1.2.0
     */
    public @Nullable CommandArgument<C, ?> getCurrentArgument() {
        return this.currentArgument;
    }

    /**
     * Set the argument that is currently being parsed for this command context.
     * This value should be updated whenever the context is used to provide new
     * suggestions or parse a new command argument
     *
     * @param argument Currently parsing {@link CommandArgument} or {@code null}
     * @since 1.2.0
     */
    public void setCurrentArgument(final @Nullable CommandArgument<C, ?> argument) {
        this.currentArgument = argument;
    }


    /**
     * Used to track performance metrics related to command parsing. This is attached
     * to the command context, as this depends on the command context that is being
     * parsed.
     * <p>
     * The times are measured in nanoseconds.
     */
    public static final class ArgumentTiming {

        private long start;
        private long end;
        private boolean success;

        /**
         * Created a new argument timing instance
         *
         * @param start   Start time (in nanoseconds)
         * @param end     End time (in nanoseconds)
         * @param success Whether or not the argument was parsed successfully
         */
        public ArgumentTiming(final long start, final long end, final boolean success) {
            this.start = start;
            this.end = end;
            this.success = success;
        }

        /**
         * Created a new argument timing instance without an end time
         *
         * @param start Start time (in nanoseconds)
         */
        @SuppressWarnings("unused")
        public ArgumentTiming(final long start) {
            this(start, -1, false);
        }

        /**
         * Created a new argument timing instance
         */
        public ArgumentTiming() {
            this(-1, -1, false);
        }

        /**
         * Get the elapsed time
         *
         * @return Elapsed time (in nanoseconds)
         */
        public long getElapsedTime() {
            if (this.end == -1) {
                throw new IllegalStateException("No end time has been registered");
            } else if (this.start == -1) {
                throw new IllegalStateException("No start time has been registered");
            }
            return this.end - this.start;
        }

        /**
         * Set the end time
         *
         * @param end     End time (in nanoseconds)
         * @param success Whether or not the argument was parsed successfully
         */
        public void setEnd(final long end, final boolean success) {
            this.end = end;
            this.success = success;
        }

        /**
         * Set the start time
         *
         * @param start Start time (in nanoseconds)
         */
        public void setStart(final long start) {
            this.start = start;
        }

        /**
         * Check whether or not the value was parsed successfully
         *
         * @return {@code true} if the value was parsed successfully, {@code false} if not
         */
        public boolean wasSuccess() {
            return this.success;
        }

    }

}
