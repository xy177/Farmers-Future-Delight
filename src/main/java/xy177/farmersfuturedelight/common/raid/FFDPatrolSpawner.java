package xy177.farmersfuturedelight.common.raid;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIPillagerPatrol;
import xy177.farmersfuturedelight.common.raid.RaiderSpawnConfiguration.Member;
import xy177.farmersfuturedelight.common.raid.RaiderSpawnConfiguration.Patrol;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPatrolSpawner {
    private static final Map<WorldServer, Integer> DELAYS = new WeakHashMap<>();

    private FFDPatrolSpawner() {
    }

    static void clearDelays() {
        DELAYS.clear();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityLiving)
                || event.getEntity() instanceof EntityPillager) return;
        EntityLiving entity = (EntityLiving) event.getEntity();
        if (!EntityAIPillagerPatrol.isPatrolling(entity)) return;
        for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry : entity.tasks.taskEntries) {
            if (entry.action instanceof EntityAIPillagerPatrol) return;
        }
        entity.tasks.addTask(4, new EntityAIPillagerPatrol(entity));
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        Patrol settings = RaiderSpawnConfiguration.patrol();
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)
                || !settings.enabled || settings.members.isEmpty()
                || !settings.dimensions.contains(event.world.provider.getDimension())) return;
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
        DELAYS.put(world, RaiderSpawnConfiguration.between(world.rand, settings.intervalMin, settings.intervalMax));
        if (settings.daytimeOnly && !world.isDaytime()
                || world.rand.nextDouble() >= settings.chance || world.playerEntities.isEmpty()) return;
        EntityPlayer player = world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
        if (player.isSpectator() || RaidVillages.isCloseToVillage(world, player.getPosition(), settings.villageDistance)) return;
        BlockPos column = player.getPosition().add(
                RaiderSpawnConfiguration.between(world.rand, settings.offsetMin, settings.offsetMax) * (world.rand.nextBoolean() ? -1 : 1), 0,
                RaiderSpawnConfiguration.between(world.rand, settings.offsetMin, settings.offsetMax) * (world.rand.nextBoolean() ? -1 : 1));
        int radius = settings.loadedAreaRadius;
        if (!world.isAreaLoaded(column.add(-radius, 0, -radius), column.add(radius, 0, radius))
                || world.getWorldTime() < settings.minimumWorldTime
                || settings.excludedBiomes.contains(world.getBiome(column).getRegistryName())) return;
        BlockPos target = null;
        boolean leaderSpawned = false;
        int difficulty = (int) Math.ceil(world.getDifficultyForLocation(column).getAdditionalDifficulty());
        for (Member entry : settings.members) {
            int count = RaiderSpawnConfiguration.between(world.rand, entry.minCount, entry.maxCount)
                    + (entry.addLocalDifficulty ? difficulty : 0);
            for (int index = 0; index < count; index++) {
                if (!world.isBlockLoaded(column)) continue;
                EntityLiving member = spawnMember(world, world.getTopSolidOrLiquidBlock(column),
                        entry, !leaderSpawned && entry.leader, target, settings);
                if (target == null) {
                    if (member == null) return;
                    target = EntityAIPillagerPatrol.getPatrolTarget(member);
                }
                if (member != null && FFDRaidCaptains.isLeader(member)) leaderSpawned = true;
                column = column.add(world.rand.nextInt(settings.memberSpread + 1) - world.rand.nextInt(settings.memberSpread + 1), 0,
                        world.rand.nextInt(settings.memberSpread + 1) - world.rand.nextInt(settings.memberSpread + 1));
            }
        }
    }

    private static EntityLiving spawnMember(WorldServer world, BlockPos position, Member entry,
                                            boolean leader, BlockPos target, Patrol settings) {
        if (!world.isAreaLoaded(position.add(-1, -1, -1), position.add(1, 2, 1))
                || world.getLightFor(EnumSkyBlock.BLOCK, position) > settings.maxBlockLight
                || !WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                EntityLiving.SpawnPlacementType.ON_GROUND, world, position)
                || isDangerousSpawnBlock(world, position)) return null;
        try {
            net.minecraft.entity.Entity created = entry.entity.newInstance(world);
            if (!(created instanceof EntityLiving)) return null;
            EntityLiving member = (EntityLiving) created;
            leader &= FFDRaidCaptains.canLead(member);
            member.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
            if (!world.getCollisionBoxes(member, member.getEntityBoundingBox()).isEmpty()
                    || !world.checkNoEntityCollision(member.getEntityBoundingBox())
                    || world.containsAnyLiquid(member.getEntityBoundingBox())
                    || ForgeEventFactory.canEntitySpawn(member, world, (float) member.posX,
                    (float) member.posY, (float) member.posZ, false) == Event.Result.DENY) return null;
            FFDRaidCaptains.prepareSpawn(member, "patrol");
            FFDRaidCaptains.setLeader(member, leader);
            member.onInitialSpawn(world.getDifficultyForLocation(position), null);
            if (!entry.nbt.hasNoTags()) {
                NBTTagCompound data = member.writeToNBT(new NBTTagCompound());
                NBTTagCompound overrides = entry.nbt.copy();
                for (String key : java.util.Arrays.asList("id", "Pos", "Motion", "Rotation", "UUIDMost", "UUIDLeast", "Dimension")) {
                    overrides.removeTag(key);
                }
                data.merge(overrides);
                member.readFromNBT(data);
            }
            if (!member.isEntityAlive() || !Float.isFinite(member.getHealth())
                    || !world.getCollisionBoxes(member, member.getEntityBoundingBox()).isEmpty()
                    || !world.checkNoEntityCollision(member.getEntityBoundingBox())
                    || world.containsAnyLiquid(member.getEntityBoundingBox())) return null;
            FFDRaidCaptains.setLeader(member, leader);
            if (target == null) EntityAIPillagerPatrol.findPatrolTarget(member);
            else EntityAIPillagerPatrol.setPatrolTarget(member, target);
            return world.spawnEntity(member) ? member : null;
        } catch (RuntimeException exception) {
            org.apache.logging.log4j.LogManager.getLogger("FFD Raider Spawning")
                    .warn("Could not create patrol entity {}", entry.entity.getRegistryName(), exception);
            return null;
        }
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
