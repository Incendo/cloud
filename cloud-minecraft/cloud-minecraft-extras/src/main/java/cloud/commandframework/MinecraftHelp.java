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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Opinionated extension of {@link CommandHelpHandler} for Minecraft
 *
 * @param <C> Command sender type
 */
public final class MinecraftHelp<C> {

    /* General help */
    public static final String MESSAGE_HELP_HEADER = "help_header";
    public static final String MESSAGE_HELP_FOOTER = "help_footer";
    /* Query specific */
    public static final String MESSAGE_QUERY_QUERY = "help_query_query";
    public static final String MESSAGE_QUERY_AVAILABLE_COMMANDS = "help_query_available_comments";
    public static final String MESSAGE_QUERY_COMMAND_SYNTAX = "help_query_command_syntax";
    public static final String MESSAGE_QUERY_COMMAND_SYNTAX_LAST = "help_query_command_syntax_last";
    public static final String MESSAGE_QUERY_LONGEST_PATH = "help_query_longest_path";
    public static final String MESSAGE_QUERY_SUGGESTION = "help_query_suggestion";
    public static final String MESSAGE_QUERY_VERBOSE_SYNTAX = "help_query_verbose_syntax";
    public static final String MESSAGE_QUERY_VERBOSE_DESCRIPTION = "help_query_verbose_description";
    public static final String MESSAGE_QUERY_VERBOSE_ARGS = "help_query_verbose_args";
    public static final String MESSAGE_QUERY_VERBOSE_OPTIONAL = "help_query_verbose_optional";
    public static final String MESSAGE_QUERY_VERBOSE_REQUIRED = "help_query_verbose_required";
    public static final String MESSAGE_QUERY_VERBOSE_LITERAL = "help_query_verbose_literal";

    private final MiniMessage miniMessage = MiniMessage.builder().build();
    private final Map<String, String> messageMap = new HashMap<>();

    private final AudienceProvider<C> audienceProvider;
    private final CommandManager<C> commandManager;
    private final String commandPrefix;

    /**
     * Construct a new Minecraft help instance
     *
     * @param commandPrefix    Command that was used to trigger the help menu. Used to help insertion generation
     * @param audienceProvider Provider that maps the command sender type to {@link Audience}
     * @param commandManager   Command manager instance
     */
    public MinecraftHelp(@Nonnull final String commandPrefix,
                         @Nonnull final AudienceProvider<C> audienceProvider,
                         @Nonnull final CommandManager<C> commandManager) {
        this.commandPrefix = commandPrefix;
        this.audienceProvider = audienceProvider;
        this.commandManager = commandManager;
        /* Default messages */
        this.messageMap.put(MESSAGE_HELP_HEADER, "<gold><bold>------------ Help ------------</bold></gold>");
        this.messageMap.put(MESSAGE_HELP_FOOTER, "<gold><bold>----------------------------</bold></gold>");
        this.messageMap.put(MESSAGE_QUERY_QUERY, "<gray>Showing search results for query: \"<green>/<query></green>\"</gray>");
        this.messageMap.put(MESSAGE_QUERY_AVAILABLE_COMMANDS, "<dark_gray>└─</dark_gray><gray> Available Commands:</gray>");
        this.messageMap.put(MESSAGE_QUERY_COMMAND_SYNTAX, "<dark_gray>   ├─</dark_gray> <green>"
        + "<click:run_command:cmdprefix><hover:show_text:\"<gray><description></gray>\">/<command></hover></click></green>");
        this.messageMap.put(MESSAGE_QUERY_COMMAND_SYNTAX_LAST, "<dark_gray>   └─</dark_gray> <green>"
        + "<click:run_command:cmdprefix><hover:show_text:\"<gray><description></gray>\">/<command></hover></click></green>");
        this.messageMap.put(MESSAGE_QUERY_LONGEST_PATH, "<dark_gray>└─</dark_gray> <green>/<command></green>");
        this.messageMap.put(MESSAGE_QUERY_SUGGESTION, "<dark_gray><indentation><prefix></dark_gray> <green><click:run_command:"
                + "<cmdprefix>><hover:show_text:\"<gray>Click to show help for this command</gray>\">"
                + "<suggestion></hover></click></green>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_SYNTAX, "<dark_gray>└─</dark_gray> <gold>Command:"
                + " </gold><green>/<command></green>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_DESCRIPTION, "<dark_gray>   ├─</dark_gray>"
                + " <gold>Description:</gold> <gray><description></gray>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_ARGS, "<dark_gray>   └─</dark_gray> <gold>Args:</gold>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_OPTIONAL, "<dark_gray>       <prefix></dark_gray> <white><syntax><white> "
                + "<yellow>(Optional)</yellow> <dark_gray>-</dark_gray> <gray><description></gray>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_REQUIRED, "<dark_gray>       <prefix></dark_gray> <white><syntax><white> "
                + "<dark_gray>-</dark_gray> <gray><description></gray>");
        this.messageMap.put(MESSAGE_QUERY_VERBOSE_LITERAL, "<gray><syntax></gray>");
    }

