package xy177.farmersfuturedelight.common.worldgen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.noise.FFDFlowerNoise;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

final class FFDJigsawFeatures {
    private static final Set<String> FEATURES = new HashSet<>(Arrays.asList("acacia", "oak", "pine", "spruce",
            "flower_plain", "patch_berry_bush", "patch_cactus", "patch_taiga_grass", "pile_hay", "pile_ice",
            "pile_melon", "pile_pumpkin", "pile_snow"));
    private static final FFDFlowerNoise FLOWER_NOISE = new FFDFlowerNoise(2345L);
    private static final String[] LOW_FLOWERS = {"orange_tulip", "red_tulip", "pink_tulip", "white_tulip"};
    private static final String[] HIGH_FLOWERS = {"poppy", "houstonia", "oxeye_daisy", "cornflower"};
    private static final EnumFacing[] HORIZONTAL = {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    private FFDJigsawFeatures() {
    }

    static void requireSupported(ResourceLocation feature) {
        if (!"minecraft".equals(feature.getResourceDomain()) || !FEATURES.contains(feature.getResourcePath())) {
            throw new IllegalArgumentException("Unsupported jigsaw feature " + feature);
        }
    }

    static boolean isAreaLoaded(World world, BlockPos position) {
        return world.isAreaLoaded(new BlockPos(position.getX() - 8, FFDHeightHooks.minY(world), position.getZ() - 8),
                new BlockPos(position.getX() + 8, FFDHeightHooks.maxYExclusive(world) - 1, position.getZ() + 8));
    }

    static boolean place(World world, ResourceLocation feature, BlockPos origin, Random random) {
        requireSupported(feature);
        if (!isAreaLoaded(world, origin)) throw new IllegalArgumentException("Jigsaw feature requires loaded chunks");
        String name = feature.getResourcePath();
        if (name.startsWith("pile_")) return pile(world, origin, random, name);
        if (name.equals("oak") || name.equals("acacia") || name.equals("spruce") || name.equals("pine")) {
            return tree(world, origin, random, name);
        }
        int count = "flower_plain".equals(name) ? 64 : "patch_berry_bush".equals(name) ? 96
                : "patch_cactus".equals(name) ? 10 : 32;
        int horizontal = "flower_plain".equals(name) ? 6 : 7;
        int vertical = "flower_plain".equals(name) ? 2 : 3;
        boolean placed = false;
        for (int attempt = 0; attempt < count; attempt++) {
            BlockPos position = origin.add(offset(random, horizontal), offset(random, vertical), offset(random, horizontal));
            if (!withinHeight(world, position) || !world.isAirBlock(position)) continue;
            if ("patch_cactus".equals(name)) {
                if (!Blocks.CACTUS.canBlockStay(world, position)) continue;
                int height = 1 + random.nextInt(random.nextInt(3) + 1);
                random.nextInt(4);
                for (int level = 0; level < height; level++) {
                    BlockPos next = position.up(level);
                    if (!withinHeight(world, next) || !world.isAirBlock(next)) break;
                    world.setBlockState(next, Blocks.CACTUS.getDefaultState(), 2);
                    placed = true;
                }
                continue;
            }
            IBlockState state;
            if ("patch_berry_bush".equals(name)) {
                if (world.getBlockState(position.down()).getBlock() != Blocks.GRASS) continue;
                state = FFDItems.effectiveBlockState(FFDBlocks.SWEET_BERRY_BUSH);
                state = property(state, "age", "3");
            } else if ("patch_taiga_grass".equals(name)) {
                state = Blocks.TALLGRASS.getStateFromMeta(random.nextInt(5) == 0 ? 1 : 2);
            } else state = flower(position, random);
            if (state == null || !(state.getBlock() instanceof BlockBush)
                    || !((BlockBush) state.getBlock()).canBlockStay(world, position, state)) continue;
            world.setBlockState(position, state, 2);
            placed = true;
        }
        return placed;
    }

    private static int offset(Random random, int spread) {
        return random.nextInt(spread + 1) + random.nextInt(spread + 1) - spread;
    }

    private static IBlockState property(IBlockState state, String name, String value) {
        if (state == null) return null;
        IProperty<?> property = state.getBlock().getBlockState().getProperty(name);
        if (property == null) return state;
        return assign(state, property, value);
    }

    private static <Value extends Comparable<Value>> IBlockState assign(IBlockState state, IProperty<Value> property, String value) {
        com.google.common.base.Optional<Value> parsed = property.parseValue(value);
        return parsed.isPresent() ? state.withProperty(property, parsed.get()) : state;
    }

    private static IBlockState flower(BlockPos position, Random random) {
        String name;
        if (FLOWER_NOISE.sample(position.getX() * 0.005F, position.getY() * 0.005F,
                position.getZ() * 0.005F) < -0.8F) name = LOW_FLOWERS[random.nextInt(LOW_FLOWERS.length)];
        else if (random.nextFloat() < 0.33333334F) name = HIGH_FLOWERS[random.nextInt(HIGH_FLOWERS.length)];
        else return Blocks.YELLOW_FLOWER.getDefaultState();
        if ("cornflower".equals(name)) return FFDItems.effectiveBlockState(FFDBlocks.CORNFLOWER);
        return property(Blocks.RED_FLOWER.getDefaultState(), "type", name);
    }

    private static boolean pile(World world, BlockPos origin, Random random, String name) {
        if (origin.getY() < FFDHeightHooks.minY(world) + 5) return false;
        int radiusX = 2 + random.nextInt(2);
        int radiusZ = 2 + random.nextInt(2);
        for (BlockPos position : BlockPos.getAllInBox(origin.add(-radiusX, 0, -radiusZ), origin.add(radiusX, 1, radiusZ))) {
            int deltaX = origin.getX() - position.getX();
            int deltaZ = origin.getZ() - position.getZ();
            if (deltaX * deltaX + deltaZ * deltaZ > random.nextFloat() * 10.0F - random.nextFloat() * 6.0F
                    && random.nextFloat() >= 0.031D) continue;
            if (!withinHeight(world, position) || !world.isAirBlock(position)) continue;
            BlockPos below = position.down();
            IBlockState support = world.getBlockState(below);
            boolean supported = support.getBlock() == Blocks.GRASS_PATH ? random.nextBoolean()
                    : support.isSideSolid(world, below, EnumFacing.UP);
            if (!supported) continue;
            IBlockState state;
            switch (name) {
                case "pile_hay":
                    state = property(Blocks.HAY_BLOCK.getDefaultState(), "axis", EnumFacing.Axis.values()[random.nextInt(3)].getName());
                    break;
                case "pile_ice":
                    state = random.nextInt(6) == 0 ? FFDItems.effectiveBlockState(FFDBlocks.BLUE_ICE) : null;
                    if (state == null) state = Blocks.PACKED_ICE.getDefaultState();
                    break;
                case "pile_melon": state = Blocks.MELON_BLOCK.getDefaultState(); break;
                case "pile_pumpkin":
                    state = random.nextInt(20) == 19 ? Blocks.LIT_PUMPKIN.getDefaultState()
                            : FFDItems.effectiveBlockState(FFDBlocks.PUMPKIN);
                    if (state == null) state = Blocks.PUMPKIN.getDefaultState();
                    break;
                default: state = Blocks.SNOW_LAYER.getDefaultState();
            }
            world.setBlockState(position, state, 2);
        }
        return true;
    }

    private static boolean tree(World world, BlockPos origin, Random random, String type) {
        boolean acacia = "acacia".equals(type);
        boolean spruce = "spruce".equals(type);
        boolean pine = "pine".equals(type);
        int height = (acacia || spruce ? 5 : pine ? 6 : 4) + random.nextInt(pine ? 5 : 3)
                + random.nextInt(acacia ? 3 : spruce ? 2 : 1);
        int foliageHeight = acacia ? 0 : spruce ? Math.max(4, height - 1 - random.nextInt(2))
                : pine ? 3 + random.nextInt(2) : 3;
        int radius = spruce ? 2 + random.nextInt(2) : pine ? 1 + random.nextInt(Math.max(height - foliageHeight + 1, 1)) : 2;
        if (origin.getY() < FFDHeightHooks.minY(world) + 1 || origin.getY() + height + 1 > FFDHeightHooks.maxYExclusive(world)) return false;
        IBlockState sapling = Blocks.SAPLING.getStateFromMeta(acacia ? 4 : spruce || pine ? 1 : 0);
        if (!((BlockSapling) Blocks.SAPLING).canBlockStay(world, origin, sapling)) return false;
        for (int vertical = 0; vertical <= height + 1; vertical++) {
            int scan = vertical < (spruce || pine ? 2 : 1) ? 0 : acacia || spruce || pine ? 2 : 1;
            for (int deltaX = -scan; deltaX <= scan; deltaX++) {
                for (int deltaZ = -scan; deltaZ <= scan; deltaZ++) {
                    BlockPos position = origin.add(deltaX, vertical, deltaZ);
                    if (!replaceable(world, position) && !(world.getBlockState(position).getBlock() instanceof BlockLog)) return false;
                }
            }
        }
        IBlockState soil = world.getBlockState(origin.down());
        if (soil.getBlock() != Blocks.DIRT && soil.getBlock() != Blocks.GRASS && soil.getBlock() != Blocks.MYCELIUM) {
            world.setBlockState(origin.down(), Blocks.DIRT.getDefaultState(), 2);
        }
        IBlockState log = (acacia ? Blocks.LOG2 : Blocks.LOG).getStateFromMeta(acacia ? 0 : spruce || pine ? 1 : 0);
        IBlockState leaves = (acacia ? Blocks.LEAVES2 : Blocks.LEAVES).getStateFromMeta(acacia ? 0 : spruce || pine ? 1 : 0)
                .withProperty(BlockLeaves.CHECK_DECAY, false);
        if (acacia) {
            EnumFacing lean = HORIZONTAL[random.nextInt(4)];
            int leanHeight = height - random.nextInt(4) - 1;
            int leanSteps = 3 - random.nextInt(3);
            BlockPos last = null;
            int offsetX = 0;
            int offsetZ = 0;
            for (int vertical = 0; vertical < height; vertical++) {
                if (vertical >= leanHeight && leanSteps > 0) {
                    offsetX += lean.getFrontOffsetX();
                    offsetZ += lean.getFrontOffsetZ();
                    leanSteps--;
                }
                BlockPos position = origin.add(offsetX, vertical, offsetZ);
                if (putLog(world, position, log)) last = position.up();
            }
            BlockPos firstFoliage = last;
            EnumFacing branch = HORIZONTAL[random.nextInt(4)];
            last = null;
            if (branch != lean) {
                int start = leanHeight - random.nextInt(2) - 1;
                int steps = 1 + random.nextInt(3);
                offsetX = 0;
                offsetZ = 0;
                for (int vertical = start; vertical < height && steps > 0; vertical++, steps--) {
                    if (vertical < 1) continue;
                    offsetX += branch.getFrontOffsetX();
                    offsetZ += branch.getFrontOffsetZ();
                    BlockPos position = origin.add(offsetX, vertical, offsetZ);
                    if (putLog(world, position, log)) last = position.up();
                }
            }
            if (firstFoliage != null) acaciaLeaves(world, firstFoliage, 1, leaves, random);
            if (last != null) acaciaLeaves(world, last, 0, leaves, random);
            return true;
        }
        for (int vertical = 0; vertical < height; vertical++) putLog(world, origin.up(vertical), log);
        BlockPos attachment = origin.up(height);
        if (spruce) {
            int offset = random.nextInt(3);
            int currentRadius = random.nextInt(2);
            int maxRadius = 1;
            int minRadius = 0;
            for (int vertical = offset; vertical >= -foliageHeight; vertical--) {
                leavesRow(world, attachment, currentRadius, vertical, leaves, random, 1);
                if (currentRadius >= maxRadius) {
                    currentRadius = minRadius;
                    minRadius = 1;
                    maxRadius = Math.min(maxRadius + 1, radius);
                } else currentRadius++;
            }
        } else if (pine) {
            int currentRadius = 0;
            for (int vertical = 1; vertical >= 1 - foliageHeight; vertical--) {
                leavesRow(world, attachment, currentRadius, vertical, leaves, random, 1);
                if (currentRadius >= 1 && vertical == 2 - foliageHeight) currentRadius--;
                else if (currentRadius < radius) currentRadius++;
            }
        } else {
            for (int vertical = 0; vertical >= -foliageHeight; vertical--) {
                leavesRow(world, attachment, Math.max(radius - 1 - vertical / 2, 0), vertical, leaves, random, 0);
            }
        }
        return true;
    }

    private static void acaciaLeaves(World world, BlockPos origin, int extra, IBlockState state, Random random) {
        leavesRow(world, origin, 2 + extra, -1, state, random, 2);
        leavesRow(world, origin, 1, 0, state, random, 2);
        leavesRow(world, origin, 1 + extra, 0, state, random, 2);
    }

    private static boolean putLog(World world, BlockPos position, IBlockState state) {
        if (!replaceable(world, position)) return false;
        world.setBlockState(position, state, 2);
        return true;
    }

    private static void leavesRow(World world, BlockPos origin, int radius, int vertical, IBlockState state, Random random, int shape) {
        for (int deltaX = -radius; deltaX <= radius; deltaX++) {
            for (int deltaZ = -radius; deltaZ <= radius; deltaZ++) {
                int absoluteX = Math.abs(deltaX);
                int absoluteZ = Math.abs(deltaZ);
                boolean skip = shape == 2 && vertical == 0
                        ? (absoluteX > 1 || absoluteZ > 1) && absoluteX != 0 && absoluteZ != 0
                        : absoluteX == radius && absoluteZ == radius && (shape == 0
                                ? random.nextInt(2) == 0 || vertical == 0 : radius > 0);
                if (skip) continue;
                BlockPos position = origin.add(deltaX, vertical, deltaZ);
                IBlockState previous = world.getBlockState(position);
                if (!replaceable(world, position) || previous.getBlock() instanceof BlockLeaves
                        && !previous.getValue(BlockLeaves.DECAYABLE)) continue;
                boolean water = WaterloggedBlockApi.isWaterSource(world, position);
                world.setBlockState(position, state, 2);
                if (water) WaterloggedBlockApi.setContainedFluid(world, position, state, net.minecraftforge.fluids.FluidRegistry.WATER, 2);
            }
        }
    }

    private static boolean replaceable(World world, BlockPos position) {
        IBlockState state = world.getBlockState(position);
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block.isAir(state, world, position) || block.isLeaves(state, world, position)
                || material == Material.WATER || block == Blocks.VINE || block == Blocks.DEADBUSH
                || block instanceof BlockFlower || block == Blocks.TALLGRASS || block == Blocks.DOUBLE_PLANT
                || block == FFDBlocks.CORNFLOWER;
    }

    private static boolean withinHeight(World world, BlockPos position) {
        return position.getY() >= FFDHeightHooks.minY(world) && position.getY() < FFDHeightHooks.maxYExclusive(world);
    }
}
