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
package cloud.commandframework.bukkit.arguments.selector;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiplePlayerSelector extends MultipleEntitySelector {

    private final List<Player> players = new ArrayList<>();

    /**
     * Construct a new selector
     *
     * @param selector The input string used to create this selector
     * @param entities The List of Bukkit {@link Entity}s to construct the {@link EntitySelector} from
     */
    public MultiplePlayerSelector(
            final @NonNull String selector,
            final @NonNull List<@NonNull Entity> entities
    ) {
        super(selector, entities);
        entities.forEach(e -> {
            if (e.getType() != EntityType.PLAYER) {
                throw new IllegalArgumentException("Non-players selected in player selector.");
            } else {
                players.add((Player) e);
            }
        });
    }

    /**
     * Get the resulting players
     *
     * @return Immutable views of the list of Bukkit {@link Player players} parsed from the selector
     */
    public final @NonNull List<@NonNull Player> getPlayers() {
        return Collections.unmodifiableList(this.players);
    }

}
