package xy177.farmersfuturedelight.common.entity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockTurtleEgg;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityTurtle extends net.minecraft.entity.passive.EntityAnimal {
    private static final DataParameter<BlockPos> HOME_POS = EntityDataManager.createKey(
            EntityTurtle.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> HAS_EGG = EntityDataManager.createKey(
            EntityTurtle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LAYING_EGG = EntityDataManager.createKey(
            EntityTurtle.class, DataSerializers.BOOLEAN);
    private final PathNavigateSwimmer waterNavigator;
    private final PathNavigateGround groundNavigator;
    private boolean usingWaterNavigator;
    private boolean goingHome;
    private int layEggCounter;

    public EntityTurtle(World world) {
        super(world);
        setSize(1.2F, 0.4F);
        stepHeight = 1.0F;
        setPathPriority(PathNodeType.WATER, 0.0F);
        setPathPriority(PathNodeType.WALKABLE, 1.0F);
        waterNavigator = new PathNavigateSwimmer(this, world);
        groundNavigator = new PathNavigateGround(this, world);
        navigator = groundNavigator;
        moveHelper = new TurtleMoveHelper(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(HOME_POS, BlockPos.ORIGIN);
        dataManager.register(HAS_EGG, false);
        dataManager.register(LAYING_EGG, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new TurtlePanicGoal(this, 1.2D));
        tasks.addTask(1, new TurtleMateGoal(this, 1.0D));
        tasks.addTask(1, new TurtleLayEggGoal(this, 1.0D));
        Set<Item> seagrassItems = getSeagrassItems();
        if (!seagrassItems.isEmpty()) {
            tasks.addTask(2, new EntityAITempt(this, 1.1D, false, seagrassItems));
        }
        tasks.addTask(3, new TurtleGoToWaterGoal(this, 1.0D));
        tasks.addTask(4, new TurtleGoHomeGoal(this, 1.0D));
        tasks.addTask(7, new TurtleTravelGoal(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(9, new TurtleLandWanderGoal(this, 1.0D, 100));
    }

    @Override
    public void onUpdate() {
        updateNavigation();
        super.onUpdate();
    }

    private void updateNavigation() {
        if (world.isRemote) {
            return;
        }
        boolean inWater = isInWater();
        if (inWater == usingWaterNavigator) {
            return;
        }
        navigator.clearPath();
        navigator = inWater ? waterNavigator : groundNavigator;
        usingWaterNavigator = inWater;

    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && isEntityAlive() && isLayingEgg() && layEggCounter >= 1
                && layEggCounter % 5 == 0) {
            BlockPos pos = getPosition();
            if (BlockTurtleEgg.isOnSand(world, pos)) {
                world.playEvent(2001, pos,
                        net.minecraft.block.Block.getStateId(world.getBlockState(pos.down())));
            }
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isServerWorld() && isInWater()) {
            moveRelative(strafe, vertical, forward, 0.1F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.9D;
            motionY *= 0.9D;
            motionZ *= 0.9D;
            if (getAttackTarget() == null
                    && !(goingHome && getDistanceSqToCenter(getHomePos()) < 400.0D)) {
                motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return FFDItems.isTurtleEnabled() && pos.getY() < world.getSeaLevel() + 4
                && BlockTurtleEgg.isOnSand(world, pos) && world.getLight(pos) > 7
                && world.checkNoEntityCollision(getEntityBoundingBox())
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingData) {
        setHomePos(new BlockPos(this));
        return super.onInitialSpawn(difficulty, livingData);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return getSeagrassItems().contains(stack.getItem());
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hasEgg() && isBreedingItem(player.getHeldItem(hand))) {
            return false;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean canMateWith(net.minecraft.entity.passive.EntityAnimal otherAnimal) {
        return !hasEgg() && super.canMateWith(otherAnimal);
    }

    @Nullable
    @Override
    public EntityTurtle createChild(EntityAgeable ageable) {
        return new EntityTurtle(world);
    }

    @Override
    protected void onGrowingAdult() {
        super.onGrowingAdult();
        if (!world.isRemote && FFDItems.isTurtleEnabled()
                && world.getGameRules().getBoolean("doMobLoot")) {
            entityDropItem(new net.minecraft.item.ItemStack(FFDItems.TURTLE_SCUTE), 0.0F);
        }
    }

    @Override
    public void setScaleForAge(boolean child) {
        setSize(child ? 0.36F : 1.2F, child ? 0.12F : 0.4F);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        if (!FFDItems.isTurtleEnabled()) {
            return;
        }
        int amount = rand.nextInt(3) + rand.nextInt(lootingModifier + 1);
        if (amount > 0) {
            Item seagrass = getPrimarySeagrassItem();
            if (seagrass != null) {
                entityDropItem(new ItemStack(seagrass, amount), 0.0F);
            }
        }
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        if (!world.isRemote) {
            if (world.getGameRules().getBoolean("doMobLoot")) {
                entityDropItem(new ItemStack(Items.BOWL), 0.0F);
            }
            attackEntityFrom(DamageSource.LIGHTNING_BOLT, Float.MAX_VALUE);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return !isInWater() && onGround && !isChild() ? FFDSounds.TURTLE_AMBIENT : null;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return FFDSounds.TURTLE_SWIM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isChild() ? FFDSounds.TURTLE_BABY_HURT : FFDSounds.TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isChild() ? FFDSounds.TURTLE_BABY_DEATH : FFDSounds.TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
        playSound(isChild() ? FFDSounds.TURTLE_BABY_STEP : FFDSounds.TURTLE_STEP, 0.15F, 1.0F);
    }

    @Override
    public int getTalkInterval() {
        return 200;
    }

    @Override
    public float getEyeHeight() {
        return height * 0.5F;
    }

    public void setHomePos(BlockPos pos) {
        dataManager.set(HOME_POS, pos);
    }

    public BlockPos getHomePos() {
        return dataManager.get(HOME_POS);
    }

    public boolean hasEgg() {
        return dataManager.get(HAS_EGG);
    }

    public void setHasEgg(boolean hasEgg) {
        dataManager.set(HAS_EGG, hasEgg);
    }

    public boolean isLayingEgg() {
        return dataManager.get(LAYING_EGG);
    }

    public void setLayingEgg(boolean layingEgg) {
        layEggCounter = layingEgg ? 1 : 0;
        dataManager.set(LAYING_EGG, layingEgg);
    }

    public boolean isGoingHome() {
        return goingHome;
    }

    private void setGoingHome(boolean goingHome) {
        this.goingHome = goingHome;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        BlockPos home = getHomePos();
        compound.setInteger("HomePosX", home.getX());
        compound.setInteger("HomePosY", home.getY());
        compound.setInteger("HomePosZ", home.getZ());
        compound.setBoolean("HasEgg", hasEgg());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("HomePosX", 99) && compound.hasKey("HomePosY", 99)
                && compound.hasKey("HomePosZ", 99)) {
            setHomePos(new BlockPos(compound.getInteger("HomePosX"), compound.getInteger("HomePosY"),
                    compound.getInteger("HomePosZ")));
        } else {
            setHomePos(getPosition());
        }
        setHasEgg(compound.getBoolean("HasEgg"));
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        if (!goingHome && world.getBlockState(pos).getMaterial() == Material.WATER) {
            return 10.0F;
        }
        if (BlockTurtleEgg.isOnSand(world, pos)) {
            return 10.0F;
        }
        return super.getBlockPathWeight(pos);
    }

    private static final class TurtleMoveHelper extends EntityMoveHelper {
        private final EntityTurtle turtle;

        private TurtleMoveHelper(EntityTurtle turtle) {
            super(turtle);
            this.turtle = turtle;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (turtle.isInWater()) {
                turtle.motionY += 0.005D;
                if (turtle.getDistanceSqToCenter(turtle.getHomePos()) > 256.0D) {
                    turtle.setAIMoveSpeed(Math.max(turtle.getAIMoveSpeed() / 2.0F, 0.08F));
                }
                if (turtle.isChild()) {
                    turtle.setAIMoveSpeed(Math.max(turtle.getAIMoveSpeed() / 3.0F, 0.06F));
                }
                if (action != Action.MOVE_TO || turtle.getNavigator().noPath()) {
                    turtle.setAIMoveSpeed(0.0F);
                    return;
                }
            }
            if (action == Action.MOVE_TO && turtle.isInWater() && !turtle.getNavigator().noPath()) {
                double x = posX - turtle.posX;
                double y = posY - turtle.posY;
                double z = posZ - turtle.posZ;
                double distance = Math.sqrt(x * x + y * y + z * z);
                if (distance < 1.0E-5D) {
                    turtle.setAIMoveSpeed(0.0F);
                    return;
                }
                y /= distance;
                float yaw = (float) (MathHelper.atan2(z, x) * (180.0D / Math.PI)) - 90.0F;
                turtle.rotationYaw = limitAngle(turtle.rotationYaw, yaw, 90.0F);
                turtle.renderYawOffset = turtle.rotationYaw;
                float desiredSpeed = (float) (speed
                        * turtle.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                turtle.setAIMoveSpeed(turtle.getAIMoveSpeed()
                        + (desiredSpeed - turtle.getAIMoveSpeed()) * 0.125F);
                turtle.motionY += turtle.getAIMoveSpeed() * y * 0.1D;
            } else if (!turtle.isInWater()) {
                super.onUpdateMoveHelper();
                if (turtle.onGround) {
                    turtle.setAIMoveSpeed(Math.max(turtle.getAIMoveSpeed() / 2.0F, 0.06F));
                }
            }
        }
    }

    private static final class TurtlePanicGoal extends EntityAIPanic {
        private final EntityTurtle turtle;

        private TurtlePanicGoal(EntityTurtle turtle, double speed) {
            super(turtle, speed);
            this.turtle = turtle;
        }

        @Override
        public boolean shouldExecute() {
            if (turtle.getRevengeTarget() == null && !turtle.isBurning()) {
                return false;
            }
            BlockPos water = findNearestWater(turtle, 7, 1);
            if (water != null) {
                randPosX = water.getX();
                randPosY = water.getY();
                randPosZ = water.getZ();
                return true;
            }
            return findRandomPosition();
        }
    }

    private static final class TurtleMateGoal extends EntityAIBase {
        private final EntityTurtle turtle;
        private final double speed;
        private EntityTurtle mate;
        private int mateTicks;

        private TurtleMateGoal(EntityTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (turtle.hasEgg() || !turtle.isInLove()) {
                return false;
            }
            List<EntityTurtle> turtles = turtle.world.getEntitiesWithinAABB(EntityTurtle.class,
                    turtle.getEntityBoundingBox().grow(8.0D));
            double nearestDistance = Double.MAX_VALUE;
            EntityTurtle nearest = null;
            for (EntityTurtle candidate : turtles) {
                if (turtle.canMateWith(candidate) && turtle.getDistanceSq(candidate) < nearestDistance) {
                    nearestDistance = turtle.getDistanceSq(candidate);
                    nearest = candidate;
                }
            }
            mate = nearest;
            return mate != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return mate != null && mate.isEntityAlive() && mate.isInLove() && mateTicks < 60;
        }

        @Override
        public void resetTask() {
            mate = null;
            mateTicks = 0;
        }

        @Override
        public void updateTask() {
            turtle.getLookHelper().setLookPositionWithEntity(mate, turtle.getHorizontalFaceSpeed(),
                    turtle.getVerticalFaceSpeed());
            turtle.getNavigator().tryMoveToEntityLiving(mate, speed);
            mateTicks++;
            if (mateTicks >= 60 && turtle.getDistanceSq(mate) < 9.0D) {
                breed();
            }
        }

        private void breed() {
            EntityPlayerMP player = turtle.getLoveCause();
            if (player == null) {
                player = mate.getLoveCause();
            }
            turtle.setGrowingAge(6000);
            mate.setGrowingAge(6000);
            turtle.resetInLove();
            mate.resetInLove();
            turtle.setHasEgg(true);
            if (player != null) {
                player.addStat(StatList.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(player, turtle, mate, null);
            }
            if (!turtle.world.isRemote && turtle.world.getGameRules().getBoolean("doMobLoot")) {
                turtle.world.spawnEntity(new EntityXPOrb(turtle.world, turtle.posX, turtle.posY,
                        turtle.posZ, turtle.rand.nextInt(7) + 1));
            }
            resetTask();
        }
    }

    private static final class TurtleGoHomeGoal extends EntityAIBase {
        private final EntityTurtle turtle;
        private final double speed;
        private int closeToHomeTryTicks;
        private boolean stuck;

        private TurtleGoHomeGoal(EntityTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (turtle.isChild()) {
                return false;
            }
            if (turtle.hasEgg()) {
                return true;
            }
            return turtle.rand.nextInt(700) == 0 && turtle.getDistanceSqToCenter(turtle.getHomePos()) > 4096.0D;
        }

        @Override
        public void startExecuting() {
            turtle.setGoingHome(true);
            closeToHomeTryTicks = 0;
            stuck = false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return turtle.getDistanceSqToCenter(turtle.getHomePos()) > 49.0D
                    && !stuck && closeToHomeTryTicks <= 600;
        }

        @Override
        public void updateTask() {
            BlockPos home = turtle.getHomePos();
            boolean closeToHome = turtle.getDistanceSqToCenter(home) < 256.0D;
            if (closeToHome) {
                closeToHomeTryTicks++;
            }
            if (turtle.getNavigator().noPath()) {
                Vec3d homeCenter = new Vec3d(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
                Vec3d next = RandomPositionGenerator.findRandomTargetBlockTowards(
                        turtle, 16, 3, homeCenter);
                if (next == null) {
                    next = RandomPositionGenerator.findRandomTargetBlockTowards(
                            turtle, 8, 7, homeCenter);
                }
                if (next != null && !closeToHome
                        && !isWaterBlock(turtle.world, new BlockPos(next))) {
                    next = RandomPositionGenerator.findRandomTargetBlockTowards(
                            turtle, 16, 5, homeCenter);
                }
                if (next == null) {
                    stuck = true;
                    return;
                }
                turtle.getNavigator().tryMoveToXYZ(next.x, next.y, next.z, speed);
            }
        }

        @Override
        public void resetTask() {
            turtle.setGoingHome(false);
        }
    }

    private static final class TurtleLayEggGoal extends net.minecraft.entity.ai.EntityAIMoveToBlock {
        private final EntityTurtle turtle;
        private TurtleLayEggGoal(EntityTurtle turtle, double speed) {
            super(turtle, speed, 16);
            this.turtle = turtle;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return turtle.hasEgg() && turtle.getDistanceSqToCenter(turtle.getHomePos()) < 81.0D
                    && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return turtle.hasEgg() && turtle.getDistanceSqToCenter(turtle.getHomePos()) < 81.0D
                    && super.shouldContinueExecuting();
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
        }

        @Override
        public void resetTask() {
            turtle.setLayingEgg(false);
            super.resetTask();
        }

        @Override
        public void updateTask() {
            super.updateTask();
            if (turtle.isInWater() || turtle.getDistanceSqToCenter(destinationBlock) > 1.0D) {
                return;
            }
            if (turtle.layEggCounter < 1) {
                turtle.setLayingEgg(true);
            } else if (turtle.layEggCounter > 200) {
                BlockPos eggPos = destinationBlock.up();
                turtle.world.playSound(null, turtle.getPosition(), FFDSounds.TURTLE_LAY_EGG,
                        SoundCategory.BLOCKS, 0.3F, 0.9F + turtle.rand.nextFloat() * 0.2F);
                turtle.world.setBlockState(eggPos, FFDBlocks.TURTLE_EGG.getDefaultState()
                        .withProperty(BlockTurtleEgg.EGGS, turtle.rand.nextInt(4) + 1), 3);
                turtle.setHasEgg(false);
                turtle.setLayingEgg(false);
                turtle.setInLove(null);
            }
            if (turtle.isLayingEgg()) {
                turtle.layEggCounter++;
            }
        }

        @Override
        protected boolean shouldMoveTo(World world, BlockPos pos) {
            return BlockTurtleEgg.isSand(world, pos) && world.isAirBlock(pos.up());
        }
    }

    private static final class TurtleGoToWaterGoal extends EntityAIBase {
        private final EntityTurtle turtle;
        private final double speed;
        private BlockPos waterPos;
        private int ticks;
        private int runDelay;

        private TurtleGoToWaterGoal(EntityTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = turtle.isChild() ? 2.0D : speed;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (turtle.isInWater() || turtle.hasEgg() || turtle.isGoingHome()) {
                return false;
            }
            if (runDelay > 0) {
                runDelay--;
                return false;
            }
            runDelay = 200 + turtle.rand.nextInt(200);
            waterPos = findWater(turtle, 24);
            return waterPos != null;
        }

        @Override
        public void startExecuting() {
            ticks = 0;
            moveToWater();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !turtle.isInWater() && waterPos != null && ticks <= 1200
                    && isWaterBlock(turtle.world, waterPos);
        }

        @Override
        public void updateTask() {
            ticks++;
            if (turtle.getNavigator().noPath() || ticks % 160 == 0) {
                moveToWater();
            }
        }

        private void moveToWater() {
            turtle.getNavigator().tryMoveToXYZ(waterPos.getX() + 0.5D, waterPos.getY(),
                    waterPos.getZ() + 0.5D, turtle.isChild() ? 2.0D : speed);
        }

        @Nullable
        private static BlockPos findWater(EntityTurtle turtle, int radius) {
            BlockPos origin = turtle.getPosition();
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = -2; dy <= 3; dy++) {
                        BlockPos candidate = origin.add(dx, dy, dz);
                        if (!isWaterBlock(turtle.world, candidate)) {
                            continue;
                        }
                        double distance = candidate.distanceSq(origin);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = candidate;
                        }
                    }
                }
            }
            return nearest;
        }
    }

    private static final class TurtleLandWanderGoal extends EntityAIWander {
        private final EntityTurtle turtle;

        private TurtleLandWanderGoal(EntityTurtle turtle, double speed, int chance) {
            super(turtle, speed, chance);
            this.turtle = turtle;
        }

        @Override
        public boolean shouldExecute() {
            return !turtle.isInWater() && !turtle.isGoingHome() && !turtle.hasEgg()
                    && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !turtle.isInWater() && !turtle.isGoingHome() && !turtle.hasEgg()
                    && super.shouldContinueExecuting();
        }
    }

    private static final class TurtleTravelGoal extends EntityAIBase {
        private final EntityTurtle turtle;
        private final double speed;
        @Nullable
        private BlockPos travelPos;
        private boolean stuck;

        private TurtleTravelGoal(EntityTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return turtle.isInWater() && !turtle.isGoingHome() && !turtle.hasEgg();
        }

        @Override
        public void startExecuting() {
            int x = turtle.rand.nextInt(1025) - 512;
            int y = turtle.rand.nextInt(9) - 4;
            int z = turtle.rand.nextInt(1025) - 512;
            if (turtle.posY + y > turtle.world.getSeaLevel() - 1) {
                y = 0;
            }
            travelPos = new BlockPos(turtle.posX + x, turtle.posY + y, turtle.posZ + z);
            stuck = false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !turtle.getNavigator().noPath() && !stuck
                    && !turtle.isGoingHome() && !turtle.hasEgg() && !turtle.isInLove();
        }

        @Override
        public void resetTask() {
            travelPos = null;
            stuck = false;
        }

        @Override
        public void updateTask() {
            if (travelPos == null) {
                stuck = true;
                return;
            }
            if (turtle.getNavigator().noPath()) {
                Vec3d target = new Vec3d(travelPos.getX() + 0.5D, travelPos.getY() + 0.5D,
                        travelPos.getZ() + 0.5D);
                Vec3d next = RandomPositionGenerator.findRandomTargetBlockTowards(turtle, 16, 3, target);
                if (next == null) {
                    next = RandomPositionGenerator.findRandomTargetBlockTowards(turtle, 8, 7, target);
                }
                if (next != null && !turtle.world.isAreaLoaded(new BlockPos(next), 34)) {
                    next = null;
                }
                if (next == null) {
                    stuck = true;
                    return;
                }
                turtle.getNavigator().tryMoveToXYZ(next.x, next.y, next.z, speed);
            }
        }
    }

    private static boolean isWaterBlock(World world, BlockPos pos) {
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    private static Set<Item> getSeagrassItems() {
        Set<Item> items = new LinkedHashSet<>();
        addRegisteredItem(items, FarmerFutureDelight.MODID, "seagrass");
        addRegisteredItem(items, "oe", "seagrass");
        addRegisteredItem(items, "futuremc", "seagrass");
        addRegisteredItem(items, "brewinandchewinlegacy", "seagrass");
        return items;
    }

    @Nullable
    private static Item getPrimarySeagrassItem() {
        Set<Item> items = getSeagrassItems();
        return items.isEmpty() ? null : items.iterator().next();
    }

    private static void addRegisteredItem(Set<Item> items, String namespace, String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, path));
        if (item != null && item != Items.AIR) {
            items.add(item);
        }
    }

    @Nullable
    private static BlockPos findNearestWater(EntityTurtle turtle, int horizontalRadius,
                                             int verticalRadius) {
        BlockPos origin = turtle.getPosition();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockPos candidate = origin.add(dx, dy, dz);
                    if (!isWaterBlock(turtle.world, candidate)) {
                        continue;
                    }
                    double distance = candidate.distanceSq(origin);
                    if (distance < nearestDistance) {
                        nearest = candidate;
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }
}
