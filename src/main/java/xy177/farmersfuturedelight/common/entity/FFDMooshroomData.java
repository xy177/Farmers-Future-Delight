package xy177.farmersfuturedelight.common.entity;

import java.util.Collections;

import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDMooshroomData {
    public static final String BROWN_TAG = "FFDBrownMooshroom";
    private static final String STEW_TAG = "FFDMooshroomStew";
    private static final String LIGHTNING_TAG = "FFDMooshroomLightning";

    private FFDMooshroomData() {
    }

    public static boolean isBrown(EntityMooshroom entity) {
        return entity.getEntityData().getBoolean(BROWN_TAG);
    }

    public static void setBrown(EntityMooshroom entity, boolean brown) {
        entity.getEntityData().setBoolean(BROWN_TAG, brown);
    }

    public static boolean wasStruckBy(EntityMooshroom entity, java.util.UUID lightningId) {
        NBTTagCompound data = entity.getEntityData();
        String id = lightningId.toString();
        if (id.equals(data.getString(LIGHTNING_TAG))) {
            return true;
        }
        data.setString(LIGHTNING_TAG, id);
        return false;
    }

    public static PotionEffect getStewEffect(EntityMooshroom entity) {
        NBTTagCompound data = entity.getEntityData();
        if (!data.hasKey(STEW_TAG, 10)) {
            return null;
        }
        NBTTagCompound effectTag = data.getCompoundTag(STEW_TAG);
        Potion effect = Potion.getPotionFromResourceLocation(effectTag.getString("EffectId"));
        int duration = effectTag.getInteger("Duration");
        return effect == null || duration <= 0 ? null : new PotionEffect(effect, duration);
    }

    public static void setStewEffect(EntityMooshroom entity, PotionEffect effect) {
        NBTTagCompound effectTag = new NBTTagCompound();
        effectTag.setString("EffectId", effect.getPotion().getRegistryName().toString());
        effectTag.setInteger("Duration", effect.getDuration());
        entity.getEntityData().setTag(STEW_TAG, effectTag);
    }

    public static void clearStewEffect(EntityMooshroom entity) {
        entity.getEntityData().removeTag(STEW_TAG);
    }

    public static PotionEffect getFlowerEffect(ItemStack stack) {
        return xy177.farmersfuturedelight.common.registry.FFDSuspiciousStewIngredients.getEffect(stack);
    }

    public static ItemStack createStew(PotionEffect effect) {
        Item item = FFDItems.effectiveItem(FFDItems.SUSPICIOUS_STEW);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        if (item == FFDItems.SUSPICIOUS_STEW) {
            return ((xy177.farmersfuturedelight.common.item.ItemSuspiciousStew) item)
                    .createStack(effect.getPotion(), effect.getDuration());
        }
        return PotionUtils.appendEffects(new ItemStack(item), Collections.singletonList(effect));
    }

}
