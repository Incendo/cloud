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

package cloud.commandframework.quilt;

import cloud.commandframework.captions.SimpleCaptionRegistry;

/**
 * Caption registry that uses bi-functions to produce messages
 *
 * @param <C> Command sender type
 * @since 1.5.0
 */
public class QuiltCaptionRegistry<C> extends SimpleCaptionRegistry<C> {

    /**
     * Default caption for {@link QuiltCaptionKeys#ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY}
     *
     * @since 1.5.0
     */
    public static final String ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY = "Could not find key {id} in registry '{registry}'";
    /**
     * Default caption for {@link QuiltCaptionKeys#ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN}
     *
     * @since 1.5.0
     */
    public static final String ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN = "Could not find any team named '{input}'!";

    protected QuiltCaptionRegistry() {
        super();

        this.registerMessageFactory(
                QuiltCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_UNKNOWN_ENTRY
        );
        this.registerMessageFactory(
                QuiltCaptionKeys.ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN
        );
    }


}
