package club.heiqi.qz_uilibaddon_fontrender.mixins.earlyMixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.util.ChatAllowedCharacters.class)
public class ChatAllowedCharacters_Mixin {

    @Inject(
        method = "filerAllowedCharacters",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private static void qz_uilibAddonFontRender$filerAllowedCharacters(String input, CallbackInfoReturnable<String> ci) {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            int codepoint = input.codePointAt(i);
            String t = new String(Character.toChars(codepoint));

            if (!t.equals("§")) {
                stringbuilder.append(t);
            }
        }
        String s = stringbuilder.toString();
        ci.setReturnValue(s);
    }
}
