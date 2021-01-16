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

package cloud.commandframework.meta;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

final class SimpleKey<V> implements CommandMeta.Key<V> {

    private final @NonNull TypeToken<V> valueType;
    private final @NonNull String name;
    private final @Nullable Function<@NonNull CommandMeta, @Nullable V> derivationFunction;

    SimpleKey(
        final @NonNull TypeToken<V> valueType,
        final @NonNull String name,
        final @Nullable Function<@NonNull CommandMeta, @Nullable V> derivationFunction
    ) {
        this.valueType = valueType;
        this.name = name;
        this.derivationFunction = derivationFunction;
    }

    @Override
    public @NonNull TypeToken<V> getValueType() {
        return this.valueType;
    }

    @Override
    public @NonNull String getName() {
        return this.name;
    }

    @Override
    public @Nullable Function<@NonNull CommandMeta, @Nullable V> getFallbackDerivation() {
        return this.derivationFunction;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final SimpleKey<?> that = (SimpleKey<?>) other;
        return this.valueType.equals(that.valueType)
                && this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return 7 * GenericTypeReflector.hashCode(this.valueType.getAnnotatedType())
                + 31 * this.name.hashCode();
    }

}
