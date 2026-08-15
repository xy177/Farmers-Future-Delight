package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.worldgen.MapGenLushCaves;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDAxolotlSpawner {
    private static final int SPAWN_RADIUS_CHUNKS = 8;

    private FFDAxolotlSpawner() {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || !(event.world instanceof WorldServer)) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        if (!FFDEntities.isLocalAxolotlEnabled() || !FFDItems.isLushCaveEnabled()
                || FFDConfig.axolotlMobCap <= 0
                || !world.getGameRules().getBoolean("doMobSpawning")
                || world.provider.getDimension() != 0
                || world.getTotalWorldTime() % FFDConfig.axolotlSpawnCheckIntervalTicks != 0L) {
            return;
        }

        List<Long> eligibleChunks = collectEligibleChunks(world);
        if (eligibleChunks.isEmpty()) {
            return;
        }
        int cap = FFDConfig.axolotlMobCap * eligibleChunks.size() / 289;
        if (cap <= 0) {
            return;
        }
        int count = countAxolotls(world);
        if (count >= cap) {
            return;
        }

        for (EntityPlayer player : world.playerEntities) {
            if (player.isSpectator()) {
                continue;
            }
            for (int attempt = 0; attempt < FFDConfig.axolotlSpawnAttemptsPerPlayer
                    && count < cap; attempt++) {
                BlockPos origin = findSpawnOrigin(world, eligibleChunks);
                if (origin == null || world.getClosestPlayer(origin.getX() + 0.5D,
                        origin.getY() + 0.5D, origin.getZ() + 0.5D, 24.0D, false) != null) {
                    continue;
                }
                count += spawnGroup(world, origin, cap - count);
            }
        }
    }

    private static List<Long> collectEligibleChunks(WorldServer world) {
        Set<Long> seen = new HashSet<>();
        List<Long> chunks = new ArrayList<>();
        ChunkProviderServer provider = world.getChunkProvider();
        for (EntityPlayer player : world.playerEntities) {
            if (player.isSpectator()) {
                continue;
            }
            int centerX = MathHelper.floor(player.posX / 16.0D);
            int centerZ = MathHelper.floor(player.posZ / 16.0D);
            for (int x = centerX - SPAWN_RADIUS_CHUNKS;
                    x <= centerX + SPAWN_RADIUS_CHUNKS; x++) {
                for (int z = centerZ - SPAWN_RADIUS_CHUNKS;
                        z <= centerZ + SPAWN_RADIUS_CHUNKS; z++) {
                    if (provider.getLoadedChunk(x, z) != null) {
                        long packed = ChunkPos.asLong(x, z);
                        if (seen.add(packed)) {
                            chunks.add(packed);
                        }
                    }
                }
            }
        }
        return chunks;
    }

    private static int countAxolotls(WorldServer world) {
        int count = 0;
        for (net.minecraft.entity.Entity entity : world.loadedEntityList) {
            if (entity instanceof EntityAxolotl && entity.isEntityAlive()) {
                count++;
            }
        }
        return count;
    }

    private static BlockPos findSpawnOrigin(WorldServer world, List<Long> eligibleChunks) {
        long packed = eligibleChunks.get(world.rand.nextInt(eligibleChunks.size()));
        int chunkX = (int) packed;
        int chunkZ = (int) (packed >>> 32);
        int x = chunkX * 16 + world.rand.nextInt(16);
        int z = chunkZ * 16 + world.rand.nextInt(16);
        int minY = FFDHeightHooks.isExtended(world)
                ? FFDHeightHooks.minY(world) + 1
                : Math.max(1, FFDConfig.lushCaveMinY);
        int maxY = FFDHeightHooks.isExtended(world)
                ? FFDHeightHooks.maxYExclusive(world) - 2
                : Math.min(254, FFDConfig.lushCaveMaxY);
        if (maxY < minY) {
            return null;
        }
        int range = maxY - minY + 1;
        int start = world.rand.nextInt(range);
        for (int offset = 0; offset < range; offset++) {
            int y = minY + (start + offset) % range;
            BlockPos pos = new BlockPos(x, y, z);
            if (isSpawnWater(world, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static int spawnGroup(WorldServer world, BlockPos origin, int remainingCap) {
        int requested = FFDConfig.axolotlMinGroupSize + world.rand.nextInt(
                FFDConfig.axolotlMaxGroupSize - FFDConfig.axolotlMinGroupSize + 1);
        int targetCount = Math.min(requested, remainingCap);
        int spawned = 0;
        IEntityLivingData groupData = null;
        for (int index = 0; index < targetCount; index++) {
            BlockPos pos = index == 0 ? origin : nearbySpawnWater(world, origin);
            if (pos == null) {
                continue;
            }
            EntityAxolotl axolotl = new EntityAxolotl(world);
            axolotl.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.1D,
                    pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
            Event.Result result = ForgeEventFactory.canEntitySpawn(axolotl, world,
                    (float) axolotl.posX, (float) axolotl.posY, (float) axolotl.posZ, false);
            if (result == Event.Result.DENY || result == Event.Result.DEFAULT
                    && (!axolotl.getCanSpawnHere() || !axolotl.isNotColliding())) {
                continue;
            }
            groupData = axolotl.onInitialSpawn(world.getDifficultyForLocation(pos), groupData);
            if (world.spawnEntity(axolotl)) {
                spawned++;
            }
        }
        return spawned;
    }

    private static BlockPos nearbySpawnWater(WorldServer world, BlockPos origin) {
        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos pos = origin.add(world.rand.nextInt(7) - 3,
                    world.rand.nextInt(3) - 1, world.rand.nextInt(7) - 3);
            if (isSpawnWater(world, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSpawnWater(WorldServer world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER
                && world.getBlockState(pos.down()).getBlock() == Blocks.CLAY
                && MapGenLushCaves.isPositionInLushCave(world, pos);
    }
}
