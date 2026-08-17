package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public abstract class BlockAbstractCandle extends Block {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    protected BlockAbstractCandle(Material material) {
        super(material);
        setLightOpacity(0);
    }

    protected abstract Vec3d[] getParticleOffsets(IBlockState state);

    protected int getCandleCount(IBlockState state) {
        return 1;
    }

    public final boolean isLit(IBlockState state) {
        return state.getBlock() == this && state.getValue(LIT);
    }

    public final boolean canLight(IBlockState state) {
        return state.getBlock() == this && !state.getValue(LIT);
    }

    public final void setLit(World world, BlockPos pos, IBlockState state, boolean lit) {
        if (state.getBlock() == this && state.getValue(LIT) != lit) {
            world.setBlockState(pos, state.withProperty(LIT, lit), 11);
        }
    }

    protected final boolean handleLightingInteraction(World world, BlockPos pos,
                                                       IBlockState state, EntityPlayer player,
                                                       EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            if (!state.getValue(LIT) || !player.capabilities.allowEdit) {
                return false;
            }
            extinguish(world, pos, state);
            return true;
        }
        if (state.getValue(LIT)
                || held.getItem() != Items.FLINT_AND_STEEL
                && held.getItem() != Items.FIRE_CHARGE) {
            return false;
        }
        if (!world.isRemote) {
            setLit(world, pos, state, true);
            world.playSound(null, pos,
                    held.getItem() == Items.FLINT_AND_STEEL
                            ? SoundEvents.ITEM_FLINTANDSTEEL_USE
                            : SoundEvents.ITEM_FIRECHARGE_USE,
                    SoundCategory.BLOCKS, 1.0F,
                    world.rand.nextFloat() * 0.4F + 0.8F);
            player.addStat(StatList.getObjectUseStats(held.getItem()));
            if (!player.capabilities.isCreativeMode) {
                if (held.getItem() == Items.FLINT_AND_STEEL) {
                    held.damageItem(1, player);
                } else {
                    held.shrink(1);
                }
            }
        }
        return true;
    }

    public final void extinguish(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            for (Vec3d offset : getParticleOffsets(state)) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        pos.getX() + offset.x, pos.getY() + offset.y,
                        pos.getZ() + offset.z, 0.0D, 0.1D, 0.0D);
            }
            return;
        }
        setLit(world, pos, state, false);
        world.playSound(null, pos, FFDSounds.CANDLE_EXTINGUISH,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        for (Vec3d offset : getParticleOffsets(state)) {
            double x = pos.getX() + offset.x;
            double y = pos.getY() + offset.y;
            double z = pos.getZ() + offset.z;
            float chance = random.nextFloat();
            if (chance < 0.3F) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        x, y, z, 0.0D, 0.0D, 0.0D);
                if (chance < 0.17F) {
                    world.playSound(x, y, z, FFDSounds.CANDLE_AMBIENT,
                            SoundCategory.BLOCKS, 1.0F + random.nextFloat(),
                            random.nextFloat() * 0.7F + 0.3F, false);
                }
            }
            FarmerFutureDelight.proxy.spawnSmallFlameParticle(world, x, y, z);
        }
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(LIT) ? getCandleCount(state) * 3 : 0;
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
