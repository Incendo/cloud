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
package org.incendo.cloud.annotations.extractor;

import java.lang.reflect.Method;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotations.assembler.FlagAssembler;
import org.incendo.cloud.annotations.descriptor.FlagDescriptor;

/**
 * Extractor that extracts {@link FlagDescriptor flag descriptors} from command methods.
 * <p>
 * The flag instances are then assembled by a {@link FlagAssembler}.
 *
 */
@API(status = API.Status.STABLE)
public interface FlagExtractor {

    /**
     * Extracts the flags from the given {@code method}.
     *
     * @param method the method
     * @return the extracted flags
     */
    @NonNull Collection<@NonNull FlagDescriptor> extractFlags(@NonNull Method method);
}
