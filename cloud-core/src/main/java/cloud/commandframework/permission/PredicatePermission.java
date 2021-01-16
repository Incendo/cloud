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
package cloud.commandframework.permission;

import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.CloudKeyHolder;
import cloud.commandframework.keys.SimpleCloudKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * A functional {@link CommandPermission} implementation
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
@FunctionalInterface
public interface PredicatePermission<C> extends CommandPermission, CloudKeyHolder<Void> {

    /**
     * Create a new predicate permission
     *
     * @param key Key that identifies the permission node
     * @param predicate Predicate that determines whether or not the sender has the permission
     * @param <C> Command sender type
     * @return Created permission node
     */
    static <C> PredicatePermission<C> of(final @NonNull CloudKey<Void> key, final @NonNull Predicate<C> predicate) {
        return new WrappingPredicatePermission<>(key, predicate);
    }

    @Override
    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    default @NonNull CloudKey<Void> getKey() {
        return SimpleCloudKey.of(this.getClass().getSimpleName());
    }

    /**
     * Check whether or not the given sender has this permission
     *
     * @param sender Sender to check for
     * @return {@code true} if the sender has the given permission, else {@code false}
     */
    boolean hasPermission(C sender);

    @Override
    default @NonNull Collection<@NonNull CommandPermission> getPermissions() {
        return Collections.singleton(this);
    }

}
