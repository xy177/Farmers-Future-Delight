package xy177.farmersfuturedelight.client;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDClientCustomModelData {
    private static final ResourceLocation PROPERTY = new ResourceLocation("custom_model_data");

    private FFDClientCustomModelData() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            IItemPropertyGetter existing = item.getPropertyGetter(PROPERTY);
            if (existing == null) {
                item.addPropertyOverride(PROPERTY, FFDClientCustomModelData::getCustomModelData);
            } else {
                item.addPropertyOverride(PROPERTY, (stack, world, entity) -> {
                    if (hasCustomModelData(stack)) {
                        return getCustomModelData(stack, world, entity);
                    }
                    return existing.apply(stack, world, entity);
                });
            }
        }
    }

    private static float getCustomModelData(ItemStack stack, World world,
                                            EntityLivingBase entity) {
        return stack.hasTagCompound()
                ? stack.getTagCompound().getInteger("CustomModelData") : 0.0F;
    }

    private static boolean hasCustomModelData(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        return tag.hasKey("CustomModelData", 99);
    }
}
