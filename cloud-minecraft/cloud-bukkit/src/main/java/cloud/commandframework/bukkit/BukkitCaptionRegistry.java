//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.bukkit;

import cloud.commandframework.captions.SimpleCaptionRegistry;

/**
 * Caption registry that uses bi-functions to produce messages
 *
 * @param <C> Command sender type
 */
public class BukkitCaptionRegistry<C> extends SimpleCaptionRegistry<C> {

    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_ENCHANTMENT = "'{input}' is not a valid enchantment";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_MATERIAL = "'{input}' is not a valid material name";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = "No player found for input '{input}'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '{input}'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_ENCHANTMENT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_WORLD = "'{input}' is not a valid Minecraft world";

    protected BukkitCaptionRegistry() {
        super();
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ENCHANTMENT,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_ENCHANTMENT
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_MATERIAL,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_MATERIAL
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_PLAYER
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_WORLD
        );
    }

}
