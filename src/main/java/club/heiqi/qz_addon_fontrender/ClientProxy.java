package club.heiqi.qz_addon_fontrender;

import club.heiqi.qz_addon_fontrender.fontSystem.ClientRenderPoller;
import club.heiqi.qz_addon_fontrender.fontSystem.FontManager;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    public static boolean isInit = false;
    public static ClientRenderPoller poller;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        FontManager.init();
        FontManager.registryChars();
        poller = new ClientRenderPoller().register();
        isInit = true;
    }
}
