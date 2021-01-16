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

import cloud.commandframework.captions.Caption;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * {@link Caption} instances for messages in cloud-bukkit
 */
public final class BukkitCaptionKeys {

    private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();

    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_ENCHANTMENT = of("argument.parse.failure.enchantment");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_MATERIAL = of("argument.parse.failure.material");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = of("argument.parse.failure.offlineplayer");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_PLAYER = of("argument.parse.failure.player");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_WORLD = of("argument.parse.failure.world");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_MALFORMED = of("argument.parse.failure.selector.malformed");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED = of("argument.parse.failure.selector.unsupported");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_PLAYERS = of(
            "argument.parse.failure.selector.too_many_players");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_TOO_MANY_ENTITIES = of(
            "argument.parse.failure.selector.too_many_entities");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_NON_PLAYER = of(
            "argument.parse.failure.selector.non_player_in_player_selector");
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT = of(
            "argument.parse.failure.location.invalid_format");
    /**
     * Variables: None
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE = of(
            "argument.parse.failure.location.mixed_local_absolute");

    private BukkitCaptionKeys() {
    }

    private static @NonNull Caption of(final @NonNull String key) {
        final Caption caption = Caption.of(key);
        RECOGNIZED_CAPTIONS.add(caption);
        return caption;
    }

    /**
     * Get an immutable collection containing all standard caption keys
     *
     * @return Immutable collection of keys
     */
    public static @NonNull Collection<@NonNull Caption> getBukkitCaptionKeys() {
        return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
    }

}
