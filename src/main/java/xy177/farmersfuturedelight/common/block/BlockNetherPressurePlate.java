package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherPressurePlate extends BlockPressurePlate {
    public BlockNetherPressurePlate(String name) {
        super(Material.WOOD, Sensitivity.EVERYTHING);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    protected void playClickOnSound(World world, BlockPos pos) {
        world.playSound(null, pos, FFDSounds.NETHER_WOOD_PRESSURE_PLATE_CLICK_ON,
                SoundCategory.BLOCKS, 0.3F, 0.8F);
    }

    @Override
    protected void playClickOffSound(World world, BlockPos pos) {
        world.playSound(null, pos, FFDSounds.NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF,
                SoundCategory.BLOCKS, 0.3F, 0.7F);
    }
}
