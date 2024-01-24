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

import cloud.commandframework.util.annotation.AnnotationAccessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class AnnotationAccessorTest {

    private static final String QUALIFIER_TEST_STRING_PARAMETER = "parameter";
    private static final String QUALIFIER_TEST_STRING_METHOD = "method";

    @Qualifier(QUALIFIER_TEST_STRING_METHOD)
    public static void annotatedMethod(@Qualifier(QUALIFIER_TEST_STRING_PARAMETER) final String parameter) {
    }

    @Test
    void testQualifierResolutionOrder() throws Exception {
        // Given
        final Method method = AnnotationAccessorTest.class.getMethod("annotatedMethod", String.class);
        final Parameter parameter = method.getParameters()[0];
        final AnnotationAccessor accessor = AnnotationAccessor.of(
                AnnotationAccessor.of(parameter),
                AnnotationAccessor.of(method)
        );

        // When
        final Qualifier qualifier = accessor.annotation(Qualifier.class);

        // Then
        assertThat(qualifier).isNotNull();
        assertThat(qualifier.value()).isEqualTo(QUALIFIER_TEST_STRING_PARAMETER);
    }

    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Qualifier {

        String value();
    }
}
