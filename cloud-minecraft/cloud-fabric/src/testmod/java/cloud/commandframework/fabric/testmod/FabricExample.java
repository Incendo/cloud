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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricCommandManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;

public final class FabricExample implements ModInitializer {
    private static final CommandArgument<ServerCommandSource, String> NAME = StringArgument.of("name");
    private static final CommandArgument<ServerCommandSource, Integer> HUGS = IntegerArgument.<ServerCommandSource>newBuilder("hugs")
                    .asOptionalWithDefault("1")
                    .build();

    @Override
    public void onInitialize() {
        // Create a commands manager. We'll use native command source types for this.

        final FabricCommandManager<ServerCommandSource> manager =
                FabricCommandManager.createNative(CommandExecutionCoordinator.simpleCoordinator());

        manager.command(manager.commandBuilder("cloudtest")
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

    }

}
