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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Opinionated extension of {@link CommandHelpHandler} for Minecraft
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class MinecraftHelp<C> {

    public static final HelpColors DEFAULT_HELP_COLORS = HelpColors.of(
            NamedTextColor.GOLD,
            NamedTextColor.GREEN,
            NamedTextColor.YELLOW,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY
    );

    public static final String MESSAGE_HELP = "help";
    public static final String MESSAGE_COMMAND = "command";
    public static final String MESSAGE_DESCRIPTION = "description";
    public static final String MESSAGE_NO_DESCRIPTION = "no_description";
    public static final String MESSAGE_ARGUMENTS = "arguments";
    public static final String MESSAGE_OPTIONAL = "optional";
    public static final String MESSAGE_UNKNOWN_HELP_TOPIC_TYPE = "unknown_help_topic_type";
    public static final String MESSAGE_SHOWING_RESULTS_FOR_QUERY = "showing_results_for_query";
    public static final String MESSAGE_AVAILABLE_COMMANDS = "available_commands";
    public static final String MESSAGE_CLICK_TO_SHOW_HELP = "click_to_show_help";

    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

    private final AudienceProvider<C> audienceProvider;
    private final CommandManager<C> commandManager;
    private final String commandPrefix;
    private final Map<String, String> messageMap = new HashMap<>();

    private HelpColors colors = DEFAULT_HELP_COLORS;
    private BiFunction<C, String, String> messageProvider = (sender, key) -> this.messageMap.get(key);

    /**
     * Construct a new Minecraft help instance
     *
     * @param commandPrefix    Command that was used to trigger the help menu. Used to help insertion generation
     * @param audienceProvider Provider that maps the command sender type to {@link Audience}
     * @param commandManager   Command manager instance
     */
    public MinecraftHelp(
            final @NonNull String commandPrefix,
            final @NonNull AudienceProvider<C> audienceProvider,
            final @NonNull CommandManager<C> commandManager
    ) {
        this.commandPrefix = commandPrefix;
        this.audienceProvider = audienceProvider;
        this.commandManager = commandManager;

        /* Default Messages */
        this.messageMap.put(MESSAGE_HELP, "Help");
        this.messageMap.put(MESSAGE_COMMAND, "Command");
        this.messageMap.put(MESSAGE_DESCRIPTION, "Description");
        this.messageMap.put(MESSAGE_NO_DESCRIPTION, "No description");
        this.messageMap.put(MESSAGE_ARGUMENTS, "Arguments");
        this.messageMap.put(MESSAGE_OPTIONAL, "Optional");
        this.messageMap.put(MESSAGE_UNKNOWN_HELP_TOPIC_TYPE, "Unknown help topic type");
        this.messageMap.put(MESSAGE_SHOWING_RESULTS_FOR_QUERY, "Showing search results for query");
        this.messageMap.put(MESSAGE_AVAILABLE_COMMANDS, "Available Commands");
        this.messageMap.put(MESSAGE_CLICK_TO_SHOW_HELP, "Click to show help for this command");
    }

    /**
     * Get the command manager instance
     *
     * @return Command manager
     */
    public @NonNull CommandManager<C> getCommandManager() {
        return this.commandManager;
    }

    /**
     * Get the audience provider that was used to create this instance
     *
     * @return Audience provider
     */
    public @NonNull AudienceProvider<C> getAudienceProvider() {
        return this.audienceProvider;
    }

    /**
     * Map a command sender to an {@link Audience}
     *
     * @param sender Sender to map
     * @return Mapped audience
     */
    public @NonNull Audience getAudience(final @NonNull C sender) {
        return this.audienceProvider.apply(sender);
    }

    /**
     * Configure a message
     *
     * @param key   Message key. These are constants in {@link MinecraftHelp}
     * @param value The text for the message
     */
    public void setMessage(
            final @NonNull String key,
            final @NonNull String value
    ) {
        this.messageMap.put(key, value);
    }

    /**
     * Set a custom message provider function to be used for getting messages from keys.
     * <p>
     * The keys are constants in {@link MinecraftHelp}.
     *
     * @param messageProvider The message provider to use
     */
    public void setMessageProvider(final @NonNull BiFunction<C, String, String> messageProvider) {
        this.messageProvider = messageProvider;
    }

    /**
     * Get the colors currently used for help messages.
     *
     * @return The current {@link HelpColors} for this {@link MinecraftHelp} instance
     */
    public @NonNull HelpColors getHelpColors() {
        return this.colors;
    }

    /**
     * Set the colors to use for help messages.
     *
     * @param colors The new {@link HelpColors} to use
     */
    public void setHelpColors(final @NonNull HelpColors colors) {
        this.colors = colors;
    }

    /**
     * Query commands and send the results to the recipient
     *
     * @param query     Command query (without leading '/')
     * @param recipient Recipient
     */
    public void queryCommands(
            final @NonNull String query,
            final @NonNull C recipient
    ) {
        final Audience audience = this.getAudience(recipient);
        audience.sendMessage(this.line(13)
                .append(Component.text(" " + this.messageProvider.apply(recipient, MESSAGE_HELP) + " ", this.colors.highlight))
                .append(this.line(13))
        );
        this.printTopic(recipient, query, this.commandManager.getCommandHelpHandler().queryHelp(recipient, query));
        audience.sendMessage(this.line(30));
    }

    private void printTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final CommandHelpHandler.@NonNull HelpTopic<C> helpTopic
    ) {
        this.getAudience(sender).sendMessage(
                Component.text(this.messageProvider.apply(sender, MESSAGE_SHOWING_RESULTS_FOR_QUERY) + ": \"", this.colors.text)
                        .append(this.highlight(Component.text("/" + query, this.colors.highlight)))
                        .append(Component.text("\"", this.colors.text))
        );
        if (helpTopic instanceof CommandHelpHandler.IndexHelpTopic) {
            this.printIndexHelpTopic(sender, (CommandHelpHandler.IndexHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.MultiHelpTopic) {
            this.printMultiHelpTopic(sender, (CommandHelpHandler.MultiHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.VerboseHelpTopic) {
            this.printVerboseHelpTopic(sender, (CommandHelpHandler.VerboseHelpTopic<C>) helpTopic);
        } else {
            throw new IllegalArgumentException(this.messageProvider.apply(sender, MESSAGE_UNKNOWN_HELP_TOPIC_TYPE));
        }
    }

    private void printIndexHelpTopic(
            final @NonNull C sender,
            final CommandHelpHandler.@NonNull IndexHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        audience.sendMessage(lastBranch()
                .append(Component.text(
                        String.format(" %s:", this.messageProvider.apply(sender, MESSAGE_AVAILABLE_COMMANDS)),
                        this.colors.text
                )));
        final Iterator<CommandHelpHandler.VerboseHelpEntry<C>> iterator = helpTopic.getEntries().iterator();
        while (iterator.hasNext()) {
            final CommandHelpHandler.VerboseHelpEntry<C> entry = iterator.next();

            final String description = entry.getDescription().isEmpty()
                    ? this.messageProvider.apply(sender, MESSAGE_CLICK_TO_SHOW_HELP)
                    : entry.getDescription();

            Component message = Component.text("   ")
                    .append(iterator.hasNext() ? branch() : lastBranch())
                    .append(this.highlight(Component.text(String.format(" /%s", entry.getSyntaxString()), this.colors.highlight))
                            .hoverEvent(Component.text(description, this.colors.text))
                            .clickEvent(ClickEvent.runCommand(this.commandPrefix + ' ' + entry.getSyntaxString())));

            audience.sendMessage(message);
        }
    }

    private void printMultiHelpTopic(
            final @NonNull C sender,
            final CommandHelpHandler.@NonNull MultiHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        audience.sendMessage(lastBranch()
                .append(this.highlight(Component.text(" /" + helpTopic.getLongestPath(), this.colors.highlight))));
        final int headerIndentation = helpTopic.getLongestPath().length();
        final Iterator<String> iterator = helpTopic.getChildSuggestions().iterator();
        while (iterator.hasNext()) {
            final String suggestion = iterator.next();

            final StringBuilder indentation = new StringBuilder();
            for (int i = 0; i < headerIndentation; i++) {
                indentation.append(" ");
            }

            audience.sendMessage(
                    Component.text(indentation.toString())
                            .append(iterator.hasNext() ? this.branch() : this.lastBranch())
                            .append(this.highlight(Component.text(" /" + suggestion, this.colors.highlight))
                                    .hoverEvent(Component.text(
                                            this.messageProvider.apply(sender, MESSAGE_CLICK_TO_SHOW_HELP),
                                            this.colors.text
                                    ))
                                    .clickEvent(ClickEvent.runCommand(this.commandPrefix + ' ' + suggestion)))
            );
        }
    }

    private void printVerboseHelpTopic(
            final @NonNull C sender,
            final CommandHelpHandler.@NonNull VerboseHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        final String command = this.commandManager.getCommandSyntaxFormatter()
                .apply(helpTopic.getCommand().getArguments(), null);
        audience.sendMessage(
                this.lastBranch()
                        .append(Component.text(
                                " " + this.messageProvider.apply(sender, MESSAGE_COMMAND) + ": ",
                                this.colors.primary
                        ))
                        .append(this.highlight(Component.text("/" + command, this.colors.highlight)))
        );
        final String topicDescription = helpTopic.getDescription().isEmpty()
                ? this.messageProvider.apply(sender, MESSAGE_NO_DESCRIPTION)
                : helpTopic.getDescription();
        final boolean hasArguments = helpTopic.getCommand().getArguments().size() > 1;
        audience.sendMessage(
                Component.text("   ")
                        .append(hasArguments ? this.branch() : this.lastBranch())
                        .append(Component.text(
                                " " + this.messageProvider.apply(sender, MESSAGE_DESCRIPTION) + ": ",
                                this.colors.primary
                        ))
                        .append(Component.text(topicDescription, this.colors.text))
        );
        if (hasArguments) {
            audience.sendMessage(
                    Component.text("   ")
                            .append(this.lastBranch())
                            .append(Component.text(
                                    " " + this.messageProvider.apply(sender, MESSAGE_ARGUMENTS) + ":",
                                    this.colors.primary
                            ))
            );

            final Iterator<CommandArgument<C, ?>> iterator = helpTopic.getCommand().getArguments().iterator();
            /* Skip the first one because it's the command literal */
            iterator.next();

            while (iterator.hasNext()) {
                final CommandArgument<C, ?> argument = iterator.next();

                String syntax = this.commandManager.getCommandSyntaxFormatter()
                        .apply(Collections.singletonList(argument), null);

                final TextComponent.Builder component = Component.text()
                        .append(Component.text("       "))
                        .append(iterator.hasNext() ? this.branch() : this.lastBranch())
                        .append(this.highlight(Component.text(" " + syntax, this.colors.highlight)));
                if (!argument.isRequired()) {
                    component.append(Component.text(
                            " (" + this.messageProvider.apply(sender, MESSAGE_OPTIONAL) + ")",
                            this.colors.alternateHighlight
                    ));
                }
                final String description = helpTopic.getCommand().getArgumentDescription(argument);
                if (!description.isEmpty()) {
                    component
                            .append(Component.text(" - ", this.colors.accent))
                            .append(Component.text(description, this.colors.text));
                }

                audience.sendMessage(component);
            }
        }
    }

    private @NonNull Component line(final int length) {
        final TextComponent.Builder line = Component.text();
        for (int i = 0; i < length; i++) {
            line.append(Component.text("-", this.colors.primary, TextDecoration.STRIKETHROUGH));
        }
        return line.build();
    }

    private @NonNull Component branch() {
        return Component.text("├─", this.colors.accent);
    }

    private @NonNull Component lastBranch() {
        return Component.text("└─", this.colors.accent);
    }

    private @NonNull Component highlight(final @NonNull Component component) {
        return component.replaceText(
                SPECIAL_CHARACTERS_PATTERN,
                match -> match.color(this.colors.alternateHighlight)
        );
    }

    /**
     * Class for holding the {@link TextColor}s used for help menus
     */
    public static final class HelpColors {

        private final TextColor primary;
        private final TextColor highlight;
        private final TextColor alternateHighlight;
        private final TextColor text;
        private final TextColor accent;

        private HelpColors(
                final @NonNull TextColor primary,
                final @NonNull TextColor highlight,
                final @NonNull TextColor alternateHighlight,
                final @NonNull TextColor text,
                final @NonNull TextColor accent
        ) {
            this.primary = primary;
            this.highlight = highlight;
            this.alternateHighlight = alternateHighlight;
            this.text = text;
            this.accent = accent;
        }

        /**
         * @return The primary color for the color scheme
         */
        public @NonNull TextColor primary() {
            return this.primary;
        }

        /**
         * @return The primary color used to highlight commands and queries
         */
        public @NonNull TextColor highlight() {
            return this.highlight;
        }

        /**
         * @return The secondary color used to highlight commands and queries
         */
        public @NonNull TextColor alternateHighlight() {
            return this.alternateHighlight;
        }

        /**
         * @return The color used for description text
         */
        public @NonNull TextColor text() {
            return this.text;
        }

        /**
         * @return The color used for accents and symbols
         */
        public @NonNull TextColor accent() {
            return this.accent;
        }

        /**
         * @param primary            The primary color for the color scheme
         * @param highlight          The primary color used to highlight commands and queries
         * @param alternateHighlight The secondary color used to highlight commands and queries
         * @param text               The color used for description text
         * @param accent             The color used for accents and symbols
         * @return A new {@link HelpColors} instance
         */
        public static HelpColors of(
                final @NonNull TextColor primary,
                final @NonNull TextColor highlight,
                final @NonNull TextColor alternateHighlight,
                final @NonNull TextColor text,
                final @NonNull TextColor accent
        ) {
            return new HelpColors(primary, highlight, alternateHighlight, text, accent);
        }

    }

}
