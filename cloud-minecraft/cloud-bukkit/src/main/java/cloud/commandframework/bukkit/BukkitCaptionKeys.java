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
package cloud.commandframework.bukkit;

import cloud.commandframework.captions.Caption;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link Caption} instances for messages in cloud-bukkit
 */
public final class BukkitCaptionKeys {

    private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();

    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_ENCHANTMENT = of("argument.parse.failure.enchantment");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_MATERIAL = of("argument.parse.failure.material");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = of("argument.parse.failure.offlineplayer");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_PLAYER = of("argument.parse.failure.player");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_WORLD = of("argument.parse.failure.world");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED = of("argument.parse.failure.selector.unsupported");
    /**
     * Variables: {@code <input>}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT = of(
            "argument.parse.failure.location.invalid_format");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE = of(
            "argument.parse.failure.location.mixed_local_absolute");
    /**
     * Variables: {@code <input>}
     *
     * @since 1.7.0
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE =
            of("argument.parse.failure.namespacedkey.namespace");
    /**
     * Variables: {@code <input>}
     *
     * @since 1.7.0
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY =
            of("argument.parse.failure.namespacedkey.key");
    /**
     * Variables: {@code <input>}
     *
     * @since 1.7.0
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE =
            of("argument.parse.failure.namespacedkey.need_namespace");

    private BukkitCaptionKeys() {
    }

    private static @NonNull Caption of(final @NonNull String key) {
        final Caption caption = Caption.of(key);
        RECOGNIZED_CAPTIONS.add(caption);
        return caption;
    }

    /**
     * Get an immutable collection containing all standard caption keys.
     *
     * @return immutable collection of keys
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull Collection<@NonNull Caption> bukkitCaptionKeys() {
        return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
    }
}
