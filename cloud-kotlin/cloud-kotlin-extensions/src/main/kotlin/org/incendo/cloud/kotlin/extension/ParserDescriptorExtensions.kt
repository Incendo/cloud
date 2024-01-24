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
package org.incendo.cloud.kotlin.extension

import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.MappedArgumentParser.Mapper
import org.incendo.cloud.parser.ParserDescriptor
import java.util.concurrent.CompletableFuture

/**
 * Returns a [ParserDescriptor] that describes [this] parser.
 *
 * @return the descriptor
 */
public inline fun <C, reified T> ArgumentParser<C, T>.asDescriptor(): ParserDescriptor<C, T> =
    ParserDescriptor.of(this, T::class.java)

/**
 * Returns a [ParserDescriptor] that describes the [parser].
 *
 * @return the descriptor
 */
public inline fun <C, reified T> parserDescriptor(parser: ArgumentParser<C, T>): ParserDescriptor<C, T> =
    parser.asDescriptor()

/**
 * Creates a descriptor for a flat-mapped parser.
 */
public inline fun <C, T, reified O> ParserDescriptor<C, T>.flatMap(mapper: Mapper<C, T, O>): ParserDescriptor<C, O> =
    flatMap(O::class.java, mapper)

/**
 * Creates a descriptor for a flat-mapped parser.
 */
public inline fun <C, T, reified O> ParserDescriptor<C, T>.flatMapSuccess(
    noinline mapper: (CommandContext<C>, T) -> CompletableFuture<ArgumentParseResult<O>>
): ParserDescriptor<C, O> = flatMapSuccess(O::class.java, mapper)

/**
 * Creates a descriptor for a mapped parser.
 */
public inline fun <C, T, reified O> ParserDescriptor<C, T>.mapSuccess(
    noinline mapper: (CommandContext<C>, T) -> CompletableFuture<O>
): ParserDescriptor<C, O> = mapSuccess(O::class.java, mapper)
