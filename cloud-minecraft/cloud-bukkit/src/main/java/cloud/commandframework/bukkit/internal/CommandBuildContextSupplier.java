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

import com.google.common.annotations.Beta;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This is not API, and as such, may break, change, or be removed without any notice.
 */
@Beta
public final class CommandBuildContextSupplier {

    private static final Class<?> COMMAND_BUILD_CONTEXT_CLASS = CraftBukkitReflection.needMCClass("commands.CommandBuildContext");
    private static final Constructor<?> COMMAND_BUILD_CONTEXT_CTR = COMMAND_BUILD_CONTEXT_CLASS.getDeclaredConstructors()[0];
    private static final Class<?> REG_ACC_CLASS = COMMAND_BUILD_CONTEXT_CTR.getParameterTypes()[0];
    private static final Class<?> MC_SERVER_CLASS = CraftBukkitReflection.needNMSClassOrElse(
            "MinecraftServer", "net.minecraft.server.MinecraftServer"
    );
    private static final Method GET_SERVER_METHOD;
    private static final Method REGISTRY_ACCESS = Arrays.stream(MC_SERVER_CLASS.getDeclaredMethods())
            .filter(m -> REG_ACC_CLASS.isAssignableFrom(m.getReturnType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find MinecraftServer#registryAccess"));

    static {
        try {
            GET_SERVER_METHOD = MC_SERVER_CLASS.getDeclaredMethod("getServer");
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandBuildContextSupplier() {
    }

    public static Object commandBuildContext() {
        try {
            final Object server = GET_SERVER_METHOD.invoke(null);
            return COMMAND_BUILD_CONTEXT_CTR.newInstance(REGISTRY_ACCESS.invoke(server));
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
