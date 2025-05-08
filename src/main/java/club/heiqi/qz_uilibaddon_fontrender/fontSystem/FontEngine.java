package club.heiqi.qz_uilibaddon_fontrender.fontSystem;

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
        CharType type;
        switch (mask) {
            case 0b1000 -> {
                type = CharType.BOLD;
            }
            case 0b1 -> {
                type = CharType.ITALY;
            }
            default -> {
                type = CharType.NORMAL;
            }
        }
        hashName = type.type+"-"+c;
        // 1.确认字符页存在该字形
        if (!CharPage.charWithType.contains(hashName)) {
            switch (type) {
                case NORMAL -> {
                    CharPage.autoAddNormalChar(c,FontLoader.fonts);
                }
                case BOLD -> {
                    CharPage.autoAddBoldChar(c,FontLoader.fonts);
                }
                case ITALY -> {
                    CharPage.autoAddItalyChar(c,FontLoader.fonts);
                }
            }
            CharPage.autoAddNormalChar(c, FontLoader.fonts);
        }
        // 2.获取字形信息
        CharPage.StoredChar storedChar;
        if (!cache.asMap().containsKey(hashName)) {
            storedChar = CharPage.findStoredChar(type.type,c);
            if (storedChar == null) return width/2f; // 字形为null可能是还未添加完毕
            cache.put(hashName,storedChar);
        }
        else {
            storedChar = cache.asMap().get(hashName);
        }
        // 3.使用获取到的字形信息进行渲染操作
        width = (storedChar.right-storedChar.left)*(8f/storedChar.charPage.charSize);
        storedChar.renderAt(x,y,width,8f);
        return Math.min(width,8);
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


    public static void MarkString(String text) {

    }
}
