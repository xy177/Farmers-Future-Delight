package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.CompostingRules;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.common.tile.TileEntityComposter;

public class BlockComposter extends BlockContainer {
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 8);
    private static final AxisAlignedBB BOTTOM = new AxisAlignedBB(0, 0, 0, 1, 0.125D, 1);
    private static final AxisAlignedBB[] SIDES = {
            new AxisAlignedBB(0, 0.125D, 0, 0.125D, 1, 1),
            new AxisAlignedBB(0.875D, 0.125D, 0, 1, 1, 1),
            new AxisAlignedBB(0.125D, 0.125D, 0, 0.875D, 1, 0.125D),
            new AxisAlignedBB(0.125D, 0.125D, 0.875D, 0.875D, 1, 1)
    };

    public BlockComposter() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "composter");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".composter");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.6F);
        setResistance(0.6F / 3.0F);
        setSoundType(SoundType.WOOD);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(LEVEL, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityComposter();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                      @Nullable Entity entity, boolean actualState) {
        addCollisionBoxToList(pos, entityBox, boxes, BOTTOM);
        for (AxisAlignedBB side : SIDES) {
            addCollisionBoxToList(pos, entityBox, boxes, side);
        }
    }

    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos,
                                            Vec3d start, Vec3d end) {
        double height = Math.max(2, Math.min(15, 1 + state.getValue(LEVEL) * 2)) / 16.0D;
        RayTraceResult closest = rayTrace(pos, start, end, new AxisAlignedBB(0, 0, 0, 1, height, 1));
        for (AxisAlignedBB side : SIDES) {
            RayTraceResult hit = rayTrace(pos, start, end, side);
            if (hit != null && (closest == null || start.squareDistanceTo(hit.hitVec)
                    < start.squareDistanceTo(closest.hitVec))) {
                closest = hit;
            }
        }
        return closest;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        int level = state.getValue(LEVEL);
        if (level == 8) {
            if (!world.isRemote) {
                extract(world, pos, true);
                player.addStat(FFDStats.COMPOSTER_INTERACTION);
            }
            return true;
        }
        ItemStack stack = player.getHeldItem(hand);
        CompostingRules.Rule rule = CompostingRules.find(stack);
        if (level < 8 && rule != null) {
            if (!world.isRemote && level < 7) {
                insert(world, pos, rule);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                player.addStat(FFDStats.COMPOSTER_INTERACTION);
            }
            return true;
        }
        return false;
    }

    public static boolean insert(World world, BlockPos pos, CompostingRules.Rule rule) {
        IBlockState state = world.getBlockState(pos);
        if (world.isRemote || !(state.getBlock() instanceof BlockComposter)
                || state.getValue(LEVEL) >= 7 || rule == null) {
            return false;
        }
        int level = state.getValue(LEVEL);
        boolean success = level == 0 && rule.getChance() > 0.0D
                || world.rand.nextDouble() < rule.getChance();
        if (success) {
            world.setBlockState(pos, state.withProperty(LEVEL, level + 1), 3);
            if (level + 1 == 7) {
                world.scheduleUpdate(pos, state.getBlock(), 20);
            }
        }
        world.playSound(null, pos, success ? FFDSounds.COMPOSTER_FILL_SUCCESS
                : FFDSounds.COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (world instanceof WorldServer) {
            double center = Math.max(2, Math.min(15, 1 + (level + (success ? 1 : 0)) * 2))
                    / 16.0D + 0.03125D;
            for (int index = 0; index < 10; index++) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        pos.getX() + 0.1875D + 0.625D * world.rand.nextFloat(),
                        pos.getY() + center + world.rand.nextFloat() * (1.0D - center),
                        pos.getZ() + 0.1875D + 0.625D * world.rand.nextFloat(),
                        0, world.rand.nextGaussian() * 0.02D,
                        world.rand.nextGaussian() * 0.02D, world.rand.nextGaussian() * 0.02D, 1.0D);
            }
        }
        return success;
    }

    public static ItemStack extract(World world, BlockPos pos, boolean spawn) {
        IBlockState state = world.getBlockState(pos);
        if (world.isRemote || !(state.getBlock() instanceof BlockComposter)
                || state.getValue(LEVEL) != 8) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(Items.DYE, 1, 15);
        world.setBlockState(pos, state.withProperty(LEVEL, 0), 3);
        world.playSound(null, pos, FFDSounds.COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (spawn) {
            EntityItem item = new EntityItem(world, pos.getX() + 0.15D + world.rand.nextDouble() * 0.7D,
                    pos.getY() + 1.01D, pos.getZ() + 0.15D + world.rand.nextDouble() * 0.7D, result);
            item.setDefaultPickupDelay();
            world.spawnEntity(item);
        }
        return result;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(LEVEL) == 7) {
            world.scheduleUpdate(pos, this, 20);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (state.getValue(LEVEL) == 7) {
            world.setBlockState(pos, state.withProperty(LEVEL, 8), 3);
            world.playSound(null, pos, FFDSounds.COMPOSTER_READY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(LEVEL, Math.min(8, metadata & 15));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }
}
