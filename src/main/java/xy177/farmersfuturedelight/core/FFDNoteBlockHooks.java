package xy177.farmersfuturedelight.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDNoteBlockHooks {
    private FFDNoteBlockHooks() {
    }

    public static SoundEvent instrument(SoundEvent original, World world, BlockPos pos, int instrument) {
        Block base = world.getBlockState(pos.down()).getBlock();
        if (base == Blocks.IRON_BLOCK && instrument == 0) {
            return FFDSounds.NOTE_IRON_XYLOPHONE;
        }
        if (base == Blocks.SOUL_SAND && instrument == 2) {
            return FFDSounds.NOTE_COW_BELL;
        }
        if (base == FFDBlocks.PUMPKIN && instrument == 0) {
            return FFDSounds.NOTE_DIDGERIDOO;
        }
        if (base == Blocks.EMERALD_BLOCK && instrument == 0) {
            return FFDSounds.NOTE_BIT;
        }
        if (base == Blocks.HAY_BLOCK && instrument == 0) {
            return FFDSounds.NOTE_BANJO;
        }
        if (base == Blocks.GLOWSTONE && instrument == 3) {
            return FFDSounds.NOTE_PLING;
        }
        return original;
    }
}
