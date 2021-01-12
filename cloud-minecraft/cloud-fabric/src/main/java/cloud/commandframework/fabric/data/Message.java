package cloud.commandframework.fabric.data;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * A parsed message.
 */
public interface Message {

    Collection<Entity> getMentionedEntities();

    Text getContents();

}
