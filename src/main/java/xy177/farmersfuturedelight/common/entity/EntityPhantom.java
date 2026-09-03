package xy177.farmersfuturedelight.common.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityBodyHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityPhantom extends EntityMob {
    private static final DataParameter<Integer> SIZE = EntityDataManager.createKey(
            EntityPhantom.class, DataSerializers.VARINT);
    private static final float BASE_WIDTH = 0.9F;
    private static final float BASE_HEIGHT = 0.5F;
    private static final float FLAP_DEGREES_PER_TICK = 7.448451F;
    private static final int TICKS_PER_FLAP = MathHelper.ceil(24.166098F);

    private Vec3d moveTargetPoint = Vec3d.ZERO;
    @Nullable
    private BlockPos anchorPoint;
    private AttackPhase attackPhase = AttackPhase.CIRCLE;

    public EntityPhantom(World world) {
        super(world);
        setSize(BASE_WIDTH, BASE_HEIGHT);
        setNoGravity(true);
        experienceValue = 5;
        moveHelper = new PhantomMoveHelper(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(SIZE, 0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new PhantomAttackStrategyGoal(this));
        tasks.addTask(2, new PhantomSweepAttackGoal(this));
        tasks.addTask(3, new PhantomCircleAroundAnchorGoal(this));
        targetTasks.addTask(1, new PhantomAttackPlayerTargetGoal(this));
    }

    @Override
    protected EntityBodyHelper createBodyHelper() {
        return new PhantomBodyHelper(this);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        anchorPoint = getPosition().up(5);
        setPhantomSize(0);
        return super.onInitialSpawn(difficulty, livingData);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote) {
            burnInDaylight();
            return;
        }

        float animation = MathHelper.cos((getUniqueFlapTickOffset() + ticksExisted)
                * FLAP_DEGREES_PER_TICK * 0.017453292F + (float) Math.PI);
        float nextAnimation = MathHelper.cos((getUniqueFlapTickOffset() + ticksExisted + 1)
                * FLAP_DEGREES_PER_TICK * 0.017453292F + (float) Math.PI);
        if (animation > 0.0F && nextAnimation <= 0.0F) {
            world.playSound(posX, posY, posZ, FFDSounds.PHANTOM_FLAP, getSoundCategory(),
                    0.95F + rand.nextFloat() * 0.05F,
                    0.95F + rand.nextFloat() * 0.05F, false);
        }

        float width = this.width * 1.48F;
        float xOffset = MathHelper.cos(rotationYaw * 0.017453292F) * width;
        float zOffset = MathHelper.sin(rotationYaw * 0.017453292F) * width;
        float yOffset = (0.3F + animation * 0.45F) * height * 2.5F;
        world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                posX + xOffset, posY + yOffset, posZ + zOffset, 0.0D, 0.0D, 0.0D);
        world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                posX - xOffset, posY + yOffset, posZ - zOffset, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isInWater()) {
            moveRelative(strafe, vertical, forward, 0.02F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.8D;
            motionY *= 0.8D;
            motionZ *= 0.8D;
        } else if (isInLava()) {
            moveRelative(strafe, vertical, forward, 0.02F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        } else {
            moveRelative(strafe, vertical, forward, 0.2F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.91D;
            motionY *= 0.91D;
            motionZ *= 0.91D;
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean isOnLadder() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.PHANTOM_DEATH;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        if (!wasRecentlyHit || !xy177.farmersfuturedelight.common.registry.FFDEntities.isPhantomEnabled()) {
            return;
        }
        int count = rand.nextInt(2) + rand.nextInt(lootingModifier + 1);
        if (count > 0) {
            entityDropItem(new ItemStack(FFDItems.PHANTOM_MEMBRANE, count), 0.0F);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (anchorPoint != null) {
            compound.setInteger("AX", anchorPoint.getX());
            compound.setInteger("AY", anchorPoint.getY());
            compound.setInteger("AZ", anchorPoint.getZ());
        }
        compound.setInteger("Size", getPhantomSize());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("AX", 3) && compound.hasKey("AY", 3)
                && compound.hasKey("AZ", 3)) {
            anchorPoint = new BlockPos(compound.getInteger("AX"), compound.getInteger("AY"),
                    compound.getInteger("AZ"));
        } else {
            anchorPoint = null;
        }
        setPhantomSize(compound.getInteger("Size"));
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (SIZE.equals(key)) {
            updatePhantomSizeInfo();
        }
        super.notifyDataManagerChange(key);
    }

    public int getPhantomSize() {
        return dataManager.get(SIZE);
    }

    public void setPhantomSize(int size) {
        dataManager.set(SIZE, MathHelper.clamp(size, 0, 64));
        updatePhantomSizeInfo();
    }

    public int getUniqueFlapTickOffset() {
        return getEntityId() * 3;
    }

    public float getRenderPitch(float partialTicks) {
        float pitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
        if (Math.abs(pitch) > 0.01F) {
            return MathHelper.clamp(pitch, -90.0F, 90.0F);
        }
        double movementX = posX - prevPosX;
        double movementY = posY - prevPosY;
        double movementZ = posZ - prevPosZ;
        double horizontal = Math.sqrt(movementX * movementX + movementZ * movementZ);
        if (horizontal <= 1.0E-5D || Math.abs(movementY) <= 1.0E-5D) {
            return pitch;
        }
        return MathHelper.clamp((float) (MathHelper.atan2(movementY, horizontal)
                * 57.2957763671875D), -90.0F, 90.0F);
    }

    private void updatePhantomSizeInfo() {
        float scale = 1.0F + 0.15F * getPhantomSize();
        setSize(BASE_WIDTH * scale, BASE_HEIGHT * scale);
        if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                    .setBaseValue(6.0D + getPhantomSize());
        }
    }

    private void burnInDaylight() {
        if (!isEntityAlive() || !world.provider.hasSkyLight() || !world.isDaytime()) {
            return;
        }
        float brightness = getBrightness();
        BlockPos eyePos = new BlockPos(posX, posY + getEyeHeight(), posZ);
        if (brightness <= 0.5F || rand.nextFloat() * 30.0F >= (brightness - 0.4F) * 2.0F
                || isWet() || !world.canSeeSky(eyePos)) {
            return;
        }

        ItemStack head = getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.isEmpty()) {
            setFire(8);
            return;
        }
        if (head.isItemStackDamageable()) {
            head.setItemDamage(head.getItemDamage() + rand.nextInt(2));
            if (head.getItemDamage() >= head.getMaxDamage()) {
                renderBrokenItemStack(head);
                setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }
    }

    private boolean canAttackTarget(EntityLivingBase target, double range) {
        if (target == null || target == this || !target.isEntityAlive()
                || isOnSameTeam(target) || !canEntityBeSeen(target)) {
            return false;
        }
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            if (player.isSpectator() || player.isCreative()) {
                return false;
            }
        }
        return range < 0.0D || getDistanceSq(target) <= range * range;
    }

    private enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    private static final class PhantomMoveHelper extends EntityMoveHelper {
        private final EntityPhantom phantom;
        private float speed = 0.1F;

        private PhantomMoveHelper(EntityPhantom phantom) {
            super(phantom);
            this.phantom = phantom;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (phantom.collidedHorizontally) {
                phantom.rotationYaw += 180.0F;
                speed = 0.1F;
            }

            double dx = phantom.moveTargetPoint.x - phantom.posX;
            double dy = phantom.moveTargetPoint.y - phantom.posY;
            double dz = phantom.moveTargetPoint.z - phantom.posZ;
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(horizontalDistance) <= 1.0E-5F) {
                return;
            }

            double verticalScale = 1.0D - Math.abs(dy * 0.7D) / horizontalDistance;
            dx *= verticalScale;
            dz *= verticalScale;
            horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            double distance = Math.sqrt(dx * dx + dz * dz + dy * dy);
            if (horizontalDistance <= 1.0E-5F || distance <= 1.0E-5F) {
                return;
            }

            float previousYaw = phantom.rotationYaw;
            float targetAngle = (float) MathHelper.atan2(dz, dx);
            float current = MathHelper.wrapDegrees(phantom.rotationYaw + 90.0F);
            float target = MathHelper.wrapDegrees(targetAngle * 57.295776F);
            phantom.rotationYaw = approachDegrees(current, target, 4.0F) - 90.0F;
            phantom.renderYawOffset = phantom.rotationYaw;

            if (Math.abs(MathHelper.wrapDegrees(previousYaw - phantom.rotationYaw)) < 3.0F) {
                speed = approach(speed, 1.8F, 0.005F * (1.8F / speed));
            } else {
                speed = approach(speed, 0.2F, 0.025F);
            }

            float pitch = (float) (-(MathHelper.atan2(-dy, horizontalDistance) * 57.295776D));
            phantom.rotationPitch = pitch;
            float moveAngle = phantom.rotationYaw + 90.0F;
            double targetX = speed * MathHelper.cos(moveAngle * 0.017453292F)
                    * Math.abs(dx / distance);
            double targetZ = speed * MathHelper.sin(moveAngle * 0.017453292F)
                    * Math.abs(dz / distance);
            double targetY = speed * MathHelper.sin(pitch * 0.017453292F)
                    * Math.abs(dy / distance);
            phantom.motionX += (targetX - phantom.motionX) * 0.2D;
            phantom.motionY += (targetY - phantom.motionY) * 0.2D;
            phantom.motionZ += (targetZ - phantom.motionZ) * 0.2D;
        }

        private static float approach(float current, float target, float step) {
            if (current < target) {
                return Math.min(current + step, target);
            }
            return Math.max(current - step, target);
        }

        private static float approachDegrees(float current, float target, float step) {
            return approach(current, current + MathHelper.wrapDegrees(target - current), step);
        }
    }

    private static final class PhantomBodyHelper extends EntityBodyHelper {
        private final EntityPhantom phantom;

        private PhantomBodyHelper(EntityPhantom phantom) {
            super(phantom);
            this.phantom = phantom;
        }

        @Override
        public void updateRenderAngles() {
            phantom.rotationYawHead = phantom.renderYawOffset;
            phantom.renderYawOffset = phantom.rotationYaw;
        }
    }

    private abstract static class PhantomMoveTargetGoal extends EntityAIBase {
        protected final EntityPhantom phantom;

        private PhantomMoveTargetGoal(EntityPhantom phantom) {
            this.phantom = phantom;
            setMutexBits(1);
        }

        protected boolean touchingTarget() {
            return phantom.moveTargetPoint.squareDistanceTo(phantom.posX, phantom.posY, phantom.posZ)
                    < 4.0D;
        }
    }

    private static final class PhantomAttackStrategyGoal extends EntityAIBase {
        private final EntityPhantom phantom;
        private int nextSweepTick;

        private PhantomAttackStrategyGoal(EntityPhantom phantom) {
            this.phantom = phantom;
        }

        @Override
        public boolean shouldExecute() {
            return phantom.canAttackTarget(phantom.getAttackTarget(), -1.0D);
        }

        @Override
        public void startExecuting() {
            nextSweepTick = 10;
            phantom.attackPhase = AttackPhase.CIRCLE;
            setAnchorAboveTarget();
        }

        @Override
        public void resetTask() {
            if (phantom.anchorPoint != null) {
                BlockPos top = phantom.world.getHeight(phantom.anchorPoint);
                phantom.anchorPoint = top.up(10 + phantom.rand.nextInt(20));
            }
        }

        @Override
        public void updateTask() {
            if (phantom.attackPhase != AttackPhase.CIRCLE) {
                return;
            }
            nextSweepTick--;
            if (nextSweepTick <= 0) {
                phantom.attackPhase = AttackPhase.SWOOP;
                setAnchorAboveTarget();
                nextSweepTick = (8 + phantom.rand.nextInt(4)) * 20;
                phantom.playSound(FFDSounds.PHANTOM_SWOOP, 10.0F,
                        0.95F + phantom.rand.nextFloat() * 0.1F);
            }
        }

        private void setAnchorAboveTarget() {
            EntityLivingBase target = phantom.getAttackTarget();
            if (target == null || phantom.anchorPoint == null) {
                return;
            }
            BlockPos anchor = target.getPosition().up(20 + phantom.rand.nextInt(20));
            if (anchor.getY() < phantom.world.getSeaLevel()) {
                anchor = new BlockPos(anchor.getX(), phantom.world.getSeaLevel() + 1, anchor.getZ());
            }
            phantom.anchorPoint = anchor;
        }
    }

    private static final class PhantomSweepAttackGoal extends PhantomMoveTargetGoal {
        private boolean scaredOfCat;
        private int catSearchTick;

        private PhantomSweepAttackGoal(EntityPhantom phantom) {
            super(phantom);
        }

        @Override
        public boolean shouldExecute() {
            return phantom.getAttackTarget() != null && phantom.attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = phantom.getAttackTarget();
            if (!phantom.canAttackTarget(target, -1.0D) || !shouldExecute()) {
                return false;
            }
            if (phantom.ticksExisted > catSearchTick) {
                catSearchTick = phantom.ticksExisted + 20;
                List<EntityOcelot> cats = phantom.world.getEntitiesWithinAABB(EntityOcelot.class,
                        phantom.getEntityBoundingBox().grow(16.0D),
                        new Predicate<EntityOcelot>() {
                            @Override
                            public boolean apply(@Nullable EntityOcelot cat) {
                                return cat != null && cat.isEntityAlive();
                            }
                        });
                for (EntityOcelot cat : cats) {
                    cat.playSound(SoundEvents.ENTITY_CAT_HISS, 1.0F, 1.0F);
                }
                scaredOfCat = !cats.isEmpty();
            }
            return !scaredOfCat;
        }

        @Override
        public void resetTask() {
            phantom.setAttackTarget(null);
            phantom.attackPhase = AttackPhase.CIRCLE;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = phantom.getAttackTarget();
            if (target == null) {
                return;
            }
            phantom.moveTargetPoint = new Vec3d(target.posX,
                    target.getEntityBoundingBox().minY + target.height * 0.5D, target.posZ);
            if (phantom.getEntityBoundingBox().grow(0.2D)
                    .intersects(target.getEntityBoundingBox())) {
                if (phantom.attackEntityAsMob(target)) {
                    phantom.playSound(FFDSounds.PHANTOM_BITE, 1.0F,
                            0.95F + phantom.rand.nextFloat() * 0.1F);
                }
                phantom.attackPhase = AttackPhase.CIRCLE;
            } else if (phantom.collidedHorizontally || phantom.hurtTime > 0) {
                phantom.attackPhase = AttackPhase.CIRCLE;
            }
        }
    }

    private static final class PhantomCircleAroundAnchorGoal extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float heightOffset;
        private float clockwise;

        private PhantomCircleAroundAnchorGoal(EntityPhantom phantom) {
            super(phantom);
        }

        @Override
        public boolean shouldExecute() {
            return phantom.getAttackTarget() == null || phantom.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void startExecuting() {
            distance = 5.0F + phantom.rand.nextFloat() * 10.0F;
            heightOffset = -4.0F + phantom.rand.nextFloat() * 9.0F;
            clockwise = phantom.rand.nextBoolean() ? 1.0F : -1.0F;
            selectNext();
        }

        @Override
        public void updateTask() {
            if (phantom.rand.nextInt(350) == 0) {
                heightOffset = -4.0F + phantom.rand.nextFloat() * 9.0F;
            }
            if (phantom.rand.nextInt(250) == 0) {
                distance += 1.0F;
                if (distance > 15.0F) {
                    distance = 5.0F;
                    clockwise = -clockwise;
                }
            }
            if (phantom.rand.nextInt(450) == 0) {
                angle = phantom.rand.nextFloat() * ((float) Math.PI * 2.0F);
                selectNext();
            }
            if (touchingTarget()) {
                selectNext();
            }
            if (phantom.moveTargetPoint.y < phantom.posY
                    && !phantom.world.isAirBlock(phantom.getPosition().down())) {
                heightOffset = Math.max(1.0F, heightOffset);
                selectNext();
            }
            if (phantom.moveTargetPoint.y > phantom.posY
                    && !phantom.world.isAirBlock(phantom.getPosition().up())) {
                heightOffset = Math.min(-1.0F, heightOffset);
                selectNext();
            }
        }

        private void selectNext() {
            if (phantom.anchorPoint == null) {
                phantom.anchorPoint = phantom.getPosition();
            }
            angle += clockwise * 15.0F * 0.017453292F;
            phantom.moveTargetPoint = new Vec3d(phantom.anchorPoint).addVector(
                    distance * MathHelper.cos(angle), -4.0F + heightOffset,
                    distance * MathHelper.sin(angle));
        }
    }

    private static final class PhantomAttackPlayerTargetGoal extends EntityAIBase {
        private final EntityPhantom phantom;
        private int nextScanTick = 20;

        private PhantomAttackPlayerTargetGoal(EntityPhantom phantom) {
            this.phantom = phantom;
        }

        @Override
        public boolean shouldExecute() {
            if (nextScanTick > 0) {
                nextScanTick--;
                return false;
            }
            nextScanTick = 60;
            AxisAlignedBB searchBox = phantom.getEntityBoundingBox().grow(16.0D, 64.0D, 16.0D);
            List<EntityPlayer> players = phantom.world.getEntitiesWithinAABB(EntityPlayer.class,
                    searchBox, new Predicate<EntityPlayer>() {
                        @Override
                        public boolean apply(@Nullable EntityPlayer player) {
                            return player != null && phantom.canAttackTarget(player, 64.0D);
                        }
                    });
            if (players.isEmpty()) {
                return false;
            }
            Collections.sort(players, new Comparator<EntityPlayer>() {
                @Override
                public int compare(EntityPlayer first, EntityPlayer second) {
                    return Double.compare(second.posY, first.posY);
                }
            });
            phantom.setAttackTarget(players.get(0));
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return phantom.canAttackTarget(phantom.getAttackTarget(), -1.0D);
        }
    }
}
