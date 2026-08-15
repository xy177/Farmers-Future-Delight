package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public abstract class BlockWeatheringCopperSlab extends BlockFutureSlab
        implements IWeatheringCopper {
    private final CopperWeathering.WeatherState weatherState;
    private final boolean waxed;

    protected BlockWeatheringCopperSlab(String name, BlockSlab itemSlab,
                                        CopperWeathering.WeatherState weatherState,
                                        boolean waxed) {
        super(name, FFDSounds.COPPER, itemSlab);
        this.weatherState = weatherState;
        this.waxed = waxed;
        setHardness(3.0F);
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

    public static final class Half extends BlockWeatheringCopperSlab {
        public Half(String name, CopperWeathering.WeatherState weatherState, boolean waxed) {
            super(name, null, weatherState, waxed);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static final class Double extends BlockWeatheringCopperSlab {
        public Double(String name, BlockSlab itemSlab, CopperWeathering.WeatherState weatherState,
                      boolean waxed) {
            super(name, itemSlab, weatherState, waxed);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }
}
