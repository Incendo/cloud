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
package cloud.commandframework.internal;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Tokenizer that splits command inputs into tokens. This will split the string
 * at every blank space. If the input string ends with a blank space, a trailing
 * empty string will be added to the token list
 */
public final class CommandInputTokenizer {

    private static final String DELIMITER = " ";
    private static final String EMPTY = "";

    private final StringTokenizerFactory stringTokenizerFactory = new StringTokenizerFactory();
    private final String input;

    /**
     * Create a new input tokenizer
     *
     * @param input Input that is to be turned into tokens
     */
    public CommandInputTokenizer(final @NonNull String input) {
        this.input = input;
    }

    /**
     * Turn the input into tokens
     *
     * @return Linked list containing the tokenized input
     */
    public @NonNull LinkedList<@NonNull String> tokenize() {
        final StringTokenizer stringTokenizer = stringTokenizerFactory.createStringTokenizer();
        final LinkedList<String> tokens = new LinkedList<>();
        while (stringTokenizer.hasMoreElements()) {
            tokens.add(stringTokenizer.nextToken());
        }
        if (input.endsWith(DELIMITER)) {
            tokens.add(EMPTY);
        }
        return tokens;
    }


    /**
     * Factory class that creates {@link StringTokenizer} instances
     */
    private final class StringTokenizerFactory {

        private @NonNull StringTokenizer createStringTokenizer() {
            return new StringTokenizer(input, DELIMITER);
        }

    }

}
