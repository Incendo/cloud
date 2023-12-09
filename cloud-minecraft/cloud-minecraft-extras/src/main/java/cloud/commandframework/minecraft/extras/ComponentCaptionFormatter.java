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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionFormatter;
import cloud.commandframework.captions.CaptionVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Extension of {@link CaptionFormatter} to make it easier to convert from {@link Caption captions}
 * to adventure {@link Component components}.
 * <p>
 * This utility does not dictate how the captions are mapped to components. You therefore have to supply
 * a {@link ComponentMapper} that maps between captions and components.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ComponentCaptionFormatter<C> extends CaptionFormatter<C, Component> {

    /**
     * Returns a caption formatter that replaces the results from the given {@code pattern} with
     * the values from the caption variables.
     * <p>
     * The 1st capturing group will be used to determine the name of the variable to use for
     * the replacement.
     *
     * @param <C>     the command sender type
     * @param pattern the pattern
     * @param mapper  the mapping between captions and components
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> patternReplacing(
            final @NonNull Pattern pattern,
            final @NonNull ComponentMapper<C> mapper
    ) {
        return new PatternReplacingComponentCaptionFormatter<>(pattern, mapper);
    }

    /**
     * Returns a caption formatter that replaces placeholders in the form of {@code <placeholder>}
     * with the caption variables.
     *
     * @param <C>    the command sender type
     * @param mapper the mapping between captions and components
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> placeholderReplacing(final @NonNull ComponentMapper<C> mapper) {
        return new PatternReplacingComponentCaptionFormatter<>(Pattern.compile("<(\\S+)>"), mapper);
    }

    /**
     * Returns a caption formatter that forwards the result from the given {@code mapper}.
     *
     * @param <C>    the command sender type
     * @param mapper the mapper
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> mapping(final @NonNull ComponentMapper<C> mapper) {
        return new MappingComponentCaptionFormatter<>(mapper);
    }

    /**
     * Returns a caption formatter that maps to a {@link net.kyori.adventure.text.TranslatableComponent} using the caption key
     * as the translation key.
     *
     * @param <C> the command sender type
     * @return the formatter
     */
    static <C> @NonNull ComponentCaptionFormatter<C> translatable() {
        return mapping(ComponentMapper.translatable());
    }


    /**
     * Maps from {@link Caption captions} to {@link Component components}.
     *
     * @param <C> the command sender type
     */
    interface ComponentMapper<C> {

        /**
         * Returns a mapper that maps to a {@link net.kyori.adventure.text.TranslatableComponent} using the caption key
         * as the translation key.
         *
         * @param <C> the command sender type
         * @return the mapper
         */
        static <C> @NonNull ComponentMapper<C> translatable() {
            return (key, caption, recipient) -> Component.translatable(key.key());
        }

        /**
         * Returns a mapper that maps to a {@link net.kyori.adventure.text.TextComponent} using the caption value.
         *
         * @param <C> the command sender type
         * @return the mapper
         */
        static <C> @NonNull ComponentMapper<C> text() {
            return (key, caption, recipient) -> Component.text(caption);
        }

        /**
         * Maps the caption to a component.
         *
         * @param captionKey the caption key
         * @param caption    the caption
         * @param recipient  the recipient
         * @return the component
         */
        @NonNull Component mapComponent(@NonNull Caption captionKey, @NonNull String caption, @NonNull C recipient);
    }

    final class PatternReplacingComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {

        private final Pattern pattern;
        private final ComponentMapper<C> mapper;

        private PatternReplacingComponentCaptionFormatter(
                final @NonNull Pattern pattern,
                final @NonNull ComponentMapper<C> mapper
        ) {
            this.pattern = pattern;
            this.mapper = mapper;
        }

        @Override
        public @NonNull Component formatCaption(
                final @NonNull Caption captionKey,
                final @NonNull C recipient,
                final @NonNull String caption,
                final @NonNull CaptionVariable @NonNull... variables
        ) {
            final Map<String, String> replacements = new HashMap<>();
            for (final CaptionVariable variable : variables) {
                replacements.put(variable.key(), variable.value());
            }

            final TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                    .match(this.pattern)
                    .replacement((matcher, builder) ->
                            builder.content(replacements.getOrDefault(matcher.group(1), matcher.group())))
                    .build();
            return this.mapper.mapComponent(captionKey, caption, recipient).replaceText(replacementConfig);
        }
    }

    final class MappingComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {

        private final ComponentMapper<C> mapper;

        private MappingComponentCaptionFormatter(final @NonNull ComponentMapper<C> mapper) {
            this.mapper = mapper;
        }

        @Override
        public @NonNull Component formatCaption(
                final @NonNull Caption captionKey,
                final @NonNull C recipient,
                final @NonNull String caption,
                final @NonNull CaptionVariable @NonNull... variables
        ) {
            return this.mapper.mapComponent(captionKey, caption, recipient);
        }
    }
}
