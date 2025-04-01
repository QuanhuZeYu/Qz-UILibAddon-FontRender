package club.heiqi.qz_addon_fontrender.mixins.earlyMixins;

import club.heiqi.qz_addon_fontrender.ClientProxy;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;


@Mixin(FontRenderer.class)
public abstract class FontRender {
    @Shadow
    public float posX;
    @Shadow
    public float posY;
    @Shadow
    public boolean randomStyle;
    @Shadow
    public boolean boldStyle;
    @Shadow
    public boolean strikethroughStyle;
    @Shadow
    public boolean underlineStyle;
    @Shadow
    private boolean italicStyle;
    @Shadow
    public int[] colorCode;
    @Shadow
    public int textColor;
    @Shadow
    public float alpha;
    @Shadow
    public float red;
    @Shadow
    public float blue;
    @Shadow
    public float green;
    @Shadow
    public Random fontRandom;
    @Shadow
    public int[] charWidth;
    @Shadow
    public boolean unicodeFlag;


}
