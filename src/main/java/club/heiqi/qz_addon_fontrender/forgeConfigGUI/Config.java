package club.heiqi.qz_addon_fontrender.forgeConfigGUI;

import java.io.File;

import club.heiqi.qz_addon_fontrender.ConstField;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {
    public static Configuration config;
    public static String configPath;

    public static String greeting = "Hello World";
    public static float dynamicFontSize = 0.0267F;

    public static void init(File configFile) {
        configPath = configFile.getAbsolutePath();
        config = new Configuration(configFile);
        load();
        /*if (configuration.hasChanged()) {
            configuration.save();
        }*/
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(ConstField.MODID)) {
            load();
            save();
        }
    }

    public static void load() {
        Property property = config.get(Configuration.CATEGORY_GENERAL, "greeting", "Hello World", "How shall I greet?");
        greeting = property.getString();
        Property property2 = config.get(Configuration.CATEGORY_GENERAL, "dynamicFontSize", 0.0267, "字体高度所占屏幕高度百分比",0,1);
        dynamicFontSize = (float) property2.getDouble();
        config.save();
    }

    public static void save() {
        Property property = config.get(Configuration.CATEGORY_GENERAL, "greeting", "Hello World", "How shall I greet?");
        property.set(greeting);
        Property property2 = config.get(Configuration.CATEGORY_GENERAL, "dynamicFontSize", 0.0267, "字体高度所占屏幕高度百分比",0,1);
        property2.set(dynamicFontSize);
    }

    public Config register() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        return this;
    }

    /*public static class ConfigObject<T> {
        public Field field;
        public String category;
        public String comment;
        public Field defaultValue;
        @Nullable public Field minValue;
        @Nullable public Field maxValue;

        public String getName() {return field.getName();}
        public String getCategory() {return category;}
        public String getComment() {return comment;}
        public T getField() {
            try {
                return (T) field.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        public T getDefaultValue() {
            try {
                return (T) defaultValue.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        public T getMinValue() {
            try {
                if (minValue != null) {
                    return (T) minValue.get(this);
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        public T getMaxValue() {
            try {
                if (maxValue != null) {
                    return (T) maxValue.get(this);
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }*/
}
