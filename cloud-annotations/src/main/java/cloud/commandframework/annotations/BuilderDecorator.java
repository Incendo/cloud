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
package cloud.commandframework.annotations;

import cloud.commandframework.Command;
import cloud.commandframework.CommandDescription;
import cloud.commandframework.permission.Permission;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Decorators that get to modify a command builder that is used to construct the command for annotated command methods.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface BuilderDecorator<C> {

    /**
     * Returns a {@link BuilderDecorator} that sets the default {@link Command.Builder#commandDescription() description}
     * of a command.
     *
     * @param <C>         the command sender type
     * @param description the description
     * @return the decorator
     */
    static <C> @NonNull BuilderDecorator<C> defaultDescription(final @NonNull CommandDescription description) {
        return builder -> builder.commandDescription(description);
    }

    /**
     * Returns a {@link BuilderDecorator} that sets the default {@link Command.Builder#permission(Permission) permission}
     * of a command.
     *
     * @param <C>        the command sender type
     * @param permission the permission
     * @return the decorator
     */
    static <C> @NonNull BuilderDecorator<C> defaultPermission(final @NonNull Permission permission) {
        return builder -> builder.permission(permission);
    }

    /**
     * Returns a {@link BuilderDecorator} applies the given {@code applicable} to the command builders.
     *
     * @param <C>        the command sender type
     * @param applicable the applicable to apply
     * @return the decorator
     */
    static <C> @NonNull BuilderDecorator<C> applicable(final Command.Builder.@NonNull Applicable<C> applicable) {
        return builder -> builder.apply(applicable);
    }

    /**
     * Decorates the given {@code builder} and returns the result.
     *
     * @param builder the builder to decorate
     * @return the result
     */
    Command.@NonNull Builder<C> decorate(Command.@NonNull Builder<C> builder);
}
