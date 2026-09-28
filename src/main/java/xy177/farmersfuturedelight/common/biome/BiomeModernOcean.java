package xy177.farmersfuturedelight.common.biome;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class BiomeModernOcean extends BiomeOcean implements ModernWaterBiome {
    private final Type type;
    private final int modernWaterColor;
    private final int modernWaterFogColor;
    private long frozenNoiseSeed = Long.MIN_VALUE;
    private NoiseGeneratorPerlin frozenIceNoise;
    private NoiseGeneratorPerlin frozenPillarNoise;

    public BiomeModernOcean(String registryName, String displayName, Type type,
                            int modernWaterColor, int modernWaterFogColor) {
        super(properties(displayName, type));
        this.type = type;
        this.modernWaterColor = modernWaterColor;
        this.modernWaterFogColor = modernWaterFogColor;
        this.topBlock = topState();
        this.fillerBlock = fillerState();
        setRegistryName(FarmerFutureDelight.MODID, registryName);
    }

    private static Biome.BiomeProperties properties(String name, Type type) {
        Biome.BiomeProperties properties = new Biome.BiomeProperties(name)
                .setBaseHeight(type.isDeep() ? -1.8F : -1.0F)
                .setHeightVariation(0.1F)
                .setTemperature(type.isFrozen() ? 0.0F : 0.5F)
                .setRainfall(0.5F)
                .setWaterColor(0xFFFFFF);
        return type.isFrozen() ? properties.setSnowEnabled() : properties;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void genTerrainBlocks(World world, Random random, ChunkPrimer primer,
                                 int x, int z, double noiseValue) {
        if (type.isFrozen()) {
            ensureFrozenNoise(world.getSeed());
        }
        int seaLevel = world.getSeaLevel();
        int localX = x & 15;
        int localZ = z & 15;
        IBlockState top = topState();
        IBlockState filler = fillerState();
        IBlockState underwater = underwaterState();
        int remainingDepth = -1;
        int surfaceDepth = (int) (noiseValue / 3.0D + 3.0D
                + random.nextDouble() * 0.25D);
        double upperIce = 0.0D;
        double lowerIce = 0.0D;
        int snowCaps = 0;
        int snowCapLimit = 0;
        int snowCapMinimumY = 0;
        if (type.isFrozen()) {
            double iceNoise = Math.min(Math.abs(noiseValue),
                    frozenIceNoise.getValue(x * 0.1D, z * 0.1D));
            if (iceNoise > 1.8D) {
                double pillarNoise = Math.abs(frozenPillarNoise.getValue(
                        x * 0.09765625D, z * 0.09765625D));
                upperIce = iceNoise * iceNoise * 1.2D;
                upperIce = Math.min(upperIce, Math.ceil(pillarNoise * 40.0D) + 14.0D);
                if (getTemperature(new BlockPos(x, 63, z)) > 0.1F) {
                    upperIce -= 2.0D;
                }
                if (upperIce > 2.0D) {
                    lowerIce = seaLevel - upperIce - 7.0D;
                    upperIce += seaLevel;
                } else {
                    upperIce = 0.0D;
                }
            }
            snowCapLimit = 2 + random.nextInt(4);
            snowCapMinimumY = seaLevel + 18 + random.nextInt(10);
        }

        for (int y = Math.max(255, (int) upperIce + 1); y >= 0; y--) {
            IBlockState state = primer.getBlockState(localZ, y, localX);
            if (type.isFrozen()) {
                if (state.getMaterial() == Material.AIR && y < (int) upperIce
                        && random.nextDouble() > 0.01D) {
                    primer.setBlockState(localZ, y, localX, Blocks.PACKED_ICE.getDefaultState());
                } else if (state.getMaterial() == Material.WATER && y > (int) lowerIce
                        && y < seaLevel && lowerIce != 0.0D && random.nextDouble() > 0.15D) {
                    primer.setBlockState(localZ, y, localX, Blocks.PACKED_ICE.getDefaultState());
                }
                state = primer.getBlockState(localZ, y, localX);
                if (state.getMaterial() == Material.AIR) {
                    remainingDepth = -1;
                    continue;
                }
                if (state.getBlock() == Blocks.PACKED_ICE && snowCaps <= snowCapLimit
                        && y > snowCapMinimumY) {
                    primer.setBlockState(localZ, y, localX, Blocks.SNOW.getDefaultState());
                    snowCaps++;
                    continue;
                }
                if (state.getBlock() != Blocks.STONE) {
                    continue;
                }
            }

            if (state.getMaterial() == Material.AIR) {
                remainingDepth = -1;
                continue;
            }
            if (state.getBlock() != Blocks.STONE) {
                continue;
            }
            if (remainingDepth == -1) {
                IBlockState currentTop = top;
                IBlockState currentFiller = filler;
                if (surfaceDepth <= 0) {
                    currentTop = Blocks.AIR.getDefaultState();
                    currentFiller = Blocks.STONE.getDefaultState();
                } else if (y < seaLevel - 4 || y > seaLevel + 1) {
                    currentTop = top;
                    currentFiller = filler;
                }
                if (y < seaLevel && currentTop.getMaterial() == Material.AIR) {
                    currentTop = getTemperature(new BlockPos(x, y, z)) < 0.15F
                            ? Blocks.ICE.getDefaultState() : Blocks.WATER.getDefaultState();
                }
                remainingDepth = surfaceDepth;
                if (y >= seaLevel - 1) {
                    if (currentTop.getBlock() == Blocks.GRASS
                            && primer.getBlockState(localZ, y + 1, localX)
                                    .getMaterial() == Material.WATER) {
                        currentTop = underwater;
                    }
                    primer.setBlockState(localZ, y, localX, currentTop);
                } else if (y < seaLevel - 7 - surfaceDepth) {
                    top = Blocks.AIR.getDefaultState();
                    filler = Blocks.STONE.getDefaultState();
                    primer.setBlockState(localZ, y, localX, underwater);
                } else {
                    primer.setBlockState(localZ, y, localX, currentFiller);
                }
            } else if (remainingDepth > 0) {
                remainingDepth--;
                primer.setBlockState(localZ, y, localX, filler);
                if (remainingDepth == 0 && filler.getBlock() == Blocks.SAND
                        && surfaceDepth > 1) {
                    remainingDepth = random.nextInt(4) + Math.max(0, y - 63);
                    filler = Blocks.SANDSTONE.getDefaultState();
                }
            }
        }
    }

    private IBlockState topState() {
        return type == Type.WARM ? Blocks.SAND.getDefaultState()
                : Blocks.GRASS.getDefaultState();
    }

    private IBlockState fillerState() {
        return type == Type.WARM ? Blocks.SAND.getDefaultState()
                : Blocks.DIRT.getDefaultState();
    }

    private IBlockState underwaterState() {
        return type == Type.WARM || type == Type.LUKEWARM || type == Type.DEEP_LUKEWARM
                ? Blocks.SAND.getDefaultState() : Blocks.GRAVEL.getDefaultState();
    }

    private synchronized void ensureFrozenNoise(long seed) {
        if (frozenNoiseSeed == seed && frozenIceNoise != null && frozenPillarNoise != null) {
            return;
        }
        Random random = new Random(seed);
        frozenIceNoise = new NoiseGeneratorPerlin(random, 4);
        frozenPillarNoise = new NoiseGeneratorPerlin(random, 1);
        frozenNoiseSeed = seed;
    }

    @Override
    public int getModernWaterColor() {
        return modernWaterColor;
    }

    @Override
    public int getModernWaterFogColor() {
        return modernWaterFogColor;
    }

    public enum Type {
        WARM(false, false),
        LUKEWARM(false, false),
        COLD(false, false),
        FROZEN(false, true),
        DEEP_LUKEWARM(true, false),
        DEEP_COLD(true, false),
        DEEP_FROZEN(true, true);

        private final boolean deep;
        private final boolean frozen;

        Type(boolean deep, boolean frozen) {
            this.deep = deep;
            this.frozen = frozen;
        }

        public boolean isDeep() {
            return deep;
        }

        public boolean isFrozen() {
            return frozen;
        }
    }
}
