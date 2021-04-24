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
package cloud.commandframework.examples.sponge;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.sponge.CloudInjectionModule;
import cloud.commandframework.sponge.SpongeCommandManager;
import cloud.commandframework.sponge.argument.NamedTextColorArgument;
import cloud.commandframework.sponge.argument.OperatorArgument;
import cloud.commandframework.sponge.argument.RegistryEntryArgument;
import cloud.commandframework.sponge.argument.SinglePlayerSelectorArgument;
import cloud.commandframework.sponge.argument.WorldArgument;
import cloud.commandframework.sponge.data.SinglePlayerSelector;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.command.parameter.managed.operator.Operators;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.function.Function;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextColor.color;

@Plugin("cloud-example-sponge")
public final class CloudExamplePlugin {

    private static final Component COMMAND_PREFIX = text()
            .color(color(0x333333))
            .content("[")
            .append(text("Cloud-Sponge", color(0xF7CF0D)))
            .append(text(']'))
            .build();

    private final SpongeCommandManager<CommandCause> commandManager;

    /**
     * Create example plugin instance
     *
     * @param injector injector
     */
    @Inject
    public CloudExamplePlugin(final @NonNull Injector injector) {
        // Create child injector with cloud module
        final Injector childInjector = injector.createChildInjector(
                CloudInjectionModule.createNative(CommandExecutionCoordinator.simpleCoordinator())
        );

        // Get command manager instance
        this.commandManager = childInjector.getInstance(Key.get(new TypeLiteral<SpongeCommandManager<CommandCause>>() {
        }));

        // Use Cloud's enhanced number suggestions
        this.commandManager.parserMapper().cloudNumberSuggestions(true);

        // Register minecraft-extras exception handlers
        new MinecraftExceptionHandler<CommandCause>()
                .withDefaultHandlers()
                .withDecorator(message -> TextComponent.ofChildren(COMMAND_PREFIX, space(), message))
                .apply(this.commandManager, CommandCause::audience);

        this.registerCommands();
    }

