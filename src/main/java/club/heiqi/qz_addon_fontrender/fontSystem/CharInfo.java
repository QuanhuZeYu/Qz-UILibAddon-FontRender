package club.heiqi.qz_addon_fontrender.fontSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CharInfo {
    public static Logger LOG = LogManager.getLogger();

    public final int left,right,top,bottom;
    public Page page;

    public CharInfo(int left, int right, int top, int bottom, Page page) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.page = page;
    }
    // 左上角00坐标系
    public float getU1() {
        float u1 = (float) left / Page.PAGE_SIZE;
        return u1;
    }

    public float getU2() {
        float u2 = (float) right / Page.PAGE_SIZE;
        return u2;
    }

    public float getV1() {
        float v1 = (float) top / Page.PAGE_SIZE;
        return v1;
    }

    public float getV2() {
        float v2 = (float) bottom / Page.PAGE_SIZE;
        return v2;
    }
}
