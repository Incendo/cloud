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
package cloud.commandframework.arguments.parser;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.ImmutableImpl;
import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableImpl
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ParserDescriptor<C, T> {

    /**
     * Returns the parser.
     *
     * @return the parser
     */
    @NonNull ArgumentParser<C, T> parser();

    /**
     * Returns the type of values produced by the parser.
     *
     * @return the type of values produced by the parser
     */
    @NonNull TypeToken<T> valueType();

    /**
     * Create a descriptor for a {@link ArgumentParser#flatMap(MappedArgumentParser.Mapper) flatMapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> flatMap(
            final @NonNull TypeToken<O> mappedType,
            final MappedArgumentParser.@NonNull Mapper<C, T, O> mapper
    ) {
        return parserDescriptor(this.parser().flatMap(mapper), mappedType);
    }

    /**
     * Create a descriptor for a {@link ArgumentParser#flatMap(MappedArgumentParser.Mapper) flatMapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> flatMap(
            final @NonNull Class<O> mappedType,
            final MappedArgumentParser.@NonNull Mapper<C, T, O> mapper
    ) {
        return parserDescriptor(this.parser().flatMap(mapper), mappedType);
    }

    /**
     * Create a descriptor for a {@link ArgumentParser#flatMapSuccess(BiFunction) flatMapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> flatMapSuccess(
            final @NonNull TypeToken<O> mappedType,
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
    ) {
        return parserDescriptor(this.parser().flatMapSuccess(mapper), mappedType);
    }

    /**
     * Create a descriptor for a {@link ArgumentParser#flatMapSuccess(BiFunction) flatMapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> flatMapSuccess(
            final @NonNull Class<O> mappedType,
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
    ) {
        return parserDescriptor(this.parser().flatMapSuccess(mapper), mappedType);
    }

    /**
     * Create a descriptor for a {@link ArgumentParser#mapSuccess(BiFunction) mapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> mapSuccess(
            final @NonNull TypeToken<O> mappedType,
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper
    ) {
        return parserDescriptor(this.parser().mapSuccess(mapper), mappedType);
    }

    /**
     * Create a descriptor for a {@link ArgumentParser#mapSuccess(BiFunction) mapped} parser.
     *
     * @param mappedType mapped parser result type
     * @param mapper     mapper
     * @param <O>        mapped parser result type
     * @return mapped parser descriptor
     */
    default <O> @NonNull ParserDescriptor<C, O> mapSuccess(
            final @NonNull Class<O> mappedType,
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper
    ) {
        return parserDescriptor(this.parser().mapSuccess(mapper), mappedType);
    }

    /**
     * Creates a new parser descriptor.
     *
     * @param <C>       the command sender type
     * @param <T>       the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    static <C, T> @NonNull ParserDescriptor<C, T> of(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType
    ) {
        return ParserDescriptorImpl.of(parser, valueType);
    }

    /**
     * Creates a new parser descriptor.
     *
     * @param <C>       the command sender type
     * @param <T>       the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    static <C, T> @NonNull ParserDescriptor<C, T> of(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType
    ) {
        return ParserDescriptorImpl.of(parser, TypeToken.get(valueType));
    }

    /**
     * Creates a new parser descriptor.
     *
     * @param <C>       the command sender type
     * @param <T>       the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    static <C, T> @NonNull ParserDescriptor<C, T> parserDescriptor(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull TypeToken<T> valueType
    ) {
        return of(parser, valueType);
    }

    /**
     * Creates a new parser descriptor.
     *
     * @param <C>       the command sender type
     * @param <T>       the type of values produced by the parser
     * @param parser    the parser
     * @param valueType the type of values produced by the parser
     * @return the created descriptor
     */
    static <C, T> @NonNull ParserDescriptor<C, T> parserDescriptor(
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType
    ) {
        return of(parser, TypeToken.get(valueType));
    }
}
