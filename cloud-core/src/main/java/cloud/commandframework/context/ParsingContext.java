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

import cloud.commandframework.component.CommandComponent;
import java.time.Duration;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.MAINTAINED, since = "2.0.0")
public final class ParsingContext<C> {

    private final CommandComponent<@NonNull C> component;
    private @Nullable String consumed = null;
    private long startTime = -1;
    private long endTime = -1;
    private int consumedFrom = -1;
    private int consumedTo = -1;
    private boolean success;

    /**
     * Construct an ParsingContext object with the given argument.
     *
     * @param component the command component to be assigned to the ParsingContext
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public ParsingContext(final @NonNull CommandComponent<@NonNull C> component) {
        this.component = component;
    }

    /**
     * Return the associated component.
     *
     * @return the component
     */
    public @NonNull CommandComponent<C> component() {
        return this.component;
    }

    /**
     * Return the duration taken to parse the component.
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
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public void markStart() {
        this.startTime = System.nanoTime();
    }

    /**
     * Set the end time.
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
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
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public void success(final boolean success) {
        this.success = success;
    }

    /**
     * Store information about consumed input post-parsing.
     *
     * @param original pre-parse input
     * @param postParse post-parse input
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public void consumedInput(final @NonNull CommandInput original, final @NonNull CommandInput postParse) {
        if (this.consumed != null) {
            throw new IllegalStateException();
        }
        this.consumed = original.difference(postParse);
        this.consumedFrom = original.cursor();
        this.consumedTo = original.cursor() + this.consumed.length();
    }

    /**
     * Returns the consumed input.
     *
     * @return the consumed input
     */
    @API(status = API.Status.STABLE)
    public @NonNull String consumedInput() {
        return Objects.requireNonNull(this.consumed);
    }

    /**
     * Return the exact alias used, if the argument was static. If no alias was consumed
     * then {@code null} is returned.
     *
     * @return the exact alias, or {@code null}
     */
    public @Nullable String exactAlias() {
        if (!this.success || this.component.type() != CommandComponent.ComponentType.LITERAL) {
            return null;
        }
        return this.consumed;
    }

    /**
     * Index the parser consumed from.
     *
     * @return consumed from index
     */
    @API(status = API.Status.STABLE)
    public int consumedFrom() {
        return this.consumedFrom;
    }

    /**
     * Index the parser consumed to.
     *
     * @return consumed to index
     */
    @API(status = API.Status.STABLE)
    public int consumedTo() {
        return this.consumedTo;
    }
}
