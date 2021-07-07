package cloud.commandframework.kotlin.coroutines

import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.internal.CommandRegistrationHandler
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.meta.SimpleCommandMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class KotlinAnnotatedMethodsTest {

    companion object {
        val executorService = Executors.newSingleThreadExecutor()
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
        }.also { it.installCoroutineSupport() }.parse(CommandMethods())

        commandManager.executeCommand(TestCommandSender(), "test").await()
    }

    private class TestCommandSender {}

    private class TestCommandManager : CommandManager<TestCommandSender>(
            AsynchronousCommandExecutionCoordinator.newBuilder<TestCommandSender>().withExecutor(executorService).build(),
            CommandRegistrationHandler.nullCommandRegistrationHandler()
    ) {

        override fun hasPermission(sender: TestCommandSender, permission: String): Boolean = true

        override fun createDefaultCommandMeta(): CommandMeta = SimpleCommandMeta.empty()

    }

    public class CommandMethods {

        @CommandMethod("test")
        public suspend fun suspendingCommand(): Unit = withContext(Dispatchers.Default) {
            println("called from thread: ${Thread.currentThread().name}")
        }

    }

}
