package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;
import xy177.farmersfuturedelight.common.world.noise.FFDXoroshiroRandom;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class WorldGenModernOres {
    private static final int MIN_Y = FFDModernWorldgenData.MIN_Y;
    private static final int MAX_Y = FFDModernWorldgenData.MIN_Y
            + FFDModernWorldgenData.HEIGHT - 1;

    private final long worldSeed;
    private final FFDModernStoneProvider stones;

    public WorldGenModernOres(long worldSeed, FFDModernStoneProvider stones) {
        this.worldSeed = worldSeed;
        this.stones = stones;
    }

    public void generateChunk(int chunkX, int chunkZ, Access access) {
        int maximumSize = scaledSize(64);
        int originRadius = Math.max(1, (maximumReach(maximumSize) + 15) / 16);
        for (int originChunkX = chunkX - originRadius;
             originChunkX <= chunkX + originRadius; originChunkX++) {
            for (int originChunkZ = chunkZ - originRadius;
                 originChunkZ <= chunkZ + originRadius; originChunkZ++) {
                generateFromOrigin(originChunkX, originChunkZ, access);
            }
        }
    }

    private void generateFromOrigin(int chunkX, int chunkZ, Access access) {
        placeCount(chunkX, chunkZ, 0x01L, FFDConfig.modernDirtCount,
                HeightRange.uniform(0, 160), Vein.DIRT, 33, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x02L, FFDConfig.modernGravelCount,
                HeightRange.uniform(MIN_Y, MAX_Y), Vein.GRAVEL, 33, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x03L, FFDConfig.modernGraniteLowerCount,
                HeightRange.uniform(0, 60), Vein.GRANITE, 64, 0.0F, Filter.ANY, access);
        placeRarity(chunkX, chunkZ, 0x04L, FFDConfig.modernGraniteUpperRarity,
                HeightRange.uniform(64, 128), Vein.GRANITE, 64, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x05L, FFDConfig.modernDioriteLowerCount,
                HeightRange.uniform(0, 60), Vein.DIORITE, 64, 0.0F, Filter.ANY, access);
        placeRarity(chunkX, chunkZ, 0x06L, FFDConfig.modernDioriteUpperRarity,
                HeightRange.uniform(64, 128), Vein.DIORITE, 64, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x07L, FFDConfig.modernAndesiteLowerCount,
                HeightRange.uniform(0, 60), Vein.ANDESITE, 64, 0.0F, Filter.ANY, access);
        placeRarity(chunkX, chunkZ, 0x08L, FFDConfig.modernAndesiteUpperRarity,
                HeightRange.uniform(64, 128), Vein.ANDESITE, 64, 0.0F, Filter.ANY, access);
        if (stones.hasTuff()) {
            placeCount(chunkX, chunkZ, 0x09L, FFDConfig.modernTuffCount,
                    HeightRange.uniform(MIN_Y, 0), Vein.TUFF, 64, 0.0F, Filter.ANY, access);
        }

        placeCount(chunkX, chunkZ, 0x11L, FFDConfig.modernCoalUpperCount,
                HeightRange.uniform(136, MAX_Y), Vein.COAL, 17, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x12L, FFDConfig.modernCoalLowerCount,
                HeightRange.trapezoid(0, 192), Vein.COAL, 17, 0.5F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x13L, FFDConfig.modernIronUpperCount,
                HeightRange.trapezoid(80, 384), Vein.IRON, 9, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x14L, FFDConfig.modernIronMiddleCount,
                HeightRange.trapezoid(-24, 56), Vein.IRON, 9, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x15L, FFDConfig.modernIronSmallCount,
                HeightRange.uniform(MIN_Y, 72), Vein.IRON, 4, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x16L, FFDConfig.modernGoldCount,
                HeightRange.trapezoid(-64, 32), Vein.GOLD, 9, 0.5F, Filter.ANY, access);
        placeRandomCount(chunkX, chunkZ, 0x17L, FFDConfig.modernGoldLowerMaxCount,
                HeightRange.uniform(-64, -48), Vein.GOLD, 9, 0.5F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x18L, FFDConfig.modernGoldExtraCount,
                HeightRange.uniform(32, 256), Vein.GOLD, 9, 0.0F, Filter.BADLANDS, access);
        placeCount(chunkX, chunkZ, 0x19L, FFDConfig.modernRedstoneCount,
                HeightRange.uniform(MIN_Y, 15), Vein.REDSTONE, 8, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x1AL, FFDConfig.modernRedstoneLowerCount,
                HeightRange.trapezoid(MIN_Y - 32, MIN_Y + 32),
                Vein.REDSTONE, 8, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x1BL, FFDConfig.modernDiamondCount,
                HeightRange.trapezoid(MIN_Y - 80, MIN_Y + 80),
                Vein.DIAMOND, 4, 0.5F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x1CL, FFDConfig.modernDiamondBuriedCount,
                HeightRange.trapezoid(MIN_Y - 80, MIN_Y + 80),
                Vein.DIAMOND, 8, 1.0F, Filter.ANY, access);
        placeRarity(chunkX, chunkZ, 0x1DL, FFDConfig.modernDiamondLargeRarity,
                HeightRange.trapezoid(MIN_Y - 80, MIN_Y + 80),
                Vein.DIAMOND, 12, 0.7F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x1EL, FFDConfig.modernLapisCount,
                HeightRange.trapezoid(-32, 32), Vein.LAPIS, 7, 0.0F, Filter.ANY, access);
        placeCount(chunkX, chunkZ, 0x1FL, FFDConfig.modernLapisBuriedCount,
                HeightRange.uniform(MIN_Y, 64), Vein.LAPIS, 7, 1.0F, Filter.ANY, access);
        if (stones.hasCopperOre()) {
            placeCount(chunkX, chunkZ, 0x20L, FFDConfig.modernCopperCount,
                    HeightRange.trapezoid(-16, 112), Vein.COPPER, 10, 0.0F, Filter.ANY, access);
        }
        placeCount(chunkX, chunkZ, 0x21L, FFDConfig.modernEmeraldCount,
                HeightRange.trapezoid(-16, 480), Vein.EMERALD, 3, 0.0F, Filter.MOUNTAIN, access);
        if (stones.hasDeepBase()) {
            placeCount(chunkX, chunkZ, 0x22L, FFDConfig.modernInfestedCount,
                    HeightRange.uniform(MIN_Y, 63), Vein.INFESTED, 9, 0.0F,
                    Filter.MOUNTAIN, access);
        }
        if (stones.hasDeepBase()) {
            for (FFDDeepslateOreCompat.Generation generation
                    : FFDDeepslateOreCompat.generations()) {
                placeCompatCount(chunkX, chunkZ, generation, access);
            }
        }
    }

    private void placeCount(int chunkX, int chunkZ, long salt, int count,
                            HeightRange height, Vein vein, int size,
                            float discardChance, Filter filter, Access access) {
        FFDXoroshiroRandom random = random(chunkX, chunkZ, salt);
        placeAttempts(chunkX, chunkZ, count, height, vein, size,
                discardChance, filter, access, random);
    }

    private void placeRandomCount(int chunkX, int chunkZ, long salt, int maximumCount,
                                  HeightRange height, Vein vein, int size,
                                  float discardChance, Filter filter, Access access) {
        FFDXoroshiroRandom random = random(chunkX, chunkZ, salt);
        int count = maximumCount <= 0 ? 0 : random.nextInt(maximumCount + 1);
        placeAttempts(chunkX, chunkZ, count, height, vein, size,
                discardChance, filter, access, random);
    }

    private void placeRarity(int chunkX, int chunkZ, long salt, int rarity,
                             HeightRange height, Vein vein, int size,
                             float discardChance, Filter filter, Access access) {
        FFDXoroshiroRandom random = random(chunkX, chunkZ, salt);
        if (rarity <= 0 || random.nextInt(rarity) != 0) {
            return;
        }
        placeAttempts(chunkX, chunkZ, 1, height, vein, size,
                discardChance, filter, access, random);
    }

    private void placeAttempts(int chunkX, int chunkZ, int count, HeightRange height,
                               Vein vein, int size, float discardChance,
                               Filter filter, Access access, FFDXoroshiroRandom random) {
        int scaledSize = scaledSize(size);
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int attempt = 0; attempt < count; attempt++) {
            int x = startX + random.nextInt(16);
            int y = height.sample(random);
            int z = startZ + random.nextInt(16);
            if (filter == Filter.ANY || filter.matches(access.getBiome(x, z))) {
                generateVein(x, y, z, scaledSize, vein, discardChance, access, random);
            }
        }
    }

    private void placeCompatCount(int chunkX, int chunkZ,
                                  FFDDeepslateOreCompat.Generation generation, Access access) {
        FFDXoroshiroRandom random = random(chunkX, chunkZ, generation.salt());
        int count = generation.count();
        int size = scaledSize(generation.size());
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int attempt = 0; attempt < count; attempt++) {
            int x = startX + random.nextInt(16);
            int y = new HeightRange(generation.minY(), generation.maxY(),
                    generation.trapezoid(), generation.plateau()).sample(random);
            int z = startZ + random.nextInt(16);
            generateCompatVein(x, y, z, size, generation.state(), MIN_Y,
                    -1, generation.discardChance(), access, random);
        }
    }

    private void generateCompatVein(int originX, int originY, int originZ, int size,
                                    IBlockState replacement, int minimumY, int maximumY,
                                    float discardChance, Access access,
                                    FFDXoroshiroRandom random) {
        int reach = maximumReach(size);
        if (originX + reach < access.targetMinX() || originX - reach > access.targetMaxX()
                || originZ + reach < access.targetMinZ() || originZ - reach > access.targetMaxZ()
                || originY + reach < minimumY || originY - reach > maximumY) {
            return;
        }
        float angle = random.nextFloat() * (float) Math.PI;
        float horizontalRadius = size / 8.0F;
        double startX = originX + Math.sin(angle) * horizontalRadius;
        double endX = originX - Math.sin(angle) * horizontalRadius;
        double startZ = originZ + Math.cos(angle) * horizontalRadius;
        double endZ = originZ - Math.cos(angle) * horizontalRadius;
        double startY = originY + random.nextInt(3) - 2;
        double endY = originY + random.nextInt(3) - 2;
        double[] spheres = new double[size * 4];
        for (int index = 0; index < size; index++) {
            float progress = index / (float) size;
            double centerX = lerp(progress, startX, endX);
            double centerY = lerp(progress, startY, endY);
            double centerZ = lerp(progress, startZ, endZ);
            double randomScale = random.nextDouble() * size / 16.0D;
            double radius = ((Math.sin(Math.PI * progress) + 1.0D) * randomScale + 1.0D) / 2.0D;
            int offset = index * 4;
            spheres[offset] = centerX;
            spheres[offset + 1] = centerY;
            spheres[offset + 2] = centerZ;
            spheres[offset + 3] = radius;
        }
        removeContainedSpheres(spheres, size);
        for (int index = 0; index < size; index++) {
            int offset = index * 4;
            double radius = spheres[offset + 3];
            if (radius < 0.0D) {
                continue;
            }
            double centerX = spheres[offset];
            double centerY = spheres[offset + 1];
            double centerZ = spheres[offset + 2];
            int minX = Math.max(access.targetMinX(), floor(centerX - radius));
            int maxX = Math.min(access.targetMaxX(), floor(centerX + radius));
            int minY = Math.max(minimumY, floor(centerY - radius));
            int maxY = Math.min(maximumY, floor(centerY + radius));
            int minZ = Math.max(access.targetMinZ(), floor(centerZ - radius));
            int maxZ = Math.min(access.targetMaxZ(), floor(centerZ + radius));
            for (int x = minX; x <= maxX; x++) {
                double normalizedX = (x + 0.5D - centerX) / radius;
                double xSquared = normalizedX * normalizedX;
                if (xSquared >= 1.0D) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    double normalizedY = (y + 0.5D - centerY) / radius;
                    double xySquared = xSquared + normalizedY * normalizedY;
                    if (xySquared >= 1.0D) {
                        continue;
                    }
                    for (int z = minZ; z <= maxZ; z++) {
                        double normalizedZ = (z + 0.5D - centerZ) / radius;
                        if (xySquared + normalizedZ * normalizedZ >= 1.0D) {
                            continue;
                        }
                        IBlockState current = access.getState(x, y, z);
                        if (stones.isDeepslateBase(current)
                                && (discardChance <= 0.0F
                                || random.nextFloat() >= discardChance
                                || !isAdjacentToAir(access, x, y, z))) {
                            access.setState(x, y, z, replacement);
                        }
                    }
                }
            }
        }
    }

    private void generateVein(int originX, int originY, int originZ, int size,
                              Vein vein, float discardChance, Access access,
                              FFDXoroshiroRandom random) {
        int reach = maximumReach(size);
        if (originX + reach < access.targetMinX() || originX - reach > access.targetMaxX()
                || originZ + reach < access.targetMinZ() || originZ - reach > access.targetMaxZ()
                || originY + reach < MIN_Y || originY - reach > MAX_Y) {
            return;
        }

        float angle = random.nextFloat() * (float) Math.PI;
        float horizontalRadius = size / 8.0F;
        double startX = originX + Math.sin(angle) * horizontalRadius;
        double endX = originX - Math.sin(angle) * horizontalRadius;
        double startZ = originZ + Math.cos(angle) * horizontalRadius;
        double endZ = originZ - Math.cos(angle) * horizontalRadius;
        double startY = originY + random.nextInt(3) - 2;
        double endY = originY + random.nextInt(3) - 2;
        double[] spheres = new double[size * 4];

        for (int index = 0; index < size; index++) {
            float progress = index / (float) size;
            double centerX = lerp(progress, startX, endX);
            double centerY = lerp(progress, startY, endY);
            double centerZ = lerp(progress, startZ, endZ);
            double randomScale = random.nextDouble() * size / 16.0D;
            double radius = ((Math.sin(Math.PI * progress) + 1.0D) * randomScale + 1.0D) / 2.0D;
            int offset = index * 4;
            spheres[offset] = centerX;
            spheres[offset + 1] = centerY;
            spheres[offset + 2] = centerZ;
            spheres[offset + 3] = radius;
        }

        removeContainedSpheres(spheres, size);
        for (int index = 0; index < size; index++) {
            int offset = index * 4;
            double radius = spheres[offset + 3];
            if (radius < 0.0D) {
                continue;
            }
            double centerX = spheres[offset];
            double centerY = spheres[offset + 1];
            double centerZ = spheres[offset + 2];
            int minX = Math.max(access.targetMinX(), floor(centerX - radius));
            int maxX = Math.min(access.targetMaxX(), floor(centerX + radius));
            int minY = Math.max(MIN_Y, floor(centerY - radius));
            int maxY = Math.min(MAX_Y, floor(centerY + radius));
            int minZ = Math.max(access.targetMinZ(), floor(centerZ - radius));
            int maxZ = Math.min(access.targetMaxZ(), floor(centerZ + radius));

            for (int x = minX; x <= maxX; x++) {
                double normalizedX = (x + 0.5D - centerX) / radius;
                double xSquared = normalizedX * normalizedX;
                if (xSquared >= 1.0D) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    double normalizedY = (y + 0.5D - centerY) / radius;
                    double xySquared = xSquared + normalizedY * normalizedY;
                    if (xySquared >= 1.0D) {
                        continue;
                    }
                    for (int z = minZ; z <= maxZ; z++) {
                        double normalizedZ = (z + 0.5D - centerZ) / radius;
                        if (xySquared + normalizedZ * normalizedZ >= 1.0D) {
                            continue;
                        }
                        IBlockState current = access.getState(x, y, z);
                        IBlockState replacement = replacement(current, vein);
                        if (replacement == null || discardChance > 0.0F
                                && random.nextFloat() < discardChance
                                && isAdjacentToAir(access, x, y, z)) {
                            continue;
                        }
                        access.setState(x, y, z, replacement);
                    }
                }
            }
        }
    }

    private static void removeContainedSpheres(double[] spheres, int size) {
        for (int first = 0; first < size - 1; first++) {
            int firstOffset = first * 4;
            if (spheres[firstOffset + 3] <= 0.0D) {
                continue;
            }
            for (int second = first + 1; second < size; second++) {
                int secondOffset = second * 4;
                if (spheres[secondOffset + 3] <= 0.0D) {
                    continue;
                }
                double dx = spheres[firstOffset] - spheres[secondOffset];
                double dy = spheres[firstOffset + 1] - spheres[secondOffset + 1];
                double dz = spheres[firstOffset + 2] - spheres[secondOffset + 2];
                double radiusDifference = spheres[firstOffset + 3] - spheres[secondOffset + 3];
                if (radiusDifference * radiusDifference <= dx * dx + dy * dy + dz * dz) {
                    continue;
                }
                if (radiusDifference > 0.0D) {
                    spheres[secondOffset + 3] = -1.0D;
                } else {
                    spheres[firstOffset + 3] = -1.0D;
                }
            }
        }
    }

    private IBlockState replacement(IBlockState current, Vein vein) {
        boolean deep = stones.isDeepBase(current);
        boolean baseStone = current.getBlock() == Blocks.STONE || deep;
        if (!baseStone) {
            return null;
        }
        switch (vein) {
            case DIRT:
                return Blocks.DIRT.getDefaultState();
            case GRAVEL:
                return Blocks.GRAVEL.getDefaultState();
            case GRANITE:
                return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
                        BlockStone.EnumType.GRANITE);
            case DIORITE:
                return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
                        BlockStone.EnumType.DIORITE);
            case ANDESITE:
                return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
                        BlockStone.EnumType.ANDESITE);
            case TUFF:
                return stones.tuff();
            case COAL:
                return deep ? stones.coalOre()
                        : Blocks.COAL_ORE.getDefaultState();
            case IRON:
                return deep ? stones.ironOre()
                        : Blocks.IRON_ORE.getDefaultState();
            case COPPER:
                IBlockState copper = stones.normalCopperOre();
                if (copper == null) {
                    return null;
                }
                return deep ? stones.copperOre() : copper;
            case GOLD:
                return deep ? stones.goldOre()
                        : Blocks.GOLD_ORE.getDefaultState();
            case REDSTONE:
                return deep ? stones.redstoneOre()
                        : Blocks.REDSTONE_ORE.getDefaultState();
            case LAPIS:
                return deep ? stones.lapisOre()
                        : Blocks.LAPIS_ORE.getDefaultState();
            case DIAMOND:
                return deep ? stones.diamondOre()
                        : Blocks.DIAMOND_ORE.getDefaultState();
            case EMERALD:
                return deep ? stones.emeraldOre()
                        : Blocks.EMERALD_ORE.getDefaultState();
            case INFESTED:
                return deep ? stones.infestedDeepslate()
                        : Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT,
                                BlockSilverfish.EnumType.STONE);
            default:
                return null;
        }
    }

    private static boolean isAdjacentToAir(Access access, int x, int y, int z) {
        return access.getState(x - 1, y, z).getBlock() == Blocks.AIR
                || access.getState(x + 1, y, z).getBlock() == Blocks.AIR
                || access.getState(x, y - 1, z).getBlock() == Blocks.AIR
                || access.getState(x, y + 1, z).getBlock() == Blocks.AIR
                || access.getState(x, y, z - 1).getBlock() == Blocks.AIR
                || access.getState(x, y, z + 1).getBlock() == Blocks.AIR;
    }

    private FFDXoroshiroRandom random(int chunkX, int chunkZ, long salt) {
        long mixed = mix64(worldSeed ^ salt * 0x9E3779B97F4A7C15L
                ^ (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L);
        return new FFDXoroshiroRandom(mixed);
    }

    private static int scaledSize(int size) {
        return Math.max(1, Math.round(size * FFDConfig.modernOreVeinSizePercent / 100.0F));
    }

    private static int maximumReach(int size) {
        return (int) Math.ceil(size / 8.0D + size / 16.0D + 1.0D);
    }

    private static double lerp(double amount, double start, double end) {
        return start + amount * (end - start);
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static long mix64(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public interface Access {
        IBlockState getState(int x, int y, int z);

        void setState(int x, int y, int z, IBlockState state);

        Biome getBiome(int x, int z);

        int targetMinX();

        int targetMaxX();

        int targetMinZ();

        int targetMaxZ();
    }

    private enum Vein {
        DIRT,
        GRAVEL,
        GRANITE,
        DIORITE,
        ANDESITE,
        TUFF,
        COAL,
        IRON,
        COPPER,
        GOLD,
        REDSTONE,
        LAPIS,
        DIAMOND,
        EMERALD,
        INFESTED
    }

    private enum Filter {
        ANY {
            @Override
            boolean matches(Biome biome) {
                return true;
            }
        },
        BADLANDS {
            @Override
            boolean matches(Biome biome) {
                return BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA);
            }
        },
        MOUNTAIN {
            @Override
            boolean matches(Biome biome) {
                return BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN);
            }
        };

        abstract boolean matches(Biome biome);
    }

    private static final class HeightRange {
        private final int minimum;
        private final int maximum;
        private final boolean trapezoid;
        private final int plateau;

        private HeightRange(int minimum, int maximum, boolean trapezoid, int plateau) {
            this.minimum = minimum;
            this.maximum = maximum;
            this.trapezoid = trapezoid;
            this.plateau = plateau;
        }

        static HeightRange uniform(int minimum, int maximum) {
            return new HeightRange(minimum, maximum, false, 0);
        }

        static HeightRange trapezoid(int minimum, int maximum) {
            return new HeightRange(minimum, maximum, true, 0);
        }

        int sample(FFDXoroshiroRandom random) {
            int range = maximum - minimum;
            if (!trapezoid || range <= 0 || plateau >= range) {
                return minimum + (range <= 0 ? 0 : random.nextInt(range + 1));
            }
            int middle = (range - plateau) / 2;
            return minimum + random.nextInt(range - middle + 1) + random.nextInt(middle + 1);
        }
    }
}
