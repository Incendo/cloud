package cloud.commandframework.kotlin.coroutines

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.MethodCommandExecutionHandler
import cloud.commandframework.context.CommandContext
import cloud.commandframework.execution.CommandExecutionCoordinator
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture

/** Adds coroutine support to the [AnnotationParser]. */
public fun <C> AnnotationParser<C>.installCoroutineSupport(
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext
) {
    if (manager().commandExecutionCoordinator() is CommandExecutionCoordinator.SimpleCoordinator) {
        RuntimeException(
                """You are highly advised to not use the simple command execution coordinator together
                            with coroutine support. Consider using the asynchronous command execution coordinator instead.""")
            .printStackTrace()
    }

    val predicate = Predicate<Method> { it.kotlinFunction?.isSuspend == true }
    registerCommandExecutionMethodFactory(predicate) {
        KotlinMethodCommandExecutionHandler(scope, context, it)
    }
}

private class KotlinMethodCommandExecutionHandler<C>(
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext,
    context: CommandMethodContext<C>
) : MethodCommandExecutionHandler<C>(context) {

    override fun executeFuture(commandContext: CommandContext<C>): CompletableFuture<Any?> {
        val instance = context().instance()
        val params = createParameterValues(commandContext, commandContext.flags(), false)
        // We need to propagate exceptions to the caller.
        return coroutineScope
            .async(this@KotlinMethodCommandExecutionHandler.coroutineContext) {
                context().method().kotlinFunction?.callSuspend(instance, *params.toTypedArray())
            }
            .asCompletableFuture()
    }
}
