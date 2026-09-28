package xy177.farmersfuturedelight.common.worldgen;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FFDOceanStructureLocator {
    private static Locator locator;

    private FFDOceanStructureLocator() {
    }

    public static void setLocator(@Nullable Locator next) {
        locator = next;
    }

    @Nullable
    public static BlockPos findDolphinTreasure(World world, BlockPos origin, int radius) {
        return locator == null ? null : locator.findDolphinTreasure(world, origin, radius);
    }

    @Nullable
    public static BlockPos findBuriedTreasure(World world, BlockPos origin, int radius) {
        return locator == null ? null : locator.findBuriedTreasure(world, origin, radius);
    }

    public interface Locator {
        @Nullable
        BlockPos findDolphinTreasure(World world, BlockPos origin, int radius);

        @Nullable
        BlockPos findBuriedTreasure(World world, BlockPos origin, int radius);
    }
}
