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
package cloud.commandframework.sponge7;

import cloud.commandframework.meta.CommandMeta;
import org.spongepowered.api.text.Text;

/**
 * Metadata keys used when registering commands targeting Sponge.
 *
 * @since 1.4.0
 */
public final class SpongeMetaKeys {

    private SpongeMetaKeys() {
    }

    /**
     * A rich short description for commands.
     */
    public static final CommandMeta.Key<Text> RICH_DESCRIPTION = CommandMeta.Key.of(Text.class, "cloud:sponge7/rich_description");

    /**
     * A rich long description for commands.
     */
    public static final CommandMeta.Key<Text> RICH_LONG_DESCRIPTION = CommandMeta.Key.of(
            Text.class,
            "cloud:sponge7/rich_long_description"
    );

}
