package club.heiqi.qz_uilibaddon_fontrender.fontSystem;

public enum CharType {
    NORMAL("normal"),
    BOLD("bold"),
    ITALY("italy"),
    ;
    final String type;
    CharType(String type) {
        this.type = type;
    }
}
