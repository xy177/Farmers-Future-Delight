package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockFutureCauldron extends BlockCauldron {
    public enum Content {
        LAVA,
        POWDER_SNOW
    }

    private final Content content;

    public BlockFutureCauldron(String name, Content content) {
        this.content = content;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(2.0F);
        setResistance(2.0F);
        setDefaultState(blockState.getBaseState().withProperty(LEVEL,
                content == Content.LAVA ? 3 : 1));
        if (content == Content.LAVA) {
            setLightLevel(1.0F);
        }
    }

    public Content getContent() {
        return content;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!FFDConfig.modernCauldronFeatures || player.getHeldItem(hand).getItem() != Items.BUCKET) {
            return false;
        }
        if (content == Content.POWDER_SNOW && state.getValue(LEVEL) < 3) {
            return true;
        }
        ItemStack filled = content == Content.LAVA ? new ItemStack(Items.LAVA_BUCKET)
                : FFDItems.effectiveStack(FFDItems.POWDER_SNOW_BUCKET);
        if (filled.isEmpty()) {
            return false;
        }
        if (!world.isRemote) {
            replaceHeldBucket(player, hand, filled);
            player.addStat(StatList.CAULDRON_USED);
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState(), 3);
            world.updateComparatorOutputLevel(pos, Blocks.CAULDRON);
            world.playSound(null, pos,
                    content == Content.LAVA ? net.minecraft.init.SoundEvents.ITEM_BUCKET_FILL_LAVA
                            : FFDSounds.BUCKET_FILL_POWDER_SNOW,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    private static void replaceHeldBucket(EntityPlayer player, EnumHand hand, ItemStack filled) {
        if (player.capabilities.isCreativeMode) {
            return;
        }
        ItemStack empty = player.getHeldItem(hand);
        empty.shrink(1);
        if (empty.isEmpty()) {
            player.setHeldItem(hand, filled);
        } else if (!player.inventory.addItemStackToInventory(filled)) {
            player.dropItem(filled, false);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state,
                                          Entity entity) {
        double contentHeight = content == Content.LAVA ? 0.9375D
                : (6.0D + 3.0D * state.getValue(LEVEL)) / 16.0D;
        if (entity.getEntityBoundingBox().minY > pos.getY() + contentHeight) {
            return;
        }
        if (content == Content.LAVA) {
            entity.setFire(15);
            entity.attackEntityFrom(DamageSource.LAVA, 4.0F);
            entity.getEntityData().setInteger("ffdTicksFrozen", 0);
        } else if (!world.isRemote && entity.isBurning()) {
            entity.extinguish();
            int waterLevel = state.getValue(LEVEL) - 1;
            world.setBlockState(pos, waterLevel <= 0 ? Blocks.CAULDRON.getDefaultState()
                    : Blocks.CAULDRON.getDefaultState().withProperty(LEVEL, waterLevel), 3);
            world.updateComparatorOutputLevel(pos, Blocks.CAULDRON);
        }
    }

    public boolean canReceiveDrip(IBlockState state, boolean water) {
        return false;
    }

    public boolean receiveDrip(World world, BlockPos pos, IBlockState state, boolean water) {
        if (!canReceiveDrip(state, water)) {
            return false;
        }
        world.setBlockState(pos, state.withProperty(LEVEL, state.getValue(LEVEL) + 1), 3);
        return true;
    }

    @Override
    public void fillWithRain(World world, BlockPos pos) {
        if (!FFDConfig.modernCauldronFeatures || content != Content.POWDER_SNOW
                || world.rand.nextFloat() >= FFDConfig.cauldronSnowFillChance) {
            return;
        }
        float temperature = world.getBiome(pos).getTemperature(pos);
        if (world.getBiomeProvider().getTemperatureAtHeight(temperature, pos.getY()) >= 0.15F) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        int level = state.getValue(LEVEL);
        if (level < 3) {
            world.setBlockState(pos, state.withProperty(LEVEL, level + 1), 2);
            world.updateComparatorOutputLevel(pos, this);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.CAULDRON;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Items.CAULDRON);
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return content == Content.LAVA ? 3 : state.getValue(LEVEL);
    }
}
