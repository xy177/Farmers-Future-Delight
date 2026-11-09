package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.init.SoundEvents;
import net.minecraft.block.material.Material;
import net.minecraft.util.SoundEvent;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDHorseSounds {
    private FFDHorseSounds() {
    }

    public static void armorChanged(EntityHorse horse, HorseArmorType previous) {
        if (horse.getClass() == EntityHorse.class && !horse.world.isRemote && horse.ticksExisted > 20
                && previous != HorseArmorType.NONE && horse.getHorseArmorType() == HorseArmorType.NONE) {
            horse.playSound(FFDSounds.HORSE_ARMOR_UNEQUIP, 1.0F, 1.0F);
        }
    }

    public static SoundEvent angry(SoundEvent sound, AbstractHorse horse) {
        if (horse.getClass() == EntityHorse.class && horse.isChild()) return FFDSounds.BABY_HORSE_ANGRY;
        return horse.getClass() == EntityZombieHorse.class ? FFDSounds.ZOMBIE_HORSE_ANGRY : sound;
    }

    public static SoundEvent adapt(SoundEvent sound, AbstractHorse horse) {
        Class<?> type = horse.getClass();
        if (type != EntityHorse.class && type != EntitySkeletonHorse.class && type != EntityZombieHorse.class) return sound;
        boolean baby = type == EntityHorse.class && horse.isChild();
        if (type == EntitySkeletonHorse.class && horse.isInWater()) {
            if (sound == SoundEvents.ENTITY_HORSE_GALLOP) return FFDSounds.SKELETON_HORSE_GALLOP_WATER;
            if (sound == SoundEvents.ENTITY_HORSE_JUMP) return FFDSounds.SKELETON_HORSE_JUMP_WATER;
            if (sound == SoundEvents.ENTITY_HORSE_STEP || sound == SoundEvents.ENTITY_HORSE_STEP_WOOD) return FFDSounds.SKELETON_HORSE_STEP_WATER;
        }
        if (sound == SoundEvents.ENTITY_HORSE_ARMOR) return FFDSounds.HORSE_ARMOR;
        if (sound == SoundEvents.ENTITY_HORSE_SADDLE) return FFDSounds.HORSE_SADDLE;
        if (sound == SoundEvents.ENTITY_HORSE_EAT) {
            if (baby) return FFDSounds.BABY_HORSE_EAT;
            return type == EntityZombieHorse.class ? FFDSounds.ZOMBIE_HORSE_EAT : FFDSounds.HORSE_EAT;
        }
        if (sound == SoundEvents.ENTITY_HORSE_AMBIENT) {
            if (baby) return FFDSounds.BABY_HORSE_AMBIENT;
            if (type == EntitySkeletonHorse.class) return FFDSounds.SKELETON_HORSE_AMBIENT;
            if (type == EntityZombieHorse.class) return FFDSounds.ZOMBIE_HORSE_AMBIENT;
            return FFDSounds.HORSE_AMBIENT;
        }
        if (sound == SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT) {
            if (horse.isInsideOfMaterial(Material.WATER)) return FFDSounds.SKELETON_HORSE_AMBIENT_WATER;
            return FFDSounds.SKELETON_HORSE_AMBIENT;
        }
        if (sound == SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT) {
            return FFDSounds.ZOMBIE_HORSE_AMBIENT;
        }
        if (sound == SoundEvents.ENTITY_HORSE_ANGRY) {
            if (baby) return FFDSounds.BABY_HORSE_ANGRY;
            if (type == EntityZombieHorse.class) return FFDSounds.ZOMBIE_HORSE_ANGRY;
            return FFDSounds.HORSE_ANGRY;
        }
        if (sound == SoundEvents.ENTITY_HORSE_BREATHE) {
            return baby ? FFDSounds.BABY_HORSE_BREATHE : FFDSounds.HORSE_BREATHE;
        }
        if (sound == SoundEvents.ENTITY_HORSE_DEATH
                || sound == SoundEvents.ENTITY_SKELETON_HORSE_DEATH
                || sound == SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH) {
            if (baby) return FFDSounds.BABY_HORSE_DEATH;
            if (type == EntitySkeletonHorse.class) return FFDSounds.SKELETON_HORSE_DEATH;
            if (type == EntityZombieHorse.class) return FFDSounds.ZOMBIE_HORSE_DEATH;
            return FFDSounds.HORSE_DEATH;
        }
        if (sound == SoundEvents.ENTITY_HORSE_HURT
                || sound == SoundEvents.ENTITY_SKELETON_HORSE_HURT
                || sound == SoundEvents.ENTITY_ZOMBIE_HORSE_HURT) {
            if (baby) return FFDSounds.BABY_HORSE_HURT;
            if (type == EntitySkeletonHorse.class) return FFDSounds.SKELETON_HORSE_HURT;
            if (type == EntityZombieHorse.class) return FFDSounds.ZOMBIE_HORSE_HURT;
            return FFDSounds.HORSE_HURT;
        }
        if (sound == SoundEvents.ENTITY_HORSE_GALLOP) return FFDSounds.HORSE_GALLOP;
        if (sound == SoundEvents.ENTITY_HORSE_JUMP) return FFDSounds.HORSE_JUMP;
        if (sound == SoundEvents.ENTITY_HORSE_LAND) {
            return baby ? FFDSounds.BABY_HORSE_LAND : FFDSounds.HORSE_LAND;
        }
        if (sound == SoundEvents.ENTITY_HORSE_STEP) return baby ? FFDSounds.BABY_HORSE_STEP : FFDSounds.HORSE_STEP;
        if (sound == SoundEvents.ENTITY_HORSE_STEP_WOOD) return baby ? FFDSounds.BABY_HORSE_STEP : FFDSounds.HORSE_STEP_WOOD;
        if (type == EntitySkeletonHorse.class) {
            if (!horse.isInWater()) return sound;
            if (sound == SoundEvents.ENTITY_GENERIC_SWIM) {
                if (horse.onGround) {
                    if (!horse.isBeingRidden()) return FFDSounds.SKELETON_HORSE_STEP_WATER;
                    int count = horse.getEntityData().getInteger("FFDWaterGallop") + 1;
                    horse.getEntityData().setInteger("FFDWaterGallop", count);
                    if (count <= 5) return FFDSounds.SKELETON_HORSE_STEP_WATER;
                    if (count % 3 == 0) return FFDSounds.SKELETON_HORSE_GALLOP_WATER;
                }
                return FFDSounds.SKELETON_HORSE_SWIM;
            }
        }
        return sound;
    }
}
