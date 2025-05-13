package club.heiqi.qz_uilibaddon_fontrender.forgeConfigGUI;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import club.heiqi.qz_uilibaddon_fontrender.ConstField;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    public static Logger LOG = LogManager.getLogger();

    public static Configuration config;
    public static String configPath;
    public static String CLIENT = "client";

    public static double advance = 0;
    public static double height = 8;
    public static double shadowOffsetX = 0.5;
    public static double shadowOffsetY = 0.5;
    public static double fixWidth = 7;

    public static boolean useUnicodeFix = true;
    public static boolean useFixWidth = false;

    public static void init(File configFile) {
        configPath = configFile.getAbsolutePath();
        config = new Configuration(configFile);
        load();
        /*
         * if (configuration.hasChanged()) {
         * configuration.save();
         * }
         */
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent event) {
        config.save();
        load();
    }

    public static void walkMap(Consumer<Property> consumer) {
        List<Property> properties = new ArrayList<>();
        Property advance = config.get(CLIENT, "advance", 1, "控制字符横向间距", -Double.MAX_VALUE,Double.MAX_VALUE);
        Property height = config.get(CLIENT, "height", 8, "控制字符高度", -Double.MAX_VALUE,Double.MAX_VALUE);
        Property shadowOffsetX = config.get(CLIENT, "shadowOffsetX", 0.5, "控制字体阴影横向偏移", -Double.MAX_VALUE,Double.MAX_VALUE);
        Property shadowOffsetY = config.get(CLIENT, "shadowOffsetY", 0.5, "控制字体阴影纵向偏移", -Double.MAX_VALUE,Double.MAX_VALUE);
        Property fixWidth = config.get(CLIENT,"fixWidth",7,"固定宽度模式下的宽度值",-Double.MAX_VALUE,Double.MAX_VALUE);

        Property useUnicodeFix = config.get(CLIENT, "useUnicodeFix", true, "是否启用unicode修复功能");
        Property useFixWidth = config.get(CLIENT,"useFixWidth",false,"是否固定让每个字符的宽度一样");

        properties.add(advance);
        properties.add(height);
        properties.add(shadowOffsetX);
        properties.add(shadowOffsetY);
        properties.add(fixWidth);

        properties.add(useUnicodeFix);
        properties.add(useFixWidth);

        properties.forEach(consumer);
    }

    /**
     * 将配置写入字段
     */
    public static void load() {
        walkMap(p -> {
            String key = p.getName();
            try {
                Field field = Config.class.getField(key);
                if (field.getType() == double.class) {
                    field.setDouble(null, p.getDouble());
                } else if (field.getType() == boolean.class) {
                    field.setBoolean(null, p.getBoolean());
                }
            } catch (NoSuchFieldException e) {
                LOG.warn("无法获取字段:{}", key);
            } catch (IllegalAccessException e) {
                LOG.warn("无法设置字段:{} -> {}", key, p.getDefault());
            }
        });
        config.save();
    }

    public Config register() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        return this;
    }
}
