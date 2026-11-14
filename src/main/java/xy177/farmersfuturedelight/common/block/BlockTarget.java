package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class BlockTarget extends Block {
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);

    public BlockTarget() {
        super(Material.GRASS);
        setRegistryName(FarmerFutureDelight.MODID, "target");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".target");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(SoundType.PLANT);
        setDefaultState(blockState.getBaseState().withProperty(POWER, 0));
    }

    public void hit(World world, IBlockState state, RayTraceResult hit, Entity projectile) {
        BlockPos pos = hit.getBlockPos();
        if (world.isUpdateScheduled(pos, this) || state.getValue(POWER) != 0) return;
        Vec3d point = hit.hitVec;
        double x = Math.abs(point.x - Math.floor(point.x) - 0.5D);
        double y = Math.abs(point.y - Math.floor(point.y) - 0.5D);
        double z = Math.abs(point.z - Math.floor(point.z) - 0.5D);
        EnumFacing.Axis axis = hit.sideHit.getAxis();
        double distance = axis == EnumFacing.Axis.X ? Math.max(y, z)
                : axis == EnumFacing.Axis.Y ? Math.max(x, z) : Math.max(x, y);
        int strength = Math.max(1, MathHelper.ceil(15.0D * (1.0D - 2.0D * distance)));
        Entity shooter = projectile instanceof EntityArrow ? ((EntityArrow) projectile).shootingEntity
                : projectile instanceof net.minecraft.entity.projectile.EntityThrowable
                ? ((net.minecraft.entity.projectile.EntityThrowable) projectile).getThrower() : null;
        if (!world.isRemote && strength == 15 && shooter instanceof net.minecraft.entity.player.EntityPlayerMP
                && (shooter.posX - point.x) * (shooter.posX - point.x)
                + (shooter.posZ - point.z) * (shooter.posZ - point.z) >= 900.0D) {
            xy177.farmersfuturedelight.common.advancement.FFDAdvancements.BULLSEYE
                    .trigger((net.minecraft.entity.player.EntityPlayerMP) shooter);
        }
        world.setBlockState(pos, state.withProperty(POWER, strength), 3);
        world.scheduleUpdate(pos, this, projectile instanceof EntityArrow ? 20 : 8);
    }

    @Override public boolean canProvidePower(IBlockState state) { return true; }

    @Override public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWER);
    }

    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (state.getValue(POWER) != 0) world.setBlockState(pos, state.withProperty(POWER, 0), 3);
    }

    @Override public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote && state.getValue(POWER) != 0 && !world.isUpdateScheduled(pos, this)) {
            world.setBlockState(pos, state.withProperty(POWER, 0), 18);
        }
    }

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, POWER); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(POWER); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(POWER, meta & 15); }
}
