package xy177.farmersfuturedelight.compat.jei;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.item.ItemBannerPattern;
import xy177.farmersfuturedelight.common.registry.FFDBannerPatterns;
import xy177.farmersfuturedelight.common.registry.FFDModernVillagerTrades;
import xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations;
import xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration;
import xy177.farmersfuturedelight.common.registry.FFDTradeDisplay;
import xy177.farmersfuturedelight.common.registry.FFDWanderingTraderTrades;

final class FFDJeiRecipes {
    private FFDJeiRecipes() {
    }

    static List<FFDJeiRecipe> workstation(WorkstationRecipes.Type type, mezz.jei.api.recipe.IStackHelper helper) {
        List<FFDJeiRecipe> result = new ArrayList<>();
        for (WorkstationRecipes.Recipe source : WorkstationRecipes.getRecipes(type)) {
            FFDJeiRecipe display = new FFDJeiRecipe();
            List<ItemStack> representative = new ArrayList<>();
            boolean available = true;
            for (Ingredient ingredient : source.getIngredients()) {
                List<ItemStack> candidates = new ArrayList<>();
                for (ItemStack candidate : helper.getAllSubtypes(Arrays.asList(ingredient.getMatchingStacks()))) {
                    if (!candidate.isEmpty() && ingredient.apply(candidate)
                            && (type.getInputCount() != 1 || type == WorkstationRecipes.Type.STONECUTTING
                            || WorkstationRecipes.find(type, Collections.singletonList(candidate)) == source)) {
                        candidates.add(candidate.copy());
                    }
                }
                if (ingredient != Ingredient.EMPTY && candidates.isEmpty()) available = false;
                display.inputs.add(candidates);
                representative.add(candidates.isEmpty() ? ItemStack.EMPTY : candidates.get(0));
            }
            if (!available) continue;
            ItemStack output = source.assemble(representative);
            if (output.isEmpty()) {
                org.apache.logging.log4j.LogManager.getLogger("FFD JEI").warn(
                        "No preview result for workstation recipe {}; dynamic assemblers may require specific inputs",
                        source.getId());
                continue;
            }
            display.output(output);
            display.seconds = type == WorkstationRecipes.Type.SMOKING || type == WorkstationRecipes.Type.BLASTING
                    || type == WorkstationRecipes.Type.CAMPFIRE_COOKING
                    ? Math.max(1, (source.getProcessingTime() + 19) / 20) : 0;
            display.experience = type == WorkstationRecipes.Type.CAMPFIRE_COOKING ? 0.0F : source.getExperience();
            display.processingTime = source.getProcessingTime();
            result.add(display);
        }
        return result;
    }

    static List<FFDJeiRecipe> composting() {
        List<FFDJeiRecipe> result = new ArrayList<>();
        for (CompostingRules.Rule rule : CompostingRules.getRules()) {
            FFDJeiRecipe display = new FFDJeiRecipe();
            List<ItemStack> inputs = new ArrayList<>();
            for (ItemStack stack : rule.getIngredient().getMatchingStacks()) {
                if (CompostingRules.find(stack) == rule) inputs.add(stack.copy());
            }
            if (inputs.isEmpty()) continue;
            display.inputs.add(inputs);
            display.chance = rule.getChance();
            result.add(display);
        }
        return result;
    }

