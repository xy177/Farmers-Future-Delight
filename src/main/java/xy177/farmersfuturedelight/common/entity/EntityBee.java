package xy177.farmersfuturedelight.common.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockSweetBerryBush;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesBase;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityBee extends EntityAnimal implements EntityFlying {
    private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(EntityBee.class,
            DataSerializers.BYTE);
    private static final DataParameter<Integer> ANGER = EntityDataManager.createKey(EntityBee.class,
            DataSerializers.VARINT);

    private static final int FLAG_NECTAR = 1;
    private static final int FLAG_STUNG = 2;
    private static final int FLAG_POLLINATING = 4;
    private static final int FLAG_ROLLING = 8;

    private int ticksSinceSting;
    private int underWaterTicks;
    private int ticksSincePollination;
    private int cannotEnterHiveTicks;
    private int cropsGrownSincePollination;
    private int remainingCooldownBeforeLocatingNewHive;
    private int remainingCooldownBeforeLocatingNewFlower;
    private float rollAmount;
    private float previousRollAmount;
    @Nullable
    private BlockPos flowerPos;
    @Nullable
    private BlockPos hivePos;
    @Nullable
    private UUID angerTarget;
    private BeePollinateGoal pollinateGoal;
    private BeeGoToHiveGoal goToHiveGoal;

    public EntityBee(World worldIn) {
        super(worldIn);
        moveHelper = new EntityFlyHelper(this);
        setNoGravity(true);
        setSize(0.7F, 0.6F);
        setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        setPathPriority(PathNodeType.DAMAGE_FIRE, -1.0F);
        setPathPriority(PathNodeType.WATER, -1.0F);
        setPathPriority(PathNodeType.FENCE, -1.0F);
        remainingCooldownBeforeLocatingNewFlower = 20 + rand.nextInt(41);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(FLAGS, (byte) 0);
        dataManager.register(ANGER, 0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new BeeAttackGoal(this));
        tasks.addTask(1, new BeeEnterHiveGoal(this));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new BeeTemptGoal(this));
        pollinateGoal = new BeePollinateGoal(this);
        tasks.addTask(4, pollinateGoal);
        tasks.addTask(5, new EntityAIFollowParent(this, 1.25D));
        tasks.addTask(5, new BeeLocateHiveGoal(this));
        goToHiveGoal = new BeeGoToHiveGoal(this);
        tasks.addTask(5, goToHiveGoal);
        tasks.addTask(6, new BeeGoToKnownFlowerGoal(this));
        tasks.addTask(7, new BeeGrowCropGoal(this));
        tasks.addTask(8, new BeeWanderGoal(this));
        tasks.addTask(9, new EntityAISwimming(this));
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(11, new EntityAILookIdle(this));
        targetTasks.addTask(1, new BeeHurtByTarget(this));
        targetTasks.addTask(2, new BeeTargetGoal(this));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        PathNavigateFlying navigator = new PathNavigateFlying(this, worldIn);
        navigator.setCanOpenDoors(false);
        navigator.setCanEnterDoors(false);
        return navigator;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.6D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        updateRollAmount();
        if (world.isRemote) {
            if (hasNectar() && cropsGrownSincePollination < 10 && rand.nextFloat() < 0.05F) {
                int count = 1 + rand.nextInt(2);
                for (int i = 0; i < count; i++) {
                    FarmerFutureDelight.proxy.spawnHoneyDripParticle(world,
                            posX - 0.3D + rand.nextDouble() * 0.6D,
                            posY + height * 0.5D,
                            posZ - 0.3D + rand.nextDouble() * 0.6D);
                }
            }
            return;
        }

        if (cannotEnterHiveTicks > 0) {
            cannotEnterHiveTicks--;
        }
        if (remainingCooldownBeforeLocatingNewHive > 0) {
            remainingCooldownBeforeLocatingNewHive--;
        }
        if (remainingCooldownBeforeLocatingNewFlower > 0) {
            remainingCooldownBeforeLocatingNewFlower--;
        }
        if (ticksExisted % 20 == 0 && hivePos != null && !isHiveValid()) {
            dropHive();
        }
        if (hasStung()) {
            ticksSinceSting++;
            if (ticksSinceSting % 5 == 0
                    && rand.nextInt(MathHelper.clamp(1200 - ticksSinceSting, 1, 1200)) == 0) {
                attackEntityFrom(DamageSource.GENERIC, getHealth());
            }
        }
        underWaterTicks = isInWater() ? underWaterTicks + 1 : 0;
        if (underWaterTicks > 20) {
            attackEntityFrom(DamageSource.DROWN, 1.0F);
        }
        EntityLivingBase attackTarget = getAttackTarget();
        setRolling(getAnger() > 0 && !hasStung() && attackTarget != null
                && attackTarget.getDistanceSq(this) < 4.0D);
        if (getAnger() > 0) {
            setAnger(getAnger() - 1);
            if (getAnger() == 0) {
                stopBeingAngry();
            } else if (getAttackTarget() == null && angerTarget != null
                    && ticksExisted % 20 == 0) {
                restoreAngerTarget();
            }
        }
        if (!hasNectar()) {
            ticksSincePollination++;
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = target.attackEntityFrom(causeBeeDamage(this),
                (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (!hit) {
            return false;
        }
        if (target instanceof EntityLivingBase) {
            int seconds = world.getDifficulty() == EnumDifficulty.HARD ? 18
                    : world.getDifficulty() == EnumDifficulty.NORMAL ? 10 : 0;
            if (seconds > 0) {
                ((EntityLivingBase) target).addPotionEffect(
                        new PotionEffect(MobEffects.POISON, seconds * 20));
            }
        }
        setHasStung(true);
        stopBeingAngry();
        playSound(FFDSounds.BEE_STING, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        Entity attacker = source.getTrueSource();
        if (hurt) {
            setPollinating(false);
        }
        if (hurt && attacker instanceof EntityLivingBase && !hasStung()) {
            setBeeAttacker((EntityLivingBase) attacker, 400 + rand.nextInt(380));
        }
        return hurt;
    }

    public boolean setBeeAttacker(EntityLivingBase target, int duration) {
        if (target == null) {
            return false;
        }
        angerTarget = target.getUniqueID();
        setAnger(duration);
        setRevengeTarget(target);
        setAttackTarget(target);
        return true;
    }

    private void restoreAngerTarget() {
        EntityPlayer player = world.getPlayerEntityByUUID(angerTarget);
        if (player != null && player.isEntityAlive()) {
            setAttackTarget(player);
            return;
        }
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(48.0D))) {
            if (candidate.isEntityAlive() && angerTarget.equals(candidate.getUniqueID())) {
                setAttackTarget(candidate);
                return;
            }
        }
    }

    private void stopBeingAngry() {
        setAnger(0);
        angerTarget = null;
        setRevengeTarget(null);
        setAttackTarget(null);
    }

    public boolean canEnterHive() {
        TileEntity tile = hivePos == null ? null : world.getTileEntity(hivePos);
        return wantsToEnterHive() && tile instanceof TileEntityBeehive
                && !isTooFarAway(hivePos)
                && !((TileEntityBeehive) tile).isFireNearby()
                && !((TileEntityBeehive) tile).isFullOfBees();
    }

    private boolean wantsToEnterHive() {
        return cannotEnterHiveTicks <= 0 && !isPollinating() && !hasStung()
                && getAttackTarget() == null
                && (hasNectar() || !world.isDaytime() || world.isRaining()
                        || ticksSincePollination > 3600);
    }

    public void onHoneyDelivered() {
        setHasNectar(false);
        setPollinating(false);
        ticksSincePollination = 0;
        cropsGrownSincePollination = 0;
    }

    public boolean hasNectar() {
        return hasBeeFlag(FLAG_NECTAR);
    }

    public void setHasNectar(boolean hasNectar) {
        setBeeFlag(FLAG_NECTAR, hasNectar);
        if (hasNectar) {
            ticksSincePollination = 0;
        }
        if (!hasNectar) {
            setPollinating(false);
        }
    }

    public boolean hasStung() {
        return hasBeeFlag(FLAG_STUNG);
    }

    public void setHasStung(boolean hasStung) {
        setBeeFlag(FLAG_STUNG, hasStung);
    }

    public boolean isPollinating() {
        return hasBeeFlag(FLAG_POLLINATING);
    }

    public void setPollinating(boolean pollinating) {
        setBeeFlag(FLAG_POLLINATING, pollinating);
    }

    public float getRollAmount(float partialTicks) {
        return previousRollAmount + (rollAmount - previousRollAmount) * partialTicks;
    }

    private boolean isRolling() {
        return hasBeeFlag(FLAG_ROLLING);
    }

    private void setRolling(boolean rolling) {
        setBeeFlag(FLAG_ROLLING, rolling);
    }

    private void updateRollAmount() {
        previousRollAmount = rollAmount;
        rollAmount = isRolling() ? Math.min(1.0F, rollAmount + 0.2F)
                : Math.max(0.0F, rollAmount - 0.24F);
    }

    public int getAnger() {
        return dataManager.get(ANGER);
    }

    public void setAnger(int anger) {
        dataManager.set(ANGER, Math.max(0, anger));
    }

    @Nullable
    public BlockPos getHivePos() {
        return hivePos;
    }

    public void setHivePos(@Nullable BlockPos hivePos) {
        this.hivePos = hivePos == null ? null : hivePos.toImmutable();
    }

    @Nullable
    public BlockPos getFlowerPos() {
        return flowerPos;
    }

    public void setFlowerPos(@Nullable BlockPos flowerPos) {
        this.flowerPos = flowerPos == null ? null : flowerPos.toImmutable();
    }

    public void setCannotEnterHiveTicks(int ticks) {
        cannotEnterHiveTicks = Math.max(0, ticks);
    }

    private boolean isHiveValid() {
        return hivePos != null && !isTooFarAway(hivePos)
                && world.getTileEntity(hivePos) instanceof TileEntityBeehive;
    }

    private void dropHive() {
        hivePos = null;
        remainingCooldownBeforeLocatingNewHive = 200;
    }

    private void dropFlower() {
        flowerPos = null;
        remainingCooldownBeforeLocatingNewFlower = 20 + rand.nextInt(41);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block instanceof BlockDoublePlant) {
            BlockDoublePlant.EnumPlantType type = BlockDoublePlant.EnumPlantType.byMetadata(
                    stack.getMetadata() & 7);
            return type == BlockDoublePlant.EnumPlantType.SUNFLOWER
                    || type == BlockDoublePlant.EnumPlantType.SYRINGA
                    || type == BlockDoublePlant.EnumPlantType.ROSE
                    || type == BlockDoublePlant.EnumPlantType.PAEONIA;
        }
        return isBeeAttractiveBlock(block);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityBee(world);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return FFDSounds.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public float getEyeHeight() {
        return 0.35F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        writePos(compound, "HivePos", hivePos);
        writePos(compound, "FlowerPos", flowerPos);
        compound.setBoolean("HasNectar", hasNectar());
        compound.setBoolean("HasStung", hasStung());
        compound.setInteger("TicksSincePollination", ticksSincePollination);
        compound.setInteger("CannotEnterHiveTicks", cannotEnterHiveTicks);
        compound.setInteger("CropsGrownSincePollination", cropsGrownSincePollination);
        compound.setInteger("Anger", getAnger());
        if (angerTarget != null) {
            compound.setUniqueId("AngerTarget", angerTarget);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        hivePos = readPos(compound, "HivePos");
        flowerPos = readPos(compound, "FlowerPos");
        setHasNectar(compound.getBoolean("HasNectar"));
        setHasStung(compound.getBoolean("HasStung"));
        ticksSincePollination = compound.getInteger("TicksSincePollination");
        cannotEnterHiveTicks = compound.getInteger("CannotEnterHiveTicks");
        cropsGrownSincePollination = compound.getInteger("CropsGrownSincePollination");
        setAnger(compound.getInteger("Anger"));
        angerTarget = compound.hasUniqueId("AngerTarget") ? compound.getUniqueId("AngerTarget") : null;
    }

    private boolean hasBeeFlag(int flag) {
        return (dataManager.get(FLAGS) & flag) != 0;
    }

    private void setBeeFlag(int flag, boolean value) {
        byte flags = dataManager.get(FLAGS);
        flags = value ? (byte) (flags | flag) : (byte) (flags & ~flag);
        dataManager.set(FLAGS, flags);
    }

    private boolean isFlowerValid(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockDoublePlant) {
            BlockDoublePlant.EnumPlantType type = state.getValue(BlockDoublePlant.VARIANT);
            if (type == BlockDoublePlant.EnumPlantType.SUNFLOWER) {
                return state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER;
            }
            return type == BlockDoublePlant.EnumPlantType.SYRINGA
                    || type == BlockDoublePlant.EnumPlantType.ROSE
                    || type == BlockDoublePlant.EnumPlantType.PAEONIA;
        }
        return isBeeAttractiveBlock(state.getBlock());
    }

    private static boolean isBeeAttractiveBlock(Block block) {
        return block instanceof BlockFlower || block == Blocks.CHORUS_FLOWER
                || block == FFDBlocks.FLOWERING_AZALEA
                || block == FFDBlocks.FLOWERING_AZALEA_LEAVES
                || block == FFDBlocks.SPORE_BLOSSOM;
    }

    @Nullable
    private BlockPos findNearestFlower(int radius) {
        BlockPos origin = getPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-radius, -radius, -radius),
                origin.add(radius, radius, radius))) {
            int manhattan = Math.abs(pos.getX() - origin.getX())
                    + Math.abs(pos.getY() - origin.getY())
                    + Math.abs(pos.getZ() - origin.getZ());
            if (manhattan > radius || !isFlowerValid(pos)) {
                continue;
            }
            candidates.add(pos.toImmutable());
        }
        candidates.sort((first, second) -> Double.compare(
                first.distanceSq(origin), second.distanceSq(origin)));
        for (BlockPos candidate : candidates) {
            if (canReach(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean canReach(BlockPos target) {
        Path path = getNavigator().getPathToPos(target);
        PathPoint end = path == null ? null : path.getFinalPathPoint();
        return end != null && target.distanceSq(new BlockPos(end.x, end.y, end.z)) <= 1.0D;
    }

    @Nullable
    private BlockPos findNearestHive(int radius) {
        return findNearestHive(radius, null);
    }

    @Nullable
    private BlockPos findNearestHive(int radius, @Nullable List<BlockPos> excluded) {
        BlockPos origin = getPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        double maxDistanceSq = radius * radius;
        for (TileEntity tile : world.loadedTileEntityList) {
            if (!(tile instanceof TileEntityBeehive) || ((TileEntityBeehive) tile).isFullOfBees()
                    || ((TileEntityBeehive) tile).isFireNearby()
                    || excluded != null && excluded.contains(tile.getPos())) {
                continue;
            }
            double distance = tile.getPos().distanceSq(origin);
            if (distance <= maxDistanceSq && distance < closestDistance) {
                closest = tile.getPos().toImmutable();
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void moveTo(BlockPos target, double speed) {
        getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY() + 0.5D,
                target.getZ() + 0.5D, speed);
    }

    private void moveToFlower(BlockPos target, double speed) {
        getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY() + 0.6D,
                target.getZ() + 0.5D, speed);
    }

    private boolean isTooFarAway(BlockPos target) {
        return target == null || target.distanceSq(getPosition()) >= 48.0D * 48.0D;
    }

    private void growNearbyCrops() {
        if (!hasNectar() || cropsGrownSincePollination >= 10
                || rand.nextInt(30) != 0 || hivePos == null
                || !(world.getTileEntity(hivePos) instanceof TileEntityBeehive)) {
            return;
        }
        BlockPos origin = getPosition();
        for (int y = 1; y <= 2; y++) {
            BlockPos pos = origin.down(y);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!isVanillaBeeGrowable(block)) {
                continue;
            }
            IBlockState grown = growOneStage(state);
            if (grown != null) {
                world.setBlockState(pos, grown, 3);
            } else if (block instanceof BlockCaveVinesBase && block instanceof IGrowable
                    && ((IGrowable) block).canGrow(world, pos, state, false)) {
                ((IGrowable) block).grow(world, rand, pos, state);
            } else {
                continue;
            }
            if (world instanceof WorldServer) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        15, 0.5D, 0.5D, 0.5D, 0.0D);
            }
            cropsGrownSincePollination++;
        }
    }

    private static boolean isVanillaBeeGrowable(Block block) {
        return block instanceof BlockCrops || block instanceof BlockStem
                || block instanceof BlockSweetBerryBush || block instanceof BlockCaveVinesBase;
    }

    @Nullable
    private static IBlockState growOneStage(IBlockState state) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (!(property instanceof PropertyInteger) || !"age".equals(property.getName())) {
                continue;
            }
            PropertyInteger age = (PropertyInteger) property;
            int current = state.getValue(age);
            int maximum = 0;
            for (Integer allowed : age.getAllowedValues()) {
                maximum = Math.max(maximum, allowed);
            }
            return current < maximum ? state.withProperty(age, current + 1) : null;
        }
        return null;
    }

    private static void writePos(NBTTagCompound compound, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger("X", pos.getX());
        posTag.setInteger("Y", pos.getY());
        posTag.setInteger("Z", pos.getZ());
        compound.setTag(key, posTag);
    }

    @Nullable
    private static BlockPos readPos(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 10)) {
            return null;
        }
        NBTTagCompound posTag = compound.getCompoundTag(key);
        return new BlockPos(posTag.getInteger("X"), posTag.getInteger("Y"), posTag.getInteger("Z"));
    }

    public static EntityDamageSource causeBeeDamage(EntityBee bee) {
        EntityDamageSource damage = new EntityDamageSource("bee_sting", bee);
        return damage;
    }

    private static final class BeeHurtByTarget extends EntityAIHurtByTarget {
        private final EntityBee bee;

        private BeeHurtByTarget(EntityBee bee) {
            super(bee, true);
            this.bee = bee;
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            EntityLivingBase target = bee.getAttackTarget();
            if (target != null && bee.getAnger() <= 0) {
                bee.setBeeAttacker(target, 400 + bee.rand.nextInt(380));
            }
        }

        @Override
        protected void setEntityAttackTarget(net.minecraft.entity.EntityCreature creatureIn,
                                             EntityLivingBase target) {
            if (creatureIn instanceof EntityBee && creatureIn.canEntityBeSeen(target)) {
                EntityBee other = (EntityBee) creatureIn;
                other.setBeeAttacker(target, 400 + other.rand.nextInt(380));
            }
        }
    }

    private static final class BeeAttackGoal extends EntityAIAttackMelee {
        private final EntityBee bee;

        private BeeAttackGoal(EntityBee bee) {
            super(bee, 1.4D, true);
            this.bee = bee;
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() > 0 && !bee.hasStung() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return bee.getAnger() > 0 && !bee.hasStung() && super.shouldContinueExecuting();
        }
    }

    private static final class BeeTargetGoal extends EntityAINearestAttackableTarget<EntityPlayer> {
        private final EntityBee bee;

        private BeeTargetGoal(EntityBee bee) {
            super(bee, EntityPlayer.class, 10, true, false,
                    player -> bee.angerTarget != null
                            && bee.angerTarget.equals(player.getUniqueID()));
            this.bee = bee;
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() > 0 && !bee.hasStung() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return bee.getAnger() > 0 && !bee.hasStung() && super.shouldContinueExecuting();
        }
    }

    private static final class BeeEnterHiveGoal extends EntityAIBase {
        private final EntityBee bee;

        private BeeEnterHiveGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (bee.getAnger() > 0 || bee.hivePos == null || !bee.wantsToEnterHive()
                    || bee.getDistanceSqToCenter(bee.hivePos) >= 4.0D) {
                return false;
            }
            TileEntity tile = bee.world.getTileEntity(bee.hivePos);
            if (!(tile instanceof TileEntityBeehive)) {
                bee.dropHive();
                return false;
            }
            if (((TileEntityBeehive) tile).isFullOfBees()) {
                bee.dropHive();
                return false;
            }
            return !((TileEntityBeehive) tile).isFireNearby();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            TileEntity tile = bee.world.getTileEntity(bee.hivePos);
            if (tile instanceof TileEntityBeehive) {
                ((TileEntityBeehive) tile).tryEnterHive(bee, bee.hasNectar());
            }
        }
    }

    private static final class BeeTemptGoal extends EntityAIBase {
        private final EntityBee bee;
        @Nullable
        private EntityPlayer player;
        private int cooldown;

        private BeeTemptGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            player = bee.world.getClosestPlayerToEntity(bee, 10.0D);
            return player != null && (bee.isBreedingItem(player.getHeldItemMainhand())
                    || bee.isBreedingItem(player.getHeldItemOffhand()));
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void resetTask() {
            player = null;
            bee.getNavigator().clearPath();
            cooldown = 100;
        }

        @Override
        public void updateTask() {
            if (player == null) {
                return;
            }
            bee.getLookHelper().setLookPositionWithEntity(player,
                    bee.getHorizontalFaceSpeed() + 20.0F, bee.getVerticalFaceSpeed());
            if (bee.getDistanceSq(player) < 6.25D) {
                bee.getNavigator().clearPath();
            } else {
                bee.getNavigator().tryMoveToEntityLiving(player, 1.25D);
            }
        }
    }

    private static final class BeePollinateGoal extends EntityAIBase {
        private final EntityBee bee;
        private int successfulPollinatingTicks;
        private int pollinatingTicks;
        private int lastSoundTick;
        private boolean pollinating;

        private BeePollinateGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (bee.getAnger() > 0 || bee.remainingCooldownBeforeLocatingNewFlower > 0
                    || bee.hasNectar() || bee.world.isRaining()) {
                return false;
            }
            bee.flowerPos = bee.findNearestFlower(5);
            if (bee.flowerPos == null) {
                bee.dropFlower();
                return false;
            }
            bee.moveToFlower(bee.flowerPos, 1.2D);
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (!pollinating || bee.getAnger() > 0 || bee.flowerPos == null
                    || !bee.isFlowerValid(bee.flowerPos) || bee.world.isRaining()
                    || pollinatingTicks > 600) {
                return false;
            }
            return successfulPollinatingTicks <= 400 || bee.rand.nextFloat() < 0.2F;
        }

        @Override
        public void startExecuting() {
            successfulPollinatingTicks = 0;
            pollinatingTicks = 0;
            lastSoundTick = 0;
            pollinating = true;
            bee.setPollinating(true);
            bee.ticksSincePollination = 0;
        }

        @Override
        public void resetTask() {
            if (successfulPollinatingTicks > 400) {
                bee.setHasNectar(true);
            }
            pollinating = false;
            bee.setPollinating(false);
            bee.getNavigator().clearPath();
            bee.remainingCooldownBeforeLocatingNewFlower = 200;
        }

        @Override
        public void updateTask() {
            if (bee.flowerPos == null) {
                return;
            }
            if (++pollinatingTicks > 600) {
                bee.dropFlower();
                pollinating = false;
                bee.setPollinating(false);
                return;
            }
            double x = bee.flowerPos.getX() + 0.5D;
            double y = bee.flowerPos.getY() + 0.6D;
            double z = bee.flowerPos.getZ() + 0.5D;
            if (bee.getDistanceSq(x, y, z) > 1.0D) {
                bee.moveToFlower(bee.flowerPos, 1.2D);
                return;
            }
            bee.getNavigator().clearPath();
            bee.getMoveHelper().setMoveTo(x + (bee.rand.nextFloat() * 2.0F - 1.0F) / 3.0F,
                    y, z + (bee.rand.nextFloat() * 2.0F - 1.0F) / 3.0F, 0.35D);
            bee.getLookHelper().setLookPosition(x, y, z, 10.0F, bee.getVerticalFaceSpeed());
            successfulPollinatingTicks++;
            if (bee.rand.nextFloat() < 0.05F
                    && successfulPollinatingTicks > lastSoundTick + 60) {
                lastSoundTick = successfulPollinatingTicks;
                bee.playSound(FFDSounds.BEE_POLLINATE, 1.0F, 1.0F);
            }
        }
    }

    private static final class BeeLocateHiveGoal extends EntityAIBase {
        private final EntityBee bee;

        private BeeLocateHiveGoal(EntityBee bee) {
            this.bee = bee;
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() == 0 && bee.remainingCooldownBeforeLocatingNewHive <= 0
                    && bee.hivePos == null && bee.wantsToEnterHive();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            bee.remainingCooldownBeforeLocatingNewHive = 200;
            List<BlockPos> blacklist = bee.goToHiveGoal == null
                    ? null : bee.goToHiveGoal.blacklistedTargets;
            bee.hivePos = bee.findNearestHive(20, blacklist);
            if (bee.hivePos == null && blacklist != null && !blacklist.isEmpty()) {
                blacklist.clear();
                bee.hivePos = bee.findNearestHive(20);
            }
        }
    }

    private static final class BeeGoToHiveGoal extends EntityAIBase {
        private final EntityBee bee;
        private final List<BlockPos> blacklistedTargets = new ArrayList<>();
        private int travellingTicks;
        private int stuckTicks;
        private double lastDistanceSq = Double.MAX_VALUE;

        private BeeGoToHiveGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() == 0 && bee.hivePos != null && bee.isHiveValid()
                    && bee.wantsToEnterHive()
                    && bee.getDistanceSqToCenter(bee.hivePos) >= 4.0D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void startExecuting() {
            travellingTicks = 0;
            stuckTicks = 0;
            lastDistanceSq = Double.MAX_VALUE;
        }

        @Override
        public void resetTask() {
            bee.getNavigator().clearPath();
            travellingTicks = 0;
            stuckTicks = 0;
        }

        @Override
        public void updateTask() {
            if (bee.hivePos == null || ++travellingTicks > 2400) {
                dropAndBlacklistHive();
                return;
            }
            double distanceSq = bee.getDistanceSqToCenter(bee.hivePos);
            if (distanceSq + 0.01D < lastDistanceSq) {
                stuckTicks = 0;
            } else if (++stuckTicks > 60) {
                bee.dropHive();
                return;
            }
            lastDistanceSq = distanceSq;
            if (!bee.getNavigator().noPath()) {
                return;
            }
            if (distanceSq < 16.0D * 16.0D) {
                if (!bee.getNavigator().tryMoveToXYZ(bee.hivePos.getX() + 0.5D,
                        bee.hivePos.getY() + 0.5D, bee.hivePos.getZ() + 0.5D, 1.0D)) {
                    dropAndBlacklistHive();
                }
                return;
            }
            Vec3d target = RandomPositionGenerator.findRandomTargetBlockTowards(bee, 6, 8,
                    new Vec3d(bee.hivePos).addVector(0.5D, 0.5D, 0.5D));
            if (target != null) {
                bee.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, 1.0D);
            }
        }

        private void dropAndBlacklistHive() {
            if (bee.hivePos != null) {
                blacklistedTargets.add(bee.hivePos.toImmutable());
                while (blacklistedTargets.size() > 3) {
                    blacklistedTargets.remove(0);
                }
            }
            bee.dropHive();
        }
    }

    private static final class BeeGoToKnownFlowerGoal extends EntityAIBase {
        private final EntityBee bee;
        private int travellingTicks;

        private BeeGoToKnownFlowerGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() == 0 && bee.flowerPos != null
                    && bee.ticksSincePollination > 600 && !bee.isTooFarAway(bee.flowerPos)
                    && bee.getDistanceSqToCenter(bee.flowerPos) >= 4.0D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void startExecuting() {
            travellingTicks = 0;
        }

        @Override
        public void resetTask() {
            travellingTicks = 0;
            bee.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (bee.flowerPos == null || ++travellingTicks > 2400
                    || !bee.isFlowerValid(bee.flowerPos) || bee.isTooFarAway(bee.flowerPos)) {
                bee.dropFlower();
                return;
            }
            if (!bee.getNavigator().noPath()) {
                return;
            }
            Vec3d target = RandomPositionGenerator.findRandomTargetBlockTowards(bee, 6, 8,
                    new Vec3d(bee.flowerPos).addVector(0.5D, 0.6D, 0.5D));
            if (target != null) {
                bee.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, 1.0D);
            }
        }
    }

    private static final class BeeGrowCropGoal extends EntityAIBase {
        private final EntityBee bee;

        private BeeGrowCropGoal(EntityBee bee) {
            this.bee = bee;
        }

        @Override
        public boolean shouldExecute() {
            return bee.getAnger() == 0 && bee.cropsGrownSincePollination < 10
                    && bee.rand.nextFloat() >= 0.3F && bee.hasNectar() && bee.isHiveValid();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            bee.growNearbyCrops();
        }
    }

    private static final class BeeWanderGoal extends EntityAIBase {
        private final EntityBee bee;

        private BeeWanderGoal(EntityBee bee) {
            this.bee = bee;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return bee.getNavigator().noPath() && bee.rand.nextInt(10) == 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !bee.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            Vec3d target;
            int threshold = bee.hivePos != null || bee.flowerPos != null ? 24 : 32;
            if (bee.isHiveValid() && bee.getDistanceSqToCenter(bee.hivePos) >= threshold * threshold) {
                target = RandomPositionGenerator.findRandomTargetBlockTowards(bee, 8, 7,
                        new Vec3d(bee.hivePos).addVector(0.5D, 0.5D, 0.5D));
            } else {
                target = RandomPositionGenerator.findRandomTarget(bee, 8, 7);
            }
            if (target != null) {
                bee.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, 1.0D);
            }
        }
    }
}
