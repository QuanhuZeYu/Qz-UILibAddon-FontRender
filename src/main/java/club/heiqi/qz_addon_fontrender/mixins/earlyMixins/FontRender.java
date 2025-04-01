package club.heiqi.qz_addon_fontrender.mixins.earlyMixins;

import club.heiqi.qz_addon_fontrender.ClientProxy;
import club.heiqi.qz_addon_fontrender.ConstField;
import club.heiqi.qz_addon_fontrender.fontSystem.Page;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
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

    @Inject(
            method = "renderStringAtPos",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    public void qz_uilibAddon_fontRender$renderStringAtPos(String text,boolean shadow,CallbackInfo ci) {
        /*ConstField.LOG.info("渲染字符串:{}",text);*/
        if (!ClientProxy.isInit) return;
        if (shadow) {
            ci.cancel();
            return;
        }
        // 格式化枚举值
        final String COLOR_CODE_CHARS = "0123456789abcdefklmnor";
        final String CHAR_MAPPING =
                "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵž‡\0\0\0\0\0\0\0 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                        "[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\0" +
                        "ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»" +
                        "░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\0";
        // 开始对字符串进行循环
        for (int charIdx = 0; charIdx < text.length(); charIdx++) {
            int codepoint = text.charAt(charIdx);
            String currentText = new String(Character.toChars(codepoint));
            int mappedIndex;
            int adjustedIndex;

            // 处理格式代码（§开头）
            if (currentText.equals("§") && charIdx + 1 < text.length()) {
                int formatCode = text.toLowerCase().charAt(charIdx + 1);
                int formatType = COLOR_CODE_CHARS.indexOf(formatCode); // 获取枚举索引

                // 颜色代码处理（0-15）
                if (formatType < 16) {
                    resetTextStyles(); // 先重置样式

                    if (formatType < 0 || formatType > 15) {
                        formatType = 15;  // 默认白色
                    }

                    int color = shadow ?
                            this.colorCode[formatType + 16] :
                            this.colorCode[formatType];
                    setTextColor(color);
                }
                // 特殊样式处理
                else {
                    switch (formatType) {
                        case 16 -> this.randomStyle = true;    // §k
                        case 17 -> this.boldStyle = true;           // §l
                        case 18 -> this.strikethroughStyle = true;  // §m
                        case 19 -> this.underlineStyle = true;      // §n
                        case 20 -> this.italicStyle = true;         // §o
                        case 21 -> {                             // §r
                            resetTextStyles();
                        }
                    }
                }
                charIdx++; // 跳过格式代码字符
            }
            // 渲染普通字符
            else {
                mappedIndex = CHAR_MAPPING.indexOf(currentText);
                float scaleFactor = this.unicodeFlag ? 0.5f : 1.0f;

                // 处理随机字符样式（§k）
                if (this.randomStyle && mappedIndex != -1) {
                    do {
                        adjustedIndex = this.fontRandom.nextInt(this.charWidth.length);
                    } while (this.charWidth[mappedIndex] != this.charWidth[adjustedIndex]);
                    mappedIndex = adjustedIndex;
                }

                // 处理不可见字符阴影
                boolean isInvisibleChar = (codepoint == 0 || mappedIndex == -1) && shadow;
                if (isInvisibleChar) {
                    adjustPosition(-scaleFactor, -scaleFactor);
                }

                // 实际渲染字符
                /*float charWidth = ((FontRenderer)((Object)this)).renderCharAtPos(mappedIndex, (char) codepoint, this.italicStyle);*/
                int color = ((int)(this.red*255) << 24) & 0xFF000000
                        | ((int)(this.green*255) << 16) & 0x00FF0000
                        | ((int)(this.blue*255) << 8) & 0x0000FF00
                        | ((int)(this.alpha*255)) & 0x000000FF;
                float charWidth = 0;
                if (boldStyle) charWidth = club.heiqi.qz_addon_fontrender.fontSystem.FontRender.renderChar(currentText,posX,posY,color,Page.BOLD);
                else if (italicStyle) charWidth = club.heiqi.qz_addon_fontrender.fontSystem.FontRender.renderChar(currentText,posX,posY,color,Page.ITALIC);
                else charWidth = club.heiqi.qz_addon_fontrender.fontSystem.FontRender.renderChar(currentText,posX,posY,color,Page.NORMAL);
                // 恢复位置偏移
                if (isInvisibleChar) {
                    adjustPosition(scaleFactor, scaleFactor);
                }

                // 处理粗体样式（§l）
                /*if (this.boldStyle) {
                    adjustPosition(scaleFactor, 0);
                    if (isInvisibleChar) adjustPosition(-scaleFactor, -scaleFactor);

                    ((FontRenderer)((Object)this)).renderCharAtPos(mappedIndex, (char) codepoint, this.italicStyle);
                    charWidth += scaleFactor;

                    if (isInvisibleChar) adjustPosition(scaleFactor, scaleFactor);
                    adjustPosition(-scaleFactor, 0);
                }*/

                ((FontRenderer)((Object)this)).doDraw(charWidth);
            }
        }
        ci.cancel();
    }

    // region 原函数中没有的
    // 辅助方法（原类中需补充实现）
    @Unique
    private void resetTextStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.strikethroughStyle = false;
        this.underlineStyle = false;
        this.italicStyle = false;
        ((FontRenderer)((Object)this)).setColor(this.red, this.green, this.blue, this.alpha);
    }

    @Unique
    private void setTextColor(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        ((FontRenderer)((Object)this)).setColor(r, g, b, this.alpha);
    }

    @Unique
    private void adjustPosition(float xOffset, float yOffset) {
        this.posX += xOffset;
        this.posY += yOffset;
    }
    // endregion
}
