//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.components.standard;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import com.intellectualsites.commands.components.parser.ComponentParser;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.parsing.NumberParseException;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Queue;

@SuppressWarnings("unused")
public class ByteComponent<C extends CommandSender> extends CommandComponent<C, Byte> {

    private final byte min;
    private final byte max;

    private ByteComponent(final boolean required, @Nonnull final String name, final byte min, final byte max) {
        super(required, name, new ByteParser<>(min, max));
        this.min = min;
        this.max = max;
    }

    @Nonnull
    public static <C extends CommandSender> Builder<C> newBuilder() {
        return new Builder<>();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Byte> required(@Nonnull final String name) {
        return ByteComponent.<C>newBuilder().named(name).asRequired().build();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Byte> optional(@Nonnull final String name) {
        return ByteComponent.<C>newBuilder().named(name).asOptional().build();
    }


    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, Byte> {

        private byte min = Byte.MIN_VALUE;
        private byte max = Byte.MAX_VALUE;

        @Nonnull
        public Builder<C> withMin(final byte min) {
            this.min = min;
            return this;
        }

        @Nonnull
        public Builder<C> withMax(final byte max) {
            this.max = max;
            return this;
        }

        @Nonnull
        @Override
        public ByteComponent<C> build() {
            return new ByteComponent<>(this.required, this.name, this.min, this.max);
        }

    }


    /**
     * Get the minimum accepted byteeger that could have been parsed
     *
     * @return Minimum byteeger
     */
    public byte getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted byteeger that could have been parsed
     *
     * @return Maximum byteeger
     */
    public byte getMax() {
        return this.max;
    }


    private static final class ByteParser<C extends CommandSender> implements ComponentParser<C, Byte> {

        private final byte min;
        private final byte max;

        public ByteParser(final byte min, final byte max) {
            this.min = min;
            this.max = max;
        }

        @Nonnull
        @Override
        public ComponentParseResult<Byte> parse(
                @Nonnull final CommandContext<C> commandContext,
                @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }
            try {
                final byte value = Byte.parseByte(input);
                if (value < this.min || value > this.max) {
                    return ComponentParseResult.failure(
                            new ByteParseException(input,
                                                   this.min,
                                                   this.max));
                }
                return ComponentParseResult.success(value);
            } catch (final Exception e) {
                return ComponentParseResult.failure(
                        new ByteParseException(input, this.min,
                                               this.max));
            }
        }

    }


    public static final class ByteParseException extends NumberParseException {

        public ByteParseException(@Nonnull final String input, final byte min, final byte max) {
            super(input, min, max);
        }

        @Override
        public boolean hasMin() {
            return this.getMin().byteValue() == Byte.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().byteValue() == Byte.MAX_VALUE;
        }

        @Override
        public String getNumberType() {
            return "byte";
        }

    }

}
