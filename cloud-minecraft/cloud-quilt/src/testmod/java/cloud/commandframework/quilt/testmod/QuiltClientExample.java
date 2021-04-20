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
package cloud.commandframework.quilt.testmod;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.quilt.QuiltClientCommandManager;
import cloud.commandframework.meta.CommandMeta;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsBridgeScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class QuiltClientExample implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        final QuiltClientCommandManager<FabricClientCommandSource> commandManager =
                QuiltClientCommandManager.createNative(CommandExecutionCoordinator.simpleCoordinator());

        final Command.Builder<FabricClientCommandSource> base = commandManager.commandBuilder("cloud_client");

        commandManager.command(base.literal("dump")
                .meta(CommandMeta.DESCRIPTION, "Dump the client's Brigadier command tree")
                .handler(ctx -> {
                    final Path target = FabricLoader.getInstance().getGameDir().resolve(
                            "cloud-dump-" + Instant.now().toString().replace(':', '-') + ".json"
                    );
                    ctx.getSender().sendFeedback(
                            new LiteralText("Dumping command output to ")
                                    .append(new LiteralText(target.toString())
                                            .styled(s -> s.withClickEvent(new ClickEvent(
                                                    ClickEvent.Action.OPEN_FILE,
                                                    target.toAbsolutePath().toString()
                                            ))))
                    );

                    try (BufferedWriter writer = Files.newBufferedWriter(target); JsonWriter json = new JsonWriter(writer)) {
                        final CommandDispatcher<CommandSource> dispatcher = MinecraftClient.getInstance()
                                .getNetworkHandler()
                                .getCommandDispatcher();
                        final JsonObject object = ArgumentTypes.toJson(dispatcher, dispatcher.getRoot());
                        json.setIndent("  ");
                        Streams.write(object, json);
                    } catch (final IOException ex) {
                        ctx.getSender().sendError(new LiteralText(
                                "Unable to write file, see console for details: " + ex.getMessage()
                        ));
                    }
                }));

        commandManager.command(base.literal("say")
                .argument(StringArgument.greedy("message"))
                .handler(ctx -> ctx.getSender().sendFeedback(
                        new LiteralText("Cloud client commands says: " + ctx.get("message"))
                )));

        commandManager.command(base.literal("quit")
                .handler(ctx -> {
                    final MinecraftClient client = MinecraftClient.getInstance();
                    disconnectClient(client);
                    client.scheduleStop();
                }));

        commandManager.command(base.literal("disconnect")
                .handler(ctx -> disconnectClient(MinecraftClient.getInstance())));

        commandManager.command(base.literal("requires_cheats")
                .permission(QuiltClientCommandManager.cheatsAllowed(false))
                .handler(ctx -> ctx.getSender().sendFeedback(new LiteralText("Cheats are enabled!"))));
    }

    private static void disconnectClient(final @NonNull MinecraftClient client) {
        boolean singlePlayer = client.isInSingleplayer();
        client.world.disconnect();
        if (singlePlayer) {
            client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
        } else {
            client.disconnect();
        }
        if (singlePlayer) {
            client.openScreen(new TitleScreen());
        } else if (client.isConnectedToRealms()) {
            new RealmsBridgeScreen().switchToRealms(new TitleScreen());
        } else {
            client.openScreen(new MultiplayerScreen(new TitleScreen()));
        }
    }

}
