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
import xy177.farmersfuturedelight.common.registry.FFDCustomDeepslateOres;

public final class CustomDeepslateOreModelLoader implements ICustomModelLoader {
    public static final CustomDeepslateOreModelLoader INSTANCE =
            new CustomDeepslateOreModelLoader();
    private static final String PREFIX = "custom_deepslate_ore/";
    private IResourceManager resourceManager;

    private CustomDeepslateOreModelLoader() {
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        FFDCustomDeepslateOres.resetDerivedColors();
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return FarmerFutureDelight.MODID.equals(modelLocation.getResourceDomain())
                && modelLocation.getResourcePath().startsWith(PREFIX);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String material = modelLocation.getResourcePath().substring(PREFIX.length());
        FFDCustomDeepslateOres.Entry entry = FFDCustomDeepslateOres.get(material);
        if (entry == null) {
            throw new IOException("Unknown custom deepslate ore model " + modelLocation);
        }
        if (hasSpecificTexture(entry)) {
            IModel model = ModelLoaderRegistry.getModel(
                    new ResourceLocation("minecraft", "block/cube_all"));
            return model.retexture(ImmutableMap.of("all", texture(entry).toString()));
        }
        return ModelLoaderRegistry.getModel(new ResourceLocation(
                FarmerFutureDelight.MODID, "block/deepslate_compat_ore"));
    }

    public int color(FFDCustomDeepslateOres.Entry entry, int tintIndex) {
        if (tintIndex != 0 || hasSpecificTexture(entry)) {
            return 0xFFFFFF;
        }
        if (!entry.isColorResolved()) {
            entry.setDerivedColor(deriveColor(entry));
        }
        return entry.tintColor();
    }

    public boolean hasSpecificTexture(FFDCustomDeepslateOres.Entry entry) {
        IResourceManager manager = resourceManager == null
                ? Minecraft.getMinecraft().getResourceManager() : resourceManager;
        try {
            return manager.getResource(textureFile(entry)) != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    private ResourceLocation texture(FFDCustomDeepslateOres.Entry entry) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "block/custom_deepslate_ores/" + entry.material());
    }

    private ResourceLocation textureFile(FFDCustomDeepslateOres.Entry entry) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/block/custom_deepslate_ores/" + entry.material() + ".png");
    }

    private int deriveColor(FFDCustomDeepslateOres.Entry entry) {
        ItemStack source = entry.colorSource();
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
        Bucket background = buckets.get(0);
        List<Bucket> oreColors = new ArrayList<>();
        for (Bucket bucket : buckets) {
            if (bucket.distance(background) >= 24) {
                oreColors.add(bucket);
            }
        }
        if (oreColors.isEmpty()) {
            oreColors = buckets;
        }
        int representatives = Math.min(4, oreColors.size());
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int index = 0; index < representatives; index++) {
            red += oreColors.get(index).red();
            green += oreColors.get(index).green();
            blue += oreColors.get(index).blue();
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

        private int distance(Bucket other) {
            int dr = red() - other.red();
            int dg = green() - other.green();
            int db = blue() - other.blue();
            return (int) Math.sqrt(dr * dr + dg * dg + db * db);
        }
    }
}
