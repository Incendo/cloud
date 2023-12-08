//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations;

import java.lang.reflect.Parameter;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface ParameterNameExtractor {

    /**
     * Returns a parameter name extractor that returns {@link Parameter#getName()} without any transformations.
     *
     * @return the extractor
     */
    static @NonNull ParameterNameExtractor simple() {
        return Parameter::getName;
    }

    /**
     * Returns a parameter name extractor that transforms {@link Parameter#getName()} using the given {@code transformation}.
     *
     * @param transformation the name transformation
     * @return the transformed name
     */
    static @NonNull ParameterNameExtractor withTransformation(@NonNull Function<String, String> transformation) {
        return parameter -> transformation.apply(parameter.getName());
    }

    /**
     * Extracts the name from the given {@code parameter}.
     *
     * @param parameter the parameter
     * @return the extracted name
     */
    @NonNull String extract(@NonNull Parameter parameter);
}
