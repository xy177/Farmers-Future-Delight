package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.block.BlockCompatDeepslateOre;

public final class FFDDeepslateOreCompat {
    private static final Logger LOGGER = LogManager.getLogger("FFD Deepslate Ore Compat");
    private static final String MODEL_PATH = "deepslate_compat_ore";
    private static final List<Definition> DEFINITIONS = new ArrayList<>();
    private static boolean prepared;
    private static boolean recipesRegistered;
    private static List<Generation> generations;

    private FFDDeepslateOreCompat() {
    }

    public static void prepare() {
        prepare(ForgeRegistries.BLOCKS);
    }

    public static void prepare(IForgeRegistry<Block> registry) {
        if (prepared) {
            return;
        }
        prepared = true;
        if (!FFDItems.isDeepslateEnabled()) {
            return;
        }
        add(registry, "mekanism", "osmium", "osmium", "mekanism:oreblock", 0, 2,
                0x6FAFC4, generation("mekanism:osmium", 4, 8, -64, -1, false));
        add(registry, "mekanism", "tin", "tin", "mekanism:oreblock", 2, 1,
                0xB7C5D0, generation("mekanism:tin", 4, 6, -64, -1, false));
        add(registry, "mekanism", "lead", "lead", "mekanism:oreblock", 4, 1,
                0x59677D, generation("mekanism:lead", 8, 9, -88, 64, true, 0.25F, 0));
        add(registry, "mekanism", "uranium", "uranium", "mekanism:oreblock", 5, 2,
                0x78B84A,
                generation("mekanism:uranium#small", 4, 4, -64, 8, true),
                generation("mekanism:uranium#buried", 7, 9, -88, -8, true, 0.75F, 16));
        add(registry, "thermalfoundation", "tin", "tin", "thermalfoundation:ore", 1, 1,
                0xB7C5D0, generation("thermalfoundation:tin", 6, 8, -64, -1, false));
        add(registry, "thermalfoundation", "silver", "silver", "thermalfoundation:ore", 2, 1,
                0xD9E8EE, generation("thermalfoundation:silver", 4, 8, -64, -1, false));
        add(registry, "thermalfoundation", "lead", "lead", "thermalfoundation:ore", 3, 1,
                0x59677D, generation("thermalfoundation:lead", 6, 8, -64, -1, false));
        add(registry, "thermalfoundation", "nickel", "nickel", "thermalfoundation:ore", 5, 1,
                0xC8B978, generation("thermalfoundation:nickel", 4, 8, -64, -1, false));
        add(registry, "immersiveengineering", "aluminium", "aluminium", "immersiveengineering:ore", 1, 1,
                0xC7CBD0, generation("immersiveengineering:aluminium", 6, 8, -64, -1, false));
        add(registry, "immersiveengineering", "lead", "lead", "immersiveengineering:ore", 2, 1,
                0x59677D, generation("immersiveengineering:lead", 6, 8, -64, -1, false));
        add(registry, "immersiveengineering", "silver", "silver", "immersiveengineering:ore", 3, 1,
                0xD9E8EE, generation("immersiveengineering:silver", 5, 8, -64, -1, false));
        add(registry, "immersiveengineering", "nickel", "nickel", "immersiveengineering:ore", 4, 1,
                0xC8B978, generation("immersiveengineering:nickel", 4, 6, -64, -1, false));
        add(registry, "immersiveengineering", "uranium", "uranium", "immersiveengineering:ore", 5, 2,
                0x78B84A, generation("immersiveengineering:uranium", 5, 6, -64, -1, false));
        add(registry, "techreborn", "aluminium", "aluminium", "techreborn:ore", 4, 1,
                0xC7CBD0, generation("techreborn:aluminium", 4, 6, -64, -1, false));
        add(registry, "techreborn", "iridium", "iridium", "techreborn:ore", 1, 2,
                0xB8D9DF, generation("techreborn:iridium", 2, 3, -64, -1, false));
        add(registry, "techreborn", "lead", "lead", "techreborn:ore", 12, 1,
                0x59677D, generation("techreborn:lead", 5, 6, -64, -1, false));
        add(registry, "techreborn", "silver", "silver", "techreborn:ore", 13, 1,
                0xD9E8EE, generation("techreborn:silver", 5, 6, -64, -1, false));
        add(registry, "techreborn", "tin", "tin", "techreborn:ore2", 1, 1,
                0xB7C5D0, generation("techreborn:tin", 6, 8, -64, -1, false));
        add(registry, "simpleores", "tin", "tin", "simpleores:tin_ore", 0, 1,
                0xB7C5D0, generation("simpleores:tin#intrusion", 0, 7, 0, 72, false));
        add(registry, "simpleores", "mythril", "mithril", "simpleores:mythril_ore", 0, 2,
                0x67CFCB,
                generation("simpleores:mythril#deposit", 8, 4, -63, 32, true),
                generation("simpleores:mythril#intrusion", 4, 4, -64, 35, false));
        add(registry, "simpleores", "adamantium", "adamantium", "simpleores:adamantium_ore", 0, 2,
                0x55B96C,
                generation("simpleores:adamantium#deposit", 4, 4, -63, -16, true),
                generation("simpleores:adamantium#intrusion", 4, 4, -64, 20, false));
        addCustom(registry);
    }

