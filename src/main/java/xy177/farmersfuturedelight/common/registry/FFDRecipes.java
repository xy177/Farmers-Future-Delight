package xy177.farmersfuturedelight.common.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xy177.farmersfuturedelight.common.FFDFutureMcRecipes;

public final class FFDRecipes {
    private FFDRecipes() {
    }

    public static void init() {
        if (FFDItems.isKelpEnabled()) {
            addSmelting(FFDItems.KELP, FFDItems.DRIED_KELP, 0.1F);
        }
        if (FFDItems.isSeaPickleEnabled()) {
            ItemStack pickle = FFDItems.effectiveStack(FFDItems.SEA_PICKLE);
            if (!pickle.isEmpty()) {
                GameRegistry.addSmelting(pickle,
                        new ItemStack(Items.DYE, 1, EnumDyeColor.LIME.getDyeDamage()), 0.1F);
            }
        }
        if (FFDItems.isDeepslateEnabled()) {
            addSmelting(FFDItems.COBBLED_DEEPSLATE, FFDItems.DEEPSLATE, 0.1F);
            addSmelting(FFDItems.DEEPSLATE_BRICKS, FFDItems.CRACKED_DEEPSLATE_BRICKS, 0.1F);
            addSmelting(FFDItems.DEEPSLATE_TILES, FFDItems.CRACKED_DEEPSLATE_TILES, 0.1F);
            addSmelting(FFDItems.DEEPSLATE_COAL_ORE, new ItemStack(Items.COAL), 0.1F);
            addSmelting(FFDItems.DEEPSLATE_IRON_ORE, new ItemStack(Items.IRON_INGOT), 0.7F);
            addSmelting(FFDItems.DEEPSLATE_GOLD_ORE, new ItemStack(Items.GOLD_INGOT), 1.0F);
            addSmelting(FFDItems.DEEPSLATE_REDSTONE_ORE, new ItemStack(Items.REDSTONE), 0.7F);
            addSmelting(FFDItems.DEEPSLATE_LAPIS_ORE, new ItemStack(Items.DYE, 1,
                    net.minecraft.item.EnumDyeColor.BLUE.getDyeDamage()), 0.2F);
            addSmelting(FFDItems.DEEPSLATE_DIAMOND_ORE, new ItemStack(Items.DIAMOND), 1.0F);
            addSmelting(FFDItems.DEEPSLATE_EMERALD_ORE, new ItemStack(Items.EMERALD), 1.0F);
        }
        if (FFDItems.isRawOreEnabled()) {
            addSmelting(FFDItems.RAW_IRON, new ItemStack(Items.IRON_INGOT), 0.7F);
            addSmelting(FFDItems.RAW_GOLD, new ItemStack(Items.GOLD_INGOT), 1.0F);
        }
        if (FFDItems.isCopperEnabled()) {
            addSmelting(FFDItems.RAW_COPPER, FFDItems.COPPER_INGOT, 0.7F);
            addSmelting(FFDItems.COPPER_ORE, FFDItems.COPPER_INGOT, 0.7F);
            if (FFDItems.isDeepslateEnabled()) {
                addSmelting(FFDItems.DEEPSLATE_COPPER_ORE, FFDItems.COPPER_INGOT, 0.7F);
            }
        }
        FFDFutureMcRecipes.register();
    }

    private static void addSmelting(net.minecraft.item.Item input,
                                    net.minecraft.item.Item output, float experience) {
        if (FFDItems.isItemRegistered(output)) {
            addSmelting(input, new ItemStack(output), experience);
        }
    }

    private static void addSmelting(net.minecraft.item.Item input,
                                    ItemStack output, float experience) {
        ItemStack effectiveInput = FFDItems.effectiveStack(input);
        if (!effectiveInput.isEmpty()) {
            GameRegistry.addSmelting(effectiveInput, output, experience);
        }
    }
}
