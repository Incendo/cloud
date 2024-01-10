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
package cloud.commandframework.annotations.method;

import cloud.commandframework.annotations.Descriptor;
import cloud.commandframework.internal.ImmutableImpl;
import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

@ImmutableImpl
@Value.Immutable
@API(status = API.Status.INTERNAL, since = "2.0.0")
public interface ParameterValue {

    /**
     * Creates a new parameter value.
     *
     * @param parameter parameter
     * @param value     value
     * @return the value
     */
    static @NonNull ParameterValue of(
            final @NonNull Parameter parameter,
            final @Nullable Object value
    ) {
        return of(parameter, value, null /* descriptor */);
    }

    /**
     * Creates a new parameter value.
     *
     * @param parameter  parameter
     * @param value      value
     * @param descriptor optional descriptor
     * @return the value
     */
    static @NonNull ParameterValue of(
            final @NonNull Parameter parameter,
            final @Nullable Object value,
            final @Nullable Descriptor descriptor
    ) {
        return ParameterValueImpl.of(parameter, value, descriptor);
    }

    /**
     * Returns the parameter.
     *
     * @return the parameter
     */
    @NonNull Parameter parameter();

    /**
     * Returns the parameter value.
     *
     * @return the value
     */
    @Nullable Object value();

    /**
     * Returns the associated descriptor.
     *
     * @return the descriptor, or {@code null}
     */
    @Nullable Descriptor descriptor();
}
