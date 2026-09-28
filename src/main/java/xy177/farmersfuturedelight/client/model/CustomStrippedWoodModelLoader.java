package xy177.farmersfuturedelight.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCustomStrippedWoods;

public final class CustomStrippedWoodModelLoader implements ICustomModelLoader {
    public static final CustomStrippedWoodModelLoader INSTANCE =
            new CustomStrippedWoodModelLoader();
    private static final String LOG_PREFIX = "custom_stripped_wood_log/";
    private static final String WOOD_PREFIX = "custom_stripped_wood_wood/";
    private IResourceManager resourceManager;

    private CustomStrippedWoodModelLoader() {
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        FFDCustomStrippedWoods.resetDerivedColors();
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!FarmerFutureDelight.MODID.equals(modelLocation.getResourceDomain())) {
            return false;
        }
        String path = modelLocation.getResourcePath();
        return path.startsWith(LOG_PREFIX) || path.startsWith(WOOD_PREFIX);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getResourcePath();
        boolean wood = path.startsWith(WOOD_PREFIX);
        String material = path.substring(wood ? WOOD_PREFIX.length() : LOG_PREFIX.length());
        FFDCustomStrippedWoods.Entry entry = FFDCustomStrippedWoods.get(material);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown custom stripped wood model "
                    + modelLocation);
        }
        Block source = entry.sourceBlock(wood);
        if (source == null || source.getRegistryName() == null) {
            throw new IllegalArgumentException("Missing custom stripped wood source "
                    + modelLocation);
        }
        IModel base = ModelLoaderRegistry.getModel(new ResourceLocation(
                FarmerFutureDelight.MODID, "block/custom_stripped_wood_base"));
        ResourceLocation end = sourceTexture(source, wood ? "all" : "end",
                wood ? "all" : "top");
        if (end == null) {
            end = fallbackTexture(source, wood);
        }
        IModel retextured = base.retexture(ImmutableMap.of(
                "end", end.toString(),
                "side", new ResourceLocation(FarmerFutureDelight.MODID,
                        "block/custom_stripped_wood_side").toString()));
        return rotated(retextured, rotation(modelLocation));
    }

    private ModelRotation rotation(ResourceLocation modelLocation) {
        if (!(modelLocation instanceof ModelResourceLocation)) {
            return ModelRotation.X0_Y0;
        }
        String variant = ((ModelResourceLocation) modelLocation).getVariant();
        if ("axis=z".equals(variant)) {
            return ModelRotation.X90_Y0;
        }
        if ("axis=x".equals(variant)) {
            return ModelRotation.X90_Y90;
        }
        return ModelRotation.X0_Y0;
    }

    private IModel rotated(final IModel model, ModelRotation rotation) {
        if (rotation == ModelRotation.X0_Y0) {
            return model;
        }
        final TRSRTransformation transformation = TRSRTransformation.from(rotation);
        return new IModel() {
            @Override
            public Collection<ResourceLocation> getDependencies() {
                return model.getDependencies();
            }

            @Override
            public Collection<ResourceLocation> getTextures() {
                return model.getTextures();
            }

            @Override
            public IBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
                return model.bake(transformation, format, textureGetter);
            }

            @Override
            public IModelState getDefaultState() {
                return transformation;
            }
        };
    }

    public int color(FFDCustomStrippedWoods.Entry entry, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        if (!entry.isColorResolved()) {
            entry.setDerivedColor(deriveColor(entry));
        }
        return entry.tintColor();
    }

    private ResourceLocation sourceTexture(Block source, String preferred, String fallback) {
        try {
            ResourceLocation name = source.getRegistryName();
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(
                    name.getResourceDomain(), "block/" + name.getResourcePath()));
            java.util.Optional<ModelBlock> vanilla = model.asVanillaModel();
            if (vanilla.isPresent()) {
                ModelBlock block = vanilla.get();
                ResourceLocation texture = resolve(block, preferred);
                if (texture == null) {
                    texture = resolve(block, fallback);
                }
                if (texture == null) {
                    texture = resolve(block, "all");
                }
                if (texture != null) {
                    return texture;
                }
            }
            for (ResourceLocation texture : model.getTextures()) {
                if (texture != null && !"missingno".equals(texture.getResourcePath())) {
                    return texture;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private ResourceLocation resolve(ModelBlock model, String key) {
        String texture = model.resolveTextureName("#" + key);
        if (texture == null || texture.startsWith("#") || "missingno".equals(texture)) {
            return null;
        }
        try {
            return new ResourceLocation(texture);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private ResourceLocation fallbackTexture(Block source, boolean wood) {
        ResourceLocation name = source.getRegistryName();
        String path = "block/" + name.getResourcePath();
        if (!wood) {
            path += "_top";
        }
        return new ResourceLocation(name.getResourceDomain(), path);
    }

    private int deriveColor(FFDCustomStrippedWoods.Entry entry) {
        ItemStack source = entry.sourceStack();
        if (source.isEmpty()) {
            return 0xFFFFFF;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        IBakedModel model = minecraft.getRenderItem().getItemModelWithOverrides(
                source, null, null);
        Map<Integer, Bucket> colors = new HashMap<>();
        collect(colors, model.getQuads(null, null, 0L), source, minecraft);
        for (EnumFacing face : EnumFacing.values()) {
            collect(colors, model.getQuads(null, face, 0L), source, minecraft);
        }
        if (colors.isEmpty()) {
            return 0xFFFFFF;
        }
        List<Bucket> buckets = new ArrayList<>(colors.values());
        buckets.sort(Comparator.comparingInt(Bucket::count).reversed());
        int representatives = Math.min(4, buckets.size());
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int index = 0; index < representatives; index++) {
            red += buckets.get(index).red();
            green += buckets.get(index).green();
            blue += buckets.get(index).blue();
        }
        return red / representatives << 16
                | green / representatives << 8
                | blue / representatives;
    }

    private void collect(Map<Integer, Bucket> colors, List<BakedQuad> quads,
                         ItemStack stack, Minecraft minecraft) {
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.getSprite();
            if (sprite == null || "missingno".equals(sprite.getIconName())
                    || sprite.getFrameCount() == 0) {
                continue;
            }
            int tint = quad.hasTintIndex()
                    ? minecraft.getItemColors().colorMultiplier(stack, quad.getTintIndex())
                    : 0xFFFFFF;
            if (tint < 0) {
                tint = 0xFFFFFF;
            }
            int[][] frame = sprite.getFrameTextureData(0);
            if (frame.length == 0 || frame[0] == null) {
                continue;
            }
            for (int pixel : frame[0]) {
                if ((pixel >>> 24) == 0) {
                    continue;
                }
                int red = (pixel >> 16 & 255) * (tint >> 16 & 255) / 255;
                int green = (pixel >> 8 & 255) * (tint >> 8 & 255) / 255;
                int blue = (pixel & 255) * (tint & 255) / 255;
                int key = red >> 3 << 10 | green >> 3 << 5 | blue >> 3;
                colors.computeIfAbsent(key, ignored -> new Bucket()).add(red, green, blue);
            }
        }
    }

    private static final class Bucket {
        private int count;
        private long red;
        private long green;
        private long blue;

        private void add(int red, int green, int blue) {
            count++;
            this.red += red;
            this.green += green;
            this.blue += blue;
        }

        private int count() {
            return count;
        }

        private int red() {
            return (int) (red / count);
        }

        private int green() {
            return (int) (green / count);
        }

        private int blue() {
            return (int) (blue / count);
        }
    }
}
