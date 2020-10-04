package cloud.commandframework.javacord.sender;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public class JavacordPrivateSender extends JavacordCommandSender {

    JavacordPrivateSender(final MessageCreateEvent event) {
        super(event);
    }

    @Override
    public TextChannel getTextChannel() {
        return event.getPrivateChannel().orElseThrow(() -> new UnsupportedOperationException("PrivateChannel not present even though message was sent in a private channel"));
    }
}
