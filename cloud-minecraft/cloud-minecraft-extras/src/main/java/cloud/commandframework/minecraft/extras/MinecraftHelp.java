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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Opinionated extension of {@link CommandHelpHandler} for Minecraft
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class MinecraftHelp<C> {

    public static final int DEFAULT_HEADER_FOOTER_LENGTH = 46;
    public static final int DEFAULT_MAX_RESULTS_PER_PAGE = 6;

    /**
     * The default color scheme for {@link MinecraftHelp}
     */
    public static final HelpColors DEFAULT_HELP_COLORS = HelpColors.of(
            NamedTextColor.GOLD,
            NamedTextColor.GREEN,
            NamedTextColor.YELLOW,
            NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY
    );

    public static final String MESSAGE_HELP_TITLE = "help";
    public static final String MESSAGE_COMMAND = "command";
    public static final String MESSAGE_DESCRIPTION = "description";
    public static final String MESSAGE_NO_DESCRIPTION = "no_description";
    public static final String MESSAGE_ARGUMENTS = "arguments";
    public static final String MESSAGE_OPTIONAL = "optional";
    public static final String MESSAGE_SHOWING_RESULTS_FOR_QUERY = "showing_results_for_query";
    public static final String MESSAGE_NO_RESULTS_FOR_QUERY = "no_results_for_query";
    public static final String MESSAGE_AVAILABLE_COMMANDS = "available_commands";
    public static final String MESSAGE_CLICK_TO_SHOW_HELP = "click_to_show_help";
    public static final String MESSAGE_PAGE_OUT_OF_RANGE = "page_out_of_range";
    public static final String MESSAGE_CLICK_FOR_NEXT_PAGE = "click_for_next_page";
    public static final String MESSAGE_CLICK_FOR_PREVIOUS_PAGE = "click_for_previous_page";

    private final AudienceProvider<C> audienceProvider;
    private final CommandManager<C> commandManager;
    private final String commandPrefix;
    private final Map<String, String> messageMap = new HashMap<>();

    private BiFunction<C, String, String> messageProvider = (sender, key) -> this.messageMap.get(key);
    private HelpColors colors = DEFAULT_HELP_COLORS;
    private int headerFooterLength = DEFAULT_HEADER_FOOTER_LENGTH;
    private int maxResultsPerPage = DEFAULT_MAX_RESULTS_PER_PAGE;

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
        this.messageMap.put(MESSAGE_HELP_TITLE, "Help");
        this.messageMap.put(MESSAGE_COMMAND, "Command");
        this.messageMap.put(MESSAGE_DESCRIPTION, "Description");
        this.messageMap.put(MESSAGE_NO_DESCRIPTION, "No description");
        this.messageMap.put(MESSAGE_ARGUMENTS, "Arguments");
        this.messageMap.put(MESSAGE_OPTIONAL, "Optional");
        this.messageMap.put(MESSAGE_SHOWING_RESULTS_FOR_QUERY, "Showing search results for query");
        this.messageMap.put(MESSAGE_NO_RESULTS_FOR_QUERY, "No results for query");
        this.messageMap.put(MESSAGE_AVAILABLE_COMMANDS, "Available Commands");
        this.messageMap.put(MESSAGE_CLICK_TO_SHOW_HELP, "Click to show help for this command");
        this.messageMap.put(MESSAGE_PAGE_OUT_OF_RANGE, "Error: Page <page> is not in range. Must be in range [1, <max_pages>]");
        this.messageMap.put(MESSAGE_CLICK_FOR_NEXT_PAGE, "Click for next page");
        this.messageMap.put(MESSAGE_CLICK_FOR_PREVIOUS_PAGE, "Click for previous page");
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
     * Set the colors to use for help messages.
     *
     * @param colors The new {@link HelpColors} to use
     */
    public void setHelpColors(final @NonNull HelpColors colors) {
        this.colors = colors;
    }

    /**
     * Get the colors used for help messages.
     *
     * @return The active {@link HelpColors}
     */
    public @NonNull HelpColors getHelpColors() {
        return this.colors;
    }

    /**
     * Set the length of the header/footer of help menus
     * <p>
     * Defaults to {@link MinecraftHelp#DEFAULT_HEADER_FOOTER_LENGTH}
     *
     * @param headerFooterLength The new length
     */
    public void setHeaderFooterLength(final int headerFooterLength) {
        this.headerFooterLength = headerFooterLength;
    }

    /**
     * Set the maximum number of help results to display on one page
     * <p>
     * Defaults to {@link MinecraftHelp#DEFAULT_MAX_RESULTS_PER_PAGE}
     *
     * @param maxResultsPerPage The new value
     */
    public void setMaxResultsPerPage(final int maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
    }

    /**
     * Query commands and send the results to the recipient. Will respect permissions.
     *
     * @param rawQuery  Command query (without leading '/', including optional page number)
     * @param recipient Recipient
     */
    public void queryCommands(
            final @NonNull String rawQuery,
            final @NonNull C recipient
    ) {
        final String[] splitQuery = rawQuery.split(" ");
        int page;
        String query;
        try {
            final String pageText = splitQuery[splitQuery.length - 1];
            page = Integer.parseInt(pageText);
            query = rawQuery.substring(0, Math.max(rawQuery.lastIndexOf(pageText) - 1, 0));
        } catch (NumberFormatException e) {
            page = 1;
            query = rawQuery;
        }
        final Audience audience = this.getAudience(recipient);
        this.printTopic(
                recipient,
                query,
                page,
                this.commandManager.getCommandHelpHandler().queryHelp(recipient, query)
        );
    }

    private void printTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final CommandHelpHandler.@NonNull HelpTopic<C> helpTopic
    ) {
        if (helpTopic instanceof CommandHelpHandler.IndexHelpTopic) {
            this.printIndexHelpTopic(sender, query, page, (CommandHelpHandler.IndexHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.MultiHelpTopic) {
            this.printMultiHelpTopic(sender, query, page, (CommandHelpHandler.MultiHelpTopic<C>) helpTopic);
        } else if (helpTopic instanceof CommandHelpHandler.VerboseHelpTopic) {
            this.printVerboseHelpTopic(sender, query, (CommandHelpHandler.VerboseHelpTopic<C>) helpTopic);
        } else {
            throw new IllegalArgumentException("Unknown help topic type");
        }
    }

    private void printIndexHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final CommandHelpHandler.@NonNull IndexHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        if (helpTopic.isEmpty()) {
            audience.sendMessage(this.basicHeader(sender));
            audience.sendMessage(Component.text(
                    this.messageProvider.apply(sender, MESSAGE_NO_RESULTS_FOR_QUERY) + ": \"",
                    this.colors.text
            )
                    .append(this.highlight(Component.text("/" + query, this.colors.highlight)))
                    .append(Component.text("\"", this.colors.text)));
            audience.sendMessage(this.footer(sender));
            return;
        }
        new Pagination<CommandHelpHandler.VerboseHelpEntry<C>>(
                (currentPage, maxPages) -> {
                    final List<Component> header = new ArrayList<>();
                    header.add(this.paginatedHeader(sender, currentPage, maxPages));
                    header.add(this.showingResults(sender, query));
                    header.add(this.lastBranch()
                            .append(Component.text(
                                    String.format(" %s:", this.messageProvider.apply(sender, MESSAGE_AVAILABLE_COMMANDS)),
                                    this.colors.text
                            )));
                    return header;
                },
                (helpEntry, isLastOfPage) -> {
                    final String description = helpEntry.getDescription().isEmpty()
                            ? this.messageProvider.apply(sender, MESSAGE_CLICK_TO_SHOW_HELP)
                            : helpEntry.getDescription();

                    final boolean lastBranch =
                            isLastOfPage || helpTopic.getEntries().indexOf(helpEntry) == helpTopic.getEntries().size() - 1;

                    return Component.text("   ")
                            .append(lastBranch ? this.lastBranch() : this.branch())
                            .append(this.highlight(Component.text(
                                    String.format(" /%s", helpEntry.getSyntaxString()),
                                    this.colors.highlight
                            ))
                                    .hoverEvent(Component.text(description, this.colors.text))
                                    .clickEvent(ClickEvent.runCommand(this.commandPrefix + " " + helpEntry.getSyntaxString())));
                },
                (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
                (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
        ).render(helpTopic.getEntries(), page, this.maxResultsPerPage).forEach(audience::sendMessage);
    }

    private void printMultiHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final CommandHelpHandler.@NonNull MultiHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        final int headerIndentation = helpTopic.getLongestPath().length();
        new Pagination<String>(
                (currentPage, maxPages) -> {
                    final List<Component> header = new ArrayList<>();
                    header.add(this.paginatedHeader(sender, currentPage, maxPages));
                    header.add(this.showingResults(sender, query));
                    header.add(this.lastBranch()
                            .append(this.highlight(Component.text(" /" + helpTopic.getLongestPath(), this.colors.highlight))));
                    return header;
                },
                (suggestion, isLastOfPage) -> {
                    final boolean lastBranch = isLastOfPage
                            || helpTopic.getChildSuggestions().indexOf(suggestion) == helpTopic.getChildSuggestions().size() - 1;

                    return ComponentHelper.repeat(Component.space(), headerIndentation)
                            .append(lastBranch ? this.lastBranch() : this.branch())
                            .append(this.highlight(Component.text(" /" + suggestion, this.colors.highlight))
                                    .hoverEvent(Component.text(
                                            this.messageProvider.apply(sender, MESSAGE_CLICK_TO_SHOW_HELP),
                                            this.colors.text
                                    ))
                                    .clickEvent(ClickEvent.runCommand(this.commandPrefix + " " + suggestion)));
                },
                (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
                (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
        ).render(helpTopic.getChildSuggestions(), page, this.maxResultsPerPage).forEach(audience::sendMessage);
    }

    private void printVerboseHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final CommandHelpHandler.@NonNull VerboseHelpTopic<C> helpTopic
    ) {
        final Audience audience = this.getAudience(sender);
        audience.sendMessage(this.basicHeader(sender));
        audience.sendMessage(this.showingResults(sender, query));
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
        audience.sendMessage(this.footer(sender));
    }

    private @NonNull Component showingResults(
            final @NonNull C sender,
            final @NonNull String query
    ) {
        return Component.text(this.messageProvider.apply(sender, MESSAGE_SHOWING_RESULTS_FOR_QUERY) + ": \"", this.colors.text)
                .append(this.highlight(Component.text("/" + query, this.colors.highlight)))
                .append(Component.text("\"", this.colors.text));
    }

    private @NonNull Component button(
            final char icon,
            final @NonNull String command,
            final @NonNull String hoverText
    ) {
        return Component.text()
                .append(Component.space())
                .append(Component.text('[', this.colors.accent))
                .append(Component.text(icon, this.colors.alternateHighlight))
                .append(Component.text(']', this.colors.accent))
                .append(Component.space())
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(Component.text(hoverText, this.colors.text))
                .build();
    }

    private @NonNull Component footer(final @NonNull C sender) {
        return this.paginatedFooter(sender, 1, 1, "");
    }

    private @NonNull Component paginatedFooter(
            final @NonNull C sender,
            final int currentPage,
            final int maxPages,
            final @NonNull String query
    ) {
        final boolean firstPage = currentPage == 1;
        final boolean lastPage = currentPage == maxPages;

        if (firstPage && lastPage) {
            return this.line(this.headerFooterLength);
        }

        final String nextPageCommand = String.format("%s %s %s", this.commandPrefix, query, currentPage + 1);
        final Component nextPageButton = this.button('→', nextPageCommand,
                this.messageProvider.apply(sender, MESSAGE_CLICK_FOR_NEXT_PAGE)
        );
        if (firstPage) {
            return this.header(sender, nextPageButton);
        }

        final String previousPageCommand = String.format("%s %s %s", this.commandPrefix, query, currentPage - 1);
        final Component previousPageButton = this.button('←', previousPageCommand,
                this.messageProvider.apply(sender, MESSAGE_CLICK_FOR_PREVIOUS_PAGE)
        );
        if (lastPage) {
            return this.header(sender, previousPageButton);
        }

        final Component buttons = Component.text()
                .append(previousPageButton)
                .append(this.line(3))
                .append(nextPageButton).build();
        return this.header(sender, buttons);
    }

    private @NonNull Component header(
            final @NonNull C sender,
            final @NonNull Component title
    ) {
        final int sideLength = (this.headerFooterLength - ComponentHelper.length(title)) / 2;
        return Component.text()
                .append(this.line(sideLength))
                .append(title)
                .append(this.line(sideLength))
                .build();
    }

    private @NonNull Component basicHeader(final @NonNull C sender) {
        return this.header(sender, Component.text(
                " " + this.messageProvider.apply(sender, MESSAGE_HELP_TITLE) + " ",
                this.colors.highlight
        ));
    }

    private @NonNull Component paginatedHeader(
            final @NonNull C sender,
            final int currentPage,
            final int pages
    ) {
        return this.header(sender, Component.text()
                .append(Component.text(
                        " " + this.messageProvider.apply(sender, MESSAGE_HELP_TITLE) + " ",
                        this.colors.highlight
                ))
                .append(Component.text("(", this.colors.alternateHighlight))
                .append(Component.text(currentPage, this.colors.text))
                .append(Component.text("/", this.colors.alternateHighlight))
                .append(Component.text(pages, this.colors.text))
                .append(Component.text(")", this.colors.alternateHighlight))
                .append(Component.space())
                .build()
        );
    }

    private @NonNull Component line(final int length) {
        return ComponentHelper.repeat(
                Component.text("-", this.colors.primary, TextDecoration.STRIKETHROUGH),
                length
        );
    }

    private @NonNull Component branch() {
        return Component.text("├─", this.colors.accent);
    }

    private @NonNull Component lastBranch() {
        return Component.text("└─", this.colors.accent);
    }

    private @NonNull Component highlight(final @NonNull Component component) {
        return ComponentHelper.highlight(component, this.colors.alternateHighlight);
    }

    private @NonNull Component pageOutOfRange(
            final @NonNull C sender,
            final int attemptedPage,
            final int maxPages
    ) {
        return this.highlight(
                Component.text(
                        this.messageProvider.apply(sender, MESSAGE_PAGE_OUT_OF_RANGE)
                                .replace("<page>", String.valueOf(attemptedPage))
                                .replace("<max_pages>", String.valueOf(maxPages)),
                        this.colors.text
                )
        );
    }

    /**
     * Class for holding the {@link TextColor TextColors} used for help menus
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
