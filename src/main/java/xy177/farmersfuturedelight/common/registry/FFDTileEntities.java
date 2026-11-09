package xy177.farmersfuturedelight.common.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;
import xy177.farmersfuturedelight.common.tile.TileEntityCaveVines;
import xy177.farmersfuturedelight.common.tile.TileEntityConduit;
import xy177.farmersfuturedelight.common.tile.TileEntityNetherVines;

public final class FFDTileEntities {
    private FFDTileEntities() {
    }

    public static void register() {
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityJigsaw.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "jigsaw"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityCampfire.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "campfire"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "cooking_furnace"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityComposter.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "composter"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityBarrel.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "barrel"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityStonecutter.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "stonecutter"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityGrindstone.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "grindstone"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "cartography_table"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityLectern.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "lectern"));
        GameRegistry.registerTileEntity(xy177.farmersfuturedelight.common.tile.TileEntityBell.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "bell"));
        GameRegistry.registerTileEntity(TileEntityBeehive.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "beehive"));
        GameRegistry.registerTileEntity(TileEntityCaveVines.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "cave_vines"));
        GameRegistry.registerTileEntity(TileEntityNetherVines.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "nether_vines"));
        GameRegistry.registerTileEntity(TileEntityConduit.class,
                new ResourceLocation(FarmerFutureDelight.MODID, "conduit"));
    }
}
