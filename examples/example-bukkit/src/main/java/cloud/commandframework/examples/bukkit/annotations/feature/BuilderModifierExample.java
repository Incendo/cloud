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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.permission.PredicatePermission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example of a command builder modifier that gets triggered on the `@GameModeRequirement` annotation
 * and inserts a predicate permission that enforces the given game mode in order to use the command.
 */
public final class BuilderModifierExample implements AnnotationFeature {

    private static final CloudKey<Void> PERMISSION_KEY = CloudKey.of("gamemode");

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        annotationParser.registerBuilderModifier(
                GameModeRequirement.class,
                (requirement, builder) -> builder.permission(
                        PredicatePermission.of(PERMISSION_KEY, new GameModePredicate(requirement.value()))
                )
        );
    }


    public static final class GameModePredicate implements Predicate<CommandSender> {

        private final GameMode requiredGameMode;

        public GameModePredicate(final @NonNull GameMode requiredGameMode) {
            this.requiredGameMode = requiredGameMode;
        }

        @Override
        public boolean test(final @NonNull CommandSender commandSender) {
            if (!(commandSender instanceof Player)) {
                return true;
            }
            return ((Player) commandSender).getGameMode() == this.requiredGameMode;
        }
    }

    /**
     * Custom annotation that indicates the required game mode for a command.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GameModeRequirement {

        /**
         * The required game mode
         *
         * @return Required game mode
         */
        GameMode value();
    }
}
