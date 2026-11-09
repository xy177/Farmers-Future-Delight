package xy177.farmersfuturedelight.common.registry;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.BlockStatePaletteRegistry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDVillagerBedIndex implements IWorldEventListener {
    private static final Map<WorldServer, FFDVillagerBedIndex> WORLDS = new WeakHashMap<>();
    private static final Field PALETTE = ObfuscationReflectionHelper.findField(
            BlockStateContainer.class, "field_186022_c");
    private final Map<Long, Set<BlockPos>> chunks = new HashMap<>();

    private FFDVillagerBedIndex() {
    }

    public static Set<BlockPos> getBeds(WorldServer world, Chunk chunk) {
        if (!chunk.isPopulated()) return scan(chunk);
        FFDVillagerBedIndex index = WORLDS.get(world);
        if (index == null) {
            index = new FFDVillagerBedIndex();
            WORLDS.put(world, index);
            world.addEventListener(index);
        }
        return index.chunks.computeIfAbsent(ChunkPos.asLong(chunk.x, chunk.z), ignored -> scan(chunk));
    }

    private static Set<BlockPos> scan(Chunk chunk) {
        Set<BlockPos> beds = new HashSet<>();
        for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
            if (section == null || section.isEmpty() || !mayContainBeds(section.getData())) continue;
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        if (FFDVillagerHomes.isBedHead(section.get(localX, localY, localZ))) {
                            beds.add(new BlockPos(chunk.x * 16 + localX, section.getYLocation() + localY,
                                    chunk.z * 16 + localZ));
                        }
                    }
                }
            }
        }
        return beds;
    }

    private static boolean mayContainBeds(BlockStateContainer states) {
        try {
            IBlockStatePalette palette = (IBlockStatePalette) PALETTE.get(states);
            if (palette instanceof BlockStatePaletteRegistry) return true;
            for (int index = 0; index < 256; index++) {
                IBlockState state = palette.getBlockState(index);
                if (state == null) return false;
                if (state.getBlock() instanceof BlockBed) return true;
            }
            return false;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot inspect bed section palette", exception);
        }
    }

    @Override
    public void notifyBlockUpdate(World world, BlockPos position, IBlockState before, IBlockState after, int flags) {
        if (!(before.getBlock() instanceof BlockBed) && !(after.getBlock() instanceof BlockBed)) return;
        Set<BlockPos> beds = chunks.get(ChunkPos.asLong(position.getX() >> 4, position.getZ() >> 4));
        if (beds == null) return;
        if (FFDVillagerHomes.isBedHead(before)) beds.remove(position);
        if (FFDVillagerHomes.isBedHead(after)) beds.add(position.toImmutable());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        FFDVillagerBedIndex index = WORLDS.get(event.getWorld());
        if (index != null) index.chunks.remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z));
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        FFDVillagerBedIndex index = WORLDS.remove(event.getWorld());
        if (index != null) event.getWorld().removeEventListener(index);
    }

    @Override
    public void notifyLightSet(BlockPos position) {
    }

    @Override
    public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }

    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent sound, SoundCategory category,
                                         double positionX, double positionY, double positionZ,
                                         float volume, float pitch) {
    }

    @Override
    public void playRecord(SoundEvent sound, BlockPos position) {
    }

    @Override
    public void spawnParticle(int particle, boolean ignoreRange, double positionX, double positionY,
                              double positionZ, double speedX, double speedY, double speedZ, int... parameters) {
    }

    @Override
    public void spawnParticle(int particle, boolean ignoreRange, boolean minimiseParticleLevel,
                              double positionX, double positionY, double positionZ, double speedX,
                              double speedY, double speedZ, int... parameters) {
    }

    @Override
    public void onEntityAdded(Entity entity) {
    }

    @Override
    public void onEntityRemoved(Entity entity) {
    }

    @Override
    public void broadcastSound(int sound, BlockPos position, int data) {
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos position, int data) {
    }

    @Override
    public void sendBlockBreakProgress(int breaker, BlockPos position, int progress) {
    }
}
