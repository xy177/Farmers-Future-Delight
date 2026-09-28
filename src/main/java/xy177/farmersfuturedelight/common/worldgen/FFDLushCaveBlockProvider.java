package xy177.farmersfuturedelight.common.worldgen;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.DripleafTilt;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

public final class FFDLushCaveBlockProvider {
    private final IBlockState mossBlock;
    private final IBlockState mossCarpet;
    private final IBlockState caveVines;
    private final IBlockState caveVinesPlant;
    private final IBlockState azalea;
    private final IBlockState floweringAzalea;
    private final IBlockState azaleaLeaves;
    private final IBlockState floweringAzaleaLeaves;
    private final IBlockState smallDripleaf;
    private final IBlockState bigDripleafStem;
    private final IBlockState bigDripleaf;
    private final IBlockState bigDripleafWaterlogged;
    private final IBlockState rootedDirt;
    private final IBlockState hangingRoots;
    private final IBlockState sporeBlossom;

    private FFDLushCaveBlockProvider() {
        mossBlock = resolve(FFDConfig.mossMode, FFDCompat.Feature.MOSS,
                FFDBlocks.MOSS_BLOCK, "moss_block");
        mossCarpet = resolve(FFDConfig.mossMode, FFDCompat.Feature.MOSS,
                FFDBlocks.MOSS_CARPET, "moss_carpet");
        caveVines = resolve(FFDConfig.glowBerryMode, FFDCompat.Feature.GLOW_BERRY,
                FFDBlocks.CAVE_VINES, "cave_vines");
        caveVinesPlant = resolve(FFDConfig.glowBerryMode, FFDCompat.Feature.GLOW_BERRY,
                FFDBlocks.CAVE_VINES_PLANT, "cave_vines_plant");
        azalea = resolve(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA,
                FFDBlocks.AZALEA, "azalea");
        floweringAzalea = resolve(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA,
                FFDBlocks.FLOWERING_AZALEA, "flowering_azalea");
        azaleaLeaves = resolve(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA,
                FFDBlocks.AZALEA_LEAVES, "azalea_leaves");
        floweringAzaleaLeaves = resolve(FFDConfig.azaleaMode, FFDCompat.Feature.AZALEA,
                FFDBlocks.FLOWERING_AZALEA_LEAVES, "flowering_azalea_leaves");
        smallDripleaf = resolve(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF,
                FFDBlocks.SMALL_DRIPLEAF, "small_dripleaf");
        bigDripleafStem = resolve(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF,
                FFDBlocks.BIG_DRIPLEAF_STEM, "big_dripleaf_stem");
        bigDripleaf = resolve(FFDConfig.dripleafMode, FFDCompat.Feature.DRIPLEAF,
                FFDBlocks.BIG_DRIPLEAF, "big_dripleaf");
        IBlockState externalWaterlogged = externalState(FFDConfig.dripleafMode,
                FFDCompat.Feature.DRIPLEAF, "big_dripleaf_waterlogged");
        bigDripleafWaterlogged = FFDConfig.dripleafMode == FFDConfig.FeatureMode.DISABLED
                ? null : isRegistered(FFDBlocks.BIG_DRIPLEAF_WATERLOGGED)
                && FFDCompat.isLocalBlockEnabled(FFDConfig.dripleafMode,
                        FFDCompat.Feature.DRIPLEAF, FFDBlocks.BIG_DRIPLEAF_WATERLOGGED,
                        "big_dripleaf")
                ? FFDBlocks.BIG_DRIPLEAF_WATERLOGGED.getDefaultState()
                : externalWaterlogged != null ? externalWaterlogged : bigDripleaf;
        rootedDirt = resolve(FFDConfig.rootedDirtMode, FFDCompat.Feature.ROOTED_DIRT,
                FFDBlocks.ROOTED_DIRT, "rooted_dirt");
        hangingRoots = resolve(FFDConfig.hangingRootsMode, FFDCompat.Feature.HANGING_ROOTS,
                FFDBlocks.HANGING_ROOTS, "hanging_roots");
        sporeBlossom = resolve(FFDConfig.sporeBlossomMode, FFDCompat.Feature.SPORE_BLOSSOM,
                FFDBlocks.SPORE_BLOSSOM, "spore_blossom");
    }

    public static FFDLushCaveBlockProvider get() {
        return Holder.INSTANCE;
    }

    public boolean hasMoss() {
        return mossBlock != null && mossCarpet != null;
    }

    public boolean hasCaveVines() {
        return caveVines != null && caveVinesPlant != null;
    }

    public boolean hasAzalea() {
        return azalea != null && floweringAzalea != null
                && azaleaLeaves != null && floweringAzaleaLeaves != null;
    }

    public boolean hasDripleaf() {
        return smallDripleaf != null && bigDripleafStem != null && bigDripleaf != null;
    }

    public boolean hasRootedDirt() {
        return rootedDirt != null;
    }

    public boolean hasHangingRoots() {
        return hangingRoots != null;
    }

    public boolean hasSporeBlossom() {
        return sporeBlossom != null;
    }

    public IBlockState mossBlock() {
        return mossBlock;
    }

