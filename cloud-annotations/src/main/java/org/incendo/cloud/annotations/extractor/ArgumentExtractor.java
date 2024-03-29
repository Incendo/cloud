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
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotations.SyntaxFragment;
import org.incendo.cloud.annotations.descriptor.ArgumentDescriptor;

/**
 * Extracts {@link ArgumentDescriptor argument descriptors} from {@link Method methods}.
 *
 */
@API(status = API.Status.STABLE)
public interface ArgumentExtractor {

    /**
     * Extracts the arguments from the given {@code method}.
     *
     * @param syntax the syntax of the command
     * @param method the method
     * @return the extracted arguments
     */
    @NonNull Collection<@NonNull ArgumentDescriptor> extractArguments(
            @NonNull List<@NonNull SyntaxFragment> syntax,
            @NonNull Method method
    );
}
