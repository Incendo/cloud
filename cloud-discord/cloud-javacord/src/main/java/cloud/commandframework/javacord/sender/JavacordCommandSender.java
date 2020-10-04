package cloud.commandframework.javacord;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.concurrent.CompletableFuture;

public class JavacordCommandSender {

    private final MessageCreateEvent event;

    JavacordCommandSender(MessageCreateEvent event) {
        this.event = event;
    }

    /**
     * Gets the author of the {@link Message message} which triggered the event
     * @return The author of the message
     */
    public MessageAuthor getAuthor() {
        return event.getMessageAuthor();
    }

    /**
     * Gets the message of the event.
     *
     * @return The message of the event.
     */
    public Message getMessage() {
       return event.getMessage();
    }

    /**
     * Gets the textchannel the {@link Message message} was sent in
     * @return The textchannel of the event
     */
    public TextChannel getTextChannel() {
        return event.getChannel();
    }

    public CompletableFuture<Message> sendMessage(String message) {
        return event.getChannel().sendMessage(message);
    }

    public CompletableFuture<Message> sendErrorMessage(String message) {
        return sendMessage(":x:" + message);
    }

    public CompletableFuture<Message> sendSuccessMessage(String message) {
        return sendMessage(":white_check_mark: " + message);
    }

}
