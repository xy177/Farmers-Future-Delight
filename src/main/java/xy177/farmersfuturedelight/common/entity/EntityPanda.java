package xy177.farmersfuturedelight.common.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityPanda extends EntityAnimal {
    @Override
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return xy177.farmersfuturedelight.common.FFDEntityLootTables.PANDA;
    }
    private static final DataParameter<Byte> MAIN_GENE = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> HIDDEN_GENE = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> EAT_TICKS = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SNEEZE_TICKS = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> UNHAPPY_TICKS = EntityDataManager.createKey(
            EntityPanda.class, DataSerializers.VARINT);
    private static final int SNEEZING = 2;
    private static final int ROLLING = 4;
    private static final int SITTING = 8;
    private static final int ON_BACK = 16;
    private int rollTicks;
    private double rollMotionX;
    private double rollMotionZ;
    private float sitAmount;
    private float previousSitAmount;
    private float onBackAmount;
    private float previousOnBackAmount;
    private float rollAmount;
    private float previousRollAmount;

    public EntityPanda(World world) {
        super(world);
        setSize(1.3F, 1.25F);
        moveHelper = new net.minecraft.entity.ai.EntityMoveHelper(this) {
            @Override
            public void onUpdateMoveHelper() {
                if (canPerformAction()) {
                    super.onUpdateMoveHelper();
                } else {
                    setMoveForward(0.0F);
                    setMoveStrafing(0.0F);
                    setAIMoveSpeed(0.0F);
                }
            }
        };
        net.minecraft.item.Item bamboo = FFDItems.effectiveItem(FFDItems.BAMBOO);
        if (bamboo != null) {
            tasks.addTask(4, new net.minecraft.entity.ai.EntityAITempt(this, 1.0D,
                    bamboo, false) {
                @Override
                public boolean shouldExecute() {
                    return canPerformAction() && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return canPerformAction() && super.shouldContinueExecuting();
                }
            });
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(MAIN_GENE, (byte) Gene.NORMAL.id);
        dataManager.register(HIDDEN_GENE, (byte) Gene.NORMAL.id);
        dataManager.register(FLAGS, (byte) 0);
        dataManager.register(EAT_TICKS, 0);
        dataManager.register(SNEEZE_TICKS, 0);
        dataManager.register(UNHAPPY_TICKS, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new PandaPanicGoal(this, 2.0D));
        tasks.addTask(2, new PandaMateGoal(this));
        tasks.addTask(3, new PandaAttackGoal(this, 1.2D, true));
        tasks.addTask(5, new PandaAvoidGoal<>(this, EntityPlayer.class, 8.0F, 2.0D, 2.0D));
        tasks.addTask(5, new PandaAvoidGoal<>(this, EntityMob.class, 4.0F, 2.0D, 2.0D));
        tasks.addTask(6, new PandaEatGoal(this));
        tasks.addTask(7, new PandaLieGoal(this));
        tasks.addTask(8, new PandaSneezeGoal(this));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(10, new EntityAIWander(this, 1.0D));
        tasks.addTask(11, new EntityAILookIdle(this));
        tasks.addTask(12, new PandaRollGoal(this));
        tasks.addTask(13, new EntityAIFollowParent(this, 1.25D));
        targetTasks.addTask(1, new PandaHurtByTargetGoal(this));
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        setMainGene(Gene.random(rand));
        setHiddenGene(Gene.random(rand));
        updateGeneAttributes();
        return result;
    }

    @Override
    public void onLivingUpdate() {
        previousSitAmount = sitAmount;
        previousOnBackAmount = onBackAmount;
        previousRollAmount = rollAmount;
        super.onLivingUpdate();
        if (isRolling()) {
            updateRoll();
        } else {
            rollTicks = 0;
        }
        if (isSitting()) rotationPitch = 0.0F;
        if (!world.isRemote) {
            if (isWorried()) {
                if (world.isThundering() && !isInWater()) {
                    sit(true);
                    setEating(false);
                } else if (!isEating()) {
                    sit(false);
                }
            }
            if (getAttackTarget() == null) {
                gotBamboo = false;
                didBite = false;
            }
            int unhappy = getUnhappyTicks();
            if (unhappy > 0) {
                setUnhappyTicks(unhappy - 1);
                if (unhappy == 29 || unhappy == 14) {
                    playPandaSound(FFDSounds.PANDA_CANT_BREED);
                }
            }
            if (isSneezing()) {
                int ticks = getSneezeTicks() + 1;
                setSneezeTicks(ticks);
                if (ticks == 1) {
                    playPandaSound(FFDSounds.PANDA_PRE_SNEEZE);
                } else if (ticks > 20) {
                    setPandaFlag(SNEEZING, false);
                    setSneezeTicks(0);
                    finishSneeze();
                }
            }
            updateEating();
        } else if (isEating() && getEatTicks() % 5 == 0) {
            spawnEatingParticles();
        }
        sitAmount = approach(sitAmount, isSitting() ? 1.0F : 0.0F,
                isSitting() ? 0.15F : 0.19F);
        onBackAmount = approach(onBackAmount, isOnBack() ? 1.0F : 0.0F,
                isOnBack() ? 0.15F : 0.19F);
        rollAmount = approach(rollAmount, isRolling() ? 1.0F : 0.0F,
                isRolling() ? 0.15F : 0.19F);
    }

    private boolean gotBamboo;
    private boolean didBite;

    private void updateEating() {
        ItemStack held = getHeldItemMainhand();
        if (!isEating() && isSitting() && !isScared() && !held.isEmpty()
                && rand.nextInt(80) == 1) {
            setEating(true);
        } else if (held.isEmpty() || !isSitting()) {
            setEating(false);
        }
        if (!isEating()) {
            return;
        }
        int ticks = getEatTicks() + 1;
        setEatTicks(ticks);
        if (ticks % 5 == 0) {
            playPandaSound(FFDSounds.PANDA_EAT);
            if (world.isRemote) spawnEatingParticles();
        }
        if (ticks > 80 && !world.isRemote && rand.nextInt(20) == 1) {
            if (ticks > 100 && isGroundFood(held)) {
                setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,
                        ItemStack.EMPTY);
                sit(false);
            }
            setEating(false);
            setEatTicks(0);
        }
    }

    private void updateRoll() {
        rollTicks++;
        if (rollTicks > 32) {
            setPandaFlag(ROLLING, false);
            return;
        }
        if (!world.isRemote) {
            if (rollTicks == 1) {
                float yaw = rotationYaw * ((float) Math.PI / 180.0F);
                double speed = isChild() ? 0.1D : 0.2D;
                rollMotionX = -MathHelper.sin(yaw) * speed;
                rollMotionZ = MathHelper.cos(yaw) * speed;
                motionX += rollMotionX;
                motionZ += rollMotionZ;
                motionY = 0.27D;
            } else if (rollTicks == 7 || rollTicks == 15 || rollTicks == 23) {
                rollMotionX = 0.0D;
                rollMotionZ = 0.0D;
                motionX = 0.0D;
                motionZ = 0.0D;
                if (onGround) {
                    motionY = 0.27D;
                }
            } else {
                motionX = rollMotionX;
                motionZ = rollMotionZ;
            }
            velocityChanged = true;
        }
    }

    private void finishSneeze() {
        if (!world.isRemote) {
            world.setEntityState(this, (byte) 20);
            playPandaSound(FFDSounds.PANDA_SNEEZE);
            AxisAlignedBB area = getEntityBoundingBox().grow(10.0D);
            for (EntityPanda panda : world.getEntitiesWithinAABB(EntityPanda.class, area)) {
                if (!panda.isChild() && panda.onGround && !panda.isInWater()
                        && panda.canPerformAction()) {
                    panda.jumpHelper.setJumping();
                }
            }
            net.minecraft.world.WorldServer server = (net.minecraft.world.WorldServer) world;
            net.minecraft.world.storage.loot.LootContext context =
                    new net.minecraft.world.storage.loot.LootContext.Builder(server)
                            .withLootedEntity(this).build();
            for (ItemStack gift : server.getLootTableManager().getLootTableFromLocation(
                    xy177.farmersfuturedelight.common.FFDEntityLootTables.PANDA_SNEEZE)
                    .generateLootForPools(rand, context)) {
                entityDropItem(gift, 0.0F);
            }
        }
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("MainGene", dataManager.get(MAIN_GENE));
        compound.setByte("HiddenGene", dataManager.get(HIDDEN_GENE));
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dataManager.set(MAIN_GENE, normalizeGene(compound.getByte("MainGene")));
        dataManager.set(HIDDEN_GENE, normalizeGene(compound.getByte("HiddenGene")));
        updateGeneAttributes();
    }

    @Nullable
    @Override
    public EntityPanda createChild(EntityAgeable mate) {
        if (!(mate instanceof EntityPanda)) {
            return null;
        }
        EntityPanda other = (EntityPanda) mate;
        EntityPanda child = new EntityPanda(world);
        if (rand.nextBoolean()) {
            child.setMainGene(randomGene(this));
            child.setHiddenGene(randomGene(other));
        } else {
            child.setMainGene(randomGene(other));
            child.setHiddenGene(randomGene(this));
        }
        if (rand.nextInt(32) == 0) {
            child.setMainGene(Gene.random(rand));
        }
        if (rand.nextInt(32) == 0) {
            child.setHiddenGene(Gene.random(rand));
        }
        child.updateGeneAttributes();
        return child;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isBambooStack(stack);
    }

    public boolean isPandaFood(ItemStack stack) {
        return isBambooStack(stack);
    }

    private boolean isBambooStack(ItemStack stack) {
        ItemStack bamboo = FFDItems.effectiveStack(FFDItems.BAMBOO);
        return !stack.isEmpty() && !bamboo.isEmpty() && ItemStack.areItemsEqual(stack, bamboo);
    }

    private boolean isGroundFood(ItemStack stack) {
        return isBambooStack(stack) || !stack.isEmpty() && stack.getItem() == Items.CAKE;
    }

    private void spawnEatingParticles() {
        ItemStack held = getHeldItemMainhand();
        if (held.isEmpty()) return;
        for (int i = 0; i < 6; i++) {
            net.minecraft.util.math.Vec3d velocity = new net.minecraft.util.math.Vec3d(
                    (rand.nextFloat() - 0.5D) * 0.1D, rand.nextFloat() * 0.1D + 0.1D,
                    (rand.nextFloat() - 0.5D) * 0.1D)
                    .rotatePitch(-rotationPitch * 0.017453292F).rotateYaw(-rotationYaw * 0.017453292F);
            net.minecraft.util.math.Vec3d position = new net.minecraft.util.math.Vec3d(
                    (rand.nextFloat() - 0.5D) * 0.8D, -rand.nextFloat() * 0.6D - 0.3D,
                    1.0D + (rand.nextFloat() - 0.5D) * 0.4D)
                    .rotateYaw(-renderYawOffset * 0.017453292F).addVector(posX, posY + getEyeHeight() + 1.0D, posZ);
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.ITEM_CRACK,
                    position.x, position.y, position.z, velocity.x, velocity.y + 0.05D, velocity.z,
                    net.minecraft.item.Item.getIdFromItem(held.getItem()), held.getMetadata());
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (isScared()) {
            return true;
        }
        if (isOnBack()) {
            setPandaFlag(ON_BACK, false);
            return true;
        }
        if (!isPandaFood(held)) {
            return super.processInteract(player, hand);
        }
        if (isChild()) {
            if (!world.isRemote) {
                int growth = Math.max(1, (int) ((float) (-getGrowingAge()) * 0.1F));
                setGrowingAge(Math.min(0, getGrowingAge() + growth));
                consume(player, hand, held);
            }
            return true;
        }
        if (isBambooStack(held) && !isInLove()) {
            if (getAttackTarget() != null && !world.isRemote) {
                gotBamboo = true;
            }
            return super.processInteract(player, hand);
        }
        if (isSitting() || isInWater() || world.isRemote) {
            return true;
        }
        tryToSit();
        setEating(true);
        ItemStack current = getHeldItemMainhand();
        if (!current.isEmpty() && !player.capabilities.isCreativeMode) {
            entityDropItem(current, 0.0F);
        }
        setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,
                new ItemStack(held.getItem(), 1));
        consume(player, hand, held);
        return true;
    }

    private void consume(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
    }

    private void tryToSit() {
        if (!isInWater()) {
            setMoveForward(0.0F);
            getNavigator().clearPath();
            sit(true);
        }
    }

    public Gene getMainGene() {
        return Gene.byId(dataManager.get(MAIN_GENE));
    }

    public void setMainGene(Gene gene) {
        dataManager.set(MAIN_GENE, (byte) gene.id);
        updateGeneAttributes();
    }

    public Gene getHiddenGene() {
        return Gene.byId(dataManager.get(HIDDEN_GENE));
    }

    public void setHiddenGene(Gene gene) {
        dataManager.set(HIDDEN_GENE, (byte) gene.id);
        updateGeneAttributes();
    }

    public Gene getVariant() {
        Gene main = getMainGene();
        return main.recessive && main != getHiddenGene() ? Gene.NORMAL : main;
    }

    public String getVariantTextureName() {
        switch (getVariant()) {
            case LAZY: return "lazy";
            case WORRIED: return "worried";
            case PLAYFUL: return "playful";
            case BROWN: return "brown";
            case WEAK: return "weak";
            case AGGRESSIVE: return "aggressive";
            default: return "";
        }
    }

    public boolean isLazy() { return getVariant() == Gene.LAZY; }
    public boolean isWorried() { return getVariant() == Gene.WORRIED; }
    public boolean isPlayful() { return getVariant() == Gene.PLAYFUL; }
    public boolean isBrown() { return getVariant() == Gene.BROWN; }
    public boolean isWeak() { return getVariant() == Gene.WEAK; }
    public boolean isRolling() { return hasPandaFlag(ROLLING); }
    public boolean isAggressive() { return getVariant() == Gene.AGGRESSIVE; }
    public boolean isScared() { return isWorried() && world.isThundering(); }
    public boolean isSneezing() { return hasPandaFlag(SNEEZING); }
    public boolean isSitting() { return hasPandaFlag(SITTING); }
    public boolean isOnBack() { return hasPandaFlag(ON_BACK); }
    public boolean isEating() { return dataManager.get(EAT_TICKS) > 0; }
    public int getUnhappyTicks() { return dataManager.get(UNHAPPY_TICKS); }
    public int getSneezeTicks() { return dataManager.get(SNEEZE_TICKS); }
    public int getEatTicks() { return dataManager.get(EAT_TICKS); }
    public void setEatTicks(int value) { dataManager.set(EAT_TICKS, value); }
    public void sit(boolean value) { setPandaFlag(SITTING, value); }
    public void setEating(boolean value) { dataManager.set(EAT_TICKS, value ? 1 : 0); }
    public void setUnhappyTicks(int value) { dataManager.set(UNHAPPY_TICKS, value); }
    public void setSneezeTicks(int value) { dataManager.set(SNEEZE_TICKS, value); }
    public boolean canPerformAction() {
        return !isOnBack() && !isScared() && !isEating() && !hasPandaFlag(ROLLING) && !isSitting();
    }

    public void roll(boolean value) { setPandaFlag(ROLLING, value); }

    public float getSitAmount(float partialTicks) {
        return previousSitAmount + (sitAmount - previousSitAmount) * partialTicks;
    }

    public float getOnBackAmount(float partialTicks) {
        return previousOnBackAmount + (onBackAmount - previousOnBackAmount) * partialTicks;
    }

    public float getRollAmount(float partialTicks) {
        return previousRollAmount + (rollAmount - previousRollAmount) * partialTicks;
    }

    public int getRollTicks() {
        return rollTicks;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity target) {
        boolean hit = super.attackEntityAsMob(target);
        if (hit) {
            if (!isAggressive()) {
                didBite = true;
            }
            playPandaSound(FFDSounds.PANDA_BITE);
        }
        return hit;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        sit(false);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 20) {
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD,
                    posX - MathHelper.sin(renderYawOffset * 0.017453292F) * (width + 1.0F) * 0.5F,
                    getPositionEyes(1.0F).y - 0.1D,
                    posZ + MathHelper.cos(renderYawOffset * 0.017453292F) * (width + 1.0F) * 0.5F,
                    motionX, 0.0D, motionZ);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isAggressive() ? FFDSounds.PANDA_AGGRESSIVE_AMBIENT
                : isWorried() ? FFDSounds.PANDA_WORRIED_AMBIENT : FFDSounds.PANDA_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.PANDA_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.PANDA_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.Block block) {
        playSound(FFDSounds.PANDA_STEP, 0.15F, 1.0F);
    }

    private void playPandaSound(SoundEvent sound) {
        playSound(sound, 1.0F, 0.9F + rand.nextFloat() * 0.2F);
    }

    private boolean hasPandaFlag(int flag) {
        return (dataManager.get(FLAGS) & flag) != 0;
    }

    private void setPandaFlag(int flag, boolean value) {
        byte current = dataManager.get(FLAGS);
        dataManager.set(FLAGS, (byte) (value ? current | flag : current & ~flag));
    }

    private void updateGeneAttributes() {
        if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) == null) {
            return;
        }
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(isWeak() ? 10.0D : 20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(isLazy() ? 0.07D : 0.15D);
    }

    private static byte normalizeGene(byte gene) {
        return gene >= 0 && gene < Gene.values().length ? gene : (byte) 0;
    }

    private static Gene randomGene(EntityPanda panda) {
        return panda.rand.nextBoolean() ? panda.getMainGene() : panda.getHiddenGene();
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    private boolean hasBambooNearby() {
        net.minecraft.block.Block bamboo = FFDItems.effectiveBlock(FFDBlocks.BAMBOO);
        if (bamboo == null) {
            return false;
        }
        BlockPos origin = getPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = 0; y < 3; y++) {
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                    cursor.setPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (world.getBlockState(cursor).getBlock() == bamboo) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private enum Gene {
        NORMAL(0, false), LAZY(1, false), WORRIED(2, false), PLAYFUL(3, false),
        BROWN(4, true), WEAK(5, true), AGGRESSIVE(6, false);

        private final int id;
        private final boolean recessive;

        Gene(int id, boolean recessive) {
            this.id = id;
            this.recessive = recessive;
        }

        private static Gene byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : NORMAL;
        }

        private static Gene random(java.util.Random random) {
            int roll = random.nextInt(16);
            if (roll == 0) return LAZY;
            if (roll == 1) return WORRIED;
            if (roll == 2) return PLAYFUL;
            if (roll == 4) return AGGRESSIVE;
            if (roll >= 5 && roll < 9) return WEAK;
            if (roll == 9 || roll == 10) return BROWN;
            return NORMAL;
        }
    }

    private static class PandaMateGoal extends EntityAIMate {
        private final EntityPanda panda;
        private int nextUnhappyTick;

        private PandaMateGoal(EntityPanda panda) {
            super(panda, 1.0D);
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            if (super.shouldExecute() && panda.getUnhappyTicks() == 0) {
                if (panda.hasBambooNearby()) return true;
                if (panda.ticksExisted >= nextUnhappyTick) {
                    panda.setUnhappyTicks(32);
                    nextUnhappyTick = panda.ticksExisted + 600;
                }
            }
            return false;
        }
    }

    private static class PandaPanicGoal extends EntityAIPanic {
        private final EntityPanda panda;

        private PandaPanicGoal(EntityPanda panda, double speed) {
            super(panda, speed);
            this.panda = panda;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (panda.isSitting()) {
                panda.getNavigator().clearPath();
                return false;
            }
            return super.shouldContinueExecuting();
        }
    }

    private static class PandaAttackGoal extends EntityAIAttackMelee {
        private final EntityPanda panda;

        private PandaAttackGoal(EntityPanda panda, double speed, boolean longMemory) {
            super(panda, speed, longMemory);
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            return panda.canPerformAction() && super.shouldExecute();
        }
    }

    private static class PandaAvoidGoal<T extends net.minecraft.entity.EntityLivingBase>
            extends EntityAIAvoidEntity<T> {
        private final EntityPanda panda;

        private PandaAvoidGoal(EntityPanda panda, Class<T> target, float distance,
                               double farSpeed, double nearSpeed) {
            super(panda, target, distance, farSpeed, nearSpeed);
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            return panda.isWorried() && panda.canPerformAction() && super.shouldExecute();
        }
    }

    private static class PandaEatGoal extends net.minecraft.entity.ai.EntityAIBase {
        private final EntityPanda panda;
        private EntityItem target;
        private int cooldown;

        private PandaEatGoal(EntityPanda panda) {
            this.panda = panda;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!panda.canPerformAction() || panda.isChild() || panda.ticksExisted < cooldown) {
                return false;
            }
            if (!panda.getHeldItemMainhand().isEmpty()) {
                return panda.isGroundFood(panda.getHeldItemMainhand());
            }
            List<EntityItem> items = panda.world.getEntitiesWithinAABB(EntityItem.class,
                    panda.getEntityBoundingBox().grow(8.0D), item -> panda.isGroundFood(item.getItem())
                            && !item.cannotPickup());
            target = items.isEmpty() ? null : items.get(0);
            return target != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (panda.isInWater() || panda.isScared()) return false;
            return panda.getHeldItemMainhand().isEmpty()
                    ? target != null && target.isEntityAlive()
                            && panda.isGroundFood(target.getItem())
                    : panda.isGroundFood(panda.getHeldItemMainhand());
        }

        @Override
        public void startExecuting() {
            if (panda.getHeldItemMainhand().isEmpty() && target != null) {
                panda.getNavigator().tryMoveToEntityLiving(target, 1.2D);
            } else {
                panda.tryToSit();
                panda.setEating(true);
            }
        }

        @Override
        public void updateTask() {
            if (panda.getHeldItemMainhand().isEmpty() && target != null
                    && target.isEntityAlive()) {
                if (panda.getDistanceSq(target) < 1.0D) {
                    ItemStack pickedUp = target.getItem().copy();
                    if (!pickedUp.isEmpty() && panda.isGroundFood(pickedUp)) {
                        panda.onItemPickup(target, pickedUp.getCount());
                        panda.setItemStackToSlot(
                                net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, pickedUp);
                        target.setDead();
                        panda.tryToSit();
                        panda.setEating(true);
                    }
                }
            } else if (!panda.getHeldItemMainhand().isEmpty() && !panda.isSitting()) {
                panda.tryToSit();
                panda.setEating(true);
            }
        }

        @Override
        public void resetTask() {
            ItemStack stack = panda.getHeldItemMainhand();
            if (!stack.isEmpty()) {
                panda.entityDropItem(stack, 0.0F);
                panda.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,
                        ItemStack.EMPTY);
                cooldown = panda.ticksExisted + (panda.isLazy() ? 200 + panda.rand.nextInt(1000)
                        : 200 + panda.rand.nextInt(3000));
            }
            panda.setEating(false);
            panda.sit(false);
            target = null;
        }
    }

    private static class PandaRollGoal extends EntityAIBase {
        private final EntityPanda panda;

        private PandaRollGoal(EntityPanda panda) {
            this.panda = panda;
            setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            if ((!panda.isChild() && !panda.isPlayful()) || !panda.onGround
                    || !panda.canPerformAction()) {
                return false;
            }
            float angle = panda.rotationYaw * ((float) Math.PI / 180.0F);
            int xStep = Math.abs(MathHelper.sin(angle)) > 0.5F
                    ? (int) Math.signum(-MathHelper.sin(angle)) : 0;
            int zStep = Math.abs(MathHelper.cos(angle)) > 0.5F
                    ? (int) Math.signum(MathHelper.cos(angle)) : 0;
            if (panda.world.isAirBlock(panda.getPosition().add(xStep, -1, zStep))) {
                return true;
            }
            return panda.rand.nextInt(panda.isPlayful() ? 60 : 500) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            panda.roll(true);
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }
    }

    private static class PandaLieGoal extends net.minecraft.entity.ai.EntityAIBase {
        private final EntityPanda panda;
        private int cooldown;

        private PandaLieGoal(EntityPanda panda) {
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            return panda.ticksExisted >= cooldown && panda.isLazy() && panda.canPerformAction()
                    && panda.rand.nextInt(400) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !panda.isInWater() && panda.rand.nextInt(2000) != 1;
        }

        @Override
        public void startExecuting() {
            panda.setPandaFlag(ON_BACK, true);
        }

        @Override
        public void resetTask() {
            panda.setPandaFlag(ON_BACK, false);
            cooldown = panda.ticksExisted + 200;
        }
    }

    private static class PandaSneezeGoal extends net.minecraft.entity.ai.EntityAIBase {
        private final EntityPanda panda;

        private PandaSneezeGoal(EntityPanda panda) {
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            if (!panda.isChild() || !panda.canPerformAction()) return false;
            return panda.rand.nextInt(panda.isWeak() ? 500 : 6000) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            panda.setPandaFlag(SNEEZING, true);
        }
    }

    private static class PandaHurtByTargetGoal extends EntityAIHurtByTarget {
        private final EntityPanda panda;

        private PandaHurtByTargetGoal(EntityPanda panda) {
            super(panda, true);
            this.panda = panda;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (panda.gotBamboo || panda.didBite) {
                panda.setAttackTarget(null);
                return false;
            }
            return super.shouldContinueExecuting();
        }
    }
}
