//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.meta.CommandMeta;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class FabricExample implements ModInitializer {
    private static final CommandArgument<ServerCommandSource, String> NAME = StringArgument.of("name");
    private static final CommandArgument<ServerCommandSource, Integer> HUGS = IntegerArgument.<ServerCommandSource>newBuilder("hugs")
                    .asOptionalWithDefault("1")
                    .build();

    @Override
    public void onInitialize() {
        // Create a commands manager. We'll use native command source types for this.

        final FabricServerCommandManager<ServerCommandSource> manager =
                FabricServerCommandManager.createNative(CommandExecutionCoordinator.simpleCoordinator());

        final Command.Builder<ServerCommandSource> base = manager.commandBuilder("cloudtest");

        manager.command(base
                .argument(NAME)
                .argument(HUGS)
                .handler(ctx -> {
                    ctx.getSender().sendFeedback(new LiteralText("Hello, ")
                            .append(ctx.get(NAME))
                            .append(", hope you're doing well!")
                            .styled(style -> style.withColor(TextColor.fromRgb(0xAA22BB))), false);

                    ctx.getSender().sendFeedback(new LiteralText("Cloud would like to give you ")
                            .append(new LiteralText(String.valueOf(ctx.get(HUGS)))
                                    .styled(style -> style.withColor(TextColor.fromRgb(0xFAB3DA))))
                            .append(" hug(s) <3")
                            .styled(style -> style.withBold(true)), false);
                }));

        manager.command(base.literal("dump")
                .meta(CommandMeta.DESCRIPTION, "Dump the client's Brigadier command tree (integrated server only)")
                .meta(FabricServerCommandManager.META_REGISTRATION_ENVIRONMENT, CommandManager.RegistrationEnvironment.INTEGRATED)
        .handler(ctx -> {
            final Path target =
                    FabricLoader.getInstance().getGameDir().resolve(
                            "cloud-dump-"
                            + Instant.now().toString().replace(':', '-')
                            + ".json"
                    );
            ctx.getSender().sendFeedback(new LiteralText("Dumping command output to ")
                    .append(new LiteralText(target.toString())
                            .styled(s -> s.withClickEvent(new ClickEvent(
                                    ClickEvent.Action.OPEN_FILE,
                                    target.toAbsolutePath().toString()
                            )))), false);

            try (BufferedWriter writer = Files.newBufferedWriter(target); JsonWriter json = new JsonWriter(writer)) {
                final CommandDispatcher<CommandSource> dispatcher = MinecraftClient.getInstance()
                        .getNetworkHandler()
                        .getCommandDispatcher();
                final JsonObject object = ArgumentTypes.toJson(dispatcher, dispatcher.getRoot());
                json.setIndent("  ");
                Streams.write(object, json);
            } catch (final IOException ex) {
                ctx.getSender().sendError(new LiteralText("Unable to write file, see console for details: " + ex.getMessage()));
            }
        }));



    }

}
