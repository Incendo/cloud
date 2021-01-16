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
package cloud.commandframework.examples.javacord.components;

import cloud.commandframework.Command;
import cloud.commandframework.examples.javacord.modules.ExampleModule;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.javacord.JavacordCommandManager;
import cloud.commandframework.javacord.sender.JavacordCommandSender;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import dev.simplix.core.common.aop.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

@Component(ExampleModule.class)
public class CommandsComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CommandsComponent.class);
    private static final String COMMAND_PREFIX = "!";

    private final DiscordApiComponent discordApiComponent;

    private JavacordCommandManager<@NonNull JavacordCommandSender> commandManager;

    /**
     * Creates a new instance of the CommandsComponent used for registering commands. Done automatically
     *
     * @param discordApiComponent Instance of the {@link DiscordApiComponent} for registering the command listener
     */
    @Inject
    public CommandsComponent(final @NonNull DiscordApiComponent discordApiComponent) {
        this.discordApiComponent = discordApiComponent;
    }

    /**
     * Creates a new {@link JavacordCommandManager} instance
     *
     * @throws Exception If something went wrong while instantiating the CommandManager
     */
    public final void initializeCommandManager() throws Exception {
        Preconditions.checkArgument(
                this.commandManager == null,
                "The CommandManager instance has already been created!"
        );

        LOG.info("Instantiating new Command Manager");

        this.commandManager = new JavacordCommandManager<>(
                this.discordApiComponent.getDiscordApi(),
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity(),
                sender -> COMMAND_PREFIX,
                null
        );
    }

    /**
     * Registers a new {@link Command}
     *
     * @param command Command which should be registered
     */
    public final void registerCommand(final @NonNull Command<@NonNull JavacordCommandSender> command) {
        LOG.info("Registering Command: " + command.getArguments().get(0).getName());

        this.getCommandManager().command(command);
    }

    /**
     * Gets the {@link JavacordCommandManager} instance
     *
     * @return The CommandManager
     */
    public final @NonNull JavacordCommandManager<@NonNull JavacordCommandSender> getCommandManager() {
        Preconditions.checkNotNull(this.commandManager, "Command Manager has not been initialized yet");
        return this.commandManager;
    }

}
