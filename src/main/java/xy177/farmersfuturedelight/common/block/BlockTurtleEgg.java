package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.EntityEntry;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockTurtleEgg extends Block {
    public static final PropertyInteger HATCH = PropertyInteger.create("hatch", 0, 2);
    public static final PropertyInteger EGGS = PropertyInteger.create("eggs", 1, 4);
    private static final long NIGHT_HATCH_START = 21062L;
    private static final long NIGHT_HATCH_END = 21905L;

    private static final AxisAlignedBB SINGLE_EGG_AABB =
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.75D, 0.4375D, 0.75D);
    private static final AxisAlignedBB MULTIPLE_EGGS_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.4375D, 0.9375D);

    public BlockTurtleEgg() {
        super(Material.CIRCUITS);
        setRegistryName(FarmerFutureDelight.MODID, "turtle_egg");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".turtle_egg");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(SoundType.METAL);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(HATCH, 0).withProperty(EGGS, 1));
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!entity.isSneaking()) {
            tryDestroyEgg(world, pos, world.getBlockState(pos), entity, 100);
        }
        super.onEntityWalk(world, pos, entity);
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (!(entity instanceof EntityZombie)) {
            tryDestroyEgg(world, pos, world.getBlockState(pos), entity, 3);
        }
        super.onFallenUpon(world, pos, entity, fallDistance);
    }

    private void tryDestroyEgg(World world, BlockPos pos, IBlockState state, Entity entity, int chance) {
        if (world.isRemote || state.getBlock() != this || world.rand.nextInt(chance) != 0
                || !canDestroyEgg(world, pos, entity)) {
            return;
        }
        decreaseEggs(world, pos, state);
    }

    private boolean canDestroyEgg(World world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityTurtle || entity instanceof EntityBat) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            return !world.getMinecraftServer().isBlockProtected(world, pos, (EntityPlayer) entity);
        }
        return entity instanceof EntityLivingBase && ForgeEventFactory.getMobGriefingEvent(world, entity);
    }

    public void decreaseEggs(World world, BlockPos pos, IBlockState state) {
        world.playSound(null, pos, FFDSounds.TURTLE_EGG_BREAK, SoundCategory.BLOCKS,
                0.7F, 0.9F + world.rand.nextFloat() * 0.2F);
        world.playEvent(2001, pos, Block.getStateId(state));
        int eggs = state.getValue(EGGS);
        if (eggs <= 1) {
            world.setBlockToAir(pos);
        } else {
            world.setBlockState(pos, state.withProperty(EGGS, eggs - 1), 2);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!FFDItems.isTurtleEnabled() || !isOnSand(world, pos)
                || !shouldUpdateHatchLevel(world, random)) {
            return;
        }

        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            world.playSound(null, pos, FFDSounds.TURTLE_EGG_CRACK, SoundCategory.BLOCKS,
                    0.7F, 0.9F + random.nextFloat() * 0.2F);
            world.setBlockState(pos, state.withProperty(HATCH, hatch + 1), 2);
            return;
        }

        world.playSound(null, pos, FFDSounds.TURTLE_EGG_HATCH, SoundCategory.BLOCKS,
                0.7F, 0.9F + random.nextFloat() * 0.2F);
        int eggs = state.getValue(EGGS);
        world.setBlockToAir(pos);
        for (int index = 0; index < eggs; index++) {
            world.playEvent(2001, pos, Block.getStateId(state));
            Entity turtle = createTurtle(world);
            if (turtle == null) {
                continue;
            }
            if (turtle instanceof EntityAgeable) {
                ((EntityAgeable) turtle).setGrowingAge(-24000);
            }
            if (turtle instanceof EntityTurtle) {
                ((EntityTurtle) turtle).setHomePos(pos);
            }
            turtle.setLocationAndAngles(pos.getX() + 0.3D + index * 0.2D, pos.getY(),
                    pos.getZ() + 0.3D, 0.0F, 0.0F);
            world.spawnEntity(turtle);
        }
    }

    @Nullable
    private static Entity createTurtle(World world) {
        if (FFDEntities.isLocalTurtleEnabled()) {
            return new EntityTurtle(world);
        }
        EntityEntry external = FFDCompat.getExternalEntityEntry(
                FFDCompat.Feature.TURTLE, "turtle");
        return external == null ? null : external.newInstance(world);
    }

    private static boolean shouldUpdateHatchLevel(World world, Random random) {
        long dayTime = Math.floorMod(world.getWorldTime(), 24000L);
        float chance = dayTime >= NIGHT_HATCH_START && dayTime < NIGHT_HATCH_END
                ? 1.0F : FFDConfig.turtleEggHatchChance;
        return random.nextFloat() < chance;
    }

    public static boolean isOnSand(IBlockAccess world, BlockPos pos) {
        return isSand(world, pos.down());
    }

    public static boolean isSand(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == net.minecraft.init.Blocks.SAND
                && (state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.SAND
                || state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote && world instanceof WorldServer && isOnSand(world, pos)) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    15, 0.5D, 0.5D, 0.5D, 0.0D);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tileEntity, ItemStack tool) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (!world.isRemote && FFDItems.isTurtleEnabled()
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.TURTLE_EGG));
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        if (!world.isRemote) {
            decreaseEggs(world, pos, state);
        }
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.TURTLE_EGG);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(EGGS) == 1 ? SINGLE_EGG_AABB : MULTIPLE_EGGS_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos,
                                            EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(EGGS, (meta & 3) + 1)
                .withProperty(HATCH, (meta >> 2) & 3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(EGGS) - 1 | state.getValue(HATCH) << 2;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HATCH, EGGS);
    }
}
