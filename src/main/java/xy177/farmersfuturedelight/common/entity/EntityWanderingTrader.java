package xy177.farmersfuturedelight.common.entity;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration;
import xy177.farmersfuturedelight.common.registry.FFDWanderingTraderTrades;

public class EntityWanderingTrader extends EntityAgeable implements IMerchant, net.minecraft.entity.INpc {
    private EntityPlayer customer;
    private MerchantRecipeList offers;
    private int despawnDelay;
    private BlockPos wanderTarget;
    private boolean hasTraded;
    private String tradeProfileKey = "";

    public EntityWanderingTrader(World world) {
        super(world);
        setSize(0.6F, 1.95F);
        ((PathNavigateGround) getNavigator()).setCanSwim(true);
        ((PathNavigateGround) getNavigator()).setBreakDoors(true);
        setPathPriority(PathNodeType.DANGER_FIRE, 16.0F);
        setPathPriority(PathNodeType.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.7D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(0, new DrinkGoal());
        tasks.addTask(1, new TradeGoal());
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityZombie.class, 8.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityEvoker.class, 12.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityVindicator.class, 8.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityVex.class, 8.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityPillager.class, 15.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityIllusionIllager.class, 12.0F, 0.5D, 0.5D));
        tasks.addTask(1, new EntityAIPanic(this, 0.5D));
        tasks.addTask(2, new WanderTargetGoal());
        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 0.35D));
        tasks.addTask(8, new EntityAIWanderAvoidWater(this, 0.35D));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        tasks.addTask(10, new EntityAILookIdle(this));
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        return null;
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
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() == Items.SPAWN_EGG
                || !isEntityAlive() || isChild() || customer != null) {
            return super.processInteract(player, hand);
        }
        if (hand == EnumHand.MAIN_HAND) player.addStat(StatList.TALKED_TO_VILLAGER);
        if (!world.isRemote && !getRecipes(player).isEmpty()) {
            setCustomer(player);
            player.displayVillagerTradeGui(this);
        }
        return true;
    }

    @Override
    public void setCustomer(@Nullable EntityPlayer player) {
        customer = player;
    }

    @Nullable
    @Override
    public EntityPlayer getCustomer() {
        return customer;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (offers == null) {
            offers = FFDWanderingTraderTrades.create(rand);
            FFDTradeConfiguration.applyWanderingTrader(offers, 1, rand);
            tradeProfileKey = FFDTradeConfiguration.getWanderingTraderProfileKey();
        }
        return offers;
    }

    @Override
    public void setRecipes(@Nullable MerchantRecipeList recipes) {
        offers = recipes;
    }

    @Override
    public void useRecipe(MerchantRecipe recipe) {
        recipe.incrementToolUses();
        hasTraded = true;
        livingSoundTime = -getTalkInterval();
        playSound(FFDSounds.WANDERING_TRADER_YES, getSoundVolume(), getSoundPitch());
        if (!world.isRemote && recipe.getRewardsExp()) {
            world.spawnEntity(new EntityXPOrb(world, posX, posY + 0.5D, posZ, 3 + rand.nextInt(4)));
        }
        if (customer instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.registry.FFDMerchantMenus.send(
                    (net.minecraft.entity.player.EntityPlayerMP) customer);
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack) {
        if (!world.isRemote && livingSoundTime > -getTalkInterval() + 20) {
            livingSoundTime = -getTalkInterval();
            playSound(stack.isEmpty() ? FFDSounds.WANDERING_TRADER_NO
                    : FFDSounds.WANDERING_TRADER_YES, getSoundVolume(), getSoundPitch());
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getPos() {
        return getPosition();
    }

    public int getDespawnDelay() {
        return despawnDelay;
    }

    public void setDespawnDelay(int ticks) {
        despawnDelay = ticks;
    }

    public void setWanderTarget(@Nullable BlockPos target) {
        wanderTarget = target == null ? null : target.toImmutable();
    }

    @Nullable
    public BlockPos getWanderTarget() {
        return wanderTarget;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && despawnDelay > 0 && customer == null && --despawnDelay == 0) {
            setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("DespawnDelay", despawnDelay);
        compound.setBoolean("HasTraded", hasTraded);
        compound.setString("TradeProfile", tradeProfileKey);
        if (offers != null) compound.setTag("Offers", offers.getRecipiesAsTags());
        if (wanderTarget != null) compound.setLong("WanderTarget", wanderTarget.toLong());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        despawnDelay = compound.getInteger("DespawnDelay");
        hasTraded = compound.getBoolean("HasTraded");
        offers = compound.hasKey("Offers", 10) ? new MerchantRecipeList(compound.getCompoundTag("Offers")) : null;
        tradeProfileKey = compound.getString("TradeProfile");
        if (offers != null) {
            for (MerchantRecipe recipe : offers) hasTraded |= recipe.getToolUses() > 0;
        }
        if (offers != null && !hasTraded && FFDTradeConfiguration.shouldUpdateExistingUntradedVillagers()
                && !tradeProfileKey.equals(FFDTradeConfiguration.getWanderingTraderProfileKey())) {
            offers = null;
        }
        wanderTarget = compound.hasKey("WanderTarget", 99) ? BlockPos.fromLong(compound.getLong("WanderTarget")) : null;
        setGrowingAge(Math.max(0, getGrowingAge()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return customer == null ? FFDSounds.WANDERING_TRADER_AMBIENT : FFDSounds.WANDERING_TRADER_TRADE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damage) {
        return FFDSounds.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.WANDERING_TRADER_DEATH;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        setCustomer(null);
    }

    @Override
    protected void updateItemUse(ItemStack stack, int particleCount) {
        if (stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Items.POTIONITEM) {
            playSound(stack.getItem() == Items.MILK_BUCKET ? FFDSounds.WANDERING_TRADER_DRINK_MILK
                    : FFDSounds.WANDERING_TRADER_DRINK_POTION, 0.5F, 0.9F + rand.nextFloat() * 0.1F);
        } else {
            super.updateItemUse(stack, particleCount);
        }
    }

    private final class TradeGoal extends EntityAIBase {
        private TradeGoal() {
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return isEntityAlive() && !isInWater() && onGround && !velocityChanged
                    && customer != null && customer.isEntityAlive() && customer.world == world
                    && getDistanceSq(customer) <= 16.0D
                    && customer.openContainer instanceof net.minecraft.inventory.ContainerMerchant;
        }

        @Override
        public void startExecuting() {
            getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (customer != null) getLookHelper().setLookPositionWithEntity(customer, 10.0F, getVerticalFaceSpeed());
        }

        @Override
        public void resetTask() {
            setCustomer(null);
        }
    }

    private final class DrinkGoal extends EntityAIBase {
        private boolean milk;
        private boolean wasInvisible;

        @Override
        public boolean shouldExecute() {
            return world.isDaytime() == isInvisible();
        }

        @Override
        public void startExecuting() {
            milk = world.isDaytime();
            wasInvisible = isInvisible();
            setHeldItem(EnumHand.MAIN_HAND, milk ? new ItemStack(Items.MILK_BUCKET)
                    : PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.INVISIBILITY));
            setActiveHand(EnumHand.MAIN_HAND);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return isHandActive();
        }

        @Override
        public void resetTask() {
            resetActiveHand();
            setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            if (wasInvisible != isInvisible()) playSound(isInvisible()
                    ? FFDSounds.WANDERING_TRADER_DISAPPEARED : FFDSounds.WANDERING_TRADER_REAPPEARED,
                    1.0F, 0.9F + rand.nextFloat() * 0.2F);
        }
    }

    private final class WanderTargetGoal extends EntityAIBase {
        private WanderTargetGoal() {
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return wanderTarget != null && getDistanceSqToCenter(wanderTarget) >= 4.0D;
        }

        @Override
        public void updateTask() {
            if (wanderTarget == null || !getNavigator().noPath()) return;
            Vec3d target = new Vec3d(wanderTarget);
            if (getDistanceSqToCenter(wanderTarget) >= 100.0D) {
                target = target.subtract(getPositionVector()).normalize().scale(10.0D).add(getPositionVector());
            }
            BlockPos position = new BlockPos(target);
            if (world.isBlockLoaded(position)) getNavigator().tryMoveToXYZ(target.x, target.y, target.z, 0.35D);
        }

        @Override
        public void resetTask() {
            setWanderTarget(null);
            getNavigator().clearPath();
        }
    }
}
