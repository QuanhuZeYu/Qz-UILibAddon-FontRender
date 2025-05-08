package club.heiqi.qz_uilibaddon_fontrender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ConstField {
    public static final String MODID = "qz_uilibaddon_fontrender";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static final String CLIENT_PROXY = "club.heiqi.qz_uilibaddon_fontrender.ClientProxy";
    public static final String SERVER_PROXY = "club.heiqi.qz_uilibaddon_fontrender.CommonProxy";
    public static final String MOD_NAME = "Qz-UILIBAddon-FontRender";
    public static final File MC_DIR = new File(System.getProperty("user.dir"));
}