    public IBlockState mossCarpet() {
        return mossCarpet;
    }

    public IBlockState azalea(boolean flowering) {
        return flowering ? floweringAzalea : azalea;
    }

    public IBlockState azaleaLeaves(boolean flowering) {
        return flowering ? floweringAzaleaLeaves : azaleaLeaves;
    }

    public IBlockState rootedDirt() {
        return rootedDirt;
    }

    public IBlockState hangingRoots() {
        return hangingRoots;
    }

    public IBlockState sporeBlossom() {
        return sporeBlossom;
    }

    public IBlockState caveVines(boolean tip, boolean berries) {
        IBlockState state = tip ? caveVines : caveVinesPlant;
        state = withProperty(state, "berries", Boolean.toString(berries));
        return berries ? withLargestIntegerProperty(state, "age") : state;
    }

    public IBlockState smallDripleaf(EnumFacing facing, BlockDoublePlant.EnumBlockHalf half,
                                     boolean waterlogged) {
        IBlockState state = withProperty(smallDripleaf, "facing", facing.getName());
        state = withProperty(state, "half", half == BlockDoublePlant.EnumBlockHalf.LOWER
                ? "lower" : "upper");
        return withProperty(state, "waterlogged", Boolean.toString(waterlogged));
    }

    public IBlockState bigDripleafStem(EnumFacing facing, boolean waterlogged) {
        IBlockState state = withProperty(bigDripleafStem, "facing", facing.getName());
        return withProperty(state, "waterlogged", Boolean.toString(waterlogged));
    }

    public IBlockState bigDripleaf(EnumFacing facing, boolean waterlogged) {
        IBlockState state = waterlogged ? bigDripleafWaterlogged : bigDripleaf;
        state = withProperty(state, "facing", facing.getName());
        state = withProperty(state, "tilt", DripleafTilt.NONE.getName());
        return withProperty(state, "waterlogged", Boolean.toString(waterlogged));
    }

    public boolean isMossBlock(IBlockState state) {
        return matches(state, mossBlock);
    }

    public boolean isRootedDirt(IBlockState state) {
        return matches(state, rootedDirt);
    }

    public boolean isHangingRoots(IBlockState state) {
        return matches(state, hangingRoots);
    }

    public boolean isCaveVine(IBlockState state) {
        return matches(state, caveVines) || matches(state, caveVinesPlant);
    }

    public boolean isSmallDripleaf(IBlockState state) {
        return matches(state, smallDripleaf);
    }

    public boolean isBigDripleafStem(IBlockState state) {
        return matches(state, bigDripleafStem);
    }

    public boolean isBigDripleaf(IBlockState state) {
        return matches(state, bigDripleaf) || matches(state, bigDripleafWaterlogged);
    }

    public boolean isFloweringAzalea(IBlockState state) {
        return matches(state, floweringAzalea) || matches(state, floweringAzaleaLeaves);
    }

    public boolean isSporeBlossom(IBlockState state) {
        return matches(state, sporeBlossom);
    }

    @Nullable
    private static IBlockState resolve(FFDConfig.FeatureMode mode, FFDCompat.Feature feature,
                                       Block local, String... externalPaths) {
        if (mode == FFDConfig.FeatureMode.DISABLED) {
            return null;
        }
        if (FFDCompat.isLocalBlockEnabled(mode, feature, local, externalPaths)) {
            return isRegistered(local) ? local.getDefaultState() : null;
        }
        return FFDCompat.getExternalBlockState(feature, externalPaths);
    }

    @Nullable
    private static IBlockState externalState(FFDConfig.FeatureMode mode,
                                              FFDCompat.Feature feature,
                                              String... paths) {
        return mode == FFDConfig.FeatureMode.AUTO
                ? FFDCompat.getExternalBlockState(feature, paths) : null;
    }

    private static boolean isRegistered(Block block) {
        return block.getRegistryName() != null
                && ForgeRegistries.BLOCKS.getValue(block.getRegistryName()) == block;
    }

    private static boolean matches(IBlockState state, @Nullable IBlockState target) {
        return target != null && state.getBlock() == target.getBlock();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withProperty(@Nullable IBlockState state, String name,
                                            String value) {
        if (state == null) {
            return null;
        }
        for (IProperty property : state.getPropertyKeys()) {
            if (!name.equals(property.getName())) {
                continue;
            }
            Optional parsed = property.parseValue(value);
            if (parsed.isPresent()) {
                return state.withProperty(property, (Comparable) parsed.get());
            }
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withLargestIntegerProperty(IBlockState state, String name) {
        for (IProperty property : state.getPropertyKeys()) {
            if (!name.equals(property.getName())) {
                continue;
            }
            Collection values = property.getAllowedValues();
            Comparable largest = null;
            for (Object value : values) {
                if (value instanceof Integer && (largest == null
                        || ((Integer) value) > ((Integer) largest))) {
                    largest = (Comparable) value;
                }
            }
            if (largest != null) {
                return state.withProperty(property, largest);
            }
        }
        return state;
    }

    private static final class Holder {
        private static final FFDLushCaveBlockProvider INSTANCE =
                new FFDLushCaveBlockProvider();
    }
}
