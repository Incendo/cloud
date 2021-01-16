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
package cloud.commandframework.minecraft.extras;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

final class Pagination<T> {

    private final BiFunction<Integer, Integer, List<Component>> headerRenderer;
    private final BiFunction<T, Boolean, Component> rowRenderer;
    private final BiFunction<Integer, Integer, Component> footerRenderer;
    private final BiFunction<Integer, Integer, Component> outOfRangeRenderer;

    Pagination(
            final @NonNull BiFunction<Integer, Integer, List<Component>> headerRenderer,
            final @NonNull BiFunction<T, Boolean, Component> rowRenderer,
            final @NonNull BiFunction<Integer, Integer, Component> footerRenderer,
            final @NonNull BiFunction<Integer, Integer, Component> outOfRangeRenderer
    ) {
        this.headerRenderer = headerRenderer;
        this.rowRenderer = rowRenderer;
        this.footerRenderer = footerRenderer;
        this.outOfRangeRenderer = outOfRangeRenderer;
    }

    @NonNull List<Component> render(
            final @NonNull List<T> content,
            final int page,
            final int itemsPerPage
    ) {
        final int pages = (int) Math.ceil(content.size() / (itemsPerPage * 1.00));
        if (page < 1 || page > pages) {
            return Collections.singletonList(outOfRangeRenderer.apply(page, pages));
        }

        final List<Component> renderedContent = new ArrayList<>(this.headerRenderer.apply(page, pages));

        final int start = itemsPerPage * (page - 1);
        final int maxIndex = (start + itemsPerPage);
        for (int index = start; index < maxIndex; index++) {
            if (index > content.size() - 1) {
                break;
            }
            renderedContent.add(this.rowRenderer.apply(content.get(index), index == maxIndex - 1));
        }

        renderedContent.add(this.footerRenderer.apply(page, pages));

        return Collections.unmodifiableList(renderedContent);
    }

}
