package xy177.farmersfuturedelight.common.registry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDRawOreCompat {
    private static final Logger LOGGER = LogManager.getLogger("FFD Raw Ore Compatibility");
    private static boolean registered;
    private static boolean craftingRecipesRegistered;

    private FFDRawOreCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        if (Loader.isModLoaded("ic2")) {
            registerIc2();
        }
        if (Loader.isModLoaded("mekanism")) {
            registerMekanism();
        }
        if (Loader.isModLoaded("thermalexpansion")) {
            registerThermalExpansion();
        }
        if (Loader.isModLoaded("tconstruct")) {
            registerTConstruct();
        }
        if (Loader.isModLoaded("enderio")) {
            registerEnderIo();
        }
        if (Loader.isModLoaded("galacticraftcore")
                || Loader.isModLoaded("galacticraftplanets")) {
            registerGalacticraft();
        }
        if (Loader.isModLoaded("advancedrocketry")) {
            registerAdvancedRocketry();
        }
        if (Loader.isModLoaded("techguns")) {
            registerTechguns();
        }
        if (Loader.isModLoaded("thaumcraft")) {
            registerThaumcraft();
        }
        if (Loader.isModLoaded("metallurgy")) {
            registerMetallurgy();
        }
        if (Loader.isModLoaded("gregtech")) {
            registerGregTech();
        }
        if (Loader.isModLoaded("jer") || Loader.isModLoaded("jeresources")) {
            registerJer();
        }
        registerCustomRawOreCompatibility();
    }

    public static void registerCraftingRecipes() {
        if (craftingRecipesRegistered) {
            return;
        }
        craftingRecipesRegistered = true;
        if (Loader.isModLoaded("galaxyspace")) {
            registerGalaxySpaceRecipe();
        }
        if (Loader.isModLoaded("sakura")) {
            registerSakura();
        }
        if (Loader.isModLoaded("croparia")) {
            registerCroparia();
        }
        if (Loader.isModLoaded("mysticalagriculture")) {
            registerMysticalAgriculture();
        }
    }

    private static void registerIc2() {
        try {
            Class<?> recipesClass = Class.forName("ic2.api.recipe.Recipes");
            Object macerator = staticField(recipesClass, "macerator");
            Object inputFactory = staticField(recipesClass, "inputFactory");
            if (macerator == null || inputFactory == null) {
                return;
            }
            String prefix = Loader.isModLoaded("ic2-classic-spmod")
                    && !Loader.isModLoaded("ic2c_extras") ? "dust" : "crushed";
            boolean iridiumSource = Loader.isModLoaded("iridiumsource");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String name = FFDRawOres.NAMES[i];
                ItemStack raw = new ItemStack(FFDItems.RAW_ORE_ITEMS[i]);
                String rawOutput = iridiumSource && "platinum".equals(name)
                        ? "prillPlatinum" : prefix + FFDRawOres.capitalize(name);
                ItemStack output = oreStack(rawOutput, FFDConfig.maceratorRawOutputAmount);
                Object input = invoke(inputFactory, "forStack", raw);
                addIc2Recipe(macerator, input, output);
                ItemStack ingot = refinedStack(name);
                ItemStack dust = oreStack("dust" + FFDRawOres.capitalize(name), 1);
                if (!ingot.isEmpty() && !dust.isEmpty()) {
                    addIc2Recipe(macerator, invoke(inputFactory, "forStack", ingot), dust);
                }
            }
            if (iridiumSource && FFDItems.isRawOreMaterialEnabled("platinum")) {
                ItemStack prill = registryStack("iridiumsource", "prill_platina");
                if (!prill.isEmpty()) {
                    addIc2Recipe(macerator, invoke(inputFactory, "forStack", prill),
                            oreStack(prefix + "Platinum", 1));
                }
            }
            registerIc2BlastFurnace(recipesClass, inputFactory);
        } catch (Throwable ignored) {
            LOGGER.debug("IC2 raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void addIc2Recipe(Object macerator, Object input, ItemStack output) {
        if (input == null || output.isEmpty()) {
            return;
        }
        invoke(macerator, "addRecipe", input, null, false,
                (Object) new ItemStack[] {output});
    }

    private static void registerIc2BlastFurnace(Class<?> recipesClass, Object inputFactory) {
        if (!FFDItems.isItemRegistered(FFDItems.RAW_IRON)) {
            return;
        }
        try {
            Object blastFurnace = staticField(recipesClass, "blastfurnace");
            ItemStack rawIron = rawStack("iron");
            ItemStack steel = oreStack("ingotSteel",
                    FFDConfig.ic2BlastFurnaceSteelOutputAmount);
            ItemStack slag = oreStack("itemSlag", FFDConfig.ic2BlastFurnaceSlagOutputAmount);
            if (blastFurnace == null || rawIron.isEmpty() || steel.isEmpty() || slag.isEmpty()) {
                return;
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("fluid", 1);
            nbt.setInteger("duration", 6000);
            Object input = invoke(inputFactory, "forStack", rawIron);
            invoke(blastFurnace, "addRecipe", input, nbt, false,
                    (Object) new ItemStack[] {steel, slag});
        } catch (Throwable ignored) {
            LOGGER.debug("IC2 blast-furnace raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerMekanism() {
        try {
            Class<?> handler = Class.forName("mekanism.common.recipe.RecipeHandler");
            Object gas = null;
            Class<?> gasStackClass = null;
            try {
                Class<?> gasRegistry = Class.forName("mekanism.api.gas.GasRegistry");
                gas = invokeStatic(gasRegistry, "getGas", "hydrogenchloride");
                gasStackClass = Class.forName("mekanism.api.gas.GasStack");
            } catch (Throwable ignored) {
                gas = null;
            }
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String material = FFDRawOres.NAMES[i];
                String name = FFDRawOres.capitalize(material);
                ItemStack raw = new ItemStack(FFDItems.RAW_ORE_ITEMS[i]);
                ItemStack dust = oreStack("dust" + name, FFDConfig.enrichmentOutputAmount);
                ItemStack clump = oreStack("clump" + name, FFDConfig.purificationOutputAmount);
                ItemStack shard = oreStack("shard" + name,
                        FFDConfig.chemicalInjectionChamberOutputAmount);
                if (!dust.isEmpty()) {
                    invokeStatic(handler, "addEnrichmentChamberRecipe", raw, dust);
                }
                if (!clump.isEmpty()) {
                    invokeStatic(handler, "addPurificationChamberRecipe", raw, clump);
                }
                if (!shard.isEmpty() && gas != null) {
                    invokeStatic(handler, "addChemicalInjectionChamberRecipe", raw, gas, shard);
                }
                if (gasStackClass != null && isMekanismDissolutionMaterial(material)) {
                    Object dissolutionGas = invokeStatic(Class.forName(
                            "mekanism.api.gas.GasRegistry"), "getGas", material);
                    if (dissolutionGas != null) {
                        Object gasStack = newInstance(gasStackClass, dissolutionGas,
                                FFDConfig.chemicalDissolutionChamberMultiple * 1000);
                        invokeStatic(handler, "addChemicalDissolutionChamberRecipe", raw,
                                gasStack);
                    }
                }
                ItemStack ingot = refinedStack(FFDRawOres.NAMES[i]);
                ItemStack ingotDust = oreStack("dust" + name, 1);
                if (!ingot.isEmpty() && !ingotDust.isEmpty()) {
                    invokeStatic(handler, "addCrusherRecipe", ingot, ingotDust);
                }
            }
            registerMekanismInfuser(handler);
        } catch (Throwable ignored) {
            LOGGER.debug("Mekanism raw-ore compatibility was unavailable", ignored);
        }
    }

    private static boolean isMekanismDissolutionMaterial(String name) {
        return "gold".equals(name) || "iron".equals(name) || "copper".equals(name)
                || "tin".equals(name) || "lead".equals(name) || "silver".equals(name)
                || "osmium".equals(name);
    }

    private static void registerMekanismInfuser(Class<?> handler) throws Exception {
        Class<?> registry = Class.forName("mekanism.api.infuse.InfuseRegistry");
        Item ingotItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("mekanism", "ingot"));
        Item circuitItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("mekanism", "controlcircuit"));
        if (ingotItem != null && FFDItems.isRawOreMaterialEnabled("gold")) {
            ItemStack gold = refinedStack("gold");
            if (!gold.isEmpty()) {
                gold.setCount(3);
                invokeStatic(handler, "addMetallurgicInfuserRecipe",
                        invokeStatic(registry, "get", "TIN"), 10, gold,
                        new ItemStack(ingotItem, 4, 2));
            }
        }
        if (circuitItem != null && FFDItems.isRawOreMaterialEnabled("osmium")) {
            ItemStack osmium = refinedStack("osmium");
            if (!osmium.isEmpty()) {
                invokeStatic(handler, "addMetallurgicInfuserRecipe",
                        invokeStatic(registry, "get", "REDSTONE"), 10, osmium,
                        new ItemStack(circuitItem, 1, 0));
            }
        }
    }

    private static void registerThermalExpansion() {
        try {
            Class<?> pulverizer = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.PulverizerManager");
            Class<?> crucible = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.CrucibleManager");
            Class<?> smelter = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.SmelterManager");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String name = FFDRawOres.NAMES[i];
                ItemStack raw = new ItemStack(FFDItems.RAW_ORE_ITEMS[i]);
                String[] products = FFDRawOres.productOreNames(i);
                ItemStack dust = oreStack(products[0], FFDConfig.pulverizerOutputAmount);
                ItemStack secondary = oreStack(products.length > 1 ? products[1] : products[0],
                        FFDConfig.pulverizerSecondaryOutputAmount);
                if (!dust.isEmpty() && !isTrue(invokeStatic(pulverizer,
                        "recipeExists", raw))) {
                    invokeStatic(pulverizer, "addRecipe", 4000, raw, dust, secondary, 10);
                }
                ItemStack ingot = refinedStack(name);
                ItemStack ingotDust = oreStack("dust" + FFDRawOres.capitalize(name), 1);
                if (!ingot.isEmpty() && !ingotDust.isEmpty()
                        && !isTrue(invokeStatic(pulverizer, "recipeExists", ingot))) {
                    invokeStatic(pulverizer, "addRecipe", 4000, ingot, ingotDust,
                            ItemStack.EMPTY, 0);
                }
                registerInductionSmelter(smelter, i, raw, ingot);
                Fluid fluid = fluidFor(name);
                if (fluid != null) {
                    FluidStack rawFluid = new FluidStack(fluid,
                            (int) (FFDConfig.magmaCrucibleOutputMultiple * 288.0F));
                    FluidStack blockFluid = new FluidStack(fluid,
                            (int) (FFDConfig.magmaCrucibleOutputMultiple * 2592.0F));
                    if (!isTrue(invokeStatic(crucible, "recipeExists", raw))) {
                        invokeStatic(crucible, "addRecipe", 16000, raw, rawFluid);
                    }
                    if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[i])) {
                        ItemStack rawBlock = new ItemStack(FFDItems.RAW_ORE_BLOCK_ITEMS[i]);
                        if (!isTrue(invokeStatic(crucible, "recipeExists", rawBlock))) {
                            invokeStatic(crucible, "addRecipe", 16000, rawBlock, blockFluid);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Thermal Expansion raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerInductionSmelter(Class<?> smelter, int index, ItemStack raw,
                                                  ItemStack refined) {
        if (refined.isEmpty()) {
            return;
        }
        String[] additives = {"sand", "itemSlagRich", "itemCinnabar"};
        String[] fallbacks = {"itemSlagRich", "itemSlag", "itemSlagRich"};
        int[] amounts = {FFDConfig.inductionSmelterSandOutputAmount,
                FFDConfig.inductionSmelterRichSlagOutputAmount,
                FFDConfig.inductionSmelterCinnabarOutputAmount};
        int[] chances = {20, 75, 75};
        for (int i = 0; i < additives.length; i++) {
            for (ItemStack additive : OreDictionary.getOres(additives[i])) {
                ItemStack output = refined.copy();
                output.setCount(amounts[i]);
                ItemStack secondary = i == 2 ? firstRefinedByproduct(index) : ItemStack.EMPTY;
                int chance = chances[i];
                if (secondary.isEmpty()) {
                    secondary = oreStack(fallbacks[i], 1);
                } else if (i == 2) {
                    chance = 100;
                }
                if (!isTrue(invokeStatic(smelter, "recipeExists", raw, additive))) {
                    invokeStatic(smelter, "addRecipe", 4000, raw, additive, output,
                            secondary, chance);
                }
            }
        }
    }

    private static ItemStack firstRefinedByproduct(int index) {
        String[] products = FFDRawOres.productOreNames(index);
        for (int i = 1; i < products.length; i++) {
            if (products[i].startsWith("dust")) {
                ItemStack stack = refinedStack(products[i].substring("dust".length())
                        .toLowerCase(java.util.Locale.ROOT));
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void registerTConstruct() {
        try {
            Class<?> registry = Class.forName("slimeknights.tconstruct.library.TinkerRegistry");
            Class<?> smeltery = Class.forName("slimeknights.tconstruct.smeltery.TinkerSmeltery");
            Object castIngot = staticField(smeltery, "castIngot");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String name = FFDRawOres.NAMES[i];
                Fluid fluid = fluidFor(name);
                if (fluid == null) {
                    continue;
                }
                ItemStack raw = new ItemStack(FFDItems.RAW_ORE_ITEMS[i]);
                ItemStack ingot = refinedStack(name);
                ItemStack ingotBlock = refinedBlockStack(name);
                registerTinkerMelting(registry, raw, fluid,
                        (int) (FFDConfig.fluidMultiple * 288.0F));
                if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[i])) {
                    registerTinkerMelting(registry,
                            new ItemStack(FFDItems.RAW_ORE_BLOCK_ITEMS[i]), fluid,
                            (int) (FFDConfig.fluidMultiple * 2592.0F));
                }
                if (!ingot.isEmpty()) {
                    registerTinkerMelting(registry, ingot, fluid, 144);
                    if (castIngot instanceof ItemStack
                            && invokeStatic(registry, "getTableCasting", castIngot, fluid) == null) {
                        invokeStatic(registry, "registerTableCasting", ingot,
                                ((ItemStack) castIngot).copy(), fluid, 144);
                    }
                }
                if (!ingotBlock.isEmpty()) {
                    registerTinkerMelting(registry, ingotBlock, fluid, 1296);
                    if (invokeStatic(registry, "getBasinCasting", ItemStack.EMPTY, fluid) == null) {
                        invokeStatic(registry, "registerBasinCasting", ingotBlock,
                                ItemStack.EMPTY, fluid, 1296);
                    }
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Tinkers' Construct raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerTinkerMelting(Class<?> registry, ItemStack input, Fluid fluid,
                                              int amount) {
        if (input.isEmpty() || amount <= 0) {
            return;
        }
        if (invokeStatic(registry, "getMelting", input) == null) {
            invokeStatic(registry, "registerMelting", input, fluid, amount);
        }
        if (!Loader.isModLoaded("tcomplement")) {
            return;
        }
        try {
            Class<?> recipeMatch = Class.forName("slimeknights.mantle.util.RecipeMatch");
            Object match = invokeStatic(recipeMatch, "of", input);
            Class<?> meltingRecipe = Class.forName(
                    "slimeknights.tconstruct.library.smeltery.MeltingRecipe");
            Object recipe = newInstance(meltingRecipe, match,
                    new FluidStack(fluid, amount * 2), fluid.getTemperature());
            Class<?> registryClass = Class.forName(
                    "knightminer.tcomplement.library.TCompRegistry");
            if (recipe != null) {
                invokeStatic(registryClass, "registerHighOvenOverride", recipe);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Tinkers' Complement raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerEnderIo() {
        try {
            Class<?> managerClass = Class.forName(
                    "crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager");
            Class<?> inputClass = Class.forName("crazypants.enderio.base.recipe.RecipeInput");
            Class<?> outputClass = Class.forName("crazypants.enderio.base.recipe.RecipeOutput");
            Class<?> recipeClass = Class.forName("crazypants.enderio.base.recipe.Recipe");
            Class<?> bonusClass = Class.forName("crazypants.enderio.base.recipe.RecipeBonusType");
            Object bonus = enumValue(bonusClass, "MULTIPLY_OUTPUT");
            Object level = null;
            Class<?> levelClass = null;
            try {
                levelClass = Class.forName("crazypants.enderio.base.recipe.RecipeLevel");
                level = enumValue(levelClass, "IGNORE");
            } catch (Throwable ignored) {
                levelClass = null;
            }
            Object manager = invokeStatic(managerClass, "getInstance");
            if (manager == null || bonus == null) {
                return;
            }
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String[] productNames = FFDRawOres.productOreNames(i);
                float[] productChances = FFDRawOres.productChances(i);
                List<Object> recipeOutputs = new ArrayList<>();
                ItemStack primary = oreStack(productNames[0],
                        Math.max(1, (int) productChances[0]));
                Object primaryOutput = primary.isEmpty() ? null
                        : newInstance(outputClass, primary, 1.0F);
                if (primaryOutput == null) {
                    continue;
                }
                recipeOutputs.add(primaryOutput);
                for (int product = 1; product < productNames.length; product++) {
                    ItemStack secondary = oreStack(productNames[product], 1);
                    Object secondaryOutput = secondary.isEmpty() ? null
                            : newInstance(outputClass, secondary, productChances[product]);
                    if (secondaryOutput != null) {
                        recipeOutputs.add(secondaryOutput);
                    }
                }
                Object recipeInput = newInstance(inputClass,
                        new ItemStack(FFDItems.RAW_ORE_ITEMS[i]));
                if (recipeInput == null) {
                    continue;
                }
                Object outputs = java.lang.reflect.Array.newInstance(outputClass,
                        recipeOutputs.size());
                for (int output = 0; output < recipeOutputs.size(); output++) {
                    java.lang.reflect.Array.set(outputs, output, recipeOutputs.get(output));
                }
                Object recipe = levelClass == null
                        ? newInstance(recipeClass, recipeInput, 2000, bonus, (Object) outputs)
                        : newInstance(recipeClass, recipeInput, 2000, bonus, level,
                                (Object) outputs);
                if (recipe != null) {
                    invoke(manager, "addRecipe", recipe);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Ender IO raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerGalacticraft() {
        try {
            Class<?> compressor = Class.forName(
                    "micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                String name = FFDRawOres.NAMES[i];
                if (!FFDItems.isRawOreMaterialEnabled(name)) {
                    continue;
                }
                ItemStack ingot = refinedStack(name);
                ItemStack compressed = oreStack("compressed" + FFDRawOres.capitalize(name),
                        FFDConfig.galacticraftCompressedOutputAmount);
                if (!ingot.isEmpty() && !compressed.isEmpty()) {
                    invokeStatic(compressor, "addRecipe", compressed,
                            (Object) new Object[] {"XX", 'X', ingot});
                }
                if (Loader.isModLoaded("ic2")) {
                    ItemStack plate = oreStack("plate" + FFDRawOres.capitalize(name),
                            FFDConfig.galacticraftPlateOutputAmount);
                    if (!ingot.isEmpty() && !plate.isEmpty()
                            && plate.getItem().getRegistryName() != null
                            && "ic2".equals(plate.getItem().getRegistryName().getResourceDomain())) {
                        invokeStatic(compressor, "addRecipe", plate,
                                (Object) new Object[] {"X", 'X', ingot});
                    }
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Galacticraft raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerGalaxySpaceRecipe() {
        if (!Loader.isModLoaded("galaxyspace")
                || !FFDItems.isRawOreMaterialEnabled("magnesium")) {
            return;
        }
        ItemStack magnesium = refinedStack("magnesium");
        ItemStack machineTiered = registryStack("galacticraftcore", "machine_tiered", 1, 8);
        ItemStack basic = registryStack("galaxyspace", "gs_basic", 1, 5);
        ItemStack frame = registryStack("galaxyspace", "machineframes", 1, 2);
        ItemStack output = registryStack("galaxyspace", "modern_storage_module",
                FFDConfig.galaxySpaceModernStorageModuleOutputAmount, 0);
        if (magnesium.isEmpty() || machineTiered.isEmpty() || basic.isEmpty()
                || frame.isEmpty() || output.isEmpty()) {
            return;
        }
        GameRegistry.addShapedRecipe(new ResourceLocation("farmers_future_delight",
                        "galaxyspace_modern_storage_module"),
                new ResourceLocation("farmers_future_delight"), output,
                "ABA", "CDC", "ABA", 'A', magnesium, 'B', machineTiered,
                'C', basic, 'D', frame);
    }

    private static void registerAdvancedRocketry() {
        try {
            Class<?> recipes = Class.forName("zmaster587.libVulpes.recipe.RecipesMachine");
            Object manager = invokeStatic(recipes, "getInstance");
            Class<?> rolling = Class.forName(
                    "zmaster587.advancedRocketry.tile.multiblock.machine.TileRollingMachine");
            Class<?> press = Class.forName("zmaster587.advancedRocketry.block.BlockSmallPlatePress");
            Class<?> lathe = Class.forName(
                    "zmaster587.advancedRocketry.tile.multiblock.machine.TileLathe");
            for (String name : FFDRawOres.NAMES) {
                if (!FFDItems.isRawOreMaterialEnabled(name)) {
                    continue;
                }
                ItemStack refined = refinedStack(name);
                ItemStack refinedBlock = refinedBlockStack(name);
                ItemStack plate = oreStack("plate" + FFDRawOres.capitalize(name),
                        FFDConfig.rollingMachinePlateOutputAmount);
                ItemStack pressedPlate = plate.copy();
                if (!pressedPlate.isEmpty()) {
                    pressedPlate.setCount(FFDConfig.smallPlatePressOutputAmount);
                }
                ItemStack rod = oreStack("stick" + FFDRawOres.capitalize(name),
                        FFDConfig.latheRodOutputAmount);
                addLibVulpesRecipe(manager, rolling, plate, refined, true, 300, 20);
                addLibVulpesRecipe(manager, press, pressedPlate, refinedBlock, false, 0, 0);
                addLibVulpesRecipe(manager, lathe, rod, refined, false, 300, 20);
            }
            registerAdvancedRocketryArcFurnace(manager);
        } catch (Throwable ignored) {
            LOGGER.debug("Advanced Rocketry raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void addLibVulpesRecipe(Object manager, Class<?> machine, ItemStack output,
                                           ItemStack input, boolean water, int time, int power) {
        if (manager == null || output.isEmpty() || input.isEmpty()) {
            return;
        }
        Object[] inputs = water
                ? new Object[] {input, new FluidStack(FluidRegistry.WATER, 100)}
                : new Object[] {input};
        invoke(manager, "addRecipe", machine, output, time, power, inputs);
    }

    private static void registerAdvancedRocketryArcFurnace(Object manager) throws Exception {
        Class<?> arcFurnace = Class.forName(
                "zmaster587.advancedRocketry.tile.multiblock.machine.TileElectricArcFurnace");
        ItemStack titanium = refinedStack("titanium");
        ItemStack iridium = refinedStack("iridium");
        ItemStack aluminium = refinedStack("aluminium");
        ItemStack titaniumIridium = oreStack("ingotTitaniumIridium",
                FFDConfig.arcFurnaceTitaniumIridiumOutputAmount);
        ItemStack titaniumAluminide = oreStack("ingotTitaniumAluminide",
                FFDConfig.arcFurnaceTitaniumAluminideOutputAmount);
        if (FFDItems.isRawOreMaterialEnabled("titanium")
                && FFDItems.isRawOreMaterialEnabled("iridium")
                && !titanium.isEmpty() && !iridium.isEmpty() && !titaniumIridium.isEmpty()) {
            invoke(manager, "addRecipe", arcFurnace, titaniumIridium, 3000, 20,
                    new Object[] {titanium, iridium});
        }
        if (FFDItems.isRawOreMaterialEnabled("titanium")
                && FFDItems.isRawOreMaterialEnabled("aluminium")
                && !titanium.isEmpty() && !aluminium.isEmpty() && !titaniumAluminide.isEmpty()) {
            invoke(manager, "addRecipe", arcFurnace, titaniumAluminide, 9000, 20,
                    new Object[] {titanium, aluminium});
        }
    }

    private static void registerTechguns() {
        try {
            Class<?> items = Class.forName("techguns.TGItems");
            Class<?> fluids = Class.forName("techguns.TGFluids");
            Object acid = staticField(fluids, "ACID");
            ItemStack rawUranium = rawStack("uranium");
            ItemStack yellowcake = copyStack(staticField(items, "YELLOWCAKE"),
                    FFDConfig.techgunsYellowcakeOutputAmount);
            if (!rawUranium.isEmpty() && !yellowcake.isEmpty() && acid instanceof Fluid) {
                Class<?> chemLab = Class.forName(
                        "techguns.tileentities.operation.ChemLabRecipes");
                invokeStatic(chemLab, "addRecipe", rawUranium, 1, ItemStack.EMPTY, 0,
                        ItemStack.EMPTY, 0,
                        new FluidStack((Fluid) acid,
                                FFDConfig.techgunsChemicalLabAcidAmount), null,
                        yellowcake, false, 20);
            }
            registerTechgunsReaction(items, acid);
        } catch (Throwable ignored) {
            LOGGER.debug("Techguns raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerTechgunsReaction(Class<?> items, Object acid) throws Exception {
        ItemStack rawTitanium = rawStack("titanium");
        ItemStack rawIron = rawStack("iron");
        ItemStack heatRay = copyStack(staticField(items, "RC_HEAT_RAY"), 1);
        ItemStack titaniumOre = copyStack(staticField(items, "ORE_TITANIUM"),
                FFDConfig.techgunsTitaniumOreOutputAmount);
        if (rawTitanium.isEmpty() || rawIron.isEmpty() || heatRay.isEmpty()
                || titaniumOre.isEmpty() || !(acid instanceof Fluid)) {
            return;
        }
        rawIron.setCount(FFDConfig.techgunsRawIronOutputAmount);
        Class<?> inputClass = Class.forName("techguns.util.ItemStackOreDict");
        Object input = newInstance(inputClass, rawTitanium);
        Class<?> reaction = Class.forName(
                "techguns.tileentities.operation.ReactionChamberRecipe");
        Class<?> riskClass = Class.forName(
                "techguns.tileentities.operation.ReactionChamberRecipe$RiskType");
        Object risk = enumValue(riskClass, "BREAK_ITEM");
        if (input != null && risk != null && invokeStatic(reaction, "getByKey",
                "FFD_TITANIUM_RAW") == null) {
            invokeStatic(reaction, "addRecipe", "FFD_TITANIUM_RAW", input, heatRay,
                    acid, new ItemStack[] {titaniumOre, rawIron}, 2, 1, 5, 0, 3,
                    FFDConfig.techgunsReactionChamberAcidAmount, 0.0F, risk, 25000);
        }
    }

    private static void registerThaumcraft() {
        try {
            Class<?> api = Class.forName("thaumcraft.api.ThaumcraftApi");
            Class<?> aspectHelper = Class.forName("thaumcraft.api.aspects.AspectHelper");
            Class<?> aspectList = Class.forName("thaumcraft.api.aspects.AspectList");
            Class<?> aspect = Class.forName("thaumcraft.api.aspects.Aspect");
            Class<?> crucible = Class.forName("thaumcraft.api.crafting.CrucibleRecipe");
            Object metal = staticField(aspect, "METAL");
            Object order = staticField(aspect, "ORDER");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String name = FFDRawOres.NAMES[i];
                registerThaumcraftAspect(api, aspectHelper, FFDRawOres.rawOreName(name),
                        refinedStack(name));
                registerThaumcraftAspect(api, aspectHelper, FFDRawOres.rawBlockOreName(name),
                        refinedBlockStack(name));
                ItemStack cluster = oreStack("cluster" + FFDRawOres.capitalize(name), 1);
                if (cluster.isEmpty()) {
                    continue;
                }
                Object aspects = newInstance(aspectList);
                invoke(aspects, "merge", metal, 5);
                invoke(aspects, "merge", order, 5);
                Object recipe = newInstance(crucible, "METALPURIFICATION", cluster,
                        FFDRawOres.rawOreName(name), aspects);
                if (recipe != null) {
                    invokeStatic(api, "addCrucibleRecipe", new ResourceLocation(
                            "farmers_future_delight", "raw_" + name + "_cluster"), recipe);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Thaumcraft raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerThaumcraftAspect(Class<?> api, Class<?> aspectHelper,
                                                  String oreName, ItemStack source) {
        if (source.isEmpty()) {
            return;
        }
        Object aspects = invokeStatic(aspectHelper, "getObjectAspects", source);
        Object copy = invoke(aspects, "copy");
        if (copy != null) {
            invokeStatic(api, "registerObjectTag", oreName, copy);
        }
    }

    private static void registerMetallurgy() {
        try {
            Class<?> recipes = Class.forName("it.hurts.metallurgy_reforged.recipe.CrusherRecipes");
            Object manager = invokeStatic(recipes, "getInstance");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                ItemStack output = oreStack("dust" + FFDRawOres.capitalize(
                                FFDRawOres.NAMES[i]),
                        FFDConfig.metallurgyCrusherOutputAmount);
                if (!output.isEmpty()) {
                    invoke(manager, "addCrushingRecipe",
                            new ItemStack(FFDItems.RAW_ORE_ITEMS[i]), output, 1.0F);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Metallurgy raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerSakura() {
        try {
            Item tatara = ForgeRegistries.ITEMS.getValue(new ResourceLocation("sakura", "tatara"));
            Item bambooCharcoal = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("sakura", "bamboo_charcoal_block"));
            ItemStack rawIron = rawStack("iron");
            if (tatara == null || rawIron.isEmpty()
                    || OreDictionary.getOres("toolForginghammer").isEmpty()) {
                return;
            }
            if (bambooCharcoal != null) {
                GameRegistry.addShapelessRecipe(new ResourceLocation(
                                "farmers_future_delight", "sakura_tatara_bamboo_charcoal"),
                        new ResourceLocation("farmers_future_delight"), new ItemStack(tatara),
                        Ingredient.fromStacks(rawIron),
                        Ingredient.fromItem(bambooCharcoal), new OreIngredient("toolForginghammer"));
            }
            GameRegistry.addShapelessRecipe(new ResourceLocation(
                            "farmers_future_delight", "sakura_tatara_charcoal"),
                    new ResourceLocation("farmers_future_delight"), new ItemStack(tatara),
                    Ingredient.fromStacks(rawIron), Ingredient.fromStacks(
                            new ItemStack(Blocks.COAL_BLOCK)),
                    new OreIngredient("toolForginghammer"));
        } catch (Throwable ignored) {
            LOGGER.debug("Sakura raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerGregTech() {
        try {
            Class<?> maps = Class.forName("gregtech.api.recipes.RecipeMaps");
            Object hammer = staticField(maps, "FORGE_HAMMER_RECIPES");
            Object macerator = staticField(maps, "MACERATOR_RECIPES");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (!FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                    continue;
                }
                String[] products = FFDRawOres.productOreNames(i);
                float[] chances = FFDRawOres.productChances(i);
                ItemStack primary = oreStack("crushed" + FFDRawOres.capitalize(
                        FFDRawOres.NAMES[i]), Math.max(1, (int) chances[0]));
                if (primary.isEmpty()) {
                    continue;
                }
                ItemStack input = new ItemStack(FFDItems.RAW_ORE_ITEMS[i]);
                registerGregTechRecipe(hammer, input, primary, products, chances, false);
                registerGregTechRecipe(macerator, input, primary, products, chances, true);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("GregTech raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerGregTechRecipe(Object map, ItemStack input, ItemStack output,
                                               String[] products, float[] chances,
                                               boolean byproducts) {
        Object builder = invoke(map, "recipeBuilder");
        if (builder == null) {
            return;
        }
        invoke(builder, "inputs", (Object) new ItemStack[] {input});
        invoke(builder, "outputs", (Object) new ItemStack[] {output});
        invoke(builder, "duration", 400);
        if (invoke(builder, "EUt", 2) == null) {
            invoke(builder, "EUt", 2L);
        }
        if (byproducts) {
            Object maxOutputsValue = invoke(map, "getMaxOutputs");
            int remainingOutputs = maxOutputsValue instanceof Number
                    ? Math.max(0, ((Number) maxOutputsValue).intValue() - 1) : 2;
            for (int i = 1; i < products.length; i++) {
                if (remainingOutputs <= 0) {
                    break;
                }
                if (!products[i].startsWith("dust")) {
                    continue;
                }
                ItemStack secondary = oreStack(products[i], 1);
                if (!secondary.isEmpty()) {
                    float boost = chances[i] < 0.1F ? 0.015F : 0.085F;
                    invoke(builder, "chancedOutput", secondary,
                            (int) (chances[i] * 10000.0F), (int) (boost * 10000.0F));
                    remainingOutputs--;
                }
            }
        }
        invoke(builder, "buildAndRegister");
    }

    private static void registerCroparia() {
        for (String name : FFDRawOres.NAMES) {
            if (!FFDItems.isRawOreMaterialEnabled(name)) {
                continue;
            }
            ItemStack fruit = registryStack("croparia", "fruit_" + name);
            if (fruit.isEmpty()) {
                fruit = registryStack("croparia", "fruit_" + name.replace("inium", "inum"));
            }
            if (fruit.isEmpty()) {
                fruit = registryStack("croparia", "fruit_" + name.replace("ium", "e"));
            }
            if (fruit.isEmpty()) {
                fruit = oreStack("fruit" + FFDRawOres.capitalize(name), 1);
            }
            ItemStack output = refinedStack(name);
            if (fruit.isEmpty() || output.isEmpty()) {
                continue;
            }
            output.setCount(FFDConfig.cropariaRefinedOutputAmount);
            GameRegistry.addShapelessRecipe(new ResourceLocation("farmers_future_delight",
                            "croparia_" + name + "_refining"),
                    new ResourceLocation("farmers_future_delight"), output,
                    Ingredient.fromStacks(fruit));
        }
    }

    private static void registerMysticalAgriculture() {
        for (String name : FFDRawOres.NAMES) {
            if (!FFDItems.isRawOreMaterialEnabled(name)) {
                continue;
            }
            ItemStack essence = registryStack("mysticalagriculture", name + "_essence");
            if (essence.isEmpty()) {
                essence = registryStack("mysticalagriculture",
                        name.replace("inium", "inum") + "_essence");
            }
            if (essence.isEmpty()) {
                essence = registryStack("mysticalagradditions", name + "_essence");
            }
            if (essence.isEmpty()) {
                essence = registryStack("mysticalagradditions",
                        name.replace("inium", "inum") + "_essence");
            }
            if (essence.isEmpty()) {
                essence = oreStack("essence" + FFDRawOres.capitalize(name), 1);
            }
            ItemStack output = refinedStack(name);
            if (essence.isEmpty() || output.isEmpty()) {
                continue;
            }
            output.setCount(FFDConfig.mysticalAgricultureRefinedOutputAmount);
            GameRegistry.addShapedRecipe(new ResourceLocation("farmers_future_delight",
                            "mystical_agriculture_" + name + "_refining"),
                    new ResourceLocation("farmers_future_delight"), output,
                    "AAA", "A A", "AAA", 'A', essence);
        }
    }

    private static void registerJer() {
        if (!FFDConfig.oresDropRawMaterials) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("jeresources.compatibility.JERAPI");
            Object api = invokeStatic(apiClass, "getInstance");
            Object registry = invoke(api, "getWorldGenRegistry");
            Class<?> lootDropClass = Class.forName("jeresources.api.drop.LootDrop");
            Class<?> conditionalClass = Class.forName("jeresources.api.conditionals.Conditional");
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                if (FFDRawOres.isCopper(FFDRawOres.NAMES[i])
                        ? !FFDItems.isCopperEnabled() : !FFDItems.isRawOreEnabled()) {
                    continue;
                }
                ItemStack raw = rawStack(FFDRawOres.NAMES[i]);
                if (raw.isEmpty()) {
                    continue;
                }
                int baseMin = FFDRawOres.isCopper(FFDRawOres.NAMES[i])
                        ? 2 : FFDConfig.rawOreDropAmount;
                int baseMax = FFDRawOres.isCopper(FFDRawOres.NAMES[i])
                        ? 5 : FFDConfig.rawOreDropAmount;
                for (ItemStack ore : matchingOres(FFDRawOres.NAMES[i])) {
                    int multiplier = FFDConfig.denseRawOreDrop && isDenseOre(ore)
                            ? denseOreMultiplier() : 1;
                    Object conditionals = java.lang.reflect.Array.newInstance(
                            conditionalClass, 0);
                    Object drop = newInstance(lootDropClass, raw, baseMin * multiplier,
                            baseMax * multiplier, conditionals);
                    if (drop == null) {
                        continue;
                    }
                    Object drops = java.lang.reflect.Array.newInstance(lootDropClass, 1);
                    java.lang.reflect.Array.set(drops, 0, drop);
                    invoke(registry, "registerDrops", ore, drops);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Just Enough Resources raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomRawOreCompatibility() {
        if (FFDCustomRawOres.entries().isEmpty()) {
            return;
        }
        if (Loader.isModLoaded("ic2")) {
            registerCustomIc2();
        }
        if (Loader.isModLoaded("mekanism")) {
            registerCustomMekanism();
        }
        if (Loader.isModLoaded("thermalexpansion")) {
            registerCustomThermalExpansion();
        }
        if (Loader.isModLoaded("tconstruct")) {
            registerCustomTConstruct();
        }
        if (Loader.isModLoaded("enderio")) {
            registerCustomEnderIo();
        }
        if (Loader.isModLoaded("thaumcraft")) {
            registerCustomThaumcraft();
        }
        if (Loader.isModLoaded("metallurgy")) {
            registerCustomMetallurgy();
        }
        if (Loader.isModLoaded("gregtech")) {
            registerCustomGregTech();
        }
        if (Loader.isModLoaded("galacticraftcore")
                || Loader.isModLoaded("galacticraftplanets")) {
            registerCustomGalacticraft();
        }
        if (Loader.isModLoaded("advancedrocketry")) {
            registerCustomAdvancedRocketry();
        }
        if (Loader.isModLoaded("jer") || Loader.isModLoaded("jeresources")) {
            registerCustomJer();
        }
    }

    private static void registerCustomIc2() {
        try {
            Class<?> recipesClass = Class.forName("ic2.api.recipe.Recipes");
            Object macerator = staticField(recipesClass, "macerator");
            Object inputFactory = staticField(recipesClass, "inputFactory");
            if (macerator == null || inputFactory == null) {
                return;
            }
            String prefix = Loader.isModLoaded("ic2-classic-spmod")
                    && !Loader.isModLoaded("ic2c_extras") ? "dust" : "crushed";
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                ItemStack output = oreStack(prefix + entry.refinedSuffix(),
                        FFDConfig.maceratorRawOutputAmount);
                if (!raw.isEmpty() && !output.isEmpty()) {
                    addIc2Recipe(macerator, invoke(inputFactory, "forStack", raw), output);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("IC2 custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomMekanism() {
        try {
            Class<?> handler = Class.forName("mekanism.common.recipe.RecipeHandler");
            Object hydrogenChloride = null;
            try {
                hydrogenChloride = invokeStatic(Class.forName("mekanism.api.gas.GasRegistry"),
                        "getGas", "hydrogenchloride");
            } catch (Throwable ignored) {
                hydrogenChloride = null;
            }
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                if (raw.isEmpty()) {
                    continue;
                }
                String suffix = entry.refinedSuffix();
                ItemStack dust = oreStack("dust" + suffix, FFDConfig.enrichmentOutputAmount);
                ItemStack clump = oreStack("clump" + suffix, FFDConfig.purificationOutputAmount);
                ItemStack shard = oreStack("shard" + suffix,
                        FFDConfig.chemicalInjectionChamberOutputAmount);
                if (!dust.isEmpty()) {
                    invokeStatic(handler, "addEnrichmentChamberRecipe", raw, dust);
                }
                if (!clump.isEmpty()) {
                    invokeStatic(handler, "addPurificationChamberRecipe", raw, clump);
                }
                if (!shard.isEmpty() && hydrogenChloride != null) {
                    invokeStatic(handler, "addChemicalInjectionChamberRecipe", raw,
                            hydrogenChloride, shard);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Mekanism custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomThermalExpansion() {
        try {
            Class<?> pulverizer = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.PulverizerManager");
            Class<?> crucible = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.CrucibleManager");
            Class<?> smelter = Class.forName(
                    "cofh.thermalexpansion.util.managers.machine.SmelterManager");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                if (raw.isEmpty()) {
                    continue;
                }
                ItemStack dust = oreStack("dust" + entry.refinedSuffix(),
                        FFDConfig.pulverizerOutputAmount);
                if (!dust.isEmpty() && !isTrue(invokeStatic(pulverizer,
                        "recipeExists", raw))) {
                    invokeStatic(pulverizer, "addRecipe", 4000, raw, dust,
                            ItemStack.EMPTY, 0);
                }
                ItemStack refined = entry.resolveSmeltResult();
                if (!refined.isEmpty()) {
                    for (ItemStack sand : OreDictionary.getOres("sand")) {
                        ItemStack output = refined.copy();
                        output.setCount(FFDConfig.inductionSmelterSandOutputAmount);
                        if (!isTrue(invokeStatic(smelter, "recipeExists", raw, sand))) {
                            invokeStatic(smelter, "addRecipe", 4000, raw, sand, output,
                                    oreStack("itemSlagRich", 1), 20);
                        }
                    }
                }
                Fluid fluid = customFluidFor(entry);
                if (fluid != null) {
                    if (!isTrue(invokeStatic(crucible, "recipeExists", raw))) {
                        invokeStatic(crucible, "addRecipe", 16000, raw,
                                new FluidStack(fluid,
                                        (int) (FFDConfig.magmaCrucibleOutputMultiple * 288.0F)));
                    }
                    ItemStack block = entry.blockStack();
                    if (!block.isEmpty()
                            && !isTrue(invokeStatic(crucible, "recipeExists", block))) {
                        invokeStatic(crucible, "addRecipe", 16000, block,
                                new FluidStack(fluid,
                                        (int) (FFDConfig.magmaCrucibleOutputMultiple * 2592.0F)));
                    }
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Thermal Expansion custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomTConstruct() {
        try {
            Class<?> registry = Class.forName("slimeknights.tconstruct.library.TinkerRegistry");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                Fluid fluid = tconstructFluidFor(registry, entry);
                if (fluid == null) {
                    continue;
                }
                registerTinkerMelting(registry, entry.rawStack(), fluid,
                        (int) (FFDConfig.fluidMultiple * 288.0F));
                registerTinkerMelting(registry, entry.blockStack(), fluid,
                        (int) (FFDConfig.fluidMultiple * 2592.0F));
                registerTinkerMelting(registry, entry.resolveSmeltResult(), fluid, 144);
                registerTinkerMelting(registry, entry.resolveRefinedBlock(), fluid, 1296);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Tinkers' Construct custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static Fluid tconstructFluidFor(Class<?> registry,
                                             FFDCustomRawOres.Entry entry) {
        Fluid sourceFluid = null;
        boolean found = false;
        for (ItemStack source : entry.sourceStacks()) {
            Fluid candidate = tconstructMeltingFluid(registry, source);
            if (candidate == null) {
                sourceFluid = null;
                found = false;
                break;
            }
            if (sourceFluid != null && sourceFluid != candidate) {
                return null;
            }
            sourceFluid = candidate;
            found = true;
        }
        if (found) {
            return sourceFluid;
        }
        Fluid fluid = tconstructMeltingFluid(registry, entry.resolveSmeltResult());
        if (fluid != null) {
            return fluid;
        }
        return customFluidFor(entry);
    }

    private static Fluid tconstructMeltingFluid(Class<?> registry, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            Object recipe = invokeStatic(registry, "getMelting", stack);
            Object result = invoke(recipe, "getResult");
            return result instanceof FluidStack ? ((FluidStack) result).getFluid() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void registerCustomEnderIo() {
        try {
            Class<?> managerClass = Class.forName(
                    "crazypants.enderio.base.recipe.sagmill.SagMillRecipeManager");
            Class<?> inputClass = Class.forName("crazypants.enderio.base.recipe.RecipeInput");
            Class<?> outputClass = Class.forName("crazypants.enderio.base.recipe.RecipeOutput");
            Class<?> recipeClass = Class.forName("crazypants.enderio.base.recipe.Recipe");
            Class<?> bonusClass = Class.forName("crazypants.enderio.base.recipe.RecipeBonusType");
            Object bonus = enumValue(bonusClass, "MULTIPLY_OUTPUT");
            Object level = null;
            Class<?> levelClass = null;
            try {
                levelClass = Class.forName("crazypants.enderio.base.recipe.RecipeLevel");
                level = enumValue(levelClass, "IGNORE");
            } catch (Throwable ignored) {
                levelClass = null;
            }
            Object manager = invokeStatic(managerClass, "getInstance");
            if (manager == null || bonus == null) {
                return;
            }
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                ItemStack dust = oreStack("dust" + entry.refinedSuffix(),
                        FFDConfig.pulverizerOutputAmount);
                Object input = raw.isEmpty() ? null : newInstance(inputClass, raw);
                Object output = dust.isEmpty() ? null : newInstance(outputClass, dust, 1.0F);
                if (input == null || output == null) {
                    continue;
                }
                Object outputs = java.lang.reflect.Array.newInstance(outputClass, 1);
                java.lang.reflect.Array.set(outputs, 0, output);
                Object recipe = levelClass == null
                        ? newInstance(recipeClass, input, 2000, bonus, outputs)
                        : newInstance(recipeClass, input, 2000, bonus, level, outputs);
                if (recipe != null) {
                    invoke(manager, "addRecipe", recipe);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Ender IO custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomThaumcraft() {
        try {
            Class<?> api = Class.forName("thaumcraft.api.ThaumcraftApi");
            Class<?> aspectHelper = Class.forName("thaumcraft.api.aspects.AspectHelper");
            Class<?> aspectList = Class.forName("thaumcraft.api.aspects.AspectList");
            Class<?> aspect = Class.forName("thaumcraft.api.aspects.Aspect");
            Class<?> crucible = Class.forName("thaumcraft.api.crafting.CrucibleRecipe");
            Object metal = staticField(aspect, "METAL");
            Object order = staticField(aspect, "ORDER");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                registerThaumcraftAspect(api, aspectHelper, entry.rawOreName(),
                        entry.resolveSmeltResult());
                registerThaumcraftAspect(api, aspectHelper, entry.rawBlockOreName(),
                        entry.resolveRefinedBlock());
                ItemStack cluster = oreStack("cluster" + entry.refinedSuffix(), 1);
                if (cluster.isEmpty()) {
                    continue;
                }
                Object aspects = newInstance(aspectList);
                invoke(aspects, "merge", metal, 5);
                invoke(aspects, "merge", order, 5);
                Object recipe = newInstance(crucible, "METALPURIFICATION", cluster,
                        entry.rawOreName(), aspects);
                if (recipe != null) {
                    invokeStatic(api, "addCrucibleRecipe", new ResourceLocation(
                            FarmerFutureDelight.MODID,
                            "custom_raw_" + entry.material() + "_cluster"), recipe);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Thaumcraft custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomMetallurgy() {
        try {
            Class<?> recipes = Class.forName("it.hurts.metallurgy_reforged.recipe.CrusherRecipes");
            Object manager = invokeStatic(recipes, "getInstance");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                ItemStack dust = oreStack("dust" + entry.refinedSuffix(),
                        FFDConfig.metallurgyCrusherOutputAmount);
                if (!raw.isEmpty() && !dust.isEmpty()) {
                    invoke(manager, "addCrushingRecipe", raw, dust, 1.0F);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Metallurgy custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomGregTech() {
        try {
            Class<?> maps = Class.forName("gregtech.api.recipes.RecipeMaps");
            Object hammer = staticField(maps, "FORGE_HAMMER_RECIPES");
            Object macerator = staticField(maps, "MACERATOR_RECIPES");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                ItemStack crushed = oreStack("crushed" + entry.refinedSuffix(), 2);
                if (raw.isEmpty() || crushed.isEmpty()) {
                    continue;
                }
                String[] products = {"dust" + entry.refinedSuffix()};
                float[] chances = {2.0F};
                registerGregTechRecipe(hammer, raw, crushed, products, chances, false);
                registerGregTechRecipe(macerator, raw, crushed, products, chances, true);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("GregTech custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomGalacticraft() {
        try {
            Class<?> compressor = Class.forName(
                    "micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack refined = entry.resolveSmeltResult();
                ItemStack compressed = oreStack("compressed" + entry.refinedSuffix(),
                        FFDConfig.galacticraftCompressedOutputAmount);
                if (!refined.isEmpty() && !compressed.isEmpty()) {
                    invokeStatic(compressor, "addRecipe", compressed,
                            (Object) new Object[] {"XX", 'X', refined});
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Galacticraft custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomAdvancedRocketry() {
        try {
            Class<?> recipes = Class.forName("zmaster587.libVulpes.recipe.RecipesMachine");
            Object manager = invokeStatic(recipes, "getInstance");
            Class<?> rolling = Class.forName(
                    "zmaster587.advancedRocketry.tile.multiblock.machine.TileRollingMachine");
            Class<?> press = Class.forName("zmaster587.advancedRocketry.block.BlockSmallPlatePress");
            Class<?> lathe = Class.forName(
                    "zmaster587.advancedRocketry.tile.multiblock.machine.TileLathe");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack refined = entry.resolveSmeltResult();
                ItemStack refinedBlock = entry.resolveRefinedBlock();
                ItemStack plate = oreStack("plate" + entry.refinedSuffix(),
                        FFDConfig.rollingMachinePlateOutputAmount);
                ItemStack pressedPlate = plate.copy();
                if (!pressedPlate.isEmpty()) {
                    pressedPlate.setCount(FFDConfig.smallPlatePressOutputAmount);
                }
                ItemStack rod = oreStack("stick" + entry.refinedSuffix(),
                        FFDConfig.latheRodOutputAmount);
                addLibVulpesRecipe(manager, rolling, plate, refined, true, 300, 20);
                addLibVulpesRecipe(manager, press, pressedPlate, refinedBlock, false, 0, 0);
                addLibVulpesRecipe(manager, lathe, rod, refined, false, 300, 20);
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Advanced Rocketry custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static void registerCustomJer() {
        if (!FFDConfig.oresDropRawMaterials) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName("jeresources.compatibility.JERAPI");
            Object api = invokeStatic(apiClass, "getInstance");
            Object registry = invoke(api, "getWorldGenRegistry");
            Class<?> lootDropClass = Class.forName("jeresources.api.drop.LootDrop");
            Class<?> conditionalClass = Class.forName("jeresources.api.conditionals.Conditional");
            for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
                ItemStack raw = entry.rawStack();
                if (raw.isEmpty()) {
                    continue;
                }
                for (ItemStack ore : entry.sourceStacks()) {
                    int multiplier = FFDConfig.denseRawOreDrop && isDenseOre(ore)
                            ? denseOreMultiplier() : 1;
                    Object conditionals = java.lang.reflect.Array.newInstance(conditionalClass, 0);
                    Object drop = newInstance(lootDropClass, raw,
                            FFDConfig.rawOreDropAmount * multiplier,
                            FFDConfig.rawOreDropAmount * multiplier, conditionals);
                    if (drop == null) {
                        continue;
                    }
                    Object drops = java.lang.reflect.Array.newInstance(lootDropClass, 1);
                    java.lang.reflect.Array.set(drops, 0, drop);
                    invoke(registry, "registerDrops", ore, drops);
                }
            }
        } catch (Throwable ignored) {
            LOGGER.debug("Just Enough Resources custom raw-ore compatibility was unavailable", ignored);
        }
    }

    private static Fluid customFluidFor(FFDCustomRawOres.Entry entry) {
        String material = entry.material();
        String suffix = entry.refinedSuffix().toLowerCase(java.util.Locale.ROOT);
        String[] names = {material, suffix, "molten_" + material, "molten_" + suffix,
                "molten" + material, "molten" + suffix};
        for (String name : names) {
            Fluid fluid = FluidRegistry.getFluid(name);
            if (fluid != null) {
                return fluid;
            }
        }
        return null;
    }

    private static ItemStack oreStack(String name, int amount) {
        ItemStack stack = FFDCompat.firstOreDictionaryStack(name);
        if (!stack.isEmpty()) {
            stack.setCount(amount);
        }
        return stack;
    }

    private static ItemStack rawStack(String name) {
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            if (name.equals(FFDRawOres.NAMES[i])) {
                return FFDItems.effectiveStack(FFDItems.RAW_ORE_ITEMS[i]);
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack registryStack(String namespace, String path) {
        return registryStack(namespace, path, 1, 0);
    }

    private static ItemStack registryStack(String namespace, String path, int amount, int metadata) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, path));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, amount, metadata);
    }

    private static ItemStack copyStack(Object value, int amount) {
        if (!(value instanceof ItemStack) || ((ItemStack) value).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ((ItemStack) value).copy();
        stack.setCount(amount);
        return stack;
    }

    private static List<ItemStack> matchingOres(String name) {
        List<ItemStack> ores = new ArrayList<>();
        for (String refined : FFDRawOres.refinedOreNames(name)) {
            String suffix = refined.startsWith("ingot")
                    ? refined.substring("ingot".length()) : FFDRawOres.capitalize(name);
            ores.addAll(OreDictionary.getOres("ore" + suffix));
            ores.addAll(OreDictionary.getOres("ore" + suffix + "Dense"));
        }
        return ores;
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
        if (!Loader.isModLoaded("densemetals")) {
            return 2;
        }
        try {
            Class<?> config = Class.forName("com.mcmoddev.densemetals.DenseMetalsConfig");
            return Math.max(1, config.getField("denseOreValue").getInt(null));
        } catch (Throwable ignored) {
            return 2;
        }
    }

    private static ItemStack refinedStack(String name) {
        if ("iron".equals(name)) {
            return new ItemStack(net.minecraft.init.Items.IRON_INGOT);
        }
        if ("gold".equals(name)) {
            return new ItemStack(net.minecraft.init.Items.GOLD_INGOT);
        }
        if ("copper".equals(name)) {
            return FFDItems.effectiveStack(FFDItems.COPPER_INGOT);
        }
        for (String oreName : FFDRawOres.refinedOreNames(name)) {
            ItemStack stack = FFDCompat.firstOreDictionaryStack(oreName);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack refinedBlockStack(String name) {
        if ("iron".equals(name)) {
            return new ItemStack(Blocks.IRON_BLOCK);
        }
        if ("gold".equals(name)) {
            return new ItemStack(Blocks.GOLD_BLOCK);
        }
        if ("copper".equals(name)) {
            return FFDItems.effectiveStack(FFDItems.COPPER_BLOCK);
        }
        for (String refined : FFDRawOres.refinedOreNames(name)) {
            String suffix = refined.startsWith("ingot")
                    ? refined.substring("ingot".length()) : FFDRawOres.capitalize(name);
            ItemStack stack = FFDCompat.firstOreDictionaryStack("block" + suffix);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Fluid fluidFor(String name) {
        for (String candidate : FFDRawOres.fluidNames(name)) {
            Fluid fluid = FluidRegistry.getFluid(candidate);
            if (fluid != null) {
                return fluid;
            }
        }
        return null;
    }

    private static Object staticField(Class<?> type, String name) throws ReflectiveOperationException {
        Field field = type.getField(name);
        return field.get(null);
    }

    private static Object newInstance(Class<?> type, Object... args) {
        for (java.lang.reflect.Constructor<?> constructor : type.getConstructors()) {
            if (constructor.getParameterTypes().length != args.length
                    || !matches(constructor.getParameterTypes(), args)) {
                continue;
            }
            try {
                return constructor.newInstance(args);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object enumValue(Class<?> type, String name) {
        if (!type.isEnum()) {
            return null;
        }
        for (Object value : type.getEnumConstants()) {
            if (name.equals(String.valueOf(value))) {
                return value;
            }
        }
        return null;
    }

    private static boolean isTrue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    private static Object invokeStatic(Class<?> type, String name, Object... args) {
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(name)
                    || method.getParameterTypes().length != args.length
                    || !matches(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                return method.invoke(null, args);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object invoke(Object target, String name, Object... args) {
        if (target == null) {
            return null;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name)
                    || method.getParameterTypes().length != args.length
                    || !matches(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                return method.invoke(target, args);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean matches(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                continue;
            }
            if (parameterTypes[i].isPrimitive()) {
                if (!primitiveWrapper(parameterTypes[i]).isInstance(args[i])) {
                    return false;
                }
            } else if (!parameterTypes[i].isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> primitiveWrapper(Class<?> primitive) {
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
    }
}
