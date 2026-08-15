package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

/** Places the 26.3 lower fossil feature without adding a new structure type. */
public final class WorldGenModernFossils {
    private static final long FEATURE_SALT = 0x6F7A5B2D11L;
    private static final ResourceLocation[] FOSSILS = fossils("");
    private static final ResourceLocation[] FOSSILS_COAL = fossils("_coal");

    private final long worldSeed;
    private final long xSeed;
    private final long zSeed;
    private final IBlockState diamondOverlay;

    public WorldGenModernFossils(long worldSeed, FFDModernStoneProvider stones) {
        this.worldSeed = worldSeed;
        Random seedRandom = new Random(worldSeed);
        xSeed = odd(seedRandom.nextLong());
        zSeed = odd(seedRandom.nextLong());
        diamondOverlay = stones.diamondOre();
    }

    public void generateChunk(World world, int targetChunkX, int targetChunkZ) {
        if (!FFDHeightHooks.isExtended(world) || diamondOverlay == null
                || FFDConfig.modernFossilRarity <= 0) {
            return;
        }
        for (int sourceChunkX = targetChunkX - 1; sourceChunkX <= targetChunkX + 1; sourceChunkX++) {
            for (int sourceChunkZ = targetChunkZ - 1; sourceChunkZ <= targetChunkZ + 1; sourceChunkZ++) {
                Biome biome = world.getBiome(new BlockPos((sourceChunkX << 4) + 8, 0,
                        (sourceChunkZ << 4) + 8));
                if (!isFossilBiome(biome)) {
                    continue;
                }
                Random random = randomForChunk(sourceChunkX, sourceChunkZ);
                if (random.nextInt(FFDConfig.modernFossilRarity) != 0) {
                    continue;
                }
                int originX = (sourceChunkX << 4) + random.nextInt(16);
                int originZ = (sourceChunkZ << 4) + random.nextInt(16);
                int originY = between(random, FFDHeightHooks.MIN_Y, -8);
                place(world, new BlockPos(originX, originY, originZ),
                        targetChunkX, targetChunkZ, random);
            }
        }
    }

    private boolean place(World world, BlockPos origin, int targetChunkX, int targetChunkZ,
                          Random random) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return false;
        }
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        int index = random.nextInt(FOSSILS.length);
        TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
        Template fossil = manager.getTemplate(server, FOSSILS[index]);
        Template overlay = manager.getTemplate(server, FOSSILS_COAL[index]);
        if (fossil == null || overlay == null) {
            return false;
        }
        BlockPos size = fossil.transformedSize(rotation);
        BlockPos anchor = origin.add(-size.getX() / 2, 0, -size.getZ() / 2);
        int targetY = origin.getY();
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                int height = FFDHeightHooks.getWorldHeight(world,
                        anchor.getX() + x, anchor.getZ() + z);
                if (height > FFDHeightHooks.MIN_Y) {
                    targetY = Math.min(targetY, height);
                }
            }
        }
        targetY = Math.max(targetY - 15 - random.nextInt(10), FFDHeightHooks.MIN_Y + 10);
        BlockPos zero = fossil.getZeroPositionWithTransform(
                new BlockPos(anchor.getX(), targetY, anchor.getZ()),
                Mirror.NONE, rotation);

        BlockPos max = zero.add(size).add(-1, -1, -1);
        if (countEmptyCorners(world, zero, max) > 4) {
            return false;
        }

        StructureBoundingBox bounds = new StructureBoundingBox(
                targetChunkX << 4, FFDHeightHooks.MIN_Y, targetChunkZ << 4,
                (targetChunkX << 4) + 15, FFDHeightHooks.maxYExclusive(world) - 1,
                (targetChunkZ << 4) + 15);
        PlacementSettings settings = new PlacementSettings()
                .setRotation(rotation)
                .setBoundingBox(bounds)
                .setRandom(random)
                .setIntegrity(0.9F);
        fossil.addBlocksToWorld(world, zero, settings, 20);
        settings.setIntegrity(0.1F);
        final BlockRotationProcessor overlayIntegrity = new BlockRotationProcessor(zero, settings);
        ITemplateProcessor diamondProcessor = new ITemplateProcessor() {
            @Override
            public Template.BlockInfo processBlock(World processWorld, BlockPos processPos,
                                                    Template.BlockInfo info) {
                Template.BlockInfo accepted = overlayIntegrity.processBlock(
                        processWorld, processPos, info);
                if (accepted == null) {
                    return null;
                }
                return accepted.blockState.getBlock() == Blocks.COAL_ORE
                        ? new Template.BlockInfo(accepted.pos, diamondOverlay,
                        accepted.tileentityData) : accepted;
            }
        };
        overlay.addBlocksToWorld(world, zero, diamondProcessor, settings, 20);
        return true;
    }

    private static int countEmptyCorners(World world, BlockPos min, BlockPos max) {
        int empty = 0;
        for (int x : new int[]{min.getX(), max.getX()}) {
            for (int y : new int[]{min.getY(), max.getY()}) {
                for (int z : new int[]{min.getZ(), max.getZ()}) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isBlockLoaded(pos, false)) {
                        continue;
                    }
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (block == Blocks.AIR || block == Blocks.WATER || block == Blocks.FLOWING_WATER
                            || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                        empty++;
                    }
                }
            }
        }
        return empty;
    }

    private static boolean isFossilBiome(Biome biome) {
        if (biome == null) {
            return false;
        }
        if (biome instanceof BiomeDesert || biome instanceof BiomeSwamp) {
            return true;
        }
        ResourceLocation id = biome == null ? null : biome.getRegistryName();
        if (id == null) {
            return false;
        }
        String path = id.getResourcePath();
        return "desert".equals(path) || "desert_hills".equals(path)
                || "swampland".equals(path) || "mangrove_swamp".equals(path);
    }

    private Random randomForChunk(int chunkX, int chunkZ) {
        long seed = ((long) chunkX * xSeed + (long) chunkZ * zSeed) ^ worldSeed ^ FEATURE_SALT;
        return new Random(seed);
    }

    private static ResourceLocation[] fossils(String suffix) {
        return new ResourceLocation[]{
                new ResourceLocation("fossils/fossil_spine_01" + suffix),
                new ResourceLocation("fossils/fossil_spine_02" + suffix),
                new ResourceLocation("fossils/fossil_spine_03" + suffix),
                new ResourceLocation("fossils/fossil_spine_04" + suffix),
                new ResourceLocation("fossils/fossil_skull_01" + suffix),
                new ResourceLocation("fossils/fossil_skull_02" + suffix),
                new ResourceLocation("fossils/fossil_skull_03" + suffix),
                new ResourceLocation("fossils/fossil_skull_04" + suffix)
        };
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static long odd(long value) {
        return value / 2L * 2L + 1L;
    }

}
