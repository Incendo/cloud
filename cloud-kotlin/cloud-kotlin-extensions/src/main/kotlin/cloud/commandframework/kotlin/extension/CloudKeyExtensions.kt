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
package cloud.commandframework.kotlin.extension

import cloud.commandframework.keys.CloudKey
import cloud.commandframework.keys.CloudKeyContainer
import cloud.commandframework.keys.CloudKeyHolder

/**
 * Returns the value associated with the given [key], if it exists.
 *
 * @param T the value type
 * @param key the key
 * @return the value, or `null`
 */
public fun <T : Any> CloudKeyContainer.getOrNull(key: CloudKey<T>): T? =
    this.optional(key).orElseGet(null)

/**
 * Returns the value associated with the given [key], if it exists.
 *
 * @param T the value type
 * @param key the key
 * @return the value, or `null`
 */
public fun <T : Any> CloudKeyContainer.getOrNull(key: String): T? =
    this.optional<T>(key).orElseGet(null)

/**
 * Returns the value associated with the given [keyHolder], if it exists.
 *
 * @param T the value type
 * @param keyHolder the holder of the key
 * @return the value, or `null`
 */
public fun <T : Any> CloudKeyContainer.getOrNull(keyHolder: CloudKeyHolder<T>): T? =
    this.optional<T>(keyHolder).orElseGet(null)

/**
 * Creates a new key with the given [name].
 *
 * @param T the type of the key
 * @param name the name of the key
 * @return the created key
 */
public inline fun <reified T : Any> cloudKey(name: String): CloudKey<T> =
    CloudKey.of(name, T::class.java)
