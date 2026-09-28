package xy177.farmersfuturedelight.common.entity;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.Path;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;

public abstract class EntityAbstractFish extends net.minecraft.entity.passive.EntityWaterMob
        implements IFishBucketEntity {
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(
            EntityAbstractFish.class, DataSerializers.BOOLEAN);

    protected EntityAbstractFish(World world) {
        super(world);
        moveHelper = new FishMoveHelper(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(FROM_BUCKET, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new FishPanicGoal(this));
        tasks.addTask(2, new FishAvoidPlayerGoal(this));
        tasks.addTask(4, new FishSwimGoal(this));
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        return new PathNavigateSwimmer(this, world);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isServerWorld() && isInWater()) {
            moveRelative(strafe, vertical, forward, 0.01F);
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
        if (!isInWater() && onGround && collidedVertically) {
            motionY += 0.4D;
            motionX += (rand.nextFloat() * 2.0F - 1.0F) * 0.05F;
            motionZ += (rand.nextFloat() * 2.0F - 1.0F) * 0.05F;
            onGround = false;
            isAirBorne = true;
            playSound(getFlopSound(), getSoundVolume(), getSoundPitch());
        }
        super.onLivingUpdate();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() != Items.WATER_BUCKET || !isEntityAlive()) {
            return super.processInteract(player, hand);
        }
        ItemStack bucket = getBucketStack();
        if (bucket.isEmpty()) {
            return super.processInteract(player, hand);
        }
        writeToBucket(bucket);
        playSound(FFDSounds.BUCKET_FILL_FISH, 1.0F, 1.0F);
        if (!world.isRemote) {
            held.shrink(1);
            if (held.isEmpty()) {
                player.setHeldItem(hand, bucket);
            } else if (!player.inventory.addItemStackToInventory(bucket)) {
                player.dropItem(bucket, false);
            }
            player.addStat(StatList.getObjectUseStats(Items.WATER_BUCKET));
            if (player instanceof EntityPlayerMP) {
                FFDAdvancements.TACTICAL_FISHING.trigger((EntityPlayerMP) player);
            }
            setDead();
        }
        return true;
    }

    protected void writeToBucket(ItemStack bucket) {
        if (hasCustomName()) {
            bucket.setStackDisplayName(getCustomNameTag());
        }
        NBTTagCompound entityTag = bucket.getOrCreateSubCompound("EntityTag");
        if (isAIDisabled()) {
            entityTag.setBoolean("NoAI", true);
        }
        if (isSilent()) {
            entityTag.setBoolean("Silent", true);
        }
        if (hasNoGravity()) {
            entityTag.setBoolean("NoGravity", true);
        }
        if (isGlowing()) {
            entityTag.setBoolean("Glowing", true);
        }
        if (getIsInvulnerable()) {
            entityTag.setBoolean("Invulnerable", true);
        }
        if (isNoDespawnRequired()) {
            entityTag.setBoolean("PersistenceRequired", true);
        }
        entityTag.setFloat("Health", getHealth());
    }

    @Override
    public void readFromBucket(ItemStack bucket) {
        if (bucket.hasDisplayName()) {
            setCustomNameTag(bucket.getDisplayName());
        }
        setFromBucket(true);
        if (bucket.hasTagCompound() && bucket.getTagCompound().hasKey("EntityTag", 10)) {
            NBTTagCompound entityTag = bucket.getTagCompound().getCompoundTag("EntityTag");
            if (entityTag.hasKey("CustomName", 8)) {
                setCustomNameTag(entityTag.getString("CustomName"));
            }
            if (entityTag.hasKey("NoAI", 1)) {
                setNoAI(entityTag.getBoolean("NoAI"));
            }
            if (entityTag.hasKey("Silent", 1)) {
                setSilent(entityTag.getBoolean("Silent"));
            }
            if (entityTag.hasKey("NoGravity", 1)) {
                setNoGravity(entityTag.getBoolean("NoGravity"));
            }
            if (entityTag.hasKey("Glowing", 1)) {
                setGlowing(entityTag.getBoolean("Glowing"));
            }
            if (entityTag.hasKey("Invulnerable", 1)) {
                setEntityInvulnerable(entityTag.getBoolean("Invulnerable"));
            }
            if (entityTag.getBoolean("PersistenceRequired")) {
                enablePersistence();
            }
            if (entityTag.hasKey("Health", 99)) {
                setHealth(entityTag.getFloat("Health"));
            }
        }
        enablePersistence();
    }

    public boolean isFromBucket() {
        return dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", isFromBucket());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setFromBucket(compound.getBoolean("FromBucket"));
        if (isFromBucket()) {
            enablePersistence();
        }
    }

    @Override
    protected boolean canDespawn() {
        return !isFromBucket() && !hasCustomName();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 8;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        int seaLevel = world.getSeaLevel();
        return pos.getY() >= seaLevel - 13 && pos.getY() <= seaLevel
                && world.getBlockState(pos).getMaterial() == Material.WATER
                && world.getBlockState(pos.down()).getMaterial() == Material.WATER
                && world.getBlockState(pos.up()).getBlock() == net.minecraft.init.Blocks.WATER
                && super.getCanSpawnHere() && isNotColliding();
    }

    @Override
    public float getEyeHeight() {
        return height * 0.65F;
    }

    protected boolean canRandomSwim() {
        return true;
    }

    protected abstract ItemStack getBucketStack();

    protected abstract SoundEvent getFlopSound();

    @Override
    protected SoundEvent getSwimSound() {
        return FFDSounds.FISH_SWIM;
    }

    @Nullable
    private static Vec3d findRandomWaterTarget(EntityAbstractFish fish,
                                                int horizontal, int vertical) {
        BlockPos origin = fish.getPosition();
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = origin.add(
                    fish.rand.nextInt(horizontal * 2 + 1) - horizontal,
                    fish.rand.nextInt(vertical * 2 + 1) - vertical,
                    fish.rand.nextInt(horizontal * 2 + 1) - horizontal);
            if (fish.world.getBlockState(candidate).getMaterial() == Material.WATER) {
                return new Vec3d(candidate).addVector(0.5D, 0.5D, 0.5D);
            }
        }
        return null;
    }

    @Nullable
    private static Vec3d findWaterTargetAway(EntityAbstractFish fish, Vec3d source,
                                              int horizontal, int vertical) {
        BlockPos origin = fish.getPosition();
        double awayX = fish.posX - source.x;
        double awayZ = fish.posZ - source.z;
        double currentDistance = fish.getDistanceSq(source.x, source.y, source.z);
        for (int attempt = 0; attempt < 10; attempt++) {
            int offsetX = fish.rand.nextInt(horizontal * 2 + 1) - horizontal;
            int offsetY = fish.rand.nextInt(vertical * 2 + 1) - vertical;
            int offsetZ = fish.rand.nextInt(horizontal * 2 + 1) - horizontal;
            if (offsetX * awayX + offsetZ * awayZ < 0.0D) {
                continue;
            }
            BlockPos candidate = origin.add(offsetX, offsetY, offsetZ);
            if (fish.world.getBlockState(candidate).getMaterial() != Material.WATER) {
                continue;
            }
            double distance = candidate.distanceSq(source.x, source.y, source.z);
            if (distance >= currentDistance) {
                return new Vec3d(candidate).addVector(0.5D, 0.5D, 0.5D);
            }
        }
        return null;
    }

    @Nullable
    private static EntityPlayer findPlayerToAvoid(EntityAbstractFish fish) {
        EntityPlayer closest = null;
        double closestDistance = 64.0D;
        for (EntityPlayer candidate : fish.world.playerEntities) {
            double distance = fish.getDistanceSq(candidate);
            if (distance > closestDistance || candidate.isCreative() || candidate.isSpectator()
                    || fish.isOnSameTeam(candidate)
                    || !fish.getEntitySenses().canSee(candidate)) {
                continue;
            }
            closest = candidate;
            closestDistance = distance;
        }
        return closest;
    }

    private static final class FishPanicGoal extends EntityAIBase {
        private final EntityAbstractFish fish;
        @Nullable
        private Path path;

        private FishPanicGoal(EntityAbstractFish fish) {
            this.fish = fish;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (fish.getRevengeTarget() == null && !fish.isBurning()) {
                return false;
            }
            Vec3d target = findRandomWaterTarget(fish, 5, 4);
            if (target == null) {
                return false;
            }
            path = fish.getNavigator().getPathToXYZ(target.x, target.y, target.z);
            return path != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !fish.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            fish.getNavigator().setPath(path, 1.25D);
        }

        @Override
        public void resetTask() {
            path = null;
        }
    }

    private static final class FishAvoidPlayerGoal extends EntityAIBase {
        private final EntityAbstractFish fish;
        @Nullable
        private EntityPlayer player;
        @Nullable
        private Path path;

        private FishAvoidPlayerGoal(EntityAbstractFish fish) {
            this.fish = fish;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            player = findPlayerToAvoid(fish);
            if (player == null) {
                return false;
            }
            Vec3d target = findWaterTargetAway(fish,
                    new Vec3d(player.posX, player.posY, player.posZ), 16, 7);
            if (target == null
                    || player.getDistanceSq(target.x, target.y, target.z)
                    < player.getDistanceSq(fish)) {
                return false;
            }
            path = fish.getNavigator().getPathToXYZ(target.x, target.y, target.z);
            return path != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return player != null && !fish.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            fish.getNavigator().setPath(path, 1.6D);
        }

        @Override
        public void updateTask() {
            fish.getNavigator().setSpeed(fish.getDistanceSq(player) < 49.0D
                    ? 1.4D : 1.6D);
        }

        @Override
        public void resetTask() {
            player = null;
            path = null;
        }
    }

    private static final class FishMoveHelper extends EntityMoveHelper {
        private final EntityAbstractFish fish;

        private FishMoveHelper(EntityAbstractFish fish) {
            super(fish);
            this.fish = fish;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (fish.isInsideOfMaterial(Material.WATER)) {
                fish.motionY += 0.005D;
            }
            if (action == Action.MOVE_TO && !fish.getNavigator().noPath()) {
                double x = posX - fish.posX;
                double y = posY - fish.posY;
                double z = posZ - fish.posZ;
                float targetSpeed = (float) (speed * fish.getEntityAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                fish.setAIMoveSpeed(fish.getAIMoveSpeed()
                        + (targetSpeed - fish.getAIMoveSpeed()) * 0.125F);
                if (y != 0.0D) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance > 1.0E-5D) {
                        fish.motionY += fish.getAIMoveSpeed() * (y / distance) * 0.1D;
                    }
                }
                if (x != 0.0D || z != 0.0D) {
                    float yaw = (float) (MathHelper.atan2(z, x) * 180.0D / Math.PI)
                            - 90.0F;
                    fish.rotationYaw = limitAngle(fish.rotationYaw, yaw, 90.0F);
                    fish.renderYawOffset = fish.rotationYaw;
                }
            } else {
                fish.setAIMoveSpeed(0.0F);
            }
        }
    }

    private static final class FishSwimGoal extends EntityAIBase {
        private final EntityAbstractFish fish;
        @Nullable
        private Path path;

        private FishSwimGoal(EntityAbstractFish fish) {
            this.fish = fish;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!fish.isInWater() || !fish.canRandomSwim() || fish.rand.nextInt(40) != 0) {
                return false;
            }
            Vec3d target = findRandomWaterTarget(fish, 10, 7);
            if (target == null) {
                return false;
            }
            path = fish.getNavigator().getPathToXYZ(target.x, target.y, target.z);
            return path != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return fish.isInWater() && fish.canRandomSwim() && !fish.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            fish.getNavigator().setPath(path, 1.0D);
        }

        @Override
        public void resetTask() {
            path = null;
        }
    }
}
