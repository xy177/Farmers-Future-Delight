package xy177.farmersfuturedelight.common.raid;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Biomes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPatrolSpawner {
    private static final Map<WorldServer, Integer> DELAYS = new WeakHashMap<>();

    private FFDPatrolSpawner() {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)
                || event.world.provider.getDimension() != 0 || !FFDEntities.isLocalPillagerEnabled()) return;
        WorldServer world = (WorldServer) event.world;
        RaidGameRules.ensure(world.getGameRules());
        if (!world.getGameRules().getBoolean(RaidGameRules.PATROLS)
                || !world.getGameRules().getBoolean("doMobSpawning")
                || world.getDifficulty() == EnumDifficulty.PEACEFUL) return;
        int delay = DELAYS.getOrDefault(world, 0) - 1;
        if (delay > 0) {
            DELAYS.put(world, delay);
            return;
        }
        DELAYS.put(world, delay + 12000 + world.rand.nextInt(1200));
        if (!world.isDaytime() || world.rand.nextInt(5) != 0 || world.playerEntities.isEmpty()) return;
        EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
        if (player.isSpectator() || RaidVillages.isCloseToVillage(world, player.getPosition(), 2)) return;
        BlockPos column = player.getPosition().add(
                (24 + world.rand.nextInt(24)) * (world.rand.nextBoolean() ? -1 : 1), 0,
                (24 + world.rand.nextInt(24)) * (world.rand.nextBoolean() ? -1 : 1));
        if (!world.isAreaLoaded(column.add(-10, 0, -10), column.add(10, 0, 10))
                || world.getWorldTime() < 120000L
                || world.getBiome(column) == Biomes.MUSHROOM_ISLAND
                || world.getBiome(column) == Biomes.MUSHROOM_ISLAND_SHORE) return;
        int count = (int) Math.ceil(world.getDifficultyForLocation(column).getAdditionalDifficulty()) + 1;
        BlockPos target = null;
        for (int index = 0; index < count; index++) {
            if (!world.isBlockLoaded(column)) continue;
            BlockPos position = world.getTopSolidOrLiquidBlock(column);
            EntityPillager member = spawnMember(world, position, index == 0, target);
            if (index == 0) {
                if (member == null) break;
                target = member.getPatrolTarget();
            }
            column = column.add(world.rand.nextInt(5) - world.rand.nextInt(5), 0,
                    world.rand.nextInt(5) - world.rand.nextInt(5));
        }
    }

    private static EntityPillager spawnMember(WorldServer world, BlockPos position,
                                             boolean leader, BlockPos target) {
        if (!world.isAreaLoaded(position.add(-1, -1, -1), position.add(1, 2, 1))
                || world.getLightFor(EnumSkyBlock.BLOCK, position) > 8
                || !WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                EntityLiving.SpawnPlacementType.ON_GROUND, world, position)
                || isDangerousSpawnBlock(world, position)) return null;
        EntityPillager member = new EntityPillager(world);
        member.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
        if (!world.getCollisionBoxes(member, member.getEntityBoundingBox()).isEmpty()
                || !world.checkNoEntityCollision(member.getEntityBoundingBox())
                || world.containsAnyLiquid(member.getEntityBoundingBox())
                || ForgeEventFactory.canEntitySpawn(member, world, (float) member.posX,
                (float) member.posY, (float) member.posZ, false) == Event.Result.DENY) return null;
        FFDRaidCaptains.prepareSpawn(member, "patrol");
        FFDRaidCaptains.setLeader(member, leader);
        member.onInitialSpawn(world.getDifficultyForLocation(position), null);
        if (leader) member.findPatrolTarget();
        else if (target != null) member.setPatrolTarget(target);
        return world.spawnEntity(member) ? member : null;
    }

    private static boolean isDangerousSpawnBlock(WorldServer world, BlockPos position) {
        IBlockState state = world.getBlockState(position);
        PathNodeType pathNodeType = state.getBlock().getAiPathNodeType(state, world, position);
        return state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.CACTUS
                || pathNodeType == PathNodeType.DAMAGE_FIRE
                || pathNodeType == PathNodeType.DAMAGE_CACTUS
                || pathNodeType == PathNodeType.DAMAGE_OTHER;
    }
}
