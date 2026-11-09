package xy177.farmersfuturedelight.common.registry;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.Event;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.entity.EntityRavager;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public final class FFDVillagerGolems {
    private static final String DETECTED_UNTIL = "farmers_future_delight:golem_detected_until";
    private static final String NEXT_SENSOR = "farmers_future_delight:golem_sensor_at";
    private static final String HOSTILE_UNTIL = "farmers_future_delight:hostile_until";

    private FFDVillagerGolems() {
    }

    public static void tick(EntityVillager villager) {
        long now = villager.world.getTotalWorldTime();
        NBTTagCompound data = villager.getEntityData();
        boolean senseGolems = now >= data.getLong(NEXT_SENSOR);
        if (senseGolems) data.setLong(NEXT_SENSOR, now + 200L);
        boolean hostile = false;
        for (EntityLivingBase nearby : villager.world.getEntitiesWithinAABB(EntityLivingBase.class,
                villager.getEntityBoundingBox().grow(16.0D))) {
            if (!nearby.isEntityAlive()) continue;
            if (senseGolems && nearby instanceof EntityIronGolem) remember(villager, now);
            float range = hostileRange(nearby);
            if (range > 0.0F && villager.getDistanceSq(nearby) <= range * range
                    && villager.canEntityBeSeen(nearby)) hostile = true;
        }
        if (hostile) data.setLong(HOSTILE_UNTIL, now + 40L);
        if (isHostileNearby(villager)) FFDVillagerHomes.wake(villager);
        if (FFDVillagerRoutines.isPanicking(villager) && now % 100L < 20L) summon(villager, 3);
    }

    public static boolean isHostileNearby(EntityVillager villager) {
        return villager.world.getTotalWorldTime() < villager.getEntityData().getLong(HOSTILE_UNTIL);
    }

    private static float hostileRange(EntityLivingBase entity) {
        if (entity instanceof EntityPillager) return 15.0F;
        if (entity instanceof EntityRavager || entity instanceof EntityEvoker
                || entity instanceof EntityIllusionIllager) return 12.0F;
        if (entity instanceof EntityVindicator) return 10.0F;
        if (entity instanceof EntityZombie || entity instanceof EntityDrowned || entity instanceof EntityVex) return 8.0F;
        return 0.0F;
    }

    public static void summon(EntityVillager villager, int required) {
        long now = villager.world.getTotalWorldTime();
        if (!wantsGolem(villager, now)) return;
        List<EntityVillager> nearby = villager.world.getEntitiesWithinAABB(EntityVillager.class,
                villager.getEntityBoundingBox().grow(10.0D));
        int eligible = 0;
        for (EntityVillager candidate : nearby) {
            if (wantsGolem(candidate, now) && ++eligible >= required) break;
        }
        if (eligible < required) return;
        BlockPos origin = villager.getPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int offsetX = villager.getRNG().nextInt(17) - 8;
            int offsetZ = villager.getRNG().nextInt(17) - 8;
            BlockPos above = origin.add(offsetX, 6, offsetZ);
            for (int offsetY = 6; offsetY >= -6; offsetY--) {
                BlockPos floor = above.down();
                if (!villager.world.isBlockLoaded(floor) || !villager.world.getWorldBorder().contains(above)) break;
                IBlockState floorState = villager.world.getBlockState(floor);
                IBlockState aboveState = villager.world.getBlockState(above);
                if (supportsGolem(floorState) && (aboveState.getBlock().isAir(aboveState,
                        villager.world, above) || aboveState.getMaterial().isLiquid())) {
                    EntityIronGolem golem = new EntityIronGolem(villager.world);
                    golem.setLocationAndAngles(above.getX() + 0.5D, above.getY(), above.getZ() + 0.5D,
                            villager.getRNG().nextFloat() * 360.0F, 0.0F);
                    Event.Result permission = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(golem,
                            villager.world, (float) golem.posX, (float) golem.posY, (float) golem.posZ, false);
                    if (permission != Event.Result.DENY && canSpawn(golem, above)
                            && villager.world.spawnEntity(golem)) {
                        for (EntityVillager candidate : nearby) remember(candidate, now);
                        return;
                    }
                    golem.setDead();
                    break;
                }
                above = floor;
            }
        }
    }

    private static boolean canSpawn(EntityIronGolem golem, BlockPos feet) {
        BlockPos floor = feet.down();
        if (!golem.world.isBlockLoaded(feet.up(2))
                || !golem.world.getBlockState(floor).isSideSolid(golem.world, floor, EnumFacing.UP)) return false;
        for (int height = 0; height < 3; height++) {
            BlockPos position = feet.up(height);
            IBlockState state = golem.world.getBlockState(position);
            Block block = state.getBlock();
            IBlockState fluid = WaterloggedBlockApi.getFluidState(golem.world, position);
            if (state.isFullCube() || state.canProvidePower() || block instanceof BlockRailBase
                    || block == Blocks.CACTUS || block == Blocks.FIRE || block == Blocks.LAVA
                    || block == Blocks.FLOWING_LAVA || block == Blocks.MAGMA
                    || block instanceof xy177.farmersfuturedelight.common.block.BlockPowderSnow
                    || block instanceof xy177.farmersfuturedelight.common.block.BlockSweetBerryBush
                    || block == FFDItems.effectiveBlock(FFDBlocks.WITHER_ROSE)
                    || block instanceof xy177.farmersfuturedelight.common.block.BlockCampfire
                    && state.getValue(xy177.farmersfuturedelight.common.block.BlockCampfire.LIT)
                    || fluid != null && (fluid.getMaterial() == net.minecraft.block.material.Material.LAVA
                            || height > 0 && fluid.getMaterial().isLiquid())) return false;
        }
        return golem.world.checkNoEntityCollision(golem.getEntityBoundingBox(), golem);
    }

    private static boolean wantsGolem(EntityVillager villager, long now) {
        NBTTagCompound data = villager.getEntityData();
        long slept = data.getLong(FFDVillagerHomes.LAST_SLEPT_TAG);
        return villager.getClass() == EntityVillager.class && villager.isEntityAlive() && !villager.isAIDisabled()
                && data.hasKey(FFDVillagerHomes.LAST_SLEPT_TAG, 99) && now >= slept && now - slept < 24000L
                && now >= data.getLong(DETECTED_UNTIL);
    }

    private static void remember(EntityVillager villager, long now) {
        villager.getEntityData().setLong(DETECTED_UNTIL, now + 599L);
    }

    private static boolean supportsGolem(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockLeaves || block instanceof BlockStainedGlass
                || block instanceof BlockStainedGlassPane || block == Blocks.WEB || block == Blocks.CACTUS
                || block == Blocks.GLASS_PANE || block == Blocks.GLASS || block == Blocks.ICE
                || block == Blocks.FROSTED_ICE || block == Blocks.TNT || block == Blocks.GLOWSTONE
                || block == Blocks.BEACON || block == Blocks.SEA_LANTERN
                || block instanceof xy177.farmersfuturedelight.common.block.BlockConduit) return false;
        return state.getMaterial().isSolid()
                || block instanceof xy177.farmersfuturedelight.common.block.BlockPowderSnow;
    }

    public static int legacySpawnRoll(int roll, net.minecraft.village.Village village) {
        if (roll != 0 || village.getClass() != net.minecraft.village.Village.class) return roll;
        net.minecraft.world.World world = net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue(
                net.minecraft.village.Village.class, village, "field_75586_a");
        if (world == null || world.isRemote) return roll;
        net.minecraft.util.math.AxisAlignedBB bounds = new net.minecraft.util.math.AxisAlignedBB(village.getCenter())
                .grow(village.getVillageRadius(), 4.0D, village.getVillageRadius());
        List<EntityVillager> residents = world.getEntitiesWithinAABB(EntityVillager.class, bounds);
        if (residents.isEmpty()) return roll;
        for (EntityVillager resident : residents) {
            if (resident.getClass() != EntityVillager.class) return roll;
        }
        return 1;
    }
}
