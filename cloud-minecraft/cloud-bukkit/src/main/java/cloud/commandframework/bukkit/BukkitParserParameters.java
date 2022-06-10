//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.bukkit;

import cloud.commandframework.arguments.parser.ParserParameter;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link ParserParameter} keys for cloud-bukkit.
 *
 * @since 1.7.0
 */
public final class BukkitParserParameters {

    private BukkitParserParameters() {
    }

    /**
     * Sets to require explicit namespaces for {@link cloud.commandframework.bukkit.argument.NamespacedKeyArgument}
     * (i.e. 'test' will be rejected but 'test:test' will pass).
     *
     * @since 1.7.0
     */
    public static final ParserParameter<Boolean> REQUIRE_EXPLICIT_NAMESPACE =
            create("require_explicit_namespace", TypeToken.get(Boolean.class));

    /**
     * Sets a custom default namespace for {@link cloud.commandframework.bukkit.argument.NamespacedKeyArgument}.
     * Without this annotation the default is {@link org.bukkit.NamespacedKey#MINECRAFT}.
     *
     * @since 1.7.0
     */
    public static final ParserParameter<String> DEFAULT_NAMESPACE =
            create("default_namespace", TypeToken.get(String.class));

    private static <T> @NonNull ParserParameter<T> create(
            final @NonNull String key,
            final @NonNull TypeToken<T> expectedType
    ) {
        return new ParserParameter<>(key, expectedType);
    }

}
