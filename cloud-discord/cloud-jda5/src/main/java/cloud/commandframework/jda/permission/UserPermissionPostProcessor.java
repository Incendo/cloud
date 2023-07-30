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
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.meta.CommandMeta;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UserPermissionPostProcessor<T> implements CommandPostprocessor<T> {

    @Override
    public void accept(final @NonNull CommandPostprocessingContext<T> postprocessingContext) {
        final CommandContext<T> context = postprocessingContext.getCommandContext();
        final CommandMeta meta = postprocessingContext.getCommand().getCommandMeta();
        final MessageReceivedEvent event = context.get("MessageReceivedEvent");

        final EnumSet<Permission> actualPermissions;

        if (context.contains("Guild")) {
            final Member user = event.getMember();
            assert user != null;

            if (context.contains("TextChannel")) {
                final TextChannel channel = context.get("TextChannel");
                actualPermissions = user.getPermissions(channel);
            } else {
                actualPermissions = user.getPermissions();
            }
        } else {
            actualPermissions = EnumSet.of(// dms ?
                    Permission.MESSAGE_ADD_REACTION,
                    Permission.MESSAGE_SEND,
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_EXT_EMOJI,
                    Permission.MESSAGE_TTS,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ATTACH_FILES,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_MENTION_EVERYONE
            );
        }

        final List<Permission> requiredPermissions = meta.getOrDefault(
                JDAPermissionMeta.USER_PERMISSIONS,
                Collections.emptyList()
        );

        if (!actualPermissions.containsAll(requiredPermissions)) {
            throw new UserJDAPermissionException(
                    context,
                    requiredPermissions.stream().filter(perm -> !actualPermissions.contains(perm)).collect(Collectors.toList())
            );
        }
    }

}
