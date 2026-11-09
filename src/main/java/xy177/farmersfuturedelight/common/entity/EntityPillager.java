package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIPillagerCrossbowAttack;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIPillagerPatrol;
import xy177.farmersfuturedelight.common.raid.FFDRaidCaptains;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.item.ItemCrossbow;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityPillager extends AbstractIllager implements IRangedAttackMob {
    private static final DataParameter<Boolean> CHARGING_CROSSBOW =
            EntityDataManager.createKey(EntityPillager.class, DataSerializers.BOOLEAN);
    private BlockPos patrolTarget;
    private boolean patrolling;

    public EntityPillager(World world) {
        super(world);
        setSize(0.6F, 1.95F);
        setCanPickUpLoot(true);
        if (!world.isRemote) {
            equipCrossbow();
        }
    }

    public static void registerFixesPillager(DataFixer fixer) {
        net.minecraft.entity.EntityLiving.registerFixesMob(fixer, EntityPillager.class);
    }

    @Override
    public double getYOffset() {
        Entity vehicle = getRidingEntity();
        if (vehicle instanceof net.minecraft.entity.item.EntityBoat) {
            return vehicle.height / 3.0D - 0.6D - vehicle.getMountedYOffset();
        }
        return super.getYOffset();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CHARGING_CROSSBOW, false);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIPillagerCrossbowAttack(this, 1.0D, 8.0F));
        tasks.addTask(4, new EntityAIPillagerPatrol(this));
        tasks.addTask(8, new EntityAIWander(this, 0.6D));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 15.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true,
                EntityPillager.class, net.minecraft.entity.monster.EntityVindicator.class,
                net.minecraft.entity.monster.EntityEvoker.class));
        targetTasks.addTask(2, new net.minecraft.entity.ai.EntityAINearestAttackableTarget<>(
                this, EntityPlayer.class, true));
        targetTasks.addTask(3, new net.minecraft.entity.ai.EntityAINearestAttackableTarget<>(
                this, EntityVillager.class, false));
        targetTasks.addTask(3, new net.minecraft.entity.ai.EntityAINearestAttackableTarget<>(
                this, EntityIronGolem.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(FarmerFutureDelight.MODID, "entities/pillager");
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        setAggressive(1, getAttackTarget() != null);
    }

    @Override
    protected boolean isValidLightLevel() {
        return world.getLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, getPosition()) <= 8;
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL
                && isValidLightLevel() && world.getBlockState(getPosition().down()).canEntitySpawn(this);
    }

    @Override
    protected boolean canDespawn() {
        if (!patrolling) return super.canDespawn();
        EntityPlayer player = world.getClosestPlayerToEntity(this, -1.0D);
        return player != null && getDistanceSq(player) > 16384.0D;
    }

    @Override
    public boolean isOnSameTeam(Entity entity) {
        if (super.isOnSameTeam(entity)) {
            return true;
        }
        return entity instanceof EntityLivingBase
                && ((EntityLivingBase) entity).getCreatureAttribute() == EnumCreatureAttribute.ILLAGER
                && getTeam() == null && entity.getTeam() == null;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                           IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        equipCrossbow();
        setEquipmentBasedOnDifficulty(difficulty);
        setEnchantmentBasedOnDifficulty(difficulty);
        FFDRaidCaptains.onInitialSpawn(this);
        return result;
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        equipCrossbow();
    }

    private void equipCrossbow() {
        net.minecraft.item.Item crossbow = FFDItems.effectiveItem(FFDItems.CROSSBOW);
        if (crossbow != null) {
            setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,
                    new ItemStack(crossbow));
        }
    }

    @Override
    protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEnchantmentBasedOnDifficulty(difficulty);
        ItemStack weapon = getHeldItemMainhand();
        if (weapon.getItem() instanceof ItemCrossbow && rand.nextInt(300) == 0) {
            weapon.addEnchantment(FFDEnchantments.PIERCING, 1);
        }
    }

    public boolean isChargingCrossbow() {
        return dataManager.get(CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean charging) {
        dataManager.set(CHARGING_CROSSBOW, charging);
    }

    public void loadCrossbow() {
        ItemStack weapon = getCrossbow();
        if (weapon.getItem() instanceof ItemCrossbow) {
            ItemCrossbow.loadProjectileForMob(weapon, new ItemStack(Items.ARROW));
        }
    }

    public void fireCrossbow() {
        ItemStack weapon = getCrossbow();
        if (weapon.getItem() instanceof ItemCrossbow
                && ItemCrossbow.isCharged(weapon)) {
            ((ItemCrossbow) weapon.getItem()).fireLoadedProjectiles(world, this, weapon, 1.6F);
        }
    }

    private ItemStack getCrossbow() {
        return getHeldItemMainhand().getItem() instanceof ItemCrossbow
                ? getHeldItemMainhand() : getHeldItemOffhand();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("ChargingCrossbow", isChargingCrossbow());
        compound.setBoolean("Patrolling", patrolling);
        compound.setBoolean("PatrolLeader", FFDRaidCaptains.isLeader(this));
        if (patrolTarget != null) compound.setLong("FFDPatrolTarget", patrolTarget.toLong());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setChargingCrossbow(compound.getBoolean("ChargingCrossbow"));
        patrolling = compound.getBoolean("Patrolling");
        patrolTarget = compound.hasKey("FFDPatrolTarget", 99)
                ? BlockPos.fromLong(compound.getLong("FFDPatrolTarget")) : null;
        if (compound.hasKey("PatrolLeader")) {
            getEntityData().setBoolean("farmers_future_delight:patrol_leader",
                    compound.getBoolean("PatrolLeader"));
        }
    }

    public BlockPos getPatrolTarget() {
        return patrolTarget;
    }

    public void setPatrolTarget(BlockPos target) {
        patrolTarget = target.toImmutable();
        patrolling = true;
    }

    public void findPatrolTarget() {
        setPatrolTarget(getPosition().add(-500 + rand.nextInt(1000), 0,
                -500 + rand.nextInt(1000)));
    }

    public boolean isPatrolling() {
        return patrolling;
    }

    public void setPatrolling(boolean value) {
        patrolling = value;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        fireCrossbow();
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        setChargingCrossbow(swingingArms);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IllagerArmPose getArmPose() {
        return isChargingCrossbow() || getCrossbow().getItem() instanceof ItemCrossbow
                ? IllagerArmPose.BOW_AND_ARROW
                : getAttackTarget() != null ? IllagerArmPose.ATTACKING : IllagerArmPose.CROSSED;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.PILLAGER_HURT;
    }
}
