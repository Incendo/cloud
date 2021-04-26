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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import net.kyori.adventure.util.ComponentMessageThrowable;
import net.minecraft.commands.arguments.EntityArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Shared utilities for Selector arguments. Not API.
 */
final class SelectorUtil {

    private SelectorUtil() {
    }

    static <T> @NonNull ArgumentParseResult<T> selectorParseFailure(final @NonNull IllegalArgumentException ex) {
        final Throwable cause = ex.getCause();
        if (cause instanceof ComponentMessageThrowable) {
            return ArgumentParseResult.failure(cause);
        }
        return ArgumentParseResult.failure(ex);
    }

    static <T> @NonNull ArgumentParseResult<T> onlyPlayersAllowed() {
        return ArgumentParseResult.failure(EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create());
    }

    static <T> @NonNull ArgumentParseResult<T> notSinglePlayer() {
        return ArgumentParseResult.failure(EntityArgument.ERROR_NOT_SINGLE_PLAYER.create());
    }

    static <T> @NonNull ArgumentParseResult<T> notSingleEntity() {
        return ArgumentParseResult.failure(EntityArgument.ERROR_NOT_SINGLE_ENTITY.create());
    }

    static <T> @NonNull ArgumentParseResult<T> noPlayersFound() {
        return ArgumentParseResult.failure(EntityArgument.NO_PLAYERS_FOUND.create());
    }

    static <T> @NonNull ArgumentParseResult<T> noEntitiesFound() {
        return ArgumentParseResult.failure(EntityArgument.NO_ENTITIES_FOUND.create());
    }

}
