package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;

public class BlockFutureFlower extends BlockBush {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(
            0.3125D, 0.0D, 0.3125D, 0.6875D, 0.625D, 0.6875D);
    private final boolean witherRose;
    private static volatile Block[] effectiveBeeFlowers;

    public BlockFutureFlower(String name, boolean witherRose) {
        this.witherRose = witherRose;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setTickRandomly(false);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return !world.getBlockState(pos).getMaterial().isLiquid()
                && world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && canBlockStay(world, pos, getDefaultState());
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState below = world.getBlockState(pos.down());
        Block support = below.getBlock();
        return support == Blocks.GRASS || support == Blocks.DIRT || support == Blocks.MYCELIUM
                || support == Blocks.FARMLAND
                || FFDLushCaveBlockProvider.get().isMossBlock(below)
                || FFDLushCaveBlockProvider.get().isRootedDirt(below)
                || witherRose && (support == Blocks.NETHERRACK || support == Blocks.SOUL_SAND)
                || support.canSustainPlant(below, world, pos.down(), EnumFacing.UP, this);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE.offset(state.getOffset(world, pos));
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (witherRose && !world.isRemote && world.getDifficulty() != EnumDifficulty.PEACEFUL
                && entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            PotionEffect effect = new PotionEffect(MobEffects.WITHER, 40);
            if (living.isPotionApplicable(effect)) {
                living.addPotionEffect(effect);
            }
        }
    }

    public static boolean isFlower(Block block) {
        if (block instanceof BlockFutureFlower) {
            return true;
        }
        if (block.getRegistryName() == null) {
            return false;
        }
        Block[] flowers = effectiveBeeFlowers;
        if (flowers == null) {
            synchronized (BlockFutureFlower.class) {
                flowers = effectiveBeeFlowers;
                if (flowers == null) {
                    flowers = new Block[]{FFDItems.effectiveBlock(FFDBlocks.CORNFLOWER),
                            FFDItems.effectiveBlock(FFDBlocks.LILY_OF_THE_VALLEY),
                            FFDItems.effectiveBlock(FFDBlocks.WITHER_ROSE)};
                    effectiveBeeFlowers = flowers;
                }
            }
        }
        for (Block flower : flowers) {
            if (flower == block) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!witherRose) {
            return;
        }
        AxisAlignedBB bounds = getBoundingBox(state, world, pos);
        for (int attempt = 0; attempt < 3; attempt++) {
            if (random.nextBoolean()) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        pos.getX() + (bounds.minX + bounds.maxX) * 0.5D + random.nextDouble() / 5.0D,
                        pos.getY() + 0.5D - random.nextDouble(),
                        pos.getZ() + (bounds.minZ + bounds.maxZ) * 0.5D + random.nextDouble() / 5.0D,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
