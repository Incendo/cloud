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
package cloud.commandframework.paper;

import cloud.commandframework.bukkit.BukkitBrigadierMapper;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.paper.parser.KeyedWorldParser;
import io.leangen.geantyref.TypeToken;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Brigadier mappings for Paper native Brigadier. This is currently only used when the PaperBrigadierListener is in use,
 * not when the CloudCommodoreManager is in use on Paper. This is because all argument types registered here require
 * Paper 1.15+ anyways.
 *
 * @param <C> sender type
 */
final class PaperBrigadierMapper<C> {

    PaperBrigadierMapper(
            final @NonNull BukkitBrigadierMapper<C> mapper
    ) {
        this.registerMappings(mapper);
    }

    private void registerMappings(final @NonNull BukkitBrigadierMapper<C> mapper) {
        final Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
        if (keyed != null && keyed.isAssignableFrom(World.class)) {
            mapper.mapSimpleNMS(new TypeToken<KeyedWorldParser<C>>() {
            }, "resource_location", true);
        }
    }
}
