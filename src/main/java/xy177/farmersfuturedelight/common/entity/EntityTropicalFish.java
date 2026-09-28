package xy177.farmersfuturedelight.common.entity;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityTropicalFish extends EntitySchoolingFish {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(
            EntityTropicalFish.class, DataSerializers.VARINT);
    private static final ResourceLocation[] BASE_TEXTURES = {
            texture("tropical_a.png"), texture("tropical_b.png")
    };
    private static final ResourceLocation[][] PATTERN_TEXTURES = {
            patternTextures("tropical_a_pattern_"), patternTextures("tropical_b_pattern_")
    };
    public static final int[] PREDEFINED_VARIANTS = {
            variant(Type.STRIPEY, EnumDyeColor.ORANGE, EnumDyeColor.GRAY),
            variant(Type.FLOPPER, EnumDyeColor.GRAY, EnumDyeColor.GRAY),
            variant(Type.FLOPPER, EnumDyeColor.GRAY, EnumDyeColor.BLUE),
            variant(Type.CLAYFISH, EnumDyeColor.WHITE, EnumDyeColor.GRAY),
            variant(Type.SUNSTREAK, EnumDyeColor.BLUE, EnumDyeColor.GRAY),
            variant(Type.KOB, EnumDyeColor.ORANGE, EnumDyeColor.WHITE),
            variant(Type.SPOTTY, EnumDyeColor.PINK, EnumDyeColor.LIGHT_BLUE),
            variant(Type.BLOCKFISH, EnumDyeColor.PURPLE, EnumDyeColor.YELLOW),
            variant(Type.CLAYFISH, EnumDyeColor.WHITE, EnumDyeColor.RED),
            variant(Type.SPOTTY, EnumDyeColor.WHITE, EnumDyeColor.YELLOW),
            variant(Type.GLITTER, EnumDyeColor.WHITE, EnumDyeColor.GRAY),
            variant(Type.CLAYFISH, EnumDyeColor.WHITE, EnumDyeColor.ORANGE),
            variant(Type.DASHER, EnumDyeColor.CYAN, EnumDyeColor.PINK),
            variant(Type.BRINELY, EnumDyeColor.LIME, EnumDyeColor.LIGHT_BLUE),
            variant(Type.BETTY, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variant(Type.SNOOPER, EnumDyeColor.GRAY, EnumDyeColor.RED),
            variant(Type.BLOCKFISH, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variant(Type.FLOPPER, EnumDyeColor.WHITE, EnumDyeColor.YELLOW),
            variant(Type.KOB, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variant(Type.SUNSTREAK, EnumDyeColor.GRAY, EnumDyeColor.WHITE),
            variant(Type.DASHER, EnumDyeColor.CYAN, EnumDyeColor.YELLOW),
            variant(Type.FLOPPER, EnumDyeColor.YELLOW, EnumDyeColor.YELLOW)
    };
    private boolean schooling = true;

    public EntityTropicalFish(World world) {
        super(world);
        setSize(0.5F, 0.4F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, 0);
    }

    public int getVariant() {
        return dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        dataManager.set(VARIANT, variant);
    }

    public int getShape() {
        return shape(getVariant());
    }

    public int getPattern() {
        return pattern(getVariant());
    }

    public EnumDyeColor getBaseColor() {
        return color(baseColor(getVariant()));
    }

    public EnumDyeColor getPatternColor() {
        return color(patternColor(getVariant()));
    }

    public float[] getBaseColorComponents() {
        return getBaseColor().getColorComponentValues();
    }

    public float[] getPatternColorComponents() {
        return getPatternColor().getColorComponentValues();
    }

    public ResourceLocation getBaseTexture() {
        return BASE_TEXTURES[getShape()];
    }

    public ResourceLocation getPatternTexture() {
        return PATTERN_TEXTURES[getShape()][getPattern()];
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        if (livingData instanceof TropicalFishSchoolData) {
            setVariant(((TropicalFishSchoolData) livingData).variant);
            return livingData;
        }
        int selected;
        if (rand.nextFloat() < 0.9F) {
            selected = PREDEFINED_VARIANTS[rand.nextInt(PREDEFINED_VARIANTS.length)];
        } else {
            schooling = false;
            selected = pack(rand.nextInt(2), rand.nextInt(6), rand.nextInt(16), rand.nextInt(16));
        }
        setVariant(selected);
        return schooling ? new TropicalFishSchoolData(this, selected) : result;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return schooling ? super.getMaxSpawnedInChunk() : 1;
    }

    @Override
    protected ItemStack getBucketStack() {
        return FFDItems.effectiveStack(FFDItems.TROPICAL_FISH_BUCKET);
    }

    @Override
    protected void writeToBucket(ItemStack bucket) {
        super.writeToBucket(bucket);
        bucket.setTagInfo("BucketVariantTag", new net.minecraft.nbt.NBTTagInt(getVariant()));
    }

    @Override
    public void readFromBucket(ItemStack bucket) {
        super.readFromBucket(bucket);
        if (bucket.hasTagCompound() && bucket.getTagCompound().hasKey("BucketVariantTag", 99)) {
            setVariant(bucket.getTagCompound().getInteger("BucketVariantTag"));
        } else if (bucket.hasTagCompound() && bucket.getTagCompound().hasKey("EntityTag", 10)
                && bucket.getTagCompound().getCompoundTag("EntityTag").hasKey("Variant", 99)) {
            setVariant(bucket.getTagCompound().getCompoundTag("EntityTag").getInteger("Variant"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setVariant(compound.getInteger("Variant"));
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        entityDropItem(new ItemStack(Items.FISH, 1, 2), 0.0F);
        if (rand.nextFloat() < 0.05F) {
            entityDropItem(new ItemStack(Items.DYE, 1, 15), 0.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return FFDSounds.TROPICAL_FISH_FLOP;
    }

    public static int pack(int shape, int pattern, int baseColor, int patternColor) {
        return shape & 255 | (pattern & 255) << 8 | (baseColor & 255) << 16
                | (patternColor & 255) << 24;
    }

    public static int shape(int variant) {
        return Math.min(variant & 255, 1);
    }

    public static int pattern(int variant) {
        return Math.min((variant & 0xFF00) >> 8, 5);
    }

    public static int baseColor(int variant) {
        return (variant & 0xFF0000) >> 16;
    }

    public static int patternColor(int variant) {
        return variant >>> 24 & 255;
    }

    public static int predefinedIndex(int variant) {
        for (int index = 0; index < PREDEFINED_VARIANTS.length; index++) {
            if (PREDEFINED_VARIANTS[index] == variant) {
                return index;
            }
        }
        return -1;
    }

    public static String typeName(int variant) {
        return Type.by(shape(variant), pattern(variant)).name().toLowerCase(Locale.ROOT);
    }

    private static int variant(Type type, EnumDyeColor base, EnumDyeColor pattern) {
        return pack(type.shape, type.pattern, base.getMetadata(), pattern.getMetadata());
    }

    private static EnumDyeColor color(int value) {
        return EnumDyeColor.byMetadata(Math.max(0, Math.min(15, value)));
    }

    private static ResourceLocation texture(String file) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/fish/" + file);
    }

    private static ResourceLocation[] patternTextures(String prefix) {
        ResourceLocation[] textures = new ResourceLocation[6];
        for (int index = 0; index < textures.length; index++) {
            textures[index] = texture(prefix + (index + 1) + ".png");
        }
        return textures;
    }

    private static final class TropicalFishSchoolData extends SchoolData {
        private final int variant;

        private TropicalFishSchoolData(EntityTropicalFish leader, int variant) {
            super(leader);
            this.variant = variant;
        }
    }

    private enum Type {
        KOB(0, 0),
        SUNSTREAK(0, 1),
        SNOOPER(0, 2),
        DASHER(0, 3),
        BRINELY(0, 4),
        SPOTTY(0, 5),
        FLOPPER(1, 0),
        STRIPEY(1, 1),
        GLITTER(1, 2),
        BLOCKFISH(1, 3),
        BETTY(1, 4),
        CLAYFISH(1, 5);

        private static final Type[] VALUES = values();
        private final int shape;
        private final int pattern;

        Type(int shape, int pattern) {
            this.shape = shape;
            this.pattern = pattern;
        }

        private static Type by(int shape, int pattern) {
            return VALUES[Math.max(0, Math.min(1, shape)) * 6
                    + Math.max(0, Math.min(5, pattern))];
        }
    }
}
