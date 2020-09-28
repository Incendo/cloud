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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Command context used to assist in the parsing of commands
 *
 * @param <C> Command sender type
 */
public final class CommandContext<C> {

    private final Map<CommandArgument<C, ?>, ArgumentTiming> argumentTimings = new HashMap<>();
    private final Map<String, Object> internalStorage = new HashMap<>();
    private final C commandSender;
    private final boolean suggestions;

    /**
     * Create a new command context instance
     *
     * @param commandSender Sender of the command
     */
    public CommandContext(@NonNull final C commandSender) {
        this(false, commandSender);
    }

    /**
     * Create a new command context instance
     *
     * @param suggestions   Whether or not the context is created for command suggestions
     * @param commandSender Sender of the command
     */
    public CommandContext(final boolean suggestions,
                          @NonNull final C commandSender) {
        this.commandSender = commandSender;
        this.suggestions = suggestions;
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
     * Store a value in the context map
     *
     * @param key   Key
     * @param value Value
     * @param <T>   Value type
     */
    public <T> void store(@NonNull final String key, @NonNull final T value) {
        this.internalStorage.put(key, value);
    }

    /**
     * Get a value from its key
     *
     * @param key Key
     * @param <T> Value type
     * @return Value
     */
    public <T> @NonNull Optional<T> get(@NonNull final String key) {
        final Object value = this.internalStorage.get(key);
        if (value != null) {
            @SuppressWarnings("ALL") final T castedValue = (T) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get a required argument from the context
     *
     * @param key Argument key
     * @param <T> Argument type
     * @return Argument
     * @throws NullPointerException If no such argument is stored
     */
    @SuppressWarnings("unchecked")
    public <T> @NonNull T getRequired(@NonNull final String key) {
        final Object value = this.internalStorage.get(key);
        if (value == null) {
            throw new NullPointerException("No such object stored in the context: " + key);
        }
        return (T) value;
    }

    /**
     * Get a value if it exists, else return the provided default value
     *
     * @param key          Argument key
     * @param defaultValue Default value
     * @param <T>          Argument type
     * @return Argument, or supplied default value
     */
    public <T> @NonNull T getOrDefault(@NonNull final String key,
                                       @NonNull final T defaultValue) {
        return this.<T>get(key).orElse(defaultValue);
    }

    /**
     * Create an argument timing for a specific argument
     *
     * @param argument Argument
     * @return Created timing instance
     */
    public @NonNull ArgumentTiming createTiming(@NonNull final CommandArgument<C, ?> argument) {
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
         * @param end End time (in nanoseconds)
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
