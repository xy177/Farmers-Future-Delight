package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.FFDLodestoneCompass;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class BlockLodestone extends BlockFutureStone {
    public BlockLodestone() {
        super("lodestone", 3.5F, 3.5F, FFDSounds.LODESTONE);
        setResistance(3.5F / 0.6F);
        setMapColor(net.minecraft.block.material.MapColor.IRON);
    }

    @Override public EnumPushReaction getMobilityFlag(IBlockState state) { return EnumPushReaction.BLOCK; }

    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return FFDLodestoneCompass.bind(world, pos, player, hand);
    }
}
