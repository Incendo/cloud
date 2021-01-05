package cloud.commandframework.fabric;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import io.leangen.geantyref.TypeToken;
import net.minecraft.command.CommandSource;

/**
 * Keys used in {@link CommandContext}s available within a {@link FabricCommandManager}
 */
public final class FabricCommandContextKeys {

    private FabricCommandContextKeys() {
    }

    public static final CloudKey<CommandSource> NATIVE_COMMAND_SOURCE = SimpleCloudKey.of(
            "cloud:fabric_command_source",
            TypeToken.get(CommandSource.class)
    );

}
