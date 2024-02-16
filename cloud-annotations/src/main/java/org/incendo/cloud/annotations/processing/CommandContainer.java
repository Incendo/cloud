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
package org.incendo.cloud.annotations.processing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Command;

/**
 * Indicates that the class contains
 * {@link Command command metods}.
 * <p>
 * If using <i>cloud-annotations</i> as an annotation processor, then the class will
 * be listed in a special file under META-INF. These containers can be collectively
 * parsed using {@link AnnotationParser#parseContainers()}, which will create instances
 * of the containers and then call {@link AnnotationParser#parse(Object[])} with the created instance.
 * <p>
 * Every class annotated with {@link CommandContainer} needs to be {@code public}, and it
 * also needs to have one of the following:
 * <ul>
 *     <li>A {@code public} no-arg constructor</li>
 *     <li>A {@code public} constructor with {@link AnnotationParser} as the sole parameter</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> For container parsing to work, you need to make sure that <i>cloud-annotations</i> is added
 * as an annotation processor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandContainer {

    String ANNOTATION_PATH = "org.incendo.cloud.annotations.processing.CommandContainer";

    /**
     * Returns the priority of the container.
     *
     * <p>A container with a higher priority will get parsed earlier than a container with a lower priority.</p>
     *
     * @return the priority
     */
    int priority() default 1;
}
