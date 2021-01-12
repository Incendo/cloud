package cloud.commandframework.fabric.mixin;

import net.minecraft.command.argument.MessageArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MessageArgumentType.MessageFormat.class)
public interface MessageArgumentTypeMessageFormatAccess {

    @Accessor("selectors") MessageArgumentType.MessageSelector[] accessor$selectors();

}
