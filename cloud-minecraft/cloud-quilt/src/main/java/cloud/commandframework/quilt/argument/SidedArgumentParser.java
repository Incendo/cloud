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

package cloud.commandframework.quilt.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.quilt.QuiltCommandContextKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Queue;

/**
 * An argument parser that is resolved in different ways on the logical server and logical client.
 *
 * @param <C> command sender type
 * @param <I> intermediate type to resolve
 * @param <R> resolved type
 * @since 1.5.0
 */
abstract class SidedArgumentParser<C, I, R> implements ArgumentParser<C, R> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull R> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        final CommandSource source = commandContext.get(QuiltCommandContextKeys.NATIVE_COMMAND_SOURCE);
        final ArgumentParseResult<I> intermediate = this.parseIntermediate(commandContext, inputQueue);

        return intermediate.flatMapParsedValue(value -> {
            if (source instanceof ServerCommandSource) {
                return this.resolveServer(commandContext, source, value);
            } else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                return this.resolveClient(commandContext, source, value);
            } else {
                throw new IllegalStateException("Cannot have non-server command source when not on client");
            }
        });
    }

    protected abstract @NonNull ArgumentParseResult<@NonNull I> parseIntermediate(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull Queue<@NonNull String> inputQueue
    );

    /**
     * Resolve the final value for this argument when running on the client.
     *
     * @param context Command context
     * @param source  The command source
     * @param value   parsed intermediate value
     * @return a resolved value
     * @since 1.5.0
     */
    protected abstract @NonNull ArgumentParseResult<@NonNull R> resolveClient(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull CommandSource source,
            @NonNull I value
    );

    /**
     * Resolve the final value for this argument when running on the server.
     *
     * @param context Command context
     * @param source  The command source
     * @param value   Parsed intermediate value
     * @return a resolved value
     * @since 1.5.0
     */
    protected abstract @NonNull ArgumentParseResult<@NonNull R> resolveServer(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull CommandSource source,
            @NonNull I value
    );

}
