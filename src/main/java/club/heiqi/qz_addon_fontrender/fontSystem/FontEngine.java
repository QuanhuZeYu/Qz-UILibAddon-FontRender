package club.heiqi.qz_addon_fontrender.fontSystem;

import club.heiqi.qz_uilib.skija.font.FontLoader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class FontEngine {
    /**
     * 高速缓存<br/>
     * "type-字符": 字符信息*/
    public static Cache<String, CharPage.StoredChar> cache = CacheBuilder.newBuilder().maximumSize(10000).build();

    /**
     *
     * @param c
     * @param x
     * @param y
     * @param mask 掩码 -0b1: 普通字体 -0b10: 粗体 -0b100: 斜体
     */
    public static float renderCharAt(String c,float x,float y,int mask) {
        float width = 8f;
        if (c.equals(" ")) return width/2f;
        String hashName;
        String type;
        switch (mask) {
            case 0b1 -> {
                type = CharType.NORMAL.type;
            }
            case 0b10 -> {
                type = "bold";
            }
            case 0b100 -> {
                type = "italy";
            }
            default -> {
                type = CharType.NORMAL.type;
            }
        }
        hashName = CharType.NORMAL.type+"-"+c;
        // 1.确认字符页存在该字形
        if (!CharPage.charWithType.contains(hashName)) {
            CharPage.autoAddNormalChar(c, FontLoader.fonts);
        }
        // 2.获取字形信息
        CharPage.StoredChar storedChar;
        if (!cache.asMap().containsKey(hashName)) {
            storedChar = CharPage.findStoredChar(type,c);
            if (storedChar == null) return width/2f; // 字形为null可能是还未添加完毕
            cache.put(hashName,storedChar);
        }
        else {
            storedChar = cache.asMap().get(hashName);
        }
        // 3.使用获取到的字形信息进行渲染操作
        width = (storedChar.right-storedChar.left)*(8f/storedChar.charPage.charSize);
        storedChar.renderAt(x,y,width,8f);
        return width;
    }

    /**
     * 绘制Debug外框
     * @param x 屏幕左上
     * @param y 左上
     * @param w 宽度
     * @param h 高度
     */
    public static void renderDebugLine(float x,float y,float w,float h) {
        // 禁用纹理和设置颜色（例如红色）
        glDisable(GL_TEXTURE_2D);
        glColor4f(1.0f, 0.0f, 0.0f, 1f); // 红色

        // 启用混合（Blending）以实现透明效果
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 绘制线框矩形
        glBegin(GL_LINE_LOOP);
        {
            glVertex2f(x, y);          // 左上角
            glVertex2f(x + w, y);      // 右上角
            glVertex2f(x + w, y + h);  // 右下角
            glVertex2f(x, y + h);      // 左下角
        }
        glEnd();

        // 恢复之前的设置
        glEnable(GL_TEXTURE_2D);
        glColor3f(1.0f, 1.0f, 1.0f); // 恢复默认白色
    }


    public static class RenderSection {
        public boolean random = false;
        public boolean bold = false;
        public boolean delete = false;
        public boolean underline = false;
        public boolean italic = false;
        public int rgb;
        public float alpha;
        public int mask;
        public String text;

        /**
         * @param mask 掩码记录 1:随机 2:粗体 3:删除线 4:下划线 5:斜体 0b00000
         * @param rgb rgba
         * @param text 字符串
         */
        public RenderSection(int mask, int rgb, float alpha, String text) {
            this.mask = mask;
            if ((mask & 0b1) != 0) italic = true;
            if ((mask & 0b10) != 0) underline = true;
            if ((mask & 0b100) != 0) delete = true;
            if ((mask & 0b1000) != 0) bold = true;
            if ((mask & 0b10000) != 0) random = true;
            this.rgb = rgb;this.text = text;this.alpha = alpha;
        }
        public void setColor() {
            float r = ((rgb >> 16)&255) / 255f;
            float g = ((rgb >> 8)&255) / 255f;
            float b = ((rgb)&255) / 255f;
            glColor4f(r,b,g,alpha);
        }
    }







    public static void MarkString(String text) {

    }
}
