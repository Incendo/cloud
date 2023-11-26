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
package cloud.commandframework.examples.bukkit.feature;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.services.types.ConsumerService;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example showcasing how to use the pre- &amp; post-processing systems.
 */
public final class ProcessorExample {

    public ProcessorExample(
            final @NonNull CommandManager<CommandSender> manager,
            final @NonNull Logger logger
    ) {
        manager.registerCommandPreProcessor(new ExamplePreProcessor(logger));
        manager.registerCommandPostProcessor(new ExamplePostProcessor());
    }


    /**
     * Pre-processor that logs the command input before it's being processed.
     */
    public static final class ExamplePreProcessor implements CommandPreprocessor<CommandSender> {

        private final Logger logger;

        public ExamplePreProcessor(final @NonNull Logger logger) {
            this.logger = logger;
        }

        @Override
        public void accept(final @NonNull CommandPreprocessingContext<CommandSender> context) {
            this.logger.fine("Command input: " + context.commandInput().input());
        }
    }

    /**
     * Example post-processor that disallows the execution of the command if the sender is not wearing boots
     * and the command has the required metadata set.
     */
    public static final class ExamplePostProcessor implements CommandPostprocessor<CommandSender> {

        private static final CommandMeta.Key<Boolean> REQUIRES_BOOTS = CommandMeta.Key.of(Boolean.class, "requires_boots");

        @Override
        public void accept(final @NonNull CommandPostprocessingContext<CommandSender> context) {
            final Command<CommandSender> command = context.getCommand();
            if (!command.commandMeta().getOrDefault(REQUIRES_BOOTS, false)) {
                return;
            }
            final CommandSender sender = context.getCommandContext().getSender();
            if (!(sender instanceof Player)) {
                return;
            }
            final Player player = (Player) sender;
            if (player.getInventory().getBoots() != null) {
                return;
            }
            player.sendMessage("You need to wear boots to execute this command!");
            ConsumerService.interrupt();
        }
    }
}
