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
package cloud.commandframework.paper;

import cloud.commandframework.Completion;
import cloud.commandframework.brigadier.BrigadierCompletion;
import cloud.commandframework.bukkit.BukkitPluginRegistrationHandler;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.mojang.brigadier.Message;
import io.papermc.paper.brigadier.PaperBrigadier;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

final class AsyncCommandSuggestionsListener<C> implements Listener {

    private final PaperCommandManager<C> paperCommandManager;

    private final BiConsumer<AsyncTabCompleteEvent, List<Completion>> completionsApplier;

    @SuppressWarnings("ConstantConditions")
    AsyncCommandSuggestionsListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
        if (paperCommandManager.hasCapability(CloudBukkitCapabilities.PAPER_TOOLTIPS)) {
            BiFunction<String, Message, AsyncTabCompleteEvent.Completion> completionWithDescription;
            if (Audience.class.isAssignableFrom(Player.class)) { // If we use native adventure
                completionWithDescription = (s, desc) -> {
                    Component component = PaperBrigadier.componentFromMessage(desc);
                    return AsyncTabCompleteEvent.Completion.completion(s, component);
                };
            } else { // We have a shaded adventure, using method handles to get needed methods without shading
                final Method componentFromMessageMethod = CraftBukkitReflection.needMethod(
                        PaperBrigadier.class,
                        "componentFromMessage",
                        Message.class
                );
                final Method completionWithTooltipMethod = CraftBukkitReflection.needMethod(
                                AsyncTabCompleteEvent.Completion.class,
                                "completion",
                                String.class,
                                componentFromMessageMethod.getReturnType()
                        );

                final MethodHandle completionWithTooltip;
                final MethodHandle componentFromMessage;
                try {
                    componentFromMessage = MethodHandles.publicLookup().unreflect(componentFromMessageMethod);
                    completionWithTooltip = MethodHandles.publicLookup().unreflect(completionWithTooltipMethod);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                completionWithDescription = (suggestion, description) -> {
                    try {
                        final Object component = componentFromMessage.invoke(description);
                        return (AsyncTabCompleteEvent.Completion) completionWithTooltip.invoke(suggestion, component);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                };
            }
            this.completionsApplier = (event, list) -> {
                List<AsyncTabCompleteEvent.Completion> completions = new LinkedList<>();
                for (Completion completion : list) {
                    if (completion instanceof BrigadierCompletion) {
                        String suggest = completion.suggestion();
                        Message desc = ((BrigadierCompletion) completion).tooltip();
                        completions.add(completionWithDescription.apply(suggest, desc));
                    } else {
                        completions.add(AsyncTabCompleteEvent.Completion.completion(completion.suggestion()));
                    }
                }
                event.completions(completions);
                event.setHandled(true);
            };
        } else {
            this.completionsApplier = (event, list) -> {
                event.setCompletions(Completion.raw(list));
                event.setHandled(true);
            };
        }
    }

    @EventHandler
    void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) {
        // Strip leading slash
        final String strippedBuffer = event.getBuffer().startsWith("/")
                ? event.getBuffer().substring(1)
                : event.getBuffer();
        if (strippedBuffer.trim().isEmpty()) {
            return;
        }

        @SuppressWarnings("unchecked")
        final BukkitPluginRegistrationHandler<C> bukkitPluginRegistrationHandler =
                (BukkitPluginRegistrationHandler<C>) this.paperCommandManager.commandRegistrationHandler();

        /* Turn 'plugin:command arg1 arg2 ...' into 'plugin:command' */
        final String commandLabel = strippedBuffer.split(" ")[0];
        if (!bukkitPluginRegistrationHandler.isRecognized(commandLabel)) {
            return;
        }

        final CommandSender sender = event.getSender();
        final C cloudSender = this.paperCommandManager.getCommandSenderMapper().apply(sender);
        final String inputBuffer = this.paperCommandManager.stripNamespace(event.getBuffer());

        final List<Completion> completions = new ArrayList<>(this.paperCommandManager.completions(
                cloudSender,
                inputBuffer
        ));

        this.completionsApplier.accept(event, completions);
    }
}
