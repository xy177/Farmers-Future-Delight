package xy177.farmersfuturedelight.common.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDGuiHandler;

public class BlockSmithingTable extends BlockWorkstation {
    public BlockSmithingTable() {
        super("smithing_table");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos,
                                    net.minecraft.block.state.IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(FarmerFutureDelight.instance, FFDGuiHandler.SMITHING_TABLE,
                    world, pos.getX(), pos.getY(), pos.getZ());
            player.addStat(FFDStats.SMITHING_TABLE_INTERACTION);
        }
        return true;
    }
}
