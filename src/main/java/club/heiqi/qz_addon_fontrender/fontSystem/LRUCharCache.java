package club.heiqi.qz_addon_fontrender.fontSystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCharCache extends LinkedHashMap<String, CharInfo> {
private static final int MAX_CAPACITY = 10000; // 最大容量

    public LRUCharCache() {
        super(16, 0.75f, true); // 初始容量, 负载因子, true表示按访问顺序排序
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CharInfo> eldest) {
        // 当容量超过最大值时，自动移除最久未使用的条目
        return size() > MAX_CAPACITY;
    }

}
