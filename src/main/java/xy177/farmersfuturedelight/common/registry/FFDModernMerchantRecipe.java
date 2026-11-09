package xy177.farmersfuturedelight.common.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;

public class FFDModernMerchantRecipe extends MerchantRecipe {
    public static final String DATA_TAG = "farmers_future_delight:offer";
    private int demand;
    private int specialPrice;
    private float priceMultiplier = 0.05F;
    private int villagerExperience = 1;

    public FFDModernMerchantRecipe(NBTTagCompound compound) {
        super(compound);
        if (compound.hasKey(DATA_TAG, 10)) {
            NBTTagCompound data = compound.getCompoundTag(DATA_TAG);
            demand = data.getInteger("demand");
            priceMultiplier = data.getFloat("priceMultiplier");
            if (!Float.isFinite(priceMultiplier) || priceMultiplier < 0.0F) priceMultiplier = 0.05F;
            villagerExperience = Math.max(0, data.getInteger("experience"));
        }
    }

    @Override
    public ItemStack getItemToBuy() {
        ItemStack base = super.getItemToBuy();
        if (base.isEmpty()) return base;
        int demandIncrease = Math.max(0, MathHelper.floor((float) base.getCount() * demand * priceMultiplier));
        long cost = (long) base.getCount() + demandIncrease + specialPrice;
        int count = (int) Math.max(1L, Math.min((long) base.getMaxStackSize(), cost));
        if (count == base.getCount()) return base;
        ItemStack result = base.copy();
        result.setCount(count);
        return result;
    }

    public ItemStack getBaseCost() {
        return super.getItemToBuy();
    }

    public int getDemand() {
        return demand;
    }

    public void updateDemand() {
        long updated = (long) demand + getToolUses() - (getMaxTradeUses() - getToolUses());
        demand = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, updated));
    }

    public void resetUses() {
        NBTTagCompound compound = super.writeToTags();
        compound.setInteger("uses", 0);
        super.readFromTags(compound);
    }

    public int getVillagerExperience() {
        return villagerExperience;
    }

    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setSpecialPrice(int adjustment) {
        specialPrice = adjustment;
    }

    @Override
    public NBTTagCompound writeToTags() {
        NBTTagCompound compound = super.writeToTags();
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("demand", demand);
        data.setFloat("priceMultiplier", priceMultiplier);
        data.setInteger("experience", villagerExperience);
        compound.setTag(DATA_TAG, data);
        return compound;
    }
}
