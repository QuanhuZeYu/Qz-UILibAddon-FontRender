package club.heiqi.qz_addon_fontrender.mixins.earlyMixins;

import club.heiqi.qz_addon_fontrender.ClientProxy;
import club.heiqi.qz_addon_fontrender.fontSystem.FontEngine;
import net.minecraft.client.gui.FontRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glColor4f;


@Mixin(FontRenderer.class)
public abstract class FontRender {
    @Unique
    private static Logger LOG = LogManager.getLogger();

    @Inject(
            method = "renderStringAtPos",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    public void qz_uilibAddon_fontRender$renderStringAtPos(String text,boolean shadow,CallbackInfo ci) {
        if (!ClientProxy.isInit) return;
        FontEngine.MarkString(text);
        // 1.特殊格式拆解
        // 特殊节记录
        String mark = "0123456789abcdefklmnor";
        // 掩码记录 1:随机 2:粗体 3:删除线 4:下划线 5:斜体
        int mask = 0b00000;
        float red = ((FontRenderer)((Object)this)).red;
        float green = ((FontRenderer)((Object)this)).green;
        float blue = ((FontRenderer)((Object)this)).blue;
        int redI = (int) (red*255)&255;
        int greenI = (int) (green*255)&255;
        int blueI = (int) (blue*255)&255;
        int color = (redI << 16) | (greenI << 8) | blueI;
        String sectionString = "";
        List<FontEngine.RenderSection> sections = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);
            String c = new String(Character.toChars(codepoint));
            int nextCodepoint = 0;
            String c1 = " ";
            if (i+1 < text.length()-1) {
                nextCodepoint = text.codePointAt(i+1);
                c1 = new String(Character.toChars(nextCodepoint));
            }
            if (c.equals("§")) {
                i++;
                switch (c1) {
                    case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" -> {
                        int index = mark.indexOf(c1);
                        if (shadow) index += 16;
                        color = ((FontRenderer)((Object)this)).colorCode[index];
                    }
                    case "k" -> {// 16
                        mask |= 0b10000;
                    }
                    case "l" -> {
                        mask |= 0b1000;
                    }
                    case "m" -> {
                        mask |= 0b100;
                    }
                    case "n" -> {
                        mask |= 0b10;
                    }
                    case "o" -> {
                        mask |= 0b1;
                    }
                    case "r", "g", "h" -> {
                        red = ((FontRenderer)((Object)this)).red;
                        green = ((FontRenderer)((Object)this)).green;
                        blue = ((FontRenderer)((Object)this)).blue;
                        redI = (int) (red*255);
                        greenI = (int) (green*255);
                        blueI = (int) (blue*255);
                        color = (redI << 16) | (greenI << 8) | blueI;
                        FontEngine.RenderSection section = new FontEngine.RenderSection(mask,color,((FontRenderer)((Object)this)).alpha,sectionString);
                        sections.add(section);
                        sectionString = ""; // 重置字符串收集
                        mask = 0;
                    }
                    default -> {
                        red = ((FontRenderer)((Object)this)).red;
                        green = ((FontRenderer)((Object)this)).green;
                        blue = ((FontRenderer)((Object)this)).blue;
                        redI = (int) (red*255);
                        greenI = (int) (green*255);
                        blueI = (int) (blue*255);
                        color = (redI << 16) | (greenI << 8) | blueI;
                        FontEngine.RenderSection section = new FontEngine.RenderSection(mask,color,((FontRenderer)((Object)this)).alpha,sectionString);
                        sections.add(section);
                        sectionString = ""; // 重置字符串收集
                        mask = 0;
                    }
                }
            }
            else {
                sectionString += c;
                if (i==text.length()-1) {
                    FontEngine.RenderSection section = new FontEngine.RenderSection(mask,color,((FontRenderer)((Object)this)).alpha,sectionString);
                    sections.add(section);
                }
            }
        }
        if (shadow) {
            ((FontRenderer) ((Object) this)).posX -= 0.5f;
            ((FontRenderer) ((Object) this)).posY -= 0.5f;
        }
        for (FontEngine.RenderSection section : sections) {
            section.setColor();
            ((FontRenderer) ((Object) this)).textColor = section.rgb;
            for (int i = 0; i < section.text.length(); i++) {
                int codepoint = section.text.codePointAt(i);
                String c = new String(Character.toChars(codepoint));
                float width = FontEngine.renderCharAt(c,((FontRenderer)((Object)this)).posX,((FontRenderer)((Object)this)).posY,mask);
                ((FontRenderer)((Object)this)).posX += width;
            }
        }
        ci.cancel();
    }
}
