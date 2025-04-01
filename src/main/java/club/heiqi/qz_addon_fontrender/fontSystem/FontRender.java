package club.heiqi.qz_addon_fontrender.fontSystem;

import club.heiqi.qz_uilib.skija.state.SkiaStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Map;

public class FontRender {
    public static Logger LOG = LogManager.getLogger();
    public static float SPACE = 0.5f;
    public static float WORD_SPACING = 1.0f;

    public static CharInfo findInfo(String t,String type) {
        // 首先寻找高速缓存
        CharInfo charInfo = null;
        switch (type) {
            case Page.NORMAL -> {
                for (Map.Entry<String, CharInfo> entry : Page.NORMAL_CACHE.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals(t)) {
                        charInfo = entry.getValue();
                        if (charInfo != null) return charInfo;
                    }
                }
            }
            case Page.BOLD -> {
                for (Map.Entry<String, CharInfo> entry : Page.BOLD_CACHE.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals(t)) {
                        charInfo = entry.getValue();
                        if (charInfo != null) return charInfo;
                    }
                }
            }
        }
        // 如果高速缓存中没有找到开始遍历所有存储字符
        if (Page.GLOBAL.isEmpty()) throw new RuntimeException("不应该为空");
        for (Map.Entry<Page, String> entry : Page.GLOBAL.entrySet()) {
            Page page = entry.getKey();
            String typeV = entry.getValue();
            if (!typeV.equals(type)) {
                continue; // 类型不符合直接过滤
            }
            for (Map.Entry<String, CharInfo> infoEntry : page.page.entrySet()) {
                String key = infoEntry.getKey();
                if (key.equals(t)) {
                    charInfo = infoEntry.getValue();
                }
            }
        }
        // 如果依然没有找到则需要新建
        if (charInfo == null) {
            LOG.info("全局缓存中没有找到:[{}]",t);
            charInfo = Page.dynamicAddChar(t,type);
        }
        // 将结果缓存到高速中
        switch (type) {
            case Page.NORMAL -> {
                Page.NORMAL_CACHE.put(t,charInfo);
            }
            case Page.BOLD -> {
                Page.BOLD_CACHE.put(t,charInfo);
            }
        }
        return charInfo;
    }

    /**
     *
     * @param t
     * @param x
     * @param y
     * @param color RGBA
     * @param type 使用{@code Page}下的常量字符串表示
     * @return
     */
    public static float renderChar(String t,float x,float y,int color,String type) {
        switch (t) {
            case " ": return 8*SPACE;
        }
        CharInfo info = findInfo(t,type);
        if (info.page.textureID == -1) return 8f*SPACE;
        float fontHeight = 8f;
        float width = ((float) (info.right - info.left) /Page.GRID_SIZE)*8f;
        // 绑定Page纹理页
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,info.page.textureID);
        try {
            SkiaStore.glBindTexture.invoke(GL11.GL_TEXTURE_2D,info.page.textureID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        // 混合
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        /*try {
            SkiaStore.glBindTexture.invoke(GL11.GL_TEXTURE_2D,info.page.textureID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }*/
        // UV
        float u1 = info.getU1(), u2 = info.getU2(), v1 = info.getV1(), v2 = info.getV2();
        /*u1 = 0.125f; u2 = 0.1508789f; v1 = 0.65625f; v2 = 0.6875f;*/
        // 开始绘制
        try {
            GL11.glBegin(GL11.GL_QUADS);
            SkiaStore.glTexCoord2f.invoke(u1, v1);SkiaStore.glVertex3f.invoke(x,y,0);
            SkiaStore.glTexCoord2f.invoke(u1, v2);SkiaStore.glVertex3f.invoke(x,y+fontHeight,0);
            SkiaStore.glTexCoord2f.invoke(u2, v2);SkiaStore.glVertex3f.invoke(x+width,y+fontHeight,0);
            SkiaStore.glTexCoord2f.invoke(u2,v1);SkiaStore.glVertex3f.invoke(x+width,y,0);
            GL11.glEnd();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        /*renderDebugBezel(x,y,width,fontHeight);*/
        return width + WORD_SPACING;
    }

    public static void renderDebugBezel(float x,float y,float width,float height) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(1,0,0);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(x,y,0); // 左上
        GL11.glVertex3f(x,y+8,0); // 左下
        GL11.glVertex3f(x+width,y+8,0); // 右下
        GL11.glVertex3f(x+width,y,0); // 右上
        GL11.glVertex3f(x,y,0); // 左上
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