    static List<FFDJeiRecipe> loom() {
        List<FFDJeiRecipe> result = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof ItemBannerPattern)) continue;
            result.add(pattern(new ItemStack(item), ((ItemBannerPattern) item).getPattern()));
        }
        for (BannerPattern pattern : BannerPattern.values()) {
            if (pattern.hasPatternItem() && FFDBannerPatterns.ingredientPatterns(pattern.getPatternItem()).contains(pattern)) {
                result.add(pattern(pattern.getPatternItem(), pattern));
            }
        }
        return result;
    }

    private static FFDJeiRecipe pattern(ItemStack source, BannerPattern pattern) {
        FFDJeiRecipe display = new FFDJeiRecipe().input(source);
        ItemStack banner = new ItemStack(Items.BANNER, 1, EnumDyeColor.WHITE.getDyeDamage());
        NBTTagCompound layer = new NBTTagCompound();
        layer.setString("Pattern", pattern.getHashname());
        layer.setInteger("Color", EnumDyeColor.BLACK.getDyeDamage());
        NBTTagList layers = new NBTTagList();
        layers.appendTag(layer);
        banner.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", layers);
        display.output(banner);
        display.bannerPattern = pattern;
        return display;
    }

    static List<FFDJeiRecipe> professions() {
        List<FFDJeiRecipe> result = new ArrayList<>();
        for (VillagerProfession profession : ForgeRegistries.VILLAGER_PROFESSIONS) {
            List<String> careers = FFDProfessionWorkstations.careerNames(profession);
            for (int careerIndex = 0; careerIndex < careers.size(); careerIndex++) {
                String career = careers.get(careerIndex);
                List<ItemStack> blocks = new ArrayList<>();
                FFDJeiRecipe display = new FFDJeiRecipe();
                for (FFDProfessionWorkstations.WorkstationMapping mapping :
                        FFDProfessionWorkstations.getMappings(profession.getRegistryName(), career)) {
                    for (IBlockState state : mapping.getMatchingStates()) {
                        ItemStack stack = workstationStack(state);
                        if (stack.isEmpty()) continue;
                        if (blocks.stream().noneMatch(existing -> ItemStack.areItemStacksEqual(existing, stack))) blocks.add(stack);
                    }
                }
                if (blocks.isEmpty()) continue;
                display.outputs.add(blocks);
                display.profession = profession;
                display.career = careerIndex;
                display.heading = careerName(career);
                result.add(display);
            }
        }
        return result;
    }

    static String careerName(String career) {
        String key = "entity.Villager." + career;
        return I18n.hasKey(key) ? I18n.format(key) : career;
    }

    static List<FFDJeiRecipe> trades() {
        List<FFDJeiRecipe> result = new ArrayList<>();
        Map<String, JsonObject> groups = FFDModernVillagerTrades.getDisplayGroups();
        for (VillagerProfession profession : ForgeRegistries.VILLAGER_PROFESSIONS) {
            ResourceLocation id = profession.getRegistryName();
            if (id == null || !("minecraft".equals(id.getResourceDomain())
                    || "farmers_future_delight:mason".equals(id.toString()))) continue;
            for (String career : FFDProfessionWorkstations.careerNames(profession)) {
                String modern = modernCareer(career);
                if ("farmers_future_delight:mason".equals(id.toString())) modern = "mason";
                List<TradeRow> offers = new ArrayList<>();
                for (int level = 1; level <= 5; level++) {
                    JsonObject group = groups.get(modern + "/level_" + level);
                    if (group == null) continue;
                    MerchantRecipeList recipes = new MerchantRecipeList();
                    for (JsonElement element : group.getAsJsonArray("trades")) {
                        JsonObject definition = element.getAsJsonObject().getAsJsonObject("definition");
                        MerchantRecipe recipe = FFDTradeDisplay.preview(definition);
                        if (recipe == null) continue;
                        recipes.add(recipe);
                    }
                    FFDTradeConfiguration.applyDisplayProfile(id, career, recipes, level);
                    for (MerchantRecipe recipe : recipes) offers.add(new TradeRow(recipe, level));
                }
                pages(result, careerName(career), offers, workstationStacks(profession, career));
            }
        }
        if (!xy177.farmersfuturedelight.common.registry.FFDEntities.isLocalWanderingTraderEnabled()) return result;
        MerchantRecipeList wandering = new MerchantRecipeList();
        for (JsonObject definition : FFDWanderingTraderTrades.getDisplayOffers()) {
            MerchantRecipe recipe = FFDTradeDisplay.preview(definition);
            if (recipe == null) continue;
            wandering.add(recipe);
        }
        FFDTradeConfiguration.applyWanderingTrader(wandering, 1);
        List<TradeRow> offers = new ArrayList<>();
        for (MerchantRecipe recipe : wandering) offers.add(new TradeRow(recipe, 0));
        ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        net.minecraft.item.ItemMonsterPlacer.applyEntityIdToItemStack(egg,
                xy177.farmersfuturedelight.common.registry.FFDEntities.WANDERING_TRADER_ID);
        pages(result, I18n.format("entity.farmers_future_delight.wandering_trader.name"), offers,
                Collections.singletonList(egg));
        return result;
    }

    static String modernCareer(String career) {
        switch (career) {
            case "armor": return "armorer";
            case "weapon": return "weaponsmith";
            case "tool": return "toolsmith";
            case "leather": return "leatherworker";
            default: return career;
        }
    }

    private static List<ItemStack> workstationStacks(VillagerProfession profession, String career) {
        List<ItemStack> result = new ArrayList<>();
        for (FFDProfessionWorkstations.WorkstationMapping mapping :
                FFDProfessionWorkstations.getMappings(profession.getRegistryName(), career)) {
            for (IBlockState state : mapping.getMatchingStates()) {
                ItemStack stack = workstationStack(state);
                if (stack.isEmpty()) continue;
                if (result.stream().noneMatch(existing -> ItemStack.areItemStacksEqual(existing, stack))) result.add(stack);
            }
        }
        return result;
    }

    private static ItemStack workstationStack(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == Items.AIR) item = ForgeRegistries.ITEMS.getValue(state.getBlock().getRegistryName());
        return item == null || item == Items.AIR ? ItemStack.EMPTY
                : new ItemStack(item, 1, state.getBlock().damageDropped(state));
    }

    private static void pages(List<FFDJeiRecipe> destination, String name, List<TradeRow> offers,
                              List<ItemStack> sources) {
        for (int offset = 0; offset < offers.size(); offset += 4) {
            FFDJeiRecipe page = new FFDJeiRecipe();
            page.heading = name + " " + (offset / 4 + 1) + "/" + ((offers.size() + 3) / 4);
            for (int index = offset; index < Math.min(offers.size(), offset + 4); index++) {
                TradeRow row = offers.get(index);
                page.input(row.recipe.getItemToBuy()).input(row.recipe.getSecondItemToBuy())
                        .output(row.recipe.getItemToSell());
                page.rows.add((row.level == 0 ? I18n.format("jei.farmers_future_delight.trade_offer")
                        : I18n.format("jei.farmers_future_delight.trade_level", row.level)) + "\n"
                        + I18n.format("jei.farmers_future_delight.trade_uses", row.recipe.getMaxTradeUses()));
            }
            page.headerStart = page.inputs.size();
            for (int index = 0; index < Math.min(3, sources.size()); index++) page.input(sources.get(index));
            destination.add(page);
        }
    }

    private static final class TradeRow {
        final MerchantRecipe recipe;
        final int level;
        TradeRow(MerchantRecipe recipe, int level) {
            this.recipe = recipe;
            this.level = level;
        }
    }
}
