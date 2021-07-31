package cloud.commandframework.jda.permission;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.meta.CommandMeta;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class UserPermissionPostProcessor<T> implements CommandPostprocessor<T> {

    @Override
    public void accept(@NonNull final CommandPostprocessingContext<T> postprocessingContext) {
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
            actualPermissions = EnumSet.of( // dms ?
                    Permission.MESSAGE_ADD_REACTION,
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_READ,
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

