package xy177.farmersfuturedelight.common.worldgen;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

import javax.annotation.Nullable;

public final class FFDModernStoneProvider {
    private static final Logger LOGGER = LogManager.getLogger("FFD Modern Stone Provider");
    private static final boolean TRACE = Boolean.getBoolean("ffd.worldgen.traceStoneProvider");
    private final Block[] deepBaseBlocks;
    private final IBlockState deepslate;
    private final IBlockState tuff;
    private final IBlockState coalOre;
    private final IBlockState ironOre;
    private final IBlockState copperOre;
    private final IBlockState goldOre;
    private final IBlockState redstoneOre;
    private final IBlockState lapisOre;
    private final IBlockState diamondOre;
    private final IBlockState emeraldOre;
    private final IBlockState infestedDeepslate;
    private final IBlockState rawIronBlock;
    private final IBlockState rawCopperBlock;
    private final IBlockState normalCopperOre;
    private final IBlockState dripstoneBlock;
    private final IBlockState pointedDripstone;

    public FFDModernStoneProvider() {
        boolean localDeepslate = FFDItems.isBlockRegistered(FFDBlocks.DEEPSLATE);
        boolean localCopper = FFDItems.isBlockRegistered(FFDBlocks.COPPER_ORE);
        deepslate = resolve(FFDConfig.deepslateMode, FFDCompat.Feature.DEEPSLATE,
                FFDBlocks.DEEPSLATE, "deepslate");
        tuff = resolve(FFDConfig.deepslateMode, FFDCompat.Feature.DEEPSLATE,
                FFDBlocks.TUFF, "tuff");
        coalOre = deepOre(FFDBlocks.DEEPSLATE_COAL_ORE,
                Blocks.COAL_ORE, "deepslate_coal_ore");
        ironOre = deepOre(FFDBlocks.DEEPSLATE_IRON_ORE,
                Blocks.IRON_ORE, "deepslate_iron_ore");
        normalCopperOre = resolve(FFDConfig.copperMode, FFDCompat.Feature.COPPER,
                FFDBlocks.COPPER_ORE, "copper_ore");
        copperOre = deepCopperOre(localDeepslate, localCopper);
        goldOre = deepOre(FFDBlocks.DEEPSLATE_GOLD_ORE,
                Blocks.GOLD_ORE, "deepslate_gold_ore");
        redstoneOre = deepOre(FFDBlocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.REDSTONE_ORE, "deepslate_redstone_ore");
        lapisOre = deepOre(FFDBlocks.DEEPSLATE_LAPIS_ORE,
                Blocks.LAPIS_ORE, "deepslate_lapis_ore");
        diamondOre = deepOre(FFDBlocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.DIAMOND_ORE, "deepslate_diamond_ore");
        emeraldOre = deepOre(FFDBlocks.DEEPSLATE_EMERALD_ORE,
                Blocks.EMERALD_ORE, "deepslate_emerald_ore");
        IBlockState externalInfested = externalState(FFDConfig.deepslateMode,
                FFDCompat.Feature.DEEPSLATE, "infested_deepslate");
        infestedDeepslate = FFDItems.isBlockRegistered(FFDBlocks.INFESTED_DEEPSLATE)
                ? FFDBlocks.INFESTED_DEEPSLATE.getDefaultState()
                : externalInfested != null ? externalInfested
                : Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT,
                        BlockSilverfish.EnumType.STONE);
        rawIronBlock = resolve(FFDConfig.rawOreMode, FFDCompat.Feature.RAW_ORE,
                FFDBlocks.RAW_IRON_BLOCK, "raw_iron_block");
        rawCopperBlock = resolve(FFDConfig.copperMode, FFDCompat.Feature.COPPER,
                FFDBlocks.RAW_COPPER_BLOCK, "raw_copper_block");
        dripstoneBlock = resolve(FFDConfig.dripstoneMode, FFDCompat.Feature.DRIPSTONE,
                FFDBlocks.DRIPSTONE_BLOCK, "dripstone_block");
        pointedDripstone = resolve(FFDConfig.dripstoneMode, FFDCompat.Feature.DRIPSTONE,
                FFDBlocks.POINTED_DRIPSTONE, "pointed_dripstone");
        deepBaseBlocks = blocksOf(deepslate, tuff);
        if (TRACE) {
            LOGGER.info("FFD_MODERN_STONES deepslate={} tuff={} coal={} iron={} copper={} "
                            + "gold={} redstone={} lapis={} diamond={} emerald={} infested={} "
                            + "raw_iron={} raw_copper={} normal_copper={}",
                    id(deepslate), id(tuff), id(coalOre), id(ironOre), id(copperOre),
                    id(goldOre), id(redstoneOre), id(lapisOre), id(diamondOre),
                    id(emeraldOre), id(infestedDeepslate), id(rawIronBlock),
                    id(rawCopperBlock), id(normalCopperOre));
        }
    }

    public static FFDModernStoneProvider get() {
        return Holder.INSTANCE;
    }

    @Nullable
    public IBlockState deepslate() {
        return deepslate;
    }

    @Nullable
    public IBlockState tuff() {
        return tuff;
    }

    public IBlockState coalOre() {
        return coalOre;
    }

    public IBlockState ironOre() {
        return ironOre;
    }

    @Nullable
    public IBlockState copperOre() {
        return copperOre;
    }

    public IBlockState goldOre() {
        return goldOre;
    }

    public IBlockState redstoneOre() {
        return redstoneOre;
    }

    public IBlockState lapisOre() {
        return lapisOre;
    }

    public IBlockState diamondOre() {
        return diamondOre;
    }

    public IBlockState emeraldOre() {
        return emeraldOre;
    }

    public IBlockState infestedDeepslate() {
        return infestedDeepslate;
    }

    @Nullable
    public IBlockState rawIronBlock() {
        return rawIronBlock;
    }

    @Nullable
    public IBlockState rawCopperBlock() {
        return rawCopperBlock;
    }

    @Nullable
    public IBlockState normalCopperOre() {
        return normalCopperOre;
    }

    @Nullable
    public IBlockState dripstoneBlock() {
        return dripstoneBlock;
    }

    @Nullable
    public IBlockState pointedDripstone(EnumFacing direction, String thickness) {
        if (pointedDripstone == null) {
            return null;
        }
        IBlockState state = withProperty(pointedDripstone, "vertical_direction",
                direction.getName());
        return withProperty(state, "thickness", thickness);
    }

    public boolean isDripstoneBlock(IBlockState state) {
        return dripstoneBlock != null && state.getBlock() == dripstoneBlock.getBlock();
    }

    public boolean isPointedDripstone(IBlockState state) {
        return pointedDripstone != null && state.getBlock() == pointedDripstone.getBlock();
    }

    public boolean isDeepBase(IBlockState state) {
        Block block = state.getBlock();
        for (Block deepBase : deepBaseBlocks) {
            if (block == deepBase) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeepslateBase(IBlockState state) {
        return deepslate != null && state.getBlock() == deepslate.getBlock();
    }

    private IBlockState deepCopperOre(boolean localDeepslate, boolean localCopper) {
        if (normalCopperOre == null) {
            return null;
        }
        if (FFDItems.isBlockRegistered(FFDBlocks.DEEPSLATE_COPPER_ORE)) {
            return FFDBlocks.DEEPSLATE_COPPER_ORE.getDefaultState();
        }
        IBlockState external = externalState(FFDConfig.deepslateMode,
                FFDCompat.Feature.DEEPSLATE, "deepslate_copper_ore");
        if (external == null) {
            external = externalState(FFDConfig.copperMode, FFDCompat.Feature.COPPER,
                    "deepslate_copper_ore");
        }
        return external != null ? external : normalCopperOre();
    }

    private static IBlockState deepOre(Block local, Block fallback, String path) {
        if (FFDItems.isBlockRegistered(local)) {
            return local.getDefaultState();
        }
        IBlockState external = externalState(FFDConfig.deepslateMode,
                FFDCompat.Feature.DEEPSLATE, path);
        return external != null ? external : fallback.getDefaultState();
    }

    private static Block[] blocksOf(IBlockState... states) {
        Block[] blocks = new Block[states.length];
        int count = 0;
        for (IBlockState state : states) {
            if (state == null) {
                continue;
            }
            Block block = state.getBlock();
            boolean duplicate = false;
            for (int index = 0; index < count; index++) {
                duplicate |= blocks[index] == block;
            }
            if (!duplicate) {
                blocks[count++] = block;
            }
        }
        Block[] result = new Block[count];
        System.arraycopy(blocks, 0, result, 0, count);
        return result;
    }

    @Nullable
    private static IBlockState resolve(FFDConfig.FeatureMode mode, FFDCompat.Feature feature,
                                       Block local, String... externalPaths) {
        if (mode == FFDConfig.FeatureMode.DISABLED) {
            return null;
        }
        if (FFDCompat.isLocalBlockEnabled(mode, feature, local, externalPaths)) {
            return local.getDefaultState();
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

    public boolean hasDeepBase() {
        return deepslate != null;
    }

    public boolean hasTuff() {
        return tuff != null;
    }

    public boolean hasCopperOre() {
        return normalCopperOre() != null;
    }

    public IBlockState oreFor(DeepOre ore) {
        switch (ore) {
            case COAL:
                return coalOre;
            case IRON:
                return ironOre;
            case COPPER:
                return copperOre;
            case GOLD:
                return goldOre;
            case REDSTONE:
                return redstoneOre;
            case LAPIS:
                return lapisOre;
            case DIAMOND:
                return diamondOre;
            case EMERALD:
                return emeraldOre;
            case INFESTED:
                return infestedDeepslate;
            default:
                throw new IllegalArgumentException("Unknown deep ore " + ore);
        }
    }

    public enum DeepOre {
        COAL, IRON, COPPER, GOLD, REDSTONE, LAPIS, DIAMOND, EMERALD, INFESTED
    }

    @Nullable
    public static IBlockState optionalState(String... ids) {
        for (String id : ids) {
            ResourceLocation key = new ResourceLocation(id);
            Block block = Block.REGISTRY.getObject(key);
            if (block != null && key.equals(Block.REGISTRY.getNameForObject(block))) {
                return block.getDefaultState();
            }
        }
        return null;
    }

    private static String id(@Nullable IBlockState state) {
        return state == null ? "none" : String.valueOf(
                Block.REGISTRY.getNameForObject(state.getBlock()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withProperty(IBlockState state, String name, String value) {
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

    private static final class Holder {
        private static final FFDModernStoneProvider INSTANCE = new FFDModernStoneProvider();
    }
}
