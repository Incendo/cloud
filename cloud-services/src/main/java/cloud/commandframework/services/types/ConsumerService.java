//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.services.types;

import cloud.commandframework.services.State;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

/**
 * Service type where each implementation gets a chance to consume the context. This service type
 * effectively turns the pipeline into an event bus.
 * <p>
 * A service can forcefully terminate execution by calling {@link #interrupt()}
 *
 * @param <Context> Context
 */
@FunctionalInterface
public interface ConsumerService<Context>
        extends SideEffectService<Context>, Consumer<Context> {

    /**
     * Immediately terminate the execution and return {@link State#ACCEPTED}
     *
     * @throws PipeBurst Pipe burst
     */
    @SuppressWarnings("DoNotCallSuggester")
    static void interrupt() throws PipeBurst {
        throw new PipeBurst();
    }

    @Override
    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    default @NonNull State handle(final @NonNull Context context) {
        try {
            this.accept(context);
        } catch (final PipeBurst burst) {
            return State.ACCEPTED;
        }
        return State.REJECTED;
    }

    /**
     * Accept the context. Call {@link #interrupt()} to interrupt the entire pipeline and immediately
     * return {@link State#ACCEPTED} to the sink
     *
     * @param context Context to consume
     */
    @Override
    void accept(@NonNull Context context);


    class PipeBurst extends RuntimeException {

        private static final long serialVersionUID = -1143137258194595985L;

        private PipeBurst() {
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        @Override
        public synchronized Throwable initCause(final Throwable cause) {
            return this;
        }

    }

}
