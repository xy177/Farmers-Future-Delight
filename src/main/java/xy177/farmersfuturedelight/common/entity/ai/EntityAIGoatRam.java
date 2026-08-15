package xy177.farmersfuturedelight.common.entity.ai;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityAIGoatRam extends EntityAIBase {
    private static final int APPROACH_TIMEOUT = 160;
    private static final int RAM_TIMEOUT = 200;
    private static final int PATH_REFRESH_INTERVAL = 10;
    private static final double TARGET_SEARCH_RANGE = 16.0D;
    private static final double TEMPT_RANGE = 10.0D;

    private final EntityGoat goat;
    private EntityLivingBase target;
    private BlockPos targetPosition;
    private BlockPos startPosition;
    private Vec3d ramTarget;
    private Vec3d ramDirection = Vec3d.ZERO;
    private Phase phase;
    private int phaseTicks;
    private boolean finished;
    private boolean ramStarted;

    public EntityAIGoatRam(EntityGoat goat) {
        this.goat = goat;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (goat.getRamCooldown() > 0 || !goat.onGround || goat.isInWater()
                || goat.isInLava() || goat.isInLove() || goat.isLongJumping()
                || isTemptedByNearbyPlayer()) {
            return false;
        }

        RamCandidate candidate = findCandidate();
        if (candidate == null) {
            goat.setRamCooldown(goat.failedRamCooldown());
            return false;
        }
        target = candidate.target;
        targetPosition = target.getPosition();
        startPosition = candidate.start;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (finished || target == null || !target.isEntityAlive()) {
            return false;
        }
        if (isTemptedByNearbyPlayer()) {
            return false;
        }
        return phase == Phase.RAM ? phaseTicks < RAM_TIMEOUT : phaseTicks < APPROACH_TIMEOUT;
    }

    private boolean isTemptedByNearbyPlayer() {
        AxisAlignedBB search = goat.getEntityBoundingBox().grow(TEMPT_RANGE);
        for (EntityPlayer player : goat.world.playerEntities) {
            if (!player.isSpectator() && search.intersects(player.getEntityBoundingBox())
                    && (goat.isBreedingItem(player.getHeldItemMainhand())
                            || goat.isBreedingItem(player.getHeldItemOffhand()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        phase = Phase.APPROACH;
        phaseTicks = 0;
        finished = false;
        ramStarted = false;
        goat.setLoweringHead(false);
        moveToStart();
    }

    @Override
    public void updateTask() {
        phaseTicks++;
        if (target == null) {
            finished = true;
            return;
        }

        if (!target.getPosition().equals(targetPosition)) {
            if (!chooseNewStartPosition()) {
                finished = true;
                return;
            }
            goat.setLoweringHead(false);
            phase = Phase.APPROACH;
            phaseTicks = 0;
        }

        goat.getLookHelper().setLookPositionWithEntity(target,
                goat.getHorizontalFaceSpeed(), goat.getVerticalFaceSpeed());
        if (phase == Phase.APPROACH) {
            updateApproach();
        } else if (phase == Phase.PREPARE) {
            updatePreparation();
        } else {
            updateRam();
        }
    }

    private void updateApproach() {
        if (phaseTicks % PATH_REFRESH_INTERVAL == 0 && goat.getNavigator().noPath()) {
            moveToStart();
        }
        double x = startPosition.getX() + 0.5D;
        double y = startPosition.getY();
        double z = startPosition.getZ() + 0.5D;
        if (goat.getDistanceSq(x, y, z) > 1.0D) {
            return;
        }
        goat.getNavigator().clearPath();
        goat.setLoweringHead(true);
        phase = Phase.PREPARE;
        phaseTicks = 0;
    }

    private void updatePreparation() {
        goat.getNavigator().clearPath();
        if (phaseTicks < FFDConfig.goatRamPrepareTicks) {
            return;
        }

        double offsetX = 0.5D * Math.signum(targetPosition.getX() - startPosition.getX());
        double offsetZ = 0.5D * Math.signum(targetPosition.getZ() - startPosition.getZ());
        ramTarget = new Vec3d(targetPosition.getX() + 0.5D + offsetX,
                targetPosition.getY(), targetPosition.getZ() + 0.5D + offsetZ);
        ramDirection = new Vec3d(ramTarget.x - goat.posX, 0.0D,
                ramTarget.z - goat.posZ).normalize();
        goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                goat.getPrepareRamSound(), SoundCategory.NEUTRAL, 1.0F,
                goat.getGoatVoicePitch());
        phase = Phase.RAM;
        phaseTicks = 0;
        ramStarted = true;
        goat.getNavigator().tryMoveToXYZ(ramTarget.x, ramTarget.y, ramTarget.z,
                FFDConfig.goatRamSpeedMultiplier);
    }

    private void updateRam() {
        if (phaseTicks % PATH_REFRESH_INTERVAL == 0 && goat.getNavigator().noPath()) {
            goat.getNavigator().tryMoveToXYZ(ramTarget.x, ramTarget.y, ramTarget.z,
                    FFDConfig.goatRamSpeedMultiplier);
        }
        EntityLivingBase hit = findCollidedTarget();
        if (hit != null) {
            ramEntity(hit);
            finishRam();
            return;
        }

        if (hasHitHornBreakingBlock()) {
            goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                    goat.getRamImpactSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
            if (goat.dropHorn()) {
                goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                        FFDSounds.GOAT_HORN_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
            finishRam();
            return;
        }

        if (goat.getPositionVector().squareDistanceTo(ramTarget) < 0.25D
                || goat.getNavigator().noPath() && phaseTicks > 5) {
            finishRam();
        }
    }

    private void ramEntity(EntityLivingBase hit) {
        float damage = (float) goat.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                .getAttributeValue();
        hit.attackEntityFrom(DamageSource.causeMobDamage(goat), damage);

        int speedLevel = potionLevel(goat.getActivePotionEffect(MobEffects.SPEED));
        int slownessLevel = potionLevel(goat.getActivePotionEffect(MobEffects.SLOWNESS));
        float potionAdjustment = 0.25F * (speedLevel - slownessLevel);
        float movementFactor = MathHelper.clamp(
                (float) Math.sqrt(goat.motionX * goat.motionX + goat.motionZ * goat.motionZ) * 1.65F,
                0.2F, 3.0F) + potionAdjustment;
        float blockFactor = hit.isActiveItemStackBlocking() ? 0.5F : 1.0F;
        float baseKnockback = goat.isChild() ? FFDConfig.goatBabyRamKnockback
                : FFDConfig.goatAdultRamKnockback;
        hit.knockBack(goat, blockFactor * movementFactor * baseKnockback,
                ramDirection.x, ramDirection.z);
        goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                goat.getRamImpactSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    private static int potionLevel(@Nullable PotionEffect effect) {
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }

    @Nullable
    private EntityLivingBase findCollidedTarget() {
        AxisAlignedBB search = goat.getEntityBoundingBox().grow(0.2D);
        List<EntityLivingBase> nearby = goat.world.getEntitiesWithinAABB(
                EntityLivingBase.class, search);
        for (EntityLivingBase candidate : nearby) {
            if (isValidTarget(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean hasHitHornBreakingBlock() {
        Vec3d direction = new Vec3d(goat.motionX, 0.0D, goat.motionZ);
        if (direction.lengthSquared() < 1.0E-5D) {
            direction = ramDirection;
        } else {
            direction = direction.normalize();
        }
        BlockPos facing = new BlockPos(goat.posX + direction.x,
                goat.posY, goat.posZ + direction.z);
        IBlockState lower = goat.world.getBlockState(facing);
        IBlockState upper = goat.world.getBlockState(facing.up());
        return goat.canSnapHornOn(lower) || goat.canSnapHornOn(upper);
    }

    private void finishRam() {
        finished = true;
        goat.getNavigator().clearPath();
    }

    @Override
    public void resetTask() {
        goat.getNavigator().clearPath();
        goat.setLoweringHead(false);
        goat.setRamCooldown(ramStarted ? goat.nextRamCooldown() : goat.failedRamCooldown());
        target = null;
        targetPosition = null;
        startPosition = null;
        ramTarget = null;
        ramDirection = Vec3d.ZERO;
        phase = null;
        phaseTicks = 0;
        finished = false;
        ramStarted = false;
    }

    private void moveToStart() {
        goat.getNavigator().tryMoveToXYZ(startPosition.getX() + 0.5D,
                startPosition.getY(), startPosition.getZ() + 0.5D, 1.25D);
    }

    private boolean chooseNewStartPosition() {
        targetPosition = target.getPosition();
        startPosition = findStartPosition(target);
        return startPosition != null;
    }

    @Nullable
    private RamCandidate findCandidate() {
        List<EntityLivingBase> nearby = goat.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                goat.getEntityBoundingBox().grow(TARGET_SEARCH_RANGE, 4.0D,
                        TARGET_SEARCH_RANGE));
        EntityLivingBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : nearby) {
            if (!isValidTarget(candidate)) {
                continue;
            }
            double distance = goat.getDistanceSq(candidate);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        if (nearest == null) return null;
        BlockPos start = findStartPosition(nearest);
        return start == null ? null : new RamCandidate(nearest, start);
    }

    private boolean isValidTarget(EntityLivingBase candidate) {
        if (candidate == goat || candidate instanceof EntityGoat || !candidate.isEntityAlive()
                || goat.isOnSameTeam(candidate) || !goat.canEntityBeSeen(candidate)) {
            return false;
        }
        if (candidate instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) candidate;
            if (player.isSpectator() || player.capabilities.disableDamage) {
                return false;
            }
        }
        if (candidate instanceof EntityArmorStand
                && !goat.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
        }
        return goat.world.getWorldBorder().contains(new BlockPos(candidate));
    }

    @Nullable
    private BlockPos findStartPosition(EntityLivingBase ramTargetEntity) {
        BlockPos targetPos = ramTargetEntity.getPosition();
        List<BlockPos> possibilities = new java.util.ArrayList<>();
        for (EnumFacing direction : EnumFacing.HORIZONTALS) {
            BlockPos lastWalkable = targetPos;
            int distance = 0;
            for (int step = 1; step <= FFDConfig.goatRamMaxDistance; step++) {
                BlockPos candidate = targetPos.offset(direction, step);
                if (!isWalkable(candidate)) {
                    break;
                }
                lastWalkable = candidate;
                distance = step;
            }
            if (distance >= FFDConfig.goatRamMinDistance) {
                possibilities.add(lastWalkable);
            }
        }
        possibilities.sort(java.util.Comparator.comparingDouble(goat::getDistanceSqToCenter));
        for (BlockPos possibility : possibilities) {
            if (isReachable(possibility)) return possibility;
        }
        return null;
    }

    private boolean isWalkable(BlockPos pos) {
        if (!goat.world.isBlockLoaded(pos)
                || !goat.world.getBlockState(pos.down()).isSideSolid(
                        goat.world, pos.down(), EnumFacing.UP)) {
            return false;
        }
        double width = goat.isChild() ? 0.45D : 0.9D;
        double height = goat.isChild() ? 0.65D : 1.3D;
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        AxisAlignedBB box = new AxisAlignedBB(x - width / 2.0D, pos.getY(),
                z - width / 2.0D, x + width / 2.0D, pos.getY() + height,
                z + width / 2.0D);
        return goat.world.getCollisionBoxes(goat, box).isEmpty();
    }

    private boolean isReachable(BlockPos pos) {
        Path path = goat.getNavigator().getPathToPos(pos);
        if (path == null) {
            return false;
        }
        PathPoint endpoint = path.getFinalPathPoint();
        if (endpoint == null) {
            return false;
        }
        double dx = endpoint.x - pos.getX();
        double dy = endpoint.y - pos.getY();
        double dz = endpoint.z - pos.getZ();
        return dx * dx + dy * dy + dz * dz <= 2.25D;
    }

    private enum Phase {
        APPROACH,
        PREPARE,
        RAM
    }

    private static final class RamCandidate {
        private final EntityLivingBase target;
        private final BlockPos start;

        private RamCandidate(EntityLivingBase target, BlockPos start) {
            this.target = target;
            this.start = start;
        }
    }
}
