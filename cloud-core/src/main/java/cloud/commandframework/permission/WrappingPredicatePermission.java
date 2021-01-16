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
package cloud.commandframework.permission;

import cloud.commandframework.keys.CloudKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Predicate;

final class WrappingPredicatePermission<C> implements PredicatePermission<C> {

    private final CloudKey<Void> key;
    private final Predicate<C> predicate;

    WrappingPredicatePermission(
            final @NonNull CloudKey<Void> key,
            final @NonNull Predicate<C> predicate
    ) {
        this.key = key;
        this.predicate = predicate;
    }

    @Override
    public boolean hasPermission(final C sender) {
        return this.predicate.test(sender);
    }

    @Override
    public @NonNull CloudKey<Void> getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key.getName();
    }

}
