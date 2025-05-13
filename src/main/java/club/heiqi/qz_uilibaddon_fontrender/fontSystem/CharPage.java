package club.heiqi.qz_uilibaddon_fontrender.fontSystem;

import club.heiqi.qz_uilibaddon_fontrender.ConstField;
import club.heiqi.qz_uilib.skija.font.*;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.*;
import io.netty.util.internal.ConcurrentSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * 单个字符页
 */
public class CharPage {
    public static Logger LOG = LogManager.getLogger();
    /**标识符(fontName-type-count):字符页*/
    public static ConcurrentHashMap<String,CharPage> allPages = new ConcurrentHashMap<>();
    /**存储格式要求 type-char*/
    public static ConcurrentSet<String> charWithType = new ConcurrentSet<>();
    public static boolean canUpload = false;

    /** 每个字符页的标识信息 - 粗体 - 标准 - 斜体 - 标识格式:fontname-type-页数*/
    public volatile String markInfo = "DEFAULT";
    /** 字符页大小 */
    public volatile short size = 2048;
    /** 字体的像素大小 */
    public volatile short charSize = 64;
    /** 当前字符页字符数量 */
    public volatile short charCount = 0;
    /** 当前字符页存储的字符记录 - 字符: 字符信息*/
    public ConcurrentHashMap<String,StoredChar> chars = new ConcurrentHashMap<>();
    public List<String> charList = new CopyOnWriteArrayList<>();
    /** skia */
    public volatile Font font;
    public volatile Surface surface;
    public volatile Canvas canvas;
    public volatile Paint paint;
    /**opengl*/
    public volatile int textureID=-1;
    public volatile long uploadTime;
    public volatile long addCharTime;

    public CharPage(String type, Font font) {
        surface = Surface.makeRaster(ImageInfo.makeN32Premul(size,size));
        canvas = surface.getCanvas();
        paint = new Paint().setAntiAlias(true).setColor4f(new Color4f(255,255,255,255));
        String fontName = font.getTypeface().getFamilyName();
        int count = findPageWithFont(font).size();
        this.markInfo = fontName+"-"+type+"-"+count;
        this.font = font.makeWithSize(charSize*0.90f);
        allPages.put(this.markInfo,this);
    }

    public CharPage(String type,Font font,short pageSize,short charSize) {
        surface = Surface.makeRaster(ImageInfo.makeN32Premul(pageSize,pageSize));
        canvas = surface.getCanvas();
        paint = new Paint().setAntiAlias(true).setColor4f(new Color4f(255,255,255,255));
        String fontName = font.getTypeface().getFamilyName();
        int count = findPageWithFont(font).size();
        this.markInfo = fontName+"-"+type+"-"+count;
        this.font = font.makeWithSize(charSize*0.90f);
        allPages.put(this.markInfo,this);
    }

