package xy177.farmersfuturedelight.common.entity;

import java.util.Locale;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.worldgen.MapGenLushCaves;

public class EntityAxolotl extends EntityAnimal {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(
            EntityAxolotl.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> PLAYING_DEAD = EntityDataManager.createKey(
            EntityAxolotl.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(
            EntityAxolotl.class, DataSerializers.BOOLEAN);

    private static final int PLAY_DEAD_TICKS = 200;
    private static final int HUNTING_COOLDOWN_TICKS = 2400;
    private static final int MAX_AIR = 6000;
    private static final ResourceLocation FUTURE_MC_TROPICAL_FISH_BUCKET =
            new ResourceLocation("futuremc", "tropical_fish_bucket");

    private final PathNavigateSwimmer waterNavigator;
    private final PathNavigateGround groundNavigator;
    private boolean usingWaterNavigator;
    private int playingDeadTicks;
    private int huntingCooldown;
    @Nullable
    private EntityLivingBase observedAttackTarget;

    public EntityAxolotl(World world) {
        super(world);
        setSize(0.75F, 0.42F);
        stepHeight = 1.0F;
        setPathPriority(PathNodeType.WATER, 0.0F);
        setPathPriority(PathNodeType.WALKABLE, 0.0F);
        waterNavigator = new PathNavigateSwimmer(this, world);
        groundNavigator = new PathNavigateGround(this, world);
        groundNavigator.setCanSwim(true);
        navigator = groundNavigator;
        moveHelper = new AxolotlMoveHelper(this);
        setAir(MAX_AIR);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, Variant.LUCY.id);
        dataManager.register(PLAYING_DEAD, false);
        dataManager.register(FROM_BUCKET, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(8.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new PlayDeadGoal(this));
        tasks.addTask(1, new AxolotlMeleeGoal(this));
        tasks.addTask(2, new EntityAIMate(this, 0.2D));
        tasks.addTask(3, new AxolotlTemptGoal(this));
        tasks.addTask(4, new EntityAIFollowParent(this, 0.6D));
        tasks.addTask(5, new FindWaterGoal(this));
        tasks.addTask(6, new AxolotlSwimWanderGoal(this, 0.5D, 10));
        tasks.addTask(7, new AxolotlLandWanderGoal(this, 0.15D, 40));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(9, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this,
                EntityLivingBase.class, 10, true, false,
                new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(@Nullable EntityLivingBase target) {
                        return isValidAxolotlTarget(target);
                    }
                }));
    }

    @Override
    public void onUpdate() {
        updateNavigation();
        super.onUpdate();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) {
            return;
        }

        if (huntingCooldown > 0) {
            huntingCooldown--;
        }
        if ((!isInWater() || isInLove()) && playingDeadTicks > 0) {
            playingDeadTicks = 0;
        }
        if (playingDeadTicks > 0) {
            playingDeadTicks--;
            setPlayingDead(true);
            setAttackTarget(null);
            navigator.clearPath();
            motionX *= 0.8D;
            motionY *= 0.8D;
            motionZ *= 0.8D;
        } else {
            setPlayingDead(false);
        }
        updateAirSupply();
        updateFinishedTarget();
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 1;
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

