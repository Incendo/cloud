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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.MAINTAINED, since = "1.9.0")
public final class ArgumentContext<C, T> {

    private final CommandArgument<@NonNull C, @NonNull T> argument;
    private final List<String> consumedInput = new LinkedList<>();

    /**
     * Construct an ArgumentContext object with the given argument.
     *
     * @param argument the command argument to be assigned to the ArgumentContext
     */
    public ArgumentContext(final @NonNull CommandArgument<@NonNull C, @NonNull T> argument) {
        this.argument = argument;
    }

    private long startTime = -1;
    private long endTime = -1;

    private boolean success;

    /**
     * Return the associated argument.
     *
     * @return the argument
     */
    public @NonNull CommandArgument<@NonNull C, @NonNull T> argument() {
        return this.argument;
    }

    /**
     * Return the duration taken to parse the argument.
     *
     * @return the argument parse duration
     */
    public @NonNull Duration parseDuration() {
        if (this.startTime < 0) {
            throw new IllegalStateException("No start time has been registered");
        } else if (this.endTime < 0) {
            throw new IllegalStateException("No end time has been registered");
        }
        return Duration.ofNanos(this.endTime - this.startTime);
    }

    /**
     * Set the start time.
     */
    public void markStart() {
        this.startTime = System.nanoTime();
    }

    /**
     * Set the end time.
     */
    public void markEnd() {
        this.endTime = System.nanoTime();
    }

    long startTime() {
        return this.startTime;
    }

    long endTime() {
        return this.endTime;
    }

    /**
     * Return whether the argument was parsed successfully.
     *
     * @return {@code true} if the value was parsed successfully, {@code false} if not
     */
    public boolean success() {
        return this.success;
    }

    /**
     * Set whether the argument was parsed successfully.
     *
     * @param success {@code true} if the value was parsed successfully, {@code false} if not
     */
    public void success(final boolean success) {
        this.success = success;
    }

    /**
     * Add the given input to the list of consumed input.
     *
     * @param consumedInput the consumed input
     */
    public void consumedInput(final @NonNull List<@NonNull String> consumedInput) {
        this.consumedInput.addAll(consumedInput);
    }

    /**
     * Return the list of consumed input.
     *
     * @return the list of consumed input
     */
    public @NonNull List<@NonNull String> consumedInput() {
        return Collections.unmodifiableList(this.consumedInput);
    }

    /**
     * Return the exact alias used, if the argument was static. If no alias was consumed
     * then {@code null} is returned.
     *
     * @return the exact alias, or {@code null}
     */
    public @Nullable String exactAlias() {
        if (!this.success || !(this.argument instanceof StaticArgument)) {
            return null;
        }
        return this.consumedInput.get(0);
    }
}
