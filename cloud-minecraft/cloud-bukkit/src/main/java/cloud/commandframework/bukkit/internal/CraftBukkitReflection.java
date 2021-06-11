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
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utilities for doing reflection on CraftBukkit, used by the cloud implementation.
 *
 * <p>This is not API to any extent, and as such, may break, change, or be removed without any notice.</p>
 */
@Beta
public final class CraftBukkitReflection {

    private static final String PREFIX_NMS = "net.minecraft.server";
    private static final String PREFIX_MC = "net.minecraft.";
    private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
    private static final String CRAFT_SERVER = "CraftServer";
    private static final String VERSION;
    public static final int MAJOR_REVISION;

    static {
        final Class<?> serverClass = Bukkit.getServer().getClass();
        final String pkg = serverClass.getPackage().getName();
        final String nmsVersion = pkg.substring(pkg.lastIndexOf(".") + 1);
        if (!nmsVersion.contains("_")) {
            MAJOR_REVISION = -1;
            VERSION = null;
        } else {
            MAJOR_REVISION = Integer.parseInt(nmsVersion.split("_")[1]);
            String name = serverClass.getName();
            name = name.substring(PREFIX_CRAFTBUKKIT.length());
            name = name.substring(0, name.length() - CRAFT_SERVER.length());
            VERSION = name;
        }
    }

    public static boolean craftBukkit() {
        return MAJOR_REVISION != -1 && VERSION != null;
    }

    public static @NonNull Class<?> needNMSClassOrElse(
            final @NonNull String nms,
            final @NonNull String... classNames
    ) throws RuntimeException {
        final Class<?> nmsClass = findNMSClass(nms);
        if (nmsClass != null) {
            return nmsClass;
        }
        for (final String name : classNames) {
            final Class<?> maybe = findClass(name);
            if (maybe != null) {
                return maybe;
            }
        }
        throw new IllegalStateException(String.format(
                "Couldn't find a class! NMS: '%s' or '%s'.",
                nms,
                Arrays.toString(classNames)
        ));
    }

    public static @NonNull Class<?> needMCClass(final @NonNull String name) throws RuntimeException {
        return needClass(PREFIX_MC + name);
    }

    public static @NonNull Class<?> needNMSClass(final @NonNull String className) throws RuntimeException {
        return needClass(PREFIX_NMS + VERSION + className);
    }

    public static @NonNull Class<?> needOBCClass(final @NonNull String className) throws RuntimeException {
        return needClass(PREFIX_CRAFTBUKKIT + VERSION + className);
    }

    public static @Nullable Class<?> findMCClass(final @NonNull String name) throws RuntimeException {
        return findClass(PREFIX_MC + name);
    }

    public static @Nullable Class<?> findNMSClass(final @NonNull String className) throws RuntimeException {
        return findClass(PREFIX_NMS + VERSION + className);
    }

    public static @Nullable Class<?> findOBCClass(final @NonNull String className) throws RuntimeException {
        return findClass(PREFIX_CRAFTBUKKIT + VERSION + className);
    }

    public static @NonNull Class<?> needClass(final @NonNull String className) throws RuntimeException {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable Class<?> findClass(final @NonNull String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    public static @NonNull Field needField(final @NonNull Class<?> holder, final @NonNull String name) throws RuntimeException {
        try {
            final Field field = holder.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NonNull Constructor<?> needConstructor(final @NonNull Class<?> holder, final @NonNull Class<?>... parameters) {
        try {
            return holder.getDeclaredConstructor(parameters);
        } catch (final NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean classExists(final @NonNull String className) {
        return findClass(className) != null;
    }

    public static @NonNull Method needMethod(
            final @NonNull Class<?> holder,
            final @NonNull String name,
            final @NonNull Class<?>... params
    ) throws RuntimeException {
        try {
            return holder.getMethod(name, params);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private CraftBukkitReflection() {
    }

}
