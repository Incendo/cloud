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
package cloud.commandframework.bukkit.internal;

import cloud.commandframework.bukkit.BukkitCommandManager;
import java.lang.reflect.Method;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is not API, and as such, may break, change, or be removed without any notice.
 */
@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
public final class BukkitBackwardsBrigadierSenderMapper<C, S> implements Function<C, S> {

    private static final Class<?> VANILLA_COMMAND_WRAPPER_CLASS =
            CraftBukkitReflection.needOBCClass("command.VanillaCommandWrapper");
    private static final Method GET_LISTENER_METHOD =
            CraftBukkitReflection.needMethod(VANILLA_COMMAND_WRAPPER_CLASS, "getListener", CommandSender.class);

    private final BukkitCommandManager<C> commandManager;

    public BukkitBackwardsBrigadierSenderMapper(final @NonNull BukkitCommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S apply(final @NonNull C cloud) {
        try {
            return (S) GET_LISTENER_METHOD.invoke(null, this.commandManager.getBackwardsCommandSenderMapper().apply(cloud));
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
