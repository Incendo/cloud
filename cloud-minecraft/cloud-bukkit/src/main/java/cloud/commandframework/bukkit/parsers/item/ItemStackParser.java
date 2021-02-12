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
package cloud.commandframework.bukkit.parsers.item;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;

public final class ItemStackParser<C> implements ArgumentParser<C, ItemStack> {

    static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName()
            .replace(".", ",").split(",")[3];
    static final int MAJOR_MINOR_VERSION = Integer.parseInt(NMS_VERSION.split("_")[1]);
    private final VersionedItemParser itemParser = VersionedItemParserProvider.provide();

    @Override
    public @NonNull ArgumentParseResult<@NonNull ItemStack> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        String start = inputQueue.peek();
        if (start == null) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    ItemStackParser.class,
                    commandContext
            ));
        }
        inputQueue.remove();
        StringBuilder inputBuilder = new StringBuilder(start);
        int queueSize = inputQueue.size();
        if (queueSize != 0 && start.indexOf('{') != -1) {
            for (int i = 0; i < queueSize; i++) {
                String peek = inputQueue.peek();
                if (peek == null) {
                    inputQueue.remove();
                    continue;
                }
                if (i == 0) {
                    inputBuilder.append(' ').append(peek).append(' ');
                } else {
                    inputBuilder.append(peek).append(' ');
                }
                inputQueue.remove();
                int last = peek.lastIndexOf('}');
                if (last != -1 && ((last + 1) == peek.length())) {
                    break;
                }
            }
        }
        String input = inputBuilder.toString().trim();

        ItemStackParseResult parseResult = itemParser.parseItemStack(commandContext, input);
        return parseResult.getResult()
                .map(ArgumentParseResult::success)
                .orElse(ArgumentParseResult.failure(parseResult.getException()));

    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return itemParser.allItemNames()
                .stream()
                .filter(candidate -> candidate.startsWith(input))
                .collect(Collectors.toList());
    }

}
