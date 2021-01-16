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
package cloud.commandframework.pircbotx;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.pircbotx.arguments.UserArgument;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Command manager implementation for PircBotX 2.0
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public class PircBotXCommandManager<C> extends CommandManager<C> {

    /**
     * Meta key for accessing the {@link org.pircbotx.PircBotX} instance from a
     * {@link cloud.commandframework.context.CommandContext} instance
     */
    public static final String PIRCBOTX_META_KEY = "__internal_pircbotx__";
    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_USER_KEY = Caption.of("argument.parse.failure.use");
    /**
     * Default caption for {@link #ARGUMENT_PARSE_FAILURE_USER_KEY}
     */
    public static final String ARGUMENT_PARSE_FAILURE_USER = "'{input}' is not a valid user";

    private final String commandPrefix;
    private final BiFunction<C, String, Boolean> permissionFunction;
    private final Function<User, C> userMapper;
    private final PircBotX pircBotX;

    /**
     * Create a new command manager instance
     *
     * @param pircBotX                    PircBotX instance. This is used to register the
     *                                    {@link org.pircbotx.hooks.ListenerAdapter} that will forward commands to the
     *                                    command dispatcher
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link AsynchronousCommandExecutionCoordinator}
     * @param commandRegistrationHandler  Command registration handler. This will get called every time a new command is
     *                                    registered to the command manager. This may be used to forward command registration
     * @param permissionFunction          Function used to determine whether or not a sender is permitted to use a certain
     *                                    command. The first input is the sender of the command, and the second parameter is
     *                                    the the command permission string. The return value should be {@code true} if the
     *                                    sender is permitted to use the command, else {@code false}
     * @param userMapper                  Function that maps {@link User users} to the custom command sender type
     * @param commandPrefix               The prefix that must be applied to all commands for the command to be valid
     */
    public PircBotXCommandManager(
            final @NonNull PircBotX pircBotX,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull CommandRegistrationHandler commandRegistrationHandler,
            final @NonNull BiFunction<C, String, Boolean> permissionFunction,
            final @NonNull Function<User, C> userMapper,
            final @NonNull String commandPrefix
    ) {
        super(commandExecutionCoordinator, commandRegistrationHandler);
        this.pircBotX = pircBotX;
        this.permissionFunction = permissionFunction;
        this.commandPrefix = commandPrefix;
        this.userMapper = userMapper;
        this.pircBotX.getConfiguration().getListenerManager().addListener(new CloudListenerAdapter<>(this));
        if (this.getCaptionRegistry() instanceof FactoryDelegatingCaptionRegistry) {
            ((FactoryDelegatingCaptionRegistry<C>) this.getCaptionRegistry()).registerMessageFactory(
                    ARGUMENT_PARSE_FAILURE_USER_KEY,
                    (caption, user) -> ARGUMENT_PARSE_FAILURE_USER
            );
        }
        this.registerCommandPreProcessor(context -> context.getCommandContext().store(PIRCBOTX_META_KEY, pircBotX));
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(User.class),
                parameters -> new UserArgument.UserArgumentParser<>()
        );
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        return this.permissionFunction.apply(sender, permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return CommandMeta.simple().build();
    }

    /**
     * Get the command prefix. A message should be classed as a command if, and only if, it is prefixed
     * with this prefix
     *
     * @return Command prefix
     */
    public final @NonNull String getCommandPrefix() {
        return this.commandPrefix;
    }

    final @NonNull Function<User, C> getUserMapper() {
        return this.userMapper;
    }

}
