package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class BlockCryingObsidian extends BlockFutureStone {
    public BlockCryingObsidian() {
        super("crying_obsidian", 50.0F, 1200.0F, SoundType.STONE, 3);
        setResistance(2000.0F);
        setMapColor(MapColor.OBSIDIAN);
        setLightLevel(10.0F / 15.0F);
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return !(entity instanceof EntityDragon) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        EnumFacing side = EnumFacing.VALUES[random.nextInt(EnumFacing.VALUES.length)];
        if (side == EnumFacing.UP) {
            return;
        }
        BlockPos adjacent = pos.offset(side);
        if (world.getBlockState(adjacent).getBlockFaceShape(world, adjacent, side.getOpposite())
                == BlockFaceShape.SOLID) {
            return;
        }
        double x = pos.getX() + (side.getFrontOffsetX() == 0 ? random.nextDouble()
                : 0.5D + side.getFrontOffsetX() * 0.6D);
        double y = pos.getY() + (side.getFrontOffsetY() == 0 ? random.nextDouble()
                : 0.5D + side.getFrontOffsetY() * 0.6D);
        double z = pos.getZ() + (side.getFrontOffsetZ() == 0 ? random.nextDouble()
                : 0.5D + side.getFrontOffsetZ() * 0.6D);
        FarmerFutureDelight.proxy.spawnCryingObsidianParticle(world, x, y, z);
    }
}
