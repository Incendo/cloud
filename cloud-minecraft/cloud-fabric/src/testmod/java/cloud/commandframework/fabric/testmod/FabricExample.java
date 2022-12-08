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
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.fabric.argument.ItemInputArgument;
import cloud.commandframework.fabric.argument.NamedColorArgument;
import cloud.commandframework.fabric.argument.RegistryEntryArgument;
import cloud.commandframework.fabric.argument.server.ColumnPosArgument;
import cloud.commandframework.fabric.argument.server.MultipleEntitySelectorArgument;
import cloud.commandframework.fabric.argument.server.MultiplePlayerSelectorArgument;
import cloud.commandframework.fabric.argument.server.Vec3dArgument;
import cloud.commandframework.fabric.data.Coordinates;
import cloud.commandframework.fabric.data.Coordinates.ColumnCoordinates;
import cloud.commandframework.fabric.data.MultipleEntitySelector;
import cloud.commandframework.fabric.data.MultiplePlayerSelector;
import cloud.commandframework.fabric.testmod.mixin.GiveCommandAccess;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public final class FabricExample implements ModInitializer {

    @Override
    public void onInitialize() {
        // Create a commands manager. We'll use native command source types for this.
        final FabricServerCommandManager<CommandSourceStack> manager =
                FabricServerCommandManager.createNative(CommandExecutionCoordinator.simpleCoordinator());

        final Command.Builder<CommandSourceStack> base = manager.commandBuilder("cloudtest");

        final CommandArgument<CommandSourceStack, String> name = StringArgument.of("name");
        final CommandArgument<CommandSourceStack, Integer> hugs = IntegerArgument.<CommandSourceStack>newBuilder("hugs")
                .asOptionalWithDefault("1")
                .build();

        manager.command(base
                .literal("hugs")
                .argument(name)
                .argument(hugs)
                .handler(ctx -> {
                    ctx.getSender().sendSuccess(Component.literal("Hello, ")
                            .append(ctx.get(name))
                            .append(", hope you're doing well!")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA22BB))), false);

                    ctx.getSender().sendSuccess(Component.literal("Cloud would like to give you ")
                            .append(Component.literal(String.valueOf(ctx.get(hugs)))
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFAB3DA))))
                            .append(" hug(s) <3")
                            .withStyle(style -> style.withBold(true)), false);
                }));

        final CommandArgument<CommandSourceStack, Biome> biomeArgument = RegistryEntryArgument.of(
                "biome",
                Biome.class,
                Registries.BIOME
        );

        manager.command(base
                .literal("land")
                .argument(biomeArgument)
                .handler(ctx -> {
                    ctx.getSender().sendSuccess(Component.literal("Yes, the biome ")
                            .append(Component.literal(
                                            ctx.getSender().registryAccess()
                                                    .registryOrThrow(Registries.BIOME)
                                                    .getKey(ctx.get(biomeArgument)).toString())
                                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                            .append(Component.literal(" is pretty cool"))
                            .withStyle(style -> style.withColor(0x884433)), false);
                })
        );

        final CommandArgument<CommandSourceStack, MultiplePlayerSelector> playerSelector =
                MultiplePlayerSelectorArgument.of("players");
        final CommandArgument<CommandSourceStack, ChatFormatting> textColor = NamedColorArgument.of("color");

        manager.command(base.literal("wave")
                .argument(playerSelector)
                .argument(textColor)
                .handler(ctx -> {
                    final MultiplePlayerSelector selector = ctx.get(playerSelector);
                    final Collection<ServerPlayer> selected = selector.get();
                    selected.forEach(selectedPlayer ->
                            selectedPlayer.sendSystemMessage(
                                    Component.literal("Wave from ")
                                            .withStyle(style -> style.withColor(ctx.get(textColor)))
                                            .append(ctx.getSender().getDisplayName())
                            ));
                    ctx.getSender().sendSuccess(
                            Component.literal(String.format("Waved at %d players (%s)", selected.size(),
                                    selector.inputString()
                            )),
                            false
                    );
                }));

        manager.command(base.literal("give")
                .permission("cloud.give")
                .argument(MultiplePlayerSelectorArgument.of("targets"))
                .argument(ItemInputArgument.of("item"))
                .argument(IntegerArgument.<CommandSourceStack>newBuilder("amount")
                        .withMin(1)
                        .asOptionalWithDefault("1"))
                .handler(ctx -> {
                    final ItemInput item = ctx.get("item");
                    final MultiplePlayerSelector targets = ctx.get("targets");
                    final int amount = ctx.get("amount");
                    GiveCommandAccess.giveItem(
                            ctx.getSender(),
                            item,
                            targets.get(),
                            amount
                    );
                }));

        final Command.Builder<CommandSourceStack> mods = base.literal("mods").permission("cloud.mods");

        manager.command(mods.handler(ctx -> {
            final List<ModMetadata> modList = FabricLoader.getInstance().getAllMods().stream()
                    .map(ModContainer::getMetadata)
                    .sorted(Comparator.comparing(ModMetadata::getId))
                    .collect(Collectors.toList());
            final MutableComponent text = Component.literal("");
            text.append(Component.literal("Loaded Mods")
                    .withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.BOLD)));
            text.append(Component.literal(String.format(" (%s)\n", modList.size()))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY).applyFormat(ChatFormatting.ITALIC)));
            for (final ModMetadata mod : modList) {
                text.append(
                        Component.literal("")
                                .withStyle(style -> style.withColor(ChatFormatting.WHITE)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.SUGGEST_COMMAND,
                                                String.format("/cloudtest mods %s", mod.getId())
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("Click for more info")
                                        )))
                                .append(Component
                                        .literal(mod.getName())
                                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                                .append(Component.literal(String.format(" (%s) ", mod.getId()))
                                        .withStyle(style -> style
                                                .withColor(ChatFormatting.GRAY)
                                                .applyFormat(ChatFormatting.ITALIC)))
                                .append(Component.literal(String.format("v%s", mod.getVersion())))
                );
                if (modList.indexOf(mod) != modList.size() - 1) {
                    text.append(Component.literal(", ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
                }
            }
            ctx.getSender().sendSuccess(text, false);
        }));

        final CommandArgument<CommandSourceStack, ModMetadata> modMetadata = manager.argumentBuilder(ModMetadata.class, "mod")
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
                    final MutableComponent text = Component.literal("")
                            .append(Component.literal(meta.getName())
                                    .withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.BOLD)))
                            .append(Component.literal("\n modid: " + meta.getId()))
                            .append(Component.literal("\n version: " + meta.getVersion()))
                            .append(Component.literal("\n type: " + meta.getType()));

                    if (!meta.getDescription().isEmpty()) {
                        text.append(Component.literal("\n description: " + meta.getDescription()));
                    }
                    if (!meta.getAuthors().isEmpty()) {
                        text.append(Component.literal("\n authors: " + meta.getAuthors().stream()
                                .map(Person::getName)
                                .collect(Collectors.joining(", "))));
                    }
                    if (!meta.getLicense().isEmpty()) {
                        text.append(Component.literal("\n license: " + String.join(", ", meta.getLicense())));
                    }
                    ctx.getSender().sendSuccess(
                            text,
                            false
                    );
                }));

        manager.command(base.literal("teleport")
                .permission("cloud.teleport")
                .argument(MultipleEntitySelectorArgument.of("targets"))
                .argument(Vec3dArgument.of("location"))
                .handler(ctx -> {
                    final MultipleEntitySelector selector = ctx.get("targets");
                    final Vec3 location = ctx.<Coordinates>get("location").position();
                    selector.get().forEach(target ->
                            target.teleportToWithTicket(location.x(), location.y(), location.z()));
                }));

        manager.command(base.literal("gotochunk")
                .permission("cloud.gotochunk")
                .argument(ColumnPosArgument.of("chunk_position"))
                .handler(ctx -> {
                    final ServerPlayer player;
                    try {
                        player = ctx.getSender().getPlayerOrException();
                    } catch (final CommandSyntaxException e) {
                        ctx.getSender().sendSuccess(ComponentUtils.fromMessage(e.getRawMessage()), false);
                        return;
                    }
                    final Vec3 vec = ctx.<ColumnCoordinates>get("chunk_position").position();
                    final ChunkPos pos = new ChunkPos((int) vec.x(), (int) vec.z());
                    player.teleportToWithTicket(pos.getMinBlockX(), 128, pos.getMinBlockZ());
                }));
    }
}
