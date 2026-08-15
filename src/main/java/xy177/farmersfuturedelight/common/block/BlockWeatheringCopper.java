package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockWeatheringCopper extends Block implements IWeatheringCopper {
    private final CopperWeathering.WeatherState weatherState;
    private final boolean waxed;

    public BlockWeatheringCopper(String name, CopperWeathering.WeatherState weatherState,
                                 boolean waxed) {
        super(Material.IRON);
        this.weatherState = weatherState;
        this.waxed = waxed;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.0F);
        setResistance(6.0F / 3.0F);
        setSoundType(FFDSounds.COPPER);
        setHarvestLevel("pickaxe", 1);
        setTickRandomly(!waxed && weatherState != CopperWeathering.WeatherState.OXIDIZED);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        CopperWeathering.tryWeather(world, pos, state, random);
    }

    @Override
    public CopperWeathering.WeatherState getWeatherState() {
        return weatherState;
    }

    @Override
    public boolean isWaxed() {
        return waxed;
    }
}
