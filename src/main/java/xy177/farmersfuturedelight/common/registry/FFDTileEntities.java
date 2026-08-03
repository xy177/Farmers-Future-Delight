package xy177.farmersfuturedelight.common.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;
import xy177.farmersfuturedelight.common.tile.TileEntityCaveVines;
import xy177.farmersfuturedelight.common.tile.TileEntityNetherVines;

public final class FFDTileEntities {
    private FFDTileEntities() {
    }

    public static void register() {
        if (FFDItems.isHoneyEnabled()) {
            GameRegistry.registerTileEntity(TileEntityBeehive.class,
                    new ResourceLocation(FarmerFutureDelight.MODID, "beehive"));
        }
        if (FFDItems.isGlowBerryEnabled()) {
            GameRegistry.registerTileEntity(TileEntityCaveVines.class,
                    new ResourceLocation(FarmerFutureDelight.MODID, "cave_vines"));
        }
        if (FFDItems.isCrimsonEnabled() || FFDItems.isWarpedEnabled()) {
            GameRegistry.registerTileEntity(TileEntityNetherVines.class,
                    new ResourceLocation(FarmerFutureDelight.MODID, "nether_vines"));
        }
    }
}
