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
package cloud.commandframework;

import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SenderMapperImpl<A, B> implements SenderMapper<A, B> {

    static final SenderMapper<?, ?> IDENTITY =
            new SenderMapperImpl<>(Function.identity(), Function.identity());

    private final @NonNull Function<@NonNull A, @NonNull B> map;
    private final @NonNull Function<@NonNull B, @NonNull A> reverse;

    SenderMapperImpl(
            final @NonNull Function<@NonNull A, @NonNull B> map,
            final @NonNull Function<@NonNull B, @NonNull A> reverse
    ) {
        this.map = Objects.requireNonNull(map, "map function");
        this.reverse = Objects.requireNonNull(reverse, "reverse function");
    }

    @Override
    public @NonNull B map(final @NonNull A base) {
        return this.map.apply(base);
    }

    @Override
    public @NonNull A reverse(final @NonNull B mapped) {
        return this.reverse.apply(mapped);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SenderMapperImpl<?, ?> that = (SenderMapperImpl<?, ?>) o;
        return Objects.equals(this.map, that.map) && Objects.equals(this.reverse, that.reverse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map, this.reverse);
    }

    @Override
    public String toString() {
        return "SenderMapperImpl{"
                + "map=" + this.map
                + ", reverse=" + this.reverse
                + '}';
    }
}
