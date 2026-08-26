package xy177.farmersfuturedelight.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;

public final class CustomRawOreModelLoader implements ICustomModelLoader {
    public static final CustomRawOreModelLoader INSTANCE = new CustomRawOreModelLoader();
    private static final String ITEM_PREFIX = "custom_raw_ore_item/";
    private static final String BLOCK_PREFIX = "custom_raw_ore_block/";
    private IResourceManager resourceManager;

    private CustomRawOreModelLoader() {
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        FFDCustomRawOres.resetDerivedColors();
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!FarmerFutureDelight.MODID.equals(modelLocation.getResourceDomain())) {
            return false;
        }
        String path = modelLocation.getResourcePath();
        return path.startsWith(ITEM_PREFIX) || path.startsWith(BLOCK_PREFIX);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getResourcePath();
        boolean block = path.startsWith(BLOCK_PREFIX);
        String material = path.substring(block ? BLOCK_PREFIX.length() : ITEM_PREFIX.length());
        FFDCustomRawOres.Entry entry = FFDCustomRawOres.get(material);
        if (entry == null) {
            throw new IOException("Unknown custom raw ore model " + modelLocation);
        }
        ResourceLocation base = new ResourceLocation(FarmerFutureDelight.MODID,
                block ? "block/custom_raw_ore_block_base" : "item/custom_raw_ore_base");
        IModel model = ModelLoaderRegistry.getModel(base);
        ResourceLocation texture = texture(entry, block);
        return model.retexture(ImmutableMap.of(block ? "all" : "layer0", texture.toString()));
    }

    public int color(FFDCustomRawOres.Entry entry, boolean block, int tintIndex) {
        if (tintIndex != 0 || hasSpecificTexture(entry, block)) {
            return 0xFFFFFF;
        }
        if (!entry.isColorResolved()) {
            entry.setDerivedColor(deriveColor(entry));
        }
        return entry.tintColor();
    }

    public boolean hasSpecificTexture(FFDCustomRawOres.Entry entry, boolean block) {
        IResourceManager manager = resourceManager == null
                ? Minecraft.getMinecraft().getResourceManager() : resourceManager;
        try {
            return manager.getResource(textureFile(entry, block)) != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    private ResourceLocation texture(FFDCustomRawOres.Entry entry, boolean block) {
        if (hasSpecificTexture(entry, block)) {
            return new ResourceLocation(FarmerFutureDelight.MODID,
                    (block ? "block/custom_raw_ore_blocks/" : "item/custom_raw_ores/")
                            + entry.material());
        }
        return new ResourceLocation(FarmerFutureDelight.MODID,
                block ? "block/custom_raw_ore_block_base" : "item/custom_raw_ore_base");
    }

    private ResourceLocation textureFile(FFDCustomRawOres.Entry entry, boolean block) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                (block ? "textures/block/custom_raw_ore_blocks/"
                        : "textures/item/custom_raw_ores/") + entry.material() + ".png");
    }

    private int deriveColor(FFDCustomRawOres.Entry entry) {
        ItemStack output = entry.resolveSmeltResult();
        if (output.isEmpty()) {
            return 0xFFFFFF;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        IBakedModel model = minecraft.getRenderItem().getItemModelWithOverrides(
                output, null, null);
        Map<Integer, Bucket> colors = new HashMap<>();
        collect(colors, model.getQuads(null, null, 0L), output, minecraft);
        for (EnumFacing face : EnumFacing.values()) {
            collect(colors, model.getQuads(null, face, 0L), output, minecraft);
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
        for (int i = 0; i < representatives; i++) {
            red += buckets.get(i).red();
            green += buckets.get(i).green();
            blue += buckets.get(i).blue();
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
