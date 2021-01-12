package cloud.commandframework.fabric.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.EntitySelector;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public class MultipleGameProfileSelector implements Selector<GameProfile> {

    @Override
    public String getInput() {
        return null;
    }

    @Override
    public @Nullable EntitySelector getSelector() {
        return null;
    }

    @Override
    public Collection<GameProfile> get() {
        return null;
    }

}
