//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.Description;
import cloud.commandframework.help.CommandPredicate;
import cloud.commandframework.help.HelpHandler;
import cloud.commandframework.help.HelpQuery;
import cloud.commandframework.help.HelpRenderer;
import cloud.commandframework.help.result.CommandEntry;
import cloud.commandframework.help.result.HelpQueryResult;
import cloud.commandframework.help.result.IndexCommandResult;
import cloud.commandframework.help.result.MultipleCommandResult;
import cloud.commandframework.help.result.VerboseCommandResult;
import cloud.commandframework.internal.ImmutableImpl;
import cloud.commandframework.internal.StagedImmutableBuilder;
import cloud.commandframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apiguardian.api.API;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

/**
 * Opinionated extension of {@link HelpRenderer} for Minecraft.
 * <p>
 * This class should <b>not</b> be extended, and the only implementation of it is {@link ImmutableMinecraftHelp}.
 * <p>
 * You may customize certain aspects of the help menu by using the {@link #builder() builder}.
 *
 * @param <C> command sender type
 */
@StagedImmutableBuilder
@Value.Immutable
@SuppressWarnings("unused")
public abstract class MinecraftHelp<C> {

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

    private static final Pattern STRING_PLACEHOLDER_PATTERN = Pattern.compile("<([a-z_]+)>");

    private static final Map<String, String> DEFAULT_MESSAGES = new HashMap<>();

    static {
        DEFAULT_MESSAGES.put(MESSAGE_HELP_TITLE, "Help");
        DEFAULT_MESSAGES.put(MESSAGE_COMMAND, "Command");
        DEFAULT_MESSAGES.put(MESSAGE_DESCRIPTION, "Description");
        DEFAULT_MESSAGES.put(MESSAGE_NO_DESCRIPTION, "No description");
        DEFAULT_MESSAGES.put(MESSAGE_ARGUMENTS, "Arguments");
        DEFAULT_MESSAGES.put(MESSAGE_OPTIONAL, "Optional");
        DEFAULT_MESSAGES.put(MESSAGE_SHOWING_RESULTS_FOR_QUERY, "Showing search results for query");
        DEFAULT_MESSAGES.put(MESSAGE_NO_RESULTS_FOR_QUERY, "No results for query");
        DEFAULT_MESSAGES.put(MESSAGE_AVAILABLE_COMMANDS, "Available Commands");
        DEFAULT_MESSAGES.put(MESSAGE_CLICK_TO_SHOW_HELP, "Click to show help for this command");
        DEFAULT_MESSAGES.put(MESSAGE_PAGE_OUT_OF_RANGE, "Error: Page <page> is not in range. Must be in range [1, <max_pages>]");
        DEFAULT_MESSAGES.put(MESSAGE_CLICK_FOR_NEXT_PAGE, "Click for next page");
        DEFAULT_MESSAGES.put(MESSAGE_CLICK_FOR_PREVIOUS_PAGE, "Click for previous page");
    }

    /**
     * Constructs a new Minecraft help instance for a sender type which is an {@link Audience}.
     *
     * @param commandPrefix  command that was used to trigger the help menu. Used to help insertion generation
     * @param commandManager command manager
     * @param <C>            sender type extending {@link Audience}
     * @return new MinecraftHelp instance
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static <C extends Audience> @NonNull MinecraftHelp<C> createNative(
            final @NonNull String commandPrefix,
            final @NonNull CommandManager<C> commandManager
    ) {
        return ImmutableMinecraftHelp.<C>builder()
                .commandManager(commandManager)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix(commandPrefix)
                .build();
    }

    /**
     * Constructs a new Minecraft help instance using the default values.
     *
     * @param commandPrefix    command that was used to trigger the help menu. Used to help insertion generation
     * @param commandManager   command manager
     * @param audienceProvider mapper from {@link C} to {@link Audience}
     * @param <C>             sender type extending {@link Audience}
     * @return new MinecraftHelp instance
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull MinecraftHelp<C> create(
            final @NonNull String commandPrefix,
            final @NonNull CommandManager<C> commandManager,
            final @NonNull AudienceProvider<C> audienceProvider
    ) {
        return ImmutableMinecraftHelp.<C>builder()
                .commandManager(commandManager)
                .audienceProvider(audienceProvider)
                .commandPrefix(commandPrefix)
                .build();
    }

    /**
     * Returns a new {@link MinecraftHelp} builder.
     *
     * @param <C> the command sender type
     * @return the builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> ImmutableMinecraftHelp.@NonNull CommandManagerBuildStage<C> builder() {
        return ImmutableMinecraftHelp.builder();
    }

    MinecraftHelp() {
    }

    /**
     * Returns the command manager instance.
     *
     * @return command manager
     */
    public abstract @NonNull CommandManager<C> commandManager();