    /**
     * Get the command manager instance
     *
     * @return Command manager
     */
    @Nonnull
    public CommandManager<C> getCommandManager() {
        return this.commandManager;
    }

    /**
     * Get the audience provider that was used to create this instance
     *
     * @return Audience provider
     */
    @Nonnull
    public AudienceProvider<C> getAudienceProvider() {
        return this.audienceProvider;
    }

    /**
     * Map a command sender to an {@link Audience}
     *
     * @param sender Sender to map
     * @return Mapped audience
     */
    @Nonnull
    public Audience getAudience(@Nonnull final C sender) {
        return this.audienceProvider.apply(sender);
    }

    /**
     * Configure a message
     *
     * @param key     Message key
     * @param message Message
     */
    public void setMessage(@Nonnull final String key, @Nonnull final String message) {
        this.messageMap.put(key, message);
    }

    /**
     * Query commands and send the results to the recipient
     *
     * @param query     Command query (without leading '/')
     * @param recipient Recipient
     */
    public void queryCommands(@Nonnull final String query,
                              @Nonnull final C recipient) {
        final Audience audience = this.getAudience(recipient);
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_HELP_HEADER)));
        this.printTopic(recipient, query, this.commandManager.getCommandHelpHandler().queryHelp(query));
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_HELP_FOOTER)));
    }

    private void printTopic(@Nonnull final C sender,
                            @Nonnull final String query,
                            @Nonnull final CommandHelpHandler.HelpTopic<C> helpTopic) {
        this.getAudience(sender).sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_QUERY),
                                                                    Template.of("query", query)));
        if (helpTopic instanceof CommandHelpHandler.IndexHelpTopic) {
            this.printIndexHelpTopic(sender, (CommandHelpHandler.IndexHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.MultiHelpTopic) {
            this.printMultiHelpTopic(sender, (CommandHelpHandler.MultiHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.VerboseHelpTopic) {
            this.printVerboseHelpTopic(sender, (CommandHelpHandler.VerboseHelpTopic<C>) helpTopic);
        } else {
            throw new IllegalArgumentException("Unknown help topic type");
        }
    }

    private void printIndexHelpTopic(@Nonnull final C sender, @Nonnull final CommandHelpHandler.IndexHelpTopic<C> helpTopic) {
        final Audience audience = this.getAudience(sender);
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_AVAILABLE_COMMANDS)));
        final Iterator<CommandHelpHandler.VerboseHelpEntry<C>> iterator = helpTopic.getEntries().iterator();
        while (iterator.hasNext()) {
            final CommandHelpHandler.VerboseHelpEntry<C> entry = iterator.next();

            final String description = entry.getDescription().isEmpty() ? "Click to show help for this command"
                                                                        : entry.getDescription();

            String message = this.messageMap.get(iterator.hasNext() ? MESSAGE_QUERY_COMMAND_SYNTAX
                                                                    : MESSAGE_QUERY_COMMAND_SYNTAX_LAST);

            final String suggestedCommand = entry.getSyntaxString()
                                                 .replace("<", "")
                                                 .replace(">", "")
                                                 .replace("[", "")
                                                 .replace("]", "");

            message = message.replace("<command>", entry.getSyntaxString())
                             .replace("<description>", description)
                             .replace("cmdprefix", this.commandPrefix + ' ' + suggestedCommand);

            audience.sendMessage(this.miniMessage.parse(message));
        }
    }

    private void printMultiHelpTopic(@Nonnull final C sender, @Nonnull final CommandHelpHandler.MultiHelpTopic<C> helpTopic) {
        final Audience audience = this.getAudience(sender);
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_LONGEST_PATH),
                                                    Template.of("command", helpTopic.getLongestPath())));
        final int headerIndentation = helpTopic.getLongestPath().length();
        final Iterator<String> iterator = helpTopic.getChildSuggestions().iterator();
        while (iterator.hasNext()) {
            final String suggestion = iterator.next();

            final StringBuilder indentation = new StringBuilder();
            for (int i = 0; i < headerIndentation; i++) {
                indentation.append(" ");
            }

            final String prefix;
            if (iterator.hasNext()) {
                prefix = "├─";
            } else {
                prefix = "└─";
            }

            final String suggestedCommand = suggestion.replace("<", "")
                                                      .replace(">", "")
                                                      .replace("[", "")
                                                      .replace("]", "");

            audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_SUGGESTION),
                                                        Template.of("indentation", indentation.toString()),
                                                        Template.of("prefix", prefix),
                                                        Template.of("suggestion", suggestion),
                                                        Template.of("cmdprefix", this.commandPrefix + ' ' + suggestedCommand)));
        }
    }

    private void printVerboseHelpTopic(@Nonnull final C sender, @Nonnull final CommandHelpHandler.VerboseHelpTopic<C> helpTopic) {
        final Audience audience = this.getAudience(sender);
        final String command = this.commandManager.getCommandSyntaxFormatter()
                                                  .apply(helpTopic.getCommand().getArguments(), null);
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_VERBOSE_SYNTAX),
                                                    Template.of("command", command)));
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_VERBOSE_DESCRIPTION),
                                                    Template.of("description", helpTopic.getDescription())));
        audience.sendMessage(this.miniMessage.parse(this.messageMap.get(MESSAGE_QUERY_VERBOSE_ARGS)));

        final Iterator<CommandArgument<C, ?>> iterator = helpTopic.getCommand().getArguments().iterator();
        /* Skip the first one because it's the command literal */
        iterator.next();

        boolean hasLast;

        while (iterator.hasNext()) {
            final CommandArgument<C, ?> argument = iterator.next();

            String description = helpTopic.getCommand().getArgumentDescription(argument);
            if (description.isEmpty()) {
                description = "No description";
            }

            String syntax = this.commandManager.getCommandSyntaxFormatter()
                                               .apply(Collections.singletonList(argument), null);

            if (argument instanceof StaticArgument) {
                syntax = this.messageMap.get(MESSAGE_QUERY_VERBOSE_LITERAL).replace("<syntax>", syntax);
            }

            final String prefix = iterator.hasNext() ? "├─" : "└─";
            hasLast = !iterator.hasNext();

            final String message;
            if (argument.isRequired()) {
                message = this.messageMap.get(MESSAGE_QUERY_VERBOSE_REQUIRED);
            } else {
                message = this.messageMap.get(MESSAGE_QUERY_VERBOSE_OPTIONAL);
            }

            audience.sendMessage(this.miniMessage.parse(message,
                                                        Template.of("prefix", prefix),
                                                        Template.of("syntax", syntax),
                                                        Template.of("description", description)));
        }
    }

}
