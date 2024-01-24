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
package org.incendo.cloud.caption;

import java.util.LinkedList;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(status = API.Status.INTERNAL)
public final class CaptionRegistryImpl<C> implements CaptionRegistry<C> {

    private final LinkedList<@NonNull CaptionProvider<C>> providers = new LinkedList<>();

    CaptionRegistryImpl() {
    }

    @Override
    public @NonNull String caption(
            final @NonNull Caption caption,
            final @NonNull C sender
    ) {
        for (final CaptionProvider<C> provider : this.providers) {
            final String result = provider.provide(caption, sender);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalArgumentException(String.format("There is no caption stored with key '%s'", caption));
    }

    @Override
    public @This @NonNull CaptionRegistry<C> registerProvider(
            final @NonNull CaptionProvider<C> provider
    ) {
        this.providers.addFirst(provider);
        return this;
    }
}
