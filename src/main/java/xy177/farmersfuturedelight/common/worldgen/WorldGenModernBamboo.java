package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import xy177.farmersfuturedelight.common.block.BlockBamboo;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.noise.FFDSimplexNoise;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;

public final class WorldGenModernBamboo {
    private static final FFDSimplexNoise COUNT_NOISE =
            new FFDSimplexNoise(new Random(2345L));

    private WorldGenModernBamboo() {
    }

    public static void generate(World world, int chunkX, int chunkZ) {
        if (!(world.getBiomeProvider() instanceof FFDModernBiomeProvider)
                || !FFDItems.isBambooEnabled()) {
            return;
        }
        Block bamboo = FFDItems.effectiveBlock(FFDBlocks.BAMBOO);
        if (bamboo == null) {
            return;
        }
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        double noise = COUNT_NOISE.sample(originX / 80.0D, originZ / 80.0D);
        int attempts = Math.max(0, MathHelper.ceil((noise + 0.3D) * 160.0D));
        Random random = new Random(world.getSeed()
                ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L));
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = originX + random.nextInt(16);
            int z = originZ + random.nextInt(16);
            BlockPos base = findBase(world, x, z);
            if (base == null || world.getBiome(base) != FFDBiomes.BAMBOO_JUNGLE
                    || !bamboo.canPlaceBlockAt(world, base)) {
                continue;
            }
            int height = 5 + random.nextInt(12);
            if (random.nextFloat() < 0.2F) {
                placePodzolPatch(world, base, random);
            }
            placeColumn(world, base, bamboo, height);
        }
    }

    private static BlockPos findBase(World world, int x, int z) {
        int surfaceY = world.getHeight(x, z);
        int minimumY = Math.max(0, surfaceY - 64);
        for (int y = surfaceY; y >= minimumY; y--) {
            BlockPos base = new BlockPos(x, y, z);
            if (world.isAirBlock(base)
                    && BlockBamboo.supportsBamboo(world.getBlockState(base.down()))) {
                return base;
            }
        }
        return null;
    }

    private static void placePodzolPatch(World world, BlockPos center, Random random) {
        int radius = 1 + random.nextInt(4);
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                int offsetX = x - center.getX();
                int offsetZ = z - center.getZ();
                if (offsetX * offsetX + offsetZ * offsetZ > radius * radius) {
                    continue;
                }
                BlockPos ground = new BlockPos(x, world.getHeight(x, z) - 1, z);
                if (isPodzolReplaceable(world, ground)) {
                    world.setBlockState(ground, Blocks.DIRT.getDefaultState()
                            .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL), 2);
                }
            }
        }
    }

    private static boolean isPodzolReplaceable(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        FFDLushCaveBlockProvider provider = FFDLushCaveBlockProvider.get();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.MYCELIUM
                || provider.isMossBlock(world.getBlockState(pos))
                || provider.isRootedDirt(world.getBlockState(pos));
    }

    private static void placeColumn(World world, BlockPos base, Block bamboo, int height) {
        int placed = 0;
        BlockPos top = base;
        for (; placed < height && world.isAirBlock(top); placed++, top = top.up()) {
            IBlockState state = bambooState(bamboo, "none", 0);
            world.setBlockState(top, state, 2);
        }
        if (placed >= 3) {
            world.setBlockState(top, bambooState(bamboo, "large", 1), 2);
            world.setBlockState(top.down(), bambooState(bamboo, "large", 0), 2);
            world.setBlockState(top.down(2), bambooState(bamboo, "small", 0), 2);
        }
    }

    private static IBlockState bambooState(Block bamboo, String leaves, int stage) {
        IBlockState state = bamboo.getDefaultState();
        for (IProperty<?> property : state.getPropertyKeys()) {
            if ("age".equals(property.getName())) {
                state = withProperty(state, property, "1");
            } else if ("leaves".equals(property.getName())) {
                state = withProperty(state, property, leaves);
            } else if ("stage".equals(property.getName())) {
                state = withProperty(state, property, Integer.toString(stage));
            }
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withProperty(IBlockState state, IProperty property, String value) {
        for (Object allowed : property.getAllowedValues()) {
            if (value.equals(property.getName((Comparable) allowed))) {
                return state.withProperty(property, (Comparable) allowed);
            }
        }
        return state;
    }
}