    public static List<BlockCompatDeepslateOre> blocks() {
        prepare();
        List<BlockCompatDeepslateOre> blocks = new ArrayList<>();
        for (Definition definition : DEFINITIONS) {
            blocks.add(definition.block);
        }
        return Collections.unmodifiableList(blocks);
    }

    public static void registerItems() {
        prepare();
        for (Definition definition : DEFINITIONS) {
            if (definition.item == null) {
                if (definition.customEntry != null) {
                    definition.item = definition.customEntry.createItem(definition.block);
                } else {
                    definition.item = new ItemBlock(definition.block)
                            .setRegistryName(definition.block.getRegistryName())
                            .setUnlocalizedName(definition.block.getUnlocalizedName())
                            .setCreativeTab(FFDCreativeTab.INSTANCE);
                }
            }
        }
    }

    public static List<Item> items() {
        prepare();
        List<Item> items = new ArrayList<>();
        for (Definition definition : DEFINITIONS) {
            if (definition.item != null) {
                items.add(definition.item);
            }
        }
        return Collections.unmodifiableList(items);
    }

    public static void registerOreDictionary() {
        prepare();
        for (Definition definition : DEFINITIONS) {
            if (definition.item == null) {
                continue;
            }
            Set<String> names = new LinkedHashSet<>();
            for (String sourceMaterial : definition.sourceMaterials) {
                addDefaultOreNames(names, sourceMaterial);
            }
            addDefaultOreNames(names, definition.rawMaterial);
            for (String name : OreDictionary.getOreNames()) {
                if (!name.startsWith("ore")) {
                    continue;
                }
                for (ItemStack sourceStack : definition.sourceStacks) {
                    for (ItemStack candidate : OreDictionary.getOres(name, false)) {
                        if (OreDictionary.itemMatches(candidate, sourceStack, false)) {
                            names.add(name);
                            break;
                        }
                    }
                }
            }
            for (String name : names) {
                if (definition.registeredOreNames.add(name)) {
                    OreDictionary.registerOre(name, new ItemStack(definition.item));
                }
            }
        }
    }

    public static void registerRecipes() {
        prepare();
        if (recipesRegistered) {
            return;
        }
        recipesRegistered = true;
        for (Definition definition : DEFINITIONS) {
            if (definition.item == null) {
                continue;
            }
            for (ItemStack sourceStack : definition.sourceStacks) {
                ItemStack output = FurnaceRecipes.instance().getSmeltingResult(sourceStack);
                if (!output.isEmpty()) {
                    float experience = FurnaceRecipes.instance().getSmeltingExperience(output);
                    GameRegistry.addSmelting(new ItemStack(definition.item), output.copy(), experience);
                    break;
                }
            }
        }
        FFDDeepslateOreRecipeCompat.register(recipePairs());
    }

    static List<RecipePair> recipePairs() {
        prepare();
        List<RecipePair> pairs = new ArrayList<>();
        addRecipePair(pairs, new ItemStack(Blocks.COAL_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_COAL_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.IRON_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_IRON_ORE));
        addRecipePair(pairs, FFDItems.effectiveStack(FFDItems.COPPER_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_COPPER_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.GOLD_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_GOLD_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.REDSTONE_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_REDSTONE_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.LAPIS_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_LAPIS_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.DIAMOND_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_DIAMOND_ORE));
        addRecipePair(pairs, new ItemStack(Blocks.EMERALD_ORE),
                FFDItems.effectiveStack(FFDItems.DEEPSLATE_EMERALD_ORE));
        for (Definition definition : DEFINITIONS) {
            if (definition.item == null) {
                continue;
            }
            ItemStack deep = new ItemStack(definition.item);
            for (ItemStack source : definition.sourceStacks) {
                addRecipePair(pairs, source, deep);
            }
        }
        return Collections.unmodifiableList(pairs);
    }

