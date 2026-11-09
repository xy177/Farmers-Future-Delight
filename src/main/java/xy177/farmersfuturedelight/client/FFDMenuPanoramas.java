package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDMenuPanoramas implements IResourceManagerReloadListener {
    private static final List<String> VERSIONS = Arrays.asList("1.13.2", "1.14.4", "1.15.2", "1.16.5",
            "1.17.1", "1.18.2", "1.19.4", "1.20.6", "26.1", "26.3");
    private static String checked;
    private static String selected;

    public static boolean custom() {
        return !"RESOURCE_PACK".equalsIgnoreCase(FFDConfig.menuPanorama);
    }

    public static ResourceLocation face(int index) {
        String requested = FFDConfig.menuPanorama;
        if (!requested.equals(checked)) {
            checked = requested;
            selected = VERSIONS.contains(requested) ? requested : null;
            if (selected == null && !"RESOURCE_PACK".equalsIgnoreCase(requested))
                LogManager.getLogger().warn("Unknown menu panorama {}; using resource-pack panorama", requested);
            if (selected != null) {
                for (int face = 0; face < 6; face++) {
                    try (net.minecraft.client.resources.IResource resource = Minecraft.getMinecraft().getResourceManager()
                            .getResource(versionFace(selected, face))) {
                        if (resource.getInputStream().read() < 0) throw new IOException("Empty panorama");
                    } catch (IOException ex) {
                        LogManager.getLogger().warn("Incomplete panorama {}; using resource-pack panorama", selected, ex);
                        selected = null;
                        break;
                    }
                }
            }
        }
        return selected == null ? new ResourceLocation("textures/gui/title/background/panorama_" + index + ".png")
                : versionFace(selected, index);
    }

    private static ResourceLocation versionFace(String version, int index) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/gui/panorama/" + version + "/panorama_" + index + ".png");
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        checked = null;
        FFDModernMenus.release();
    }
}
