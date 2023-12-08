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
package cloud.commandframework.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.immutables.annotate.InjectAnnotation;
import org.immutables.value.Value;

/**
 * Annotation that generates immutable classes suffixed with "Impl".
 */
@Value.Style(
        typeImmutable = "*Impl",
        typeImmutableEnclosing = "*",
        typeAbstract = "*",
        deferCollectionAllocation = true,
        optionalAcceptNullable = true,
        jdkOnly = true, // We do not want any runtime dependencies!
        allParameters = true,
        headerComments = true,
        jacksonIntegration = false,
        visibility = Value.Style.ImplementationVisibility.PACKAGE,
        builderVisibility = Value.Style.BuilderVisibility.PACKAGE,
        defaults = @Value.Immutable(builder = false)
)
@InjectAnnotation(
        type = API.class,
        target = InjectAnnotation.Where.IMMUTABLE_TYPE,
        code = "(status = org.apiguardian.api.API.Status.INTERNAL, consumers = \"cloud.commandframework.*\")"
)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
@API(status = API.Status.INTERNAL, since = "2.0.0")
public @interface ImmutableImpl {

}
