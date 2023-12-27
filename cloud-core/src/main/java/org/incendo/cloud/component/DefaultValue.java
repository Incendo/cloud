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
package org.incendo.cloud.component;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;

/**
 * Default value used when an optional argument is omitted by the command sender.
 *
 * @param <C> command sender type
 * @param <T> type of the owning argument
 */
@API(status = API.Status.STABLE)
@FunctionalInterface
public interface DefaultValue<C, T> {

    /**
     * Returns a constant default value. The argument parser will be bypassed when using a constant default value.
     *
     * @param value the default value
     * @return the default value instance
     * @param <C> the command sender type
     * @param <T> the argument type
     */
    static <C, T> @NonNull DefaultValue<C, T> constant(final @NonNull T value) {
        return new ConstantDefaultValue<>(value);
    }

    /**
     * Returns a default value that will be evaluated when the command is evaluated. The argument parser will be
     * bypassed when using a dynamic default value.
     *
     * @param expression the expression producing the default value
     * @return the default value instance
     * @param <C> the command sender type
     * @param <T> the argument type
     */
    static <C, T> @NonNull DefaultValue<C, T> dynamic(final @NonNull DefaultValue<C, T> expression) {
        return new DynamicDefaultValue<>(expression);
    }

    /**
     * Returns a default value that will be parsed together with the command.
     *
     * @param value value that gets parsed by the argument parser when the command is being evaluated
     * @return the default value instance
     * @param <C> the command sender type
     * @param <T> the argument type
     */
    static <C, T> @NonNull DefaultValue<C, T> parsed(final @NonNull String value) {
        return new ParsedDefaultValue<>(value);
    }

    /**
     * Evaluates the default value for the given {@code context}.
     *
     * @param context the context
     * @return the default value
     */
    @NonNull ArgumentParseResult<T> evaluateDefault(@NonNull CommandContext<C> context);


    final class ConstantDefaultValue<C, T> implements DefaultValue<C, T> {

        private final ArgumentParseResult<T> value;

        private ConstantDefaultValue(final @NonNull T value) {
            this.value = ArgumentParseResult.success(value);
        }

        @Override
        public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
            return this.value;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            final ConstantDefaultValue<?, ?> that = (ConstantDefaultValue<?, ?>) object;
            return Objects.equals(this.value.parsedValue().get(), that.value.parsedValue().get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }
    }

    final class DynamicDefaultValue<C, T> implements DefaultValue<C, T> {

        private final DefaultValue<C, T> defaultValue;

        private DynamicDefaultValue(final @NonNull DefaultValue<C, T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
            return this.defaultValue.evaluateDefault(context);
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            final DynamicDefaultValue<?, ?> that = (DynamicDefaultValue<?, ?>) object;
            return Objects.equals(this.defaultValue, that.defaultValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.defaultValue);
        }
    }

    final class ParsedDefaultValue<C, T> implements DefaultValue<C, T> {

        private final String value;

        private ParsedDefaultValue(final @NonNull String string) {
            this.value = string;
        }

        @Override
        public @NonNull ArgumentParseResult<T> evaluateDefault(final @NonNull CommandContext<C> context) {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the string that should be parsed.
         *
         * @return the string to be parsed
         */
        public @NonNull String value() {
            return this.value;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            final ParsedDefaultValue<?, ?> that = (ParsedDefaultValue<?, ?>) object;
            return Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }
    }
}
