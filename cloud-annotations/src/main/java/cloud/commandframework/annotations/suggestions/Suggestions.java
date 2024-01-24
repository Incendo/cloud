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
package cloud.commandframework.annotations.suggestions;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.suggestion.Suggestion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This annotation allows you to create annotated methods that behave like suggestion providers.
 *
 * <p>The method parameters can be any combination of: <ul>
 *  <li>{@link CommandContext}</li>
 *  <li>{@link CommandInput}</li>
 *  <li>{@link String}, which receives {@link CommandInput#lastRemainingToken()}</li>
 *  <li>the command sender type</li>
 *  <li>any type that can be injected using {@link CommandContext#inject(Class)}</li>
 * </ul>
 *
 * <p>The return type must be an {@link Iterable} or a {@link Stream} of {@link String} or {@link Suggestion}
 * or a future that completes with any of the supported return types.</p>
 *
 * <p>Example signatures: <pre>{@code
 * ﹫Suggestions("name")
 * public List<String> methodName(CommandContext<YourSender> sender, CommandInput input)}</pre>
 * <pre>{@code
 * ﹫Suggestions("name")
 * public List<Suggestion> methodName(CommandContext<YourSender> sender, CommandInput input)}</pre>
 * <pre>{@code
 * ﹫Suggestions("name")
 * public Stream<Suggestion> methodName(CommandContext<YourSender> sender, String input)}</pre>
 *
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Suggestions {

    /**
     * Returns the name of the suggestion provider.
     *
     * <p>This should be the same as the name specified in your command arguments.</p>
     *
     * @return suggestion provider name
     */
    @NonNull String value();
}
