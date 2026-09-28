package xy177.farmersfuturedelight.proxy;

import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Map;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init() {
    }

    public void spawnGlowParticle(EntityGlowSquid squid) {
    }

    public void spawnGlowInkParticles(EntityGlowSquid squid) {
    }

    public void spawnSquidInkParticles(EntitySquid squid) {
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

    public void spawnConduitParticle(World world, double x, double y, double z,
                                     double motionX, double motionY, double motionZ) {
    }

    public void spawnBubbleColumnParticle(World world, double x, double y, double z,
                                          double motionX, double motionY, double motionZ,
                                          boolean dragDown) {
    }

    public void handleDripstoneParticle(double x, double y, double z, boolean lava) {
    }

    public void handleVerticalBiomes(int dimension, int chunkX, int chunkZ, byte[] biomes) {
    }

    public void handleRiptideState(int entityId, int ticks) {
    }

    public void handleFluidloggedChunk(int dimension, int chunkX, int chunkZ,
                                       Map<Long, String> fluids) {
    }

    public void handleFluidloggedUpdate(int dimension, BlockPos pos, String fluidName) {
    }

    public World getClientWorld() {
        return null;
    }

    public void handleStoredFluidChunk(int dimension, int x, int z,
                                       net.minecraft.nbt.NBTTagCompound data, boolean replace) {}

    public void handleMigrationPrompt(String token, String host, String fluid, boolean convertible) {}

    public AxisAlignedBB getLightSelectionBox(IBlockAccess world, BlockPos pos) {
        return net.minecraft.block.Block.NULL_AABB;
    }

    public String getLanguageCode() {
        return "en_us";
    }
}
