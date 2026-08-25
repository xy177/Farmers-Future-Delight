package xy177.farmersfuturedelight.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Resolves the final OptiFine water colormap without making OptiFine a hard dependency. */
final class OptiFineWaterColorCompat {
    private static final Logger LOGGER = LogManager.getLogger("FFD OptiFine Water Color");
    private static final Accessor UNAVAILABLE = new Accessor();
    private static volatile Accessor accessor;

    private OptiFineWaterColorCompat() {
    }

    static int getColor(IBlockAccess world, BlockPos pos, int fallback) {
        if (AquaAcrobaticsWaterCompat.isEnabled()) {
            return fallback;
        }
        Accessor current = accessor;
        if (current == null) {
            synchronized (OptiFineWaterColorCompat.class) {
                current = accessor;
                if (current == null) {
                    current = resolve();
                    accessor = current;
                }
            }
        }
        return current.getColor(world, pos, fallback);
    }

    private static Accessor resolve() {
        try {
            ClassLoader loader = OptiFineWaterColorCompat.class.getClassLoader();
            Class<?> customColors = Class.forName("net.optifine.CustomColors", true, loader);
            Class<?> colorizerType = Class.forName(
                    "net.optifine.CustomColors$IColorizer", false, loader);
            Field waterColorizerField = customColors.getDeclaredField("COLORIZER_WATER");
            waterColorizerField.setAccessible(true);
            Object waterColorizer = waterColorizerField.get(null);
            Field smoothBiomes = findSmoothBiomesField();
            Method isColorConstant = colorizerType.getMethod("isColorConstant");
            Method getColor = colorizerType.getMethod("getColor", IBlockState.class,
                    IBlockAccess.class, BlockPos.class);
            isColorConstant.setAccessible(true);
            getColor.setAccessible(true);
            LOGGER.info("Using OptiFine final water colormap for waterlogged blocks");
            return new Accessor(waterColorizer, smoothBiomes, isColorConstant, getColor);
        } catch (ClassNotFoundException exception) {
            return UNAVAILABLE;
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("OptiFine water colormap API is unavailable; using biome water color", exception);
            return UNAVAILABLE;
        }
    }

    private static Field findSmoothBiomesField() {
        try {
            Field field = Minecraft.getMinecraft().gameSettings.getClass()
                    .getField("ofSmoothBiomes");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static final class Accessor {
        private final Object waterColorizer;
        private final Field smoothBiomes;
        private final Method isColorConstant;
        private final Method getColor;
        private volatile boolean failed;

        private Accessor() {
            waterColorizer = null;
            smoothBiomes = null;
            isColorConstant = null;
            getColor = null;
        }

        private Accessor(Object waterColorizer, Field smoothBiomes,
                         Method isColorConstant, Method getColor) {
            this.waterColorizer = waterColorizer;
            this.smoothBiomes = smoothBiomes;
            this.isColorConstant = isColorConstant;
            this.getColor = getColor;
        }

        private int getColor(IBlockAccess world, BlockPos pos, int fallback) {
            if (waterColorizer == null || failed) {
                return fallback;
            }
            try {
                boolean smooth = smoothBiomes == null
                        || smoothBiomes.getBoolean(Minecraft.getMinecraft().gameSettings);
                boolean constant = (Boolean) isColorConstant.invoke(waterColorizer);
                if (!smooth || constant) {
                    return colorAt(world, pos);
                }
                int red = 0;
                int green = 0;
                int blue = 0;
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        int color = colorAt(world, pos.add(x, 0, z));
                        red += color >> 16 & 255;
                        green += color >> 8 & 255;
                        blue += color & 255;
                    }
                }
                return red / 9 << 16 | green / 9 << 8 | blue / 9;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                failed = true;
                LOGGER.warn("OptiFine water colormap call failed; using biome water color", exception);
                return fallback;
            }
        }

        private int colorAt(IBlockAccess world, BlockPos pos) throws ReflectiveOperationException {
            return (Integer) getColor.invoke(waterColorizer,
                    net.minecraft.init.Blocks.WATER.getDefaultState(), world, pos);
        }
    }
}
