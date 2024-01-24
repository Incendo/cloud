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
package cloud.commandframework.kotlin.extension

import cloud.commandframework.exception.handling.ExceptionController
import cloud.commandframework.exception.handling.ExceptionHandler
import cloud.commandframework.exception.handling.ExceptionHandlerRegistration
import org.checkerframework.checker.units.qual.C
import org.checkerframework.common.returnsreceiver.qual.This

/**
 * Decorates a registration builder using the given [decorator] and registers the result.
 *
 * The ordering matters when multiple handlers are registered tor the same exception type.
 * The last registered handler will get priority.
 *
 * @return [this] exception controller
 */
public inline fun <reified T : Throwable> @This ExceptionController<C>.register(
    decorator: ExceptionHandlerRegistration.BuilderDecorator<C, T>
): ExceptionController<C> = this.register(T::class.java, decorator)

/**
 * Registers the given [handler].
 *
 * The ordering matters when multiple handlers are registered tor the same exception type.
 * The last registered handler will get priority.
 *
 * @return [this] exception controller
 */
public inline fun <reified T : Throwable> @This ExceptionController<C>.registerHandler(
    handler: ExceptionHandler<C, T>
): ExceptionController<C> = this.registerHandler(T::class.java, handler)
