//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.services;

import cloud.commandframework.services.types.Service;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Predicate;

enum ServiceFilterHandler {
    INSTANCE;

    <Context> boolean passes(
            final @NonNull ServiceRepository<Context, ?>.ServiceWrapper<? extends Service<Context, ?>> service,
            final @NonNull Context context
    ) {
        if (!service.isDefaultImplementation()) {
            for (final Predicate<Context> predicate : service.getFilters()) {
                try {
                    if (!predicate.test(context)) {
                        return false;
                    }
                } catch (final Exception e) {
                    throw new PipelineException(String
                            .format("Failed to evaluate filter '%s' for '%s'",
                                    predicate.getClass().getCanonicalName(), service.toString()
                            ), e);
                }
            }
        }
        return true;
    }

}
