package club.heiqi.qz_addon_fontrender;

import club.heiqi.qz_addon_fontrender.hook.ClientTick;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    public static boolean isInit = false;

    ClientTick clientTick = new ClientTick();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        clientTick.register();
    }
}
