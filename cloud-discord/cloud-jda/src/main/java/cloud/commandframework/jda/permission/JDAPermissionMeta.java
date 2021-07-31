package cloud.commandframework.jda.permission;

import cloud.commandframework.Command;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.CommandMeta;
import io.leangen.geantyref.TypeToken;
import net.dv8tion.jda.api.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public final class JDAPermissionMeta {

    public static final CommandMeta.Key<List<Permission>> BOT_PERMISSIONS = CommandMeta.Key.of(
            new TypeToken<List<Permission>>() {
            },
            "bot-permissions",
            (meta) -> Collections.emptyList()
    );

    public static final CommandMeta.Key<List<Permission>> USER_PERMISSIONS = CommandMeta.Key.of(
            new TypeToken<List<Permission>>() {
            },
            "user-permissions",
            (meta) -> Collections.emptyList()
    );

    private JDAPermissionMeta() {
    }

    public static <C> AnnotationParser<C> register(final AnnotationParser<C> annotationParser) {

        annotationParser.registerBuilderModifier(JDABotPermission.class, new BotPermissionMetaModifier<>());
        annotationParser.registerBuilderModifier(JDAUserPermission.class, new UserPermissionMetaModifier<>());

        return annotationParser;
    }

    public static final class BotPermissionMetaModifier<C> implements
            BiFunction<JDABotPermission, Command.Builder<C>, Command.Builder<C>> {


        @Override
        public Command.Builder<C> apply(final JDABotPermission botPermission, final Command.Builder<C> builder) {
            return builder.meta(BOT_PERMISSIONS, Arrays.asList(botPermission.permissions()));
        }

    }

    public static final class UserPermissionMetaModifier<C> implements
            BiFunction<JDAUserPermission, Command.Builder<C>, Command.Builder<C>> {

        @Override
        public Command.Builder<C> apply(final JDAUserPermission userPermission, final Command.Builder<C> builder) {
            return builder.meta(USER_PERMISSIONS, Arrays.asList(userPermission.permissions()));
        }

    }

}
