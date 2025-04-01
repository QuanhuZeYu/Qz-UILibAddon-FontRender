package club.heiqi.qz_addon_fontrender.fontSystem;

public class CharInfo {
    public final int left,right,top,bottom;
    public Page page;
    public static int pageWidth,pageHeight;

    public CharInfo(int left, int right, int top, int bottom, Page page) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.page = page;
    }
    // 左上角00坐标系
    public float getU1() {
        return (float) top /pageHeight;
    }

    public float getU2() {
        return (float) bottom /pageHeight;
    }

    public float getV1() {
        return (float) left / pageWidth;
    }

    public float getV2() {
        return (float) right / pageWidth;
    }
}