    /**
     * Returns the audience provider that was used to create this instance.
     *
     * @return audience provider
     */
    public abstract @NonNull AudienceProvider<C> audienceProvider();

    /**
     * Returns the command prefix.
     *
     * @return command prefix
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public abstract @NonNull String commandPrefix();

    /**
     * Returns the help handler.
     *
     * @return the help handler
     * @since 2.0.0
     */
    @Value.Derived
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull HelpHandler<C> helpHandler() {
        return this.commandManager().createHelpHandler(this.commandFilter());
    }

    /**
     * Maps a command sender to an {@link Audience}
     *
     * @param sender Sender to map
     * @return Mapped audience
     */
    public @NonNull Audience audience(final @NonNull C sender) {
        return this.audienceProvider().apply(sender);
    }

    /**
     * Returns the filter that determines what commands are visible inside the help menu.
     * <p>
     * The default filter is {@link cloud.commandframework.help.CommandPredicate#acceptAll()}.
     *
     * @return the filter
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandPredicate<C> commandFilter() {
        return CommandPredicate.acceptAll();
    }

    /**
     * Returns the description decorator which turns descriptions into components.
     * <p>
     * The default decorator is {@link DescriptionDecorator#text()}.
     *
     * @return the decorator
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull DescriptionDecorator<C> descriptionDecorator() {
        return DescriptionDecorator.text();
    }

    /**
     * Returns the messages which are used by the default {@link MessageProvider}.
     * <p>
     * Placeholders in the format {@literal <name>} will be replaced.
     *
     * @return the messages
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public abstract @NonNull Map<@NonNull String, @NonNull String> messages();

    /**
     * Returns the message provider which is used to retrieve messages from message keys.
     * <p>
     * The keys are constants in {@link MinecraftHelp}.
     *
     * @return the message provider
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull MessageProvider<C> messageProvider() {
        return (sender, key, args) -> {
            final String message = this.messageOrDefault(key);
            if (args.isEmpty()) {
                return text(message);
            }
            return text(StringUtils.replaceAll(
                    message, STRING_PLACEHOLDER_PATTERN, matchResult -> args.get(matchResult.group(1))));
        };
    }

    /**
     * Returns the colors used for help messages.
     * <p>
     * Defaults to {@link #DEFAULT_HELP_COLORS}.
     *
     * @return the active {@link HelpColors}
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull HelpColors colors() {
        return DEFAULT_HELP_COLORS;
    }

    /**
     * Returns the length of the header/footer of help menus.
     * <p>
     * Defaults to {@link #DEFAULT_HEADER_FOOTER_LENGTH}.
     *
     * @return the length
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNegative int headerFooterLength() {
        return DEFAULT_HEADER_FOOTER_LENGTH;
    }

    /**
     * Returns the maximum number of help results to display on a page.
     * <p>
     * Defaults to {@link MinecraftHelp#DEFAULT_MAX_RESULTS_PER_PAGE}.
     *
     * @return the maximum number of results
     * @since 2.0.0
     */
    @Value.Default
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNegative int maxResultsPerPage() {
        return DEFAULT_MAX_RESULTS_PER_PAGE;
    }

    protected final @NonNull String messageOrDefault(final @NonNull String key) {
        return this.messages().getOrDefault(key, DEFAULT_MESSAGES.get(key));
    }

