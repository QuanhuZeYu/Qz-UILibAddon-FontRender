package club.heiqi.qz_addon_fontrender.forgeConfigGUI;

public class StyledTextSegment {
    public final String text;
    public final int color;
    public final int styleMask;

    public StyledTextSegment(String text, int color, int styleMask) {
        this.text = text;
        this.color = color;
        this.styleMask = styleMask;
    }
}
