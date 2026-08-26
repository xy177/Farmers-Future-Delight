package xy177.farmersfuturedelight.common.registry;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDRawOreDropHooks {
    private static final Random RANDOM = new Random();

    private FFDRawOreDropHooks() {
    }

    public static NonNullList<ItemStack> convertMachineDrops(NonNullList<ItemStack> drops,
                                                             int fortune) {
        if (!FFDConfig.oresDropRawMaterials) {
            return drops;
        }
        NonNullList<ItemStack> converted = NonNullList.create();
        for (ItemStack drop : drops) {
            FFDCustomRawOres.Entry custom = FFDCustomRawOres.findSource(drop);
            if (custom != null) {
                ItemStack raw = custom.rawStack();
                if (!raw.isEmpty()) {
                    int amount = FFDConfig.rawOreDropAmount;
                    if (FFDConfig.denseRawOreDrop && isDenseOre(drop)) {
                        amount *= denseOreMultiplier();
                    }
                    if (fortune > 0) {
                        int multiplier = RANDOM.nextInt(fortune + 2) - 1;
                        amount *= Math.max(0, multiplier) + 1;
                    }
                    raw.setCount(amount);
                    converted.add(raw);
                    continue;
                }
            }
            int index = rawOreIndex(drop);
            if (index < 0) {
                converted.add(drop);
                continue;
            }
            if (!FFDItems.isRawOreMaterialEnabled(FFDRawOres.NAMES[index])) {
                converted.add(drop);
                continue;
            }
            ItemStack raw = FFDItems.effectiveStack(FFDItems.RAW_ORE_ITEMS[index]);
            if (raw.isEmpty()) {
                converted.add(drop);
                continue;
            }
            int amount = FFDRawOres.isCopper(FFDRawOres.NAMES[index])
                    ? 2 + RANDOM.nextInt(4) : FFDConfig.rawOreDropAmount;
            if (FFDConfig.denseRawOreDrop && isDenseOre(drop)) {
                amount *= denseOreMultiplier();
            }
            if (fortune > 0) {
                int multiplier = RANDOM.nextInt(fortune + 2) - 1;
                amount *= Math.max(0, multiplier) + 1;
            }
            raw.setCount(amount);
            converted.add(raw);
        }
        return converted;
    }

    private static int rawOreIndex(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            String normalized = oreName.endsWith("Dense")
                    ? oreName.substring(0, oreName.length() - "Dense".length()) : oreName;
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                for (String refined : FFDRawOres.refinedOreNames(FFDRawOres.NAMES[i])) {
                    String suffix = refined.startsWith("ingot")
                            ? refined.substring("ingot".length())
                            : FFDRawOres.capitalize(FFDRawOres.NAMES[i]);
                    if (("ore" + suffix).equals(normalized)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean isDenseOre(ItemStack stack) {
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).endsWith("Dense")) {
                return true;
            }
        }
        return false;
    }

    private static int denseOreMultiplier() {
        if (!net.minecraftforge.fml.common.Loader.isModLoaded("densemetals")) {
            return 2;
        }
        try {
            Class<?> config = Class.forName("com.mcmoddev.densemetals.DenseMetalsConfig");
            return Math.max(1, config.getField("denseOreValue").getInt(null));
        } catch (Throwable ignored) {
            return 2;
        }
    }
}
