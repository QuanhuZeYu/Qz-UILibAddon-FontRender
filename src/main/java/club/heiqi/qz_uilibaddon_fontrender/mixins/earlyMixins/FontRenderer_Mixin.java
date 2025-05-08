package club.heiqi.qz_uilibaddon_fontrender.mixins.earlyMixins;

import club.heiqi.qz_uilibaddon_fontrender.ClientProxy;
import club.heiqi.qz_uilibaddon_fontrender.fontSystem.FontEngine;
import club.heiqi.qz_uilibaddon_fontrender.fontSystem.RenderSection;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;


@Mixin(value = FontRenderer.class,priority = 9999)
public abstract class FontRenderer_Mixin {

    @Inject(
            method = "renderStringAtPos",
            at = @At("HEAD"),
            cancellable = true
    )
    public void qz_uilibAddon_fontRender$renderStringAtPos(String text,boolean shadow,CallbackInfo ci) {
        if (!ClientProxy.isInit) return;
        FontEngine.MarkString(text);
        // 1.特殊格式拆解
        // 特殊节记录
        /*
        * 节点可能性:
        * 1.无 - 无
        * 2.无 - §
        * 3.§ - §
        * 4.§ - 无
        * */
        RenderSection forSection = new RenderSection();
        String mark = "0123456789abcdefklmnor";
        // 掩码记录 1:随机 2:粗体 3:删除线 4:下划线 5:斜体
        float red = ((FontRenderer)((Object)this)).red;
        float blue = ((FontRenderer)((Object)this)).green;
        float green = ((FontRenderer)((Object)this)).blue;
        int redI = (int) (red*255)&255;
        int greenI = (int) (green*255)&255;
        int blueI = (int) (blue*255)&255;
        // 默认color为上级调用者设置
        int color = (redI << 16) | (greenI << 8) | blueI;
        int cacheColor = color;
        int mask = 0;
        List<RenderSection> sections = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);
            String c = new String(Character.toChars(codepoint));
            int nextCodepoint = 0;
            String c1 = " ";
            if (i+1 < text.length()) {
                nextCodepoint = text.codePointAt(i+1);
                c1 = new String(Character.toChars(nextCodepoint));
            }
            if (c.equals("§")) {
                i++;
                switch (c1) {
                    case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" -> {
                        int index = mark.indexOf(c1);
                        if (shadow) index += 16;
                        // 更新缓存颜色
                        color = ((FontRenderer)((Object)this)).colorCode[index];
                        // 作为结尾时
                        if (forSection.hasColor) {
                            forSection.setRGB(color);
                            forSection.restAlpha = ((FontRenderer)((Object)this)).alpha;
                            sections.add(forSection.copy());
                            forSection = new RenderSection();
                        }
                        // 作为起始时
                        else {
                            forSection.setRGB(color);
                            forSection.alpha = ((FontRenderer)((Object)this)).alpha;
                        }
                    }
                    case "k" -> {// 16
                        mask |= 0b10000; // 随机化
                        forSection.addMask(mask);
                    }
                    case "l" -> {
                        mask |= 0b1000; // 粗体
                        forSection.addMask(mask);
                    }
                    case "m" -> {
                        mask |= 0b100; // 删除线
                        forSection.addMask(mask);
                    }
                    case "n" -> {
                        mask |= 0b10; // 下划线
                        forSection.addMask(mask);
                    }
                    case "o" -> {
                        mask |= 0b1; // 斜体
                        forSection.addMask(mask);
                    }
                    case "r" -> {
                        int restColor = shadow ? 0x3f3f3f : 0xffffff;
                        color = restColor;
                        forSection.setRGB(color);
                        forSection.alpha = ((FontRenderer)((Object)this)).alpha;
                        sections.add(forSection.copy());
                        forSection = new RenderSection();
                        mask = 0;
                    }
                    default -> {
                        // 分割结尾
                        if (forSection.hasColor) {
                            forSection.setRGB(color);
                            forSection.restAlpha = ((FontRenderer)((Object)this)).alpha;
                            sections.add(forSection.copy());
                            forSection = new RenderSection();
                            mask = 0;
                        }
                        // 分割起始
                        else {
                            forSection.setRGB(color);
                            forSection.alpha = ((FontRenderer)((Object)this)).alpha;
                        }
                    }
                }
            }
            else {
                // 起始为空 颜色取color动态缓存的
                if (forSection.text.isEmpty()) {
                    forSection.setRGB(color);
                    forSection.alpha = ((FontRenderer)((Object)this)).alpha;
                }
                forSection.appendString(c);
                // 结束无音节符 颜色保持color缓存
                if (i==text.length()-1) {
                    if (!forSection.hasColor) {
                        forSection.setRGB(color);
                        forSection.alpha = ((FontRenderer)((Object)this)).alpha;
                    }
                    sections.add(forSection.copy());
                    forSection = new RenderSection();
                }
            }
        }
        if (shadow) {
            ((FontRenderer) ((Object) this)).posX -= 0.5f;
            ((FontRenderer) ((Object) this)).posY -= 0.5f;
        }
        for (RenderSection section : sections) {
            section.setGLColor();
            ((FontRenderer) ((Object) this)).textColor = section.rgb;
            for (int i = 0; i < section.text.length(); i++) {
                int codepoint = section.text.codePointAt(i);
                String c = new String(Character.toChars(codepoint));
                float width = FontEngine.renderCharAt(c,((FontRenderer)((Object)this)).posX,((FontRenderer)((Object)this)).posY,section.mask);
                ((FontRenderer)((Object)this)).posX += width;
            }
            section.restGLColor();
        }
        ci.cancel();
    }
}
