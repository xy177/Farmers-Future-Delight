package xy177.farmersfuturedelight.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandReload;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.world.biome.FFDBiomeCompatibility;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDBlockLootTables {
    private static final ThreadLocal<BlockLootContext> ACTIVE_CONTEXT = new ThreadLocal<>();
    private static final Set<ResourceLocation> REGISTERED_TABLES = new HashSet<>();
    private static final Map<ResourceLocation, Boolean> BUILTIN_RESOURCE_CACHE = new HashMap<>();
    private static final Map<String, Boolean> WORLD_RESOURCE_CACHE = new HashMap<>();

    private FFDBlockLootTables() {
    }

    public static void registerConditions() {
        registerCondition(new BlockStateProperty.Serializer());
        registerCondition(new MatchTool.Serializer());
        registerCondition(new LocationCheck.Serializer());
    }

    private static void registerCondition(LootCondition.Serializer<?> serializer) {
        try {
            LootConditionManager.getSerializerForName(serializer.getLootTableLocation());
        } catch (IllegalArgumentException exception) {
            LootConditionManager.registerCondition(serializer);
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (event.getCommand() instanceof CommandReload
                && event.getParameters().length == 0) {
            WORLD_RESOURCE_CACHE.clear();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof WorldServer)) {
            return;
        }
        String prefix = ((WorldServer) event.getWorld()).getSaveHandler()
                .getWorldDirectory().getAbsolutePath() + '|';
        WORLD_RESOURCE_CACHE.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockDrops(HarvestDropsEvent event) {
        if (event.getWorld().isRemote || !(event.getWorld() instanceof WorldServer)) {
            return;
        }
        WorldServer world = (WorldServer) event.getWorld();
        EntityPlayer player = event.getHarvester();
        ItemStack tool = player == null ? ItemStack.EMPTY : player.getHeldItemMainhand().copy();
        BlockPos pos = event.getPos();
        List<ItemStack> drops = generateLoot(world, event.getState(), tool, player,
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
        if (drops == null) {
            return;
        }
        event.getDrops().clear();
        event.getDrops().addAll(drops);
    }

    public static List<ItemStack> generateLoot(WorldServer world, IBlockState state,
                                                ItemStack tool, EntityPlayer player) {
        return generateLoot(world, state, tool, player, null);
    }

    public static List<ItemStack> generateLoot(WorldServer world, IBlockState state,
                                                ItemStack tool, EntityPlayer player,
                                                Vec3d origin) {
        ResourceLocation tableId = tableId(state.getBlock());
        if (tableId == null) {
            return null;
        }
        return generateTableLoot(world, tableId, state, tool, player, origin);
    }

    public static List<ItemStack> generateTableLoot(WorldServer world, ResourceLocation tableId,
                                                     ItemStack tool, EntityPlayer player,
                                                     Vec3d origin) {
        return generateTableLoot(world, tableId, null, tool, player, origin);
    }

    private static List<ItemStack> generateTableLoot(WorldServer world, ResourceLocation tableId,
                                                      IBlockState state, ItemStack tool,
                                                      EntityPlayer player, Vec3d origin) {
        registerTable(tableId);
        if (!hasResource(world, tableId)) {
            return null;
        }
        LootTable table = world.getLootTableManager().getLootTableFromLocation(tableId);
        if (table == LootTable.EMPTY_LOOT_TABLE) {
            return null;
        }
        BlockLootContext previous = ACTIVE_CONTEXT.get();
        ACTIVE_CONTEXT.set(new BlockLootContext(world, state, tool.copy(), origin));
        try {
            LootContext.Builder builder = new LootContext.Builder(world);
            if (player != null) {
                builder.withLootedEntity(player).withPlayer(player).withLuck(player.getLuck());
            }
            return table.generateLootForPools(world.rand, builder.build());
        } finally {
            if (previous == null) {
                ACTIVE_CONTEXT.remove();
            } else {
                ACTIVE_CONTEXT.set(previous);
            }
        }
    }

    public static boolean hasLootTable(World world, IBlockState state) {
        if (!(world instanceof WorldServer)) {
            return false;
        }
        WorldServer serverWorld = (WorldServer) world;
        ResourceLocation tableId = tableId(state.getBlock());
        if (tableId == null) {
            return false;
        }
        registerTable(tableId);
        if (!hasResource(serverWorld, tableId)) {
            return false;
        }
        return serverWorld.getLootTableManager().getLootTableFromLocation(tableId)
                != LootTable.EMPTY_LOOT_TABLE;
    }

    private static ResourceLocation tableId(Block block) {
        ResourceLocation blockId = block.getRegistryName();
        if (blockId == null || blockId.getResourcePath().contains("..")) {
            return null;
        }
        return new ResourceLocation(blockId.getResourceDomain(), "blocks/" + blockId.getResourcePath());
    }

    private static boolean hasResource(WorldServer world, ResourceLocation tableId) {
        String path = tableId.getResourcePath();
        if (path.contains("..") || path.indexOf('.') >= 0) {
            return false;
        }
        String resourcePath = "/assets/" + tableId.getResourceDomain() + "/loot_tables/"
                + path + ".json";
        Boolean builtIn = BUILTIN_RESOURCE_CACHE.get(tableId);
        if (builtIn == null) {
            builtIn = LootTable.class.getResource(resourcePath) != null;
            BUILTIN_RESOURCE_CACHE.put(tableId, builtIn);
        }
        if (builtIn) {
            return true;
        }
        String worldKey = world.getSaveHandler().getWorldDirectory().getAbsolutePath()
                + '|' + tableId;
        Boolean worldTable = WORLD_RESOURCE_CACHE.get(worldKey);
        if (worldTable != null) {
            return worldTable;
        }
        File root = new File(new File(world.getSaveHandler().getWorldDirectory(), "data"),
                "loot_tables");
        worldTable = new File(new File(root, tableId.getResourceDomain()), path + ".json").isFile();
        WORLD_RESOURCE_CACHE.put(worldKey, worldTable);
        return worldTable;
    }

    public static synchronized void registerTable(ResourceLocation tableId) {
        if (REGISTERED_TABLES.add(tableId) && !LootTableList.getAll().contains(tableId)) {
            LootTableList.register(tableId);
        }
    }

    private static BlockLootContext context() {
        return ACTIVE_CONTEXT.get();
    }

    private static final class BlockLootContext {
        private final WorldServer world;
        private final IBlockState state;
        private final ItemStack tool;
        private final Vec3d origin;

        private BlockLootContext(WorldServer world, IBlockState state, ItemStack tool,
                                 Vec3d origin) {
            this.world = world;
            this.state = state;
            this.tool = tool;
            this.origin = origin;
        }
    }

    private static final class BlockStateProperty implements LootCondition {
        private final ResourceLocation blockId;
        private final Map<String, String> properties;

        private BlockStateProperty(ResourceLocation blockId, Map<String, String> properties) {
            this.blockId = blockId;
            this.properties = properties;
        }

        @Override
        public boolean testCondition(Random random, LootContext lootContext) {
            BlockLootContext context = FFDBlockLootTables.context();
            if (context == null || context.state == null
                    || !blockId.equals(context.state.getBlock().getRegistryName())) {
                return false;
            }
            for (Map.Entry<String, String> propertyValue : properties.entrySet()) {
                IProperty<?> property = findProperty(context.state.getBlock(), propertyValue.getKey());
                if (property == null || !propertyValue.getValue().equals(propertyName(context.state, property))) {
                    return false;
                }
            }
            return true;
        }

        private static IProperty<?> findProperty(Block block, String name) {
            for (IProperty<?> property : block.getBlockState().getProperties()) {
                if (name.equals(property.getName())) {
                    return property;
                }
            }
            return null;
        }

        private static <T extends Comparable<T>> String propertyName(IBlockState state,
                                                                      IProperty<T> property) {
            return property.getName(state.getValue(property));
        }

        private static final class Serializer extends LootCondition.Serializer<BlockStateProperty> {
            private Serializer() {
                super(new ResourceLocation("minecraft", "block_state_property"),
                        BlockStateProperty.class);
            }

            @Override
            public void serialize(JsonObject object, BlockStateProperty condition,
                                  JsonSerializationContext context) {
                object.addProperty("block", condition.blockId.toString());
                JsonObject properties = new JsonObject();
                for (Map.Entry<String, String> entry : condition.properties.entrySet()) {
                    properties.addProperty(entry.getKey(), entry.getValue());
                }
                object.add("properties", properties);
            }

            @Override
            public BlockStateProperty deserialize(JsonObject object,
                                                  JsonDeserializationContext context) {
                ResourceLocation blockId = new ResourceLocation(JsonUtils.getString(object, "block"));
                Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                if (block == null) {
                    throw new com.google.gson.JsonSyntaxException("Unknown block " + blockId);
                }
                Map<String, String> properties = new HashMap<>();
                if (object.has("properties")) {
                    JsonObject propertyObject = JsonUtils.getJsonObject(object, "properties");
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : propertyObject.entrySet()) {
                        IProperty<?> property = BlockStateProperty.findProperty(block, entry.getKey());
                        String value = entry.getValue().getAsString();
                        if (property == null || !hasPropertyValue(property, value)) {
                            throw new com.google.gson.JsonSyntaxException(
                                    "Invalid property " + entry.getKey() + "=" + value + " for " + blockId);
                        }
                        properties.put(entry.getKey(), value);
                    }
                }
                return new BlockStateProperty(blockId, properties);
            }

            private static <T extends Comparable<T>> boolean hasPropertyValue(IProperty<T> property,
                                                                               String value) {
                return property.parseValue(value).isPresent();
            }
        }
    }

    private static final class MatchTool implements LootCondition {
        private final List<ResourceLocation> itemIds;
        private final Integer metadata;
        private final List<EnchantmentRequirement> enchantments;

        private MatchTool(List<ResourceLocation> itemIds, Integer metadata,
                          List<EnchantmentRequirement> enchantments) {
            this.itemIds = itemIds;
            this.metadata = metadata;
            this.enchantments = enchantments;
        }

        @Override
        public boolean testCondition(Random random, LootContext lootContext) {
            BlockLootContext context = FFDBlockLootTables.context();
            if (context == null || context.tool.isEmpty()) {
                return false;
            }
            Item item = context.tool.getItem();
            ResourceLocation itemId = item.getRegistryName();
            if (!itemIds.isEmpty() && (itemId == null || !itemIds.contains(itemId))) {
                return false;
            }
            if (metadata != null && context.tool.getMetadata() != metadata) {
                return false;
            }
            for (EnchantmentRequirement requirement : enchantments) {
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(requirement.id);
                if (enchantment == null) {
                    return false;
                }
                int level = EnchantmentHelper.getEnchantmentLevel(enchantment, context.tool);
                if (level < requirement.minimum || level > requirement.maximum) {
                    return false;
                }
            }
            return true;
        }

        private static final class Serializer extends LootCondition.Serializer<MatchTool> {
            private Serializer() {
                super(new ResourceLocation("minecraft", "match_tool"), MatchTool.class);
            }

            @Override
            public void serialize(JsonObject object, MatchTool condition,
                                  JsonSerializationContext context) {
                JsonObject predicate = new JsonObject();
                JsonArray items = new JsonArray();
                for (ResourceLocation itemId : condition.itemIds) {
                    items.add(itemId.toString());
                }
                if (items.size() > 0) {
                    predicate.add("items", items);
                }
                if (condition.metadata != null) {
                    predicate.addProperty("data", condition.metadata);
                }
                if (!condition.enchantments.isEmpty()) {
                    JsonArray enchantments = new JsonArray();
                    for (EnchantmentRequirement requirement : condition.enchantments) {
                        JsonObject entry = new JsonObject();
                        entry.addProperty("enchantment", requirement.id.toString());
                        JsonObject levels = new JsonObject();
                        levels.addProperty("min", requirement.minimum);
                        levels.addProperty("max", requirement.maximum);
                        entry.add("levels", levels);
                        enchantments.add(entry);
                    }
                    predicate.add("enchantments", enchantments);
                }
                object.add("predicate", predicate);
            }

            @Override
            public MatchTool deserialize(JsonObject object, JsonDeserializationContext context) {
                JsonObject predicate = JsonUtils.getJsonObject(object, "predicate");
                for (Map.Entry<String, com.google.gson.JsonElement> entry : predicate.entrySet()) {
                    if (!"item".equals(entry.getKey()) && !"items".equals(entry.getKey())
                            && !"data".equals(entry.getKey()) && !"enchantments".equals(entry.getKey())) {
                        throw new com.google.gson.JsonSyntaxException(
                                "Unsupported match_tool predicate " + entry.getKey());
                    }
                }
                List<ResourceLocation> itemIds = new ArrayList<>();
                if (predicate.has("item")) {
                    itemIds.add(new ResourceLocation(JsonUtils.getString(predicate, "item")));
                }
                if (predicate.has("items")) {
                    for (com.google.gson.JsonElement entry : JsonUtils.getJsonArray(predicate, "items")) {
                        itemIds.add(new ResourceLocation(entry.getAsString()));
                    }
                }
                Integer metadata = predicate.has("data")
                        ? JsonUtils.getInt(predicate, "data") : null;
                List<EnchantmentRequirement> enchantments = new ArrayList<>();
                if (predicate.has("enchantments")) {
                    for (com.google.gson.JsonElement element : JsonUtils.getJsonArray(predicate,
                            "enchantments")) {
                        JsonObject entry = JsonUtils.getJsonObject(element, "enchantment");
                        ResourceLocation id = new ResourceLocation(JsonUtils.getString(entry, "enchantment"));
                        int minimum = 1;
                        int maximum = Integer.MAX_VALUE;
                        if (entry.has("levels")) {
                            JsonObject levels = JsonUtils.getJsonObject(entry, "levels");
                            if (levels.has("min")) minimum = JsonUtils.getInt(levels, "min");
                            if (levels.has("max")) maximum = JsonUtils.getInt(levels, "max");
                        }
                        enchantments.add(new EnchantmentRequirement(id, minimum, maximum));
                    }
                }
                return new MatchTool(itemIds, metadata, enchantments);
            }
        }
    }

    private static final class LocationCheck implements LootCondition {
        private final Set<ResourceLocation> biomes;

        private LocationCheck(Set<ResourceLocation> biomes) {
            this.biomes = biomes;
        }

        @Override
        public boolean testCondition(Random random, LootContext lootContext) {
            BlockLootContext context = FFDBlockLootTables.context();
            if (context == null || context.origin == null) {
                return false;
            }
            if (biomes.isEmpty()) {
                return true;
            }
            BlockPos position = new BlockPos(context.origin);
            for (ResourceLocation expected : biomes) {
                if (FFDBiomeCompatibility.matches(context.world, position, expected)) {
                    return true;
                }
            }
            return false;
        }

        private static final class Serializer extends LootCondition.Serializer<LocationCheck> {
            private Serializer() {
                super(new ResourceLocation("minecraft", "location_check"), LocationCheck.class);
            }

            @Override
            public void serialize(JsonObject object, LocationCheck condition,
                                  JsonSerializationContext context) {
                JsonObject predicate = new JsonObject();
                JsonArray biomes = new JsonArray();
                for (ResourceLocation biome : condition.biomes) {
                    biomes.add(biome.toString());
                }
                predicate.add("biomes", biomes);
                object.add("predicate", predicate);
            }

            @Override
            public LocationCheck deserialize(JsonObject object,
                                             JsonDeserializationContext context) {
                JsonObject predicate = object.has("predicate")
                        ? JsonUtils.getJsonObject(object, "predicate") : new JsonObject();
                Set<ResourceLocation> biomes = new HashSet<>();
                if (predicate.has("biome")) {
                    biomes.add(new ResourceLocation(JsonUtils.getString(predicate, "biome")));
                }
                if (predicate.has("biomes")) {
                    for (com.google.gson.JsonElement biome
                            : JsonUtils.getJsonArray(predicate, "biomes")) {
                        biomes.add(new ResourceLocation(biome.getAsString()));
                    }
                }
                for (Map.Entry<String, com.google.gson.JsonElement> entry : predicate.entrySet()) {
                    if (!"biome".equals(entry.getKey()) && !"biomes".equals(entry.getKey())) {
                        throw new com.google.gson.JsonSyntaxException(
                                "Unsupported location_check predicate " + entry.getKey());
                    }
                }
                return new LocationCheck(biomes);
            }
        }
    }

    private static final class EnchantmentRequirement {
        private final ResourceLocation id;
        private final int minimum;
        private final int maximum;

        private EnchantmentRequirement(ResourceLocation id, int minimum, int maximum) {
            this.id = id;
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }
}
