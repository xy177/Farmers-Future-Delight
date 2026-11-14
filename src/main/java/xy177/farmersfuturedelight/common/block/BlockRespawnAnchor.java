package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ExplosionEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.core.FFDBedHooks;

public final class BlockRespawnAnchor extends BlockFutureStone {
    public static final PropertyInteger CHARGE = PropertyInteger.create("charges", 0, 4);
    private static final ThreadLocal<BlockPos> WATER_PROTECTED_EXPLOSION = new ThreadLocal<>();
    private static final int[][] OFFSETS = {{0, -1}, {-1, 0}, {0, 1}, {1, 0},
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

    public BlockRespawnAnchor() {
        super("respawn_anchor", 50.0F, 1200.0F, net.minecraft.block.SoundType.STONE, 3);
        setResistance(2000.0F);
        setMapColor(MapColor.OBSIDIAN);
        setDefaultState(blockState.getBaseState().withProperty(CHARGE, 0));
    }

    @Override public EnumPushReaction getMobilityFlag(IBlockState state) { return EnumPushReaction.BLOCK; }

    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        int charges = state.getValue(CHARGE);
        ItemStack held = player.getHeldItem(hand);
        Item fuel = Item.getItemFromBlock(Blocks.GLOWSTONE);
        if (charges < 4 && held.getItem() == fuel) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(CHARGE, charges + 1), 3);
                if (!player.capabilities.isCreativeMode) held.shrink(1);
                world.playSound(null, pos, FFDSounds.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (charges + 1 == 4 && player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                    xy177.farmersfuturedelight.common.advancement.FFDAdvancements.CHARGE_RESPAWN_ANCHOR.trigger(
                            (net.minecraft.entity.player.EntityPlayerMP) player);
                }
            }
            return true;
        }
        if (charges == 0 || charges < 4 && hand == EnumHand.MAIN_HAND
                && player.getHeldItemOffhand().getItem() == fuel) return false;
        if (world.isRemote) return true;
        if (world.provider.getDimension() == -1) {
            if (!FFDBedHooks.isAnchorSpawn(player, pos)) {
                player.setSpawnPoint(pos, false);
                if (pos.equals(player.getBedLocation(-1)) && !player.isSpawnForced(-1)) {
                    FFDBedHooks.setAnchorSpawn(player, pos);
                    player.sendMessage(new TextComponentTranslation("farmers_future_delight.bed.spawn_set"));
                    world.playSound(null, pos, FFDSounds.RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
        } else {
            boolean water = WaterloggedBlockApi.containsWater(world, pos.up());
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos adjacent = pos.offset(facing);
                IBlockState fluid = world.getBlockState(adjacent);
                if (WaterloggedBlockApi.isWaterSource(world, adjacent)
                        || fluid.getMaterial() == Material.WATER && fluid.getBlock() instanceof BlockLiquid
                        && (fluid.getValue(BlockLiquid.LEVEL) & 7) <= 6
                        && !WaterloggedBlockApi.containsWater(world, adjacent.down())) water = true;
            }
            world.setBlockToAir(pos);
            BlockPos previous = WATER_PROTECTED_EXPLOSION.get();
            if (water) WATER_PROTECTED_EXPLOSION.set(pos.toImmutable());
            try {
                world.newExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, true);
            } finally {
                if (previous == null) WATER_PROTECTED_EXPLOSION.remove();
                else WATER_PROTECTED_EXPLOSION.set(previous);
            }
        }
        return true;
    }

    public static void protectWaterloggedExplosion(ExplosionEvent.Detonate event) {
        BlockPos pos = WATER_PROTECTED_EXPLOSION.get();
        if (pos != null && event.getExplosion().getPosition().squareDistanceTo(pos.getX() + .5D,
                pos.getY() + .5D, pos.getZ() + .5D) == 0.0D) event.getAffectedBlocks().clear();
    }

    @Nullable public static BlockPos safePosition(World world, BlockPos anchor) {
        net.minecraft.util.math.Vec3d position = safeLocation(world, anchor);
        return position == null ? null : new BlockPos(position);
    }

    @Nullable public static net.minecraft.util.math.Vec3d safeLocation(World world, BlockPos anchor) {
        if (world.provider.getDimension() != -1) return null;
        IBlockState state = world.getBlockState(anchor);
        if (!(state.getBlock() instanceof BlockRespawnAnchor) || state.getValue(CHARGE) == 0) return null;
        for (boolean checkDangerous : new boolean[] {true, false}) {
            for (int height : new int[] {0, -1, 1}) {
                for (int[] offset : OFFSETS) {
                    net.minecraft.util.math.Vec3d position = safe(world, anchor.add(offset[0], height, offset[1]), checkDangerous);
                    if (position != null) return position;
                }
            }
            net.minecraft.util.math.Vec3d position = safe(world, anchor.up(), checkDangerous);
            if (position != null) return position;
        }
        return null;
    }

    @Nullable private static net.minecraft.util.math.Vec3d safe(World world, BlockPos pos, boolean checkDangerous) {
        if (checkDangerous && dangerous(world.getBlockState(pos))) return null;
        AxisAlignedBB floor = floor(world, pos);
        AxisAlignedBB below = floor == null ? floor(world, pos.down()) : null;
        double height = floor != null ? floor.maxY : below != null ? below.maxY - 1.0D : Double.NEGATIVE_INFINITY;
        if (!Double.isFinite(height) || height >= 1.0D
                || checkDangerous && height <= 0.0D && dangerous(world.getBlockState(pos.down()))) return null;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5D;
        AxisAlignedBB bounds = new AxisAlignedBB(x - 0.3D, y, z - 0.3D, x + 0.3D, y + 1.8D, z + 0.3D);
        if (!world.getWorldBorder().contains(bounds) || !world.getCollisionBoxes(null, bounds).isEmpty()
                || world.getBlockState(pos).getBlock() == Blocks.END_PORTAL
                || world.getBlockState(pos.up()).getBlock() == Blocks.END_PORTAL
                || world.getBlockState(pos).getBlock() == Blocks.END_GATEWAY
                || world.getBlockState(pos.up()).getBlock() == Blocks.END_GATEWAY) return null;
        return new net.minecraft.util.math.Vec3d(x, y, z);
    }

    @Nullable private static AxisAlignedBB floor(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isLadder(state, world, pos, null)
                || state.getBlock() instanceof net.minecraft.block.BlockTrapDoor
                && state.getValue(net.minecraft.block.BlockTrapDoor.OPEN)) return null;
        return state.getCollisionBoundingBox(world, pos);
    }

    private static boolean dangerous(IBlockState state) {
        return state.getMaterial() == Material.LAVA || state.getMaterial() == Material.CACTUS
                || state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.MAGMA
                || state.getBlock() instanceof BlockCampfire;
    }

    public static void consume(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockRespawnAnchor && state.getValue(CHARGE) > 0) {
            world.setBlockState(pos, state.withProperty(CHARGE, state.getValue(CHARGE) - 1), 3);
            world.playSound(null, pos, FFDSounds.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (state.getValue(CHARGE) == 0) return;
        if (random.nextInt(100) == 0) world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, FFDSounds.RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        FarmerFutureDelight.proxy.spawnReversePortalParticle(world, pos.getX() + 1.0D - random.nextDouble(),
                pos.getY() + 1.0D, pos.getZ() + 1.0D - random.nextDouble(), random.nextFloat() * .04D);
    }

    @Override public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        int charges = state.getValue(CHARGE);
        return charges == 0 ? 0 : Math.max(1, charges * 15 / 4);
    }
    @Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }
    @Override public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return state.getValue(CHARGE) * 15 / 4;
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, CHARGE); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(CHARGE); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(CHARGE, Math.min(4, meta)); }
}
