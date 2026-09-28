package xy177.farmersfuturedelight.common.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDPotions;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.FFDOceanStructureLocator;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;

public class EntityDolphin extends EntityAgeable implements IAnimals {
    private static final int MAX_AIR = 4800;
    private static final DataParameter<BlockPos> TREASURE_POS = EntityDataManager.createKey(
            EntityDolphin.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> GOT_FISH = EntityDataManager.createKey(
            EntityDolphin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MOISTNESS = EntityDataManager.createKey(
            EntityDolphin.class, DataSerializers.VARINT);
    private int playCooldown;

    public EntityDolphin(World world) {
        super(world);
        setSize(0.9F, 0.6F);
        setPathPriority(PathNodeType.WATER, 0.0F);
        moveHelper = new DolphinMoveHelper(this);
        ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, this,
                new DolphinLookHelper(this), "field_70749_g");
        setCanPickUpLoot(true);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityDolphin(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(TREASURE_POS, BlockPos.ORIGIN);
        dataManager.register(GOT_FISH, false);
        dataManager.register(MOISTNESS, 2400);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.2D);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new DolphinBreathGoal(this));
        tasks.addTask(0, new DolphinFindWaterGoal(this));
        tasks.addTask(1, new DolphinTreasureGoal(this));
        tasks.addTask(2, new DolphinSwimWithPlayerGoal(this, 4.0D));
        tasks.addTask(4, new DolphinSwimGoal(this, 1.0D, 10));
        tasks.addTask(4, new EntityAILookIdle(this));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(5, new DolphinJumpGoal(this, 10));
        tasks.addTask(6, new EntityAIAttackMelee(this, 1.2D, true));
        tasks.addTask(8, new DolphinPlayGoal(this));
        tasks.addTask(8, new DolphinFollowBoatGoal(this));
        tasks.addTask(9, new EntityAIAvoidEntity<>(this, EntityGuardian.class,
                8.0F, 1.0D, 1.0D));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityGuardian.class));
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        return new PathNavigateSwimmer(this, world);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isServerWorld() && isInWater()) {
            moveRelative(strafe, vertical, forward, getAIMoveSpeed());
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.9D;
            motionY *= 0.9D;
            motionZ *= 0.9D;
            if (getAttackTarget() == null) {
                motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!isInsideOfMaterial(Material.WATER)) {
            setAir(MAX_AIR);
        }
        if (isWet()) {
            setMoistness(2400);
        } else {
            setMoistness(getMoistness() - 1);
            if (getMoistness() <= 0) {
                attackEntityFrom(DamageSource.DROWN, 1.0F);
            }
            if (onGround) {
                motionY += 0.5D;
                motionX += (rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                motionZ += (rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                rotationYaw = rand.nextFloat() * 360.0F;
                onGround = false;
                isAirBorne = true;
            }
        }
        if (world.isRemote && isInWater()
                && motionX * motionX + motionY * motionY + motionZ * motionZ > 0.03D) {
            Vec3d look = getLook(0.0F);
            float sideX = MathHelper.cos(rotationYaw * 0.017453292F) * 0.3F;
            float sideZ = MathHelper.sin(rotationYaw * 0.017453292F) * 0.3F;
            float back = 1.2F - rand.nextFloat() * 0.7F;
            for (int i = 0; i < 2; i++) {
                world.spawnParticle(EnumParticleTypes.WATER_WAKE,
                        posX - look.x * back + sideX, posY - look.y,
                        posZ - look.z * back + sideZ, 0.0D, 0.0D, 0.0D);
                world.spawnParticle(EnumParticleTypes.WATER_WAKE,
                        posX - look.x * back - sideX, posY - look.y,
                        posZ - look.z * back - sideZ, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (isChild()) {
            return false;
        }
        boolean attacked = entity.attackEntityFrom(DamageSource.causeMobDamage(this),
                (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (attacked) {
            applyEnchantments(this, entity);
            playSound(FFDSounds.DOLPHIN_ATTACK, 1.0F, 1.0F);
        }
        return attacked;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (isFish(held)) {
            playSound(FFDSounds.DOLPHIN_EAT, 1.0F, 1.0F);
            if (isChild()) {
                ageUp((int) ((float) (-getGrowingAge() / 20) * 0.1F), true);
            } else {
                setGotFish(true);
            }
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return FFDEntities.isLocalDolphinEnabled() && pos.getY() > 45
                && pos.getY() < world.getSeaLevel()
                && world.getBlockState(pos).getMaterial() == Material.WATER
                && world.getBlockState(pos.up()).getMaterial() == Material.WATER
                && isNotColliding();
    }

    @Override
    public boolean isNotColliding() {
        return world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        DolphinGroupData groupData = livingData instanceof DolphinGroupData
                ? (DolphinGroupData) livingData : new DolphinGroupData();
        setAir(MAX_AIR);
        rotationPitch = 0.0F;
        if (groupData.groupSize > 0 && rand.nextFloat() <= 0.1F) {
            setGrowingAge(-24000);
        }
        groupData.groupSize++;
        return super.onInitialSpawn(difficulty, groupData);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int amount = rand.nextInt(2) + rand.nextInt(lootingModifier + 1);
        if (amount > 0) {
            entityDropItem(new ItemStack(Items.FISH, amount, 0), 0.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWater() ? FFDSounds.DOLPHIN_AMBIENT_WATER : FFDSounds.DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.DOLPHIN_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return FFDSounds.DOLPHIN_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return FFDSounds.DOLPHIN_SPLASH;
    }

    @Override
    public float getEyeHeight() {
        return isChild() ? 0.09375F : 0.3F;
    }

    @Override
    public void setScaleForAge(boolean child) {
        setScale(child ? 0.65F : 1.0F);
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 1;
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 1;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return true;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 38) {
            for (int i = 0; i < 7; i++) {
                world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        posX + (rand.nextFloat() * width * 2.0F) - width,
                        posY + 0.2F + rand.nextFloat() * height,
                        posZ + (rand.nextFloat() * width * 2.0F) - width,
                        rand.nextGaussian() * 0.01D, rand.nextGaussian() * 0.01D,
                        rand.nextGaussian() * 0.01D);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public BlockPos getTreasurePos() {
        return dataManager.get(TREASURE_POS);
    }

    public void setTreasurePos(BlockPos pos) {
        dataManager.set(TREASURE_POS, pos);
    }

    public boolean hasFish() {
        return dataManager.get(GOT_FISH);
    }

    public void setGotFish(boolean gotFish) {
        dataManager.set(GOT_FISH, gotFish);
    }

    public int getMoistness() {
        return dataManager.get(MOISTNESS);
    }

    public void setMoistness(int moistness) {
        dataManager.set(MOISTNESS, moistness);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        BlockPos treasure = getTreasurePos();
        compound.setInteger("TreasurePosX", treasure.getX());
        compound.setInteger("TreasurePosY", treasure.getY());
        compound.setInteger("TreasurePosZ", treasure.getZ());
        compound.setBoolean("GotFish", hasFish());
        compound.setInteger("Moistness", getMoistness());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setTreasurePos(new BlockPos(compound.getInteger("TreasurePosX"),
                compound.getInteger("TreasurePosY"), compound.getInteger("TreasurePosZ")));
        setGotFish(compound.getBoolean("GotFish"));
        setMoistness(compound.hasKey("Moistness", 99)
                ? compound.getInteger("Moistness") : 2400);
    }

    @Nullable
    public EntityItem throwItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        EntityItem item = new EntityItem(world, posX, posY - 0.3D + getEyeHeight(), posZ, stack);
        item.setPickupDelay(40);
        item.setThrower(getName());
        float velocity = 0.3F;
        item.motionX = -MathHelper.sin(rotationYaw * 0.017453292F)
                * MathHelper.cos(rotationPitch * 0.017453292F) * velocity;
        item.motionY = MathHelper.sin(rotationPitch * 0.017453292F) * velocity * 1.5F;
        item.motionZ = MathHelper.cos(rotationYaw * 0.017453292F)
                * MathHelper.cos(rotationPitch * 0.017453292F) * velocity;
        float angle = rand.nextFloat() * ((float) Math.PI * 2.0F);
        float spread = 0.02F * rand.nextFloat();
        item.motionX += MathHelper.cos(angle) * spread;
        item.motionZ += MathHelper.sin(angle) * spread;
        world.spawnEntity(item);
        return item;
    }

    private static boolean isFish(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.FISH;
    }

    private static boolean isWater(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private static final class DolphinMoveHelper extends EntityMoveHelper {
        private final EntityDolphin dolphin;

        private DolphinMoveHelper(EntityDolphin dolphin) {
            super(dolphin);
            this.dolphin = dolphin;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (dolphin.isInWater()) {
                dolphin.motionY += 0.005D;
            }
            if (action == Action.MOVE_TO && !dolphin.getNavigator().noPath()) {
                double x = posX - dolphin.posX;
                double y = posY - dolphin.posY;
                double z = posZ - dolphin.posZ;
                double squared = x * x + y * y + z * z;
                if (squared < 2.5E-7D) {
                    dolphin.setAIMoveSpeed(0.0F);
                    return;
                }
                double horizontal = Math.sqrt(x * x + z * z);
                float yaw = (float) (MathHelper.atan2(z, x) * 57.295776D) - 90.0F;
                dolphin.rotationYaw = limitAngle(dolphin.rotationYaw, yaw, 10.0F);
                dolphin.renderYawOffset = dolphin.rotationYaw;
                dolphin.rotationYawHead = dolphin.rotationYaw;
                float speed = (float) (this.speed * dolphin.getEntityAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                if (dolphin.isInWater()) {
                    dolphin.setAIMoveSpeed(speed * 0.02F);
                    float pitch = -(float) (MathHelper.atan2(y, horizontal) * 57.295776D);
                    pitch = MathHelper.clamp(MathHelper.wrapDegrees(pitch), -85.0F, 85.0F);
                    dolphin.rotationPitch = limitAngle(dolphin.rotationPitch, pitch, 5.0F);
                    float pitchCos = MathHelper.cos(dolphin.rotationPitch * 0.017453292F);
                    float pitchSin = MathHelper.sin(dolphin.rotationPitch * 0.017453292F);
                    dolphin.moveForward = pitchCos * speed;
                    dolphin.moveVertical = -pitchSin * speed;
                } else {
                    dolphin.setAIMoveSpeed(speed * 0.1F);
                }
            } else {
                dolphin.setAIMoveSpeed(0.0F);
                dolphin.moveStrafing = 0.0F;
                dolphin.moveVertical = 0.0F;
                dolphin.moveForward = 0.0F;
            }
        }
    }

    private static final class DolphinLookHelper extends EntityLookHelper {
        private final EntityDolphin dolphin;
        private double targetX;
        private double targetY;
        private double targetZ;
        private float maxYawChange;
        private float maxPitchChange;
        private boolean looking;

        private DolphinLookHelper(EntityDolphin dolphin) {
            super(dolphin);
            this.dolphin = dolphin;
        }

        @Override
        public void setLookPositionWithEntity(Entity entity, float deltaYaw, float deltaPitch) {
            targetX = entity.posX;
            targetY = entity instanceof net.minecraft.entity.EntityLivingBase
                    ? entity.posY + entity.getEyeHeight()
                    : (entity.getEntityBoundingBox().minY
                    + entity.getEntityBoundingBox().maxY) * 0.5D;
            targetZ = entity.posZ;
            maxYawChange = deltaYaw;
            maxPitchChange = deltaPitch;
            looking = true;
        }

        @Override
        public void setLookPosition(double x, double y, double z,
                                    float deltaYaw, float deltaPitch) {
            targetX = x;
            targetY = y;
            targetZ = z;
            maxYawChange = deltaYaw;
            maxPitchChange = deltaPitch;
            looking = true;
        }

        @Override
        public void onUpdateLook() {
            if (looking) {
                looking = false;
                double x = targetX - dolphin.posX;
                double y = targetY - (dolphin.posY + dolphin.getEyeHeight());
                double z = targetZ - dolphin.posZ;
                double horizontal = Math.sqrt(x * x + z * z);
                float yaw = (float) (MathHelper.atan2(z, x) * 57.295776D) - 70.0F;
                float pitch = (float) (-(MathHelper.atan2(y, horizontal) * 57.295776D))
                        + 10.0F;
                dolphin.rotationYawHead = rotateTowards(dolphin.rotationYawHead,
                        yaw, maxYawChange);
                dolphin.rotationPitch = rotateTowards(dolphin.rotationPitch,
                        pitch, maxPitchChange);
            } else {
                if (dolphin.getNavigator().noPath()) {
                    dolphin.rotationPitch = rotateTowards(dolphin.rotationPitch, 0.0F, 5.0F);
                }
                dolphin.rotationYawHead = rotateTowards(dolphin.rotationYawHead,
                        dolphin.renderYawOffset, 10.0F);
            }
            float difference = MathHelper.wrapDegrees(
                    dolphin.rotationYawHead - dolphin.renderYawOffset);
            if (difference < -10.0F) {
                dolphin.renderYawOffset -= 4.0F;
            } else if (difference > 10.0F) {
                dolphin.renderYawOffset += 4.0F;
            }
        }

        private static float rotateTowards(float current, float target, float maximum) {
            float difference = MathHelper.wrapDegrees(target - current);
            return current + MathHelper.clamp(difference, -maximum, maximum);
        }
    }

    private static final class DolphinGroupData implements IEntityLivingData {
        private int groupSize;
    }

    private static final class DolphinBreathGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private BlockPos target;

        private DolphinBreathGoal(EntityDolphin dolphin) {
            this.dolphin = dolphin;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (dolphin.getAir() >= 140) {
                return false;
            }
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return dolphin.getAir() < 140;
        }

        @Override
        public void startExecuting() {
            dolphin.getNavigator().clearPath();
            findBreathingPos(dolphin);
        }

        @Override
        public void updateTask() {
            findBreathingPos(dolphin);
            dolphin.moveRelative(dolphin.moveStrafing, dolphin.moveVertical,
                    dolphin.moveForward, 0.02F);
            dolphin.move(MoverType.SELF, dolphin.motionX, dolphin.motionY, dolphin.motionZ);
        }

        @Override
        public void resetTask() {
            target = null;
        }

        @Nullable
        private static void findBreathingPos(EntityDolphin dolphin) {
            int minX = MathHelper.floor(dolphin.posX - 1.0D);
            int minY = MathHelper.floor(dolphin.posY);
            int minZ = MathHelper.floor(dolphin.posZ - 1.0D);
            int maxX = MathHelper.floor(dolphin.posX + 1.0D);
            int maxY = MathHelper.floor(dolphin.posY + 8.0D);
            int maxZ = MathHelper.floor(dolphin.posZ + 1.0D);
            BlockPos target = null;
            for (int x = minX; x <= maxX && target == null; x++) {
                for (int y = minY; y <= maxY && target == null; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos candidate = new BlockPos(x, y, z);
                        IBlockState state = dolphin.world.getBlockState(candidate);
                        if ((state.getMaterial() == Material.AIR
                                || state.getBlock() == FFDBlocks.BUBBLE_COLUMN)
                                && state.getBlock().isPassable(dolphin.world, candidate)) {
                            target = candidate;
                            break;
                        }
                    }
                }
            }
            if (target == null) {
                target = new BlockPos(dolphin.posX, dolphin.posY + 8.0D, dolphin.posZ);
            }
            dolphin.getNavigator().tryMoveToXYZ(target.getX(), target.getY() + 1.0D,
                    target.getZ(), 1.0D);
        }
    }

    private static final class DolphinFindWaterGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private BlockPos target;

        private DolphinFindWaterGoal(EntityDolphin dolphin) {
            this.dolphin = dolphin;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!dolphin.onGround || dolphin.isInWater()) {
                return false;
            }
            BlockPos origin = dolphin.getPosition();
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 0; y++) {
                    for (int z = -2; z <= 2; z++) {
                        BlockPos candidate = origin.add(x, y, z);
                        if (isWater(dolphin.world, candidate)) {
                            target = candidate;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && dolphin.onGround && !dolphin.isInWater();
        }

        @Override
        public void startExecuting() {
            if (target != null) {
                dolphin.getMoveHelper().setMoveTo(target.getX(), target.getY(),
                        target.getZ(), 1.0D);
            }
        }

        @Override
        public void resetTask() {
            target = null;
            dolphin.getNavigator().clearPath();
        }
    }

    private static final class DolphinSwimGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private final double speed;
        private final int chance;
        private Vec3d target;

        private DolphinSwimGoal(EntityDolphin dolphin, double speed, int chance) {
            this.dolphin = dolphin;
            this.speed = speed;
            this.chance = chance;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!dolphin.isInWater() || dolphin.rand.nextInt(chance) != 0) {
                return false;
            }
            Vec3d target = RandomPositionGenerator.findRandomTarget(dolphin, 10, 7);
            int attempt = 0;
            while (target != null && !isWaterPath(dolphin, new BlockPos(target))
                    && attempt++ < 10) {
                target = RandomPositionGenerator.findRandomTarget(dolphin, 10, 7);
            }
            if (target == null || !isWaterPath(dolphin, new BlockPos(target))) {
                return false;
            }
            this.target = target;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return dolphin.isInWater() && !dolphin.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            dolphin.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, speed);
        }

        @Override
        public void resetTask() {
            target = null;
        }
    }

    private static boolean isWaterPath(EntityDolphin dolphin, BlockPos pos) {
        return dolphin.world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private static final class DolphinSwimWithPlayerGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private final double speed;
        private EntityPlayer player;

        private DolphinSwimWithPlayerGoal(EntityDolphin dolphin, double speed) {
            this.dolphin = dolphin;
            this.speed = speed;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            player = dolphin.world.getClosestPlayerToEntity(dolphin, 10.0D);
            return isSwimming(player) && dolphin.getAttackTarget() != player;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return isSwimming(player) && dolphin.getDistanceSq(player) < 256.0D;
        }

        @Override
        public void startExecuting() {
            applyGrace();
        }

        @Override
        public void updateTask() {
            dolphin.getLookHelper().setLookPositionWithEntity(player,
                    dolphin.getHorizontalFaceSpeed() + 20, dolphin.getVerticalFaceSpeed());
            if (dolphin.getDistanceSq(player) < 6.25D) {
                dolphin.getNavigator().clearPath();
            } else {
                dolphin.getNavigator().tryMoveToEntityLiving(player, speed);
            }
            if (isSwimming(player) && player.world.rand.nextInt(6) == 0) {
                applyGrace();
            }
        }

        @Override
        public void resetTask() {
            player = null;
            dolphin.getNavigator().clearPath();
        }

        private void applyGrace() {
            Potion grace = FFDPotions.effectiveDolphinsGrace();
            if (player != null && grace != null) {
                player.addPotionEffect(new PotionEffect(grace, 100));
            }
        }

        private static boolean isSwimming(EntityPlayer player) {
            return player != null && !player.isSpectator()
                    && (FFDGameplayHooks.isSwimming(player)
                    || !FFDGameplayHooks.isSwimmingSystemEnabled()
                    && player.isSprinting() && player.isInWater());
        }
    }

    private static final class DolphinTreasureGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private boolean failed;

        private DolphinTreasureGoal(EntityDolphin dolphin) {
            this.dolphin = dolphin;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return dolphin.hasFish() && dolphin.getAir() >= 100;
        }

        @Override
        public boolean shouldContinueExecuting() {
            BlockPos treasure = dolphin.getTreasurePos();
            return dolphin.getDistanceSq(treasure.getX(), dolphin.posY, treasure.getZ()) > 16.0D
                    && !failed && dolphin.getAir() >= 100;
        }

        @Override
        public void startExecuting() {
            failed = false;
            dolphin.getNavigator().clearPath();
            BlockPos treasure = FFDOceanStructureLocator.findDolphinTreasure(
                    dolphin.world, dolphin.getPosition(), 50);
            if (treasure == null) {
                failed = true;
            } else {
                dolphin.setTreasurePos(treasure);
                dolphin.world.setEntityState(dolphin, (byte) 38);
            }
        }

        @Override
        public void updateTask() {
            BlockPos treasure = dolphin.getTreasurePos();
            if (dolphin.getNavigator().noPath()) {
                Vec3d target = findWaterToward(dolphin, treasure, 16, 4);
                if (target == null) {
                    failed = true;
                    return;
                }
                dolphin.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, 1.3D);
            }
            if (dolphin.rand.nextInt(80) == 0) {
                dolphin.world.setEntityState(dolphin, (byte) 38);
            }
        }

        @Override
        public void resetTask() {
            BlockPos treasure = dolphin.getTreasurePos();
            if (failed || dolphin.getDistanceSq(treasure.getX(), dolphin.posY,
                    treasure.getZ()) <= 16.0D) {
                dolphin.setGotFish(false);
            }
        }
    }

    private static final class DolphinPlayGoal extends EntityAIBase {
        private final EntityDolphin dolphin;

        private DolphinPlayGoal(EntityDolphin dolphin) {
            this.dolphin = dolphin;
        }

        @Override
        public boolean shouldExecute() {
            if (dolphin.playCooldown > dolphin.ticksExisted) {
                return false;
            }
            return !nearbyItems().isEmpty()
                    || !dolphin.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void startExecuting() {
            List<EntityItem> items = nearbyItems();
            if (!items.isEmpty()) {
                dolphin.getNavigator().tryMoveToEntityLiving(items.get(0), 1.2D);
                dolphin.playSound(FFDSounds.DOLPHIN_PLAY, 1.0F, 1.0F);
            }
            dolphin.playCooldown = 0;
        }

        @Override
        public void updateTask() {
            ItemStack held = dolphin.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            if (!held.isEmpty()) {
                dolphin.throwItem(held);
                dolphin.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                return;
            }
            List<EntityItem> items = nearbyItems();
            if (items.isEmpty()) {
                return;
            }
            EntityItem item = items.get(0);
            if (dolphin.getDistanceSq(item) <= 2.25D) {
                dolphin.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item.getItem());
                item.setDead();
            } else {
                dolphin.getNavigator().tryMoveToEntityLiving(item, 1.2D);
            }
        }

        @Override
        public void resetTask() {
            ItemStack held = dolphin.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            if (!held.isEmpty()) {
                dolphin.throwItem(held);
                dolphin.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                dolphin.playCooldown = dolphin.ticksExisted + dolphin.rand.nextInt(100);
            }
        }

        private List<EntityItem> nearbyItems() {
            AxisAlignedBB area = dolphin.getEntityBoundingBox().grow(8.0D);
            return dolphin.world.getEntitiesWithinAABB(EntityItem.class, area,
                    item -> item != null && !item.cannotPickup() && item.isEntityAlive()
                            && item.isInWater());
        }
    }

    private static final class DolphinFollowBoatGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private EntityBoat boat;
        private EntityLivingBase passenger;
        private BoatGoal boatGoal;
        private int updateCountdown;

        private DolphinFollowBoatGoal(EntityDolphin dolphin) {
            this.dolphin = dolphin;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (passenger != null && hasMovementInput(passenger)) {
                return true;
            }
            List<EntityBoat> boats = dolphin.world.getEntitiesWithinAABB(EntityBoat.class,
                    dolphin.getEntityBoundingBox().grow(5.0D), candidate ->
                            candidate != null && candidate.isEntityAlive()
                                    && candidate.getControllingPassenger() instanceof EntityLivingBase
                                    && hasMovementInput((EntityLivingBase)
                                    candidate.getControllingPassenger()));
            if (boats.isEmpty()) {
                return false;
            }
            boat = boats.get(0);
            passenger = (EntityLivingBase) boat.getControllingPassenger();
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return boat != null && boat.isEntityAlive() && passenger != null
                    && boat.getControllingPassenger() == passenger
                    && hasMovementInput(passenger);
        }

        @Override
        public void startExecuting() {
            updateCountdown = 0;
            boatGoal = BoatGoal.GO_TO_BOAT;
        }

        @Override
        public void resetTask() {
            boat = null;
            passenger = null;
            boatGoal = null;
            dolphin.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            boolean moving = hasMovementInput(passenger);
            float acceleration = boatGoal == BoatGoal.GO_IN_BOAT_DIRECTION
                    ? (moving ? 0.18F : 0.0F) : 0.135F;
            dolphin.moveRelative(dolphin.moveStrafing, dolphin.moveVertical,
                    dolphin.moveForward, acceleration);
            dolphin.move(MoverType.SELF, dolphin.motionX, dolphin.motionY, dolphin.motionZ);
            if (--updateCountdown > 0) {
                return;
            }
            updateCountdown = 10;
            if (boatGoal == BoatGoal.GO_TO_BOAT) {
                BlockPos target = new BlockPos(passenger)
                        .offset(passenger.getHorizontalFacing().getOpposite()).down();
                dolphin.getNavigator().tryMoveToXYZ(target.getX(), target.getY(),
                        target.getZ(), 1.0D);
                if (dolphin.getDistanceSq(passenger) < 16.0D) {
                    updateCountdown = 0;
                    boatGoal = BoatGoal.GO_IN_BOAT_DIRECTION;
                }
            } else {
                BlockPos target = new BlockPos(passenger)
                        .offset(passenger.getHorizontalFacing(), 10).down();
                dolphin.getNavigator().tryMoveToXYZ(target.getX(), target.getY(),
                        target.getZ(), 1.0D);
                if (dolphin.getDistanceSq(passenger) > 144.0D) {
                    updateCountdown = 0;
                    boatGoal = BoatGoal.GO_TO_BOAT;
                }
            }
        }

        private static boolean hasMovementInput(EntityLivingBase entity) {
            return entity != null && (Math.abs(entity.moveStrafing) > 0.0F
                    || Math.abs(entity.moveForward) > 0.0F);
        }

        private enum BoatGoal {
            GO_TO_BOAT,
            GO_IN_BOAT_DIRECTION
        }
    }

    private static final class DolphinJumpGoal extends EntityAIBase {
        private final EntityDolphin dolphin;
        private final int chance;
        private boolean breached;

        private DolphinJumpGoal(EntityDolphin dolphin, int chance) {
            this.dolphin = dolphin;
            this.chance = chance;
            setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            if (dolphin.rand.nextInt(chance) != 0) {
                return false;
            }
            EnumFacing facing = dolphin.getHorizontalFacing();
            BlockPos origin = dolphin.getPosition();
            int x = facing.getFrontOffsetX();
            int z = facing.getFrontOffsetZ();
            int[] distances = {0, 1, 4, 5, 6, 7};
            for (int distance : distances) {
                BlockPos water = origin.add(x * distance, 0, z * distance);
                if (!isWater(dolphin.world, water)
                        || !dolphin.world.isAirBlock(water.up())
                        || !dolphin.world.isAirBlock(water.up(2))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !((dolphin.motionY * dolphin.motionY) < 0.03D
                    && dolphin.rotationPitch != 0.0F
                    && Math.abs(dolphin.rotationPitch) < 10.0F
                    && dolphin.isInWater() || dolphin.onGround);
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }

        @Override
        public void startExecuting() {
            EnumFacing facing = dolphin.getHorizontalFacing();
            dolphin.motionX += facing.getFrontOffsetX() * 0.6D;
            dolphin.motionY += 0.7D;
            dolphin.motionZ += facing.getFrontOffsetZ() * 0.6D;
            dolphin.getNavigator().clearPath();
            breached = false;
        }

        @Override
        public void updateTask() {
            boolean alreadyBreached = breached;
            if (!alreadyBreached) {
                breached = dolphin.isInWater();
            }
            if (breached && !alreadyBreached) {
                dolphin.playSound(FFDSounds.DOLPHIN_JUMP, 1.0F, 1.0F);
            }
            if (dolphin.motionY * dolphin.motionY < 0.03D
                    && dolphin.rotationPitch != 0.0F) {
                dolphin.rotationPitch = limitAngle(dolphin.rotationPitch, 0.0F, 0.2F);
            } else if (dolphin.motionX * dolphin.motionX + dolphin.motionY * dolphin.motionY
                    + dolphin.motionZ * dolphin.motionZ > 1.0E-10D) {
                double horizontal = Math.sqrt(dolphin.motionX * dolphin.motionX
                        + dolphin.motionZ * dolphin.motionZ);
                double pitch = Math.atan2(-dolphin.motionY, horizontal)
                        * (180.0D / Math.PI);
                dolphin.rotationPitch = (float) pitch;
            }
        }

        @Override
        public void resetTask() {
            dolphin.rotationPitch = 0.0F;
        }

        private static float limitAngle(float current, float target, float maximum) {
            float difference = MathHelper.wrapDegrees(target - current);
            return current + MathHelper.clamp(difference, -maximum, maximum);
        }
    }

    @Nullable
    private static Vec3d findWaterToward(EntityDolphin dolphin, BlockPos target,
                                         int horizontal, int vertical) {
        BlockPos origin = dolphin.getPosition();
        Vec3d best = null;
        double bestDistance = origin.distanceSq(target);
        for (int attempt = 0; attempt < 30; attempt++) {
            BlockPos candidate = origin.add(dolphin.rand.nextInt(horizontal * 2 + 1) - horizontal,
                    dolphin.rand.nextInt(vertical * 2 + 1) - vertical,
                    dolphin.rand.nextInt(horizontal * 2 + 1) - horizontal);
            if (!isWater(dolphin.world, candidate)) {
                continue;
            }
            double distance = candidate.distanceSq(target);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = new Vec3d(candidate.getX() + 0.5D, candidate.getY() + 0.5D,
                        candidate.getZ() + 0.5D);
            }
        }
        return best;
    }
}
