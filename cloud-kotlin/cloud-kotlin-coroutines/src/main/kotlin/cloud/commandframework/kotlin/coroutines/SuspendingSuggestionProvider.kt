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
package cloud.commandframework.kotlin.coroutines

import cloud.commandframework.arguments.suggestion.Suggestion
import cloud.commandframework.arguments.suggestion.SuggestionProvider
import cloud.commandframework.context.CommandContext
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.execution.CommandExecutionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Suspending version of [SuggestionProvider] for use with coroutines.
 *
 * NOTE: It is highly advised to not use [CommandExecutionCoordinator.SimpleCoordinator] together
 * with coroutine support. Consider using [AsynchronousCommandExecutionCoordinator] instead.
 *
 * @param C command sender type.
 */
public fun interface SuspendingSuggestionProvider<C : Any> {
    /**
     * Returns the suggestions for the given [input].
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    public suspend operator fun invoke(context: CommandContext<C>, input: String): Iterable<Suggestion>

    /**
     * Creates a new [SuggestionProvider] backed by this [SuspendingExecutionHandler].
     *
     * @param scope coroutine scope
     * @param context coroutine context
     * @return new [SuggestionProvider]
     */
    public fun asSuggestionProvider(
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext
    ): SuggestionProvider<C> = createSuggestionProvider(scope, context, this)

    public companion object {
        /**
         * Creates a new [SuggestionProvider] backed by the given [SuspendingSuggestionProvider].
         *
         * @param scope coroutine scope
         * @param context coroutine context
         * @param provider suspending provider
         * @return new [SuggestionProvider]
         */
        public fun <C : Any> createSuggestionProvider(
            scope: CoroutineScope = GlobalScope,
            context: CoroutineContext = EmptyCoroutineContext,
            provider: SuspendingSuggestionProvider<C>
        ): SuggestionProvider<C> = SuggestionProvider { ctx, input ->
            scope.future(context) {
                provider(ctx, input).toList()
            }
        }
    }
}

/**
 * Creates a suspending suggestion provider backed by the given [provider].
 *
 * @param scope coroutine scope
 * @param context coroutine context
 * @return the provider
 */
public suspend inline fun <C : Any> suspendingSuggestionProvider(
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline provider: suspend (CommandContext<C>, String) -> Iterable<Suggestion>
): SuggestionProvider<C> = SuspendingSuggestionProvider<C> { commandContext, input ->
    provider(commandContext, input)
}.asSuggestionProvider(scope, context)
