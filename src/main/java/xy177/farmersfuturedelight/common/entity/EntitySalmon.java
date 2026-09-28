package xy177.farmersfuturedelight.common.entity;

import net.minecraft.init.Items;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

import javax.annotation.Nullable;

public class EntitySalmon extends EntitySchoolingFish {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(
            EntitySalmon.class, DataSerializers.VARINT);

    public EntitySalmon(World world) {
        super(world);
        updateSize();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, Variant.MEDIUM.id);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (VARIANT.equals(key)) {
            updateSize();
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        int roll = rand.nextInt(95);
        setVariant(roll < 30 ? Variant.SMALL : roll < 80 ? Variant.MEDIUM : Variant.LARGE);
        return super.onInitialSpawn(difficulty, livingData);
    }

    @Override
    protected int getMaxSchoolSize() {
        return 5;
    }

    @Override
    protected ItemStack getBucketStack() {
        return FFDItems.effectiveStack(FFDItems.SALMON_BUCKET);
    }

    @Override
    protected void writeToBucket(ItemStack bucket) {
        super.writeToBucket(bucket);
        bucket.getOrCreateSubCompound("EntityTag").setString("type", getVariant().name);
    }

    @Override
    public void readFromBucket(ItemStack bucket) {
        super.readFromBucket(bucket);
        if (bucket.hasTagCompound() && bucket.getTagCompound().hasKey("EntityTag", 10)) {
            NBTTagCompound entityTag = bucket.getTagCompound().getCompoundTag("EntityTag");
            if (entityTag.hasKey("type", 8)) {
                setVariant(Variant.byName(entityTag.getString("type")));
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("type", getVariant().name);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setVariant(compound.hasKey("type", 8)
                ? Variant.byName(compound.getString("type")) : Variant.MEDIUM);
    }

    public Variant getVariant() {
        return Variant.byId(dataManager.get(VARIANT));
    }

    public void setVariant(Variant variant) {
        dataManager.set(VARIANT, variant == null ? Variant.MEDIUM.id : variant.id);
        updateSize();
    }

    public float getSalmonScale() {
        return getVariant().scale;
    }

    private void updateSize() {
        float scale = getSalmonScale();
        setSize(0.7F * scale, 0.4F * scale);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        entityDropItem(new ItemStack(isBurning() ? Items.COOKED_FISH : Items.FISH, 1, 1), 0.0F);
        if (rand.nextFloat() < 0.05F) {
            entityDropItem(new ItemStack(Items.DYE, 1, 15), 0.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.SALMON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return FFDSounds.SALMON_FLOP;
    }

    public enum Variant {
        SMALL(0, "small", 0.5F),
        MEDIUM(1, "medium", 1.0F),
        LARGE(2, "large", 1.5F);

        private final int id;
        private final String name;
        private final float scale;

        Variant(int id, String name, float scale) {
            this.id = id;
            this.name = name;
            this.scale = scale;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public float getScale() {
            return scale;
        }

        public static Variant byId(int id) {
            if (id <= SMALL.id) {
                return SMALL;
            }
            return id >= LARGE.id ? LARGE : MEDIUM;
        }

        public static Variant byName(String name) {
            for (Variant variant : values()) {
                if (variant.name.equalsIgnoreCase(name)) {
                    return variant;
                }
            }
            return MEDIUM;
        }
    }
}
