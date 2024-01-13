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
package cloud.commandframework.kotlin.coroutines

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.suggestion.Suggestion
import cloud.commandframework.execution.ExecutionCoordinator
import cloud.commandframework.internal.CommandRegistrationHandler
import cloud.commandframework.kotlin.extension.buildAndRegister
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SuspendingArgumentParserTest {

    companion object {
        val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    }

    @Test
    fun test(): Unit = runBlocking {
        val suspendingParser = suspendingArgumentParser<TestCommandSender, Int> { _, commandInput ->
            delay(1L)
            ArgumentParseResult.success(commandInput.readInteger())
        }
        val suspendingSuggestionProvider = suspendingSuggestionProvider<TestCommandSender> { _, _ ->
            delay(1L)
            (1..3).asSequence().map(Number::toString).map(Suggestion::simple).asIterable()
        }

        val manager = TestCommandManager()

        manager.buildAndRegister("test") {
            required("int", suspendingParser) {
                suggestionProvider(suspendingSuggestionProvider)
            }
        }

        manager.commandExecutor().executeCommand(TestCommandSender(), "test 123").await()
        assertThat(manager.suggestionFactory().suggest(TestCommandSender(), "test ").await().list()).containsExactly(
            Suggestion.simple("1"),
            Suggestion.simple("2"),
            Suggestion.simple("3")
        )
    }

    private class TestCommandSender

    private class TestCommandManager : CommandManager<TestCommandSender>(
        ExecutionCoordinator.builder<TestCommandSender>()
            .executor(executorService)
            .build(),
        CommandRegistrationHandler.nullCommandRegistrationHandler()
    ) {

        override fun hasPermission(sender: TestCommandSender, permission: String): Boolean = true
    }
}
