package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class GlowLichenSupport {
    private GlowLichenSupport() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterPopulate(PopulateChunkEvent.Post event) {
        if (!FFDHeightHooks.isExtended(event.getWorld())) {
            validatePopulatedRegion(event.getWorld(), event.getChunkX(), event.getChunkZ());
        }
    }

    public static void validatePopulatedRegion(World world, int chunkX, int chunkZ) {
        if (world.isRemote || world.provider.getDimension() != 0) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = chunkX; cx <= chunkX + 1; cx++) {
            for (int cz = chunkZ; cz <= chunkZ + 1; cz++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
                    if (section == Chunk.NULL_BLOCK_STORAGE || section.isEmpty()) {
                        continue;
                    }
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 0; y < 16; y++) {
                                IBlockState state = section.get(x, y, z);
                                if (BlockGlowLichen.isGlowLichen(state)) {
                                    pos.setPos((cx << 4) + x, section.getYLocation() + y,
                                            (cz << 4) + z);
                                    BlockGlowLichen.refreshSupport(world, pos);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
