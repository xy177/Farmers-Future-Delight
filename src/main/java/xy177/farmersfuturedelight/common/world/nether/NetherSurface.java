package xy177.farmersfuturedelight.common.world.nether;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherBlockProvider;

public final class NetherSurface {
    private final NetherBiomeSource biomes;
    private final long positionalSeed;
    private final long bedrockFloorSeed;
    private final long bedrockRoofSeed;
    private final NetherMaterialNoise depth;
    private final NetherMaterialNoise patch;
    private final NetherMaterialNoise soul;
    private final NetherMaterialNoise gravel;
    private final NetherMaterialNoise netherrack;
    private final NetherMaterialNoise wart;
    private final NetherMaterialNoise selector;

    public NetherSurface(long seed, NetherBiomeSource biomes) {
        this.biomes = biomes;
        positionalSeed = new Random(seed).nextLong();
        bedrockFloorSeed = new Random(positionalSeed ^ (long) "minecraft:bedrock_floor".hashCode()).nextLong();
        bedrockRoofSeed = new Random(positionalSeed ^ (long) "minecraft:bedrock_roof".hashCode()).nextLong();
        depth = new NetherMaterialNoise(seed, "surface");
        patch = new NetherMaterialNoise(seed, "patch");
        soul = new NetherMaterialNoise(seed, "soul_sand_layer");
        gravel = new NetherMaterialNoise(seed, "gravel_layer");
        netherrack = new NetherMaterialNoise(seed, "netherrack");
        wart = new NetherMaterialNoise(seed, "nether_wart");
        selector = new NetherMaterialNoise(seed, "nether_state_selector");
    }

    public void apply(ChunkPrimer primer, int chunkX, int chunkZ) {
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockX = chunkX * 16 + localX;
                int blockZ = chunkZ * 16 + localZ;
                int surfaceDepth = (int) (depth.sample(blockX, blockZ) * 2.75D + 3.0D
                        + new Random(NetherMaterialNoise.positionSeed(blockX, 0, blockZ) ^ positionalSeed).nextDouble() * .25D);
                boolean patchLayer = patch.sample(blockX, blockZ) >= -.012D;
                boolean soulLayer = soul.sample(blockX, blockZ) >= -.012D;
                boolean gravelLayer = gravel.sample(blockX, blockZ) >= -.012D;
                boolean plain = netherrack.sample(blockX, blockZ) >= .54D;
                boolean wartLayer = wart.sample(blockX, blockZ) >= 1.17D;
                boolean first = selector.sample(blockX, blockZ) >= 0;
                int above = 0;
                int nextCeiling = Integer.MAX_VALUE;
                for (int blockY = 127; blockY >= 0; blockY--) {
                    IBlockState state = primer.getBlockState(localX, blockY, localZ);
                    if (state.getMaterial() == net.minecraft.block.material.Material.AIR) {
                        above = 0;
                        continue;
                    }
                    if (state.getMaterial() == net.minecraft.block.material.Material.LAVA) continue;
                    if (nextCeiling >= blockY) {
                        nextCeiling = -2032;
                        for (int below = blockY - 1; below >= -1; below--) {
                            if (below >= 0) {
                                net.minecraft.block.material.Material material = primer.getBlockState(localX, below, localZ).getMaterial();
                                if (material != net.minecraft.block.material.Material.AIR
                                        && material != net.minecraft.block.material.Material.LAVA) continue;
                            }
                            nextCeiling = below + 1;
                            break;
                        }
                    }
                    above++;
                    IBlockState replacement;
                    if (blockY <= 0 || blockY < 5 && random(blockX, blockY, blockZ, bedrockFloorSeed) < (5 - blockY) / 5.0D
                            || blockY >= 127 || blockY > 122 && random(blockX, blockY, blockZ, bedrockRoofSeed) >= (127 - blockY) / 5.0D) {
                        replacement = Blocks.BEDROCK.getDefaultState();
                    } else if (blockY >= 122) {
                        replacement = Blocks.NETHERRACK.getDefaultState();
                    } else {
                        int below = blockY - nextCeiling + 1;
                        if (above > 1 && above > 1 + surfaceDepth && below > 1 + surfaceDepth) {
                            replacement = Blocks.NETHERRACK.getDefaultState();
                        } else {
                            FFDVerticalBiome biome = biomes.get(blockX, blockY, blockZ);
                            replacement = material(biome, blockY, above, below, surfaceDepth,
                                    patchLayer, soulLayer, gravelLayer, plain, wartLayer, first);
                        }
                    }
                    primer.setBlockState(localX, blockY, localZ, replacement);
                }
            }
        }
    }

    private static float random(int blockX, int blockY, int blockZ, long seed) {
        return new Random(NetherMaterialNoise.positionSeed(blockX, blockY, blockZ) ^ seed).nextFloat();
    }

    private static IBlockState material(FFDVerticalBiome biome, int blockY, int above, int below,
                                        int depth, boolean patch, boolean soul, boolean gravel,
                                        boolean plain, boolean wart, boolean first) {
        boolean underFloor = above <= 1 + depth;
        boolean underCeiling = below <= 1 + depth;
        boolean band = blockY + above >= 30 && blockY + above < 35;
        if (biome == FFDVerticalBiome.BASALT_DELTAS) {
            if (underCeiling) return block(FFDBlocks.BASALT);
            if (underFloor) return patch && band ? Blocks.GRAVEL.getDefaultState()
                    : block(first ? FFDBlocks.BASALT : FFDBlocks.BLACKSTONE);
        }
        if (biome == FFDVerticalBiome.SOUL_SAND_VALLEY && (underCeiling || underFloor)) {
            if (!underCeiling && patch && band) return Blocks.GRAVEL.getDefaultState();
            return first ? Blocks.SOUL_SAND.getDefaultState() : block(FFDBlocks.SOUL_SOIL);
        }
        if (above == 1) {
            if (blockY < 32 && depth <= 0) return Blocks.LAVA.getDefaultState();
            if (blockY >= 31 && !plain && (biome == FFDVerticalBiome.CRIMSON_FOREST || biome == FFDVerticalBiome.WARPED_FOREST)) {
                boolean warped = biome == FFDVerticalBiome.WARPED_FOREST;
                IBlockState state = wart ? FFDNetherBlockProvider.get().wart(warped) : FFDNetherBlockProvider.get().nylium(warped);
                if (state != null) return state;
            }
        }
        if (biome == FFDVerticalBiome.NETHER_WASTES) {
            if (underFloor && soul) return depth > 0 && band ? Blocks.SOUL_SAND.getDefaultState() : Blocks.NETHERRACK.getDefaultState();
            if (above == 1 && blockY >= 31 && blockY + above < 35 && gravel && (blockY >= 32 || depth > 0)) return Blocks.GRAVEL.getDefaultState();
        }
        return Blocks.NETHERRACK.getDefaultState();
    }

    private static IBlockState block(net.minecraft.block.Block requested) {
        net.minecraft.block.Block effective = FFDItems.effectiveBlock(requested);
        return effective == null || effective == Blocks.AIR ? Blocks.NETHERRACK.getDefaultState() : effective.getDefaultState();
    }
}
