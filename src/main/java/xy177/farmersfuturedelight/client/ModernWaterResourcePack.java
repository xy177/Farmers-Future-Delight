package xy177.farmersfuturedelight.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.AbstractResourcePack;

final class ModernWaterResourcePack extends AbstractResourcePack {
    private static final String PACK_METADATA =
            "{\"pack\":{\"pack_format\":3,\"description\":\"Farmer's Future Delight modern water\"}}";
    private static final Set<String> CONTENTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "assets/minecraft/textures/blocks/water_still.png",
            "assets/minecraft/textures/blocks/water_still.png.mcmeta",
            "assets/minecraft/textures/blocks/water_flow.png",
            "assets/minecraft/textures/blocks/water_flow.png.mcmeta",
            "assets/minecraft/textures/blocks/water_overlay.png",
            "assets/minecraft/textures/misc/underwater.png")));

    ModernWaterResourcePack(File source) {
        super(source);
    }

    @Override
    protected InputStream getInputStreamByName(String name) throws IOException {
        if ("pack.mcmeta".equals(name)) {
            return new ByteArrayInputStream(PACK_METADATA.getBytes(StandardCharsets.UTF_8));
        }
        if (!CONTENTS.contains(name)) {
            throw new FileNotFoundException(name);
        }
        String path = "/assets/farmers_future_delight/overrides/"
                + name.substring("assets/minecraft/".length());
        InputStream input = ModernWaterResourcePack.class.getResourceAsStream(path);
        if (input == null) {
            throw new FileNotFoundException(path);
        }
        return input;
    }

    @Override
    protected boolean hasResourceName(String name) {
        return "pack.mcmeta".equals(name) || CONTENTS.contains(name);
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("minecraft");
    }

    @Nonnull
    @Override
    public String getPackName() {
        return "farmers_future_delight_modern_water";
    }
}
