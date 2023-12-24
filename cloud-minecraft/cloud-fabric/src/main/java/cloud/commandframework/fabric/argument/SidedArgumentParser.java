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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument parser that is resolved in different ways on the logical server and logical client.
 *
 * @param <C> command sender type
 * @param <I> intermediate type to resolve
 * @param <R> resolved type
 * @since 1.5.0
 */
abstract class SidedArgumentParser<C, I, R> implements ArgumentParser.FutureArgumentParser<C, R> {

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<R>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final SharedSuggestionProvider source = commandContext.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
        return this.intermediateParser().flatMapSuccess((ctx, result) -> {
            if (source instanceof CommandSourceStack) {
                return this.resolveServer(commandContext, (CommandSourceStack) source, result);
            } else if (source instanceof FabricClientCommandSource) {
                return this.resolveClient(commandContext, (FabricClientCommandSource) source, result);
            } else {
                throw new IllegalStateException("Cannot have non-server command source when not on client");
            }
        }).parseFuture(commandContext, commandInput);
    }

    protected abstract @NonNull FutureArgumentParser<C, I> intermediateParser();

    /**
     * Resolve the final value for this argument when running on the client.
     *
     * @param context Command context
     * @param source  The command source
     * @param value   parsed intermediate value
     * @return a resolved value
     * @since 1.5.0
     */
    protected abstract @NonNull CompletableFuture<@NonNull ArgumentParseResult<@NonNull R>> resolveClient(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull FabricClientCommandSource source,
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
    protected abstract @NonNull CompletableFuture<@NonNull ArgumentParseResult<@NonNull R>> resolveServer(
            @NonNull CommandContext<@NonNull C> context,
            @NonNull CommandSourceStack source,
            @NonNull I value
    );
}
