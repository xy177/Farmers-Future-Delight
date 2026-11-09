package xy177.farmersfuturedelight.common.fluid;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;

public final class FFDFluidloggedApiAccess {
    private static volatile boolean initialized;
    private static volatile Method getFluidState;
    private static Method getFluid;

    private FFDFluidloggedApiAccess() {
    }

    @Nullable
    public static Fluid getFluid(IBlockAccess world, BlockPos pos) {
        if (!initialized) initialize();
        Method reader = getFluidState;
        if (reader == null) return null;
        try {
            return (Fluid) getFluid.invoke(reader.invoke(null, world, pos));
        } catch (ReflectiveOperationException | LinkageError exception) {
            getFluidState = null;
            LogManager.getLogger("FFD Fluidlogged Compatibility")
                    .warn("Could not read external fluid state at {}", pos, exception);
            return null;
        }
    }

    private static synchronized void initialize() {
        if (initialized) return;
        if (Loader.isModLoaded("fluidlogged_api")) {
            try {
                Class<?> utils = Class.forName(
                        "git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils");
                Class<?> state = Class.forName(
                        "git.jbredwards.fluidlogged_api.api.util.FluidState");
                getFluid = state.getMethod("getFluid");
                getFluidState = utils.getMethod("getFluidState", IBlockAccess.class, BlockPos.class);
            } catch (ReflectiveOperationException | LinkageError exception) {
                LogManager.getLogger("FFD Fluidlogged Compatibility")
                        .warn("Could not access external fluid states", exception);
            }
        }
        initialized = true;
    }
}
