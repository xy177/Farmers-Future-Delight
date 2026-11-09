package xy177.farmersfuturedelight.common.entity;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.entity.SharedMonsterAttributes;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityRavager extends AbstractIllager {
    private static final DataParameter<Integer> ATTACK_TICK =
            EntityDataManager.createKey(EntityRavager.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STUN_TICK =
            EntityDataManager.createKey(EntityRavager.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ROAR_TICK =
            EntityDataManager.createKey(EntityRavager.class, DataSerializers.VARINT);

    public EntityRavager(World world) {
        super(world);
        setSize(1.95F, 2.2F);
        stepHeight = 1.0F;
        experienceValue = 20;
    }

    public static void registerFixesRavager(DataFixer fixer) {
        net.minecraft.entity.EntityLiving.registerFixesMob(fixer, EntityRavager.class);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK_TICK, 0);
        dataManager.register(STUN_TICK, 0);
        dataManager.register(ROAR_TICK, 0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
        tasks.addTask(5, new EntityAIWander(this, 0.4D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
        tasks.addTask(11, new EntityAILookIdle(this));
        targetTasks.addTask(2, new EntityAIHurtByTarget(this, true, AbstractIllager.class));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this,
                EntityPlayer.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this,
                EntityVillager.class, 0, true, false,
                (Predicate<EntityVillager>) villager -> !villager.isChild()));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this,
                EntityIronGolem.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        boolean riddenByRaider = false;
        for (Entity passenger : getPassengers()) {
            if (passenger instanceof EntityLiving
                    && ((EntityLiving) passenger).getCreatureAttribute()
                    == net.minecraft.entity.EnumCreatureAttribute.ILLAGER) {
                riddenByRaider = true;
                break;
            }
        }
        tasks.setControlFlag(1, riddenByRaider);
        tasks.setControlFlag(2, riddenByRaider);
        tasks.setControlFlag(4, riddenByRaider
                && !(isRiding() && getRidingEntity() instanceof EntityBoat));
        targetTasks.setControlFlag(8, riddenByRaider);
    }

    @Override
    public boolean canEntityBeSeen(Entity entity) {
        return getStunTick() <= 0 && getRoarTick() <= 0 && super.canEntityBeSeen(entity);
    }

    @Override
    public boolean isOnSameTeam(Entity entity) {
        if (super.isOnSameTeam(entity)) {
            return true;
        }
        return entity instanceof EntityLivingBase
                && ((EntityLivingBase) entity).getCreatureAttribute()
                == net.minecraft.entity.EnumCreatureAttribute.ILLAGER
                && getTeam() == null && entity.getTeam() == null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isEntityAlive()) {
            return;
        }
        if (world.isRemote) {
            if (getStunTick() > 0 && rand.nextInt(6) == 0) {
                double x = posX - width * Math.sin(renderYawOffset * Math.PI / 180.0D)
                        + (rand.nextDouble() * 0.6D - 0.3D);
                double y = posY + height - 0.3D;
                double z = posZ + width * Math.cos(renderYawOffset * Math.PI / 180.0D)
                        + (rand.nextDouble() * 0.6D - 0.3D);
                world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z,
                        0.498D, 0.514D, 0.573D);
            }
            return;
        }
        updateMovementSpeed();
        if (isImmobile()) {
            motionX = 0.0D;
            motionZ = 0.0D;
            getNavigator().clearPath();
        }
        destroyLeavesOnCollision();
        if (getRoarTick() > 0) {
            setRoarTick(getRoarTick() - 1);
            if (getRoarTick() == 10) {
                roar();
            }
        }
        if (getAttackTick() > 0) {
            setAttackTick(getAttackTick() - 1);
        }
        if (getStunTick() > 0) {
            setStunTick(getStunTick() - 1);
            if (getStunTick() == 0) {
                playSound(FFDSounds.RAVAGER_ROAR, 1.0F, 1.0F);
                setRoarTick(20);
            }
        }
    }

    private void updateMovementSpeed() {
        double targetSpeed = isImmobile() ? 0.0D : getAttackTarget() == null ? 0.3D : 0.35D;
        double currentSpeed = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .getBaseValue();
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(currentSpeed + (targetSpeed - currentSpeed) * 0.1D);
    }

    private void destroyLeavesOnCollision() {
        if (!collidedHorizontally || !world.getGameRules().getBoolean("mobGriefing")) {
            return;
        }
        AxisAlignedBB box = getEntityBoundingBox().grow(0.2D);
        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);
        int maxX = MathHelper.floor(box.maxX);
        int maxY = MathHelper.floor(box.maxY);
        int maxZ = MathHelper.floor(box.maxZ);
        boolean destroyed = false;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
                    if (state.getBlock().isLeaves(state, world, pos)) {
                        destroyed = world.destroyBlock(pos, true) || destroyed;
                    }
                }
            }
        }
        if (!destroyed && onGround) {
            jumpHelper.setJumping();
        }
    }

    private boolean isImmobile() {
        return getAttackTick() > 0 || getStunTick() > 0 || getRoarTick() > 0;
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && !world.containsAnyLiquid(getEntityBoundingBox());
    }

    private void roar() {
        if (!(world instanceof WorldServer)) {
            return;
        }
        boolean mobGriefing = world.getGameRules().getBoolean("mobGriefing");
        AxisAlignedBB box = getEntityBoundingBox().grow(4.0D);
        for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            if (target == this || !target.isEntityAlive()
                    || (mobGriefing && target instanceof net.minecraft.entity.item.EntityArmorStand)) {
                continue;
            }
            if (!(target instanceof AbstractIllager)) {
                target.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F);
            }
            if (!(target instanceof EntityPlayer)) {
                applyStrongKnockback(target);
            }
        }
        world.setEntityState(this, (byte) 69);
    }

    private void applyStrongKnockback(Entity target) {
        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        double distance = Math.max(dx * dx + dz * dz, 0.001D);
        target.addVelocity(dx / distance * 4.0D, 0.2D, dz / distance * 4.0D);
        target.velocityChanged = true;
    }

    public void applyClientRoarKnockback(Entity target) {
        if (target != this && target.isEntityAlive()) {
            applyStrongKnockback(target);
        }
    }

    private void setAttackTick(int value) {
        dataManager.set(ATTACK_TICK, value);
    }

    public int getAttackTick() {
        return dataManager.get(ATTACK_TICK);
    }

    private void setStunTick(int value) {
        dataManager.set(STUN_TICK, value);
    }

    public int getStunTick() {
        return dataManager.get(STUN_TICK);
    }

    private void setRoarTick(int value) {
        dataManager.set(ROAR_TICK, value);
    }

    public int getRoarTick() {
        return dataManager.get(ROAR_TICK);
    }

    public boolean canBeLeader() {
        return false;
    }

    public SoundEvent getCelebrateSound() {
        return FFDSounds.RAVAGER_CELEBRATE;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        setAttackTick(10);
        world.setEntityState(this, (byte) 4);
        playSound(FFDSounds.RAVAGER_ATTACK, 1.0F, 1.0F);
        boolean shieldBlocked = isShieldBlocking(target);
        boolean attacked = super.attackEntityAsMob(target);
        if (attacked && !shieldBlocked && target instanceof EntityLivingBase) {
            EntityLivingBase victim = (EntityLivingBase) target;
            victim.knockBack(this, 1.5F, posX - victim.posX, posZ - victim.posZ);
        }
        if (attacked && shieldBlocked && getRoarTick() == 0
                && target instanceof EntityLivingBase) {
            EntityLivingBase defender = (EntityLivingBase) target;
            if (rand.nextBoolean()) {
                setStunTick(40);
                playSound(FFDSounds.RAVAGER_STUNNED, 1.0F, 1.0F);
                world.setEntityState(this, (byte) 39);
                defender.knockBack(this, 1.0F, posX - defender.posX, posZ - defender.posZ);
            } else {
                applyStrongKnockback(defender);
            }
            defender.velocityChanged = true;
        }
        return attacked;
    }

    private boolean isShieldBlocking(Entity target) {
        if (!(target instanceof EntityLivingBase)) {
            return false;
        }
        EntityLivingBase defender = (EntityLivingBase) target;
        ItemStack active = defender.getActiveItemStack();
        if (!defender.isActiveItemStackBlocking() || active.isEmpty()
                || active.getItemUseAction() != EnumAction.BLOCK) {
            return false;
        }
        Vec3d look = defender.getLookVec();
        double dx = posX - defender.posX;
        double dz = posZ - defender.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        return length > 0.0D && (look.x * dx + look.z * dz) / length >= 0.0D;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 4) {
            setAttackTick(10);
        } else if (id == 39) {
            setStunTick(40);
        } else if (id == 69) {
            addRoarParticles();
            FarmerFutureDelight.proxy.handleRavagerRoar(this);
        }
        super.handleStatusUpdate(id);
    }

    private void addRoarParticles() {
        Vec3d center = getEntityBoundingBox().getCenter();
        for (int i = 0; i < 40; i++) {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, center.x, center.y, center.z,
                    rand.nextGaussian() * 0.2D,
                    rand.nextGaussian() * 0.2D,
                    rand.nextGaussian() * 0.2D);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("AttackTick", getAttackTick());
        compound.setInteger("StunTick", getStunTick());
        compound.setInteger("RoarTick", getRoarTick());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setAttackTick(compound.getInteger("AttackTick"));
        setStunTick(compound.getInteger("StunTick"));
        setRoarTick(compound.getInteger("RoarTick"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.Block block) {
        playSound(FFDSounds.RAVAGER_STEP, 0.15F, 1.0F);
    }

    @Override
    public ResourceLocation getLootTable() {
        return new ResourceLocation(FarmerFutureDelight.MODID, "entities/ravager");
    }
}
