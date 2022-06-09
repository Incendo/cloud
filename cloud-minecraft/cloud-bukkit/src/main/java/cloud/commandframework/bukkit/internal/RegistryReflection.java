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
package cloud.commandframework.bukkit.internal;

import com.google.common.annotations.Beta;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This is not API, and as such, may break, change, or be removed without any notice.
 */
@Beta
public final class RegistryReflection {
    public static final @Nullable Field REGISTRY_REGISTRY;
    public static final @Nullable Method REGISTRY_GET;

    private static final Class<?> RESOURCE_LOCATION_CLASS = CraftBukkitReflection.needNMSClassOrElse(
            "MinecraftKey",
            "net.minecraft.resources.MinecraftKey",
            "net.minecraft.resources.ResourceLocation"
    );
    private static final Constructor<?> RESOURCE_LOCATION_CTR = CraftBukkitReflection.needConstructor(
            RESOURCE_LOCATION_CLASS,
            String.class
    );

    private RegistryReflection() {
    }

    static {
        Class<?> registryClass;
        if (CraftBukkitReflection.MAJOR_REVISION < 17) {
            REGISTRY_REGISTRY = null;
            REGISTRY_GET = null;
        } else {
            registryClass = CraftBukkitReflection.firstNonNullOrThrow(
                    () -> "Registry",
                    CraftBukkitReflection.findMCClass("core.IRegistry"),
                    CraftBukkitReflection.findMCClass("core.Registry")
            );
            final Class<?> registryClassFinal = registryClass;
            REGISTRY_REGISTRY = Arrays.stream(registryClass.getDeclaredFields())
                    .filter(it -> it.getType().equals(registryClassFinal))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find Registry Registry field"));
            REGISTRY_REGISTRY.setAccessible(true);
            final Class<?> resourceLocationClass = CraftBukkitReflection.firstNonNullOrThrow(
                    () -> "ResourceLocation class",
                    CraftBukkitReflection.findMCClass("resources.ResourceLocation"),
                    CraftBukkitReflection.findMCClass("resources.MinecraftKey")
            );
            REGISTRY_GET = Arrays.stream(registryClass.getDeclaredMethods())
                    .filter(it -> it.getParameterCount() == 1
                            && it.getParameterTypes()[0].equals(resourceLocationClass)
                            && it.getReturnType().equals(Object.class))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find Registry#get(ResourceLocation)"));
        }
    }

    public static Object get(final Object registry, final String resourceLocation) {
        Objects.requireNonNull(REGISTRY_GET, "REGISTRY_GET");
        try {
            return REGISTRY_GET.invoke(registry, RegistryReflection.createResourceLocation(resourceLocation));
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object registryByName(final String name) {
        Objects.requireNonNull(REGISTRY_REGISTRY, "REGISTRY_REGISTRY");
        try {
            return get(REGISTRY_REGISTRY.get(null), name);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createResourceLocation(final String str) {
        try {
            return RESOURCE_LOCATION_CTR.newInstance(str);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
