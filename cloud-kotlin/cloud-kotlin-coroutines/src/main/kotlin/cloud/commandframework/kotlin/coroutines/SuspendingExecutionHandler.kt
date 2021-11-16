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
package cloud.commandframework.kotlin.coroutines

import cloud.commandframework.context.CommandContext
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.execution.CommandExecutionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Suspending version of [CommandExecutionHandler] for use with
 * coroutines.
 *
 * NOTE: It is highly advised to not use [CommandExecutionCoordinator.SimpleCoordinator] together
 * with coroutine support. Consider using [AsynchronousCommandExecutionCoordinator] instead.
 *
 * @param C command sender type
 */
public fun interface SuspendingExecutionHandler<C : Any> {
    /**
     * Handles command execution.
     *
     * @param commandContext command context
     */
    public suspend operator fun invoke(commandContext: CommandContext<C>)

    /**
     * Create a new [CommandExecutionHandler] for use in building commands,
     * backed by this [SuspendingExecutionHandler].
     *
     * @param scope coroutine scope
     * @param context coroutine context
     * @return new [CommandExecutionHandler]
     */
    public fun asCommandExecutionHandler(
        scope: CoroutineScope = GlobalScope,
        context: CoroutineContext = EmptyCoroutineContext,
    ): CommandExecutionHandler<C> = createCommandExecutionHandler(scope, context, this)

    public companion object {
        /**
         * Create a new [CommandExecutionHandler] for use in building commands,
         * backed by the given [SuspendingExecutionHandler].
         *
         * @param scope coroutine scope
         * @param context coroutine context
         * @param handler suspending handler
         * @return new [CommandExecutionHandler]
         */
        public fun <C : Any> createCommandExecutionHandler(
            scope: CoroutineScope = GlobalScope,
            context: CoroutineContext = EmptyCoroutineContext,
            handler: SuspendingExecutionHandler<C>,
        ): CommandExecutionHandler<C> = CommandExecutionHandler.FutureCommandExecutionHandler { ctx ->
            scope.future(context) {
                handler(ctx)
                null
            }
        }
    }
}