    /**待添加队列*/
    public LinkedBlockingDeque<String> waitList = new LinkedBlockingDeque<>();
    public Lock lock = new ReentrantLock(true);
    public void addChar(String c) {
        // LOG.info("尝试添加字符:{}",c);
        if (isFull()) return;
        // 避免重复添加
        if (waitList.contains(c) || chars.containsKey(c)) return;
        waitList.add(c);
        new Thread(() -> {
            lock.lock();
            try {
                Font threadFont = font;
                String c1 = c;
                Rect rect = threadFont.measureText(c1,paint);
                FontMetrics metrics = threadFont.getMetrics();
                float width = rect.getRight()-rect.getLeft();
                float height = rect.getBottom()-rect.getTop();
                // 对于超过大小的字形不断缩小字体大小
                while (width >= charSize || height >= charSize) {
                    float size = threadFont.getSize() - 1f;
                    threadFont = threadFont.makeWithSize(size);
                    rect = threadFont.measureText(c1,paint);
                    metrics = threadFont.getMetrics();
                    width = rect.getRight()-rect.getLeft();
                    height = rect.getTop()-rect.getBottom();
                }
                Vector2f leftTopChar = new Vector2f(rect.getLeft(),rect.getTop());
                Vector2f leftCharCenter = new Vector2f(rect.getLeft(),(metrics.getAscent()+metrics.getDescent())/2f);
                Vector2i leftTop = getCurCharPos();
                Vector2f leftCenter = new Vector2f(leftTop.x,(leftTop.y+leftTop.y+charSize)/2f);
                Vector2f offsetVec = new Vector2f(leftCenter.sub(leftCharCenter));
                leftTopChar = leftCharCenter.add(offsetVec);
                canvas.drawString(c1,offsetVec.x,offsetVec.y,threadFont,paint);

                charCount++;
                StoredChar storedChar = new StoredChar(this, c1,(short) leftTop.x,(short) leftTop.y,(short) leftTop.x, (short) (leftTop.x+width));
                chars.put(c1,storedChar);
                charList.add(c1);
                String hashString = markInfo.split("-")[1]+"-"+ c1;
                charWithType.add(hashString);
                addCharTime = System.currentTimeMillis();
                waitList.remove(c1); // 添加完毕后-即字符信息中有该字符后再清除等待列表中的该元素
                canUpload = true;
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public String getPageType() {
        String[] ss = markInfo.split("-");
        return ss[1];
    }

    public int getMaxMipMap() {
        int count = 0;
        int copySize = size;
        while (copySize > 4) {
            copySize /= 2;
            count++;
        }
        return count;
    }

    public long fullTime = Long.MAX_VALUE;
    public boolean isFull() {
        if(charCount + waitList.size() >= getMaxCharCount()) {
            fullTime = System.currentTimeMillis();
        }
        return false;
    }

    public boolean addDone() {
        return waitList.isEmpty();
    }

    public boolean isCharInPage(String c) {
        return chars.containsKey(c) || waitList.contains(c);
    }

    @Nullable
    public StoredChar getStoredChar(String c) {
        return chars.get(c);
    }

    public void uploadTexture() {
        if (uploadTime > addCharTime) return;
        if (textureID==-1) textureID = glGenTextures();

        Image imageSnapshot = surface.makeImageSnapshot();
        ImageInfo imageInfo = imageSnapshot.getImageInfo();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        int rowBytes = width*4;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width*height*4);
        Pixmap pixmap = Pixmap.make(imageInfo,byteBuffer,rowBytes);
        if (!surface.readPixels(pixmap,0,0)) {
            return;
        }

        glBindTexture(GL_TEXTURE_2D,textureID);
        // 设置纹理参数
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // 上传纹理数据
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_BGRA,
            width,
            height,
            0,
            GL_BGRA,
            GL_UNSIGNED_BYTE,
            pixmap.getBuffer()
        );
        // 生成Mipmap
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);
        imageSnapshot.close();pixmap.close();
        /*for (int i = 1; i <= 2; i++) {
            genMipMap(i);
        }*/
        canUpload = false;
    }

    /**
     * 求以左上为原点向右下生长的坐标系下的字符左上角坐标
     * @return
     */
    public Vector2i getCurCharPos() {
        int singleVCount = getMaxCharCount()/(size/charSize);
        int hCount = charCount == 0 ? 0 : charCount%singleVCount; // 求出在第几列
        int vv = charCount == 0 ? 0 : charCount/singleVCount;
        int vPos = vv*charSize; // 纵坐标
        int hPos = hCount*charSize; // 横坐标
        return new Vector2i(hPos,vPos);
    }

    public int getMaxCharCount() {
        return (size*size)/(charSize*charSize);
    }

    public void saveFile(Path saveTarget) {
        Image image = surface.makeImageSnapshot();
        Data data = EncoderPNG.encode(image);
        if (data == null) return;
        byte[] bytes = data.getBytes();
        try {
            Files.write(saveTarget,bytes);
        } catch (IOException e) {
            LOG.error("保存{}失败",saveTarget);
        }
    }

    public void dispose() {
        if (!isSkijaDelete) {
            surface.close();
            paint.close();
        }
        chars.clear();
        charList.clear();
        allPages.remove(markInfo);
        if (textureID != -1) {
            glDeleteTextures(textureID);
        }
    }

    public boolean isSkijaDelete = false;
    public void whenFullPost() {
        // 当页面字符存满后自动清除skija的引用内容
        if (isFull() && System.currentTimeMillis() - fullTime >= 60_000 && !isSkijaDelete) {
            surface.close();
            paint.close();
            isSkijaDelete = true;
        }
    }

    public void genMipMap(int level) {
        // 覆盖部分层级
        CharPage page = new CharPage(getPageType(),font, (short) (size/(2*level)), (short) (charSize/(2*level)));
        for (String c : this.charList) {
            page.addChar(c);
        }
        while (!page.addDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Image imageSnapshot = page.surface.makeImageSnapshot();
        ImageInfo imageInfo = imageSnapshot.getImageInfo();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        int rowBytes = width*4;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width*height*4);
        Pixmap pixmap = Pixmap.make(imageInfo,byteBuffer,rowBytes);
        if (!page.surface.readPixels(pixmap,0,0)) {
            return;
        }

        glBindTexture(GL_TEXTURE_2D,this.textureID);
        // 设置纹理参数
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // 上传纹理数据
        glTexImage2D(
            GL_TEXTURE_2D,
            level,
            GL_BGRA,
            width,
            height,
            0,
            GL_BGRA,
            GL_UNSIGNED_BYTE,
            pixmap.getBuffer()
        );
        page.dispose();
    }













    /**
     * 寻找有指定Font的Page
     * @param font
     * @return
     */
    public static List<CharPage> findPageWithFont(Font font) {
        List<CharPage> result = new ArrayList<>();
        String name = font.getTypeface().getFamilyName();
        for (Map.Entry<String,CharPage> entry : allPages.entrySet()) {
            CharPage charPage = entry.getValue();
            if (charPage.font.getTypeface().getFamilyName().equals(name)) {
                result.add(charPage);
            }
        }
        return result;
    }

    public static List<CharPage> findPageWithType(String type) {
        List<CharPage> result = new ArrayList<>();
        for (Map.Entry<String,CharPage> entry : allPages.entrySet()) {
            String pageType = entry.getKey().split("-")[1];
            if (pageType.equals(type)) result.add(entry.getValue());
        }
        return result;
    }

    public static StoredChar findStoredChar(String type, String c) {
        List<CharPage> pages = findPageWithType(type);
        for (CharPage page : pages) {
            if (page.isCharInPage(c)) {
                return page.getStoredChar(c);
            }
        }
        return null;
    }

    public static void autoAddNormalChar(String c, List<Font> fonts) {
        // 1.找到第一个适合的字体
        for (Font font : fonts) {
            short glyph = font.getUTF32Glyph(c.codePointAt(0));
            if (glyph == 0) continue;
            else {
                CharPage page = findPageWithFont(font).stream().filter(p -> {
                    String type = p.markInfo.split("-")[1];
                    return type.equals(CharType.NORMAL.type) && !p.isFull();
                }).findFirst().orElse(null);
                if (page == null) page = new CharPage(CharType.NORMAL.type, font);
                // 检查是否添加过
                String hashName = CharType.NORMAL.type+"-"+c;
                if (charWithType.contains(hashName)) break;
                // 执行添加逻辑
                page.addChar(c);
                break;
            }
        }
    }
    public static void autoAddBoldChar(String c,List<Font> fonts) {
        // 1.找到第一个适合的字体
        List<Font> suitFonts = new ArrayList<>();
        Font suitFont = null;
        for (Font font : fonts) {
            short glyph = font.getUTF32Glyph(c.codePointAt(0));
            if (glyph == 0) continue;
            suitFonts.add(font);
            if (font.getTypeface() == null) continue;
            if (font.getTypeface().isBold()) {
                suitFont = font;
                break;
            }
        }
        if (suitFont == null) suitFont = suitFonts.get(0);
        CharPage page = findPageWithFont(suitFont).stream().filter(p -> {
            String type = p.markInfo.split("-")[1];
            return type.equals(CharType.BOLD.type) && !p.isFull();
        }).findFirst().orElse(null);
        if (page == null) page = new CharPage(CharType.BOLD.type, suitFont);
        // 检查是否添加过
        String hashName = CharType.BOLD.type+"-"+c;
        if (charWithType.contains(hashName)) return;
        // 执行添加逻辑
        page.addChar(c);
    }
    public static void autoAddItalyChar(String c,List<Font> fonts) {
        // 1.找到第一个适合的字体
        List<Font> suitFonts = new ArrayList<>();
        Font suitFont = null;
        for (Font font : fonts) {
            short glyph = font.getUTF32Glyph(c.codePointAt(0));
            if (glyph == 0) continue;
            suitFonts.add(font);
            if (font.getTypeface() == null) continue;
            if (font.getTypeface().isItalic()) {
                suitFont = font;
                break;
            }
        }
        if (suitFonts.isEmpty()) return;
        if (suitFont == null) suitFont = suitFonts.get(0);
        CharPage page = findPageWithFont(suitFont).stream().filter(p -> {
            String type = p.markInfo.split("-")[1];
            return type.equals(CharType.ITALY.type) && !p.isFull();
        }).findFirst().orElse(null);
        if (page == null) page = new CharPage(CharType.ITALY.type, suitFont);
        // 检查是否添加过
        String hashName = CharType.ITALY.type+"-"+c;
        if (charWithType.contains(hashName)) return;
        // 执行添加逻辑
        page.addChar(c);
    }

    public static boolean isAllAddDone() {
        for (CharPage page : allPages.values()) {
            if (!page.addDone()) return false;
        }
        return true;
    }

    public static void saveAll(boolean force) {
        if (Boolean.parseBoolean(System.getProperty("qzdevmode")) || force) {
            for (CharPage page : allPages.values()) {
                File saveTarget = new File(ConstField.MC_DIR, "图像");
                saveTarget = new File(saveTarget, page.markInfo+".png");
                page.saveFile(saveTarget.toPath());
            }
            LOG.info("图像保存完毕");
        }
    }

    public static void uploadAll() {
        for (CharPage page : allPages.values()) {
            if (page.uploadTime > page.addCharTime) continue;
            page.uploadTexture();
            if (page.isFull() && !page.isSkijaDelete)
                page.whenFullPost();
        }
    }

    /**
     * 清除所有生成的字体，包括OpenGL的贴图
     */
    public static void restAll() {
        for (CharPage page : allPages.values()) {
            page.dispose();
        }
        allPages.clear();
        charWithType.clear();
    }


    public static class StoredChar {
        public CharPage charPage;
        public String character;
        public short x,y,left,right;
        public StoredChar(CharPage charPage, String c,short x,short y,short l,short r) {
            this.charPage=charPage;
            character=c;this.x=x;this.y=y;left=l;right=r;
        }
        public double getU1() {
            return ((double) y / charPage.size);
        }
        public double getU2() {
            return ((double) (y + charPage.charSize) / charPage.size);
        }
        public double getV1() {
            return (double) left / charPage.size;
        }
        public double getV2() {
            return (double) right /charPage.size;
        }
        public void renderAt(float x,float y,float w,float h) {
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, charPage.textureID);
            double u1 = getU1();
            double u2 = getU2();
            double v1 = getV1();
            double v2 = getV2();
            int width = right - left;
            int height = charPage.charSize;
            glBegin(GL_QUADS);
            {
                // 左上角
                glTexCoord2d(v1, u2);
                glVertex2f(x, y + h);
                // 右上角
                glTexCoord2d(v2, u2);
                glVertex2f(x + w, y + h);
                // 右下角
                glTexCoord2d(v2, u1);
                glVertex2f(x + w, y);
                // 左下角
                glTexCoord2d(v1, u1);
                glVertex2f(x, y);
            }
            glEnd();
        }
    }

    /**
     * 测试单元
     * @param arg
     */
    public static void main(String[] arg) {
        FontLoader.load();
        List<Font> fonts = FontLoader.fonts;
        File saveTargetP = new File(ConstField.MC_DIR,"图像");
        if (!saveTargetP.exists()) saveTargetP.mkdirs();

        for (int i = 0x0; i <= 0x1fbff; i++) {
            String c = new String(Character.toChars(i));
            autoAddNormalChar(c,fonts);
        }
        while (!isAllAddDone()) {
            LOG.info("等待字符生成完毕");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        saveAll(false);
    }
}
