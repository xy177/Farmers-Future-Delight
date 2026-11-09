package xy177.farmersfuturedelight.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemFood;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIFoxEatBerries;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIFoxAvoid;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIFoxPounce;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIFoxSleep;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityFox extends EntityAnimal {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(
            EntityFox.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(
            EntityFox.class, DataSerializers.BYTE);
    private static final DataParameter<String> TRUSTED_PLAYER_0 = EntityDataManager.createKey(
            EntityFox.class, DataSerializers.STRING);
    private static final DataParameter<String> TRUSTED_PLAYER_1 = EntityDataManager.createKey(
            EntityFox.class, DataSerializers.STRING);
    private static final int SITTING = 1;
    private static final int CROUCHING = 4;
    private static final int INTERESTED = 8;
    private static final int POUNCING = 16;
    private static final int SLEEPING = 32;
    private static final int DEFENDING = 128;
    private int ticksSinceAte;
    private float interestedAngle;
    private float previousInterestedAngle;
    private float crouchAmount;
    private float previousCrouchAmount;

    public EntityFox(World world) {
        super(world);
        setSize(0.6F, 0.7F);
        setCanPickUpLoot(true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, 0);
        dataManager.register(FLAGS, (byte) 0);
        dataManager.register(TRUSTED_PLAYER_0, "");
        dataManager.register(TRUSTED_PLAYER_1, "");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 2.2D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAIFoxAvoid(this, EntityPlayer.class, 16.0D,
                1.6D, 1.4D, entity -> !trusts(entity)
                        && !((EntityPlayer) entity).isCreative()
                        && !((EntityPlayer) entity).isSpectator()));
        tasks.addTask(3, new EntityAIFoxAvoid(this, EntityWolf.class, 8.0D,
                1.6D, 1.4D, entity -> !((EntityWolf) entity).isTamed()));
        tasks.addTask(3, new EntityAIFoxAvoid(this, EntityPolarBear.class, 8.0D,
                1.6D, 1.4D, entity -> true));
        tasks.addTask(4, new EntityAIFoxPounce(this));
        tasks.addTask(5, new EntityAIAttackMelee(this, 1.2D, true));
        tasks.addTask(6, new EntityAIFoxEatBerries(this, 1.2D));
        tasks.addTask(7, new EntityAIFoxSleep(this));
        tasks.addTask(8, new EntityAIWander(this, 1.0D));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        tasks.addTask(10, new EntityAILookIdle(this));
        targetTasks.addTask(1, new FoxHurtByTarget(this));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this,
                EntityChicken.class, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this,
                EntityRabbit.class, false));
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        Biome biome = world.getBiome(getPosition());
        setSnowVariant(biome == Biomes.COLD_TAIGA || biome == Biomes.MUTATED_TAIGA_COLD);
        if (rand.nextFloat() < 0.2F) {
            float roll = rand.nextFloat();
            ItemStack held = roll < 0.05F ? new ItemStack(net.minecraft.init.Items.EMERALD)
                    : roll < 0.2F ? new ItemStack(net.minecraft.init.Items.EGG)
                    : roll < 0.4F ? new ItemStack(rand.nextBoolean()
                            ? net.minecraft.init.Items.RABBIT_FOOT
                            : net.minecraft.init.Items.RABBIT_HIDE)
                    : roll < 0.6F ? new ItemStack(net.minecraft.init.Items.WHEAT)
                    : roll < 0.8F ? new ItemStack(net.minecraft.init.Items.LEATHER)
                    : new ItemStack(net.minecraft.init.Items.FEATHER);
            setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, held);
            setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 2.0F);
        }
        return result;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        previousInterestedAngle = interestedAngle;
        interestedAngle = approach(interestedAngle, isInterested() ? 1.0F : 0.0F, 0.4F);
        previousCrouchAmount = crouchAmount;
        crouchAmount = isCrouching() ? Math.min(5.0F, crouchAmount + 0.2F) : 0.0F;
        if (!world.isRemote) {
            updateHeldFood();
            if (getAttackTarget() != null || isInWater() || world.isThundering()) {
                setSleeping(false);
                setSitting(false);
            }
            if (getAttackTarget() == null) {
                setCrouching(false);
                setInterested(false);
            }
        }
    }

    private void updateHeldFood() {
        ItemStack held = getHeldItemMainhand();
        if (held.isEmpty() || !(held.getItem() instanceof ItemFood)
                || getAttackTarget() != null || !onGround || isSleeping()) {
            ticksSinceAte = 0;
            return;
        }
        if (++ticksSinceAte > 600) {
            ItemStack remaining = held.getItem().onItemUseFinish(held, world, this);
            setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, remaining);
            ticksSinceAte = 0;
        } else if (ticksSinceAte > 560 && rand.nextFloat() < 0.1F) {
            playFoxSound(FFDSounds.FOX_EAT);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return FFDItems.isSweetBerryEnabled() && isSweetBerryStack(stack);
    }

    public boolean isSweetBerryStack(ItemStack stack) {
        ItemStack berries = FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
        return !stack.isEmpty() && !berries.isEmpty()
                && ItemStack.areItemsEqual(stack, berries);
    }

    @Nullable
    @Override
    public EntityFox createChild(EntityAgeable mate) {
        if (!(mate instanceof EntityFox)) {
            return null;
        }
        EntityFox child = new EntityFox(world);
        EntityFox other = (EntityFox) mate;
        child.setSnowVariant(rand.nextBoolean() ? isSnowVariant() : other.isSnowVariant());
        EntityPlayer first = getLoveCause();
        EntityPlayer second = other.getLoveCause();
        if (first != null) {
            child.addTrustedPlayer(first.getUniqueID());
        }
        if (second != null && (first == null || !first.getUniqueID().equals(second.getUniqueID()))) {
            child.addTrustedPlayer(second.getUniqueID());
        }
        return child;
    }

    @Override
    public boolean canMateWith(EntityAnimal other) {
        return other instanceof EntityFox && super.canMateWith(other);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        EntityLivingBase attacker = source.getTrueSource() instanceof EntityLivingBase
                ? (EntityLivingBase) source.getTrueSource() : null;
        if (!world.isRemote && attacker != null && attacker != this && !trusts(attacker)) {
            setDefending(true);
        }
        boolean damaged = super.attackEntityFrom(source, amount);
        if (!damaged) {
            setDefending(false);
        }
        return damaged;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        super.setAttackTarget(target);
        if (target == null) {
            setDefending(false);
        } else {
            setSleeping(false);
            setSitting(false);
        }
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", isSnowVariant() ? 1 : 0);
        compound.setBoolean("Sleeping", isSleeping());
        compound.setBoolean("Sitting", isSitting());
        compound.setBoolean("Crouching", isCrouching());
        compound.setString("TrustedPlayer0", dataManager.get(TRUSTED_PLAYER_0));
        compound.setString("TrustedPlayer1", dataManager.get(TRUSTED_PLAYER_1));
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setSnowVariant(compound.getInteger("Variant") == 1);
        setSleeping(compound.getBoolean("Sleeping"));
        setSitting(compound.getBoolean("Sitting"));
        setCrouching(compound.getBoolean("Crouching"));
        dataManager.set(TRUSTED_PLAYER_0, compound.getString("TrustedPlayer0"));
        dataManager.set(TRUSTED_PLAYER_1, compound.getString("TrustedPlayer1"));
    }

    public boolean isSnowVariant() {
        return dataManager.get(VARIANT) == 1;
    }

    public void setSnowVariant(boolean snow) {
        dataManager.set(VARIANT, snow ? 1 : 0);
    }

    public boolean isSitting() {
        return getFoxFlag(SITTING);
    }

    public void setSitting(boolean value) {
        setFoxFlag(SITTING, value);
    }

    public boolean isCrouching() {
        return getFoxFlag(CROUCHING);
    }

    public void setCrouching(boolean value) {
        setFoxFlag(CROUCHING, value);
    }

    public boolean isInterested() {
        return getFoxFlag(INTERESTED);
    }

    public void setInterested(boolean value) {
        setFoxFlag(INTERESTED, value);
    }

    public boolean isPouncing() {
        return getFoxFlag(POUNCING);
    }

    public void setPouncing(boolean value) {
        setFoxFlag(POUNCING, value);
    }

    public boolean isSleeping() {
        return getFoxFlag(SLEEPING);
    }

    public void setSleeping(boolean value) {
        setFoxFlag(SLEEPING, value);
    }

    public boolean isDefending() {
        return getFoxFlag(DEFENDING);
    }

    private void setDefending(boolean value) {
        setFoxFlag(DEFENDING, value);
    }

    public float getInterestedAngle(float partialTicks) {
        return previousInterestedAngle
                + (interestedAngle - previousInterestedAngle) * partialTicks;
    }

    public float getCrouchAmount(float partialTicks) {
        return previousCrouchAmount + (crouchAmount - previousCrouchAmount) * partialTicks;
    }

    public void playFoxSound(SoundEvent sound) {
        playSound(sound, 1.0F, 0.8F + rand.nextFloat() * 0.4F);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.FOX_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.FOX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.FOX_DEATH;
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity target) {
        setLastAttackedEntity(target);
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this),
                (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (hit) {
            applyEnchantments(this, target);
            playFoxSound(FFDSounds.FOX_BITE);
        }
        return hit;
    }

    @Override
    protected boolean canEquipItem(ItemStack stack) {
        ItemStack held = getHeldItemMainhand();
        return super.canEquipItem(stack) && (held.isEmpty()
                || stack.getItem() instanceof ItemFood && !(held.getItem() instanceof ItemFood));
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        ItemStack incoming = itemEntity.getItem();
        if (!canEquipItem(incoming)) {
            return;
        }
        ItemStack held = getHeldItemMainhand();
        if (!held.isEmpty()) {
            EntityItem spit = entityDropItem(held.copy(), 0.0F);
            if (spit != null) {
                spit.motionX = getLookVec().x * 0.3D;
                spit.motionY = 0.1D;
                spit.motionZ = getLookVec().z * 0.3D;
            }
            playFoxSound(FFDSounds.FOX_SPIT);
        }
        ItemStack picked = incoming.splitStack(1);
        setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, picked);
        setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 2.0F);
        onItemPickup(itemEntity, 1);
        if (incoming.isEmpty()) {
            itemEntity.setDead();
        }
    }

    public boolean trusts(EntityLivingBase entity) {
        return entity != null && (isTrusted(dataManager.get(TRUSTED_PLAYER_0), entity)
                || isTrusted(dataManager.get(TRUSTED_PLAYER_1), entity));
    }

    public void addTrustedPlayer(UUID uuid) {
        String value = uuid.toString();
        if (dataManager.get(TRUSTED_PLAYER_0).isEmpty()) {
            dataManager.set(TRUSTED_PLAYER_0, value);
        } else if (!dataManager.get(TRUSTED_PLAYER_0).equals(value)
                && dataManager.get(TRUSTED_PLAYER_1).isEmpty()) {
            dataManager.set(TRUSTED_PLAYER_1, value);
        }
    }

    private boolean isTrusted(String uuid, EntityLivingBase entity) {
        return !uuid.isEmpty() && uuid.equals(entity.getUniqueID().toString());
    }

    private boolean getFoxFlag(int flag) {
        return (dataManager.get(FLAGS) & flag) != 0;
    }

    private void setFoxFlag(int flag, boolean value) {
        byte flags = dataManager.get(FLAGS);
        dataManager.set(FLAGS, (byte) (value ? flags | flag : flags & ~flag));
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    private static final class FoxHurtByTarget extends EntityAIHurtByTarget {
        private final EntityFox fox;

        private FoxHurtByTarget(EntityFox fox) {
            super(fox, true);
            this.fox = fox;
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase attacker = fox.getRevengeTarget();
            return attacker != null && !fox.trusts(attacker) && super.shouldExecute();
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            fox.setDefending(true);
            fox.playFoxSound(FFDSounds.FOX_AGGRO);
        }
    }
}
