package club.heiqi.qz_uilibaddon_fontrender.fontSystem;

import static org.lwjgl.opengl.GL11.*;

public class RenderSection {
    public boolean isStart = false;
    public boolean hasColor = false;
    public boolean end = false;

    public boolean random = false;
    public boolean bold = false;
    public boolean delete = false;
    public boolean underline = false;
    public boolean italic = false;
    public int rgb;
    public float alpha;
    public int mask;
    public String text = "";

    public int restRGB;
    public float restAlpha;
    public boolean rest;

    public RenderSection() {
        isStart = true;
    }

    public RenderSection(boolean isStart, boolean hasColor, boolean end, boolean random, boolean bold, boolean delete, boolean underline, boolean italic, int rgb, float alpha, int mask, String text, int restRGB, float restAlpha, boolean rest) {
        this.isStart = isStart;
        this.hasColor = hasColor;
        this.end = end;
        this.random = random;
        this.bold = bold;
        this.delete = delete;
        this.underline = underline;
        this.italic = italic;
        this.rgb = rgb;
        this.alpha = alpha;
        this.mask = mask;
        this.text = text;
        this.restRGB = restRGB;
        this.restAlpha = restAlpha;
        this.rest = rest;
    }

    public void appendString(String text) {
        this.text += text;
    }

    public void addMask(int mask) {
        if ((mask & 0b1) != 0) italic = true;
        if ((mask & 0b10) != 0) underline = true;
        if ((mask & 0b100) != 0) delete = true;
        if ((mask & 0b1000) != 0) bold = true;
        if ((mask & 0b10000) != 0) random = true;
        this.mask |= mask;
    }

    public void setRGB(int color) {
        if (hasColor) {
            restRGB = color;
            end = true;
            rest = true;
        } else {
            hasColor = true;
            rgb = color;
        }
    }

    /**
     * @param mask 掩码记录 1:随机 2:粗体 3:删除线 4:下划线 5:斜体 0b00000
     * @param rgb  rgba
     * @param text 字符串
     */
    public RenderSection(int mask, int rgb, float alpha, String text, int restRGB, float restAlpha) {
        this.mask = mask;
        addMask(mask);
        this.rgb = rgb;
        this.text = text;
        this.alpha = alpha;
        this.restRGB = restRGB;
        this.restAlpha = restAlpha;
    }

    public void setGLColor() {
        float r = ((rgb >> 16) & 255) / 255f;
        float g = ((rgb >> 8) & 255) / 255f;
        float b = ((rgb) & 255) / 255f;
        glColor4f(r, g, b, alpha);
    }
    public void renderExtraStyle(float x,float y,float width,float height) {
        if (delete) {
            glDisable(GL_TEXTURE_2D);
            glBegin(GL_QUADS);
            glVertex2f(x+width,y+(height/2));   glVertex2f(x,y+(height/2));
            glVertex2f(x,y+(height/2)+0.5f);      glVertex2f(x+width,y+(height/2)+0.5f);
            glEnd();
            glEnable(GL_TEXTURE_2D);
        }
        if (underline) {
            glDisable(GL_TEXTURE_2D);
            glBegin(GL_QUADS);
            glVertex2f(x+width,y+height);       glVertex2f(x,y+height);
            glVertex2f(x,y+height+1f);          glVertex2f(x+width,y+height+1f);
            glEnd();
            glEnable(GL_TEXTURE_2D);
        }
    }
    public void restGLColor() {
        if (rest) {
            float r = ((restRGB >> 16) & 255) / 255f;
            float g = ((restRGB >> 8) & 255) / 255f;
            float b = ((restRGB) & 255) / 255f;
            glColor4f(r, g, b, restAlpha);
        }
    }

    public RenderSection copy() {
        return new RenderSection(isStart, hasColor, end, random, bold, delete, underline, italic, rgb, alpha, mask, text, restRGB, restAlpha, rest);
    }
}
