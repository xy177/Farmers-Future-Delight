package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;

import javax.annotation.Nullable;

/** Resolves independently provided blocks used by Nether forest generation. */
public final class FFDNetherBlockProvider {
    private final IBlockState crimsonNylium;
    private final IBlockState crimsonFungus;
    private final IBlockState crimsonRoots;
    private final IBlockState weepingVines;
    private final IBlockState weepingVinesPlant;
    private final IBlockState warpedNylium;
    private final IBlockState warpedFungus;
    private final IBlockState warpedRoots;
    private final IBlockState netherSprouts;
    private final IBlockState twistingVines;
    private final IBlockState twistingVinesPlant;
    private final IBlockState crimsonStem;
    private final IBlockState warpedStem;
    private final IBlockState netherWartBlock;
    private final IBlockState warpedWartBlock;
    private final IBlockState shroomlight;

    private FFDNetherBlockProvider() {
        crimsonNylium = resolve(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON,
                FFDBlocks.CRIMSON_NYLIUM, "crimson_nylium");
        crimsonFungus = resolve(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON,
                FFDBlocks.CRIMSON_FUNGUS, "crimson_fungus");
        crimsonRoots = resolve(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON,
                FFDBlocks.CRIMSON_ROOTS, "crimson_roots");
        weepingVines = resolve(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON,
                FFDBlocks.WEEPING_VINES, "weeping_vines");
        weepingVinesPlant = resolve(FFDConfig.crimsonMode, FFDCompat.Feature.CRIMSON,
                FFDBlocks.WEEPING_VINES_PLANT, "weeping_vines_plant");
        warpedNylium = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.WARPED_NYLIUM, "warped_nylium");
        warpedFungus = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.WARPED_FUNGUS, "warped_fungus");
        warpedRoots = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.WARPED_ROOTS, "warped_roots");
        netherSprouts = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.NETHER_SPROUTS, "nether_sprouts");
        twistingVines = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.TWISTING_VINES, "twisting_vines");
        twistingVinesPlant = resolve(FFDConfig.warpedMode, FFDCompat.Feature.WARPED,
                FFDBlocks.TWISTING_VINES_PLANT, "twisting_vines_plant");
        crimsonStem = resolve(FFDConfig.crimsonWoodMode, FFDCompat.Feature.CRIMSON_WOOD,
                FFDBlocks.CRIMSON_STEM, "crimson_stem");
        warpedStem = resolve(FFDConfig.warpedWoodMode, FFDCompat.Feature.WARPED_WOOD,
                FFDBlocks.WARPED_STEM, "warped_stem");
        netherWartBlock = resolve(FFDConfig.crimsonWoodMode,
                FFDCompat.Feature.CRIMSON_WOOD, FFDBlocks.NETHER_WART_BLOCK,
                "nether_wart_block");
        warpedWartBlock = resolve(FFDConfig.warpedWoodMode,
                FFDCompat.Feature.WARPED_WOOD, FFDBlocks.WARPED_WART_BLOCK,
                "warped_wart_block");
        IBlockState crimsonLight = resolve(FFDConfig.crimsonWoodMode,
                FFDCompat.Feature.CRIMSON_WOOD, FFDBlocks.SHROOMLIGHT, "shroomlight");
        shroomlight = crimsonLight != null ? crimsonLight : resolve(FFDConfig.warpedWoodMode,
                FFDCompat.Feature.WARPED_WOOD, FFDBlocks.SHROOMLIGHT, "shroomlight");
    }

    public static FFDNetherBlockProvider get() {
        return Holder.INSTANCE;
    }

    public IBlockState nylium(boolean warped) {
        return warped ? warpedNylium : crimsonNylium;
    }

    public IBlockState fungus(boolean warped) {
        return warped ? warpedFungus : crimsonFungus;
    }

    public IBlockState roots(boolean warped) {
        return warped ? warpedRoots : crimsonRoots;
    }

    public IBlockState sprouts() {
        return netherSprouts;
    }

    public IBlockState vine(boolean warped, boolean tip) {
        if (warped) {
            return tip ? twistingVines : twistingVinesPlant;
        }
        return tip ? weepingVines : weepingVinesPlant;
    }

    public IBlockState stem(boolean warped) {
        return warped ? warpedStem : crimsonStem;
    }

    public IBlockState wart(boolean warped) {
        return warped ? warpedWartBlock : netherWartBlock;
    }

    public IBlockState shroomlight() {
        return shroomlight;
    }

    public boolean isNylium(IBlockState state) {
        return matches(state, crimsonNylium) || matches(state, warpedNylium);
    }

    public boolean isNylium(IBlockState state, boolean warped) {
        return matches(state, nylium(warped));
    }

    public boolean isWart(IBlockState state, boolean warped) {
        return matches(state, wart(warped));
    }

    public boolean isFungus(IBlockState state, boolean warped) {
        return matches(state, fungus(warped));
    }

    public boolean isVine(IBlockState state, boolean warped) {
        return matches(state, vine(warped, true)) || matches(state, vine(warped, false));
    }

    @Nullable
    private static IBlockState resolve(FFDConfig.FeatureMode mode, FFDCompat.Feature feature,
                                       Block local, String path) {
        if (mode == FFDConfig.FeatureMode.DISABLED) {
            return null;
        }
        if (FFDCompat.isLocalBlockEnabled(mode, feature, local, path)) {
            return isRegistered(local) ? local.getDefaultState() : null;
        }
        return FFDCompat.getExternalBlockState(feature, path);
    }

    private static boolean isRegistered(Block block) {
        return block.getRegistryName() != null
                && ForgeRegistries.BLOCKS.getValue(block.getRegistryName()) == block;
    }

    private static boolean matches(IBlockState state, @Nullable IBlockState target) {
        return target != null && state.getBlock() == target.getBlock();
    }

    private static final class Holder {
        private static final FFDNetherBlockProvider INSTANCE = new FFDNetherBlockProvider();
    }
}
