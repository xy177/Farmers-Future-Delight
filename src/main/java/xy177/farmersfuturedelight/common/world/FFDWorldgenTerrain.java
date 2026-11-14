package xy177.farmersfuturedelight.common.world;

import net.minecraft.block.state.IBlockState;
import xy177.farmersfuturedelight.common.worldgen.FFDStructureBeard;

public interface FFDWorldgenTerrain {
    int estimateModernSurfaceHeight(int blockX, int blockZ);

    int structureSurfaceHeight(int blockX, int blockZ);

    default int structureFloorHeight(int blockX, int blockZ) { return structureSurfaceHeight(blockX, blockZ); }

    default IBlockState[] structureBaseColumn(int blockX, int blockZ) { return null; }

    int structureSurfaceHeight(int blockX, int blockZ, FFDStructureBeard beard);
}
