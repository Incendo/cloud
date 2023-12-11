package cloud.commandframework.kotlin.extension

import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.arguments.parser.ParserDescriptor

/**
 * Returns a [ParserDescriptor] that describes [this] parser.
 *
 * @return the descriptor
 */
public inline fun <C, reified T> ArgumentParser<C, T>.asDescriptor(): ParserDescriptor<C, T> =
    ParserDescriptor.of(this, T::class.java)

/**
 * Returns a [ParserDescriptor] that describes the [parser].
 *
 * @return the descriptor
 */
public inline fun <C, reified T> parserDescriptor(parser: ArgumentParser<C, T>): ParserDescriptor<C, T> =
    parser.asDescriptor()
