//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations.parsers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

/**
 * This annotation allows you to create annotated methods that behave like argument parsers.
 * The method must have this exact signature: <pre>{@code
 * ﹫Parser("name") // Name may be left out
 * public ParsedType methodName(CommandContext<YourSender> sender, Queue<String> input) {
 * }}</pre>
 * <p>
 * The method can throw exceptions, and the thrown exceptions will automatically be
 * wrapped by a {@link cloud.commandframework.arguments.parser.ArgumentParseResult#failure(Throwable)}
 *
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {

    /**
     * The name of the parser. If this is left empty, the parser will
     * be registered as a default parser for the return type of the method
     *
     * @return Parser name
     */
    String name() default "";

    /**
     * Name of the suggestions provider to use. If the string is left empty, the default
     * provider for the {@link cloud.commandframework.annotations.Argument} will be used. Otherwise,
     * the {@link cloud.commandframework.arguments.parser.ParserRegistry} instance in the
     * {@link cloud.commandframework.CommandManager} will be queried for a matching suggestion provider.
     * <p>
     * For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link cloud.commandframework.arguments.parser.ParserRegistry#registerSuggestionProvider(String, BiFunction)}.
     * The registry instance can be retrieved using {@link cloud.commandframework.CommandManager#getParserRegistry()}.
     *
     * @return The name of the suggestion provider, or {@code ""}
     */
    String suggestions() default "";

}
