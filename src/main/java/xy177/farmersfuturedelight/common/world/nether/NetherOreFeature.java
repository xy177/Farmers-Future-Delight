package xy177.farmersfuturedelight.common.world.nether;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Context;
import xy177.farmersfuturedelight.common.world.nether.NetherFeaturePlacement.Feature;

final class NetherOreFeature implements Feature {
    private final int size;
    private final float discard;
    private final boolean scattered;
    private final List<Set<Block>> targets = new ArrayList<>();
    private final List<IBlockState> states = new ArrayList<>();

    NetherOreFeature(JsonObject definition, boolean scattered) {
        size = definition.get("size").getAsInt();
        discard = definition.has("discard_chance_on_air_exposure") ? definition.get("discard_chance_on_air_exposure").getAsFloat() : 0;
        this.scattered = scattered;
        for (JsonElement element : definition.getAsJsonArray("targets")) {
            JsonObject entry = element.getAsJsonObject();
            JsonObject predicate = entry.getAsJsonObject("target");
            if (predicate.has("block")) targets.add(NetherFeaturePlacement.blocks(predicate.get("block")));
            else targets.add(NetherFeaturePlacement.blocks(new com.google.gson.JsonPrimitive("#" + predicate.get("tag").getAsString())));
            states.add(NetherFeaturePlacement.state(entry.get("state")));
        }
    }

    @Override
    public void place(Context context, Random random, BlockPos pos) {
        if (scattered) {
            int count = random.nextInt(size + 1);
            for (int attempt = 0; attempt < count; attempt++) {
                int spread = Math.min(attempt, 7);
                BlockPos candidate = pos.add(Math.round((random.nextFloat() - random.nextFloat()) * spread),
                        Math.round((random.nextFloat() - random.nextFloat()) * spread),
                        Math.round((random.nextFloat() - random.nextFloat()) * spread));
                replace(context, random, candidate);
            }
            return;
        }
        float angle = random.nextFloat() * (float) Math.PI;
        float spread = size / 8.0F;
        int radius = MathHelper.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double startX = pos.getX() + Math.sin(angle) * spread;
        double endX = pos.getX() - Math.sin(angle) * spread;
        double startZ = pos.getZ() + Math.cos(angle) * spread;
        double endZ = pos.getZ() - Math.cos(angle) * spread;
        double startY = pos.getY() + random.nextInt(3) - 2;
        double endY = pos.getY() + random.nextInt(3) - 2;
        int minimumX = pos.getX() - MathHelper.ceil(spread) - radius;
        int minimumY = pos.getY() - 2 - radius;
        int minimumZ = pos.getZ() - MathHelper.ceil(spread) - radius;
        int width = 2 * (MathHelper.ceil(spread) + radius);
        int height = 2 * (2 + radius);
        BitSet visited = new BitSet(width * width * height);
        double[] spheres = new double[size * 4];
        for (int index = 0; index < size; index++) {
            float fraction = index / (float) size;
            spheres[index * 4] = startX + fraction * (endX - startX);
            spheres[index * 4 + 1] = startY + fraction * (endY - startY);
            spheres[index * 4 + 2] = startZ + fraction * (endZ - startZ);
            double scale = random.nextDouble() * size / 16.0D;
            spheres[index * 4 + 3] = ((MathHelper.sin((float) Math.PI * fraction) + 1.0F) * scale + 1) / 2;
        }
        for (int first = 0; first < size - 1; first++) {
            if (spheres[first * 4 + 3] <= 0) continue;
            for (int second = first + 1; second < size; second++) {
                if (spheres[second * 4 + 3] <= 0) continue;
                double deltaRadius = spheres[first * 4 + 3] - spheres[second * 4 + 3];
                double deltaX = spheres[first * 4] - spheres[second * 4];
                double deltaY = spheres[first * 4 + 1] - spheres[second * 4 + 1];
                double deltaZ = spheres[first * 4 + 2] - spheres[second * 4 + 2];
                if (deltaRadius * deltaRadius > deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) spheres[(deltaRadius > 0 ? second : first) * 4 + 3] = -1;
            }
        }
        for (int index = 0; index < size; index++) {
            double sphereRadius = spheres[index * 4 + 3];
            if (sphereRadius < 0) continue;
            double centerX = spheres[index * 4];
            double centerY = spheres[index * 4 + 1];
            double centerZ = spheres[index * 4 + 2];
            int minX = Math.max(MathHelper.floor(centerX - sphereRadius), minimumX);
            int minY = Math.max(MathHelper.floor(centerY - sphereRadius), minimumY);
            int minZ = Math.max(MathHelper.floor(centerZ - sphereRadius), minimumZ);
            int maxX = Math.max(MathHelper.floor(centerX + sphereRadius), minX);
            int maxY = Math.max(MathHelper.floor(centerY + sphereRadius), minY);
            int maxZ = Math.max(MathHelper.floor(centerZ + sphereRadius), minZ);
            for (int blockX = minX; blockX <= maxX; blockX++) {
                double normalizedX = (blockX + .5D - centerX) / sphereRadius;
                if (normalizedX * normalizedX >= 1) continue;
                for (int blockY = minY; blockY <= maxY; blockY++) {
                    double normalizedY = (blockY + .5D - centerY) / sphereRadius;
                    if (normalizedX * normalizedX + normalizedY * normalizedY >= 1) continue;
                    for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                        double normalizedZ = (blockZ + .5D - centerZ) / sphereRadius;
                        if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ >= 1) continue;
                        int bit = blockX - minimumX + (blockY - minimumY) * width + (blockZ - minimumZ) * width * height;
                        if (visited.get(bit)) continue;
                        visited.set(bit);
                        replace(context, random, new BlockPos(blockX, blockY, blockZ));
                    }
                }
            }
        }
    }

    private void replace(Context context, Random random, BlockPos pos) {
        if (!context.available(pos)) return;
        Block current = context.get(pos).getBlock();
        for (int index = 0; index < targets.size(); index++) {
            if (states.get(index) == null || !targets.get(index).contains(current)) continue;
            if (discard > 0 && (discard >= 1 || random.nextFloat() < discard)) {
                boolean exposed = false;
                for (EnumFacing direction : EnumFacing.values()) if (context.get(pos.offset(direction)).getMaterial() == Material.AIR) { exposed = true; break; }
                if (exposed) continue;
            }
            context.set(pos, states.get(index));
            return;
        }
    }
}
