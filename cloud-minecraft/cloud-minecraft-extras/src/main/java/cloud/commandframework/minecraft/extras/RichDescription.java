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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.Description;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * A description implementation that uses Adventure components.
 *
 * @since 1.4.0
 */
public final class RichDescription implements Description {

    private static final RichDescription EMPTY = new RichDescription(Component.empty());

    private final Component contents;

    RichDescription(final @NonNull Component contents) {
        this.contents = contents;
    }

    /**
     * Get an empty description.
     *
     * @return the empty description
     */
    public static @NonNull RichDescription empty() {
        return EMPTY;
    }

    /**
     * Create a new rich description from the provided component.
     *
     * @param contents the rich contents
     * @return a new rich description
     */
    public static @NonNull RichDescription of(final @NonNull ComponentLike contents) {
        final Component componentContents = requireNonNull(contents, "contents").asComponent();
        if (Component.empty().equals(componentContents)) {
            return EMPTY;
        }

        return new RichDescription(componentContents);
    }

    /**
     * Create a new rich description from the provided component. Alias for {@link #of(ComponentLike)}.
     *
     * @param contents the rich contents
     * @return a new rich description
     */
    public static @NonNull RichDescription richDescription(final @NonNull ComponentLike contents) {
        return of(contents);
    }

    /* Translatable helper methods */

    /**
     * Create a rich description pointing to a translation key.
     *
     * @param key the translation key
     * @return a new rich description
     */
    public static @NonNull RichDescription translatable(final @NonNull String key) {
        requireNonNull(key, "key");

        return new RichDescription(Component.translatable(key));
    }

    /**
     * Create a rich description pointing to a translation key.
     *
     * @param key  the translation key
     * @param args the arguments to use with the translation key
     * @return a new rich description
     */
    public static @NonNull RichDescription translatable(
            final @NonNull String key,
            final @NonNull ComponentLike @NonNull... args
    ) {
        requireNonNull(key, "key");
        requireNonNull(args, "args");

        return new RichDescription(Component.translatable(key, args));
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated to discourage use. A plain serialization is a somewhat expensive and lossy operation, use
     *         {@link #contents()} instead.
     */
    @Override
    @Deprecated
    public @NonNull String textDescription() {
        return net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain()
                .serialize(GlobalTranslator.render(this.contents, Locale.getDefault()));
    }

    /**
     * Returns the contents of this description.
     *
     * @return the component contents of this description
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Component contents() {
        return this.contents;
    }

    @Override
    public boolean isEmpty() {
        return Component.empty().equals(this.contents);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final RichDescription that = (RichDescription) object;
        return Objects.equals(this.contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.contents);
    }
}