    /**
     * Queries commands and send the results to the recipient. Will respect permissions.
     *
     * @param rawQuery  command query (without leading '/', including optional page number)
     * @param recipient recipient
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
        final Audience audience = this.audience(recipient);
        this.printTopic(
                recipient,
                query,
                page,
                this.helpHandler().query(HelpQuery.of(recipient, query))
        );
    }

    private void printTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final @NonNull HelpQueryResult<C> helpTopic
    ) {
        if (helpTopic instanceof IndexCommandResult) {
            this.printIndexHelpTopic(sender, query, page, (IndexCommandResult<C>) helpTopic);
        } else if (helpTopic instanceof MultipleCommandResult) {
            this.printMultiHelpTopic(sender, query, page, (MultipleCommandResult<C>) helpTopic);
        } else if (helpTopic instanceof VerboseCommandResult) {
            this.printVerboseHelpTopic(sender, query, (VerboseCommandResult<C>) helpTopic);
        } else {
            throw new IllegalArgumentException("Unknown help topic type");
        }
    }

    private void printNoResults(
            final @NonNull C sender,
            final @NonNull String query
    ) {
        final Audience audience = this.audience(sender);
        audience.sendMessage(this.basicHeader(sender));
        audience.sendMessage(LinearComponents.linear(
                this.messageProvider().provide(sender, MESSAGE_NO_RESULTS_FOR_QUERY).color(this.colors().text()),
                text(": \"", this.colors().text()),
                this.highlight(text("/" + query, this.colors().highlight())),
                text("\"", this.colors().text())
        ));
        audience.sendMessage(this.footer(sender));
    }

    private void printIndexHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final @NonNull IndexCommandResult<C> helpTopic
    ) {
        if (helpTopic.isEmpty()) {
            this.printNoResults(sender, query);
            return;
        }

        final Audience audience = this.audience(sender);
        new Pagination<CommandEntry<C>>(
                (currentPage, maxPages) -> {
                    final List<Component> header = new ArrayList<>();
                    header.add(this.paginatedHeader(sender, currentPage, maxPages));
                    header.add(this.showingResults(sender, query));
                    header.add(text()
                            .append(this.lastBranch())
                            .append(space())
                            .append(
                                    this.messageProvider().provide(
                                            sender,
                                            MESSAGE_AVAILABLE_COMMANDS
                                    ).color(this.colors().text())
                            )
                            .append(text(":", this.colors().text()))
                            .build()
                    );
                    return header;
                },
                (helpEntry, isLastOfPage) -> {
                    final CommandDescription commandDescription = helpEntry.command().commandDescription();
                    final Component description;
                    if (commandDescription.description() instanceof RichDescription) {
                        description = ((RichDescription) commandDescription.description()).contents();
                    } else if (commandDescription.isEmpty()) {
                        description = this.messageProvider().provide(sender, MESSAGE_CLICK_TO_SHOW_HELP);
                    } else {
                        description = this.descriptionDecorator().decorate(
                                sender,
                                commandDescription.description().textDescription()
                        );
                    }

                    final boolean lastBranch =
                            isLastOfPage || helpTopic.entries().indexOf(helpEntry) == helpTopic.entries().size() - 1;

                    return text()
                            .append(text("   "))
                            .append(lastBranch ? this.lastBranch() : this.branch())
                            .append(this.highlight(text(
                                                    String.format(" /%s", helpEntry.syntax()),
                                                    this.colors().highlight()
                                            ))
                                            .hoverEvent(description.color(this.colors().text()))
                                            .clickEvent(runCommand(this.commandPrefix() + " " + helpEntry.syntax()))
                            )
                            .build();
                },
                (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
                (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
        ).render(helpTopic.entries(), page, this.maxResultsPerPage()).forEach(audience::sendMessage);
    }

    private void printMultiHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final int page,
            final @NonNull MultipleCommandResult<C> helpTopic
    ) {
        if (helpTopic.childSuggestions().isEmpty()) {
            this.printNoResults(sender, query);
            return;
        }

        final Audience audience = this.audience(sender);
        final int headerIndentation = helpTopic.longestPath().length();
        new Pagination<String>(
                (currentPage, maxPages) -> {
                    final List<Component> header = new ArrayList<>();
                    header.add(this.paginatedHeader(sender, currentPage, maxPages));
                    header.add(this.showingResults(sender, query));
                    header.add(this.lastBranch()
                            .append(this.highlight(text(" /" + helpTopic.longestPath(), this.colors().highlight()))));
                    return header;
                },
                (suggestion, isLastOfPage) -> {
                    final boolean lastBranch = isLastOfPage
                            || helpTopic.childSuggestions().indexOf(suggestion) == helpTopic.childSuggestions().size() - 1;

                    return ComponentHelper.repeat(space(), headerIndentation)
                            .append(lastBranch ? this.lastBranch() : this.branch())
                            .append(this.highlight(text(" /" + suggestion, this.colors().highlight()))
                                    .hoverEvent(this.messageProvider().provide(sender, MESSAGE_CLICK_TO_SHOW_HELP)
                                            .color(this.colors().text()))
                                    .clickEvent(runCommand(this.commandPrefix() + " " + suggestion)));
                },
                (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
                (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
        ).render(helpTopic.childSuggestions(), page, this.maxResultsPerPage()).forEach(audience::sendMessage);
    }

    private void printVerboseHelpTopic(
            final @NonNull C sender,
            final @NonNull String query,
            final @NonNull VerboseCommandResult<C> helpTopic
    ) {
        final Audience audience = this.audience(sender);
        audience.sendMessage(this.basicHeader(sender));
        audience.sendMessage(this.showingResults(sender, query));
        final String command = this.commandManager().commandSyntaxFormatter()
                .apply(helpTopic.entry().command().components(), null);
        audience.sendMessage(text()
                .append(this.lastBranch())
                .append(space())
                .append(this.messageProvider().provide(sender, MESSAGE_COMMAND).color(this.colors().primary()))
                .append(text(": ", this.colors().primary()))
                .append(this.highlight(text("/" + command, this.colors().highlight())))
        );
        /* Topics will use the long description if available, but fall back to the short description. */
        final Description commandDescription = helpTopic.entry().command().commandDescription().verboseDescription();

