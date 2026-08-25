package xy177.farmersfuturedelight.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.item.ItemAxolotlBucket;

public final class FFDDispenserBehaviors {
    private FFDDispenserBehaviors() {
    }

    public static void register() {
        if (FFDItems.isItemRegistered(FFDItems.AXOLOTL_BUCKET)) {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(FFDItems.AXOLOTL_BUCKET,
                    new BehaviorDefaultDispenseItem() {
                        @Override
                        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                            BlockPos target = source.getBlockPos().offset(facing);
                            World world = source.getWorld();
                            ItemStack entityData = stack.copy();
                            if (!((ItemAxolotlBucket) FFDItems.AXOLOTL_BUCKET)
                                    .tryPlaceContainedLiquid(null, world, target)) {
                                return super.dispenseStack(source, stack);
                            }
                            if (!world.isRemote) {
                                ItemAxolotlBucket.spawnAxolotl(world, target, entityData);
                            }
                            stack.shrink(1);
                            return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
                        }
                    });
        }

        final Block powderSnow = FFDItems.effectiveBlock(FFDBlocks.POWDER_SNOW);
        final ItemStack powderSnowBucket = FFDItems.effectiveStack(FFDItems.POWDER_SNOW_BUCKET);
        if (powderSnow == null || powderSnowBucket.isEmpty()) {
            return;
        }
        if (FFDItems.isItemRegistered(FFDItems.POWDER_SNOW_BUCKET)) {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(FFDItems.POWDER_SNOW_BUCKET,
                    new BehaviorDefaultDispenseItem() {
                        @Override
                        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                            BlockPos target = source.getBlockPos().offset(facing);
                            World world = source.getWorld();
                            if (!world.mayPlace(powderSnow, target, true, facing, null)) {
                                return super.dispenseStack(source, stack);
                            }
                            world.setBlockState(target, powderSnow.getDefaultState(), 3);
                            world.playSound(null, target, FFDSounds.BUCKET_EMPTY_POWDER_SNOW,
                                    SoundCategory.BLOCKS, 1.0F, 1.0F);
                            stack.shrink(1);
                            return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
                        }
                    });
        }

        final IBehaviorDispenseItem previousBucketBehavior =
                BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.BUCKET);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BUCKET,
                new IBehaviorDispenseItem() {
                    private final BehaviorDefaultDispenseItem fallback =
                            new BehaviorDefaultDispenseItem();

                    @Override
                    public ItemStack dispense(IBlockSource source, ItemStack stack) {
                        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                        BlockPos target = source.getBlockPos().offset(facing);
                        World world = source.getWorld();
                        if (world.getBlockState(target).getBlock() != powderSnow) {
                            return previousBucketBehavior.dispense(source, stack);
                        }

                        if (!BlockPowderSnow.destroyPowderSnow(world, target, powderSnow)) {
                            return previousBucketBehavior.dispense(source, stack);
                        }
                        world.playSound(null, target, FFDSounds.BUCKET_FILL_POWDER_SNOW,
                                SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.playEvent(1000, source.getBlockPos(), 0);
                        world.playEvent(2000, source.getBlockPos(), particleData(facing));
                        stack.shrink(1);
                        ItemStack filled = powderSnowBucket.copy();
                        if (stack.isEmpty()) {
                            return filled;
                        }
                        if (((TileEntityDispenser) source.getBlockTileEntity())
                                .addItemStack(filled) < 0) {
                            fallback.dispense(source, filled);
                        }
                        return stack;
                    }
                });
    }

    private static int particleData(EnumFacing facing) {
        return facing.getFrontOffsetX() + 1 + (facing.getFrontOffsetZ() + 1) * 3;
    }
}
