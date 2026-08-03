package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockSporeBlossom extends Block {
    private static final AxisAlignedBB BLOSSOM_AABB =
            new AxisAlignedBB(0.125D, 0.8125D, 0.125D, 0.875D, 1.0D, 0.875D);

    public BlockSporeBlossom() {
        super(Material.PLANTS);
        setRegistryName(FarmerFutureDelight.MODID, "spore_blossom");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".spore_blossom");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(FFDSounds.SPORE_BLOSSOM);
        setLightOpacity(0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isSporeBlossomEnabled()
                && world.getBlockState(pos).getMaterial() != Material.WATER
                && canStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!world.isRemote && !canStay(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!FFDConfig.sporeBlossomParticlesEnabled
                || random.nextInt(FFDConfig.sporeBlossomParticleFrequency) != 0) {
            return;
        }
        for (int i = 0; i < FFDConfig.sporeBlossomParticleDensity; i++) {
            if (i == 0) {
                FarmerFutureDelight.proxy.spawnSporeBlossomParticle(world,
                        pos.getX() + random.nextDouble(), pos.getY() + 0.7D,
                        pos.getZ() + random.nextDouble(), false);
                continue;
            }
            BlockPos particlePos = pos.add(random.nextInt(21) - 10,
                    -random.nextInt(10), random.nextInt(21) - 10);
            if (!world.getBlockState(particlePos).isFullCube()) {
                FarmerFutureDelight.proxy.spawnSporeBlossomParticle(world,
                        particlePos.getX() + random.nextDouble(),
                        particlePos.getY() + random.nextDouble(),
                        particlePos.getZ() + random.nextDouble(), true);
            }
        }
    }

    private static boolean canStay(World world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), EnumFacing.DOWN);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOSSOM_AABB;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
