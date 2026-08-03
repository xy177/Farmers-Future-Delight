package xy177.farmersfuturedelight.common.entity;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityGlowSquid extends EntitySquid {
    private static final DataParameter<Boolean> CHILD =
            EntityDataManager.createKey(EntityGlowSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DARK_TICKS =
            EntityDataManager.createKey(EntityGlowSquid.class, DataSerializers.VARINT);
    private static final ResourceLocation LOOT_TABLE =
            new ResourceLocation(FarmerFutureDelight.MODID, "entities/glow_squid");
    private int growingAge;
    private int fleeTicks;

    public EntityGlowSquid(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(1, new AIFlee(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CHILD, false);
        dataManager.register(DARK_TICKS, 0);
    }

    @Override
    public boolean isChild() {
        return dataManager.get(CHILD);
    }

    public void setChild(boolean child) {
        setGrowingAge(child ? -24000 : 0);
    }

    public int getGrowingAge() {
        return world.isRemote ? (isChild() ? -1 : 0) : growingAge;
    }

    public void setGrowingAge(int age) {
        boolean wasChild = growingAge < 0;
        growingAge = age;
        boolean child = age < 0;
        if (wasChild != child || dataManager.get(CHILD) != child) {
            dataManager.set(CHILD, child);
            setSize(child ? 0.5F : 0.8F, child ? 0.5F : 0.8F);
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                              IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        GlowSquidGroupData groupData = livingdata instanceof GlowSquidGroupData
                ? (GlowSquidGroupData) livingdata : new GlowSquidGroupData();
        if (groupData.groupSize > 0 && rand.nextFloat() <= 0.05F) {
            setGrowingAge(-24000);
        }
        groupData.groupSize++;
        return groupData;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (CHILD.equals(key)) {
            boolean child = dataManager.get(CHILD);
            setSize(child ? 0.5F : 0.8F, child ? 0.5F : 0.8F);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) {
            FarmerFutureDelight.proxy.spawnGlowParticle(this);
            return;
        }
        if (isEntityAlive() && growingAge < 0) {
            setGrowingAge(growingAge + 1);
        }
        if (getDarkTicksRemaining() > 0) {
            setDarkTicks(getDarkTicksRemaining() - 1);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt && !world.isRemote) {
            setDarkTicks(100);
            if (source.getTrueSource() instanceof EntityLivingBase) {
                playSound(FFDSounds.GLOW_SQUID_SQUIRT, getSoundVolume(), 1.0F);
                world.setEntityState(this, (byte) 20);
            }
        }
        return hurt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 20) {
            FarmerFutureDelight.proxy.spawnGlowInkParticles(this);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return FFDEntities.isGlowSquidEnabled()
                && world.provider.getDimension() == 0
                && pos.getY() <= world.getSeaLevel() - FFDConfig.glowSquidDepthBelowSeaLevel
                && world.getLight(pos, true) == 0
                && world.getBlockState(pos).getBlock() == Blocks.WATER
                && world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Age", growingAge);
        compound.setInteger("DarkTicksRemaining", getDarkTicksRemaining());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setGrowingAge(compound.hasKey("Age") ? compound.getInteger("Age")
                : compound.getBoolean("IsBaby") ? -24000 : 0);
        setDarkTicks(compound.getInteger("DarkTicksRemaining"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        int packed = super.getBrightnessForRender();
        int glow = Math.max(0, Math.min(15,
                (int) ((1.0F - getDarkTicksRemaining() / 10.0F) * 15.0F)));
        int sky = packed & 0xFFFF0000;
        int block = packed & 0x0000FFFF;
        return sky | Math.max(block, glow << 4);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.GLOW_SQUID_DEATH;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return FFDEntities.isGlowSquidEnabled() ? LOOT_TABLE : null;
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        return FFDEntities.isGlowSquidEnabled() ? super.getPickedResult(target) : ItemStack.EMPTY;
    }

    public int getDarkTicksRemaining() {
        return dataManager.get(DARK_TICKS);
    }

    private void setDarkTicks(int ticks) {
        dataManager.set(DARK_TICKS, Math.max(0, ticks));
    }

    private void updateFleeBehavior() {
        EntityLivingBase attacker = getRevengeTarget();
        if (!isInWater() || attacker == null || !attacker.isEntityAlive()
                || getDistanceSq(attacker) >= 100.0D) {
            fleeTicks = 0;
            return;
        }

        fleeTicks++;
        Vec3d away = new Vec3d(posX - attacker.posX, posY - attacker.posY, posZ - attacker.posZ);
        BlockPos targetPos = new BlockPos(posX + away.x, posY + away.y, posZ + away.z);
        Material targetMaterial = world.getBlockState(targetPos).getMaterial();
        if (targetMaterial == Material.WATER || world.isAirBlock(targetPos)) {
            double distance = away.lengthVector();
            if (distance > 0.0D) {
                away.normalize();
                double speed = 3.0D;
                if (distance > 5.0D) {
                    speed -= (distance - 5.0D) / 5.0D;
                }
                if (speed > 0.0D) {
                    away = away.scale(speed);
                }
            }
            if (world.isAirBlock(targetPos)) {
                away = new Vec3d(away.x, 0.0D, away.z);
            }
            setMovementVector((float) (away.x / 20.0D),
                    (float) (away.y / 20.0D),
                    (float) (away.z / 20.0D));
        }

        if (fleeTicks % 10 == 5 && world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                    posX, posY, posZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static final class AIFlee extends EntityAIBase {
        private final EntityGlowSquid squid;

        private AIFlee(EntityGlowSquid squid) {
            this.squid = squid;
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase attacker = squid.getRevengeTarget();
            return squid.isInWater() && attacker != null && attacker.isEntityAlive()
                    && squid.getDistanceSq(attacker) < 100.0D;
        }

        @Override
        public void startExecuting() {
            squid.fleeTicks = 0;
        }

        @Override
        public void resetTask() {
            squid.fleeTicks = 0;
        }

        @Override
        public void updateTask() {
            squid.updateFleeBehavior();
        }
    }

    private static final class GlowSquidGroupData implements IEntityLivingData {
        private int groupSize;
    }

}
