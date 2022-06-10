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

import java.util.function.Predicate;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link Predicate} for {@link Block Blocks} in a {@link World}, parsed from user input.
 *
 * <p>By default, a parsed {@link BlockPredicate} will not load chunks to perform tests. It will simply
 * return {@code false} when attempting to test a block in unloaded chunks.</p>
 *
 * <p>To get a {@link BlockPredicate} which will load chunks, use {@link #loadChunks()}.</p>
 *
 * @since 1.5.0
 */
public interface BlockPredicate extends Predicate<Block> {

    /**
     * Get a version of this {@link BlockPredicate} which will load chunks in order to perform
     * tests.
     *
     * <p>If this {@link BlockPredicate} already loads chunks, it will simply return itself.</p>
     *
     * @return a {@link BlockPredicate} which loads chunks
     * @since 1.5.0
     */
    @NonNull BlockPredicate loadChunks();
}
