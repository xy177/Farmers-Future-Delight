package xy177.farmersfuturedelight.core;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDSoulFireHooks {
    private static final List<List<AxisAlignedBB>> FIRE_OUTLINES = fireOutlines();
    private FFDSoulFireHooks() {
    }

    private static List<List<AxisAlignedBB>> fireOutlines() {
        List<List<AxisAlignedBB>> outlines = new ArrayList<>();
        AxisAlignedBB[] faces = {
                new AxisAlignedBB(0, 0, 0, 1, 1, 0.0625D),
                new AxisAlignedBB(0.9375D, 0, 0, 1, 1, 1),
                new AxisAlignedBB(0, 0, 0.9375D, 1, 1, 1),
                new AxisAlignedBB(0, 0, 0, 0.0625D, 1, 1),
                new AxisAlignedBB(0, 0.9375D, 0, 1, 1, 1)
        };
        for (int mask = 0; mask < 32; mask++) {
            List<AxisAlignedBB> boxes = new ArrayList<>();
            for (int face = 0; face < faces.length; face++) {
                if ((mask & (1 << face)) != 0) boxes.add(faces[face]);
            }
            if (boxes.isEmpty()) boxes.add(new AxisAlignedBB(0, 0, 0, 1, 0.0625D, 1));
            outlines.add(Collections.unmodifiableList(boxes));
        }
        return outlines;
    }

    public static List<AxisAlignedBB> fireOutline(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = state.getActualState(world, pos);
        int mask = (state.getValue(BlockFire.NORTH) ? 1 : 0)
                | (state.getValue(BlockFire.EAST) ? 2 : 0)
                | (state.getValue(BlockFire.SOUTH) ? 4 : 0)
                | (state.getValue(BlockFire.WEST) ? 8 : 0)
                | (state.getValue(BlockFire.UPPER) ? 16 : 0);
        return FIRE_OUTLINES.get(mask);
    }

    public static AxisAlignedBB fireBounds(IBlockState state, IBlockAccess world, BlockPos pos) {
        List<AxisAlignedBB> boxes = fireOutline(state, world, pos);
        AxisAlignedBB bounds = boxes.get(0);
        for (int index = 1; index < boxes.size(); index++) bounds = bounds.union(boxes.get(index));
        return bounds;
    }

    public static RayTraceResult fireRayTrace(IBlockState state, World world, BlockPos pos,
                                              Vec3d start, Vec3d end) {
        RayTraceResult closest = null;
        double distance = Double.MAX_VALUE;
        for (AxisAlignedBB box : fireOutline(state, world, pos)) {
            RayTraceResult hit = box.offset(pos).calculateIntercept(start, end);
            if (hit != null && start.squareDistanceTo(hit.hitVec) < distance) {
                distance = start.squareDistanceTo(hit.hitVec);
                closest = new RayTraceResult(hit.hitVec, hit.sideHit, pos);
            }
        }
        return closest;
    }

    public static boolean isSoulBase(IBlockState state) {
        return state != null && (state.getBlock() == Blocks.SOUL_SAND
                || state.getBlock() == FFDItems.effectiveBlock(FFDBlocks.SOUL_SOIL));
    }

    public static boolean canPlaceFire(World world, BlockPos pos) {
        return isSoulBase(world.getBlockState(pos.down()))
                && FFDItems.isBlockRegistered(FFDBlocks.SOUL_FIRE);
    }

    public static boolean convertFire(World world, BlockPos pos) {
        if (!FFDItems.isBlockRegistered(FFDBlocks.SOUL_FIRE)
                || world.getBlockState(pos).getBlock() != Blocks.FIRE
                || !isSoulBase(world.getBlockState(pos.down()))) return false;
        IBlockState fire = FFDBlocks.SOUL_FIRE.getDefaultState();
        if (world.provider.getDimensionType().getId() > 0 || !Blocks.PORTAL.trySpawnPortal(world, pos)) {
            world.setBlockState(pos, fire, 3);
        }
        return true;
    }

    public static Predicate<IBlockState> witherBase(Predicate<IBlockState> original) {
        return state -> original.apply(state) || state != null
                && state.getBlock() == FFDItems.effectiveBlock(FFDBlocks.SOUL_SOIL);
    }

    public static boolean extinguishFire(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        BlockPos firePos = pos.offset(side);
        if (world.getBlockState(firePos).getBlock() != FFDBlocks.SOUL_FIRE) return false;
        world.playEvent(player, 1009, firePos, 0);
        world.setBlockToAir(firePos);
        return true;
    }

    public static int fireDamage(int damage, Entity entity) {
        if (damage != 1 || !FFDItems.isBlockRegistered(FFDBlocks.SOUL_FIRE)) return damage;
        AxisAlignedBB bounds = entity.getEntityBoundingBox().shrink(0.001D);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(MathHelper.floor(bounds.minX),
                MathHelper.floor(bounds.minY), MathHelper.floor(bounds.minZ),
                MathHelper.ceil(bounds.maxX) - 1, MathHelper.ceil(bounds.maxY) - 1,
                MathHelper.ceil(bounds.maxZ) - 1)) {
            if (entity.world.getBlockState(pos).getBlock() == FFDBlocks.SOUL_FIRE) return 2;
        }
        return damage;
    }
}
