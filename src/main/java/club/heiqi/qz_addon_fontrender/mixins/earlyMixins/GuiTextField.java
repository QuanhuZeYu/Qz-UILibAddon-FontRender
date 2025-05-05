package club.heiqi.qz_addon_fontrender.mixins.earlyMixins;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.client.gui.GuiTextField.class)
public class GuiTextField {

    @Inject(
        method = "textboxKeyTyped",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    public void qz_uilibAddonFontRender$textboxKeyTyped(char p_146201_1_, int p_146201_2_, CallbackInfoReturnable<Boolean> ci) {

    }
}
