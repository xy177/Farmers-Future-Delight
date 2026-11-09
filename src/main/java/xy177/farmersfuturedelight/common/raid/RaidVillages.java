package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;
import xy177.farmersfuturedelight.common.registry.FFDWorkstationData;

public final class RaidVillages {
    private RaidVillages() {
    }

    public static boolean isVillage(WorldServer world, BlockPos position) {
        return !occupied(world, position, 1).isEmpty();
    }

    public static boolean isCloseToVillage(WorldServer world, BlockPos position, int sectionDistance) {
        return sectionDistance >= 0 && sectionDistance <= 6
                && !occupied(world, position, sectionDistance).isEmpty();
    }

    public static BlockPos center(WorldServer world, BlockPos origin) {
        long totalX = 0;
        long totalY = 0;
        long totalZ = 0;
        int count = 0;
        for (BlockPos position : occupied(world, origin, 4)) {
            if (position.distanceSq(origin) > 4096.0D) continue;
            totalX += position.getX();
            totalY += position.getY();
            totalZ += position.getZ();
            count++;
        }
        return count == 0 ? origin.toImmutable()
                : new BlockPos((double) totalX / count, (double) totalY / count, (double) totalZ / count);
    }

    public static List<BlockPos> occupied(WorldServer world, BlockPos origin, int sectionRadius) {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos position : FFDWorkstationData.getHomes(world).getClaimedPositions(origin, sectionRadius)) {
            if (!world.isBlockLoaded(position) || FFDVillagerHomes.isBedHead(world.getBlockState(position))) {
                positions.add(position);
            }
        }
        for (BlockPos position : FFDWorkstationData.get(world).getClaimedPositions(origin, sectionRadius)) {
            if (!world.isBlockLoaded(position)
                    || !FFDProfessionWorkstations.findMappings(world.getBlockState(position)).isEmpty()) {
                positions.add(position);
            }
        }
        int radius = sectionRadius * 16 + 16;
        for (EntityVillager villager : world.getEntitiesWithinAABB(EntityVillager.class,
                new AxisAlignedBB(origin).grow(radius))) {
            BlockPos meeting = FFDVillagerRoutines.getMeeting(villager);
            if (meeting != null && inSections(origin, meeting, sectionRadius)) positions.add(meeting);
        }
        return new ArrayList<>(positions);
    }

    private static boolean inSections(BlockPos origin, BlockPos position, int radius) {
        return Math.abs((origin.getX() >> 4) - (position.getX() >> 4)) <= radius
                && Math.abs((origin.getY() >> 4) - (position.getY() >> 4)) <= radius
                && Math.abs((origin.getZ() >> 4) - (position.getZ() >> 4)) <= radius;
    }
}
