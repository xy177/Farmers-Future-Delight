package xy177.farmersfuturedelight.common.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDFutureMcRecipes;

public final class FFDRecipes {
    private FFDRecipes() {
    }

    public static void init() {
        FFDRawOreOreDictionaryCompat.register();
        FFDRawOreCompat.registerCraftingRecipes();
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
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDRawOres.isCopper(FFDRawOres.NAMES[i])) {
                    addRawOreSmelting(i);
                }
            }
        }
        if (FFDItems.isCopperEnabled()) {
            ItemStack copper = FFDItems.effectiveStack(FFDItems.COPPER_INGOT,
                    FFDConfig.furnaceOutputAmount);
            addSmelting(FFDItems.RAW_COPPER, copper, 0.7F);
            addRawBlockSmelting(2, FFDItems.effectiveStack(FFDItems.COPPER_BLOCK));
            addSmelting(FFDItems.COPPER_ORE, FFDItems.COPPER_INGOT, 0.7F);
            if (FFDItems.isDeepslateEnabled()) {
                addSmelting(FFDItems.DEEPSLATE_COPPER_ORE, FFDItems.COPPER_INGOT, 0.7F);
            }
        }
        addCustomRawOreSmelting();
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
        if (FFDItems.isItemRegistered(input) && output != null && !output.isEmpty()) {
            GameRegistry.addSmelting(new ItemStack(input), output, experience);
        }
    }

    private static void addRawOreSmelting(int index) {
        net.minecraft.item.Item raw = FFDItems.RAW_ORE_ITEMS[index];
        if (!FFDItems.isItemRegistered(raw)) {
            return;
        }
        String name = FFDRawOres.NAMES[index];
        ItemStack output;
        if ("iron".equals(name)) {
            output = new ItemStack(Items.IRON_INGOT);
        } else if ("gold".equals(name)) {
            output = new ItemStack(Items.GOLD_INGOT);
        } else if ("copper".equals(name)) {
            output = FFDItems.effectiveStack(FFDItems.COPPER_INGOT);
        } else {
            output = firstOreStack(FFDRawOres.refinedOreNames(name));
        }
        if (output.isEmpty()) {
            return;
        }
        output.setCount(FFDConfig.furnaceOutputAmount);
        addSmelting(raw, output, "gold".equals(name) ? 1.0F : 0.7F);
        addRawBlockSmelting(index, refinedBlockStack(name));
    }

    private static ItemStack firstOreStack(String[] names) {
        for (String name : names) {
            ItemStack stack = FFDCompat.firstOreDictionaryStack(name);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack refinedBlockStack(String name) {
        if ("iron".equals(name)) {
            return new ItemStack(net.minecraft.init.Blocks.IRON_BLOCK);
        }
        if ("gold".equals(name)) {
            return new ItemStack(net.minecraft.init.Blocks.GOLD_BLOCK);
        }
        String[] refinedNames = FFDRawOres.refinedOreNames(name);
        for (String refinedName : refinedNames) {
            String suffix = refinedName.startsWith("ingot")
                    ? refinedName.substring("ingot".length()) : FFDRawOres.capitalize(name);
            ItemStack stack = FFDCompat.firstOreDictionaryStack("block" + suffix);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void addRawBlockSmelting(int index, ItemStack output) {
        if (!FFDConfig.rawBlockSmelt || output.isEmpty()
                || !FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[index])) {
            return;
        }
        GameRegistry.addSmelting(new ItemStack(FFDItems.RAW_ORE_BLOCK_ITEMS[index]),
                output, "gold".equals(FFDRawOres.NAMES[index]) ? 1.0F : 0.7F);
    }

    private static void addCustomRawOreSmelting() {
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            ItemStack raw = entry.rawStack();
            ItemStack output = entry.resolveSmeltResult();
            if (raw.isEmpty() || output.isEmpty()) {
                continue;
            }
            output.setCount(FFDConfig.furnaceOutputAmount);
            GameRegistry.addSmelting(raw, output, 0.7F);
            if (FFDConfig.rawBlockSmelt) {
                ItemStack rawBlock = entry.blockStack();
                ItemStack refinedBlock = entry.resolveRefinedBlock();
                if (!rawBlock.isEmpty() && !refinedBlock.isEmpty()) {
                    GameRegistry.addSmelting(rawBlock, refinedBlock, 0.7F);
                }
            }
        }
    }
}
