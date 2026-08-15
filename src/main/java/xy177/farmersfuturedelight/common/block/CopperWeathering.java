package xy177.farmersfuturedelight.common.block;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class CopperWeathering {
    private static final float DAILY_ATTEMPT_CHANCE = 0.05688889F;
    private static final int SCAN_DISTANCE = 4;

    private static final Map<Block, Block> NEXT = new IdentityHashMap<>();
    private static final Map<Block, Block> PREVIOUS = new IdentityHashMap<>();
    private static final Map<Block, Block> WAXED = new IdentityHashMap<>();
    private static final Map<Block, Block> UNWAXED = new IdentityHashMap<>();

    private CopperWeathering() {
    }

    public static void registerWeatheringSequence(Block... blocks) {
        for (int i = 0; i + 1 < blocks.length; i++) {
            NEXT.put(blocks[i], blocks[i + 1]);
            PREVIOUS.put(blocks[i + 1], blocks[i]);
        }
    }

    public static void registerWaxedPair(Block unwaxed, Block waxed) {
        WAXED.put(unwaxed, waxed);
        UNWAXED.put(waxed, unwaxed);
    }

    public static void rebuildEffectiveMappings() {
        NEXT.clear();
        PREVIOUS.clear();
        WAXED.clear();
        UNWAXED.clear();
        registerEffectiveFamily(FFDBlocks.COPPER_BLOCKS, FFDBlocks.WAXED_COPPER_BLOCKS);
        registerEffectiveFamily(FFDBlocks.CUT_COPPER_BLOCKS,
                FFDBlocks.WAXED_CUT_COPPER_BLOCKS);
        registerEffectiveFamily(FFDBlocks.CUT_COPPER_STAIRS,
                FFDBlocks.WAXED_CUT_COPPER_STAIRS);
        registerEffectiveFamily(FFDBlocks.CUT_COPPER_SLABS,
                FFDBlocks.WAXED_CUT_COPPER_SLABS);
        registerEffectiveFamily(FFDBlocks.CUT_COPPER_DOUBLE_SLABS,
                FFDBlocks.WAXED_CUT_COPPER_DOUBLE_SLABS);
        registerEffectiveFamily(FFDBlocks.LIGHTNING_RODS, FFDBlocks.WAXED_LIGHTNING_RODS);
    }

    private static void registerEffectiveFamily(Block[] unwaxed, Block[] waxed) {
        for (int index = 0; index < unwaxed.length; index++) {
            Block effectiveUnwaxed = FFDItems.effectiveBlock(unwaxed[index]);
            Block effectiveWaxed = FFDItems.effectiveBlock(waxed[index]);
            if (FFDItems.isBlockRegistered(unwaxed[index]) && effectiveWaxed != null) {
                WAXED.put(unwaxed[index], effectiveWaxed);
            }
            if (FFDItems.isBlockRegistered(waxed[index]) && effectiveUnwaxed != null) {
                UNWAXED.put(waxed[index], effectiveUnwaxed);
            }
            if (index + 1 >= unwaxed.length) {
                continue;
            }
            Block effectiveNext = FFDItems.effectiveBlock(unwaxed[index + 1]);
            if (FFDItems.isBlockRegistered(unwaxed[index]) && effectiveNext != null) {
                NEXT.put(unwaxed[index], effectiveNext);
            }
            if (FFDItems.isBlockRegistered(unwaxed[index + 1]) && effectiveUnwaxed != null) {
                PREVIOUS.put(unwaxed[index + 1], effectiveUnwaxed);
            }
        }
    }

    public static IBlockState getNext(IBlockState state) {
        return transform(state, NEXT.get(state.getBlock()));
    }

    public static IBlockState getPrevious(IBlockState state) {
        return transform(state, PREVIOUS.get(state.getBlock()));
    }

    public static IBlockState getWaxed(IBlockState state) {
        return transform(state, WAXED.get(state.getBlock()));
    }

    public static IBlockState getUnwaxed(IBlockState state) {
        return transform(state, UNWAXED.get(state.getBlock()));
    }

    public static IBlockState getFirst(IBlockState state) {
        Block first = state.getBlock();
        Block previous = PREVIOUS.get(first);
        while (previous != null) {
            first = previous;
            previous = PREVIOUS.get(first);
        }
        return transform(state, first);
    }

    public static boolean isWaxed(Block block) {
        return UNWAXED.containsKey(block);
    }

    public static EnumActionResult tryWax(EntityPlayer player, World world, BlockPos pos,
                                           EnumHand hand, EnumFacing facing) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState waxedState = getWaxed(world.getBlockState(pos));
        if (waxedState == null) {
            return EnumActionResult.PASS;
        }
        EnumFacing editFace = facing == null ? EnumFacing.UP : facing;
        if (!player.canPlayerEdit(pos, editFace, stack)) {
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            world.setBlockState(pos, waxedState, 11);
            world.playSound(null, pos, FFDSounds.HONEYCOMB_WAX_ON,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            spawnWaxOnParticles(world, pos);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            player.addStat(StatList.getObjectUseStats(stack.getItem()));
            if (player instanceof EntityPlayerMP) {
                FFDAdvancements.WAX_ON.trigger((EntityPlayerMP) player);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public static void spawnWaxOnParticles(World world, BlockPos pos) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    7, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }

    public static void spawnBlockParticles(World world, BlockPos pos, IBlockState state) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.BLOCK_DUST,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    9, 0.35D, 0.35D, 0.35D, 0.05D, Block.getStateId(state));
        }
    }

    public static void spawnLightningCleanParticles(World world, BlockPos pos) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    5, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }

    public static void tryWeather(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || random.nextFloat() >= DAILY_ATTEMPT_CHANCE
                || !(state.getBlock() instanceof IWeatheringCopper)) {
            return;
        }

        IWeatheringCopper copper = (IWeatheringCopper) state.getBlock();
        if (copper.isWaxed() || copper.getWeatherState() == WeatherState.OXIDIZED) {
            return;
        }

        int ownAge = copper.getWeatherState().ordinal();
        int sameAgeCount = 0;
        int olderCount = 0;
        BlockPos min = pos.add(-SCAN_DISTANCE, -SCAN_DISTANCE, -SCAN_DISTANCE);
        BlockPos max = pos.add(SCAN_DISTANCE, SCAN_DISTANCE, SCAN_DISTANCE);
        for (BlockPos.MutableBlockPos nearby : BlockPos.getAllInBoxMutable(min, max)) {
            if (nearby.equals(pos) || manhattanDistance(pos, nearby) > SCAN_DISTANCE) {
                continue;
            }
            Block nearbyBlock = world.getBlockState(nearby).getBlock();
            if (!(nearbyBlock instanceof IWeatheringCopper)) {
                continue;
            }
            IWeatheringCopper nearbyCopper = (IWeatheringCopper) nearbyBlock;
            if (nearbyCopper.isWaxed()) {
                continue;
            }
            int nearbyAge = nearbyCopper.getWeatherState().ordinal();
            if (nearbyAge < ownAge) {
                return;
            }
            if (nearbyAge > ownAge) {
                olderCount++;
            } else {
                sameAgeCount++;
            }
        }

        float chance = (float) (olderCount + 1) / (float) (olderCount + sameAgeCount + 1);
        float modifier = copper.getWeatherState() == WeatherState.UNAFFECTED ? 0.75F : 1.0F;
        if (random.nextFloat() < chance * chance * modifier) {
            IBlockState next = getNext(state);
            if (next != null) {
                world.setBlockState(pos, next, 3);
            }
        }
    }

    private static int manhattanDistance(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX())
                + Math.abs(first.getY() - second.getY())
                + Math.abs(first.getZ() - second.getZ());
    }

    private static IBlockState transform(IBlockState state, Block target) {
        if (target == null) {
            return null;
        }
        IBlockState result = target.getDefaultState();
        for (IProperty<?> sourceProperty : state.getPropertyKeys()) {
            for (IProperty<?> targetProperty : result.getPropertyKeys()) {
                if (targetProperty.getName().equals(sourceProperty.getName())) {
                    result = copyProperty(result, state, sourceProperty, targetProperty);
                    break;
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState copyProperty(IBlockState target, IBlockState source,
                                            IProperty sourceProperty,
                                            IProperty targetProperty) {
        Comparable value = source.getValue(sourceProperty);
        com.google.common.base.Optional parsed = targetProperty.parseValue(
                sourceProperty.getName(value));
        return parsed.isPresent() ? target.withProperty(targetProperty,
                (Comparable) parsed.get()) : target;
    }

    public enum WeatherState {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED
    }
}
