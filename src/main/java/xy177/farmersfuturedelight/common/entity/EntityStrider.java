package xy177.farmersfuturedelight.common.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class EntityStrider extends EntityAnimal implements net.minecraftforge.common.IShearable {
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(EntityStrider.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> COLD = EntityDataManager.createKey(EntityStrider.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.createKey(EntityStrider.class, DataSerializers.VARINT);
    private static final AttributeModifier COLD_SPEED = new AttributeModifier(
            UUID.fromString("96d4e5bf-502d-4b07-b0f7-c8683ef4adef"), "Strider cold movement", -0.34D, 1).setSaved(false);
    private ItemStack saddle = ItemStack.EMPTY;
    private boolean boosting;
    private int boostTicks;
    private int boostDuration;

    public EntityStrider(World world) {
        super(world);
        setSize(0.9F, 1.7F);
        isImmuneToFire = true;
        stepHeight = 1.0F;
        setPathPriority(PathNodeType.WATER, -1.0F);
        setPathPriority(PathNodeType.LAVA, 0.0F);
        setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
        setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(SADDLED, false);
        dataManager.register(COLD, false);
        dataManager.register(BOOST_TIME, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.175D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new EntityAIPanic(this, 1.65D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.4D, false,
                com.google.common.collect.Sets.newHashSet(FFDItems.WARPED_FUNGUS, FFDItems.WARPED_FUNGUS_ON_A_STICK)));
        tasks.addTask(4, new EntityAIMoveToBlock(this, 1.0D, 8) {
            @Override protected boolean shouldMoveTo(World world, BlockPos pos) {
                return world.getBlockState(pos).getMaterial() == Material.LAVA && world.isAirBlock(pos.up());
            }
            @Override public boolean shouldExecute() { return !isInLava() && super.shouldExecute(); }
            @Override public boolean shouldContinueExecuting() { return !isInLava() && super.shouldContinueExecuting(); }
        });
        tasks.addTask(5, new EntityAIFollowParent(this, 1.0D));
        tasks.addTask(7, new EntityAIWander(this, 1.0D, 60));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        return new PathNavigateGround(this, world) {
            @Override protected PathFinder getPathFinder() {
                nodeProcessor = new WalkNodeProcessor() {
                    @Override public PathNodeType getPathNodeType(IBlockAccess blocks, int posX, int posY, int posZ) {
                        PathNodeType type = super.getPathNodeType(blocks, posX, posY, posZ);
                        return type == PathNodeType.LAVA ? PathNodeType.WALKABLE : type;
                    }
                };
                nodeProcessor.setCanEnterDoors(true);
                return new PathFinder(nodeProcessor);
            }
            @Override protected boolean canNavigate() { return isInLava() || super.canNavigate(); }
        };
    }

    @Override
    public void setScaleForAge(boolean baby) {
        setSize(baby ? 0.45F : 0.9F, baby ? 0.85F : 1.7F);
        setScale(1.0F);
    }

    @Override
    public float getEyeHeight() { return isChild() ? 0.4375F : 1.445F; }

    public boolean isCold() { return dataManager.get(COLD); }

    public void setCold(boolean cold) {
        dataManager.set(COLD, cold);
        net.minecraft.entity.ai.attributes.IAttributeInstance movement = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (movement == null) return;
        if (cold && !movement.hasModifier(COLD_SPEED)) movement.applyModifier(COLD_SPEED);
        else if (!cold) movement.removeModifier(COLD_SPEED);
    }

    public boolean isSaddled() { return dataManager.get(SADDLED); }

    public void equipSaddle(ItemStack stack) {
        saddle = stack.copy();
        if (!saddle.isEmpty()) saddle.setCount(1);
        dataManager.set(SADDLED, !saddle.isEmpty());
        if (!saddle.isEmpty()) playSound(FFDSounds.STRIDER_SADDLE, 0.5F, 1.0F);
    }

    public ItemStack removeSaddle() {
        ItemStack previous = saddle;
        saddle = ItemStack.EMPTY;
        dataManager.set(SADDLED, false);
        return previous;
    }

    @Override
    public boolean isShearable(ItemStack tool, IBlockAccess blocks, BlockPos pos) {
        return isSaddled() && !isBeingRidden() && !net.minecraft.enchantment.EnchantmentHelper.hasBindingCurse(saddle);
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack tool, IBlockAccess blocks, BlockPos pos, int fortune) {
        if (world.isRemote || !isShearable(tool, blocks, pos)) return java.util.Collections.emptyList();
        playSound(net.minecraft.init.SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        return java.util.Collections.singletonList(removeSaddle());
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && !isAIDisabled()) {
            boolean warmRider = getRidingEntity() instanceof EntityStrider && !((EntityStrider) getRidingEntity()).isCold();
            BlockPos pos = new BlockPos(this);
            setCold(!isInLava() && world.getBlockState(pos).getMaterial() != Material.LAVA
                    && world.getBlockState(pos.down()).getMaterial() != Material.LAVA && !warmRider);
        }
        super.onLivingUpdate();
        if (!world.isRemote && isWet()) attackEntityFrom(DamageSource.DROWN, 1.0F);
        if (isInLava()) {
            fallDistance = 0.0F;
            if (world.isRemote && !canPassengerSteer()) return;
            BlockPos lava = new BlockPos(posX, getEntityBoundingBox().minY, posZ);
            if (world.getBlockState(lava.up()).getMaterial() == Material.LAVA || posY < lava.getY() + 0.5D) {
                motionX *= 0.5D; motionY = motionY * 0.5D + 0.05D; motionZ *= 0.5D;
            } else onGround = true;
        }
    }

    @Override
    public void move(MoverType type, double moveX, double moveY, double moveZ) {
        boolean supported = false;
        if (moveY < 0.0D && !noClip) {
            BlockPos lava = new BlockPos(posX + moveX, getEntityBoundingBox().minY - 0.05D, posZ + moveZ);
            double surface = lava.getY() + 0.5D;
            if (world.getBlockState(lava).getMaterial() == Material.LAVA
                    && world.getBlockState(lava.up()).getMaterial() != Material.LAVA
                    && posY >= surface - 0.0001D && posY + moveY <= surface) {
                moveY = surface - posY;
                supported = true;
            }
        }
        super.move(type, moveX, moveY, moveZ);
        if (supported) { onGround = true; motionY = 0.0D; fallDistance = 0.0F; }
    }

    @Override
    public boolean isBurning() { return false; }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !source.isFireDamage() && super.attackEntityFrom(source, amount);
    }

    @Override
    public void fall(float distance, float multiplier) {
        if (!isInLava()) super.fall(distance, multiplier);
    }

    @Override
    public boolean canBeSteered() {
        Entity passenger = getControllingPassenger();
        return isSaddled() && passenger instanceof EntityPlayer
                && (isSteeringItem(((EntityPlayer) passenger).getHeldItemMainhand())
                || isSteeringItem(((EntityPlayer) passenger).getHeldItemOffhand()));
    }

    private static boolean isSteeringItem(ItemStack stack) {
        ItemStack rod = FFDItems.effectiveStack(FFDItems.WARPED_FUNGUS_ON_A_STICK);
        return !stack.isEmpty() && !rod.isEmpty() && stack.getItem() == rod.getItem();
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() { return getPassengers().isEmpty() ? null : getPassengers().get(0); }

    @Override
    protected boolean canFitPassenger(Entity passenger) { return !isBeingRidden() && !areEyesInLava(); }

    private boolean areEyesInLava() { return world.getBlockState(new BlockPos(posX, posY + getEyeHeight(), posZ)).getMaterial() == Material.LAVA; }

    @Override
    public double getMountedYOffset() {
        return (isChild() ? 0.65625D : 1.275D) + (isBeingRidden() ? 0.12D * MathHelper.cos(limbSwing * 1.5F)
                * 2.0D * Math.min(0.25F, limbSwingAmount) : 0.0D);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isBeingRidden() && canBeSteered()) {
            Entity rider = getControllingPassenger();
            prevRotationYaw = rotationYaw = rider.rotationYaw;
            rotationPitch = rider.rotationPitch * 0.5F;
            setRotation(rotationYaw, rotationPitch); renderYawOffset = rotationYaw; rotationYawHead = rotationYaw;
            if (boosting && boostTicks++ > boostDuration) boosting = false;
            if (canPassengerSteer()) {
                float speed = (float) getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                        * (isCold() ? 0.35F : 0.55F);
                if (boosting) speed += speed * 1.15F * MathHelper.sin((float) boostTicks / boostDuration * (float) Math.PI);
                setAIMoveSpeed(speed);
                travelStrider(0.0F, 0.0F, 1.0F);
            } else { motionX = 0; motionY = 0; motionZ = 0; }
        } else travelStrider(strafe, vertical, forward);
    }

    private void travelStrider(float strafe, float vertical, float forward) {
        if (!isInLava()) { super.travel(strafe, vertical, forward); return; }
        if (isServerWorld() || canPassengerSteer()) {
            moveRelative(strafe, vertical, forward, getAIMoveSpeed());
            motionY -= 0.08D;
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.546D; motionZ *= 0.546D; motionY *= 0.98D;
        }
        prevLimbSwingAmount = limbSwingAmount;
        float movement = Math.min(1.0F, MathHelper.sqrt((posX - prevPosX) * (posX - prevPosX)
                + (posZ - prevPosZ) * (posZ - prevPosZ)) * 4.0F);
        limbSwingAmount += (movement - limbSwingAmount) * 0.4F; limbSwing += limbSwingAmount;
    }

    public boolean boost() {
        if (boosting) return false;
        boosting = true; boostTicks = 0; boostDuration = rand.nextInt(841) + 140;
        dataManager.set(BOOST_TIME, boostDuration);
        return true;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {
        if (BOOST_TIME.equals(parameter) && world.isRemote) {
            boosting = true; boostTicks = 0; boostDuration = dataManager.get(BOOST_TIME);
        }
        super.notifyDataManagerChange(parameter);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        boolean food = isBreedingItem(held);
        if (!food && isSaddled() && !isBeingRidden() && !player.isSneaking()) {
            if (!world.isRemote) player.startRiding(this);
            return true;
        }
        if (super.processInteract(player, hand)) {
            if (food) playSound(FFDSounds.STRIDER_EAT, 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            return true;
        }
        if (held.getItem() == Items.SADDLE && !isChild() && !isSaddled()) {
            if (!world.isRemote) {
                equipSaddle(held);
                if (!player.capabilities.isCreativeMode) held.shrink(1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        ItemStack food = FFDItems.effectiveStack(FFDItems.WARPED_FUNGUS);
        return !stack.isEmpty() && !food.isEmpty() && ItemStack.areItemsEqual(stack, food);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable partner) { return new EntityStrider(world); }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.LAVA ? 10.0F : isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos surface = new BlockPos(this);
        while (world.getBlockState(surface).getMaterial() == Material.LAVA) surface = surface.up();
        return world.isAirBlock(surface) && world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        if (data instanceof SpawnData && rand.nextFloat() < 0.5F) setGrowingAge(-24000);
        if (!isChild() && rand.nextInt(30) == 0 && world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL) {
            EntityPigZombie rider = new EntityPigZombie(world);
            rider.copyLocationAndAnglesFrom(this); rider.onInitialSpawn(difficulty, null);
            rider.setHeldItem(EnumHand.MAIN_HAND, FFDItems.effectiveStack(FFDItems.WARPED_FUNGUS_ON_A_STICK));
            equipSaddle(new ItemStack(Items.SADDLE));
            world.spawnEntity(rider); rider.startRiding(this, true);
        } else if (!isChild() && rand.nextInt(10) == 0) {
            EntityStrider child = new EntityStrider(world); child.setGrowingAge(-24000); child.copyLocationAndAnglesFrom(this);
            world.spawnEntity(child); child.startRiding(this, true);
        }
        super.onInitialSpawn(difficulty, data);
        return data == null ? new SpawnData() : data;
    }

    @Override
    protected void dropEquipment(boolean recentlyHit, int looting) {
        super.dropEquipment(recentlyHit, looting);
        if (!saddle.isEmpty()) entityDropItem(removeSaddle(), 0.0F);
    }

    @Override
    protected void dropLoot(boolean recentlyHit, int looting, DamageSource source) {
        if (!isChild()) super.dropLoot(recentlyHit, looting, source);
    }

    @Override protected SoundEvent getAmbientSound() { return FFDSounds.STRIDER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return FFDSounds.STRIDER_HURT; }
    @Override protected SoundEvent getDeathSound() { return FFDSounds.STRIDER_DEATH; }
    @Override protected void playStepSound(BlockPos pos, Block block) { playSound(isInLava() ? FFDSounds.STRIDER_STEP_LAVA : FFDSounds.STRIDER_STEP, 1.0F, 1.0F); }
    @Override protected ResourceLocation getLootTable() { return new ResourceLocation(FarmerFutureDelight.MODID, "entities/strider"); }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Saddle", isSaddled()); compound.setBoolean("Suffocating", isCold());
        if (!saddle.isEmpty()) compound.setTag("SaddleItem", saddle.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        saddle = compound.hasKey("SaddleItem", 10) ? new ItemStack(compound.getCompoundTag("SaddleItem"))
                : compound.getBoolean("Saddle") ? new ItemStack(Items.SADDLE) : ItemStack.EMPTY;
        dataManager.set(SADDLED, !saddle.isEmpty()); setCold(compound.getBoolean("Suffocating"));
    }

    private static final class SpawnData implements IEntityLivingData { }
}
