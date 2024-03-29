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
package org.incendo.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotations.extractor.ParameterNameExtractor;
import org.incendo.cloud.annotations.extractor.StandardArgumentExtractor;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.suggestion.SuggestionProvider;

/**
 * Annotation used to indicate that a method parameter is a command argument.
 * <p>
 * This annotation is optional if the parameter names are preserved during compilation, and the parameter
 * name matches the corresponding syntax fragment.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * The name of the argument that this parameter is bound to. This value must be overridden unless you have explicitly enabled
     * the preservation of parameter names in your compiler options.
     * <p>
     * If the parameter names are preserved and the name of the bound argument is the same as the parameter name, the default
     * value may be used. The mapping between parameter names and argument names can be overridden by supplying a custom
     * {@link ParameterNameExtractor} to a {@link StandardArgumentExtractor}.
     *
     * @return Argument name
     */
    @NonNull String value() default AnnotationParser.INFERRED_ARGUMENT_NAME;

    /**
     * Name of the argument parser
     *
     * @return Argument name
     */
    @NonNull String parserName() default "";

    /**
     * Name of the suggestion provider to use. If the string is left empty, the default
     * provider for the argument parser will be used. Otherwise,
     * the {@link ParserRegistry} instance in the
     * {@link org.incendo.cloud.CommandManager} will be queried for a matching suggestion provider.
     * <p>
     * For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link ParserRegistry#registerSuggestionProvider(String, SuggestionProvider)}.
     * The registry instance can be retrieved using {@link org.incendo.cloud.CommandManager#parserRegistry()}.
     *
     * @return The name of the suggestion provider, or {@code ""} if the default suggestion provider for the argument parser
     *         should be used instead
     */
    @NonNull String suggestions() default "";

    /**
     * The argument description
     *
     * @return Argument description
     */
    @NonNull String description() default "";
}
