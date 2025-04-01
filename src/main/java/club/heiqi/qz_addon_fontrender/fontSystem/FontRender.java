package club.heiqi.qz_addon_fontrender.fontSystem;

import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Map;

public class FontRender {

    public static CharInfo findInfo(String t,String type) {
        // 首先寻找高速缓存
        switch (type) {
            case Page.NORMAL -> {
                for (Map.Entry<String, CharInfo> entry : Page.NORMAL_CACHE.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals(t)) {
                        return entry.getValue();
                    }
                }
            }
            case Page.BOLD -> {
                for (Map.Entry<String, CharInfo> entry : Page.BOLD_CACHE.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals(t)) {
                        return entry.getValue();
                    }
                }
            }
        }
        // 如果高速缓存中没有找到开始遍历所有存储字符
        for (Map.Entry<Page, String> entry : Page.GLOBAL.entrySet()) {
            Page page = entry.getKey();
            String typeV = entry.getValue();
            if (!typeV.equals(t)) continue; // 类型不符合直接过滤
            for (Map.Entry<String, CharInfo> infoEntry : page.page.entrySet()) {
                String key = infoEntry.getKey();
                if (key.equals(t)) {
                    return infoEntry.getValue();
                }
            }
        }
        // 如果依然没有找到则需要新建
        return Page.dynamicAddChar(t,type);
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
    public static float renderChar(String t,int x,int y,int color,String type) {
        CharInfo info = findInfo(t,type);
        if (info.page.textureID == -1) return 8f;
        float fontHeight = 8f;
        y = (int) (y-fontHeight/2f);
        float width = x + (float) (info.right - info.left) /CharInfo.pageWidth;
        // 绑定Page纹理页
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,info.page.textureID);
        // 开始绘制
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x,y,0);GL11.glTexCoord2f(info.getU1(),info.getV1()); // 左上
        GL11.glVertex3f(x,y+fontHeight,0);GL11.glTexCoord2f(info.getU1(),info.getV2()); // 左下
        GL11.glVertex3f(x+width,y+fontHeight,0);GL11.glTexCoord2f(info.getU2(),info.getV2()); // 右下
        GL11.glVertex3f(x+width,y,0);GL11.glTexCoord2f(info.getU2(),info.getV1()); // 右上
        GL11.glEnd();
        return width;
    }
}
