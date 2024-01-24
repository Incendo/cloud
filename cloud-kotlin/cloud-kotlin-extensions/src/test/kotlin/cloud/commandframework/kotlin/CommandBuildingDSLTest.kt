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
package cloud.commandframework.kotlin

import cloud.commandframework.CommandDescription
import cloud.commandframework.CommandManager
import cloud.commandframework.execution.ExecutionCoordinator
import cloud.commandframework.help.result.CommandEntry
import cloud.commandframework.internal.CommandRegistrationHandler
import cloud.commandframework.keys.CloudKey
import cloud.commandframework.kotlin.extension.argumentDescription
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.kotlin.extension.cloudKey
import cloud.commandframework.kotlin.extension.command
import cloud.commandframework.kotlin.extension.commandBuilder
import cloud.commandframework.parser.standard.StringParser.stringParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CommandBuildingDSLTest {

    @Test
    fun testCommandDSL() {
        val manager = TestCommandManager()
        val moment: CloudKey<String> = cloudKey("moment")

        manager.command(
            manager.commandBuilder("kotlin", aliases = arrayOf("alias")) {
                permission = "permission"
                senderType<SpecificCommandSender>()

                literal("dsl")

                required(moment, stringParser()) {
                    description(argumentDescription("An amazing command argument"))
                }

                handler {
                    // ...
                    val argumentValue: String = it[moment]
                }

                manager.command(
                    copy {
                        literal("bruh_moment")
                        handler {
                            // ...
                        }
                    }
                )
            }
        )

        manager.buildAndRegister("is") {
            commandDescription(CommandDescription.commandDescription("Command description"))

            registerCopy {
                literal("this")
                commandDescription(CommandDescription.commandDescription("Command description"))

                registerCopy {
                    literal("going")
                    commandDescription(CommandDescription.commandDescription("Command Description"))

                    registerCopy("too_far") {
                        // ?
                    }
                }
            }
        }

        manager.commandExecutor().executeCommand(SpecificCommandSender(), "kotlin dsl time")
        manager.commandExecutor().executeCommand(SpecificCommandSender(), "kotlin dsl time bruh_moment")

        Assertions.assertEquals(
            manager.createHelpHandler()
                .queryRootIndex(TestCommandSender())
                .entries()
                .map(CommandEntry<*>::syntax).sorted(),
            setOf(
                "kotlin dsl <moment>",
                "kotlin dsl <moment> bruh_moment",
                "is",
                "is this",
                "is this going",
                "is this going too_far"
            ).sorted()
        )
    }

    class TestCommandManager : CommandManager<TestCommandSender>(
        ExecutionCoordinator.simpleCoordinator(),
        CommandRegistrationHandler.nullCommandRegistrationHandler()
    ) {

        override fun hasPermission(sender: TestCommandSender, permission: String): Boolean {
            return !permission.equals("no", ignoreCase = true)
        }
    }

    open class TestCommandSender
    class SpecificCommandSender : TestCommandSender()
}
