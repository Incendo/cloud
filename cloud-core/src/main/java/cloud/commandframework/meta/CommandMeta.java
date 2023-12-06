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
package cloud.commandframework.meta;

import cloud.commandframework.Command;
import cloud.commandframework.keys.CloudKey;
import java.util.Map;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Object that is associated with a {@link Command}.
 * Command meta should not be mutable, as one fixed instance will be used per command.
 * <p>
 * Appropriate use for command meta would be fixed state, such as command descriptions.
 */
@API(status = API.Status.STABLE)
public abstract class CommandMeta {

    public static final CloudKey<Boolean> HIDDEN = CloudKey.of(
            "cloud:hidden",
            Boolean.class
    );

    /**
     * Create a new simple command meta-builder
     *
     * @return Builder instance
     */
    public static @NonNull CommandMetaBuilder builder() {
        return new CommandMetaBuilder();
    }

    /**
     * Creates an empty simple command meta-instance
     *
     * @return the empty instance
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull CommandMeta empty() {
        return builder().build();
    }

    @Override
    public final @NonNull String toString() {
        return "";
    }

    /**
     * Get the value associated with a key.
     *
     * @param <V> Value type
     * @param key Key
     * @return Optional that may contain the associated value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract <V> @NonNull Optional<V> get(@NonNull CloudKey<V> key);

    /**
     * Get the value if it exists, else return the default value.
     *
     * @param <V>          Value type
     * @param key          Key
     * @param defaultValue Default value
     * @return Value, or default value
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract <V> @NonNull V getOrDefault(@NonNull CloudKey<V> key, @NonNull V defaultValue);

    /**
     * Get a copy of the meta map, without type information.
     *
     * @return Copy of meta map
     * @since 1.3.0
     */
    @API(status = API.Status.STABLE, since = "1.3.0")
    public abstract @NonNull Map<@NonNull String, @NonNull ?> getAllValues();
}
