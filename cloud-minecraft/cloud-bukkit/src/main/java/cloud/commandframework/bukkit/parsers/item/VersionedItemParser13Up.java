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
package cloud.commandframework.bukkit.parsers.item;

import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.context.CommandContext;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ItemStack parser for 1.13 - 1.16
 */
class VersionedItemParser13Up implements VersionedItemParser {

    private List<String> allItemNames = null;

    @Override
    @SuppressWarnings("rawtypes")
    public @NonNull ItemStackParseResult parseItemStack(
            @NonNull final CommandContext<?> context,
            @NonNull final String input
    ) {
        String modInput = input;
        if (!input.startsWith("minecraft:")) {
            modInput = "minecraft:" + input;
        }
        String key = modInput;
        String nbt = "";
        if (modInput.contains("{")) {
            key = modInput.substring(0, modInput.indexOf("{"));
            nbt = modInput.replace(key, "");
        }
        try {
            // construct a MinecraftKey
            Class<?> minecraftKeyClass = getNMSClass("MinecraftKey");
            Constructor constructor = minecraftKeyClass.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            Object minecraftKey = constructor.newInstance(key);

            // obtain the registry
            Class<?> iRegistryClass = getNMSClass("IRegistry");
            Field itemsField = iRegistryClass.getDeclaredField("ITEM");
            itemsField.setAccessible(true);
            Object itemsRegistry = itemsField.get(null);

            Method getMethod = iRegistryClass.getDeclaredMethod("get", minecraftKeyClass);
            getMethod.setAccessible(true);

            // get an item object
            Object itemObject = getMethod.invoke(itemsRegistry, minecraftKey);
            if (itemObject == null) {
                // invalid item :(
                return ItemStackParseResult.failure(
                        new ItemStackArgument.ItemStackParseException(
                                key,
                                context,
                                BukkitCaptionKeys.ARGUMENT_PARSE_ITEM_UNKNOWN
                        )
                );
            }

            Class<?> itemStackClass = getNMSClass("ItemStack");
            Class<?> iMaterialClass = getNMSClass("IMaterial");
            Constructor itemStackConstructor = itemStackClass.getDeclaredConstructor(iMaterialClass);
            itemStackConstructor.setAccessible(true);

            // get an ItemStack object
            Object itemStackObject = itemStackConstructor.newInstance(itemObject);
            if (!nbt.isEmpty()) {
                // now to set the NBT
                Class<?> mojangsonParserClass = getNMSClass("MojangsonParser");
                Method parseMethod = mojangsonParserClass.getDeclaredMethod("parse", String.class);
                parseMethod.setAccessible(true);

                Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
                Method setTagMethod = itemStackClass.getDeclaredMethod("setTag", nbtTagCompoundClass);
                setTagMethod.setAccessible(true);
                try {
                    setTagMethod.invoke(itemStackObject, parseMethod.invoke(null, nbt));
                } catch (Exception e) { // MojangsonParseException but we can't reference it here
                    return ItemStackParseResult.failure(
                            new ItemStackArgument.ItemStackParseException(
                                    nbt,
                                    context,
                                    BukkitCaptionKeys.ARGUMENT_PARSE_INVALID_NBT
                            )
                    );
                }
            }

            // convert nms.ItemStack into Bukkit API ItemStack
            Class<?> craftItemStackClass = getOBCClass("inventory.CraftItemStack");
            Method asBukkitCopyMethod = craftItemStackClass.getDeclaredMethod("asBukkitCopy", itemStackClass);
            asBukkitCopyMethod.setAccessible(true);
            return ItemStackParseResult.success((ItemStack) asBukkitCopyMethod.invoke(null, itemStackObject));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
                |
                InvocationTargetException
                |
                NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull List<@NonNull String> allItemNames() {
        if (allItemNames != null) {
            return allItemNames;
        }
        List<String> ret = new ArrayList<>();
        try {
            Class<?> iRegistryClass = getNMSClass("IRegistry");
            Field itemsField = iRegistryClass.getDeclaredField("ITEM");
            itemsField.setAccessible(true);

            Object itemsRegistry = itemsField.get(null);
            Method keySetMethod = iRegistryClass.getDeclaredMethod("keySet");
            keySetMethod.setAccessible(true);
            Set<Object> keys = (Set<Object>) keySetMethod.invoke(itemsRegistry);
            for (Object key : keys) {
                String namespacedKey = key.toString();
                ret.add(namespacedKey);
                String itemName = namespacedKey.split(":")[1];
                ret.add(itemName);
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | InvocationTargetException
                |
                NoSuchMethodException e) {
            e.printStackTrace();
        }
        allItemNames = ret;
        return ret;
    }

}