    private static void addRecipePair(List<RecipePair> pairs, ItemStack source, ItemStack deep) {
        if (source.isEmpty() || deep.isEmpty()
                || ItemStack.areItemsEqual(source, deep)) {
            return;
        }
        pairs.add(new RecipePair(source, deep));
    }

    public static List<Generation> generations() {
        prepare();
        if (generations == null) {
            List<Generation> values = new ArrayList<>();
            for (Definition definition : DEFINITIONS) {
                values.addAll(definition.generations);
            }
            generations = Collections.unmodifiableList(values);
        }
        return generations;
    }

    public static List<EntryView> clientEntries() {
        prepare();
        List<EntryView> entries = new ArrayList<>();
        for (Definition definition : DEFINITIONS) {
            if (definition.item == null) {
                continue;
            }
            entries.add(new EntryView(definition.item, definition.block, definition.modelPath));
        }
        return Collections.unmodifiableList(entries);
    }

    public static Block legacyBlock(ResourceLocation key) {
        prepare();
        if (key == null || !FarmerFutureDelight.MODID.equals(key.getResourceDomain())) {
            return null;
        }
        for (Definition definition : DEFINITIONS) {
            if (definition.legacyPaths.contains(key.getResourcePath())) {
                return definition.block;
            }
        }
        return null;
    }

    public static Item legacyItem(ResourceLocation key) {
        prepare();
        if (key == null || !FarmerFutureDelight.MODID.equals(key.getResourceDomain())) {
            return null;
        }
        for (Definition definition : DEFINITIONS) {
            if (definition.item != null && definition.legacyPaths.contains(key.getResourcePath())) {
                return definition.item;
            }
        }
        return null;
    }

    public static int color(Block block, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        for (Definition definition : DEFINITIONS) {
            if (definition.block == block) {
                return definition.tintColor;
            }
        }
        return 0xFFFFFF;
    }

    public static int color(Item item, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        for (Definition definition : DEFINITIONS) {
            if (definition.item == item) {
                return definition.tintColor;
            }
        }
        return 0xFFFFFF;
    }

    private static void add(IForgeRegistry<Block> registry, String modid, String sourceMaterial, String rawMaterial,
                            String sourceId, int metadata, int fallbackHarvestLevel,
                            int tintColor, GenerationSpec... generationSpecs) {
        String toggle = modid + ":" + sourceMaterial;
        if (!FFDConfig.isDeepslateCompatEnabled(toggle)) {
            return;
        }
        ResourceLocation sourceKey = new ResourceLocation(sourceId);
        Block source = registry.getValue(sourceKey);
        if (source == null || !sourceKey.equals(source.getRegistryName())) {
            return;
        }
        net.minecraft.block.state.IBlockState sourceState = stateFromMeta(source, metadata);
        if (sourceState == null) {
            return;
        }
        String legacyPath = "deepslate_compat_" + modid + "_" + sourceMaterial + "_ore";
        String path = "deepslate_compat_" + rawMaterial + "_ore";
        ResourceLocation targetKey = new ResourceLocation(FarmerFutureDelight.MODID, path);
        ItemStack sourceStack = new ItemStack(source, 1, metadata);
        Definition existing = findByMaterial(rawMaterial);
        if (existing != null) {
            existing.addSource(modid, sourceMaterial, sourceStack, legacyPath);
            existing.addGenerations(buildGenerations(existing.block, legacyPath, generationSpecs));
            return;
        }
        if (ForgeRegistries.BLOCKS.containsKey(targetKey)) {
            return;
        }
        BlockCompatDeepslateOre block = new BlockCompatDeepslateOre(path, rawMaterial,
                sourceState, sourceStack, fallbackHarvestLevel);
        String modelPath = "mekanism".equals(modid)
                ? legacyPath : MODEL_PATH;
        DEFINITIONS.add(new Definition(modid, sourceMaterial, rawMaterial, block,
                sourceStack, buildGenerations(block, legacyPath, generationSpecs),
                tintColor, modelPath, legacyPath));
    }

