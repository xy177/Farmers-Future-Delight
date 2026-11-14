package xy177.farmersfuturedelight.common.world.modern;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.BlockStatePaletteHashMap;
import net.minecraft.world.chunk.BlockStatePaletteLinear;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public final class ModernSectionContents {
    private static final Field PALETTE = paletteField();

    private ModernSectionContents() {
    }

    public static boolean mayContain(ExtendedBlockStorage section, Predicate<IBlockState> predicate) {
        if (section == null) return false;
        if (PALETTE == null || section.getClass() != ExtendedBlockStorage.class
                || section.getData().getClass() != BlockStateContainer.class) return true;
        try {
            IBlockStatePalette palette = (IBlockStatePalette) PALETTE.get(section.getData());
            if (palette.getClass() != BlockStatePaletteLinear.class
                    && palette.getClass() != BlockStatePaletteHashMap.class) return true;
            for (int index = 0; index < 256; index++) {
                IBlockState state = palette.getBlockState(index);
                if (state == null) return false;
                if (predicate.test(state)) return true;
            }
            return true;
        } catch (IllegalAccessException failure) {
            return true;
        }
    }

    private static Field paletteField() {
        try {
            return ReflectionHelper.findField(BlockStateContainer.class, "palette", "field_186022_c");
        } catch (RuntimeException failure) {
            return null;
        }
    }
}