    private void updateAirSupply() {
        if (isEntityAlive() && !isWet()) {
            setAir(getAir() - 1);
            if (getAir() <= -20) {
                setAir(0);
                attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else {
            setAir(MAX_AIR);
        }
    }

    public void rehydrate() {
        setAir(Math.min(MAX_AIR, getAir() + 1800));
    }

    private void updateFinishedTarget() {
        EntityLivingBase current = getAttackTarget();
        if (observedAttackTarget != null && observedAttackTarget != current
                && !observedAttackTarget.isEntityAlive()) {
            applySupportingEffects(observedAttackTarget);
        }
        if (observedAttackTarget != null && observedAttackTarget != current) {
            huntingCooldown = HUNTING_COOLDOWN_TICKS;
        }
        observedAttackTarget = current;
    }

    private void applySupportingEffects(EntityLivingBase defeatedTarget) {
        DamageSource lastDamage = defeatedTarget.getLastDamageSource();
        Entity source = lastDamage == null ? null : lastDamage.getTrueSource();
        if (!(source instanceof EntityPlayer) || getDistanceSq(source) > 400.0D) {
            return;
        }
        EntityPlayer player = (EntityPlayer) source;
        PotionEffect regeneration = player.getActivePotionEffect(MobEffects.REGENERATION);
        if (regeneration == null || regeneration.getDuration() < 2400) {
            int previous = regeneration == null ? 0 : regeneration.getDuration();
            player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION,
                    Math.min(2400, previous + 100), 0));
        }
        player.removePotionEffect(MobEffects.MINING_FATIGUE);
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            FFDAdvancements.KILL_AXOLOTL_TARGET.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) player);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        float healthBefore = getHealth();
        boolean shouldPlayDead = !isAIDisabled() && rand.nextInt(3) == 0
                && (rand.nextInt(3) < amount || healthBefore / getMaxHealth() < 0.5F)
                && amount < healthBefore && isInWater()
                && (source.getTrueSource() != null || source.getImmediateSource() != null)
                && !isPlayingDead();
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt && shouldPlayDead && isEntityAlive()) {
            playingDeadTicks = PLAY_DEAD_TICKS;
            setPlayingDead(true);
            addPotionEffect(new PotionEffect(MobEffects.REGENERATION, PLAY_DEAD_TICKS, 0));
            clearAttackersWhilePlayingDead();
        }
        return hurt;
    }

    private void clearAttackersWhilePlayingDead() {
        for (EntityLiving attacker : world.getEntitiesWithinAABB(EntityLiving.class,
                getEntityBoundingBox().grow(64.0D))) {
            if (attacker.getAttackTarget() == this) {
                attacker.setAttackTarget(null);
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (isPlayingDead()) {
            return false;
        }
        setLastAttackedEntity(entityIn);
        float damage = (float) getEntityAttribute(
                SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        boolean attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (attacked) {
            playSound(FFDSounds.AXOLOTL_ATTACK, 1.0F, 1.0F);
        }
        return attacked;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isServerWorld() && isInWater()) {
            moveRelative(strafe, vertical, forward, 0.1F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.9D;
            motionY *= 0.9D;
            motionZ *= 0.9D;
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
    public boolean isNotColliding() {
        return world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Override
    protected boolean canDespawn() {
        return !isFromBucket() && !hasCustomName();
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return FFDEntities.isAxolotlEnabled()
                && world.provider.getDimension() == 0
                && world.getBlockState(pos).getMaterial() == Material.WATER
                && world.getBlockState(pos.down()).getBlock() == Blocks.CLAY
                && MapGenLushCaves.isPositionInLushCave(world, pos)
                && world.checkNoEntityCollision(getEntityBoundingBox(), this)
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        AxolotlGroupData group;
        if (livingData instanceof AxolotlGroupData) {
            group = (AxolotlGroupData) livingData;
        } else {
            group = new AxolotlGroupData(Variant.randomCommon(rand), Variant.randomCommon(rand));
        }
        setVariant(group.randomVariant(rand));
        if (group.groupSize >= 2) {
            setGrowingAge(-24000);
        }
        group.groupSize++;
        super.onInitialSpawn(difficulty, group);
        return group;
    }

    @Nullable
    @Override
    public EntityAxolotl createChild(EntityAgeable ageable) {
        EntityAxolotl baby = new EntityAxolotl(world);
        Variant childVariant;
        if (rand.nextInt(1200) == 0) {
            childVariant = Variant.BLUE;
        } else if (ageable instanceof EntityAxolotl && rand.nextBoolean()) {
            childVariant = ((EntityAxolotl) ageable).getVariant();
        } else {
            childVariant = getVariant();
        }
        baby.setVariant(childVariant);
        baby.enablePersistence();
        return baby;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item fishBucket = getTropicalFishBucket();
        if (fishBucket != null) {
            return stack.getItem() == fishBucket;
        }
        return stack.getItem() == Items.FISH
                && ItemFishFood.FishType.byItemStack(stack)
                        == ItemFishFood.FishType.CLOWNFISH;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.WATER_BUCKET) {
            if (!world.isRemote) {
                captureInBucket(player, hand);
            }
            return true;
        }

        boolean bucketFood = isTropicalFishBucket(held);
        boolean interacted = super.processInteract(player, hand);
        if (interacted && bucketFood && !world.isRemote && !player.capabilities.isCreativeMode) {
            if (held.isEmpty()) {
                player.setHeldItem(hand, new ItemStack(Items.WATER_BUCKET));
            } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.WATER_BUCKET))) {
                player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
            }
        }
        return interacted;
    }

    private boolean isTropicalFishBucket(ItemStack stack) {
        Item fishBucket = getTropicalFishBucket();
        return fishBucket != null && !stack.isEmpty() && stack.getItem() == fishBucket;
    }

    @Nullable
    private static Item getTropicalFishBucket() {
        return ForgeRegistries.ITEMS.getValue(FUTURE_MC_TROPICAL_FISH_BUCKET);
    }

    private void captureInBucket(EntityPlayer player, EnumHand hand) {
        ItemStack bucket = FFDItems.effectiveStack(FFDItems.AXOLOTL_BUCKET);
        if (bucket.isEmpty()) {
            return;
        }
        writeToBucket(bucket);
        if (hasCustomName()) {
            bucket.setStackDisplayName(getCustomNameTag());
        }
        playSound(FFDSounds.BUCKET_FILL_AXOLOTL, 1.0F, 1.0F);
        if (player.capabilities.isCreativeMode) {
            if (!player.inventory.addItemStackToInventory(bucket)) {
                player.dropItem(bucket, false);
            }
        } else {
            player.setHeldItem(hand, bucket);
        }
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            FFDAdvancements.AXOLOTL_IN_A_BUCKET.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) player);
        }
        setDead();
    }

    public void writeToBucket(ItemStack bucket) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("Variant", getVariant().id);
        data.setInteger("Age", getGrowingAge());
        data.setInteger("HuntingCooldown", huntingCooldown);
        data.setFloat("Health", getHealth());
        bucket.setTagInfo("BucketEntityTag", data);
    }

    public void readFromBucket(ItemStack bucket) {
        if (bucket.hasTagCompound() && bucket.getTagCompound().hasKey("BucketEntityTag", 10)) {
            NBTTagCompound data = bucket.getTagCompound().getCompoundTag("BucketEntityTag");
            setVariant(Variant.byId(data.getInteger("Variant")));
            setGrowingAge(data.getInteger("Age"));
            huntingCooldown = Math.max(0, data.getInteger("HuntingCooldown"));
            if (data.hasKey("Health", 5)) {
                setHealth(MathHelper.clamp(data.getFloat("Health"), 1.0F, getMaxHealth()));
            }
        }
        if (bucket.hasDisplayName()) {
            setCustomNameTag(bucket.getDisplayName());
        }
        setFromBucket(true);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", getVariant().id);
        compound.setBoolean("FromBucket", isFromBucket());
        compound.setInteger("PlayingDeadTicks", playingDeadTicks);
        compound.setInteger("HuntingCooldown", huntingCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setVariant(Variant.byId(compound.getInteger("Variant")));
        setFromBucket(compound.getBoolean("FromBucket"));
        playingDeadTicks = Math.max(0, compound.getInteger("PlayingDeadTicks"));
        huntingCooldown = Math.max(0, compound.getInteger("HuntingCooldown"));
        setPlayingDead(playingDeadTicks > 0);
    }

    @Override
    public void setScaleForAge(boolean child) {
        setSize(child ? 0.375F : 0.75F, child ? 0.21F : 0.42F);
    }

    public Variant getVariant() {
        return Variant.byId(dataManager.get(VARIANT));
    }

    public void setVariant(Variant variant) {
        dataManager.set(VARIANT, variant == null ? Variant.LUCY.id : variant.id);
    }

    public boolean isPlayingDead() {
        return dataManager.get(PLAYING_DEAD);
    }

    private void setPlayingDead(boolean playingDead) {
        if (dataManager.get(PLAYING_DEAD) != playingDead) {
            dataManager.set(PLAYING_DEAD, playingDead);
        }
    }

    public boolean isFromBucket() {
        return dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        dataManager.set(FROM_BUCKET, fromBucket);
        if (fromBucket) {
            enablePersistence();
        }
    }

    public int getHuntingCooldown() {
        return huntingCooldown;
    }

    private boolean isValidAxolotlTarget(@Nullable EntityLivingBase target) {
        if (target == null || target == this || target instanceof EntityAxolotl
                || target instanceof EntityPlayer || !target.isEntityAlive()
                || !target.isInWater() || getDistanceSq(target) > 64.0D || isPlayingDead()
                || isInLove()) {
            return false;
        }
        return isAlwaysHostile(target) || huntingCooldown <= 0 && isHuntTarget(target);
    }

    private static boolean isAlwaysHostile(EntityLivingBase target) {
        if (target instanceof EntityGuardian) {
            return true;
        }
        ResourceLocation id = entityId(target);
        return id != null && "drowned".equals(id.getResourcePath())
                && ("futuremc".equals(id.getResourceDomain())
                        || "oe".equals(id.getResourceDomain()));
    }

    private static boolean isHuntTarget(EntityLivingBase target) {
        if (target instanceof EntitySquid || target instanceof EntityGlowSquid) {
            return true;
        }
        ResourceLocation id = entityId(target);
        if (id == null) {
            return false;
        }
        String namespace = id.getResourceDomain();
        String path = id.getResourcePath();
        if ("futuremc".equals(namespace) || "oe".equals(namespace)) {
            return "tropical_fish".equals(path) || "pufferfish".equals(path)
                    || "salmon".equals(path) || "cod".equals(path)
                    || "glow_squid".equals(path);
        }
        return FarmerFutureDelight.MODID.equals(namespace) && "glow_squid".equals(path);
    }

    @Nullable
    private static ResourceLocation entityId(Entity entity) {
        ResourceLocation id = net.minecraft.entity.EntityList.getKey(entity);
        return id == null ? null : new ResourceLocation(
                id.getResourceDomain().toLowerCase(Locale.ROOT),
                id.getResourcePath().toLowerCase(Locale.ROOT));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isPlayingDead()) {
            return null;
        }
        return isInWater() ? FFDSounds.AXOLOTL_IDLE_WATER : FFDSounds.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return FFDSounds.AXOLOTL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.AXOLOTL_DEATH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return FFDSounds.AXOLOTL_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return FFDSounds.AXOLOTL_SPLASH;
    }

    public enum Variant {
        LUCY(0, "lucy", true),
        WILD(1, "wild", true),
        GOLD(2, "gold", true),
        CYAN(3, "cyan", true),
        BLUE(4, "blue", false);

        private final int id;
        private final String name;
        private final boolean common;

        Variant(int id, String name, boolean common) {
            this.id = id;
            this.name = name;
            this.common = common;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static Variant byId(int id) {
            for (Variant variant : values()) {
                if (variant.id == id) {
                    return variant;
                }
            }
            return LUCY;
        }

        private static Variant randomCommon(java.util.Random random) {
            Variant[] commonVariants = {LUCY, WILD, GOLD, CYAN};
            return commonVariants[random.nextInt(commonVariants.length)];
        }
    }

    public static final class AxolotlGroupData implements IEntityLivingData {
        private final Variant[] variants;
        private int groupSize;

        private AxolotlGroupData(Variant... variants) {
            this.variants = variants;
        }

        private Variant randomVariant(java.util.Random random) {
            return variants[random.nextInt(variants.length)];
        }
    }

    private static final class AxolotlMoveHelper extends EntityMoveHelper {
        private final EntityAxolotl axolotl;

        private AxolotlMoveHelper(EntityAxolotl axolotl) {
            super(axolotl);
            this.axolotl = axolotl;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (axolotl.isPlayingDead()) {
                axolotl.setAIMoveSpeed(0.0F);
                axolotl.setMoveForward(0.0F);
                return;
            }
            if (action == Action.MOVE_TO && axolotl.isInWater()
                    && !axolotl.getNavigator().noPath()) {
                double dx = posX - axolotl.posX;
                double dy = posY - axolotl.posY;
                double dz = posZ - axolotl.posZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance < 1.0E-5D) {
                    axolotl.setAIMoveSpeed(0.0F);
                    return;
                }
                float targetYaw = (float) (MathHelper.atan2(dz, dx) * 57.295776D) - 90.0F;
                axolotl.rotationYaw = limitAngle(axolotl.rotationYaw, targetYaw, 85.0F);
                axolotl.renderYawOffset = axolotl.rotationYaw;
                float movement = (float) (speed * axolotl.getEntityAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                axolotl.setAIMoveSpeed(movement * 0.5F);
                axolotl.motionY += axolotl.getAIMoveSpeed() * dy / distance * 0.1D;
                return;
            }
            super.onUpdateMoveHelper();
        }
    }

    private static final class PlayDeadGoal extends EntityAIBase {
        private final EntityAxolotl axolotl;

        private PlayDeadGoal(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return axolotl.playingDeadTicks > 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void startExecuting() {
            axolotl.navigator.clearPath();
            axolotl.setAttackTarget(null);
        }
    }

    private static final class AxolotlMeleeGoal extends EntityAIAttackMelee {
        private final EntityAxolotl axolotl;

        private AxolotlMeleeGoal(EntityAxolotl axolotl) {
            super(axolotl, 0.6D, true);
            this.axolotl = axolotl;
        }

        @Override
        public boolean shouldExecute() {
            return !axolotl.isPlayingDead() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !axolotl.isPlayingDead() && super.shouldContinueExecuting();
        }
    }

    private static final class AxolotlTemptGoal extends EntityAIBase {
        private final EntityAxolotl axolotl;
        @Nullable
        private EntityPlayer player;

        private AxolotlTemptGoal(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (axolotl.isPlayingDead()) {
                return false;
            }
            player = axolotl.world.getClosestPlayerToEntity(axolotl, 10.0D);
            return player != null && (axolotl.isBreedingItem(player.getHeldItemMainhand())
                    || axolotl.isBreedingItem(player.getHeldItemOffhand()));
        }

        @Override
        public boolean shouldContinueExecuting() {
            return player != null && player.isEntityAlive() && axolotl.getDistanceSq(player) <= 100.0D
                    && (axolotl.isBreedingItem(player.getHeldItemMainhand())
                    || axolotl.isBreedingItem(player.getHeldItemOffhand()));
        }

        @Override
        public void resetTask() {
            player = null;
            axolotl.navigator.clearPath();
        }

        @Override
        public void updateTask() {
            if (player != null) {
                axolotl.getLookHelper().setLookPositionWithEntity(player, 30.0F, 30.0F);
                axolotl.navigator.tryMoveToEntityLiving(player,
                        axolotl.isInWater() ? 0.5D : 0.15D);
            }
        }
    }

    private static final class FindWaterGoal extends EntityAIBase {
        private final EntityAxolotl axolotl;
        @Nullable
        private BlockPos target;
        private int ticks;
        private int retryDelay;

        private FindWaterGoal(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (axolotl.isInWater() || axolotl.isPlayingDead()) {
                return false;
            }
            if (retryDelay > 0) {
                retryDelay--;
                return false;
            }
            target = findReachableWater(axolotl, 6);
            if (target == null) {
                retryDelay = 20;
            }
            return target != null;
        }

        @Override
        public void startExecuting() {
            ticks = 0;
            moveToWater();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !axolotl.isInWater() && !axolotl.isPlayingDead() && target != null
                    && ticks < 600
                    && axolotl.world.getBlockState(target).getMaterial() == Material.WATER;
        }

        @Override
        public void updateTask() {
            ticks++;
            if (axolotl.navigator.noPath() || ticks % 20 == 0) {
                moveToWater();
            }
        }

        @Override
        public void resetTask() {
            target = null;
        }

        private void moveToWater() {
            if (target != null) {
                axolotl.navigator.tryMoveToXYZ(target.getX() + 0.5D, target.getY() + 0.5D,
                        target.getZ() + 0.5D, 0.15D);
            }
        }

        @Nullable
        private static BlockPos findReachableWater(EntityAxolotl axolotl, int radius) {
            BlockPos origin = axolotl.getPosition();
            BlockPos nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos candidate = origin.add(x, y, z);
                        if (axolotl.world.getBlockState(candidate).getMaterial() != Material.WATER) {
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
            if (nearest == null) {
                return null;
            }
            Path path = axolotl.navigator.getPathToPos(nearest);
            return path == null ? null : nearest;
        }
    }

    private static final class AxolotlSwimWanderGoal extends EntityAIBase {
        private final EntityAxolotl axolotl;
        private final double speed;
        private final int chance;
        @Nullable
        private Path path;

        private AxolotlSwimWanderGoal(EntityAxolotl axolotl, double speed, int chance) {
            this.axolotl = axolotl;
            this.speed = speed;
            this.chance = chance;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!axolotl.isInWater() || axolotl.isPlayingDead()
                    || axolotl.rand.nextInt(chance) != 0) {
                return false;
            }
            BlockPos origin = axolotl.getPosition();
            for (int attempt = 0; attempt < 20; attempt++) {
                BlockPos candidate = origin.add(axolotl.rand.nextInt(21) - 10,
                        axolotl.rand.nextInt(15) - 7, axolotl.rand.nextInt(21) - 10);
                if (axolotl.world.getBlockState(candidate).getMaterial() != Material.WATER) {
                    continue;
                }
                path = axolotl.navigator.getPathToPos(candidate);
                if (path != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return axolotl.isInWater() && !axolotl.isPlayingDead()
                    && !axolotl.navigator.noPath();
        }

        @Override
        public void startExecuting() {
            axolotl.navigator.setPath(path, speed);
        }

        @Override
        public void resetTask() {
            path = null;
        }
    }

    private static final class AxolotlLandWanderGoal extends EntityAIWander {
        private final EntityAxolotl axolotl;

        private AxolotlLandWanderGoal(EntityAxolotl axolotl, double speed, int chance) {
            super(axolotl, speed, chance);
            this.axolotl = axolotl;
        }

        @Override
        public boolean shouldExecute() {
            return !axolotl.isInWater() && !axolotl.isPlayingDead() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !axolotl.isInWater() && !axolotl.isPlayingDead()
                    && super.shouldContinueExecuting();
        }
    }
}
