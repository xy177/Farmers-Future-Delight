package xy177.farmersfuturedelight.common.registry;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDProfessionWorkstations {
    private static final Logger LOGGER = LogManager.getLogger("FFD Profession Workstations");
    private static final Field CAREER_ID = ObfuscationReflectionHelper.findField(EntityVillager.class, "field_175563_bv");
    private static final String CONFIG_FILE = "profession_workstations.json";
    private static final String DEFAULT_CHEST = "minecraft:chest";
    private static final String BINDING_TAG = "farmers_future_delight:workstation_binding";
    private static final String UNASSIGNED_TAG = "farmers_future_delight:workstation_unassigned";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, List<WorkstationMapping>> BY_PROFESSION = new LinkedHashMap<>();
    private static File configurationDirectory;
    private static int existingProfessionMode = 2;
    private static boolean loaded;

    private FFDProfessionWorkstations() {
    }

    public static synchronized void configure(File directory) throws IOException {
        configurationDirectory = directory;
        File config = new File(directory, CONFIG_FILE);
        File example = new File(directory, "_example_" + CONFIG_FILE);
        if (!example.exists()) {
            try (InputStream input = FFDProfessionWorkstations.class.getResourceAsStream(
                    "/assets/farmers_future_delight/workstations/_example_" + CONFIG_FILE)) {
                if (input == null) throw new IOException("Missing profession workstation example");
                try (FileOutputStream output = new FileOutputStream(example)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
                }
            }
        }
        if (!config.exists()) Files.copy(example.toPath(), config.toPath());
    }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        if (configurationDirectory == null) return;
        File file = new File(configurationDirectory, CONFIG_FILE);
        JsonObject root;
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
            root = new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            LOGGER.error("Could not read profession workstation configuration {}; mappings are disabled", file,
                    exception);
            return;
        }
        boolean migrated = root.has("chestFallbackShare");
        root.remove("chestFallbackShare");
        if (root.has("existingProfessionMode")) {
            try {
                int mode = root.get("existingProfessionMode").getAsBigDecimal().intValueExact();
                if (mode >= 1 && mode <= 3) existingProfessionMode = mode;
                else LOGGER.warn("Ignoring invalid existingProfessionMode {}; expected 1, 2 or 3", mode);
            } catch (RuntimeException exception) {
                LOGGER.warn("Ignoring invalid existingProfessionMode; using mode 2", exception);
            }
        } else {
            root.addProperty("existingProfessionMode", existingProfessionMode);
            migrated = true;
        }
        if (migrated) {
            try {
                Files.write(file.toPath(), GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
            } catch (IOException exception) {
                LOGGER.error("Could not save profession workstation configuration to {}", file, exception);
            }
        }
        JsonArray mappings;
        try {
            mappings = root.getAsJsonArray("mappings");
        } catch (RuntimeException exception) {
            LOGGER.error("Skipping invalid profession mapping list in {}", file, exception);
            return;
        }
        if (mappings == null) return;
        addGeneratedModProfessionDefaults(root, mappings, file);
        for (JsonElement element : mappings) {
            try {
                JsonObject entry = element.getAsJsonObject();
                if (entry.has("enabled") && !entry.get("enabled").getAsBoolean()) continue;
                ResourceLocation professionId = new ResourceLocation(entry.get("profession").getAsString());
                VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(professionId);
                if (profession == null) {
                    LOGGER.warn("Ignoring unknown villager profession {}", professionId);
                    continue;
                }
                String career = entry.has("career") ? entry.get("career").getAsString() : "*";
                if (!"*".equals(career) && !careerNames(profession).contains(career)) {
                    LOGGER.warn("Ignoring unknown career {} for villager profession {}", career, professionId);
                    continue;
                }
                JsonArray workstations = entry.getAsJsonArray("workstations");
                if (workstations == null) continue;
                List<BlockStateMatcher> matchers = new ArrayList<>();
                for (JsonElement workstation : workstations) {
                    try {
                        BlockStateMatcher matcher = BlockStateMatcher.parse(workstation.getAsJsonObject());
                        if (matcher != null) matchers.add(matcher);
                    } catch (RuntimeException exception) {
                        LOGGER.warn("Skipping invalid workstation for {} career {}: {}", professionId, career,
                                workstation, exception);
                    }
                }
                if (!matchers.isEmpty()) {
                    addMapping(new WorkstationMapping(professionId, career, matchers));
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Skipping invalid profession workstation mapping {}", element, exception);
            }
        }
        LOGGER.info("Loaded {} profession workstation mappings with conditional chest fallback", countMappings());
    }

    public static synchronized List<WorkstationMapping> getMappings(ResourceLocation profession,
                                                                      @Nullable String career) {
        load();
        List<WorkstationMapping> mappings = BY_PROFESSION.get(profession);
        if (mappings == null) return Collections.emptyList();
        List<WorkstationMapping> result = new ArrayList<>();
        for (WorkstationMapping mapping : mappings) {
            if (mapping.matchesCareer(career)) result.add(mapping);
        }
        return Collections.unmodifiableList(result);
    }

    @Nullable
    public static String getCareerName(EntityVillager villager) {
        VillagerProfession profession = villager.getProfessionForge();
        if (profession == null) return null;
        try {
            int careerId = CAREER_ID.getInt(villager);
            return careerId <= 0 ? null : profession.getCareer(careerId - 1).getName();
        } catch (IllegalAccessException | RuntimeException exception) {
            LOGGER.warn("Could not read career for villager profession {}", profession.getRegistryName(), exception);
            return null;
        }
    }

    public static synchronized List<WorkstationMapping> getMappings(EntityVillager villager) {
        VillagerProfession profession = villager.getProfessionForge();
        ResourceLocation professionId = profession == null ? null : profession.getRegistryName();
        if (professionId == null) return Collections.emptyList();
        return getMappings(professionId, getCareerName(villager));
    }

    @Nullable
    public static synchronized WorkstationMapping findMapping(EntityVillager villager, IBlockState state) {
        for (WorkstationMapping mapping : getMappings(villager)) {
            if (mapping.matches(state)) return mapping;
        }
        return null;
    }

    public static synchronized List<WorkstationMapping> findMappings(IBlockState state) {
        load();
        List<WorkstationMapping> result = new ArrayList<>();
        for (List<WorkstationMapping> mappings : BY_PROFESSION.values()) {
            for (WorkstationMapping mapping : mappings) {
                if (mapping.matches(state)) result.add(mapping);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Nullable
    public static synchronized WorkstationMapping chooseMapping(IBlockState state, Random random,
                                                                 int villagerCount,
                                                                 int chestFallbackVillagerCount) {
        return chooseMapping(state, random);
    }

    @Nullable
    public static synchronized WorkstationMapping chooseMapping(IBlockState state, Random random) {
        List<WorkstationMapping> eligible = findMappings(state);
        return eligible.isEmpty() ? null : eligible.get(random.nextInt(eligible.size()));
    }

    @Nullable
    public static synchronized WorkstationMapping getOrChooseBinding(EntityVillager villager, BlockPos position,
                                                                       IBlockState state, Random random,
                                                                       int villagerCount,
                                                                       int chestFallbackVillagerCount) {
        return getOrChooseBinding(villager, position, state, random);
    }

    @Nullable
    public static synchronized WorkstationMapping getOrChooseBinding(EntityVillager villager, BlockPos position,
                                                                       IBlockState state, Random random) {
        if (!FFDConfig.modernVillagerSystem || villager.world.isRemote) return null;
        NBTTagCompound entityData = villager.getEntityData();
        if (entityData.hasKey(BINDING_TAG)) {
            if (entityData.hasKey(BINDING_TAG, 10)) {
                NBTTagCompound binding = entityData.getCompoundTag(BINDING_TAG);
                if (binding.getInteger("dimension") == villager.dimension
                        && binding.getInteger("x") == position.getX()
                        && binding.getInteger("y") == position.getY()
                        && binding.getInteger("z") == position.getZ()) {
                    try {
                        ResourceLocation profession = new ResourceLocation(binding.getString("profession"));
                        String career = binding.getString("career");
                        for (WorkstationMapping mapping : getMappings(profession, career)) {
                            if (mapping.matches(state)) return mapping;
                        }
                    } catch (RuntimeException exception) {
                        LOGGER.warn("Discarding invalid persisted villager workstation binding", exception);
                    }
                }
            }
            entityData.removeTag(BINDING_TAG);
        }
        WorkstationMapping selected = chooseMapping(state, random);
        if (selected == null) return null;
        NBTTagCompound binding = new NBTTagCompound();
        binding.setInteger("dimension", villager.dimension);
        binding.setInteger("x", position.getX());
        binding.setInteger("y", position.getY());
        binding.setInteger("z", position.getZ());
        binding.setString("profession", selected.getProfession().toString());
        binding.setString("career", selected.getCareer());
        entityData.setTag(BINDING_TAG, binding);
        return selected;
    }

    public static void markProfessionUnassigned(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        villager.getEntityData().setBoolean(UNASSIGNED_TAG, true);
        villager.getEntityData().removeTag("farmers_future_delight:assigned_profession");
        FFDVillagerProfessionRetention.release(villager);
        villager.getEntityData().removeTag(FFDVillagerRestocking.STATE_TAG);
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                null, "field_70963_i");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                0, "field_175563_bv");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                0, "field_175562_bw");
    }

    public static void onExplicitProfession(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) {
            restoreLegacyState(villager);
            return;
        }
        villager.getEntityData().removeTag(UNASSIGNED_TAG);
        FFDVillagerProfessionRetention.onExplicitProfession(villager);
    }

    public static void setRandomProfession(EntityVillager villager, Random random) {
        net.minecraftforge.fml.common.registry.VillagerRegistry.setRandomProfession(villager, random);
        if (!FFDConfig.modernVillagerSystem) {
            restoreLegacyState(villager);
            return;
        }
        ResourceLocation profession = villager.getProfessionForge().getRegistryName();
        if (villager.getClass() == EntityVillager.class && profession != null
                && !new ResourceLocation("minecraft:nitwit").equals(profession)) markProfessionUnassigned(villager);
    }

    public static void restoreUnassigned(EntityVillager villager, NBTTagCompound compound) {
        if (!FFDConfig.modernVillagerSystem) {
            NBTTagCompound forgeData = compound.getCompoundTag("ForgeData");
            if (forgeData.getBoolean(UNASSIGNED_TAG)) {
                villager.getEntityData().setBoolean(UNASSIGNED_TAG, true);
            }
            restoreLegacyState(villager);
            return;
        }
        FFDVillagerProfessionRetention.restore(villager, compound.getCompoundTag("ForgeData"));
        if (compound.getCompoundTag("ForgeData").getBoolean(UNASSIGNED_TAG)) {
            villager.getEntityData().setBoolean(UNASSIGNED_TAG, true);
        }
    }

    public static boolean isProfessionUnassigned(EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && villager.getEntityData().getBoolean(UNASSIGNED_TAG);
    }

    public static void restoreLegacyState(EntityVillager villager) {
        if (FFDConfig.modernVillagerSystem) return;
        NBTTagCompound data = villager.getEntityData();
        boolean wasUnassigned = data.getBoolean(UNASSIGNED_TAG);
        boolean hadModernTrades = data.hasKey(FFDVillagerRestocking.STATE_TAG, 10);
        data.removeTag(UNASSIGNED_TAG);
        data.removeTag(BINDING_TAG);
        data.removeTag(FFDVillagerProfessionRetention.STATE_TAG);
        data.removeTag("farmers_future_delight:assigned_profession");
        data.removeTag(FFDVillagerRestocking.STATE_TAG);
        data.removeTag(FFDTradeConfiguration.getAppliedTag());
        if (hadModernTrades) {
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                    null, "field_70963_i");
        }
        if (!wasUnassigned || villager.getProfessionForge() == null || getCareerName(villager) != null) return;
        int career = villager.getProfessionForge().getRandomCareer(villager.getRNG());
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                career + 1, "field_175563_bv");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                0, "field_175562_bw");
    }

    public static void assignProfession(EntityVillager villager, WorkstationMapping mapping,
                                        Random random) {
        if (!FFDConfig.modernVillagerSystem) return;
        VillagerProfession profession = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(
                mapping.getProfession());
        if (profession == null) return;
        villager.setProfession(profession);
        int careerId = "*".equals(mapping.getCareer())
                ? profession.getRandomCareer(random) + 1 : mapping.getCareerId() + 1;
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                careerId, "field_175563_bv");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                0, "field_175562_bw");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                null, "field_70963_i");
        if ("minecraft".equals(mapping.getProfession().getResourceDomain())
                || FFDVillagerTypes.MASON.equals(mapping.getProfession())) {
            villager.getEntityData().removeTag(UNASSIGNED_TAG);
            FFDVillagerRestocking.enable(villager);
        }
        villager.getEntityData().removeTag(UNASSIGNED_TAG);
        FFDVillagerProfessionRetention.release(villager);
        FFDVillagerHomes.wake(villager);
        FFDVillagerTypes.sync(villager);
    }

    public static void completeExistingBinding(EntityVillager villager, WorkstationMapping mapping) {
        if (!FFDConfig.modernVillagerSystem) return;
        if (getCareerName(villager) == null && !"*".equals(mapping.getCareer())) {
            ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                    mapping.getCareerId() + 1, "field_175563_bv");
        }
        if (getExistingProfessionMode() != 1) FFDVillagerProfessionRetention.release(villager);
    }

    public static int selectBoundCareer(VillagerProfession profession, Random random, EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return profession.getRandomCareer(random);
        NBTTagCompound binding = villager.getEntityData().getCompoundTag(BINDING_TAG);
        if (String.valueOf(profession.getRegistryName()).equals(binding.getString("profession"))
                && getCareerName(villager) != null) {
            try {
                return CAREER_ID.getInt(villager) - 1;
            } catch (IllegalAccessException exception) {
                LOGGER.warn("Could not preserve bound villager career", exception);
            }
        }
        return profession.getRandomCareer(random);
    }

    public static void resetUntradedAssignedProfession(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        FFDVillagerProfessionRetention.initialize(villager);
        if (FFDVillagerProfessionRetention.isProtected(villager)) return;
        NBTTagCompound data = villager.getEntityData();
        String assigned = data.getString("farmers_future_delight:assigned_profession");
        String profession = String.valueOf(villager.getProfessionForge().getRegistryName());
        if (assigned.isEmpty() || !(assigned.equals(profession + "/" + getCareerName(villager))
                || assigned.equals(profession + "/null"))
                || "minecraft:nitwit".equals(profession)
                || data.getBoolean("farmers_future_delight:has_traded")
                || data.getBoolean("farmers_future_delight:legacy_trade_history_unknown")
                || FFDVillagerRestocking.getExperience(villager) != 0) return;
        int careerLevel = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager,
                "field_175562_bw");
        if (careerLevel > 1) return;
        net.minecraft.village.MerchantRecipeList recipes = ObfuscationReflectionHelper.getPrivateValue(
                EntityVillager.class, villager, "field_70963_i");
        if (recipes != null) {
            for (net.minecraft.village.MerchantRecipe recipe : recipes) {
                if (recipe.getToolUses() != 0) return;
            }
        }
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, null, "field_70963_i");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, 0, "field_175563_bv");
        ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager, 0, "field_175562_bw");
        data.removeTag("farmers_future_delight:modern_trades");
        data.removeTag(FFDTradeConfiguration.getAppliedTag());
        data.removeTag("farmers_future_delight:assigned_profession");
        markProfessionUnassigned(villager);
        FFDVillagerHomes.wake(villager);
        FFDVillagerTypes.sync(villager);
    }

    public static void rememberBinding(EntityVillager villager, BlockPos position,
                                       WorkstationMapping mapping) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound binding = new NBTTagCompound();
        binding.setInteger("dimension", villager.dimension);
        binding.setInteger("x", position.getX());
        binding.setInteger("y", position.getY());
        binding.setInteger("z", position.getZ());
        binding.setString("profession", mapping.getProfession().toString());
        binding.setString("career", mapping.getCareer());
        villager.getEntityData().setTag(BINDING_TAG, binding);
    }

    public static void clearBinding(EntityVillager villager) {
        villager.getEntityData().removeTag(BINDING_TAG);
    }

    public static synchronized double getChestFallbackShare() {
        load();
        return 1.0D;
    }

    public static synchronized int getExistingProfessionMode() {
        load();
        return existingProfessionMode;
    }

    private static int countMappings() {
        int count = 0;
        for (List<WorkstationMapping> mappings : BY_PROFESSION.values()) count += mappings.size();
        return count;
    }

    private static void addMapping(WorkstationMapping mapping) {
        List<WorkstationMapping> mappings = BY_PROFESSION.computeIfAbsent(mapping.profession,
                key -> new ArrayList<>());
        for (int index = 0; index < mappings.size(); index++) {
            WorkstationMapping existing = mappings.get(index);
            if (existing.career.equals(mapping.career)) {
                List<BlockStateMatcher> combined = new ArrayList<>(existing.workstations);
                for (BlockStateMatcher matcher : mapping.workstations) {
                    if (!combined.contains(matcher)) combined.add(matcher);
                }
                mappings.set(index, new WorkstationMapping(mapping.profession, mapping.career, combined));
                return;
            }
        }
        mappings.add(mapping);
    }

    private static void addGeneratedModProfessionDefaults(JsonObject root, JsonArray mappings, File file) {
        Map<String, JsonObject> configured = new LinkedHashMap<>();
        for (JsonElement element : mappings) {
            try {
                JsonObject entry = element.getAsJsonObject();
                String profession = entry.get("profession").getAsString();
                String career = entry.has("career") ? entry.get("career").getAsString() : "*";
                configured.put(mappingKey(profession, career), entry);
            } catch (RuntimeException ignored) {
            }
        }
        boolean changed = false;
        Collection<VillagerProfession> professions = ForgeRegistries.VILLAGER_PROFESSIONS.getValuesCollection();
        for (VillagerProfession profession : professions) {
            ResourceLocation id = profession.getRegistryName();
            if (id == null || "minecraft".equals(id.getResourceDomain())) continue;
            List<String> careers = careerNames(profession);
            if (careers.isEmpty()) careers = Collections.singletonList("*");
            for (String career : careers) {
                if (configured.containsKey(mappingKey(id.toString(), career))
                        || configured.containsKey(mappingKey(id.toString(), "*"))) continue;
                JsonObject entry = new JsonObject();
                entry.addProperty("profession", id.toString());
                if (!"*".equals(career)) entry.addProperty("career", career);
                entry.addProperty("enabled", true);
                boolean mason = FFDVillagerTypes.MASON.equals(id);
                entry.addProperty("generatedFallback", !mason);
                JsonArray workstations = new JsonArray();
                JsonObject chest = new JsonObject();
                chest.addProperty("block", mason ? FarmerFutureDelight.MODID + ":stonecutter" : DEFAULT_CHEST);
                workstations.add(chest);
                entry.add("workstations", workstations);
                mappings.add(entry);
                configured.put(mappingKey(id.toString(), career), entry);
                changed = true;
            }
        }
        if (!changed) return;
        root.add("mappings", mappings);
        try {
            Files.write(file.toPath(), GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            LOGGER.error("Could not save generated mod profession workstation defaults to {}", file, exception);
        }
    }

    public static List<String> careerNames(VillagerProfession profession) {
        List<String> careers = new ArrayList<>();
        VillagerCareer first;
        try {
            first = profession.getCareer(0);
        } catch (RuntimeException exception) {
            return careers;
        }
        for (int index = 0; index < 1024; index++) {
            VillagerCareer career;
            try {
                career = profession.getCareer(index);
            } catch (RuntimeException exception) {
                break;
            }
            if (index > 0 && career == first) break;
            careers.add(career.getName());
        }
        return careers;
    }

    private static String mappingKey(String profession, String career) {
        return profession + "/" + career;
    }

    public static final class WorkstationMapping {
        private final ResourceLocation profession;
        private final String career;
        private final List<BlockStateMatcher> workstations;

        private WorkstationMapping(ResourceLocation profession, String career,
                                   List<BlockStateMatcher> workstations) {
            this.profession = profession;
            this.career = career;
            this.workstations = Collections.unmodifiableList(new ArrayList<>(workstations));
        }

        public ResourceLocation getProfession() {
            return profession;
        }

        public String getCareer() {
            return career;
        }

        public List<IBlockState> getMatchingStates() {
            List<IBlockState> result = new ArrayList<>();
            for (BlockStateMatcher matcher : workstations) {
                for (IBlockState state : matcher.block.getBlockState().getValidStates()) {
                    if (matcher.matches(state) && !result.contains(state)) result.add(state);
                }
            }
            return Collections.unmodifiableList(result);
        }

        public int getCareerId() {
            VillagerProfession registered = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(profession);
            if (registered == null || "*".equals(career)) return 0;
            List<String> names = careerNames(registered);
            return names.indexOf(career);
        }

        public boolean matchesCareer(@Nullable String candidate) {
            return candidate == null || "*".equals(career) || career.equals(candidate);
        }

        public boolean matches(IBlockState state) {
            for (BlockStateMatcher matcher : workstations) {
                if (matcher.matches(state)) return true;
            }
            return false;
        }

        public boolean usesOrdinaryChestFallback() {
            if (workstations.isEmpty()) return false;
            for (BlockStateMatcher matcher : workstations) {
                if (!DEFAULT_CHEST.equals(matcher.block.getRegistryName().toString())) return false;
            }
            return true;
        }
    }

    private static final class BlockStateMatcher {
        private final Block block;
        @Nullable
        private final Integer metadata;
        private final Map<String, String> properties;

        private BlockStateMatcher(Block block, @Nullable Integer metadata, Map<String, String> properties) {
            this.block = block;
            this.metadata = metadata;
            this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
        }

        @Nullable
        private static BlockStateMatcher parse(JsonObject entry) {
            ResourceLocation id = new ResourceLocation(entry.get("block").getAsString());
            Block block = ForgeRegistries.BLOCKS.containsKey(id) ? ForgeRegistries.BLOCKS.getValue(id) : null;
            if (block == null && FarmerFutureDelight.MODID.equals(id.getResourceDomain())) {
                net.minecraft.item.Item local = FFDItems.localBlockItem(id);
                if (local instanceof net.minecraft.item.ItemBlock) {
                    block = FFDItems.effectiveBlock(((net.minecraft.item.ItemBlock) local).getBlock());
                }
            }
            if (block == null || block.getRegistryName() == null) {
                LOGGER.warn("Ignoring unknown workstation block {}", id);
                return null;
            }
            Integer metadata = entry.has("meta") ? entry.get("meta").getAsInt() : null;
            if (metadata != null && (metadata < 0 || metadata > 15)) {
                throw new IllegalArgumentException("Metadata must be between 0 and 15");
            }
            Map<String, String> properties = new LinkedHashMap<>();
            IBlockState candidate = metadata == null ? block.getDefaultState() : block.getStateFromMeta(metadata);
            if (entry.has("properties")) {
                JsonObject configuredProperties = entry.getAsJsonObject("properties");
                for (Map.Entry<String, JsonElement> propertyEntry : configuredProperties.entrySet()) {
                    String name = propertyEntry.getKey();
                    String value = propertyEntry.getValue().getAsString();
                    IProperty<?> property = findProperty(candidate, name);
                    if (property == null) throw new IllegalArgumentException("Unknown block state property " + name);
                    candidate = withProperty(candidate, property, value);
                    properties.put(name, value);
                }
            }
            if (metadata != null && block.getMetaFromState(candidate) != metadata) {
                throw new IllegalArgumentException("Metadata and state properties conflict");
            }
            return new BlockStateMatcher(block, metadata, properties);
        }

        private boolean matches(IBlockState state) {
            if (state.getBlock() != block) return false;
            if (metadata != null && block.getMetaFromState(state) != metadata) return false;
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                IProperty<?> property = findProperty(state, entry.getKey());
                if (property == null || !entry.getValue().equals(state.getValue(property).toString())) return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof BlockStateMatcher)) return false;
            BlockStateMatcher matcher = (BlockStateMatcher) other;
            return block == matcher.block && java.util.Objects.equals(metadata, matcher.metadata)
                    && properties.equals(matcher.properties);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(block, metadata, properties);
        }

        @Nullable
        private static IProperty<?> findProperty(IBlockState state, String name) {
            for (IProperty<?> property : state.getPropertyKeys()) {
                if (property.getName().equals(name)) return property;
            }
            return null;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static IBlockState withProperty(IBlockState state, IProperty property, String value) {
            Optional parsed = property.parseValue(value);
            if (!parsed.isPresent()) throw new IllegalArgumentException("Invalid value " + value
                    + " for block state property " + property.getName());
            return state.withProperty(property, (Comparable) parsed.get());
        }
    }
}