        final Component topicDescription;
        if (commandDescription instanceof RichDescription) {
            topicDescription = ((RichDescription) commandDescription).contents();
        } else if (commandDescription.isEmpty()) {
            topicDescription = this.messageProvider().provide(sender, MESSAGE_NO_DESCRIPTION);
        } else {
            topicDescription = this.descriptionDecorator().decorate(
                    sender,
                    commandDescription.textDescription()
            );
        }

        final boolean hasArguments = helpTopic.entry().command().components().size() > 1;
        audience.sendMessage(text()
                .append(text("   "))
                .append(hasArguments ? this.branch() : this.lastBranch())
                .append(space())
                .append(this.messageProvider().provide(sender, MESSAGE_DESCRIPTION).color(this.colors().primary()))
                .append(text(": ", this.colors().primary()))
                .append(topicDescription.color(this.colors().text()))
        );
        if (hasArguments) {
            audience.sendMessage(text()
                    .append(text("   "))
                    .append(this.lastBranch())
                    .append(space())
                    .append(this.messageProvider().provide(sender, MESSAGE_ARGUMENTS).color(this.colors().primary()))
                    .append(text(":", this.colors().primary()))
            );

            final Iterator<CommandComponent<C>> iterator = helpTopic.entry().command().components().iterator();
            /* Skip the first one because it's the command literal */
            iterator.next();

            while (iterator.hasNext()) {
                final CommandComponent<C> component = iterator.next();

                final String syntax = this.commandManager().commandSyntaxFormatter()
                        .apply(Collections.singletonList(component), null);

                final TextComponent.Builder textComponent = text()
                        .append(text("       "))
                        .append(iterator.hasNext() ? this.branch() : this.lastBranch())
                        .append(this.highlight(text(" " + syntax, this.colors().highlight())));
                if (component.optional()) {
                    textComponent.append(text(" (", this.colors().alternateHighlight()));
                    textComponent.append(
                            this.messageProvider().provide(sender, MESSAGE_OPTIONAL).color(this.colors().alternateHighlight())
                    );
                    textComponent.append(text(")", this.colors().alternateHighlight()));
                }
                final Description description = component.description();
                if (!description.isEmpty()) {
                    textComponent.append(text(" - ", this.colors().accent()));
                    textComponent.append(this.formatDescription(sender, description).colorIfAbsent(this.colors().text()));
                }

                audience.sendMessage(textComponent);
            }
        }
        audience.sendMessage(this.footer(sender));
    }

    private Component formatDescription(final C sender, final Description description) {
        if (description instanceof RichDescription) {
            return ((RichDescription) description).contents();
        } else {
            return this.descriptionDecorator().decorate(sender, description.textDescription());
        }
    }

    private @NonNull Component showingResults(
            final @NonNull C sender,
            final @NonNull String query
    ) {
        return text()
                .append(this.messageProvider().provide(sender, MESSAGE_SHOWING_RESULTS_FOR_QUERY).color(this.colors().text()))
                .append(text(": \"", this.colors().text()))
                .append(this.highlight(text("/" + query, this.colors().highlight())))
                .append(text("\"", this.colors().text()))
                .build();
    }

    private @NonNull Component button(
            final char icon,
            final @NonNull String command,
            final @NonNull Component hoverText
    ) {
        return text()
                .append(space())
                .append(text('[', this.colors().accent()))
                .append(text(icon, this.colors().alternateHighlight()))
                .append(text(']', this.colors().accent()))
                .append(space())
                .clickEvent(runCommand(command))
                .hoverEvent(hoverText)
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
            return this.line(this.headerFooterLength());
        }

        final Component nextPageButton = this.button('→', this.pageCommand(query, currentPage + 1),
                this.messageProvider().provide(sender, MESSAGE_CLICK_FOR_NEXT_PAGE).color(this.colors().text())
        );
        if (firstPage) {
            return this.header(sender, nextPageButton);
        }

        final Component previousPageButton = this.button('←', this.pageCommand(query, currentPage - 1),
                this.messageProvider().provide(sender, MESSAGE_CLICK_FOR_PREVIOUS_PAGE).color(this.colors().text())
        );
        if (lastPage) {
            return this.header(sender, previousPageButton);
        }

        final Component buttons = text()
                .append(previousPageButton)
                .append(this.line(3))
                .append(nextPageButton).build();
        return this.header(sender, buttons);
    }

    private String pageCommand(final String query, final int page) {
        if (query.isEmpty()) {
            return String.format("%s %s", this.commandPrefix(), page);
        }
        return String.format("%s %s %s", this.commandPrefix(), query, page);
    }

    private @NonNull Component header(
            final @NonNull C sender,
            final @NonNull Component title
    ) {
        final int sideLength = (this.headerFooterLength() - ComponentHelper.length(title)) / 2;
        return text()
                .append(this.line(sideLength))
                .append(title)
                .append(this.line(sideLength))
                .build();
    }

    private @NonNull Component basicHeader(final @NonNull C sender) {
        return this.header(sender, LinearComponents.linear(
                space(),
                this.messageProvider().provide(sender, MESSAGE_HELP_TITLE).color(this.colors().highlight()),
                space()
        ));
    }

    private @NonNull Component paginatedHeader(
            final @NonNull C sender,
            final int currentPage,
            final int pages
    ) {
        return this.header(sender, text()
                .append(space())
                .append(this.messageProvider().provide(sender, MESSAGE_HELP_TITLE).color(this.colors().highlight()))
                .append(space())
                .append(text("(", this.colors().alternateHighlight()))
                .append(text(currentPage, this.colors().text()))
                .append(text("/", this.colors().alternateHighlight()))
                .append(text(pages, this.colors().text()))
                .append(text(")", this.colors().alternateHighlight()))
                .append(space())
                .build()
        );
    }

    private @NonNull Component line(final int length) {
        return ComponentHelper.repeat(
                text("-", this.colors().primary(), TextDecoration.STRIKETHROUGH),
                length
        );
    }

    private @NonNull Component branch() {
        return text("├─", this.colors().accent());
    }

    private @NonNull Component lastBranch() {
        return text("└─", this.colors().accent());
    }

    private @NonNull Component highlight(final @NonNull Component component) {
        return ComponentHelper.highlight(component, this.colors().alternateHighlight());
    }

    private @NonNull Component pageOutOfRange(
            final @NonNull C sender,
            final int attemptedPage,
            final int maxPages
    ) {
        final Map<String, String> args = new HashMap<>();
        args.put("page", String.valueOf(attemptedPage));
        args.put("max_pages", String.valueOf(maxPages));
        return this.highlight(
                this.messageProvider().provide(sender, MESSAGE_PAGE_OUT_OF_RANGE, args).color(this.colors().text())
        );
    }


    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "2.0.0")
    public interface MessageProvider<C> {

        /**
         * Creates a component from a command sender, key, and arguments
         *
         * @param sender command sender
         * @param key    message key (constants in {@link MinecraftHelp}
         * @param args   args
         * @return component
         */
        @NonNull Component provide(
                @NonNull C sender,
                @NonNull String key,
                @NonNull Map<String, String> args
        );

        /**
         * Creates a component from a command sender and key.
         *
         * @param sender command sender
         * @param key    message key (constants in {@link MinecraftHelp}
         * @return component
         */
        default @NonNull Component provide(final @NonNull C sender, final @NonNull String key) {
            return this.provide(sender, key, Collections.emptyMap());
        }
    }

    @API(status = API.Status.STABLE, since = "2.0.0")
    @FunctionalInterface
    public interface DescriptionDecorator<C> {

        /**
         * Return a description decorator that wraps the given {@code function}.
         *
         * @param <C>      the command sender type
         * @param function the function
         * @return the decorator
         */
        static <C> @NonNull DescriptionDecorator<C> wrap(final @NonNull Function<String, Component> function) {
            return (sender, description) -> function.apply(description);
        }

        /**
         * Returns a description decorator that turns the description into a text component without any style.
         *
         * @param <C> the command sender type
         * @return the decorator
         */
        static <C> @NonNull DescriptionDecorator<C> text() {
            return (sender, description) -> Component.text(description);
        }

        /**
         * Turns the given {@code description} into an adventure {@link Component}.
         *
         * @param sender      command sender
         * @param description description
         * @return component
         */
        @NonNull Component decorate(@NonNull C sender, @NonNull String description);
    }

    /**
     * Class for holding the {@link TextColor TextColors} used for help menus
     */
    @ImmutableImpl
    @Value.Immutable
    public interface HelpColors {

        /**
         * Returns the configured primary color.
         *
         * @return the primary color for the color scheme
         */
        @NonNull TextColor primary();

        /**
         * Returns the configured highlight color.
         *
         * @return the primary color used to highlight commands and queries
         */
        @NonNull TextColor highlight();

        /**
         * Returns the configured alternate highlight color.
         *
         * @return the secondary color used to highlight commands and queries
         */
        @NonNull TextColor alternateHighlight();

        /**
         * Returns the configured text color.
         *
         * @return the color used for description text
         */
        @NonNull TextColor text();

        /**
         * Returns the configured accent color.
         *
         * @return the color used for accents and symbols
         */
        @NonNull TextColor accent();

        /**
         * Creates a new {@link HelpColors} instance.
         *
         * @param primary            the primary color for the color scheme
         * @param highlight          the primary color used to highlight commands and queries
         * @param alternateHighlight the secondary color used to highlight commands and queries
         * @param text               the color used for description text
         * @param accent             the color used for accents and symbols
         * @return a new {@link HelpColors} instance
         */
        static @NonNull HelpColors of(
                final @NonNull TextColor primary,
                final @NonNull TextColor highlight,
                final @NonNull TextColor alternateHighlight,
                final @NonNull TextColor text,
                final @NonNull TextColor accent
        ) {
            return HelpColorsImpl.of(primary, highlight, alternateHighlight, text, accent);
        }
    }
}
