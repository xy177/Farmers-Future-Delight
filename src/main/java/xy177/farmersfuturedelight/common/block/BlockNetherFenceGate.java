package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherFenceGate extends BlockFenceGate {
    public BlockNetherFenceGate(String name) {
        super(BlockPlanks.EnumType.OAK);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (state.getValue(OPEN)) {
            state = state.withProperty(OPEN, false);
        } else {
            EnumFacing playerFacing = EnumFacing.fromAngle(player.rotationYaw);
            if (state.getValue(FACING) == playerFacing.getOpposite()) {
                state = state.withProperty(FACING, playerFacing);
            }
            state = state.withProperty(OPEN, true);
        }

        world.setBlockState(pos, state, 10);
        playGateSound(player, world, pos, state.getValue(OPEN));
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block changedBlock, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }

        boolean powered = world.isBlockPowered(pos);
        if (state.getValue(POWERED) != powered) {
            world.setBlockState(pos,
                    state.withProperty(POWERED, powered).withProperty(OPEN, powered), 2);
            if (state.getValue(OPEN) != powered) {
                playGateSound(null, world, pos, powered);
            }
        }
    }

    private static void playGateSound(EntityPlayer player, World world, BlockPos pos, boolean open) {
        world.playSound(player, pos,
                open ? FFDSounds.NETHER_WOOD_FENCE_GATE_OPEN
                        : FFDSounds.NETHER_WOOD_FENCE_GATE_CLOSE,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }
}
