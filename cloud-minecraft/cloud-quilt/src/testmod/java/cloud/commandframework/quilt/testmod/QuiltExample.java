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
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.quilt.QuiltServerCommandManager;
import cloud.commandframework.quilt.argument.ColorArgument;
import cloud.commandframework.quilt.argument.ItemDataArgument;
import cloud.commandframework.quilt.argument.server.ColumnPosArgument;
import cloud.commandframework.quilt.argument.server.MultipleEntitySelectorArgument;
import cloud.commandframework.quilt.argument.server.MultiplePlayerSelectorArgument;
import cloud.commandframework.quilt.argument.server.Vec3Argument;
import cloud.commandframework.quilt.data.Coordinates;
import cloud.commandframework.quilt.data.Coordinates.ColumnCoordinates;
import cloud.commandframework.quilt.data.MultipleEntitySelector;
import cloud.commandframework.quilt.data.MultiplePlayerSelector;
import cloud.commandframework.quilt.testmod.mixin.GiveCommandAccess;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class QuiltExample implements ModInitializer {

    @Override
    public void onInitialize() {
        // Create a commands manager. We'll use native command source types for this.
        final QuiltServerCommandManager<ServerCommandSource> manager =
                QuiltServerCommandManager.createNative(CommandExecutionCoordinator.simpleCoordinator());

        final Command.Builder<ServerCommandSource> base = manager.commandBuilder("cloudtest");

        final CommandArgument<ServerCommandSource, String> name = StringArgument.of("name");
        final CommandArgument<ServerCommandSource, Integer> hugs = IntegerArgument.<ServerCommandSource>newBuilder("hugs")
                .asOptionalWithDefault("1")
                .build();

        manager.command(base
                .literal("hugs")
                .argument(name)
                .argument(hugs)
                .handler(ctx -> {
                    ctx.getSender().sendFeedback(new LiteralText("Hello, ")
                            .append(ctx.get(name))
                            .append(", hope you're doing well!")
                            .styled(style -> style.withColor(TextColor.fromRgb(0xAA22BB))), false);

                    ctx.getSender().sendFeedback(new LiteralText("Cloud would like to give you ")
                            .append(new LiteralText(String.valueOf(ctx.get(hugs)))
                                    .styled(style -> style.withColor(TextColor.fromRgb(0xFAB3DA))))
                            .append(" hug(s) <3")
                            .styled(style -> style.withBold(true)), false);
                }));

        final CommandArgument<ServerCommandSource, MultiplePlayerSelector> playerSelector =
                MultiplePlayerSelectorArgument.of("players");
        final CommandArgument<ServerCommandSource, Formatting> textColor = ColorArgument.of("color");

        manager.command(base.literal("wave")
                .argument(playerSelector)
                .argument(textColor)
                .handler(ctx -> {
                    final MultiplePlayerSelector selector = ctx.get(playerSelector);
                    final Collection<ServerPlayerEntity> selected = selector.get();
                    selected.forEach(selectedPlayer ->
                            selectedPlayer.sendMessage(
                                    new LiteralText("Wave from ")
                                            .styled(style -> style.withColor(ctx.get(textColor)))
                                            .append(ctx.getSender().getDisplayName()),
                                    MessageType.SYSTEM,
                                    Util.NIL_UUID
                            ));
                    ctx.getSender().sendFeedback(
                            new LiteralText(String.format("Waved at %d players (%s)", selected.size(),
                                    selector.getInput()
                            )),
                            false
                    );
                }));

        manager.command(base.literal("give")
                .permission("cloud.give")
                .argument(MultiplePlayerSelectorArgument.of("targets"))
                .argument(ItemDataArgument.of("item"))
                .argument(IntegerArgument.<ServerCommandSource>newBuilder("amount")
                        .withMin(1)
                        .asOptionalWithDefault("1"))
                .handler(ctx -> {
                    final ItemStackArgument item = ctx.get("item");
                    final MultiplePlayerSelector targets = ctx.get("targets");
                    final int amount = ctx.get("amount");
                    GiveCommandAccess.give(
                            ctx.getSender(),
                            item,
                            targets.get(),
                            amount
                    );
                }));

        final Command.Builder<ServerCommandSource> mods = base.literal("mods").permission("cloud.mods");

        manager.command(mods.handler(ctx -> {
            final List<ModMetadata> modList = FabricLoader.getInstance().getAllMods().stream()
                    .map(ModContainer::getMetadata)
                    .sorted(Comparator.comparing(ModMetadata::getId))
                    .collect(Collectors.toList());
            final LiteralText text = new LiteralText("");
            text.append(new LiteralText("Loaded Mods")
                    .styled(style -> style.withColor(Formatting.BLUE).withFormatting(Formatting.BOLD)));
            text.append(new LiteralText(String.format(" (%s)\n", modList.size()))
                    .styled(style -> style.withColor(Formatting.GRAY).withFormatting(Formatting.ITALIC)));
            for (final ModMetadata mod : modList) {
                text.append(
                        new LiteralText("")
                                .styled(style -> style.withColor(Formatting.WHITE)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                String.format("/cloudtest mods %s", mod.getId())
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                new LiteralText("Click for more info")
                                        )))
                                .append(new LiteralText(mod.getName()).styled(style -> style.withColor(Formatting.GREEN)))
                                .append(new LiteralText(String.format(" (%s) ", mod.getId()))
                                        .styled(style -> style.withColor(Formatting.GRAY).withFormatting(Formatting.ITALIC)))
                                .append(new LiteralText(String.format("v%s", mod.getVersion())))
                );
                if (modList.indexOf(mod) != modList.size() - 1) {
                    text.append(new LiteralText(", ").styled(style -> style.withColor(Formatting.GRAY)));
                }
            }
            ctx.getSender().sendFeedback(text, false);
        }));

        final CommandArgument<ServerCommandSource, ModMetadata> modMetadata = manager.argumentBuilder(ModMetadata.class, "mod")
                .withSuggestionsProvider((ctx, input) -> FabricLoader.getInstance().getAllMods().stream()
                        .map(ModContainer::getMetadata)
                        .map(ModMetadata::getId)
                        .collect(Collectors.toList()))
                .withParser((ctx, inputQueue) -> {
                    final ModMetadata meta = FabricLoader.getInstance().getModContainer(inputQueue.peek())
                            .map(ModContainer::getMetadata)
                            .orElse(null);
                    if (meta != null) {
                        inputQueue.remove();
                        return ArgumentParseResult.success(meta);
                    }
                    return ArgumentParseResult.failure(new IllegalArgumentException(String.format(
                            "No mod with id '%s'",
                            inputQueue.peek()
                    )));
                })
                .build();

        manager.command(mods.argument(modMetadata)
                .handler(ctx -> {
                    final ModMetadata meta = ctx.get(modMetadata);
                    final MutableText text = new LiteralText("")
                            .append(new LiteralText(meta.getName())
                                    .styled(style -> style.withColor(Formatting.BLUE).withFormatting(Formatting.BOLD)))
                            .append(new LiteralText("\n modid: " + meta.getId()))
                            .append(new LiteralText("\n version: " + meta.getVersion()))
                            .append(new LiteralText("\n type: " + meta.getType()));

                    if (!meta.getDescription().isEmpty()) {
                        text.append(new LiteralText("\n description: " + meta.getDescription()));
                    }
                    if (!meta.getAuthors().isEmpty()) {
                        text.append(new LiteralText("\n authors: " + meta.getAuthors().stream()
                                .map(Person::getName)
                                .collect(Collectors.joining(", "))));
                    }
                    if (!meta.getLicense().isEmpty()) {
                        text.append(new LiteralText("\n license: " + String.join(", ", meta.getLicense())));
                    }
                    ctx.getSender().sendFeedback(
                            text,
                            false
                    );
                }));

        manager.command(base.literal("teleport")
                .permission("cloud.teleport")
                .argument(MultipleEntitySelectorArgument.of("targets"))
                .argument(Vec3Argument.of("location"))
                .handler(ctx -> {
                    final MultipleEntitySelector selector = ctx.get("targets");
                    final Vec3d location = ctx.<Coordinates>get("location").position();
                    selector.get().forEach(target ->
                            target.requestTeleport(location.getX(), location.getY(), location.getZ()));
                }));

        manager.command(base.literal("gotochunk")
                .permission("cloud.gotochunk")
                .argument(ColumnPosArgument.of("chunk_position"))
                .handler(ctx -> {
                    final ServerPlayerEntity player;
                    try {
                        player = ctx.getSender().getPlayer();
                    } catch (final CommandSyntaxException e) {
                        ctx.getSender().sendFeedback(new LiteralText("Must be a player to use this command"), false);
                        return;
                    }
                    final Vec3d vec = ctx.<ColumnCoordinates>get("chunk_position").position();
                    final ChunkPos pos = new ChunkPos((int) vec.getX(), (int) vec.getZ());
                    player.requestTeleport(pos.getStartX(), 128, pos.getStartZ());
                }));
    }

}
