package xy177.farmersfuturedelight.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

final class FFDModernHorseTextures {
    private static final String[] COATS = {"white", "creamy", "chestnut", "brown", "black", "gray", "darkbrown"};
    private static final String[] MARKINGS = {"", "white", "whitefield", "whitedots", "blackdots"};
    private static final Map<Integer, ResourceLocation> TEXTURES = new HashMap<>();

    private FFDModernHorseTextures() {
    }

    static ResourceLocation get(EntityHorse horse, boolean baby) {
        int coat = (horse.getHorseVariant() & 255) % COATS.length;
        int marking = (horse.getHorseVariant() >> 8 & 255) % MARKINGS.length;
        String suffix = baby ? "_baby" : "";
        ResourceLocation base = texture("horse_" + COATS[coat] + suffix);
        if (marking == 0) return base;
        int key = (baby ? COATS.length * MARKINGS.length : 0) + coat * MARKINGS.length + marking;
        ResourceLocation combined = TEXTURES.get(key);
        if (combined == null) {
            combined = texture("combined/" + COATS[coat] + "_" + MARKINGS[marking] + suffix);
            Minecraft.getMinecraft().getTextureManager().loadTexture(combined,
                    new LayeredTexture(base.toString(), texture("horse_markings_" + MARKINGS[marking] + suffix).toString()));
            TEXTURES.put(key, combined);
        }
        return combined;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/horse/" + name + ".png");
    }
}
