package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeSampler;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class CommandLocateBiome extends CommandBase {
    private static final int MAX_RADIUS = 6400;
    private static final int HORIZONTAL_STEP = 32;
    private static final int VERTICAL_STEP = 16;
    private static final String LUSH_CAVES = FarmerFutureDelight.MODID + ":lush_caves";
    private static final String DRIPSTONE_CAVES = FarmerFutureDelight.MODID + ":dripstone_caves";

    @Override
    public String getName() {
        return "locatebiome";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.farmers_future_delight.locatebiome.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        String requested = normalize(args[0]);
        FFDVerticalBiome verticalTarget = caveTarget(requested);
        Biome surfaceTarget = verticalTarget == null
                ? ForgeRegistries.BIOMES.getValue(new ResourceLocation(requested)) : null;
        if (verticalTarget == null && surfaceTarget == null) {
            throw new CommandException(
                    "commands.farmers_future_delight.locatebiome.invalid", requested);
        }

        World world = sender.getEntityWorld();
        if (verticalTarget != null && !FFDHeightHooks.isExtended(world)) {
            throw new CommandException(
                    "commands.farmers_future_delight.locatebiome.extended_only", requested);
        }

        BlockPos origin = sender.getPosition();
        BlockPos found = verticalTarget == null
                ? findSurfaceBiome(world, origin, surfaceTarget)
                : findCaveBiome(world, origin, verticalTarget);
        if (found == null) {
            throw new CommandException(
                    "commands.farmers_future_delight.locatebiome.not_found", requested,
                    MAX_RADIUS);
        }

        int distance = (int) Math.round(Math.sqrt(origin.distanceSq(found)));
        notifyCommandListener(sender, this,
                "commands.farmers_future_delight.locatebiome.success",
                requested, found.getX(), found.getY(), found.getZ(), distance);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, @Nullable BlockPos targetPos) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (biome.getRegistryName() != null) {
                ids.add(biome.getRegistryName().toString());
            }
        }
        ids.add(LUSH_CAVES);
        ids.add(DRIPSTONE_CAVES);
        Collections.sort(ids);
        return getListOfStringsMatchingLastWord(args, ids);
    }

    @Nullable
    private static BlockPos findSurfaceBiome(World world, BlockPos origin, Biome target) {
        int originX = Math.floorDiv(origin.getX(), HORIZONTAL_STEP) * HORIZONTAL_STEP;
        int originZ = Math.floorDiv(origin.getZ(), HORIZONTAL_STEP) * HORIZONTAL_STEP;
        int rings = MAX_RADIUS / HORIZONTAL_STEP;
        for (int ring = 0; ring <= rings; ring++) {
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (ring != 0 && Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = originX + dx * HORIZONTAL_STEP;
                    int z = originZ + dz * HORIZONTAL_STEP;
                    if (world.getBiomeProvider().getBiome(new BlockPos(x, 0, z)) != target) {
                        continue;
                    }
                    int y = surfaceY(world, x, z);
                    BlockPos candidate = new BlockPos(x, y, z);
                    double distance = origin.distanceSq(candidate);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = candidate;
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos findCaveBiome(World world, BlockPos origin,
                                           FFDVerticalBiome target) {
        FFDVerticalBiomeSampler sampler =
                new FFDVerticalBiomeSampler(new FFDModernWorldgenData(world.getSeed()));
        int originX = Math.floorDiv(origin.getX(), HORIZONTAL_STEP) * HORIZONTAL_STEP;
        int originZ = Math.floorDiv(origin.getZ(), HORIZONTAL_STEP) * HORIZONTAL_STEP;
        int rings = MAX_RADIUS / HORIZONTAL_STEP;
        for (int ring = 0; ring <= rings; ring++) {
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (ring != 0 && Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = originX + dx * HORIZONTAL_STEP;
                    int z = originZ + dz * HORIZONTAL_STEP;
                    for (int y = FFDHeightHooks.MIN_Y;
                         y < FFDHeightHooks.MAX_Y_EXCLUSIVE; y += VERTICAL_STEP) {
                        FFDVerticalBiome sampled = sampler.sampleNoiseBiome(
                                Math.floorDiv(x, FFDVerticalBiomeData.CELL_SIZE),
                                Math.floorDiv(y, FFDVerticalBiomeData.CELL_SIZE),
                                Math.floorDiv(z, FFDVerticalBiomeData.CELL_SIZE));
                        if (sampled != target) {
                            continue;
                        }
                        BlockPos candidate = new BlockPos(x, y, z);
                        double distance = origin.distanceSq(candidate);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = candidate;
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private static int surfaceY(World world, int x, int z) {
        if (world.getBiomeProvider() instanceof FFDModernBiomeProvider) {
            FFDModernWorldgenData data =
                    ((FFDModernBiomeProvider) world.getBiomeProvider()).worldgenData();
            int y = data.preliminarySurfaceLevel(x, z);
            return Math.max(FFDHeightHooks.minY(world),
                    Math.min(FFDHeightHooks.maxYExclusive(world) - 1, y));
        }
        return world.getSeaLevel();
    }

    @Nullable
    private static FFDVerticalBiome caveTarget(String id) {
        if (LUSH_CAVES.equals(id)) {
            return FFDVerticalBiome.LUSH_CAVES;
        }
        if (DRIPSTONE_CAVES.equals(id)) {
            return FFDVerticalBiome.DRIPSTONE_CAVES;
        }
        return null;
    }

    private static String normalize(String id) {
        return id.indexOf(':') < 0 ? "minecraft:" + id : id;
    }
}
