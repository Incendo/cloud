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
package cloud.commandframework.permission;

import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.CloudKeyHolder;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A functional {@link Permission} implementation
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "1.4.0")
public interface PredicatePermission<C> extends Permission, CloudKeyHolder<Void> {

    /**
     * Create a new predicate permission
     *
     * @param key       Key that identifies the permission node
     * @param predicate Predicate that determines whether the sender has the permission
     * @param <C>       Command sender type
     * @return Created permission node
     */
    static <C> PredicatePermission<C> of(final @NonNull CloudKey<Void> key, final @NonNull Predicate<C> predicate) {
        return new WrappingPredicatePermission<>(key, predicate);
    }

    /**
     * Create a new predicate permission
     *
     * @param predicate Predicate that determines whether the sender has the permission
     * @param <C>       Command sender type
     * @return Created permission node
     */
    static <C> PredicatePermission<C> of(final @NonNull Predicate<C> predicate) {
        return new PredicatePermission<C>() {
            @Override
            public @NonNull PermissionResult testPermission(final @NonNull C sender) {
                return PermissionResult.of(predicate.test(sender), this);
            }
        };
    }

    @Override
    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    default @NonNull CloudKey<Void> key() {
        return CloudKey.of(this.getClass().getSimpleName());
    }

    @Override
    default @NonNull String permissionString() {
        return this.key().name();
    }

    /**
     * Checks whether the given sender has this permission
     *
     * @param sender Sender to check for
     * @return a {@link PermissionResult} representing the check result
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @NonNull PermissionResult testPermission(@NonNull C sender);

    @Override
    default boolean isEmpty() {
        return false;
    }
}
