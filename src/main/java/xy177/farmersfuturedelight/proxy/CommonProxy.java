package xy177.farmersfuturedelight.proxy;

import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityRavager;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Map;

public class CommonProxy {
    public boolean isRedstoneDot(BlockPos pos) {
        return false;
    }

    public void handleRedstoneDots(int dimension, int chunkX, int chunkZ, long[] positions) {
    }

    public void handleAgeLockState(int dimension, int entityId, boolean locked, int timer) {
    }

    public void spawnAgeLockParticle(net.minecraft.entity.EntityLivingBase entity, boolean locked) {
    }

    public void handleCookingBook(net.minecraft.nbt.NBTTagCompound data) {
    }

    public void handleMapDecorations(String mapName, net.minecraft.nbt.NBTTagCompound decorations) {
    }

    public void openJigsaw(xy177.farmersfuturedelight.common.tile.TileEntityJigsaw tile) {
    }

    public void handleVillagerState(int dimension, int entityId,
                                    net.minecraft.nbt.NBTTagCompound state) {
    }

    public void handleMerchantState(xy177.farmersfuturedelight.common.network.PacketMerchantState state) {
    }

    public Object getWorkstationGui(int id, net.minecraft.entity.player.EntityPlayer player,
                                    World world, BlockPos pos) {
        return null;
    }

    public boolean isHoldingScaffolding() {
        return false;
    }

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

    public void spawnSoulFlameParticle(World world, double x, double y, double z) {
    }

    public void spawnCryingObsidianParticle(World world, double x, double y, double z) {
    }

    public void spawnReversePortalParticle(World world, double x, double y, double z, double speed) {
    }

    public void spawnCampfireSmoke(World world, double x, double y, double z,
                                   boolean signalFire) {
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

    public void handleVerticalBiomes(int dimension, int chunkX, int chunkZ,
                                    xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData biomes) {
    }

    public void handleRiptideState(int entityId, int ticks) {
    }

    public void handleImmediateRespawnRule(boolean enabled) {
    }

    public void handleRavagerRoar(EntityRavager ravager) {
    }

    public void handleDrownedConverting(int dimension, int entityId, boolean converting) {
    }

    public void handleMooshroomState(int dimension, int entityId, boolean brown) {
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

    public void spawnSoulParticle(net.minecraft.entity.EntityLivingBase entity) { }
}
