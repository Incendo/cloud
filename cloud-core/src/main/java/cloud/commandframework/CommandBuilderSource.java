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
package cloud.commandframework;

import cloud.commandframework.meta.CommandMeta;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandBuilderSource<C> {

    /**
     * Creates a new command builder.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name        command name
     * @param aliases     command aliases
     * @param description description for the root literal
     * @param meta        command meta
     * @return the builder instance
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Collection<String> aliases,
            final @NonNull Description description,
            final @NonNull CommandMeta meta
    ) {
        return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases.toArray(new String[0])));
    }

    /**
     * Creates a new command builder with an empty description.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name    command name
     * @param aliases command aliases
     * @param meta    command meta
     * @return the builder instance
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Collection<String> aliases,
            final @NonNull CommandMeta meta
    ) {
        return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases.toArray(new String[0])));
    }

    /**
     * Creates a new command builder.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name        command name
     * @param meta        command meta
     * @param description description for the root literal
     * @param aliases     command aliases
     * @return the builder instance
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull CommandMeta meta,
            final @NonNull Description description,
            final @NonNull String... aliases
    ) {
        return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases));
    }

    /**
     * Creates a new command builder with an empty description.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name    command name
     * @param meta    command meta
     * @param aliases command aliases
     * @return the builder instance
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull CommandMeta meta,
            final @NonNull String... aliases
    ) {
        return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases));
    }

    /**
     * Create a new command builder using default command meta created by {@link #createDefaultCommandMeta()}.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name        command name
     * @param description description for the root literal
     * @param aliases     command aliases
     * @return the builder instance
     * @throws UnsupportedOperationException If the implementation does not support default command meta creation
     * @see #createDefaultCommandMeta() default command meta creation
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Description description,
            final @NonNull String... aliases
    ) {
        return this.decorateBuilder(Command.newBuilder(name, this.createDefaultCommandMeta(), description, aliases));
    }

    /**
     * Creates a new command builder using default command meta created by {@link #createDefaultCommandMeta()}, and
     * an empty description.
     *
     * <p>The builder will be decorated using {@link #decorateBuilder(Command.Builder)}.</p>
     *
     * <p>This will not register the command. Use {@link CommandManager#command(Command.Builder)} to register the command.</p>
     *
     * @param name    command name
     * @param aliases command aliases
     * @return the builder instance
     * @throws UnsupportedOperationException If the implementation does not support default command meta creation
     * @see #createDefaultCommandMeta() Default command meta creation
     */
    default Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull String... aliases
    ) {
        return this.decorateBuilder(Command.<C>newBuilder(name, this.createDefaultCommandMeta(), Description.empty(), aliases));
    }

    /**
     * Constructs a default {@link CommandMeta} instance.
     *
     * @return default command meta
     */
    @NonNull CommandMeta createDefaultCommandMeta();

    /**
     * Decorates the given {@code builder} and returns the result.
     *
     * <p>This should only be called by {@link CommandBuilderSource} to prepare the builders.</p>
     *
     * @param builder builder to decorate
     * @return the decorated builder
     */
    @API(status = API.Status.INTERNAL, since = "2.0.0")
    Command.@NonNull Builder<C> decorateBuilder(Command.@NonNull Builder<C> builder);
}
