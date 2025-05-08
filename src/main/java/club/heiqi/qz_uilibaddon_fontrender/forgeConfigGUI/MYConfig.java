package club.heiqi.qz_uilibaddon_fontrender.forgeConfigGUI;

import club.heiqi.qz_uilibaddon_fontrender.ConstField;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MYConfig extends GuiConfig {

    public MYConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                getConfigElements(),
                ConstField.MODID,
                false,
                false,
                ConstField.MOD_NAME,
                GuiConfig.getAbridgedConfigPath(Config.configPath)
        );
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = new ArrayList<>();

        List<String> topCategories = Arrays.asList(Configuration.CATEGORY_GENERAL,Config.CLIENT);
        for (String categoryName : topCategories) {
            ConfigCategory category = Config.config.getCategory(categoryName);
            elements.add(new ConfigElement(category));
        }

        return elements;
    }
}
