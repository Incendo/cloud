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
package org.incendo.cloud.kotlin.extension

import org.incendo.cloud.Command
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.MutableCommandBuilder
import kotlin.reflect.KClass

/**
 * Create a new [MutableCommandBuilder] and invoke the provided receiver lambda on it
 *
 * @param name name for the root command node
 * @param description description for the root command node
 * @param aliases aliases for the root command node
 * @param lambda receiver lambda which will be invoked on the new builder
 */
public fun <C : Any> CommandManager<C>.commandBuilder(
    name: String,
    description: Description = Description.empty(),
    aliases: Array<String> = emptyArray(),
    lambda: MutableCommandBuilder<C>.() -> Unit
): MutableCommandBuilder<C> = MutableCommandBuilder(name, description, aliases, this, lambda)

/**
 * Create a new [MutableCommandBuilder] which will invoke the provided receiver lambda, and then
 * register itself with the owning [CommandManager]
 *
 * @param name name for the root command node
 * @param description description for the root command node
 * @param aliases aliases for the root command node
 * @param lambda receiver lambda which will be invoked on the new builder
 */
public fun <C : Any> CommandManager<C>.buildAndRegister(
    name: String,
    description: Description = Description.empty(),
    aliases: Array<String> = emptyArray(),
    lambda: MutableCommandBuilder<C>.() -> Unit
): MutableCommandBuilder<C> = commandBuilder(name, description, aliases, lambda).register()

/**
 * Build the provided [MutableCommandBuilder]s into [Command]s, and then register them with the
 * command manager
 *
 * @param commands mutable command builder(s) to register
 * @return the command manager
 * @see [CommandManager.command]
 */
public fun <C : Any> CommandManager<C>.command(
    vararg commands: MutableCommandBuilder<C>
): CommandManager<C> = apply { commands.forEach { command -> this.command(command.build()) } }

/**
 * Specify a required sender type
 *
 * @param type required sender type
 * @return New builder instance using the required sender type
 */
public fun <C : Any> Command.Builder<C>.senderType(type: KClass<out C>): Command.Builder<C> =
    senderType(type.java)

/**
 * Create a new [MutableCommandBuilder].
 *
 * @param commandManager the command manager, which will own this command.
 */
public fun <C : Any> Command.Builder<C>.toMutable(
    commandManager: CommandManager<C>
): MutableCommandBuilder<C> = MutableCommandBuilder(this, commandManager)

/**
 * Create a new [MutableCommandBuilder] and invoke the provided receiver lambda on it.
 *
 * @param commandManager the command manager, which will own this command.
 * @param lambda receiver lambda, which will be invoked on the new builder.
 */
public fun <C : Any> Command.Builder<C>.mutate(
    commandManager: CommandManager<C>,
    lambda: MutableCommandBuilder<C>.() -> Unit
): MutableCommandBuilder<C> = MutableCommandBuilder(this, commandManager).also(lambda)

/**
 * Get a [Description], defaulting to [Description.empty]
 *
 * @param description description string
 * @return the description
 */
public fun argumentDescription(description: String = ""): Description =
    if (description.isEmpty()) Description.empty() else Description.of(description)
