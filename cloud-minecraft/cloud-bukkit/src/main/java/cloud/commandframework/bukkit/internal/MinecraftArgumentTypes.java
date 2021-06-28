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
/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package cloud.commandframework.bukkit.internal;

import com.google.common.annotations.Beta;
import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A registry of the {@link ArgumentType}s provided by Minecraft.
 * <p>
 * This file is taken from MIT licensed code in commodore (https://github.com/lucko/commodore).
 *
 * <p>This is not API, and as such, may break, change, or be removed without any notice.</p>
 */
@Beta
public final class MinecraftArgumentTypes {

    private MinecraftArgumentTypes() {
    }

    private static final Constructor<?> MINECRAFT_KEY_CONSTRUCTOR;
    private static final Method ARGUMENT_REGISTRY_GET_BY_KEY_METHOD;
    private static final Field ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD;

    static {
        try {
            final Class<?> minecraftKey;
            final Class<?> argumentRegistry;

            if (CraftBukkitReflection.findMCClass("resources.ResourceLocation") != null) {
                minecraftKey = CraftBukkitReflection.needMCClass("resources.ResourceLocation");
                argumentRegistry = CraftBukkitReflection.needMCClass("commands.synchronization.ArgumentTypes");
            } else {
                minecraftKey = CraftBukkitReflection.needNMSClassOrElse(
                        "MinecraftKey",
                        "net.minecraft.resources.MinecraftKey"
                );
                argumentRegistry = CraftBukkitReflection.needNMSClassOrElse(
                        "ArgumentRegistry",
                        "net.minecraft.commands.synchronization.ArgumentRegistry"
                );
            }

            MINECRAFT_KEY_CONSTRUCTOR = minecraftKey.getConstructor(String.class, String.class);
            MINECRAFT_KEY_CONSTRUCTOR.setAccessible(true);

            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD = Arrays.stream(argumentRegistry.getDeclaredMethods())
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> minecraftKey.equals(method.getParameterTypes()[0]))
                    .findFirst().orElseThrow(NoSuchMethodException::new);
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.setAccessible(true);

            Class<?> argumentRegistryEntry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.getReturnType();
            ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD = Arrays.stream(argumentRegistryEntry.getDeclaredFields())
                    .filter(field -> field.getType() == Class.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Gets a registered argument type class by key.
     *
     * @param key the key
     * @return the returned argument type class
     * @throws IllegalArgumentException if no such argument is registered
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ArgumentType<?>> getClassByKey(
            final @NonNull NamespacedKey key
    ) throws IllegalArgumentException {
        try {
            Object minecraftKey = MINECRAFT_KEY_CONSTRUCTOR.newInstance(key.getNamespace(), key.getKey());
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            if (entry == null) {
                throw new IllegalArgumentException(key.toString());
            }

            final Class<?> argument = (Class<?>) ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.get(entry);
            return (Class<? extends ArgumentType<?>>) argument;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
