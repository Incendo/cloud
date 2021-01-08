package cloud.commandframework.fabric.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.FabricCommandContextKeys;
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
 */
abstract class SidedArgumentParser<C, I, R> implements ArgumentParser<C, R> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull R> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        final CommandSource source = commandContext.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE);
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

    protected abstract ArgumentParseResult<I> parseIntermediate(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull Queue<@NonNull String> inputQueue
    );

    /**
     * Resolve the final value for this argument when running on the client.
     *
     * @param context Command context
     * @param source The command source
     * @param value parsed intermediate value
     * @return a resolved value
     */
    protected abstract ArgumentParseResult<R> resolveClient(CommandContext<C> context, CommandSource source, I value);

    /**
     * Resolve the final value for this argument when running on the server.
     *
     * @param context Command context
     * @param source The command source
     * @param value Parsed intermediate value
     * @return a resolved value
     */
    protected abstract ArgumentParseResult<R> resolveServer(CommandContext<C> context, CommandSource source, I value);

}