    private void registerCommands() {
        this.commandManager.command(this.commandManager.commandBuilder("cloud_test")
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(this.commandManager.commandBuilder("cloud_test1")
                .literal("test")
                .literal("test1")
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        final Command.Builder<CommandCause> cloudTest2 = this.commandManager.commandBuilder("cloud_test2");
        this.commandManager.command(cloudTest2.literal("test")
                .argument(StringArgument.single("string_arg"))
                .literal("test2")
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(cloudTest2.literal("test")
                .literal("literal_arg")
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(cloudTest2.literal("another_test")
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(this.commandManager.commandBuilder("string_test")
                .argument(StringArgument.single("single"))
                .argument(StringArgument.quoted("quoted"))
                .argument(StringArgument.greedy("greedy"))
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(this.commandManager.commandBuilder("int_test")
                .argument(IntegerArgument.of("any"))
                .argument(IntegerArgument.<CommandCause>newBuilder("gt0").withMin(1))
                .argument(IntegerArgument.<CommandCause>newBuilder("lt100").withMax(99))
                .argument(IntegerArgument.<CommandCause>newBuilder("5to20").withMin(5).withMax(20))
                .handler(ctx -> ctx.getSender().audience().sendMessage(text("success"))));
        this.commandManager.command(this.commandManager.commandBuilder("enchantment_type_test")
                .argument(RegistryEntryArgument.of("enchantment_type", EnchantmentType.class, RegistryTypes.ENCHANTMENT_TYPE))
                .argument(IntegerArgument.optional("level", 1))
                .handler(ctx -> {
                    final Subject subject = ctx.getSender().subject();
                    if (!(subject instanceof Player)) {
                        ctx.getSender().audience().sendMessage(text("This command is for players only!", RED));
                        return;
                    }
                    final Player player = (Player) subject;
                    final Hotbar hotbar = player.inventory().hotbar();
                    final int index = hotbar.selectedSlotIndex();
                    final Slot slot = hotbar.slot(index).get();
                    final InventoryTransactionResult.Poll result = slot.poll();
                    if (result.type() != InventoryTransactionResult.Type.SUCCESS) {
                        player.sendMessage(text("You must hold an item to enchant!", RED));
                        return;
                    }
                    final ItemStack modified = ItemStack.builder()
                            .fromItemStack(result.polledItem().createStack())
                            .add(Keys.APPLIED_ENCHANTMENTS, ImmutableList.of(
                                    Enchantment.of(
                                            ctx.<EnchantmentType>get("enchantment_type"),
                                            ctx.<Integer>get("level")
                                    )
                            ))
                            .build();
                    slot.set(modified);
                }));
        this.commandManager.command(this.commandManager.commandBuilder("color_test")
                .argument(NamedTextColorArgument.of("color"))
                .argument(StringArgument.greedy("message"))
                .handler(ctx -> {
                    ctx.getSender().audience().sendMessage(
                            text(ctx.get("message"), ctx.<NamedTextColor>get("color"))
                    );
                }));
        this.commandManager.command(this.commandManager.commandBuilder("operator_test")
                .argument(IntegerArgument.of("first"))
                .argument(OperatorArgument.of("operator"))
                .argument(IntegerArgument.of("second"))
                .handler(ctx -> {
                    final int first = ctx.get("first");
                    final int second = ctx.get("second");
                    final Operator operator = ctx.get("operator");
                    if (!(operator instanceof Operator.Simple)) {
                        ctx.getSender().audience().sendMessage(
                                text("That type of operator is not applicable here!", RED)
                        );
                        return;
                    }
                    ctx.getSender().audience().sendMessage(text()
                            .color(AQUA)
                            .append(text(first))
                            .append(space())
                            .append(text(operator.asString(), BLUE))
                            .append(space())
                            .append(text(second))
                            .append(space())
                            .append(text('→', BLUE))
                            .append(space())
                            .append(text(((Operator.Simple) operator).apply(first, second)))
                    );
                }));
        this.commandManager.command(this.commandManager.commandBuilder("modifylevel")
                .argument(OperatorArgument.of("operator"))
                .argument(DoubleArgument.of("value"))
                .handler(ctx -> {
                    final Subject subject = ctx.getSender().subject();
                    if (!(subject instanceof Player)) { // todo: a solution to this
                        ctx.getSender().audience().sendMessage(text("This command is for players only!", RED));
                        return;
                    }
                    final Player player = (Player) subject;
                    final Operator operator = ctx.get("operator");
                    final double value = ctx.get("value");
                    if (operator == Operators.ASSIGN.get()) {
                        player.offer(Keys.EXPERIENCE, (int) value);
                        return;
                    }
                    if (!(operator instanceof Operator.Simple)) {
                        ctx.getSender().audience().sendMessage(
                                text("That type of operator is not applicable here!", RED)
                        );
                        return;
                    }
                    final int currentXp = player.get(Keys.EXPERIENCE).get();
                    player.offer(Keys.EXPERIENCE, (int) ((Operator.Simple) operator).apply(currentXp, value));
                }));
        this.commandManager.command(this.commandManager.commandBuilder("selectplayer")
                .argument(SinglePlayerSelectorArgument.of("player"))
                .handler(ctx -> {
                    final Player player = ctx.<SinglePlayerSelector>get("player").getSingle();
                    ctx.getSender().audience().sendMessage(TextComponent.ofChildren(
                            text("Display name of selected player: ", GRAY),
                            player.displayName().get()
                    ));
                }));
        this.commandManager.command(this.commandManager.commandBuilder("world_test")
                .argument(WorldArgument.of("world"))
                .handler(ctx -> {
                    ctx.getSender().audience().sendMessage(text(ctx.<ServerWorld>get("world").key().asString()));
                }));
        this.commandManager.command(this.commandManager.commandBuilder("give_item")
                .argument(SinglePlayerSelectorArgument.of("player"))
                .argument(RegistryEntryArgument.of("type", ItemType.class, RegistryTypes.ITEM_TYPE))
                .handler(ctx -> {
                    final Player player = ctx.<SinglePlayerSelector>get("player").getSingle();
                    player.inventory().offer(ItemStack.of(ctx.<ItemType>get("type")));
                }));
        this.commandManager.command(this.commandManager.commandBuilder("test_entity_type")
                .argument(RegistryEntryArgument.of("type", new TypeToken<EntityType<?>>() {
                }, RegistryTypes.ENTITY_TYPE))
                .handler(ctx -> {
                    ctx.getSender().audience().sendMessage(ctx.<EntityType<?>>get("type"));
                }));
        final Function<CommandContext<CommandCause>, RegistryHolder> holderFunction = ctx -> ctx.getSender()
                .location()
                .orElse(Sponge.server().worldManager().defaultWorld().location(0, 0, 0))
                .world()
                .registries();
        this.commandManager.command(this.commandManager.commandBuilder("test_biomes")
                .argument(RegistryEntryArgument.of("biome", Biome.class, holderFunction, RegistryTypes.BIOME))
                .handler(ctx -> {
                    final ResourceKey biomeKey = holderFunction.apply(ctx)
                            .registry(RegistryTypes.BIOME)
                            .findValueKey(ctx.get("biome"))
                            .orElseThrow(IllegalStateException::new);
                    ctx.getSender().audience().sendMessage(text(biomeKey.asString()));
                }));
        this.commandManager.command(this.commandManager.commandBuilder("test_sounds")
                .argument(RegistryEntryArgument.of("type", SoundType.class, RegistryTypes.SOUND_TYPE))
                .handler(ctx -> {
                    ctx.getSender().audience().sendMessage(text(ctx.<SoundType>get("type").key().asString()));
                }));
    }

}
