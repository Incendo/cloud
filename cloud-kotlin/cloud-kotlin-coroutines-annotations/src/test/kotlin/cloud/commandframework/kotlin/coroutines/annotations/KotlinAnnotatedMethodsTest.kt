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

import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.arguments.suggestion.Suggestion
import cloud.commandframework.context.CommandContext
import cloud.commandframework.context.StandardCommandContextFactory
import cloud.commandframework.exceptions.CommandExecutionException
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.internal.CommandRegistrationHandler
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.meta.SimpleCommandMeta
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class KotlinAnnotatedMethodsTest {

    companion object {
        val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    }

    private lateinit var commandManager: CommandManager<TestCommandSender>

    @BeforeEach
    fun setUp() {
        commandManager = TestCommandManager()
    }

    private fun awaitCommands() {
        executorService.shutdown()
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    @Test
    fun `test suspending command methods`(): Unit = runBlocking {
        AnnotationParser(commandManager, TestCommandSender::class.java) {
            SimpleCommandMeta.empty()
        }
            .installCoroutineSupport()
            .parse(CommandMethods())

        commandManager.executeCommand(TestCommandSender(), "test").await()
    }

    @Test
    fun `test suspending command methods with exception`(): Unit = runBlocking {
        AnnotationParser(commandManager, TestCommandSender::class.java) {
            SimpleCommandMeta.empty()
        }
            .installCoroutineSupport()
            .parse(CommandMethods())

        assertThrows<CommandExecutionException> {
            commandManager.executeCommand(TestCommandSender(), "test-exception").await()
        }
    }

    @Test
    fun `test method with default value`(): Unit = runBlocking {
        AnnotationParser(commandManager, TestCommandSender::class.java) {
            SimpleCommandMeta.empty()
        }
            .installCoroutineSupport()
            .parse(CommandMethods())

        val result = commandManager.executeCommand(TestCommandSender(), "with-default").await()
        assertThat(result.commandContext.get<Int>("the-value")).isEqualTo(5)
    }

    @Test
    fun `test suspending suggestion method`(): Unit = runBlocking {
        AnnotationParser(commandManager, TestCommandSender::class.java) {
            SimpleCommandMeta.empty()
        }
            .installCoroutineSupport()
            .parse(SuggestionMethods())

        val commandContext = StandardCommandContextFactory<TestCommandSender>().create(
            true,
            TestCommandSender(),
            commandManager
        )
        val suggestions = commandManager.parserRegistry().getSuggestionProvider("suspending-suggestions").get()
            .suggestionsFuture(commandContext, "")
            .await()
            .map(Suggestion::suggestion)
            .map(String::toInt)
        assertThat(suggestions).containsExactlyElementsIn(1..10)
    }

    @Test
    fun `test non-suspending suggestion method`(): Unit = runBlocking {
        AnnotationParser(commandManager, TestCommandSender::class.java) {
            SimpleCommandMeta.empty()
        }
            .installCoroutineSupport()
            .parse(SuggestionMethods())

        val commandContext = StandardCommandContextFactory<TestCommandSender>().create(
            true,
            TestCommandSender(),
            commandManager
        )
        val suggestions = commandManager.parserRegistry().getSuggestionProvider("non-suspending-suggestions").get()
            .suggestionsFuture(commandContext, "")
            .await()
            .map(Suggestion::suggestion)
            .map(String::toInt)
        assertThat(suggestions).containsExactlyElementsIn(1..10)
    }

    public class TestCommandSender

    private class TestCommandManager : CommandManager<TestCommandSender>(
        AsynchronousCommandExecutionCoordinator.builder<TestCommandSender>()
            .withExecutor(executorService)
            .build(),
        CommandRegistrationHandler.nullCommandRegistrationHandler()
    ) {

        override fun hasPermission(sender: TestCommandSender, permission: String): Boolean = true

        override fun createDefaultCommandMeta(): CommandMeta = SimpleCommandMeta.empty()
    }

    public class CommandMethods {

        @CommandMethod("test")
        public suspend fun suspendingCommand(): Unit =
            withContext(Dispatchers.Default) {
                println("called from thread: ${Thread.currentThread().name}")
            }

        @CommandMethod("test-exception")
        public suspend fun suspendingCommandWithException(): Unit = throw IllegalStateException()

        @CommandMethod("with-default [value]")
        public fun commandWithDefault(@Argument("value") value: Int = 5, context: CommandContext<TestCommandSender>) {
            context["the-value"] = value
        }
    }

    class SuggestionMethods {

        @Suggestions("suspending-suggestions")
        suspend fun suspendingSuggestions(ctx: CommandContext<TestCommandSender>, input: String): Sequence<Suggestion> =
            withContext(Dispatchers.Default) {
                (1..10).asSequence().map(Int::toString).map(Suggestion::simple)
            }

        @Suggestions("non-suspending-suggestions")
        fun suggestions(ctx: CommandContext<TestCommandSender>, input: String): Sequence<Suggestion> =
            (1..10).asSequence().map(Int::toString).map(Suggestion::simple)
    }
}
