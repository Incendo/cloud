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
import java.util.Map;

/**
 * ItemStack parser for 1.8 - 1.12
 */
class VersionedItemParserTo12 implements VersionedItemParser {

    private List<String> allItemNames = null;

    @Override
    @NonNull
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ItemStackParseResult parseItemStack(
            final @NonNull CommandContext<?> context,
            final @NonNull String input
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

            Class<?> item = getNMSClass("Item");
            Field registryField = item.getDeclaredField("REGISTRY");
            registryField.setAccessible(true);

            Object registryMaterials = registryField.get(null);
            // this is a workaround since reflection doesn't want to recognize get(MinecraftKey)
            Class<?> registrySimpleClass = getNMSClass("RegistrySimple");
            Field registryMapField = registrySimpleClass.getDeclaredField("c");
            registryMapField.setAccessible(true);
            Map<Object, Object> map = (Map<Object, Object>) registryMapField.get(registryMaterials);

            // get an item object
            Object itemObject = map.get(minecraftKey);
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

            // get an ItemStack object
            Class<?> itemStackClass = getNMSClass("ItemStack");
            Constructor itemStackConstructor = itemStackClass.getDeclaredConstructor(item);
            itemStackConstructor.setAccessible(true);

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
    @NonNull
    @SuppressWarnings("unchecked")
    public List<@NonNull String> allItemNames() {
        if (allItemNames != null) {
            return allItemNames;
        }
        List<String> ret = new ArrayList<>();
        try {
            Class<?> itemClass = getNMSClass("Item");
            Field registryField = itemClass.getDeclaredField("REGISTRY");
            registryField.setAccessible(true);

            Object registry = registryField.get(null);
            Class<?> registrySimpleClass = getNMSClass("RegistrySimple");
            Field registryMapField = registrySimpleClass.getDeclaredField("c");
            registryMapField.setAccessible(true);
            Map<Object, Object> map = (Map<Object, Object>) registryMapField.get(registry);
            for (Object keyObject : map.keySet()) {
                String namespacedKey = keyObject.toString();
                ret.add(namespacedKey);
                String itemName = namespacedKey.split(":")[1];
                ret.add(itemName);
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        allItemNames = ret;
        return ret;
    }

}
