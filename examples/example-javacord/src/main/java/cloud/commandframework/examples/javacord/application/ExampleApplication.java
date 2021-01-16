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
package cloud.commandframework.examples.javacord.application;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.examples.javacord.components.CommandsComponent;
import cloud.commandframework.examples.javacord.components.DiscordApiComponent;
import cloud.commandframework.examples.javacord.modules.ExampleModule;
import cloud.commandframework.javacord.sender.JavacordPrivateSender;
import com.google.inject.Inject;
import dev.simplix.core.common.aop.AlwaysConstruct;
import dev.simplix.core.common.aop.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.entity.message.MessageAuthor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component(ExampleModule.class)
@AlwaysConstruct
public class ExampleApplication {

    private final DiscordApiComponent discordApiComponent;
    private final CommandsComponent commandsComponent;
    private final ScheduledExecutorService executorService;


    /**
     * Starts a new ExampleBot. Done automatically
     *
     * @param discordApiComponent Instance of the {@link DiscordApiComponent} required for logging in to Discord
     * @param commandsComponent   Instance of the {@link CommandsComponent} used for registering commands
     * @param executorService     {@link java.util.concurrent.ScheduledThreadPoolExecutor} instance for scheduling tasks
     * @throws Exception if something went wrong while starting the bot
     */
    @Inject
    public ExampleApplication(
            final @NonNull DiscordApiComponent discordApiComponent,
            final @NonNull CommandsComponent commandsComponent,
            final @NonNull ScheduledExecutorService executorService
    ) throws
            Exception {
        this.discordApiComponent = discordApiComponent;
        this.commandsComponent = commandsComponent;
        this.executorService = executorService;

        this.connectToDiscord();
        this.registerCommands();
    }

    private void connectToDiscord() {
        this.discordApiComponent.login(System.getProperty("bot.token"));
    }

    @SuppressWarnings("ConstantConditions")
    private void registerCommands() throws Exception {
        this.commandsComponent.initializeCommandManager();

        this.commandsComponent.registerCommand(this.commandsComponent.getCommandManager()
                .commandBuilder("ping")
                .handler(context -> context.getSender().sendMessage("Pong!")).build());

        this.commandsComponent.registerCommand(this.commandsComponent.getCommandManager()
                .commandBuilder("shutdown", "sd")
                .flag(this.commandsComponent.getCommandManager()
                        .flagBuilder("time")
                        .withAliases("t")
                        .withArgument(IntegerArgument.optional("time", 60)))
                .senderType(JavacordPrivateSender.class)
                .handler(context -> {
                    final MessageAuthor messageAuthor = context.getSender().getAuthor();
                    messageAuthor.asUser().ifPresent(user -> {
                        if (!user.getIdAsString().equals(System.getProperty("bot.owner"))) {
                            context.getSender().sendErrorMessage("You need to be the owner of the bot to do this!");
                            return;
                        }

                        final int time = context.flags().getValue("time", 60);

                        if (time > 0) {
                            context.getSender().sendSuccessMessage("Shutting down in " + time + " seconds!");
                            this.executorService.schedule(this::shutdown, time, TimeUnit.SECONDS);
                            return;
                        }

                        context.getSender().sendSuccessMessage("Shutting down now...");
                        this.shutdown();
                    });
                })
                .build());
    }

    /**
     * Shuts off the bot completely. You should not do anything after executing this method!
     */
    public void shutdown() {
        this.executorService.shutdown();
        this.discordApiComponent.shutdown();
        System.exit(0);
    }

}
