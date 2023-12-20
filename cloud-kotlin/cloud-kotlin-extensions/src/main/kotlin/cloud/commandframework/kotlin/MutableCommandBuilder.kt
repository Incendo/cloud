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
package cloud.commandframework.kotlin

import cloud.commandframework.Command
import cloud.commandframework.CommandComponent
import cloud.commandframework.CommandDescription
import cloud.commandframework.CommandManager
import cloud.commandframework.Description
import cloud.commandframework.TypedCommandComponent
import cloud.commandframework.arguments.parser.ParserDescriptor
import cloud.commandframework.execution.CommandExecutionHandler
import cloud.commandframework.keys.CloudKey
import cloud.commandframework.kotlin.extension.command
import cloud.commandframework.kotlin.extension.senderType
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.permission.Permission
import kotlin.reflect.KClass

/**
 * A mutable [Command.Builder] wrapper, providing functions to assist in creating commands using the
 * Kotlin builder DSL style
 *
 * @property commandBuilder the command builder the mutate
 * @property commandManager the command manager which will own this command
 * @constructor Create a new [MutableCommandBuilder]
 * @since 1.3.0
 */
public class MutableCommandBuilder<C : Any>(
    commandBuilder: Command.Builder<C>,
    private val commandManager: CommandManager<C>
) {
    /**
     * The command builder that is being mutated by this [MutableCommandBuilder] instance.
     *
     * This is public so that this can be returned to a command builder for interop with java apis.
     */
    public var commandBuilder: Command.Builder<C> = commandBuilder
        private set

    /**
     * Create a new [MutableCommandBuilder]
     *
     * @param name name for the root command node
     * @param description description for the root command node
     * @param aliases aliases for the root command node
     * @param commandManager the command manager which will own this command
     * @since 1.4.0
     */
    public constructor(
        name: String,
        description: Description = Description.empty(),
        aliases: Array<String> = emptyArray(),
        commandManager: CommandManager<C>
    ) : this(commandManager.commandBuilder(name, description, *aliases), commandManager)

    /**
     * Create a new [MutableCommandBuilder] and invoke the provided receiver lambda on it
     *
     * @param name name for the root command node
     * @param description description for the root command node
     * @param aliases aliases for the root command node
     * @param commandManager the command manager which will own this command
     * @param lambda receiver lambda which will be invoked on the new builder
     * @since 1.4.0
     */
    public constructor(
        name: String,
        description: Description = Description.empty(),
        aliases: Array<String> = emptyArray(),
        commandManager: CommandManager<C>,
        lambda: MutableCommandBuilder<C>.() -> Unit
    ) : this(name, description, aliases, commandManager) {
        lambda(this)
    }

    /**
     * Build a [Command] from the current state of this builder
     *
     * @return built command
     * @since 1.3.0
     */
    public fun build(): Command<C> = this.commandBuilder.build()

    /**
     * Invoke the provided receiver lambda on this builder, then build a [Command] from the
     * resulting state
     *
     * @param lambda receiver lambda which will be invoked on builder before building
     * @return built command
     * @since 1.3.0
     */
    public fun build(lambda: MutableCommandBuilder<C>.() -> Unit): Command<C> {
        lambda(this)
        return this.commandBuilder.build()
    }

    /**
     * Modify this [MutableCommandBuilder]'s internal [Command.Builder] with a unary function
     *
     * @param mutator mutator function
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun mutate(
        mutator: (Command.Builder<C>) -> Command.Builder<C>
    ): MutableCommandBuilder<C> {
        this.commandBuilder = mutator(this.commandBuilder)
        return this
    }

    private fun onlyMutate(mutator: (Command.Builder<C>) -> Command.Builder<C>) {
        mutate(mutator)
    }

    /**
     * Make a new copy of this [MutableCommandBuilder]
     *
     * @return a copy of this mutable builder
     * @since 1.3.0
     */
    public fun copy(): MutableCommandBuilder<C> =
        MutableCommandBuilder(this.commandBuilder, this.commandManager)

    /**
     * Make a new copy of this [MutableCommandBuilder] and invoke the provided receiver lambda on it
     *
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return a copy of this mutable builder
     * @since 1.3.0
     */
    public fun copy(lambda: MutableCommandBuilder<C>.() -> Unit): MutableCommandBuilder<C> =
        copy().apply { lambda(this) }

    /**
     * Make a new copy of this [MutableCommandBuilder], append a literal, and invoke the provided
     * receiver lambda on it
     *
     * @param literal name for the literal
     * @param description description for the literal
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return a copy of this mutable builder
     * @since 1.4.0
     */
    public fun copy(
        literal: String,
        description: Description,
        lambda: MutableCommandBuilder<C>.() -> Unit
    ): MutableCommandBuilder<C> =
        copy().apply {
            literal(literal, description)
            lambda(this)
        }

    /**
     * Make a new copy of this [MutableCommandBuilder], append a literal, and invoke the provided
     * receiver lambda on it
     *
     * @param literal name for the literal
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return a copy of this mutable builder
     * @since 1.3.0
     */
    public fun copy(
        literal: String,
        lambda: MutableCommandBuilder<C>.() -> Unit
    ): MutableCommandBuilder<C> =
        copy().apply {
            literal(literal)
            lambda(this)
        }

    /**
     * Build and register this command with the owning command manager
     *
     * @return this mutable builder
     * @see [CommandManager.command]
     * @since 1.3.0
     */
    public fun register(): MutableCommandBuilder<C> = apply { this.commandManager.command(this) }

    /**
     * Create a new copy of this mutable builder, act on it with a receiver lambda, and then
     * register it with the owning command manager
     *
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return the new mutable builder
     * @see [CommandManager.command]
     * @since 1.3.0
     */
    public fun registerCopy(lambda: MutableCommandBuilder<C>.() -> Unit): MutableCommandBuilder<C> =
        copy(lambda).register()

    /**
     * Create a new copy of this mutable builder, append a literal, act on it with a receiver
     * lambda, and then register it with the owning command manager
     *
     * @param literal name for the literal
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return the new mutable builder
     * @see [CommandManager.command]
     * @since 1.3.0
     */
    public fun registerCopy(
        literal: String,
        lambda: MutableCommandBuilder<C>.() -> Unit
    ): MutableCommandBuilder<C> = copy(literal, lambda).register()

    /**
     * Create a new copy of this mutable builder, append a literal, act on it with a receiver
     * lambda, and then register it with the owning command manager
     *
     * @param literal name for the literal
     * @param description description for the literal
     * @param lambda receiver lambda which will be invoked on the new builder
     * @return the new mutable builder
     * @see [CommandManager.command]
     * @since 1.4.0
     */
    public fun registerCopy(
        literal: String,
        description: Description,
        lambda: MutableCommandBuilder<C>.() -> Unit
    ): MutableCommandBuilder<C> = copy(literal, description, lambda).register()

    /**
     * Set the value for a certain [CloudKey] in the command meta storage for this builder
     *
     * @param T value type
     * @param key the key to set a value for
     * @param value new value
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun <T : Any> meta(key: CloudKey<T>, value: T): MutableCommandBuilder<C> =
        mutate {
            it.meta(key, value)
        }

    /**
     * Set the value for a certain [CloudKey] in the command meta storage for this builder
     *
     * @param T value type
     * @param value new value
     * @return this mutable builder
     * @since 1.3.0
     */
    public infix fun <T : Any> CloudKey<T>.to(value: T): MutableCommandBuilder<C> =
        meta(this, value)

    /**
     * Sets the command description meta for this command
     *
     * @param commandDescription command description
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun commandDescription(commandDescription: CommandDescription): MutableCommandBuilder<C> = mutate {
        it.commandDescription(commandDescription)
    }

    /**
     * Set the [CommandMeta.HIDDEN] meta for this command
     *
     * @param hidden whether this command should be hidden
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun hidden(hidden: Boolean = true): MutableCommandBuilder<C> =
        meta(CommandMeta.HIDDEN, hidden)

    /**
     * Specify a required sender type
     *
     * @param T sender type
     * @return this mutable builder
     * @since 1.3.0
     */
    public inline fun <reified T : C> senderType(): MutableCommandBuilder<C> = mutate {
        it.senderType(T::class)
    }

    /**
     * Specify a required sender type
     *
     * @param type sender type
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun senderType(type: KClass<out C>): MutableCommandBuilder<C> = mutate {
        it.senderType(type)
    }

    /**
     * Field to get and set the required sender type for this command builder
     *
     * @since 1.3.0
     */
    public var senderType: KClass<out C>?
        get() = this.commandBuilder.senderType()?.kotlin
        set(type) {
            if (type == null) throw UnsupportedOperationException("Cannot set a null sender type")
            onlyMutate { it.senderType(type) }
        }

    /**
     * Specify a required sender type
     *
     * @param type sender type
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun senderType(type: Class<out C>): MutableCommandBuilder<C> = mutate {
        it.senderType(type)
    }

    /**
     * Specify a permission required to execute this command
     *
     * @param permission permission string
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun permission(permission: String): MutableCommandBuilder<C> = mutate {
        it.permission(permission)
    }

    /**
     * Specify a permission required to execute this command
     *
     * @param permission command permission
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun permission(permission: Permission): MutableCommandBuilder<C> = mutate {
        it.permission(permission)
    }

    /**
     * Field to get and set the required permission for this command builder
     *
     * @since 1.3.0
     */
    public var permission: String
        get() = this.commandBuilder.commandPermission().toString()
        set(permission) = onlyMutate { it.permission(permission) }

    /**
     * Field to get and set the required permission for this command builder
     *
     * @since 1.3.0
     */
    public var commandPermission: Permission
        get() = this.commandBuilder.commandPermission()
        set(permission) = onlyMutate { it.permission(permission) }

    /**
     * Adds a new component to this command
     *
     * @param component component to add
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun argument(
        component: CommandComponent<C>
    ): MutableCommandBuilder<C> = mutate { it.argument(component) }

    /**
     * Adds a new component to this command
     *
     * @param component component to add
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun required(
        component: CommandComponent.Builder<C, *>
    ): MutableCommandBuilder<C> = mutate { it.required(component) }

    /**
     * Adds a new component to this command
     *
     * @param component component to add
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun optional(
        component: CommandComponent.Builder<C, *>
    ): MutableCommandBuilder<C> = mutate { it.optional(component) }

    /**
     * Adds a new component to this command
     *
     * @param name the name of the component
     * @param parser the parser of the component
     * @param mutator mutator of the component
     * @param T the type of the component
     * @since 2.0.0
     */
    public fun <T> required(
        name: String,
        parser: ParserDescriptor<C, T>,
        mutator: CommandComponent.Builder<C, T>.() -> Unit = {}
    ): MutableCommandBuilder<C> = mutate {
        it.required(
            CommandComponent.builder<C, T>().name(name).parser(parser).also(mutator)
        )
    }

    /**
     * Adds a new component to this command
     *
     * @param name the name of the component
     * @param parser the parser of the component
     * @param mutator mutator of the component
     * @param T the type of the component
     * @since 2.0.0
     */
    public fun <T> optional(
        name: String,
        parser: ParserDescriptor<C, T>,
        mutator: CommandComponent.Builder<C, T>.() -> Unit = {}
    ): MutableCommandBuilder<C> = mutate {
        it.required(
            CommandComponent.builder<C, T>().name(name).parser(parser).optional().also(mutator)
        )
    }

    /**
     * Adds a new component to this command
     *
     * @param name the name of the component
     * @param parser the parser of the component
     * @param mutator mutator of the component
     * @param T the type of the component
     * @since 2.0.0
     */
    public fun <T> required(
        name: CloudKey<T>,
        parser: ParserDescriptor<C, T>,
        mutator: CommandComponent.Builder<C, T>.() -> Unit = {}
    ): MutableCommandBuilder<C> = mutate {
        it.required(
            CommandComponent.builder<C, T>().key(name).parser(parser).also(mutator)
        )
    }

    /**
     * Adds a new component to this command
     *
     * @param name the name of the component
     * @param parser the parser of the component
     * @param mutator mutator of the component
     * @param T the type of the component
     * @since 2.0.0
     */
    public fun <T> optional(
        name: CloudKey<T>,
        parser: ParserDescriptor<C, T>,
        mutator: CommandComponent.Builder<C, T>.() -> Unit = {}
    ): MutableCommandBuilder<C> = mutate {
        it.required(
            CommandComponent.builder<C, T>().key(name).parser(parser).optional().also(mutator)
        )
    }

    /**
     * Add a new argument to this command
     *
     * @param componentSupplier supplier of the component
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun argument(
        componentSupplier: () -> CommandComponent<C>
    ): MutableCommandBuilder<C> = mutate { it.argument(componentSupplier()) }

    /**
     * Add a new argument to this command
     *
     * @param componentSupplier supplier of the component
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun required(
        componentSupplier: () -> CommandComponent.Builder<*, *>
    ): MutableCommandBuilder<C> = mutate { it.required(componentSupplier()) }

    /**
     * Add a new argument to this command
     *
     * @param componentSupplier supplier of the component
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun optional(
        componentSupplier: () -> CommandComponent.Builder<C, *>
    ): MutableCommandBuilder<C> = mutate { it.optional(componentSupplier()) }

    /**
     * Add a new literal argument to this command
     *
     * @param name main argument name
     * @param description literal description
     * @param aliases argument aliases
     * @return this mutable builder
     * @since 1.4.0
     */
    public fun literal(
        name: String,
        description: Description = Description.empty(),
        vararg aliases: String
    ): MutableCommandBuilder<C> = mutate { it.literal(name, description, *aliases) }

    /**
     * Set the [CommandExecutionHandler] for this builder
     *
     * @param handler command execution handler
     * @return this mutable builder
     * @since 1.3.0
     */
    public fun handler(handler: CommandExecutionHandler<C>): MutableCommandBuilder<C> = mutate {
        it.handler(handler)
    }

    /**
     * Sets a new command execution handler that invokes the given {@code handler} before the current
     * {@link #handler() handler}.
     *
     * @param handler the handler to invoke before the current handler
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun prependHandler(handler: CommandExecutionHandler<C>): MutableCommandBuilder<C> = mutate {
        it.prependHandler(handler)
    }

    /**
     * Sets a new command execution handler that invokes the given {@code handler} after the current
     * {@link #handler() handler}.
     *
     * @param handler the handler to invoke after the current handler
     * @return this mutable builder
     * @since 2.0.0
     */
    public fun appendHandler(handler: CommandExecutionHandler<C>): MutableCommandBuilder<C> = mutate {
        it.appendHandler(handler)
    }

    /**
     * Add a new flag component to this command
     *
     * @param name name of the flag
     * @param aliases flag aliases
     * @param description description of the flag
     * @param componentSupplier component supplier for the flag
     * @param <T> the component value type
     * @return this mutable builder
     * @since 1.4.0
     */
    public fun <T> flag(
        name: String,
        aliases: Array<String> = emptyArray(),
        description: Description = Description.empty(),
        componentSupplier: () -> TypedCommandComponent<C, T>
    ): MutableCommandBuilder<C> = mutate {
        it.flag(
            this.commandManager
                .flagBuilder(name)
                .withAliases(*aliases)
                .withDescription(description)
                .withComponent(componentSupplier())
                .build()
        )
    }

    /**
     * Add a new flag component to this command
     *
     * @param name name of the flag
     * @param aliases flag aliases
     * @param description description of the flag
     * @param component component for the flag
     * @param <T> the component value type
     * @return this mutable builder
     * @since 1.4.0
     */
    public fun <T> flag(
        name: String,
        aliases: Array<String> = emptyArray(),
        description: Description = Description.empty(),
        component: TypedCommandComponent<C, T>
    ): MutableCommandBuilder<C> = mutate {
        it.flag(
            this.commandManager
                .flagBuilder(name)
                .withAliases(*aliases)
                .withDescription(description)
                .withComponent(component)
                .build()
        )
    }

    /**
     * Add a new flag component to this command
     *
     * @param name name of the flag
     * @param aliases flag aliases
     * @param description description of the flag
     * @param componentBuilder command component builder for the flag
     * @param <T> the component value type
     * @return this mutable builder
     * @since 1.4.0
     */
    public fun <T> flag(
        name: String,
        aliases: Array<String> = emptyArray(),
        description: Description = Description.empty(),
        componentBuilder: CommandComponent.Builder<C, T>
    ): MutableCommandBuilder<C> = mutate {
        it.flag(
            this.commandManager
                .flagBuilder(name)
                .withAliases(*aliases)
                .withDescription(description)
                .withComponent(componentBuilder)
                .build()
        )
    }

    /**
     * Add a new presence flag component to this command
     *
     * @param name name of the flag
     * @param aliases flag aliases
     * @param description description of the flag
     * @return this mutable builder
     * @since 1.4.0
     */
    public fun flag(
        name: String,
        aliases: Array<String> = emptyArray(),
        description: Description = Description.empty()
    ): MutableCommandBuilder<C> = mutate {
        it.flag(
            this.commandManager
                .flagBuilder(name)
                .withAliases(*aliases)
                .withDescription(description)
                .build()
        )
    }
}
