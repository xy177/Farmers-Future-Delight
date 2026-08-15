package xy177.farmersfuturedelight.common;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPowderSnowEvents {
    public static final String FROZEN_TICKS_TAG = FarmerFutureDelight.MODID + ".frozenTicks";

    private static final String SKELETON_SNOW_TICKS_TAG =
            FarmerFutureDelight.MODID + ".skeletonPowderSnowTicks";
    private static final String SKELETON_CONVERSION_TAG =
            FarmerFutureDelight.MODID + ".strayConversionTicks";
    private static final UUID FROST_SLOW_UUID =
            UUID.fromString("f32d8a13-c64b-49a3-9b66-07fc71f6b96a");
    private static final DamageSource FREEZE = new DamageSource("freeze").setDamageBypassesArmor();
    private static final int TICKS_TO_FREEZE = 140;
    private static final int STRAY_CONVERSION_TIME = 300;

    private FFDPowderSnowEvents() {
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        if (!FFDItems.isPowderSnowEnabled()) {
            return;
        }
        EntityLivingBase entity = event.getEntityLiving();
        BlockPos powderSnowPos = findPowderSnow(entity);
        boolean inside = powderSnowPos != null;
        BlockPos entityPos = new BlockPos(entity);
        if (entity.world.getBlockState(entityPos).getBlock() == FFDBlocks.POWDER_SNOW) {
            if (entity.world.isRemote
                    && (entity.prevPosX != entity.posX || entity.prevPosZ != entity.posZ)
                    && entity.world.rand.nextBoolean()) {
                entity.world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL,
                        entity.posX, entityPos.getY() + 1.0D, entity.posZ,
                        (entity.world.rand.nextFloat() * 2.0F - 1.0F) / 12.0F,
                        0.05D,
                        (entity.world.rand.nextFloat() * 2.0F - 1.0F) / 12.0F);
            }
        }
        NBTTagCompound data = entity.getEntityData();
        int frozenTicks = data.getInteger(FROZEN_TICKS_TAG);
        if (inside && canFreeze(entity)) {
            frozenTicks = Math.min(TICKS_TO_FREEZE, frozenTicks + 1);
        } else {
            frozenTicks = Math.max(0, frozenTicks - 2);
        }
        data.setInteger(FROZEN_TICKS_TAG, frozenTicks);

        if (!entity.world.isRemote) {
            if (inside && entity.isBurning()) {
                if (entity instanceof EntityPlayer
                        || entity.world.getGameRules().getBoolean("mobGriefing")) {
                    BlockPowderSnow.destroyPowderSnow(entity.world, powderSnowPos);
                }
                entity.extinguish();
            }
            updateFrostSlowdown(entity, frozenTicks);
            if (frozenTicks >= TICKS_TO_FREEZE && entity.ticksExisted % 40 == 0) {
                float damage = entity instanceof EntityBlaze || entity instanceof EntityMagmaCube
                        ? 5.0F : 1.0F;
                entity.attackEntityFrom(FREEZE, damage);
            }
            if (entity.getClass() == EntitySkeleton.class) {
                updateSkeletonConversion((EntitySkeleton) entity, inside);
            }
        }
    }

    public static float getFrozenPercent(EntityLivingBase entity) {
        return MathHelper.clamp(entity.getEntityData().getInteger(FROZEN_TICKS_TAG)
                / (float) TICKS_TO_FREEZE, 0.0F, 1.0F);
    }

    public static boolean isInsidePowderSnow(EntityLivingBase entity) {
        return findPowderSnow(entity) != null;
    }

    @Nullable
    private static BlockPos findPowderSnow(EntityLivingBase entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        int minX = MathHelper.floor(box.minX + 1.0E-4D);
        int maxX = MathHelper.floor(box.maxX - 1.0E-4D);
        int minY = MathHelper.floor(box.minY + 1.0E-4D);
        int maxY = MathHelper.floor(box.maxY - 1.0E-4D);
        int minZ = MathHelper.floor(box.minZ + 1.0E-4D);
        int maxZ = MathHelper.floor(box.maxZ - 1.0E-4D);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.setPos(x, y, z);
                    if (entity.world.getBlockState(cursor).getBlock() == FFDBlocks.POWDER_SNOW) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }

    private static boolean canFreeze(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()) {
            return false;
        }
        if (entity instanceof EntityStray || entity instanceof EntityPolarBear
                || entity instanceof EntitySnowman || entity instanceof EntityWither) {
            return false;
        }
        for (EntityEquipmentSlot slot : new EntityEquipmentSlot[] {
                EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD}) {
            ItemStack armor = entity.getItemStackFromSlot(slot);
            if (armor.getItem() == Items.LEATHER_BOOTS
                    || armor.getItem() == Items.LEATHER_LEGGINGS
                    || armor.getItem() == Items.LEATHER_CHESTPLATE
                    || armor.getItem() == Items.LEATHER_HELMET) {
                return false;
            }
        }
        return true;
    }

    private static void updateFrostSlowdown(EntityLivingBase entity, int frozenTicks) {
        IAttributeInstance speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        AttributeModifier old = speed.getModifier(FROST_SLOW_UUID);
        if (old != null) {
            speed.removeModifier(old);
        }
        if (frozenTicks > 0) {
            double amount = -0.05D * Math.min(1.0D, frozenTicks / (double) TICKS_TO_FREEZE);
            speed.applyModifier(new AttributeModifier(FROST_SLOW_UUID,
                    "Powder snow frost slowdown", amount, 0).setSaved(false));
        }
    }

    private static void updateSkeletonConversion(EntitySkeleton skeleton, boolean inside) {
        NBTTagCompound data = skeleton.getEntityData();
        int conversion = data.hasKey(SKELETON_CONVERSION_TAG)
                ? data.getInteger(SKELETON_CONVERSION_TAG) : -1;
        if (!inside) {
            data.setInteger(SKELETON_SNOW_TICKS_TAG, -1);
            data.setInteger(SKELETON_CONVERSION_TAG, -1);
            return;
        }
        if (conversion >= 0) {
            conversion--;
            data.setInteger(SKELETON_CONVERSION_TAG, conversion);
            if (conversion < 0) {
                convertToStray(skeleton);
            }
            return;
        }
        int snowTicks = data.getInteger(SKELETON_SNOW_TICKS_TAG) + 1;
        data.setInteger(SKELETON_SNOW_TICKS_TAG, snowTicks);
        if (snowTicks >= TICKS_TO_FREEZE) {
            data.setInteger(SKELETON_CONVERSION_TAG, STRAY_CONVERSION_TIME);
        }
    }

    private static void convertToStray(EntitySkeleton skeleton) {
        World world = skeleton.world;
        EntityStray stray = new EntityStray(world);
        stray.copyLocationAndAnglesFrom(skeleton);
        stray.setHealth(Math.min(skeleton.getHealth(), stray.getMaxHealth()));
        stray.setNoAI(skeleton.isAIDisabled());
        stray.setCanPickUpLoot(skeleton.canPickUpLoot());
        if (skeleton.hasCustomName()) {
            stray.setCustomNameTag(skeleton.getCustomNameTag());
            stray.setAlwaysRenderNameTag(skeleton.getAlwaysRenderNameTag());
        }
        if (skeleton.isNoDespawnRequired()) {
            stray.enablePersistence();
        }
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            stray.setItemStackToSlot(slot, skeleton.getItemStackFromSlot(slot).copy());
        }
        for (PotionEffect effect : skeleton.getActivePotionEffects()) {
            stray.addPotionEffect(new PotionEffect(effect));
        }
        skeleton.setDead();
        world.spawnEntity(stray);
        world.playSound(null, new BlockPos(stray), FFDSounds.SKELETON_CONVERTED_TO_STRAY,
                SoundCategory.HOSTILE, 1.0F, 1.0F);
    }
}
