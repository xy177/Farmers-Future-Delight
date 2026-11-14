package xy177.farmersfuturedelight.common.entity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityHoglin extends EntityAnimal implements IMob {
    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = getPosition();
        return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL
                && world.getBlockState(pos.down()).getBlock() != net.minecraft.init.Blocks.NETHER_WART_BLOCK
                && net.minecraft.world.WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                        net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND, world, pos);
    }

    private static final DataParameter<Boolean> IMMUNE_TO_ZOMBIFICATION =
            EntityDataManager.createKey(EntityHoglin.class, DataSerializers.BOOLEAN);
    private int attackAnimationRemainingTicks;
    private int timeInOverworld;
    private int pacifiedTicks;
    private int retreatTicks;
    private int attackMemoryTicks;
    private boolean cannotBeHunted;
    private EntityLivingBase retreatTarget;
    private BlockPos nearestRepellent;

    public EntityHoglin(World world) {
        super(world);
        setSize(1.3964844F, 1.4F);
        experienceValue = 5;
    }

    public boolean isZoglin() {
        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        if (!isZoglin()) {
            tasks.addTask(1, new RetreatGoal());
            tasks.addTask(2, new EntityAIMate(this, 0.6D));
            tasks.addTask(4, new EntityAIFollowParent(this, 0.6D));
        }
        tasks.addTask(3, new AttackGoal());
        tasks.addTask(5, new EntityAIWander(this, 0.4D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLivingBase>(
                this, EntityLivingBase.class, 10, true, false, this::canChooseTarget) {
            @Override
            public boolean shouldExecute() {
                return !isPacified() && !isInLove() && retreatTicks == 0
                        && super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                return !isPacified() && !isInLove() && retreatTicks == 0
                        && super.shouldContinueExecuting();
            }
        });
    }

    private boolean canChooseTarget(@Nullable EntityLivingBase target) {
        if (target == null || !target.isEntityAlive() || target == this) return false;
        if (target instanceof EntityPlayer && (world.getDifficulty() == EnumDifficulty.PEACEFUL
                || ((EntityPlayer) target).isCreative()
                || ((EntityPlayer) target).isSpectator())) return false;
        return isZoglin() ? !(target instanceof EntityZoglin)
                && !(target instanceof EntityCreeper) && !target.isOnSameTeam(this)
                : target instanceof EntityPlayer;
    }

    @Override
    public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
        return type == EnumCreatureType.MONSTER;
    }

    @Override
    public void setScaleForAge(boolean child) {
        setSize(child ? 0.75F : 1.3964844F, child ? 0.85F : 1.4F);
        setScale(1.0F);
    }

    @Override
    public float getEyeHeight() {
        return isChild() ? 0.625F : 1.19F;
    }

    @Override
    public void setGrowingAge(int age) {
        super.setGrowingAge(age);
        if (getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                    .setBaseValue(isChild() ? 0.5D : 6.0D);
        }
        experienceValue = isChild() && !isZoglin() ? 3 : 5;
    }

    @Override
    public void onLivingUpdate() {
        int previousAge = getGrowingAge();
        super.onLivingUpdate();
        if (isZoglin() && previousAge < 0 && !world.isRemote) setGrowingAge(previousAge);
        if (attackAnimationRemainingTicks > 0) --attackAnimationRemainingTicks;
        if (!world.isRemote && isZoglin() && world.getDifficulty() == EnumDifficulty.PEACEFUL) setDead();
    }

    @Override
    protected void updateAITasks() {
        if (!isZoglin()) {
            if (pacifiedTicks > 0) --pacifiedTicks;
            if (retreatTicks > 0) --retreatTicks;
            if (ticksExisted % 20 == 0) findRepellent();
            if (nearestRepellent != null) pacifiedTicks = 200;
            if (isPacified() || isInLove() || retreatTicks > 0) setAttackTarget(null);
            if (isConverting()) {
                if (++timeInOverworld > 300) finishConversion();
            } else timeInOverworld = 0;
        }
        if (attackMemoryTicks > 0 && --attackMemoryTicks == 0) setAttackTarget(null);
        super.updateAITasks();
    }

    public boolean isPacified() {
        return !isZoglin() && pacifiedTicks > 0;
    }

    public boolean isConverting() {
        return !isZoglin() && !dataManager.get(IMMUNE_TO_ZOMBIFICATION)
                && !isAIDisabled() && world.provider.getDimension() != -1;
    }

    public void setImmuneToZombification(boolean immune) {
        dataManager.set(IMMUNE_TO_ZOMBIFICATION, immune);
    }

    public boolean canBeHunted() {
        return !isZoglin() && !isChild() && !cannotBeHunted;
    }

    private void findRepellent() {
        nearestRepellent = null;
        double closest = Double.MAX_VALUE;
        BlockPos center = new BlockPos(this);
        for (BlockPos.MutableBlockPos candidate : BlockPos.getAllInBoxMutable(
                center.add(-8, -4, -8), center.add(8, 4, 8))) {
            if (!world.isBlockLoaded(candidate)) continue;
            Block block = world.getBlockState(candidate).getBlock();
            ResourceLocation name = block.getRegistryName();
            if (block != Blocks.PORTAL && (name == null
                    || !"warped_fungus".equals(name.getResourcePath())
                    && !"respawn_anchor".equals(name.getResourcePath()))) continue;
            double distance = candidate.distanceSq(center);
            if (distance < closest) {
                nearestRepellent = candidate.toImmutable();
                closest = distance;
            }
        }
    }

    private void finishConversion() {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(FFDEntities.ZOGLIN_ID);
        if (entry == null && FFDConfig.zoglinMode == FFDConfig.FeatureMode.AUTO) {
            entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.ZOGLIN, "zoglin");
        }
        Entity converted = entry == null ? null : entry.newInstance(world);
        if (!(converted instanceof EntityLiving)) return;
        EntityLiving replacement = (EntityLiving) converted;
        replacement.copyLocationAndAnglesFrom(this);
        replacement.setNoAI(isAIDisabled());
        replacement.setSilent(isSilent());
        replacement.setEntityInvulnerable(getIsInvulnerable());
        if (hasCustomName()) {
            replacement.setCustomNameTag(getCustomNameTag());
            replacement.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
        }
        if (isNoDespawnRequired()) replacement.enablePersistence();
        NBTTagCompound state = new NBTTagCompound();
        replacement.writeEntityToNBT(state);
        state.setBoolean("IsBaby", isChild());
        state.setInteger("Age", isChild() ? -24000 : 0);
        replacement.readEntityFromNBT(state);
        replacement.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200));
        if (!world.spawnEntity(replacement)) return;
        if (getLeashed()) replacement.setLeashHolder(getLeashHolder(), true);
        for (Entity passenger : new java.util.ArrayList<>(getPassengers())) {
            passenger.startRiding(replacement, true);
        }
        playSound(FFDSounds.HOGLIN_CONVERTED_TO_ZOMBIFIED, 1.0F, 1.0F);
        setDead();
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (!(target instanceof EntityLivingBase)) return false;
        attackAnimationRemainingTicks = 10;
        world.setEntityState(this, (byte) 4);
        playSound(isZoglin() ? FFDSounds.ZOGLIN_ATTACK : FFDSounds.HOGLIN_ATTACK, 1.0F, 1.0F);
        float damage = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        if (!isChild() && (int) damage > 0) damage = damage / 2.0F + rand.nextInt((int) damage);
        EntityLivingBase defender = (EntityLivingBase) target;
        boolean blocked = defender.isActiveItemStackBlocking()
                && new Vec3d(posX - target.posX, 0.0D, posZ - target.posZ).normalize()
                        .dotProduct(defender.getLookVec()) > 0.0D;
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (hit) {
            applyEnchantments(this, target);
            if (!isChild()) throwTarget((EntityLivingBase) target);
        } else if (blocked && !isChild() && !defender.getIsInvulnerable()) throwTarget(defender);
        if (!isZoglin() && !isChild() && isPiglin(target)) {
            java.util.List<EntityHoglin> herd = world.getEntitiesWithinAABB(
                    EntityHoglin.class, getEntityBoundingBox().grow(16.0D),
                    nearby -> !nearby.isZoglin() && !nearby.isChild()
                            && getEntitySenses().canSee(nearby));
            int piglins = world.getEntitiesWithinAABB(EntityLiving.class,
                    getEntityBoundingBox().grow(16.0D), nearby -> isPiglin(nearby)
                            && !nearby.isChild() && getEntitySenses().canSee(nearby)).size();
            if (piglins > herd.size()) for (EntityHoglin nearby : herd) {
                nearby.retreatTarget = (EntityLivingBase) target;
                nearby.retreatTicks = 100 + nearby.rand.nextInt(301);
                nearby.setAttackTarget(null);
            }
        }
        return hit;
    }

    private static boolean isPiglin(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && "piglin".equals(id.getResourcePath());
    }

    private void throwTarget(EntityLivingBase target) {
        double power = 1.0D - target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .getAttributeValue();
        if (power <= 0.0D) return;
        float angle = rand.nextInt(21) - 10;
        Vec3d direction = new Vec3d(target.posX - posX, 0.0D, target.posZ - posZ)
                .normalize().scale(power * (rand.nextFloat() * 0.5F + 0.2F)).rotateYaw(angle);
        target.addVelocity(direction.x, power * rand.nextFloat() * 0.5D, direction.z);
        target.velocityChanged = true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (!hurt || world.isRemote || !(source.getTrueSource() instanceof EntityLivingBase)) return hurt;
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
        if (attacker == this || !isZoglin() && attacker instanceof EntityHoglin
                && !((EntityHoglin) attacker).isZoglin()
                || attacker instanceof EntityPlayer && !canChooseTarget(attacker)) {
            return hurt;
        }
        pacifiedTicks = 0;
        resetInLove();
        if (isChild() && !isZoglin()) {
            retreatTarget = attacker;
            retreatTicks = 100 + rand.nextInt(301);
            setAttackTarget(null);
        } else if (isZoglin() || retreatTicks == 0 || !isPiglin(attacker)) {
            setAttackTarget(attacker);
            attackMemoryTicks = 200;
            if (!isZoglin()) for (EntityHoglin nearby : world.getEntitiesWithinAABB(
                    EntityHoglin.class, getEntityBoundingBox().grow(16.0D))) {
                if (!nearby.isChild() && !nearby.isZoglin() && !nearby.isPacified()
                        && nearby.getEntitySenses().canSee(attacker)) {
                    nearby.setAttackTarget(attacker);
                    nearby.attackMemoryTicks = 200;
                }
            }
        }
        return hurt;
    }

    @Override
    public void handleStatusUpdate(byte status) {
        if (status == 4) attackAnimationRemainingTicks = 10;
        else super.handleStatusUpdate(status);
    }

    public int getAttackAnimationRemainingTicks() {
        return attackAnimationRemainingTicks;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        ItemStack fungus = FFDItems.effectiveStack(FFDItems.CRIMSON_FUNGUS);
        return !isZoglin() && !stack.isEmpty() && !fungus.isEmpty()
                && ItemStack.areItemsEqual(stack, fungus);
    }

    @Override
    public void setInLove(EntityPlayer player) {
        if (!isZoglin() && !isPacified()) super.setInLove(player);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        boolean food = isBreedingItem(player.getHeldItem(hand));
        if (food && !isChild() && isPacified()) return false;
        boolean handled = super.processInteract(player, hand);
        if (handled && food && !world.isRemote) enablePersistence();
        return handled;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) {
        if (isZoglin()) return null;
        EntityHoglin child = new EntityHoglin(world);
        child.enablePersistence();
        return child;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return !getLeashed();
    }

    @Override
    protected boolean canDespawn() {
        return !isNoDespawnRequired();
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return isZoglin() ? EnumCreatureAttribute.UNDEAD : EnumCreatureAttribute.UNDEFINED;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isZoglin()) return getAttackTarget() == null ? FFDSounds.ZOGLIN_AMBIENT : FFDSounds.ZOGLIN_ANGRY;
        if (retreatTicks > 0 || nearestRepellent != null || isConverting()) return FFDSounds.HOGLIN_RETREAT;
        return getAttackTarget() == null ? FFDSounds.HOGLIN_AMBIENT : FFDSounds.HOGLIN_ANGRY;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isZoglin() ? FFDSounds.ZOGLIN_HURT : FFDSounds.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isZoglin() ? FFDSounds.ZOGLIN_DEATH : FFDSounds.HOGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        playSound(isZoglin() ? FFDSounds.ZOGLIN_STEP : FFDSounds.HOGLIN_STEP, 0.15F, 1.0F);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(FarmerFutureDelight.MODID, "entities/" + (isZoglin() ? "zoglin" : "hoglin"));
    }

    @Override
    protected boolean canDropLoot() { return true; }

    @Override
    protected int getExperiencePoints(EntityPlayer player) { return isChild() && !isZoglin() ? 3 : 5; }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        if (!isChild() || isZoglin()) super.dropLoot(wasRecentlyHit, lootingModifier, source);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, data);
        if (rand.nextFloat() < 0.2F) setGrowingAge(-24000);
        return result;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsImmuneToZombification", dataManager.get(IMMUNE_TO_ZOMBIFICATION));
        compound.setInteger("TimeInOverworld", timeInOverworld);
        compound.setBoolean("CannotBeHunted", cannotBeHunted);
        if (isZoglin()) compound.setBoolean("IsBaby", isChild());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setImmuneToZombification(compound.getBoolean("IsImmuneToZombification"));
        timeInOverworld = compound.getInteger("TimeInOverworld");
        cannotBeHunted = compound.getBoolean("CannotBeHunted");
        if (isZoglin()) setGrowingAge(compound.getBoolean("IsBaby") ? -24000 : 0);
    }

    private final class AttackGoal extends EntityAIAttackMelee {
        private AttackGoal() {
            super(EntityHoglin.this, 1.0D, true);
        }

        @Override
        public boolean shouldExecute() {
            return !isPacified() && !isInLove() && retreatTicks == 0 && super.shouldExecute();
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase target, double distance) {
            if (distance <= getAttackReachSqr(target) && attackTick <= 0) {
                attackTick = isChild() ? 15 : 40;
                attackEntityAsMob(target);
            }
        }
    }

    private final class RetreatGoal extends EntityAIBase {
        private Vec3d destination;

        private RetreatGoal() {
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            Vec3d danger = retreatTicks > 0 && retreatTarget != null ? retreatTarget.getPositionVector()
                    : nearestRepellent == null ? null : new Vec3d(nearestRepellent);
            if (danger == null || getDistanceSq(danger.x, danger.y, danger.z) > 225.0D) return false;
            destination = RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityHoglin.this, 16, 7, danger);
            return destination != null;
        }

        @Override
        public void startExecuting() {
            setAttackTarget(null);
            getNavigator().tryMoveToXYZ(destination.x, destination.y, destination.z,
                    retreatTicks > 0 ? 1.3D : 1.0D);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !getNavigator().noPath() && (retreatTicks > 0 || nearestRepellent != null);
        }
    }
}
