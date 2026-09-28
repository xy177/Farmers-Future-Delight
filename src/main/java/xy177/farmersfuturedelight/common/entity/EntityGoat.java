package xy177.farmersfuturedelight.common.entity;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIGoatLongJump;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIGoatRam;
import xy177.farmersfuturedelight.common.item.ItemGoatHorn;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityGoat extends EntityAnimal {
    private static final float MAX_HEAD_YAW = 15.0F;
    private static final DataParameter<Boolean> SCREAMING = EntityDataManager.createKey(
            EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFT_HORN = EntityDataManager.createKey(
            EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_HORN = EntityDataManager.createKey(
            EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LONG_JUMPING = EntityDataManager.createKey(
            EntityGoat.class, DataSerializers.BOOLEAN);

    private boolean loweringHead;
    private int lowerHeadTick;
    private int ramCooldown;
    private int longJumpCooldown;

    public EntityGoat(World world) {
        super(world);
        setSize(0.9F, 1.3F);
        stepHeight = 1.0F;
        if (getNavigator() instanceof PathNavigateGround) {
            ((PathNavigateGround) getNavigator()).setCanSwim(true);
        }
        ramCooldown = nextRamCooldown();
        longJumpCooldown = nextLongJumpCooldown();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(SCREAMING, false);
        dataManager.register(LEFT_HORN, true);
        dataManager.register(RIGHT_HORN, true);
        dataManager.register(LONG_JUMPING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        tasks.addTask(2, new EntityAIGoatRam(this));
        tasks.addTask(3, new EntityAIMate(this, 1.0D));
        tasks.addTask(4, new EntityAITempt(this, 1.25D, Items.WHEAT, false));
        tasks.addTask(5, new EntityAIFollowParent(this, 1.25D));
        tasks.addTask(6, new EntityAIGoatLongJump(this));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        setRotationYawHead(rotationYawHead);
        lowerHeadTick += loweringHead ? 1 : -2;
        lowerHeadTick = MathHelper.clamp(lowerHeadTick, 0, 20);
        if (!world.isRemote) {
            if (ramCooldown > 0) {
                ramCooldown--;
            }
            if (longJumpCooldown > 0) {
                longJumpCooldown--;
            }
        }
    }

    @Override
    public void setRotationYawHead(float rotation) {
        float delta = MathHelper.wrapDegrees(rotation - renderYawOffset);
        super.setRotationYawHead(renderYawOffset
                + MathHelper.clamp(delta, -MAX_HEAD_YAW, MAX_HEAD_YAW));
    }

    @Override
    protected float updateDistance(float bodyYaw, float distance) {
        float result = super.updateDistance(bodyYaw, distance);
        float delta = MathHelper.wrapDegrees(rotationYawHead - renderYawOffset);
        rotationYawHead = renderYawOffset
                + MathHelper.clamp(delta, -MAX_HEAD_YAW, MAX_HEAD_YAW);
        return result;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        setScreaming(rand.nextFloat() < FFDConfig.goatScreamingChance);
        setRamCooldown(nextRamCooldown());
        setLongJumpCooldown(nextLongJumpCooldown());
        if (!isChild() && rand.nextFloat() < FFDConfig.goatSingleHornChance) {
            if (rand.nextBoolean()) {
                setLeftHorn(false);
            } else {
                setRightHorn(false);
            }
        }
        updateAttackDamage();
        return result;
    }

    @Nullable
    @Override
    public EntityGoat createChild(EntityAgeable mate) {
        EntityGoat child = new EntityGoat(world);
        EntityGoat selectedParent = rand.nextBoolean() ? this
                : mate instanceof EntityGoat ? (EntityGoat) mate : this;
        child.setScreaming(selectedParent.isScreaming()
                || rand.nextFloat() < FFDConfig.goatScreamingChance);
        child.setLeftHorn(true);
        child.setRightHorn(true);
        return child;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.WHEAT;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!isChild() && held.getItem() == Items.BUCKET) {
            playSound(getMilkingSound(), 1.0F, 1.0F);
            if (!world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                ItemStack milk = new ItemStack(Items.MILK_BUCKET);
                if (held.isEmpty()) {
                    player.setHeldItem(hand, milk);
                } else if (!player.inventory.addItemStackToInventory(milk)) {
                    player.dropItem(milk, false);
                }
            }
            return true;
        }

        boolean food = isBreedingItem(held);
        boolean interacted = super.processInteract(player, hand);
        if (interacted && food) {
            playSound(isScreaming() ? FFDSounds.GOAT_SCREAMING_EAT : FFDSounds.GOAT_EAT,
                    1.0F, 0.8F + rand.nextFloat() * 0.4F);
        }
        return interacted;
    }

    @Override
    public void setScaleForAge(boolean child) {
        updateSize(child, isLongJumping());
        updateAttackDamage();
    }

    @Override
    protected void onGrowingAdult() {
        super.onGrowingAdult();
        updateAttackDamage();
    }

    private void updateAttackDamage() {
        IAttributeInstance attackDamage = getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(isChild() ? 1.0D : 2.0D);
        }
    }

    private void updateSize(boolean child, boolean longJumping) {
        float scale = longJumping ? 0.7F : 1.0F;
        setSize((child ? 0.45F : 0.9F) * scale,
                (child ? 0.65F : 1.3F) * scale);
    }

    @Override
    public float getEyeHeight() {
        return height * 0.85F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (damageMultiplier <= 0.0F) {
            super.fall(distance, damageMultiplier);
            return;
        }
        super.fall(Math.max(0.0F, distance - 10.0F / damageMultiplier), damageMultiplier);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_AMBIENT : FFDSounds.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_HURT : FFDSounds.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_DEATH : FFDSounds.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        playSound(FFDSounds.GOAT_STEP, 0.15F, 1.0F);
    }

    private SoundEvent getMilkingSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_MILK : FFDSounds.GOAT_MILK;
    }

    public SoundEvent getLongJumpSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_LONG_JUMP : FFDSounds.GOAT_LONG_JUMP;
    }

    public SoundEvent getPrepareRamSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_PREPARE_RAM : FFDSounds.GOAT_PREPARE_RAM;
    }

    public SoundEvent getRamImpactSound() {
        return isScreaming() ? FFDSounds.GOAT_SCREAMING_RAM_IMPACT : FFDSounds.GOAT_RAM_IMPACT;
    }

    public float getGoatVoicePitch() {
        return getSoundPitch();
    }

    public boolean isScreaming() {
        return dataManager.get(SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        dataManager.set(SCREAMING, screaming);
    }

    public boolean hasLeftHorn() {
        return dataManager.get(LEFT_HORN);
    }

    public void setLeftHorn(boolean present) {
        dataManager.set(LEFT_HORN, present);
    }

    public boolean hasRightHorn() {
        return dataManager.get(RIGHT_HORN);
    }

    public void setRightHorn(boolean present) {
        dataManager.set(RIGHT_HORN, present);
    }

    public boolean isLongJumping() {
        return dataManager.get(LONG_JUMPING);
    }

    public void setLongJumping(boolean longJumping) {
        if (dataManager.get(LONG_JUMPING) != longJumping) {
            dataManager.set(LONG_JUMPING, longJumping);
        }
        updateSize(isChild(), longJumping);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (LONG_JUMPING.equals(key)) {
            updateSize(isChild(), isLongJumping());
        }
    }

    public void setLoweringHead(boolean lowering) {
        if (loweringHead == lowering) {
            return;
        }
        loweringHead = lowering;
        if (!world.isRemote) {
            world.setEntityState(this, lowering ? (byte) 58 : (byte) 59);
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 58) {
            loweringHead = true;
        } else if (id == 59) {
            loweringHead = false;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public float getRammingXHeadRot() {
        float maximum = isChild() ? 52.5F : 30.0F;
        return lowerHeadTick / 20.0F * maximum * 0.017453292F;
    }

    public ItemStack createHorn() {
        int base = isScreaming() ? 4 : 0;
        int instrument = base + new Random(getUniqueID().hashCode()).nextInt(4);
        return new ItemStack(FFDItems.GOAT_HORN, 1,
                ItemGoatHorn.normalizeInstrument(instrument));
    }

    public boolean dropHorn() {
        if (isChild() || !hasLeftHorn() && !hasRightHorn()) {
            return false;
        }

        if (!hasLeftHorn()) {
            setRightHorn(false);
        } else if (!hasRightHorn()) {
            setLeftHorn(false);
        } else if (rand.nextBoolean()) {
            setLeftHorn(false);
        } else {
            setRightHorn(false);
        }

        EntityItem item = new EntityItem(world, posX, posY, posZ, createHorn());
        item.motionX = -0.2D + rand.nextDouble() * 0.4D;
        item.motionY = 0.3D + rand.nextDouble() * 0.4D;
        item.motionZ = -0.2D + rand.nextDouble() * 0.4D;
        world.spawnEntity(item);
        return true;
    }

    public boolean canSnapHornOn(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.LOG || block == Blocks.LOG2 || hasOreName(state, "logWood")
                || block == Blocks.STONE || block == Blocks.PACKED_ICE
                || block == Blocks.IRON_ORE || block == Blocks.COAL_ORE
                || block == Blocks.EMERALD_ORE || block == FFDBlocks.COPPER_ORE
                || hasOreName(state, "oreCopper");
    }

    private static boolean hasOreName(IBlockState state, String oreName) {
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == Items.AIR) {
            return false;
        }
        ItemStack stack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
        int wanted = OreDictionary.getOreID(oreName);
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (id == wanted) {
                return true;
            }
        }
        return false;
    }

    public int getRamCooldown() {
        return ramCooldown;
    }

    public void setRamCooldown(int ticks) {
        ramCooldown = Math.max(0, ticks);
    }

    public int nextRamCooldown() {
        return isScreaming()
                ? randomRange(FFDConfig.screamingGoatRamCooldownMinTicks,
                        FFDConfig.screamingGoatRamCooldownMaxTicks)
                : randomRange(FFDConfig.goatRamCooldownMinTicks,
                        FFDConfig.goatRamCooldownMaxTicks);
    }

    public int failedRamCooldown() {
        return isScreaming() ? FFDConfig.screamingGoatRamCooldownMinTicks
                : FFDConfig.goatRamCooldownMinTicks;
    }

    public int getLongJumpCooldown() {
        return longJumpCooldown;
    }

    public void setLongJumpCooldown(int ticks) {
        longJumpCooldown = Math.max(0, ticks);
    }

    public int nextLongJumpCooldown() {
        return randomRange(FFDConfig.goatLongJumpCooldownMinTicks,
                FFDConfig.goatLongJumpCooldownMaxTicks);
    }

    private int randomRange(int minimum, int maximum) {
        int normalizedMaximum = Math.max(minimum, maximum);
        return minimum + rand.nextInt(normalizedMaximum - minimum + 1);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!FFDItems.isGoatEnabled()) {
            return false;
        }
        BlockPos pos = new BlockPos(this);
        IBlockState below = world.getBlockState(pos.down());
        Block block = below.getBlock();
        boolean validSurface = block == Blocks.GRASS || block == Blocks.DIRT
                || block == Blocks.STONE || block == Blocks.SNOW
                || block == Blocks.SNOW_LAYER || block == Blocks.PACKED_ICE
                || block == Blocks.GRAVEL;
        return validSurface && world.getLight(pos) > 8
                && world.checkNoEntityCollision(getEntityBoundingBox())
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return Math.max(1, FFDConfig.goatMaxGroupSize);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsScreamingGoat", isScreaming());
        compound.setBoolean("HasLeftHorn", hasLeftHorn());
        compound.setBoolean("HasRightHorn", hasRightHorn());
        compound.setInteger("RamCooldown", ramCooldown);
        compound.setInteger("LongJumpCooldown", longJumpCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setScreaming(compound.getBoolean("IsScreamingGoat"));
        setLeftHorn(!compound.hasKey("HasLeftHorn") || compound.getBoolean("HasLeftHorn"));
        setRightHorn(!compound.hasKey("HasRightHorn") || compound.getBoolean("HasRightHorn"));
        setRamCooldown(compound.hasKey("RamCooldown", 99)
                ? compound.getInteger("RamCooldown") : nextRamCooldown());
        setLongJumpCooldown(compound.hasKey("LongJumpCooldown", 99)
                ? compound.getInteger("LongJumpCooldown") : nextLongJumpCooldown());
        setLongJumping(false);
        setLoweringHead(false);
        updateAttackDamage();
    }
}
