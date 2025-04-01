package club.heiqi.qz_addon_fontrender.fontSystem;

import club.heiqi.qz_uilib.skija.font.FontLoader;
import io.github.humbleui.skija.Font;

import java.util.*;

/**
 * 只负责管理字体
 */
public class FontManager {
    public static List<Font> fonts = new ArrayList<>();

    public static void init() {
        fonts.addAll(FontLoader.fonts);
    }

    public static Font findFont(String t) {
        int codepoint = t.codePointAt(0);
        for (Font f : fonts) {
            if (f.getUTF32Glyph(codepoint) !=0) return f;
        }
        return fonts.get(0);
    }

    public static Font findBoldFont(String t) {
        int codepoint = t.codePointAt(0);
        for (Font f : fonts) {
            if (Objects.requireNonNull(f.getTypeface()).isBold())
                if (f.getUTF32Glyph(codepoint) !=0) return f;
        }
        return findFont(t);
    }

    public static Font findItalicFont(String t) {
        int codepoint = t.codePointAt(0);
        for (Font f : fonts) {
            if (Objects.requireNonNull(f.getTypeface()).isItalic())
                if (f.getUTF32Glyph(codepoint) !=0) return f;
        }
        return findFont(t);
    }

    public static void registryChars() {
        List<UnicodeRecorder.UnicodeType> types = Arrays.asList(
                UnicodeRecorder.UnicodeType.BASIC_LATIN,
                UnicodeRecorder.UnicodeType.LATIN1_SUPPLEMENT,
                UnicodeRecorder.UnicodeType.GENERAL_PUNCTUATION,
                UnicodeRecorder.UnicodeType.CJK_UNIFIED_IDEOGRAPHS,
                UnicodeRecorder.UnicodeType.EMOTICONS
        );
        Iterator<UnicodeRecorder.UnicodeType> it = types.iterator();
        while (it.hasNext()) {
            UnicodeRecorder.UnicodeType type = it.next();
            int start = type.start;
            int end = type.end;
            List<Integer> codepoints = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                if (!Character.isValidCodePoint(i)) continue;
                codepoints.add(i);
                if (codepoints.size() == Page.MAX_COUNT) {
                    List<String> ts = new ArrayList<>();
                    codepoints.forEach(codepoint -> {
                        String t = new String(Character.toChars(codepoint));
                        ts.add(t);
                    });
                    Page.addChar(ts,Page.NORMAL);
                    codepoints.clear();
                }
            }
            if (!codepoints.isEmpty()) {
                List<String> ts = new ArrayList<>();
                codepoints.forEach(codepoint -> {
                    String t = new String(Character.toChars(codepoint));
                    ts.add(t);
                });
                Page.addChar(ts,Page.NORMAL);
            }
        }
        // 添加完成后上传至GPU
        int i = 0;
        for (Map.Entry<Page, String> entry : Page.GLOBAL.entrySet()) {
            Page page = entry.getKey();
            String type = entry.getValue();
            page.uploadGPU();
            /*page.savePNG(type+"-"+i);*/
            i++;
        }
    }
}
