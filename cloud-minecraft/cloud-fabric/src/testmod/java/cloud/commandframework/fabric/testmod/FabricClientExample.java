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
package cloud.commandframework.fabric.testmod;

import cloud.commandframework.Command;
import cloud.commandframework.CommandDescription;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.fabric.FabricClientCommandManager;
import cloud.commandframework.fabric.argument.FabricVanillaArgumentParsers;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static cloud.commandframework.arguments.standard.StringParser.stringParser;

public final class FabricClientExample implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        final FabricClientCommandManager<FabricClientCommandSource> commandManager =
                FabricClientCommandManager.createNative(ExecutionCoordinator.simpleCoordinator());

        final Command.Builder<FabricClientCommandSource> base = commandManager.commandBuilder("cloud_client");

        commandManager.command(base.literal("dump")
                .commandDescription(CommandDescription.commandDescription("Dump the client's Brigadier command tree"))
                .handler(ctx -> {
                    final Path target = FabricLoader.getInstance().getGameDir().resolve(
                            "cloud-dump-" + Instant.now().toString().replace(':', '-') + ".json"
                    );
                    ctx.sender().sendFeedback(
                            Component.literal("Dumping command output to ")
                                    .append(Component.literal(target.toString())
                                            .withStyle(s -> s.withClickEvent(new ClickEvent(
                                                    ClickEvent.Action.OPEN_FILE,
                                                    target.toAbsolutePath().toString()
                                            ))))
                    );

                    try (BufferedWriter writer = Files.newBufferedWriter(target); JsonWriter json = new JsonWriter(writer)) {
                        final CommandDispatcher<SharedSuggestionProvider> dispatcher = Minecraft.getInstance()
                                .getConnection()
                                .getCommands();
                        final JsonObject object = ArgumentUtils.serializeNodeToJson(dispatcher, dispatcher.getRoot());
                        json.setIndent("  ");
                        Streams.write(object, json);
                    } catch (final IOException ex) {
                        ctx.sender().sendError(Component.literal(
                                "Unable to write file, see console for details: " + ex.getMessage()
                        ));
                    }
                }));

        commandManager.command(base.literal("say")
                .required("required", greedyStringParser())
                .handler(ctx -> ctx.sender().sendFeedback(
                        Component.literal("Cloud client commands says: " + ctx.get("message"))
                )));

        commandManager.command(base.literal("quit")
                .handler(ctx -> {
                    final Minecraft client = Minecraft.getInstance();
                    disconnectClient(client);
                    client.stop();
                }));

        commandManager.command(base.literal("disconnect")
                .handler(ctx -> disconnectClient(Minecraft.getInstance())));

        commandManager.command(base.literal("requires_cheats")
                .permission(FabricClientCommandManager.cheatsAllowed(false))
                .handler(ctx -> ctx.sender().sendFeedback(Component.literal("Cheats are enabled!"))));

        // Test argument which requires CommandBuildContext/RegistryAccess
        commandManager.command(base.literal("show_item")
                .required("item", FabricVanillaArgumentParsers.contextualParser(ItemArgument::item, ItemInput.class))
                .handler(ctx -> {
                    try {
                        ctx.sender().sendFeedback(
                                ctx.<ItemInput>get("item").createItemStack(1, false).getDisplayName()
                        );
                    } catch (final CommandSyntaxException ex) {
                        ctx.sender().sendError(ComponentUtils.fromMessage(ex.getRawMessage()));
                    }
                }));

        commandManager.command(base.literal("flag_test")
                .optional("parameter", stringParser())
                .flag(CommandFlag.builder("flag").withAliases("f"))
                .handler(ctx -> ctx.sender().sendFeedback(Component.literal("Had flag: " + ctx.flags().isPresent("flag")))));
    }

    private static void disconnectClient(final @NonNull Minecraft client) {
        final boolean singlePlayer = client.hasSingleplayerServer();
        client.level.disconnect();
        if (singlePlayer) {
            client.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        } else {
            client.clearLevel();
        }
        if (singlePlayer) {
            client.setScreen(new TitleScreen());
        } else if (client.isConnectedToRealms()) {
            client.setScreen(new RealmsMainScreen(new TitleScreen()));
        } else {
            client.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }
    }
}
