package club.heiqi.qz_addon_fontrender.fontSystem;

import club.heiqi.qz_addon_fontrender.ConstField;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page {
    /**记录页面类型*/
    public static Logger LOG = LogManager.getLogger();
    public static final String NORMAL = "normal";
    public static final String BOLD = "bold";
    public static Map<Page,String> GLOBAL = new HashMap<>();
    // 高速缓存
    public static LRUCharCache NORMAL_CACHE = new LRUCharCache();
    public static LRUCharCache BOLD_CACHE = new LRUCharCache();
    public static int PAGE_SIZE = 2048;
    public static int GRID_SIZE = 64;
    public static float FONT_SIZE = 64f*0.9f;
    public static int MAX_COUNT = (PAGE_SIZE*PAGE_SIZE)/(GRID_SIZE*GRID_SIZE);

    public Surface surface;
    public Canvas canvas;
    public Paint paint;
    /**记录当前页面已存字符数量*/
    public int countMark = 0;
    public int textureID = -1;
    public boolean isDirty = false;
    public Map<String,CharInfo> page = new HashMap<>();

    public Page() {
        surface = Surface.makeRaster(ImageInfo.makeN32Premul(PAGE_SIZE, PAGE_SIZE));
        canvas = surface.getCanvas();
        paint = new Paint().setColor(0xFFFFFFFF);
    }

    public CharInfo addChar(String t, String type) {
        Font font = new Font(Typeface.makeDefault());
        switch (type) {
            case NORMAL -> {
                font = new Font(FontManager.findFont(t).getTypeface(),FONT_SIZE);
            }
            case BOLD -> {
                font = new Font(FontManager.findBoldFont(t).getTypeface(),FONT_SIZE);
            }
        }
        int colsPerRow = PAGE_SIZE / GRID_SIZE; // 每行的列数
        int row = countMark / colsPerRow;  // 行号对应Y轴
        int col = countMark % colsPerRow;  // 列号对应X轴
        int pageLX = col * GRID_SIZE;  // X坐标 = 列号 * 格子尺寸
        int pageTY = row * GRID_SIZE;  // Y坐标 = 行号 * 格子尺寸

        Rect rect = font.measureText(t);
        FontMetrics metrics = font.getMetrics();
        float charWidth = -rect.getLeft()+rect.getRight();
        float zeroCharXOffset = -rect.getLeft();
        float baseLineYOffset = -metrics.getAscent()-metrics.getDescent()+(GRID_SIZE*0.08f);
        // 顶部调整
        /*if (rect.getTop()+baseLineYOffset < 0) {
            float descent = -(rect.getTop()+baseLineYOffset);
            baseLineYOffset += descent;
        }*/

        pageLX = (int) (pageLX+zeroCharXOffset);
        int pageBY = (int) (pageTY + baseLineYOffset);
        int pageRX = (int) (pageLX + charWidth);

        canvas.drawString(t,pageLX,pageBY,font,paint);
        LOG.info("已将 [{}]-{} 字符, 绘制到 ({},{})",countMark,t,pageLX,pageTY);
        font.close(); // 清除临时字体
        // 统计
        countMark++;
        isDirty = true;
        CharInfo charInfo = new CharInfo(pageLX-1,pageRX+1,pageTY,pageBY,this);
        page.put(t,charInfo);
        return charInfo;
    }

    public void uploadGPU() {
        if (!isDirty) return;
        // 清除之前的纹理ID进行更新
        if (textureID != -1) GL11.glDeleteTextures(textureID);
        // 获取画布图像数据
        ByteBuffer pixBuffer = ByteBuffer.allocateDirect(PAGE_SIZE*PAGE_SIZE*4);
        Pixmap pixmap = Pixmap.make(surface.getImageInfo(),pixBuffer,PAGE_SIZE*4);
        surface.readPixels(pixmap,0,0);
        //新建纹理
        textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
        // 上传纹理数据
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL12.GL_BGRA,
                PAGE_SIZE,
                PAGE_SIZE,
                0,
                GL12.GL_BGRA,
                GL11.GL_UNSIGNED_BYTE,
                pixmap.getBuffer()
        );
        // 生成mipmap
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        isDirty = false;
    }

    public void savePNG(String filename) {
        Image image = surface.makeImageSnapshot();
        Data data = EncoderPNG.encode(image);
        if (data == null) return;
        byte[] bytes = data.getBytes();
        try {
            File save = new File(ConstField.MC_DIR, "图像保存"+File.separator+filename+".png");
            if (!save.getParentFile().exists()) save.getParentFile().mkdirs();
            Files.write(save.toPath(),bytes);
        } catch (IOException e) {
            LOG.error("{} 保存失败:\n{}",filename,e.getMessage());
        }
        image.close(); data.close();
    }







    public static void addChar(List<String> ts, String type) {
        for (int i = 0;i < ts.size();i++) {
            int codepoint = ts.get(i).codePointAt(0);
            String t = new String(Character.toChars(codepoint));
            Page page = null;
            switch (type) {
                case NORMAL -> {
                    page = findValidNormalPage();
                    page.addChar(t,type);
                    LOG.info("添加字符:{}",t);
                }
                case BOLD -> {
                    page = findValidBoldPage();
                    page.addChar(t,type);
                }
            }
        }
    }

    public static CharInfo dynamicAddChar(String t,String type) {
        Page page;
        CharInfo charInfo = null;
        switch (type) {
            case NORMAL -> {
                page = findValidNormalPage();
                charInfo = page.addChar(t,type);
            }
            case BOLD -> {
                page = findValidBoldPage();
                charInfo = page.addChar(t,type);
            }
            default -> {throw new RuntimeException("不存在的字体类型:"+type);}
        }
        return charInfo;
    }

    public static @NotNull List<Page> findNormalPages() {
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<Page, String> entry : GLOBAL.entrySet()) {
            Page page = entry.getKey();
            String type = entry.getValue();
            if (type.equals(NORMAL)) {
                pages.add(page);
            }
        }
        return pages;
    }
    public static @NotNull List<Page> findBoldPages() {
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<Page, String> entry : GLOBAL.entrySet()) {
            Page page = entry.getKey();
            String type = entry.getValue();
            if (type.equals(BOLD)) {
                pages.add(page);
            }
        }
        return pages;
    }
    public static @NotNull Page findValidNormalPage() {
        List<Page> pages = findNormalPages();
        if (!pages.isEmpty()) {
            for (Page page : pages) {
                int count = page.countMark;
                if (count < MAX_COUNT) {
                    return page;
                }
            }
        }
        Page page = new Page();
        GLOBAL.put(page,NORMAL);
        return page;
    }
    public static @NotNull Page findValidBoldPage() {
        List<Page> pages = findBoldPages();
        if (!pages.isEmpty()) {
            for (Page page : pages) {
                int count = page.countMark;
                if (count < MAX_COUNT) {
                    return page;
                }
            }
        }
        Page page = new Page();
        GLOBAL.put(page,BOLD);
        return page;
    }


    /*public static void main(String[] args) {
        // 创建输出目录
        File out = new File("图像输出");
        if (!out.exists()) out.mkdirs();

        File outPNG = new File(out, "char.png");

        // 创建Skia表面和画布
        int width = 64;
        int height = 64;
        try (Surface surface = Surface.makeRaster(ImageInfo.makeN32Premul(width, height))) {
            Canvas canvas = surface.getCanvas();

            // 清除画布为白色背景
            canvas.clear(0x00000000);

            // 创建字体和画笔
            try (Font font = new Font(Typeface.makeFromFile("C:\\Windows\\Fonts\\seguiemj.ttf"), 64*0.9f);
                 Paint paint = new Paint().setColor(0xFFFFFFFF)) {

                // 绘制"锅"字
                String text = "😭";
                // 计算细节
                Rect rect = font.measureText(text);
                FontMetrics metrics = font.getMetrics();
                float charWidth = -rect.getLeft()+rect.getRight();
                float offsetX = ((-rect.getLeft()-rect.getRight())/2f)+(width/2f);
                float offsetY = -metrics.getAscent()- metrics.getDescent();
                LOG.info("TOP:{}",rect.getTop()+offsetY);
                if (rect.getTop()+offsetY < 0) {
                    float descent = -(rect.getTop()+offsetY);
                    offsetY += descent;
                }
                LOG.info("zeroX:{} zeroY:{} width:{}",offsetX,offsetY,charWidth);

                canvas.drawString(text, offsetX, offsetY, font, paint);

                // 保存为PNG文件
                try (Image image = surface.makeImageSnapshot();
                     Data data = EncoderPNG.encode(image)
                ) {
                    if (data != null) {
                        byte[] bytes = data.getBytes();
                        Files.write(outPNG.toPath(), bytes);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("字符图像已保存到: " + outPNG.getAbsolutePath());
    }*/
}
