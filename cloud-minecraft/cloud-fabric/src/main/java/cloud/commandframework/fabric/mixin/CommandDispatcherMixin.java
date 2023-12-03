package cloud.commandframework.fabric.mixin;

import cloud.commandframework.fabric.internal.CloudStringReader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandDispatcherMixin {

    @Redirect(
        method = {"parse(Ljava/lang/String;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;",
                  "execute(Ljava/lang/String;Ljava/lang/Object;)I"},
        at = @At(value = "NEW", target = "(Ljava/lang/String;)Lcom/mojang/brigadier/StringReader;"),
        require = 0,
        expect = 2
    )
    private StringReader cloud$newStringReader(final String contents) {
        return new CloudStringReader(contents);
    }

}
