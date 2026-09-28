package xy177.farmersfuturedelight.common.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityGlowSquid extends EntitySquid {
    private static final DataParameter<Boolean> CHILD =
            EntityDataManager.createKey(EntityGlowSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DARK_TICKS =
            EntityDataManager.createKey(EntityGlowSquid.class, DataSerializers.VARINT);
    private static final ResourceLocation LOOT_TABLE =
            new ResourceLocation(FarmerFutureDelight.MODID, "entities/glow_squid");
    private int growingAge;
    public EntityGlowSquid(World world) {
        super(world);
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
        if (isEntityAlive() && growingAge != 0) {
            setGrowingAge(growingAge + (growingAge < 0 ? 1 : -1));
        }
        if (getDarkTicksRemaining() > 0) {
            setDarkTicks(getDarkTicksRemaining() - 1);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt && !world.isRemote && getRevengeTarget() != null) {
            setDarkTicks(100);
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
                && world.getLight(pos) == 0
                && world.getBlockState(pos).getMaterial() == Material.WATER
                && world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Override
    public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
        return !(forSpawnCount && type == EnumCreatureType.WATER_CREATURE)
                && super.isCreatureType(type, forSpawnCount);
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
        return FFDItems.isItemRegistered(FFDItems.GLOW_INK_SAC) ? LOOT_TABLE : null;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        ItemStack ink = FFDItems.effectiveStack(FFDItems.GLOW_INK_SAC);
        if (ink.isEmpty()) {
            return;
        }
        ink.setCount(1 + rand.nextInt(3)
                + (lootingModifier <= 0 ? 0 : rand.nextInt(lootingModifier + 1)));
        entityDropItem(ink, 0.0F);
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

    private static final class GlowSquidGroupData implements IEntityLivingData {
        private int groupSize;
    }

}
