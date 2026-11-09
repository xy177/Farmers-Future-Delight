package xy177.farmersfuturedelight.compat.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.client.GuiLoom;
import xy177.farmersfuturedelight.client.GuiSmithingTable;
import xy177.farmersfuturedelight.common.inventory.ContainerCookingFurnace;
import xy177.farmersfuturedelight.common.inventory.ContainerSmithingTable;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@JEIPlugin
public final class FFDJeiPlugin implements IModPlugin {
    private final Map<String, FFDJeiCategory> categories = new LinkedHashMap<>();

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        categories.clear();
        category(registration, "smithing", FFDBlocks.SMITHING_TABLE);
        category(registration, "smoking", FFDBlocks.SMOKER);
        category(registration, "blasting", FFDBlocks.BLAST_FURNACE);
        category(registration, "campfire_cooking", FFDBlocks.CAMPFIRE);
        category(registration, "stonecutting", FFDBlocks.STONECUTTER);
        category(registration, "composting", FFDBlocks.COMPOSTER);
        category(registration, "loom", FFDBlocks.LOOM);
        FFDJeiCategory trades = new FFDJeiCategory(registration.getJeiHelpers().getGuiHelper(),
                "trades", new ItemStack(Items.EMERALD));
        categories.put("trades", trades);
        FFDJeiCategory professions = new FFDJeiCategory(registration.getJeiHelpers().getGuiHelper(),
                "professions", new ItemStack(Items.EMERALD));
        categories.put("professions", professions);
        registration.addRecipeCategories(categories.values().toArray(new FFDJeiCategory[0]));
    }

    private void category(IRecipeCategoryRegistration registration, String name, Block block) {
        if (FFDItems.isBlockRegistered(block)) {
            categories.put(name, new FFDJeiCategory(registration.getJeiHelpers().getGuiHelper(), name,
                    new ItemStack(block)));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        mezz.jei.api.recipe.IStackHelper stacks = registry.getJeiHelpers().getStackHelper();
        register(registry, "smoking", FFDJeiRecipes.workstation(WorkstationRecipes.Type.SMOKING, stacks), FFDBlocks.SMOKER);
        register(registry, "blasting", FFDJeiRecipes.workstation(WorkstationRecipes.Type.BLASTING, stacks), FFDBlocks.BLAST_FURNACE);
        register(registry, "campfire_cooking", FFDJeiRecipes.workstation(WorkstationRecipes.Type.CAMPFIRE_COOKING, stacks), FFDBlocks.CAMPFIRE);
        register(registry, "stonecutting", FFDJeiRecipes.workstation(WorkstationRecipes.Type.STONECUTTING, stacks), FFDBlocks.STONECUTTER);
        register(registry, "smithing", FFDJeiRecipes.workstation(WorkstationRecipes.Type.SMITHING, stacks), FFDBlocks.SMITHING_TABLE);
        register(registry, "composting", FFDJeiRecipes.composting(), FFDBlocks.COMPOSTER);
        register(registry, "loom", FFDJeiRecipes.loom(), FFDBlocks.LOOM);
        register(registry, "trades", FFDJeiRecipes.trades(), null);
        register(registry, "professions", FFDJeiRecipes.professions(), null);
        if (categories.containsKey("smoking")) {
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CookingTransfer(true, false));
            registry.addRecipeCatalyst(new ItemStack(FFDBlocks.SMOKER), "minecraft.fuel");
        }
        if (categories.containsKey("blasting")) {
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CookingTransfer(false, false));
            registry.addRecipeCatalyst(new ItemStack(FFDBlocks.BLAST_FURNACE), "minecraft.fuel");
        }
        if (categories.containsKey("smoking") || categories.containsKey("blasting")) {
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(new CookingTransfer(false, true));
        }
        if (categories.containsKey("smithing")) {
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerSmithingTable.class,
                    uid("smithing"), 0, 3, 4, 36);
            registry.addRecipeClickArea(GuiSmithingTable.class, 65, 46, 28, 21, uid("smithing"));
        }
        if (categories.containsKey("loom")) {
            registry.addRecipeClickArea(GuiLoom.class, 138, 8, 26, 42, uid("loom"));
        }
    }

    private void register(IModRegistry registry, String name, List<FFDJeiRecipe> recipes, Block catalyst) {
        if (!categories.containsKey(name)) return;
        registry.addRecipes(recipes, uid(name));
        if (catalyst != null) registry.addRecipeCatalyst(new ItemStack(catalyst), uid(name));
    }

    private static String uid(String name) {
        return "farmers_future_delight:" + name;
    }

    private static final class CookingTransfer implements IRecipeTransferInfo<ContainerCookingFurnace> {
        private final boolean smoking;
        private final boolean fuel;

        private CookingTransfer(boolean smoking, boolean fuel) {
            this.smoking = smoking;
            this.fuel = fuel;
        }

        @Override
        public Class<ContainerCookingFurnace> getContainerClass() {
            return ContainerCookingFurnace.class;
        }

        @Override
        public String getRecipeCategoryUid() {
            return fuel ? "minecraft.fuel" : uid(smoking ? "smoking" : "blasting");
        }

        @Override
        public boolean canHandle(ContainerCookingFurnace container) {
            return fuel || container.getFurnace().isSmoking() == smoking;
        }

        @Override
        public List<Slot> getRecipeSlots(ContainerCookingFurnace container) {
            return java.util.Collections.singletonList(container.getSlot(fuel ? 1 : 0));
        }

        @Override
        public List<Slot> getInventorySlots(ContainerCookingFurnace container) {
            return new ArrayList<>(container.inventorySlots.subList(3, 39));
        }
    }
}
