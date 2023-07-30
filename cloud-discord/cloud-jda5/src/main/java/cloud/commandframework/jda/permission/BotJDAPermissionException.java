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
package cloud.commandframework.jda.permission;

import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when the bot is lacking a specific permission.
 */
public final class BotJDAPermissionException extends IllegalArgumentException {

    private static final long serialVersionUID = -4791375589892612355L;
    private final @NonNull CommandContext<?> commandContext;
    private final @NonNull List<Permission> missingPermissions;

    /**
     * Construct a new no JDA permission exception
     *
     * @param commandContext     Command context
     * @param missingPermissions The permissions that are missing
     */
    BotJDAPermissionException(
            final @NonNull CommandContext<?> commandContext,
            final @NonNull List<Permission> missingPermissions
    ) {
        this.commandContext = commandContext;
        this.missingPermissions = missingPermissions;
    }


    @Override
    public String getMessage() {
        return String.format(
                "Cannot execute command due to insufficient permission. "
                        + "The bot requires the following permission(s) to execute this command: %s",
                this.missingPermissions.stream().map(Permission::getName).collect(Collectors.joining(", "))
        );
    }

    /**
     * Get the CommandContext, which led to this exception
     *
     * @return Command Context
     * @since 1.9.0
     */
    public @NotNull CommandContext<?> getCommandContext() {
        return this.commandContext;
    }

    /**
     * Get the permissions that the invoking bot is missing.
     *
     * @return The missing permissions
     * @since 1.9.0
     */
    public @NotNull List<Permission> getMissingPermissions() {
        return this.missingPermissions;
    }

}
