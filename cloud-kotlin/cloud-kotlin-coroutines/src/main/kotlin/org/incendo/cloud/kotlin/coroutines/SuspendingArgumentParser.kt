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
package org.incendo.cloud.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.incendo.cloud.CommandManager
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.SuggestionFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Suspending version of [ArgumentParser] for use with coroutines.
 *
 * NOTE: It is highly advised to not use [ExecutionCoordinator.simpleCoordinator] together
 * with coroutine support. Consider using [ExecutionCoordinator.asyncCoordinator] instead.
 *
 * @param C command sender type.
 */
public fun interface SuspendingArgumentParser<C : Any, T : Any> {
    /**
     * Returns the result of parsing the given [commandInput].
     *
     * This method may be called when a command chain is being parsed for execution
     * (using [CommandManager.executeCommand])
     * or when a command is being parsed to provide context for suggestions
     * (using [SuggestionFactory.suggest]).
     * It is possible to use [CommandContext.isSuggestions] to see what the purpose of the
     * parsing is. Particular care should be taken when parsing for suggestions, as the parsing
     * method is then likely to be called once for every character written by the command sender.
     *
     * The parser is assumed to be completely stateless and should not store any information about
     * the command sender or the command context. Instead, information should be stored in the
     * [CommandContext].
     *
     * @param commandContext Command context
     * @param commandInput   Command Input
     * @return the result
     */
    public suspend operator fun invoke(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<T>

    /**
     * Creates a new [ArgumentParser] backed by this [SuspendingArgumentParser].
     *
     * @param scope coroutine scope
     * @param context coroutine context
     * @return new [ArgumentParser]
     */
    public fun asArgumentParser(
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext
    ): ArgumentParser<C, T> = createArgumentParser(scope, context, this)

    public companion object {
        /**
         * Creates a new [ArgumentParser] backed by the given [SuspendingArgumentParser].
         *
         * @param scope coroutine scope
         * @param context coroutine context
         * @param parser suspending parser
         * @return new [ArgumentParser]
         */
        public fun <C : Any, T : Any> createArgumentParser(
            scope: CoroutineScope = GlobalScope,
            context: CoroutineContext = EmptyCoroutineContext,
            parser: SuspendingArgumentParser<C, T>
        ): ArgumentParser<C, T> = ArgumentParser.FutureArgumentParser { ctx, commandInput ->
            scope.future(context) {
                parser(ctx, commandInput)
            }
        }
    }
}

/**
 * Creates a new [ParserDescriptor] backed by this [SuspendingArgumentParser].
 *
 * @param scope coroutine scope
 * @param context coroutine context
 * @return the descriptor
 */
public inline fun <C : Any, reified T : Any> SuspendingArgumentParser<C, T>.asParserDescriptor(
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext
): ParserDescriptor<C, T> = ParserDescriptor.of(this.asArgumentParser(scope, context), T::class.java)

/**
 * Creates a suspending argument parser backed by the given [parser].
 *
 * @param scope coroutine scope
 * @param context coroutine context
 * @return the descriptor
 */
public suspend inline fun <C : Any, reified T : Any> suspendingArgumentParser(
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline parser: suspend (CommandContext<C>, CommandInput) -> ArgumentParseResult<T>
): ParserDescriptor<C, T> = SuspendingArgumentParser<C, T> { commandContext, commandInput ->
    parser(commandContext, commandInput)
}.asParserDescriptor(scope, context)
