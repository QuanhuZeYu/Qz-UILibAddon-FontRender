package club.heiqi.qz_addon_fontrender.fontSystem;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class ClientRenderPoller {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            int i = 0;
            for (Map.Entry<Page, String> entry : Page.GLOBAL.entrySet()) {
                Page page = entry.getKey();
                String type = entry.getValue();
                if (page.isDirty) {
                    page.uploadGPU();
                    // DEBUG保存图像查看
                    page.savePNG(type+"-"+i);
                }
                i++;
            }
        }
    }

    public ClientRenderPoller register() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        return this;
    }
}
