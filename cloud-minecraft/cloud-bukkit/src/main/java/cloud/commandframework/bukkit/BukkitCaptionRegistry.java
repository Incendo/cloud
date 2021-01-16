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
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_MATERIAL}
     */
    public static final String ARGUMENT_PARSE_FAILURE_MATERIAL = "'{input}' is not a valid material name";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = "No player found for input '{input}'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '{input}'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_WORLD}
     */
    public static final String ARGUMENT_PARSE_FAILURE_WORLD = "'{input}' is not a valid Minecraft world";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED = "Selector '{input}' is malformed.";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED =
            "Entity selector argument type not supported below Minecraft 1.13.";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS = "More than 1 player selected in single player selector";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES = "More than 1 entity selected in single entity selector.";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER = "Non-player(s) selected in player selector.";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT}
     */
    public static final String ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT =
            "'{input}' is not a valid location. Required format is '<x> <y> <z>'";
    /**
     * Default caption for {@link BukkitCaptionKeys#ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE}
     */
    public static final String ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE =
            "Cannot mix local and absolute coordinates. (either all coordinates use '^' or none do)";

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
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT
        );
        this.registerMessageFactory(
                BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE
        );
    }

}