    private static net.minecraft.block.state.IBlockState stateFromMeta(Block source, int metadata) {
        try {
            net.minecraft.block.state.IBlockState state = source.getStateFromMeta(metadata);
            if (state == null || state.getBlock() != source
                    || source.getMetaFromState(state) != metadata) {
                return null;
            }
            return state;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static void addCustom(IForgeRegistry<Block> registry) {
        for (FFDCustomDeepslateOres.Entry entry : FFDCustomDeepslateOres.entries()) {
            List<FFDCustomDeepslateOres.Source> sources = entry.resolveSources(registry);
            if (sources.isEmpty()) {
                LOGGER.warn("Skipping custom deepslate ore {} because its normal ore could not be resolved",
                        entry.material());
                continue;
            }
            String material = entry.material();
            String path = "deepslate_compat_" + material + "_ore";
            String sourceKey = "custom:" + material;
            Definition existing = findByMaterial(material);
            if (existing != null) {
                for (FFDCustomDeepslateOres.Source source : sources) {
                    existing.addSource("custom", material, source.stack(), path);
                }
                existing.addGenerations(buildCustomGenerations(
                        existing.block, sourceKey, entry.generation()));
                continue;
            }
            ResourceLocation targetKey = new ResourceLocation(FarmerFutureDelight.MODID, path);
            if (ForgeRegistries.BLOCKS.containsKey(targetKey)) {
                continue;
            }
            FFDCustomDeepslateOres.Source primary = sources.get(0);
            BlockCompatDeepslateOre block = new BlockCompatDeepslateOre(
                    path, material, primary.state(), primary.stack(), primary.harvestLevel());
            entry.bindBlock(block);
            Definition definition = new Definition("custom", material, material, block,
                    primary.stack(), buildCustomGenerations(block, sourceKey, entry.generation()),
                    0xFFFFFF, "custom_deepslate_ore/" + material, path, entry);
            for (int index = 1; index < sources.size(); index++) {
                definition.addSource("custom", material, sources.get(index).stack(), path);
            }
            DEFINITIONS.add(definition);
        }
    }

    private static Definition findByMaterial(String material) {
        for (Definition definition : DEFINITIONS) {
            if (definition.rawMaterial.equals(material)) {
                return definition;
            }
        }
        return null;
    }

    private static List<Generation> buildGenerations(BlockCompatDeepslateOre block,
                                                     String sourceKey,
                                                     GenerationSpec... generationSpecs) {
        long salt = 0x5D000000L ^ (long) sourceKey.hashCode() * 0x9E3779B97F4A7C15L;
        List<Generation> values = new ArrayList<>();
        for (int index = 0; index < generationSpecs.length; index++) {
            GenerationSpec spec = generationSpecs[index];
            FFDConfig.DeepslateGeneration settings = FFDConfig.deepslateCompatGeneration(
                    spec.key, spec.count, spec.size, spec.minY, spec.maxY,
                    spec.trapezoid, spec.discardChance, spec.plateau);
            values.add(new Generation(block.getDefaultState(), settings,
                    salt ^ (long) index * 0xD1B54A32D192ED03L));
        }
        return values;
    }

    private static List<Generation> buildCustomGenerations(
            BlockCompatDeepslateOre block, String sourceKey,
            List<FFDCustomDeepslateOres.GenerationSpec> generationSpecs) {
        long salt = 0x5D000000L ^ (long) sourceKey.hashCode() * 0x9E3779B97F4A7C15L;
        List<Generation> values = new ArrayList<>();
        for (int index = 0; index < generationSpecs.size(); index++) {
            FFDCustomDeepslateOres.GenerationSpec spec = generationSpecs.get(index);
            FFDConfig.DeepslateGeneration settings = new FFDConfig.DeepslateGeneration(
                    spec.count(), spec.size(), spec.minY(), spec.maxY(),
                    spec.trapezoid(), spec.discardChance(), spec.plateau());
            values.add(new Generation(block.getDefaultState(), settings,
                    salt ^ (long) index * 0xD1B54A32D192ED03L));
        }
        return values;
    }

    private static GenerationSpec generation(String key, int count, int size,
                                             int minY, int maxY, boolean trapezoid) {
        return generation(key, count, size, minY, maxY, trapezoid, 0.0F, 0);
    }

    private static GenerationSpec generation(String key, int count, int size,
                                             int minY, int maxY, boolean trapezoid,
                                             float discardChance, int plateau) {
        return new GenerationSpec(key, count, size, minY, maxY, trapezoid,
                discardChance, plateau);
    }

    private static void addDefaultOreNames(Set<String> names, String material) {
        if (material == null || material.isEmpty()) {
            return;
        }
        names.add("ore" + FFDRawOres.capitalize(material));
        if ("aluminium".equals(material) || "aluminum".equals(material)) {
            names.add("oreAluminium");
            names.add("oreAluminum");
        } else if ("mithril".equals(material) || "mythril".equals(material)) {
            names.add("oreMithril");
            names.add("oreMythril");
        } else if ("adamantium".equals(material)) {
            names.add("oreAdamantium");
            names.add("oreAdamantite");
            names.add("oreAdamantine");
        }
    }

    private static final class Definition {
        private final String primaryModid;
        private final String primarySourceMaterial;
        private final String rawMaterial;
        private final BlockCompatDeepslateOre block;
        private final List<Generation> generations;
        private final int tintColor;
        private final Set<String> sourceMaterials = new LinkedHashSet<>();
        private final List<ItemStack> sourceStacks = new ArrayList<>();
        private final Set<String> legacyPaths = new LinkedHashSet<>();
        private String modelPath;
        private final Set<String> registeredOreNames = new LinkedHashSet<>();
        private final FFDCustomDeepslateOres.Entry customEntry;
        private Item item;

        private Definition(String modid, String sourceMaterial, String rawMaterial,
                           BlockCompatDeepslateOre block, ItemStack sourceStack,
                           List<Generation> generations, int tintColor) {
            this(modid, sourceMaterial, rawMaterial, block, sourceStack, generations,
                    tintColor, MODEL_PATH,
                    "deepslate_compat_" + modid + "_" + sourceMaterial + "_ore", null);
        }

        private Definition(String modid, String sourceMaterial, String rawMaterial,
                           BlockCompatDeepslateOre block, ItemStack sourceStack,
                           List<Generation> generations, int tintColor,
                           String modelPath, String legacyPath) {
            this(modid, sourceMaterial, rawMaterial, block, sourceStack, generations,
                    tintColor, modelPath, legacyPath, null);
        }

        private Definition(String modid, String sourceMaterial, String rawMaterial,
                           BlockCompatDeepslateOre block, ItemStack sourceStack,
                           List<Generation> generations, int tintColor,
                           String modelPath, String legacyPath,
                           FFDCustomDeepslateOres.Entry customEntry) {
            this.primaryModid = modid;
            this.primarySourceMaterial = sourceMaterial;
            this.rawMaterial = rawMaterial;
            this.block = block;
            this.generations = new ArrayList<>(generations);
            this.tintColor = tintColor;
            this.modelPath = modelPath;
            this.customEntry = customEntry;
            addSource(modid, sourceMaterial, sourceStack, legacyPath);
        }

        private void addSource(String modid, String sourceMaterial,
                               ItemStack sourceStack, String legacyPath) {
            sourceMaterials.add(sourceMaterial);
            sourceStacks.add(sourceStack.copy());
            legacyPaths.add(legacyPath);
            if (!"mekanism".equals(modid) && !"custom".equals(modid)) {
                modelPath = MODEL_PATH;
            }
        }

        private void addGenerations(List<Generation> values) {
            generations.addAll(values);
        }
    }

    private static final class GenerationSpec {
        private final String key;
        private final int count;
        private final int size;
        private final int minY;
        private final int maxY;
        private final boolean trapezoid;
        private final float discardChance;
        private final int plateau;

        private GenerationSpec(String key, int count, int size, int minY, int maxY,
                               boolean trapezoid, float discardChance, int plateau) {
            this.key = key;
            this.count = count;
            this.size = size;
            this.minY = minY;
            this.maxY = maxY;
            this.trapezoid = trapezoid;
            this.discardChance = discardChance;
            this.plateau = plateau;
        }
    }

    public static final class Generation {
        private final net.minecraft.block.state.IBlockState state;
        private final FFDConfig.DeepslateGeneration settings;
        private final long salt;

        private Generation(net.minecraft.block.state.IBlockState state,
                           FFDConfig.DeepslateGeneration settings, long salt) {
            this.state = state;
            this.settings = settings;
            this.salt = salt;
        }

        public net.minecraft.block.state.IBlockState state() {
            return state;
        }

        public int count() {
            return settings.count();
        }

        public int size() {
            return settings.size();
        }

        public int minY() {
            return settings.minY();
        }

        public int maxY() {
            return settings.maxY();
        }

        public boolean trapezoid() {
            return settings.trapezoid();
        }

        public float discardChance() {
            return settings.discardChance();
        }

        public int plateau() {
            return settings.plateau();
        }

        public long salt() {
            return salt;
        }
    }

    public static final class EntryView {
        private final Item item;
        private final Block block;
        private final String modelPath;

        private EntryView(Item item, Block block, String modelPath) {
            this.item = item;
            this.block = block;
            this.modelPath = modelPath;
        }

        public Item item() {
            return item;
        }

        public Block block() {
            return block;
        }

        public String modelPath() {
            return modelPath;
        }
    }

    static final class RecipePair {
        private final ItemStack source;
        private final ItemStack deep;

        private RecipePair(ItemStack source, ItemStack deep) {
            this.source = source.copy();
            this.deep = deep.copy();
        }

        ItemStack source() {
            return source.copy();
        }

        ItemStack deep() {
            return deep.copy();
        }
    }

}
