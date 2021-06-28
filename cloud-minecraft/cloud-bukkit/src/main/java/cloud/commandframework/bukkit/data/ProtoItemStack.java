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
package cloud.commandframework.bukkit.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Intermediary result for an argument which parses a {@link Material} and optional NBT data.
 *
 * @since 1.5.0
 */
public interface ProtoItemStack {

    /**
     * Get the {@link Material} of this {@link ProtoItemStack}.
     *
     * @return the {@link Material}
     * @since 1.5.0
     */
    @NonNull Material material();

    /**
     * Get whether this {@link ProtoItemStack} contains extra data besides the {@link Material}.
     *
     * @return whether there is extra data
     * @since 1.5.0
     */
    boolean hasExtraData();

    /**
     * Create a new {@link ItemStack} from the state of this {@link ProtoItemStack}.
     *
     * @param stackSize               stack size
     * @param respectMaximumStackSize whether to respect the maximum stack size for the material
     * @return the created {@link ItemStack}
     * @throws IllegalArgumentException if the {@link ItemStack} could not be created, due to max stack size or other reasons
     * @since 1.5.0
     */
    @NonNull ItemStack createItemStack(int stackSize, boolean respectMaximumStackSize)
            throws IllegalArgumentException;

}
