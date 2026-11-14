package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class BlockNetherGoldOre extends BlockOre {
    public BlockNetherGoldOre() {
        super(MapColor.NETHERRACK);
        setRegistryName(FarmerFutureDelight.MODID,"nether_gold_ore");
        setUnlocalizedName(FarmerFutureDelight.MODID+".nether_gold_ore");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3);setResistance(5);setHarvestLevel("pickaxe",0);
        setSoundType(xy177.farmersfuturedelight.common.registry.FFDSounds.NETHER_GOLD_ORE);
    }
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune) { return Items.GOLD_NUGGET; }
    @Override public int quantityDropped(Random random) { return 2+random.nextInt(5); }
    @Override public int getExpDrop(IBlockState state,IBlockAccess world,BlockPos pos,int fortune) { return RANDOM.nextInt(2); }
}
