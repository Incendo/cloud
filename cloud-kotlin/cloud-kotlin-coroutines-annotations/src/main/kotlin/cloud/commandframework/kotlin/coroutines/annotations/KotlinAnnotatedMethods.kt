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
package cloud.commandframework.kotlin.coroutines.annotations

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.MethodCommandExecutionHandler
import cloud.commandframework.annotations.injection.ParameterInjectorRegistry
import cloud.commandframework.annotations.method.ParameterValue
import cloud.commandframework.annotations.suggestions.MethodSuggestionProvider
import cloud.commandframework.annotations.suggestions.SuggestionProviderFactory
import cloud.commandframework.arguments.suggestion.Suggestion
import cloud.commandframework.arguments.suggestion.SuggestionProvider
import cloud.commandframework.context.CommandContext
import cloud.commandframework.context.CommandInput
import io.leangen.geantyref.GenericTypeReflector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * Adds coroutine support to the [AnnotationParser].
 *
 * @param scope coroutine scope
 * @param context coroutine context
 * @param onlyForSuspending whether the Kotlin execution handler should only be used for suspending functions
 * @return annotation parser
 * @since 1.6.0
 */
public fun <C> AnnotationParser<C>.installCoroutineSupport(
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = EmptyCoroutineContext,
    onlyForSuspending: Boolean = false
): AnnotationParser<C> {
    val predicate = Predicate<Method> { method ->
        if (onlyForSuspending) {
            method.kotlinFunction?.isSuspend == true
        } else {
            method.kotlinFunction != null
        }
    }
    registerCommandExecutionMethodFactory(predicate) {
        KotlinMethodCommandExecutionHandler(scope, context, it)
    }

    suggestionProviderFactory(KotlinSuggestionProviderFactory(scope, context))

    return this
}

private class KotlinMethodCommandExecutionHandler<C>(
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext,
    context: CommandMethodContext<C>
) : MethodCommandExecutionHandler<C>(context) {

    private val paramsWithoutContinuation = parameters().filterNot { Continuation::class.java == it.type }.toTypedArray()

    override fun executeFuture(commandContext: CommandContext<C>): CompletableFuture<Void?> {
        val instance = context().instance()

        val kFunction = requireNotNull(context().method().kotlinFunction)
        val valueParameters = kFunction.valueParameters.associateBy(KParameter::name)

        // If there are no optional parameters then we pass by position. This means that everything will work just fine
        // even if the parameter names are scrambled by Java.
        if (valueParameters.values.none(KParameter::isOptional)) {
            val params = createParameterValues(
                commandContext,
                paramsWithoutContinuation
            ).map(ParameterValue::value)
            return coroutineScope.future(this@KotlinMethodCommandExecutionHandler.coroutineContext) {
                try {
                    kFunction.callSuspend(instance, *params.toTypedArray())
                } catch (e: InvocationTargetException) { // unwrap invocation exception
                    e.cause?.let { throw it } ?: throw e // if cause exists, throw, else rethrow invocation exception
                }
                null
            }
        }

        val instanceParameter = kFunction.instanceParameter?.let { mapOf(it to instance) } ?: emptyMap()

        // We then get the parameter values and try to match them up with the KParameter values. If Java is set to compile
        // with the parameter names intact, then the first if statement will catch it and everything will be easy. However,
        // if this is not the case then we try to match up the parameters by comparing the argument/flag names, and lastly
        // by comparing the types of the parameters.
        val params = createParameterValues(
            commandContext,
            paramsWithoutContinuation
        ).associate { parameterValue ->
            val descriptor = parameterValue.descriptor()
            val parameter: KParameter = if (parameterValue.parameter().name in valueParameters) {
                requireNotNull(valueParameters[parameterValue.parameter().name])
            } else if (descriptor != null && descriptor.name() in valueParameters) {
                requireNotNull(valueParameters[descriptor.name()])
            } else {
                requireNotNull(valueParameters.values.firstOrNull { kParam -> kParam.hasType(parameterValue.parameter().type) }) {
                    "could not find parameter ${parameterValue.parameter().name} of type ${parameterValue.parameter().type}, " +
                        "kParameters: ${valueParameters.entries.joinToString { "${it.key} (${it.value.type.javaType})" } }"
                }
            }
            parameter to parameterValue.value()
        }.filter { (parameter, value) ->
            value != null || (!parameter.isOptional)
        } + instanceParameter

        // We need to propagate exceptions to the caller.
        return coroutineScope.future(this@KotlinMethodCommandExecutionHandler.coroutineContext) {
            try {
                kFunction.callSuspendBy(params)
            } catch (e: InvocationTargetException) { // unwrap invocation exception
                e.cause?.let { throw it } ?: throw e // if cause exists, throw, else rethrow invocation exception
            }
            null
        }
    }

    private fun KParameter.hasType(clazz: Class<*>): Boolean {
        val javaType = GenericTypeReflector.erase(type.javaType)
        return javaType == clazz
    }
}

private class KotlinSuggestionProviderFactory<C>(
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext
) : SuggestionProviderFactory<C> {

    override fun createSuggestionProvider(
        instance: Any,
        method: Method,
        injectorRegistry: ParameterInjectorRegistry<C>
    ): SuggestionProvider<C> {
        if (method.kotlinFunction == null) {
            return SuggestionProviderFactory.defaultFactory<C>().createSuggestionProvider(instance, method, injectorRegistry)
        }
        val kFunction = requireNotNull(method.kotlinFunction)
        return KotlinSuggestionProvider(coroutineScope, coroutineContext, kFunction, instance)
    }
}

private class KotlinSuggestionProvider<C>(
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext,
    private val kFunction: KFunction<*>,
    private val instance: Any
) : SuggestionProvider<C> {

    override fun suggestionsFuture(context: CommandContext<C>, input: CommandInput): CompletableFuture<Iterable<Suggestion>> {
        return coroutineScope.future(coroutineContext) {
            try {
                if (kFunction.valueParameters[1].type.classifier == String::class) {
                    kFunction.callSuspend(instance, context, input.lastRemainingToken())
                } else {
                    kFunction.callSuspend(instance, context, input)
                }
            } catch (e: InvocationTargetException) {
                e.cause?.let { throw it } ?: throw e // if cause exists, throw, else rethrow invocation exception
            }
        }.thenCompose { result ->
            when (result) {
                null -> CompletableFuture.completedFuture(emptyList<Suggestion>())
                is Sequence<*> -> MethodSuggestionProvider.mapSuggestions(result.asIterable())
                else -> MethodSuggestionProvider.mapSuggestions(result)
            }
        }
    }
}
