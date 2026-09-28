package xy177.farmersfuturedelight.core;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDSquidBehaviorHooks {
    private FFDSquidBehaviorHooks() {
    }

    public static void register(EntitySquid squid) {
        if (squid.getClass() == EntitySquid.class || squid.getClass() == EntityGlowSquid.class) {
            squid.tasks.addTask(1, new Flee(squid));
        }
    }

    public static boolean shouldSkipInterpolatedTravel(EntitySquid squid, int remainingTicks) {
        return squid.world.isRemote && remainingTicks > 0 && !squid.canPassengerSteer()
                && (squid.getClass() == EntitySquid.class
                        || squid.getClass() == EntityGlowSquid.class);
    }

    public static boolean canSpawn(EntitySquid squid) {
        if (squid.getClass() != EntitySquid.class) {
            return squid.posY > 45.0D && squid.posY < squid.world.getSeaLevel();
        }
        World world = squid.world;
        BlockPos pos = new BlockPos(squid);
        int seaLevel = world.getSeaLevel();
        return pos.getY() >= seaLevel - 13 && pos.getY() <= seaLevel
                && world.getBlockState(pos).getMaterial() == Material.WATER
                && world.getBlockState(pos.down()).getMaterial() == Material.WATER
                && (world.getBlockState(pos.up()).getBlock() == Blocks.WATER
                        || world.getBlockState(pos.up()).getBlock() == Blocks.FLOWING_WATER);
    }

    public static boolean hurt(boolean hurt, EntitySquid squid) {
        if (hurt && !squid.world.isRemote && squid.getRevengeTarget() != null
                && (squid.getClass() == EntitySquid.class
                        || squid.getClass() == EntityGlowSquid.class)) {
            squid.playSound(squid instanceof EntityGlowSquid
                    ? FFDSounds.GLOW_SQUID_SQUIRT : FFDSounds.SQUID_SQUIRT, 0.4F, 1.0F);
            squid.world.setEntityState(squid, (byte) 20);
        }
        return hurt;
    }

    public static boolean handleInk(EntitySquid squid, byte status) {
        if (status != 20 || squid.getClass() != EntitySquid.class) {
            return false;
        }
        FarmerFutureDelight.proxy.spawnSquidInkParticles(squid);
        return true;
    }

    private static final class Flee extends EntityAIBase {
        private final EntitySquid squid;
        private int fleeTicks;

        private Flee(EntitySquid squid) {
            this.squid = squid;
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase attacker = squid.getRevengeTarget();
            return squid.isInWater() && attacker != null && squid.getDistanceSq(attacker) < 100.0D;
        }

        @Override
        public void startExecuting() {
            fleeTicks = 0;
        }

        @Override
        public void resetTask() {
            fleeTicks = 0;
        }

        @Override
        public void updateTask() {
            fleeTicks++;
            EntityLivingBase attacker = squid.getRevengeTarget();
            if (attacker == null) {
                return;
            }
            Vec3d away = new Vec3d(squid.posX - attacker.posX,
                    squid.posY - attacker.posY, squid.posZ - attacker.posZ);
            BlockPos targetPos = new BlockPos(squid.posX + away.x,
                    squid.posY + away.y, squid.posZ + away.z);
            if (squid.world.getBlockState(targetPos).getMaterial() == Material.WATER
                    || squid.world.isAirBlock(targetPos)) {
                double distance = away.lengthVector();
                if (distance > 0.0D) {
                    double speed = 3.0D;
                    if (distance > 5.0D) {
                        speed -= (distance - 5.0D) / 5.0D;
                    }
                    if (speed > 0.0D) {
                        away = away.scale(speed);
                    }
                }
                if (squid.world.isAirBlock(targetPos)) {
                    away = new Vec3d(away.x, 0.0D, away.z);
                }
                squid.setMovementVector((float) (away.x / 20.0D),
                        (float) (away.y / 20.0D), (float) (away.z / 20.0D));
            }
            if (fleeTicks % 10 == 5 && squid.world instanceof WorldServer) {
                ((WorldServer) squid.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                        squid.posX, squid.posY, squid.posZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
