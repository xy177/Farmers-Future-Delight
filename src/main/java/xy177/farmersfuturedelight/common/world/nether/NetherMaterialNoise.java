package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import xy177.farmersfuturedelight.common.world.noise.FFDImprovedNoise;

public strictfp final class NetherMaterialNoise {
    private final List<FFDImprovedNoise> layers = new ArrayList<>();
    private final List<Double> frequencies = new ArrayList<>();
    private final List<Float> amplitudes = new ArrayList<>();

    public NetherMaterialNoise(long worldSeed, String name) {
        JsonObject parameters = resource("noise/" + name);
        int baseOctave = parameters.get("base_octave").getAsInt();
        int count = parameters.has("octave_count") ? parameters.get("octave_count").getAsInt() : 1;
        double amplitude = parameters.has("base_amplitude") ? parameters.get("base_amplitude").getAsDouble() : 1;
        double[] factors = new double[count];
        double total = 0;
        double variance = 0;
        JsonArray modifiers = parameters.getAsJsonArray("amplitude_modifiers");
        amplitude *= Math.pow(.5D, -(count - 1)) / (Math.pow(.5D, -count) - 1);
        for (int index = 0; index < count; index++) {
            factors[index] = amplitude * (modifiers == null ? 1 : modifiers.get(index).getAsDouble());
            total += Math.abs(factors[index]);
            double deviation = .2702247831245211D * Math.abs(factors[index]);
            variance += deviation * deviation;
            amplitude *= .5D;
        }
        double normalization = total / 3.0D / (Math.sqrt(variance) * Math.sqrt(2.0D));
        long positionalSeed = new Random(worldSeed).nextLong();
        Random random = new Random(positionalSeed ^ (long) ("minecraft:" + name).hashCode());
        long firstSeed = random.nextLong();
        long secondSeed = random.nextLong();
        for (int index = 0; index < count; index++) {
            if (factors[index] == 0) continue;
            String octave = "octave_" + (baseOctave + index);
            float factor = (float) (normalization * factors[index]);
            layers.add(new FFDImprovedNoise(new Random(firstSeed ^ (long) octave.hashCode())));
            frequencies.add(Math.pow(2, baseOctave + index));
            amplitudes.add(factor);
            layers.add(new FFDImprovedNoise(new Random(secondSeed ^ (long) octave.hashCode())));
            frequencies.add(Math.pow(2, baseOctave + index) * 1.0181268882175227D);
            amplitudes.add(factor);
        }
    }

    public float sample(int blockX, int blockZ) {
        float result = 0;
        for (int index = 0; index < layers.size(); index++) {
            double frequency = frequencies.get(index);
            result += amplitudes.get(index) * layers.get(index).sampleFloat(blockX * frequency, 0, blockZ * frequency);
        }
        return result;
    }

    static JsonObject resource(String name) {
        String path = "/assets/farmers_future_delight/worldgen/26_3/nether/" + name + ".json";
        InputStream input = NetherMaterialNoise.class.getResourceAsStream(path);
        if (input == null) throw new IllegalStateException("Missing Nether generation resource: " + path);
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Unable to read Nether generation resource: " + path, failure);
        }
    }

    static long positionSeed(int blockX, int blockY, int blockZ) {
        long value = (long) (blockX * 3129871) ^ blockZ * 116129781L ^ blockY;
        return (value * value * 42317861L + value * 11L) >> 16;
    }
}
