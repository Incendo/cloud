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
package cloud.commandframework.brigadier.permission;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.permission.Permission;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "2.0.0")
public final class BrigadierPermissionPredicate<C, S> implements Predicate<S> {

    private final SenderMapper<S, C> senderMapper;
    private final BrigadierPermissionChecker<C> permissionChecker;
    private final CommandNode<?> node;

    /**
     * Returns a new predicate that uses the given {@code permissionChecker} to evaluate the permission attached
     * to the given {@code node}.
     *
     * @param senderMapper      mapper from brig source to cloud sender
     * @param permissionChecker the permission checker
     * @param node              the cloud command node
     */
    public BrigadierPermissionPredicate(
            final @NonNull SenderMapper<S, C> senderMapper,
            final @NonNull BrigadierPermissionChecker<C> permissionChecker,
            final @NonNull CommandNode<?> node
    ) {
        this.senderMapper = senderMapper;
        this.permissionChecker = permissionChecker;
        this.node = node;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean test(final @NonNull S source) {
        final Permission permission = (Permission) this.node.nodeMeta().getOrDefault(
                CommandNode.META_KEY_PERMISSION,
                Permission.empty()
        );
        final Set<Class<?>> senderTypes = (Set<Class<?>>) this.node.nodeMeta().getOrDefault(
                CommandNode.META_KEY_SENDER_TYPES,
                Collections.emptySet()
        );
        final C cloudSender = this.senderMapper.map(source);
        if (senderTypes.isEmpty()) {
            return this.permissionChecker.hasPermission(cloudSender, permission);
        }
        for (final Class<?> senderType : senderTypes) {
            if (senderType.isInstance(cloudSender)) {
                return this.permissionChecker.hasPermission(cloudSender, permission);
            }
        }
        return false;
    }
}
