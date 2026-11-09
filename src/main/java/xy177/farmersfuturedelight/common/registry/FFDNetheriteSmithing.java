package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreIngredient;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WorkstationRecipes;

public final class FFDNetheriteSmithing {
    private FFDNetheriteSmithing() {
    }

    public static void registerDefaults() {
        if (!FFDItems.isNetheriteEnabled()) {
            return;
        }
        ItemStack template = FFDItems.effectiveStack(FFDItems.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        ItemStack ingot = FFDItems.effectiveStack(FFDItems.NETHERITE_INGOT);
        if (template.isEmpty() || ingot.isEmpty()) {
            return;
        }
        Item[] bases = {
                Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE,
                Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_HELMET,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                Items.DIAMOND_HORSE_ARMOR
        };
        Item[] results = {
                FFDItems.NETHERITE_SWORD, FFDItems.NETHERITE_PICKAXE,
                FFDItems.NETHERITE_AXE, FFDItems.NETHERITE_SHOVEL,
                FFDItems.NETHERITE_HOE, FFDItems.NETHERITE_HELMET,
                FFDItems.NETHERITE_CHESTPLATE, FFDItems.NETHERITE_LEGGINGS,
                FFDItems.NETHERITE_BOOTS, FFDItems.NETHERITE_HORSE_ARMOR
        };
        for (int index = 0; index < bases.length; index++) {
            ItemStack result = FFDItems.effectiveStack(results[index]);
            if (result.isEmpty()) {
                continue;
            }
            ResourceLocation id = new ResourceLocation("minecraft",
                    result.getItem().getRegistryName().getResourcePath() + "_smithing");
            if (WorkstationRecipes.get(WorkstationRecipes.Type.SMITHING, id) != null) {
                continue;
            }
            List<Ingredient> ingredients = new ArrayList<>();
            ingredients.add(Ingredient.fromStacks(template));
            Item base = bases[index];
            ingredients.add(new Ingredient(new ItemStack(base)) {
                @Override
                public boolean apply(@Nullable ItemStack stack) {
                    return stack != null && !stack.isEmpty() && stack.getItem() == base;
                }

                @Override
                public boolean isSimple() {
                    return false;
                }
            });
            ingredients.add(new OreIngredient("ingotNetherite"));
            ItemStack output = result.copy();
            WorkstationRecipes.register(WorkstationRecipes.Type.SMITHING, id, ingredients,
                    inputs -> WorkstationRecipes.copySmithingData(inputs.get(1), output), 1, 0.0F);
        }
        ItemStack scrap = FFDItems.effectiveStack(FFDItems.NETHERITE_SCRAP);
        ItemStack debris = FFDItems.effectiveStack(FFDItems.ANCIENT_DEBRIS);
        if (!scrap.isEmpty() && !debris.isEmpty()) {
            ResourceLocation id = new ResourceLocation(FarmerFutureDelight.MODID,
                    "netherite_scrap_from_blasting");
            if (WorkstationRecipes.get(WorkstationRecipes.Type.BLASTING, id) == null) {
                WorkstationRecipes.register(WorkstationRecipes.Type.BLASTING, id,
                        Arrays.asList(Ingredient.fromStacks(debris)), scrap, 100, 2.0F);
            }
        }
    }
}
