package cloud.commandframework.fabric.mixin;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.MessageArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MessageArgumentType.MessageSelector.class)
public interface MessageArgumentTypeMessageSelectorAccess {
    @Accessor("selector") EntitySelector accessor$selector();
}
