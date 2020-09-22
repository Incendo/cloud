//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.extra.confirmation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.execution.CommandExecutionHandler;
import com.intellectualsites.commands.execution.postprocessor.CommandPostprocessingContext;
import com.intellectualsites.commands.execution.postprocessor.CommandPostprocessor;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.services.types.ConsumerService;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manager for the command confirmation system that enables the ability to add "confirmation" requirements to commands,
 * such that they need to be confirmed in order to be executed.
 * <p>
 * To use the confirmation system, the confirmation post processor needs to be added. To to this, use
 * {@link #registerConfirmationProcessor(CommandManager)}. After this is done, a confirmation command has
 * been added. To do this, create a command builder and attach {@link #createConfirmationExecutionHandler()}.
 * <p>
 * To require a command to be confirmed, use {@link #decorate(SimpleCommandMeta.Builder)} on the command meta builder.
 *
 * @param <C> Command sender type
 */
public class CommandConfirmationManager<C> {

    /**
     * Meta data stored for commands that require confirmation
     */
    public static final String CONFIRMATION_REQUIRED_META = "__REQUIRE_CONFIRMATION__";

    private final Consumer<CommandPostprocessingContext<C>> notifier;
    private final Consumer<C> errorNotifier;
    private final Cache<C, CommandPostprocessingContext<C>> pendingCommands;

    /**
     * Create a new confirmation manager instance
     *
     * @param timeout         Timeout value
     * @param timeoutTimeUnit Timeout time unit
     * @param notifier        Notifier that gets called when a command gets added to the queue
     * @param errorNotifier   Notifier that gets called when someone tries to confirm a command with nothing in the queue
     */
    public CommandConfirmationManager(final long timeout,
                                      @Nonnull final TimeUnit timeoutTimeUnit,
                                      @Nonnull final Consumer<CommandPostprocessingContext<C>> notifier,
                                      @Nonnull final Consumer<C> errorNotifier) {
        this.notifier = notifier;
        this.errorNotifier = errorNotifier;
        this.pendingCommands = CacheBuilder.newBuilder().expireAfterWrite(timeout, timeoutTimeUnit).concurrencyLevel(1).build();
    }

    private void notifyConsumer(@Nonnull final CommandPostprocessingContext<C> context) {
        this.notifier.accept(context);
    }

    private void addPending(@Nonnull final CommandPostprocessingContext<C> context) {
        this.pendingCommands.put(context.getCommandContext().getSender(), context);
    }

    /**
     * Get a pending context if one is stored for the sender
     *
     * @param sender Sender
     * @return Optional containing the post processing context if one has been stored, else {@link Optional#empty()}
     */
    @Nonnull
    public Optional<CommandPostprocessingContext<C>> getPending(@Nonnull final C sender) {
        return Optional.ofNullable(this.pendingCommands.getIfPresent(sender));
    }

    /**
     * Decorate a simple command meta builder, to require confirmation for a command
     *
     * @param builder Command meta builder
     * @return Builder instance
     */
    @Nonnull
    public SimpleCommandMeta.Builder decorate(@Nonnull final SimpleCommandMeta.Builder builder) {
        return builder.with(CONFIRMATION_REQUIRED_META, "true");
    }

    /**
     * Register the confirmation processor in the command manager
     *
     * @param manager Command manager
     */
    public void registerConfirmationProcessor(@Nonnull final CommandManager<C> manager) {
        manager.registerCommandPostProcessor(new CommandConfirmationPostProcessor());
    }

    /**
     * Create an execution handler for a confirmation command
     *
     * @return Handler for a confirmation command
     */
    @Nonnull
    public CommandExecutionHandler<C> createConfirmationExecutionHandler() {
        return context -> {
            final Optional<CommandPostprocessingContext<C>> pending = this.getPending(context.getSender());
            if (pending.isPresent()) {
                final CommandPostprocessingContext<C> postprocessingContext = pending.get();
                postprocessingContext.getCommand()
                                     .getCommandExecutionHandler()
                                     .execute(postprocessingContext.getCommandContext());
            } else {
                this.errorNotifier.accept(context.getSender());
            }
        };
    }


    private final class CommandConfirmationPostProcessor implements CommandPostprocessor<C> {

        @Override
        public void accept(@Nonnull final CommandPostprocessingContext<C> context) {
            if (!context.getCommand()
                        .getCommandMeta()
                        .getOrDefault(CONFIRMATION_REQUIRED_META, "false")
                        .equals("true")) {
                return;
            }
            /* Add it to the "queue" */
            addPending(context);
            /* Notify the consumer that a confirmation is required */
            notifyConsumer(context);
            /* Interrupt */
            ConsumerService.interrupt();
        }

    }

}
