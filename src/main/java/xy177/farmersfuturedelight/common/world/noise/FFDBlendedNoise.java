package xy177.farmersfuturedelight.common.world.noise;

import java.util.Arrays;

public final class FFDBlendedNoise {
    private final FFDPerlinNoise minLimitNoise;
    private final FFDPerlinNoise maxLimitNoise;
    private final FFDPerlinNoise mainNoise;
    private final double xzMultiplier;
    private final double yMultiplier;
    private final double xzFactor;
    private final double yFactor;
    private final double smearScaleMultiplier;

    public FFDBlendedNoise(FFDXoroshiroRandom random, double xzScale, double yScale, double xzFactor,
                           double yFactor, double smearScaleMultiplier) {
        double[] limitAmplitudes = new double[16];
        double[] mainAmplitudes = new double[8];
        Arrays.fill(limitAmplitudes, 1.0D);
        Arrays.fill(mainAmplitudes, 1.0D);
        minLimitNoise = FFDPerlinNoise.createLegacy(random, -15, limitAmplitudes);
        maxLimitNoise = FFDPerlinNoise.createLegacy(random, -15, limitAmplitudes);
        mainNoise = FFDPerlinNoise.createLegacy(random, -7, mainAmplitudes);
        xzMultiplier = 684.412D * xzScale;
        yMultiplier = 684.412D * yScale;
        this.xzFactor = xzFactor;
        this.yFactor = yFactor;
        this.smearScaleMultiplier = smearScaleMultiplier;
    }

    public double sample(int blockX, int blockY, int blockZ) {
        double limitX = blockX * xzMultiplier;
        double limitY = blockY * yMultiplier;
        double limitZ = blockZ * xzMultiplier;
        double mainX = limitX / xzFactor;
        double mainY = limitY / yFactor;
        double mainZ = limitZ / xzFactor;
        double limitSmear = yMultiplier * smearScaleMultiplier;
        double mainSmear = limitSmear / yFactor;
        double mainValue = 0.0D;
        double octaveScale = 1.0D;
        for (int i = 0; i < 8; i++) {
            FFDImprovedNoise noise = mainNoise.octave(i);
            if (noise != null) {
                mainValue += noise.sample(FFDPerlinNoise.wrap(mainX * octaveScale),
                        FFDPerlinNoise.wrap(mainY * octaveScale), FFDPerlinNoise.wrap(mainZ * octaveScale),
                        mainSmear * octaveScale, mainY * octaveScale) / octaveScale;
            }
            octaveScale /= 2.0D;
        }
        double blend = (mainValue / 10.0D + 1.0D) / 2.0D;
        boolean maxOnly = blend >= 1.0D;
        boolean minOnly = blend <= 0.0D;
        double minValue = 0.0D;
        double maxValue = 0.0D;
        octaveScale = 1.0D;
        for (int i = 0; i < 16; i++) {
            double x = FFDPerlinNoise.wrap(limitX * octaveScale);
            double y = FFDPerlinNoise.wrap(limitY * octaveScale);
            double z = FFDPerlinNoise.wrap(limitZ * octaveScale);
            double yScale = limitSmear * octaveScale;
            FFDImprovedNoise minNoise = minLimitNoise.octave(i);
            FFDImprovedNoise maxNoise = maxLimitNoise.octave(i);
            if (!maxOnly && minNoise != null) {
                minValue += minNoise.sample(x, y, z, yScale, limitY * octaveScale) / octaveScale;
            }
            if (!minOnly && maxNoise != null) {
                maxValue += maxNoise.sample(x, y, z, yScale, limitY * octaveScale) / octaveScale;
            }
            octaveScale /= 2.0D;
        }
        return clampedLerp(blend, minValue / 512.0D, maxValue / 512.0D) / 128.0D;
    }

    private static double clampedLerp(double amount, double first, double second) {
        if (amount <= 0.0D) {
            return first;
        }
        if (amount >= 1.0D) {
            return second;
        }
        return first + amount * (second - first);
    }
}
