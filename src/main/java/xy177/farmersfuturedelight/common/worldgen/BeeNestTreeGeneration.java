package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDWorldGenerator;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;

public final class BeeNestTreeGeneration {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private BeeNestTreeGeneration() {
    }

    public static boolean generateNatural(WorldGenerator generator, World world,
                                           Random random, BlockPos origin) {
        return generate(generator, world, random, origin, naturalChance(world.getBiome(origin)));
    }

    public static boolean generateSapling(WorldGenerator generator, World world,
                                           Random random, BlockPos origin) {
        return generate(generator, world, random, origin,
                enabled(world) && FFDWorldGenerator.hasNearbyFlower(world, origin) ? 0.05F : 0.0F);
    }

    public static boolean generate(WorldGenerator generator, World world, Random random,
                                     BlockPos origin, float probability) {
        if (!enabled(world) || probability <= 0.0F) {
            return generator.generate(world, random, origin);
        }
        Context parent = CURRENT.get();
        Context context = new Context(world);
        CURRENT.set(context);
        boolean generated;
        try {
            generated = generator.generate(world, random, origin);
        } finally {
            if (parent == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(parent);
            }
        }
        if (generated) {
            context.decorate(random, probability);
        }
        return generated;
    }

    public static void recordPlacement(World world, BlockPos pos, IBlockState requested) {
        Context context = CURRENT.get();
        if (context == null || context.world != world || world.getBlockState(pos) != requested) {
            return;
        }
        if (requested.getBlock().isWood(world, pos)) {
            context.logs.add(pos.toImmutable());
        } else if (requested.getBlock().isLeaves(requested, world, pos)) {
            context.leaves.add(pos.toImmutable());
        }
    }

    private static boolean enabled(World world) {
        return !world.isRemote && FFDItems.isBlockRegistered(FFDBlocks.BEE_NEST)
                && FFDEntities.isLocalBeeEnabled();
    }

    private static float naturalChance(Biome biome) {
        ResourceLocation id = biome.getRegistryName();
        if (id == null) return 0.0F;
        String path = id.getResourcePath();
        if ("minecraft".equals(id.getResourceDomain())) {
            if ("mutated_forest".equals(path)) return FFDConfig.beeNestFlowerForestChance;
            if ("plains".equals(path) || "mutated_plains".equals(path)) {
                return FFDConfig.beeNestPlainsChance;
            }
            if ("forest".equals(path) || "forest_hills".equals(path)
                    || "birch_forest".equals(path) || "birch_forest_hills".equals(path)
                    || "mutated_birch_forest".equals(path)
                    || "mutated_birch_forest_hills".equals(path)) {
                return FFDConfig.beeNestForestChance;
            }
            return 0.0F;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.PLAINS)) {
            return FFDConfig.beeNestPlainsChance;
        }
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)
                ? FFDConfig.beeNestForestChance : 0.0F;
    }

    private static final class Context {
        private final World world;
        private final Set<BlockPos> logs = new LinkedHashSet<>();
        private final Set<BlockPos> leaves = new LinkedHashSet<>();

        private Context(World world) {
            this.world = world;
        }

        private void decorate(Random random, float probability) {
            if (logs.isEmpty() || leaves.stream().noneMatch(
                    pos -> BeeNestLeaves.matches(world.getBlockState(pos)))
                    || random.nextFloat() >= probability) return;
            int lowestLog = logs.stream().mapToInt(BlockPos::getY).min().getAsInt();
            int lowestLeaves = leaves.stream().mapToInt(BlockPos::getY).min().getAsInt();
            int hiveY = Math.max(lowestLeaves - 1, lowestLog + 1);
            List<BlockPos> candidates = new ArrayList<>();
            for (BlockPos log : logs) {
                if (log.getY() != hiveY) continue;
                candidates.add(log.south());
                candidates.add(log.west());
                candidates.add(log.east());
            }
            Collections.shuffle(candidates, random);
            for (BlockPos candidate : candidates) {
                if (!world.isAirBlock(candidate) || !world.isAirBlock(candidate.south())) continue;
                if (!world.setBlockState(candidate, FFDBlocks.BEE_NEST.getDefaultState()
                        .withProperty(BlockBeehive.FACING, EnumFacing.SOUTH), 2 | 16)) continue;
                TileEntity tile = world.getTileEntity(candidate);
                if (tile instanceof TileEntityBeehive) {
                    int count = FFDConfig.beeNestMinBees + random.nextInt(
                            FFDConfig.beeNestMaxBees - FFDConfig.beeNestMinBees + 1);
                    ((TileEntityBeehive) tile).addNewBees(count, random);
                }
                return;
            }
        }
    }
}
