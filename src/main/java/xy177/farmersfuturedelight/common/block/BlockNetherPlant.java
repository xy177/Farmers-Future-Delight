package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

/** Shared placement rules for plants rooted in Nether vegetation. */
public class BlockNetherPlant extends BlockBush {
    private final AxisAlignedBB shape;
    private final boolean supportsMycelium;

    public BlockNetherPlant(String name) {
        this(name, 12.0D, 13.0D, false);
    }

    public BlockNetherPlant(String name, double width, double height) {
        this(name, width, height, false);
    }

    protected BlockNetherPlant(String name, double width, double height,
                               boolean supportsMycelium) {
        super(Material.PLANTS);
        double inset = (16.0D - width) / 32.0D;
        this.shape = new AxisAlignedBB(inset, 0.0D, inset,
                1.0D - inset, height / 16.0D, 1.0D - inset);
        this.supportsMycelium = supportsMycelium;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(name.endsWith("_fungus") ? FFDSounds.FUNGUS
                : "nether_sprouts".equals(name) ? FFDSounds.NETHER_SPROUTS : FFDSounds.ROOTS);
        setLightOpacity(0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.isAirBlock(pos)
                && canSustainNetherVegetation(world.getBlockState(pos.down()), supportsMycelium);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainNetherVegetation(world.getBlockState(pos.down()), supportsMycelium);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return shape;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public net.minecraftforge.common.EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world,
                                                                 BlockPos pos) {
        return net.minecraftforge.common.EnumPlantType.Nether;
    }

    public static boolean canSustainNylium(IBlockState state, boolean warped) {
        return state.getBlock() == (warped ? FFDBlocks.WARPED_NYLIUM : FFDBlocks.CRIMSON_NYLIUM);
    }

    public static boolean canSustainNetherVegetation(IBlockState state) {
        return canSustainNetherVegetation(state, false);
    }

    public static boolean canSustainNetherFungus(IBlockState state) {
        return canSustainNetherVegetation(state, true);
    }

    private static boolean canSustainNetherVegetation(IBlockState state,
                                                        boolean supportsMycelium) {
        net.minecraft.block.Block block = state.getBlock();
        return block == net.minecraft.init.Blocks.DIRT
                || block == net.minecraft.init.Blocks.GRASS
                || block == net.minecraft.init.Blocks.FARMLAND
                || block == FFDBlocks.MOSS_BLOCK
                || block == FFDBlocks.ROOTED_DIRT
                || block == FFDBlocks.CRIMSON_NYLIUM
                || block == FFDBlocks.WARPED_NYLIUM
                || supportsMycelium && block == net.minecraft.init.Blocks.MYCELIUM;
    }
}
