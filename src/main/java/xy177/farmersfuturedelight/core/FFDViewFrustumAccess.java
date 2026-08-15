package xy177.farmersfuturedelight.core;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;

public interface FFDViewFrustumAccess {
    RenderChunk ffd$getRenderChunk(BlockPos pos);
}
