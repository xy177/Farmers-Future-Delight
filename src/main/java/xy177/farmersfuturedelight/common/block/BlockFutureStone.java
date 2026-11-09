package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockFutureStone extends Block {
    private MapColor mapColor = MapColor.STONE;

    public BlockFutureStone setMapColor(MapColor color) {
        this.mapColor = color;
        return this;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return mapColor;
    }

    public BlockFutureStone(String name, float hardness, float resistance, SoundType soundType) {
        this(name, hardness, resistance, soundType, 0);
    }

    public BlockFutureStone(String name, float hardness, float resistance, SoundType soundType,
                            int harvestLevel) {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(hardness);
        setResistance(resistance / 3.0F);
        setSoundType(soundType);
        setHarvestLevel("pickaxe", harvestLevel);
    }
}
