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
package cloud.commandframework.minestom;

import cloud.commandframework.captions.SimpleCaptionRegistry;

/**
 * Caption registry that uses bi-functions to produce messages.
 *
 * @param <C> Command sender type
 * @since 1.9.0
 */
public class MinestomCaptionRegistry<C> extends SimpleCaptionRegistry<C> {

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '{input}'";

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_ENTITY_TYPE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_ENTITY_TYPE = "'{input}' is not a valid entity type";

    protected MinestomCaptionRegistry() {
        super();

        this.registerMessageFactory(
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_PLAYER
        );
        this.registerMessageFactory(
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_ENTITY_TYPE,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_ENTITY_TYPE
        );
    }
}
