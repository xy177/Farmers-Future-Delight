package xy177.farmersfuturedelight.common.advancement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDVanillaAdvancements {
    private static final String PREFIX = "/assets/minecraft/advancements/";
    private static final Set<String> OVERRIDES = new HashSet<>(Arrays.asList(
            "adventure/adventuring_time.json", "adventure/kill_a_mob.json",
            "adventure/kill_all_mobs.json", "husbandry/balanced_diet.json",
            "husbandry/bred_all_animals.json"));

    private FFDVanillaAdvancements() {
    }

    public static BufferedReader newBufferedReader(Path path) throws IOException {
        return newBufferedReader(path, StandardCharsets.UTF_8);
    }

    public static BufferedReader newBufferedReader(Path path, Charset charset) throws IOException {
        String normalized = path.toString().replace('\\', '/');
        int prefixIndex = normalized.indexOf(PREFIX);
        if (prefixIndex >= 0) {
            String relative = normalized.substring(prefixIndex + PREFIX.length());
            if (OVERRIDES.contains(relative)) {
                InputStream resource = FFDVanillaAdvancements.class.getResourceAsStream(
                        "/assets/" + FarmerFutureDelight.MODID + "/advancement_overrides/" + relative);
                if (resource != null) {
                    return new BufferedReader(new InputStreamReader(resource, charset));
                }
            }
        }
        return Files.newBufferedReader(path, charset);
    }
}
