package club.heiqi.qz_uilibaddon_fontrender.hook;

import club.heiqi.qz_uilibaddon_fontrender.ClientProxy;
import club.heiqi.qz_uilibaddon_fontrender.fontSystem.CharPage;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientTick {

    public long saveTime;
    public long uploadTime;
    @SubscribeEvent
    public void updateTexture(TickEvent.ClientTickEvent event) {
        if (!ClientProxy.isInit) ClientProxy.isInit = true;
        if (System.currentTimeMillis() - uploadTime >= 1_000L || CharPage.canUpload) {
            CharPage.uploadAll();
            uploadTime = System.currentTimeMillis();
        }
        /*if (System.currentTimeMillis() - saveTime >= 10_000L) {
            new Thread(() -> {
                CharPage.saveAll(false);
            }).start();
            saveTime = System.currentTimeMillis();
        }*/
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
}
