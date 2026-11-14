package xy177.farmersfuturedelight.client.model;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ModernSpawnEggModel extends BakedModelWrapper<IBakedModel> {
    private static final Map<ResourceLocation, ModelResourceLocation> MODELS = new HashMap<>();
    private static final Map<String, ResourceLocation> DEFINITIONS = new HashMap<>();
    private static final OptionalLoader LOADER = new OptionalLoader();
    private final ItemOverrideList overrides;

    static {
        registerModels("minecraft", "bat", "blaze", "cave_spider", "chicken", "cow", "creeper",
                "donkey", "elder_guardian", "enderman", "endermite", "ghast", "guardian", "horse",
                "husk", "llama", "magma_cube", "mooshroom", "mule", "ocelot", "parrot", "pig",
                "polar_bear", "rabbit", "sheep", "shulker", "silverfish", "skeleton", "skeleton_horse",
                "slime", "spider", "squid", "stray", "vex", "villager", "witch", "wither_skeleton",
                "wolf", "zombie", "zombie_horse", "zombie_villager");
        registerModel("minecraft", "evocation_illager", "evoker");
        registerModel("minecraft", "vindication_illager", "vindicator");
        registerModel("minecraft", "zombie_pigman", "zombified_piglin");
        registerModels(FarmerFutureDelight.MODID, "axolotl", "bee", "cat", "cod", "dolphin", "drowned",
                "fox", "glow_squid", "goat", "panda", "phantom", "pillager", "pufferfish", "ravager",
                "salmon", "trader_llama", "tropical_fish", "turtle", "wandering_trader", "hoglin", "zoglin", "strider", "piglin", "piglin_brute");
    }

    private static void registerModels(String domain, String... names) {
        for (String name : names) {
            registerModel(domain, name, name);
        }
    }

    private static void registerModel(String domain, String entityName, String modelName) {
        String modelPath = "optional_spawn_egg/" + modelName;
        MODELS.put(new ResourceLocation(domain, entityName), new ModelResourceLocation(
                FarmerFutureDelight.MODID + ":" + modelPath, "inventory"));
        DEFINITIONS.put(modelPath, new ResourceLocation(FarmerFutureDelight.MODID,
                "models/item/" + modelName + "_spawn_egg.json"));
    }

    private ModernSpawnEggModel(IBakedModel original, Map<ResourceLocation, IBakedModel> models) {
        super(original);
        overrides = new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                              World world, EntityLivingBase entity) {
                IBakedModel selected = models.get(ItemMonsterPlacer.getNamedIdFrom(stack));
                return selected != null ? selected
                        : original.getOverrides().handleItemState(original, stack, world, entity);
            }
        };
    }

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        for (ResourceLocation entity : ForgeRegistries.ENTITIES.getKeys()) {
            if (MODELS.containsKey(entity)) continue;
            String entityPath = entity.getResourceDomain() + "/" + entity.getResourcePath();
            String modelPath = "optional_spawn_egg/modded/" + entityPath;
            MODELS.put(entity, new ModelResourceLocation(FarmerFutureDelight.MODID + ":" + modelPath,
                    "inventory"));
            DEFINITIONS.put(modelPath, new ResourceLocation(FarmerFutureDelight.MODID,
                    "models/item/mod_spawn_eggs/" + entityPath + ".json"));
        }
        ModelLoaderRegistry.registerLoader(LOADER);
        ModelBakery.registerItemVariants(Items.SPAWN_EGG, new ResourceLocation("minecraft", "spawn_egg"));
        for (ModelResourceLocation model : MODELS.values()) {
            ModelBakery.registerItemVariants(Items.SPAWN_EGG,
                    new ResourceLocation(model.getResourceDomain(), model.getResourcePath()));
        }
    }

    @SubscribeEvent
    public static void bake(ModelBakeEvent event) {
        ModelResourceLocation location = new ModelResourceLocation("minecraft:spawn_egg", "inventory");
        IBakedModel original = event.getModelRegistry().getObject(location);
        if (original == null) return;
        Map<ResourceLocation, IBakedModel> models = new HashMap<>();
        for (Map.Entry<ResourceLocation, ModelResourceLocation> entry : MODELS.entrySet()) {
            if (!LOADER.available.contains(entry.getValue().getResourcePath())) continue;
            IBakedModel baked = event.getModelRegistry().getObject(entry.getValue());
            if (baked != null && baked != event.getModelManager().getMissingModel()) {
                models.put(entry.getKey(), new Untinted(baked));
            }
        }
        event.getModelRegistry().putObject(location, new ModernSpawnEggModel(original, models));
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    private static final class OptionalLoader implements ICustomModelLoader {
        private static final String PREFIX = "models/item/";
        private final Set<String> available = new HashSet<>();
        private IResourceManager resources;

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            resources = resourceManager;
            available.clear();
        }

        @Override
        public boolean accepts(ResourceLocation location) {
            return FarmerFutureDelight.MODID.equals(location.getResourceDomain())
                    && location.getResourcePath().startsWith(PREFIX)
                    && DEFINITIONS.containsKey(location.getResourcePath().substring(PREFIX.length()));
        }

        @Override
        public IModel loadModel(ResourceLocation location) throws Exception {
            String modelPath = location.getResourcePath().substring(PREFIX.length());
            ResourceLocation definition = DEFINITIONS.get(modelPath);
            try (IResource resource = resources.getResource(definition)) {
                available.add(modelPath);
            } catch (FileNotFoundException absent) {
                return ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:item/spawn_egg"));
            }
            String definitionPath = definition.getResourcePath();
            return ModelLoaderRegistry.getModel(new ResourceLocation(definition.getResourceDomain(),
                    definitionPath.substring("models/".length(), definitionPath.length() - ".json".length())));
        }
    }

    private static final class Untinted extends BakedModelWrapper<IBakedModel> {
        private final Map<EnumFacing, List<BakedQuad>> quads = new HashMap<>();

        private Untinted(IBakedModel original) {
            super(original);
            quads.put(null, withoutTint(original.getQuads(null, null, 0L)));
            for (EnumFacing side : EnumFacing.values()) {
                quads.put(side, withoutTint(original.getQuads(null, side, 0L)));
            }
        }

        private static List<BakedQuad> withoutTint(List<BakedQuad> source) {
            List<BakedQuad> converted = new ArrayList<>();
            for (BakedQuad quad : source) {
                converted.add(new BakedQuad(quad.getVertexData(), -1, quad.getFace(), quad.getSprite(),
                        quad.shouldApplyDiffuseLighting(), quad.getFormat()));
            }
            return Collections.unmodifiableList(converted);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long random) {
            return quads.get(side);
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transform) {
            return Pair.of(this, originalModel.handlePerspective(transform).getRight());
        }
    }
}
