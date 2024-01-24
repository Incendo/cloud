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
package org.incendo.cloud.annotations.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.suggestion.SuggestionProvider;

/**
 * This annotation allows you to create annotated methods that behave like argument parsers.
 *
 * <p>The method parameters can be any combination of: <ul>
 *  <li>{@link CommandContext}</li>
 *  <li>{@link CommandInput}</li>
 *  <li>the command sender type</li>
 *  <li>any type that can be injected using {@link CommandContext#inject(Class)}</li>
 * </ul>
 *
 * <p>The method can throw exceptions, and the thrown exceptions will automatically be
 * wrapped by a {@link ArgumentParseResult#failure(Throwable)}.</p>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {

    /**
     * Returns name of the parser.
     *
     * <p>If this is left empty, the parser will
     * be registered as a default parser for the return type of the method.</p>
     *
     * @return Parser name
     */
    String name() default "";

    /**
     * Returns the of the suggestion provider to use.
     *
     * <p>If the string is left empty, the default
     * provider for the {@link org.incendo.cloud.annotations.Argument} will be used. Otherwise,
     * the {@link ParserRegistry} instance in the
     * {@link org.incendo.cloud.CommandManager} will be queried for a matching suggestion provider.</p>
     *
     * <p>For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link ParserRegistry#registerSuggestionProvider(String, SuggestionProvider)}.
     * The registry instance can be retrieved using {@link org.incendo.cloud.CommandManager#parserRegistry()}.</p>
     *
     * @return The name of the suggestion provider, or {@code ""}
     */
    String suggestions() default "";
}
