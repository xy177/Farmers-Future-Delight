package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.biome.BiomeModernOcean;
import xy177.farmersfuturedelight.common.block.BlockCoralPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeResolver;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class WorldGenCoralReefs {
    private static final int WORLDGEN_FLAGS = 2 | 16;
    private static final NoiseGeneratorPerlin REEF_NOISE =
            new NoiseGeneratorPerlin(new Random(2345L), 1);

    private WorldGenCoralReefs() {
    }

    public static void generate(Random random, int chunkX, int chunkZ, World world) {
        if (!FFDItems.isCoralEnabled() || FFDConfig.coralReefNoiseRatio <= 0) {
            return;
        }
        double noise = REEF_NOISE.getValue(chunkX * 16.0D / FFDConfig.coralReefNoiseScale,
                chunkZ * 16.0D / FFDConfig.coralReefNoiseScale);
        int attempts = (int) Math.ceil(noise * FFDConfig.coralReefNoiseRatio);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            if (!isWarmOceanClimate(world, x, z)) {
                continue;
            }
            BlockPos origin = findOceanFloorWater(world, x, z);
            if (origin == null) {
                continue;
            }
            int coralIndex = random.nextInt(FFDBlocks.CORAL_BLOCKS.length);
            switch (random.nextInt(3)) {
                case 0:
                    generateTree(world, random, origin, coralIndex, chunkX, chunkZ);
                    break;
                case 1:
                    generateClaw(world, random, origin, coralIndex, chunkX, chunkZ);
                    break;
                default:
                    generateMushroom(world, random, origin, coralIndex, chunkX, chunkZ);
                    break;
            }
        }
    }

    public static boolean isWarmOceanClimate(World world, int x, int z) {
        Biome biome = world.getBiome(new BlockPos(x, 0, z));
        if (biome instanceof BiomeModernOcean) {
            return ((BiomeModernOcean) biome).getType() == BiomeModernOcean.Type.WARM;
        }
        if (FFDHeightHooks.isExtended(world)
                && world.getBiomeProvider() instanceof FFDModernBiomeProvider) {
            FFDModernBiomeResolver.ModernBiome modern = ((FFDModernBiomeProvider) world
                    .getBiomeProvider()).resolver().sampleFuzzy(x, z).biome;
            return modern == FFDModernBiomeResolver.ModernBiome.WARM_OCEAN;
        }
        return false;
    }

    private static void generateTree(World world, Random random, BlockPos origin, int coralIndex,
                                     int chunkX, int chunkZ) {
        BlockPos cursor = origin;
        int trunkHeight = random.nextInt(3) + 1;
        for (int height = 0; height < trunkHeight; height++) {
            if (!placeCoralBlock(world, random, cursor, coralIndex, chunkX, chunkZ)) {
                return;
            }
            cursor = cursor.up();
        }
        BlockPos branchOrigin = cursor;
        List<EnumFacing> directions = horizontalDirections(random);
        int branches = random.nextInt(3) + 2;
        for (int branch = 0; branch < branches; branch++) {
            EnumFacing direction = directions.get(branch);
            cursor = branchOrigin.offset(direction);
            int length = random.nextInt(5) + 2;
            int verticalRun = 0;
            for (int step = 0; step < length; step++) {
                if (!placeCoralBlock(world, random, cursor, coralIndex, chunkX, chunkZ)) {
                    break;
                }
                verticalRun++;
                cursor = cursor.up();
                if (step == 0 || verticalRun >= 2 && random.nextFloat() < 0.25F) {
                    cursor = cursor.offset(direction);
                    verticalRun = 0;
                }
            }
        }
    }

    private static void generateClaw(World world, Random random, BlockPos origin, int coralIndex,
                                     int chunkX, int chunkZ) {
        if (!placeCoralBlock(world, random, origin, coralIndex, chunkX, chunkZ)) {
            return;
        }
        EnumFacing mainDirection = EnumFacing.Plane.HORIZONTAL.random(random);
        List<EnumFacing> directions = new ArrayList<>();
        directions.add(mainDirection);
        directions.add(mainDirection.rotateY());
        directions.add(mainDirection.rotateYCCW());
        Collections.shuffle(directions, random);
        int branches = random.nextInt(2) + 2;
        for (int branch = 0; branch < branches; branch++) {
            EnumFacing branchDirection = directions.get(branch);
            BlockPos cursor = origin.offset(branchDirection);
            int initialLength = random.nextInt(2) + 1;
            EnumFacing travelDirection;
            int extensionLength;
            if (branchDirection == mainDirection) {
                travelDirection = mainDirection;
                extensionLength = random.nextInt(3) + 2;
            } else {
                cursor = cursor.up();
                travelDirection = random.nextBoolean() ? branchDirection : EnumFacing.UP;
                extensionLength = random.nextInt(3) + 3;
            }
            for (int step = 0; step < initialLength; step++) {
                if (!placeCoralBlock(world, random, cursor, coralIndex, chunkX, chunkZ)) {
                    break;
                }
                cursor = cursor.offset(travelDirection);
            }
            cursor = cursor.offset(travelDirection.getOpposite()).up();
            for (int step = 0; step < extensionLength; step++) {
                cursor = cursor.offset(mainDirection);
                if (!placeCoralBlock(world, random, cursor, coralIndex, chunkX, chunkZ)) {
                    break;
                }
                if (random.nextFloat() < 0.25F) {
                    cursor = cursor.up();
                }
            }
        }
    }

    private static void generateMushroom(World world, Random random, BlockPos origin,
                                         int coralIndex, int chunkX, int chunkZ) {
        int xSize = random.nextInt(3) + 3;
        int ySize = random.nextInt(3) + 3;
        int zSize = random.nextInt(3) + 3;
        int lower = random.nextInt(3) + 1;
        for (int x = 0; x <= ySize; x++) {
            for (int y = 0; y <= xSize; y++) {
                for (int z = 0; z <= zSize; z++) {
                    boolean interiorXy = x != 0 && x != ySize || y != 0 && y != xSize;
                    boolean interiorZy = z != 0 && z != zSize || y != 0 && y != xSize;
                    boolean interiorXz = x != 0 && x != ySize || z != 0 && z != zSize;
                    boolean onFace = x == 0 || x == ySize || y == 0 || y == xSize
                            || z == 0 || z == zSize;
                    if (interiorXy && interiorZy && interiorXz && onFace
                            && random.nextFloat() >= 0.1F) {
                        placeCoralBlock(world, random,
                                origin.add(x, y - lower, z), coralIndex, chunkX, chunkZ);
                    }
                }
            }
        }
    }

    private static boolean placeCoralBlock(World world, Random random, BlockPos pos,
                                           int coralIndex, int chunkX, int chunkZ) {
        if (!isInChunk(pos, chunkX, chunkZ)) {
            return false;
        }
        IBlockState current = world.getBlockState(pos);
        if ((!isSourceWater(current) && !isCoralBlock(current))
                || !isSourceWater(world.getBlockState(pos.up()))) {
            return false;
        }
        IBlockState coralBlock = coralState(FFDBlocks.CORAL_BLOCKS[coralIndex]);
        if (coralBlock == null) {
            return false;
        }
        world.setBlockState(pos, coralBlock, WORLDGEN_FLAGS);
        if (random.nextFloat() < 0.25F) {
            placeFloorCoral(world, random, pos.up(), chunkX, chunkZ);
        } else if (random.nextFloat() < 0.05F) {
            IBlockState pickle = seaPickleState(random.nextInt(4) + 1);
            if (pickle != null) {
                if (isInChunk(pos.up(), chunkX, chunkZ)) {
                    world.setBlockState(pos.up(), pickle, WORLDGEN_FLAGS);
                }
            }
        }
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos side = pos.offset(direction);
            if (!isInChunk(side, chunkX, chunkZ)
                    || random.nextFloat() >= 0.2F
                    || !isSourceWater(world.getBlockState(side))) {
                continue;
            }
            int decoration = random.nextInt(FFDBlocks.CORAL_WALL_FANS.length);
            IBlockState state = coralState(FFDBlocks.CORAL_WALL_FANS[decoration]);
            state = withNamedProperty(state, "facing", direction);
            state = withWater(state);
            if (state != null) {
                world.setBlockState(side, state, WORLDGEN_FLAGS);
            }
        }
        return true;
    }

    private static void placeFloorCoral(World world, Random random, BlockPos pos,
                                        int chunkX, int chunkZ) {
        if (!isInChunk(pos, chunkX, chunkZ)) {
            return;
        }
        int decoration = random.nextInt(FFDBlocks.CORALS.length + FFDBlocks.CORAL_FANS.length);
        IBlockState state;
        if (decoration < FFDBlocks.CORALS.length) {
            state = coralState(FFDBlocks.CORALS[decoration]);
        } else {
            state = coralState(FFDBlocks.CORAL_FANS[decoration - FFDBlocks.CORALS.length]);
            state = withNamedProperty(state, "facing", EnumFacing.UP);
        }
        state = withWater(state);
        if (state != null) {
            world.setBlockState(pos, state, WORLDGEN_FLAGS);
        }
    }

    private static List<EnumFacing> horizontalDirections(Random random) {
        List<EnumFacing> directions = new ArrayList<>();
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            directions.add(direction);
        }
        Collections.shuffle(directions, random);
        return directions;
    }

    private static BlockPos findOceanFloorWater(World world, int x, int z) {
        BlockPos cursor = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        int minY = FFDHeightHooks.minY(world);
        while (cursor.getY() > minY && !isSourceWater(world.getBlockState(cursor))) {
            if (world.getBlockState(cursor).getMaterial() != Material.WATER) {
                return null;
            }
            cursor = cursor.down();
        }
        if (!isSourceWater(world.getBlockState(cursor))) {
            return null;
        }
        while (cursor.getY() > minY && isSourceWater(world.getBlockState(cursor.down()))) {
            cursor = cursor.down();
        }
        return cursor;
    }

    private static boolean isSourceWater(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
        return (pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ;
    }

    private static boolean isCoralBlock(IBlockState state) {
        for (Block block : FFDBlocks.CORAL_BLOCKS) {
            IBlockState effective = coralState(block);
            if (effective != null && state.getBlock() == effective.getBlock()) {
                return true;
            }
        }
        for (Block block : FFDBlocks.DEAD_CORAL_BLOCKS) {
            IBlockState effective = coralState(block);
            if (effective != null && state.getBlock() == effective.getBlock()) {
                return true;
            }
        }
        return false;
    }

    private static IBlockState coralState(Block local) {
        if (FFDItems.isBlockRegistered(local)) {
            return local.getDefaultState();
        }
        ResourceLocation registryName = local.getRegistryName();
        return registryName == null ? null : FFDCompat.getExternalBlockState(
                FFDCompat.Feature.CORAL, registryName.getResourcePath());
    }

    private static IBlockState seaPickleState(int amount) {
        IBlockState state;
        if (FFDItems.isBlockRegistered(FFDBlocks.SEA_PICKLE)) {
            state = FFDBlocks.SEA_PICKLE.getDefaultState();
        } else {
            state = FFDCompat.getExternalBlockState(FFDCompat.Feature.SEA_PICKLE,
                    "sea_pickle");
        }
        state = withNamedProperty(state, "pickles", amount);
        state = withNamedProperty(state, "amount", amount);
        return withWater(state);
    }

    private static IBlockState withWater(IBlockState state) {
        state = withNamedProperty(state, "waterlogged", true);
        return withNamedProperty(state, "in_water", true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withNamedProperty(IBlockState state, String name,
                                                 Comparable value) {
        if (state == null) {
            return null;
        }
        for (IProperty property : state.getPropertyKeys()) {
            if (name.equals(property.getName()) && property.getAllowedValues().contains(value)) {
                return state.withProperty(property, value);
            }
        }
        return state;
    }
}
