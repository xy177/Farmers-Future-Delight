package xy177.farmersfuturedelight.proxy;

import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CommonProxy {
    public void preInit() {
    }

    public void init() {
    }

    public void spawnGlowParticle(EntityGlowSquid squid) {
    }

    public void spawnGlowInkParticles(EntityGlowSquid squid) {
    }

    public void spawnSporeBlossomParticle(World world, double x, double y, double z,
                                          boolean ambient) {
    }

    public void spawnHoneyDripParticle(World world, double x, double y, double z) {
    }

    public void spawnFallingNectarParticle(World world, double x, double y, double z) {
    }

    public void spawnDripstoneParticle(World world, double x, double y, double z,
                                       boolean lava) {
    }

    public void spawnSmallFlameParticle(World world, double x, double y, double z) {
    }

    public void handleDripstoneParticle(double x, double y, double z, boolean lava) {
    }

    public void handleVerticalBiomes(int dimension, int chunkX, int chunkZ, byte[] biomes) {
    }

    public AxisAlignedBB getLightSelectionBox(IBlockAccess world, BlockPos pos) {
        return net.minecraft.block.Block.NULL_AABB;
    }

    public String getLanguageCode() {
        return "en_us";
    }
}
