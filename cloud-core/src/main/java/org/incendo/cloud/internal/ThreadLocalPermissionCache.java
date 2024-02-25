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
package org.incendo.cloud.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.Configurable;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.type.tuple.Pair;

@API(status = API.Status.INTERNAL)
public final class ThreadLocalPermissionCache<C> {

    private final ThreadLocal<Pair<Map<Pair<C, Permission>, PermissionResult>, AtomicInteger>> threadLocalPermissionCache =
            ThreadLocal.withInitial(() -> Pair.of(new HashMap<>(), new AtomicInteger(0)));
    private final Configurable<ManagerSetting> settings;

    /**
     * Create a new cache.
     *
     * @param settings settings
     */
    public ThreadLocalPermissionCache(final Configurable<ManagerSetting> settings) {
        this.settings = settings;
    }

    /**
     * Perform an action in a cached scope.
     *
     * @param action action
     * @param <T>    result type
     * @return result
     */
    public <T> T withPermissionCache(final Supplier<T> action) {
        final boolean cache = this.settings.get(ManagerSetting.REDUCE_REDUNDANT_PERMISSION_CHECKS);
        try {
            if (cache) {
                final int prev = this.threadLocalPermissionCache.get().second().getAndIncrement();
                if (prev == 0) {
                    // Cleanup from case where cache was enabled mid-permission check
                    this.threadLocalPermissionCache.get().first().clear();
                }
            }
            return action.get();
        } finally {
            if (cache) {
                final Pair<Map<Pair<C, Permission>, PermissionResult>, AtomicInteger> pair = this.threadLocalPermissionCache.get();
                if (pair.second().getAndDecrement() == 1) {
                    pair.first().clear();
                }
            }
        }
    }

    /**
     * Test permission caching.
     *
     * @param sender     sender
     * @param permission permission
     * @param tester     tester
     * @param <T>        permission type
     * @return permission result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NonNull <T> PermissionResult testPermissionCaching(
            final @NonNull C sender,
            final @NonNull T permission,
            final @NonNull Function<Pair<C, T>, @NonNull PermissionResult> tester
    ) {
        if (!this.settings.get(ManagerSetting.REDUCE_REDUNDANT_PERMISSION_CHECKS)) {
            return tester.apply(Pair.of(sender, permission));
        }
        return this.threadLocalPermissionCache.get().first()
                .computeIfAbsent((Pair) Pair.of(sender, permission), (Function) tester);
    }
}
