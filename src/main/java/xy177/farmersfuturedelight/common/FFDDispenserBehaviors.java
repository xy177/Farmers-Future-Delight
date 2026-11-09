package xy177.farmersfuturedelight.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.item.ItemAxolotlBucket;
import xy177.farmersfuturedelight.common.item.ItemFishBucket;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;

public final class FFDDispenserBehaviors {
    private FFDDispenserBehaviors() {
    }

    public static void register() {
        registerUnderwaterBonemeal();
        registerWaterBottles();
        registerShearing();
        registerFireworks();
        for (Item item : new Item[] {FFDItems.COD_BUCKET, FFDItems.SALMON_BUCKET,
                FFDItems.PUFFERFISH_BUCKET, FFDItems.TROPICAL_FISH_BUCKET}) {
            if (FFDItems.isItemRegistered(item)) {
                registerFishBucket((ItemFishBucket) item);
            }
        }
        if (FFDItems.isBlockRegistered(FFDBlocks.BEE_NEST)
                || FFDItems.isBlockRegistered(FFDBlocks.BEEHIVE)) {
            registerBeehiveHarvest(Items.SHEARS, true);
            registerBeehiveHarvest(Items.GLASS_BOTTLE, false);
        }
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

    private static void registerFireworks() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIREWORKS,
                new BehaviorDefaultDispenseItem() {
                    @Override
                    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                        World world = source.getWorld();
                        ItemStack projectile = stack.copy();
                        projectile.setCount(1);
                        net.minecraft.entity.item.EntityFireworkRocket rocket =
                                new net.minecraft.entity.item.EntityFireworkRocket(world,
                                        source.getX() + facing.getFrontOffsetX() * 0.5000099999997474D,
                                        source.getY() + facing.getFrontOffsetY() * 0.5000099999997474D,
                                        source.getZ() + facing.getFrontOffsetZ() * 0.5000099999997474D,
                                        projectile);
                        xy177.farmersfuturedelight.core.FFDFireworkHooks.setShotAtAngle(rocket, true);
                        double spread = 0.0172275D;
                        rocket.motionX = (facing.getFrontOffsetX()
                                + (world.rand.nextDouble() - world.rand.nextDouble()) * spread) * 0.5D;
                        rocket.motionY = (facing.getFrontOffsetY()
                                + (world.rand.nextDouble() - world.rand.nextDouble()) * spread) * 0.5D;
                        rocket.motionZ = (facing.getFrontOffsetZ()
                                + (world.rand.nextDouble() - world.rand.nextDouble()) * spread) * 0.5D;
                        world.spawnEntity(rocket);
                        stack.shrink(1);
                        return stack;
                    }

                    @Override
                    protected void playDispenseSound(IBlockSource source) {
                        source.getWorld().playEvent(1004, source.getBlockPos(), 0);
                    }
                });
    }

    private static void registerWaterBottles() {
        final IBehaviorDispenseItem previous =
                BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.GLASS_BOTTLE);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.GLASS_BOTTLE, (source, stack) -> {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos target = source.getBlockPos().offset(facing);
            net.minecraft.block.state.IBlockState fluid =
                    xy177.farmersfuturedelight.api.WaterloggedBlockApi
                            .getFluidState(source.getWorld(), target);
            if (fluid.getMaterial() != net.minecraft.block.material.Material.WATER) {
                if (previous.getClass() == BehaviorDefaultDispenseItem.class) {
                    BehaviorDefaultDispenseItem.doDispense(source.getWorld(), stack.splitStack(1),
                            6, facing, BlockDispenser.getDispensePosition(source));
                    source.getWorld().playEvent(1001, source.getBlockPos(), 0);
                    source.getWorld().playEvent(2000, source.getBlockPos(), particleData(facing));
                    return stack;
                }
                return previous.dispense(source, stack);
            }
            ItemStack filled = net.minecraft.potion.PotionUtils.addPotionToItemStack(
                    new ItemStack(Items.POTIONITEM), net.minecraft.init.PotionTypes.WATER);
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
            source.getWorld().playEvent(2000, source.getBlockPos(), particleData(facing));
            return consumeWithRemainder(source, stack, filled);
        });
    }

    private static void registerShearing() {
        final IBehaviorDispenseItem previous =
                BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.SHEARS);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SHEARS, (source, stack) -> {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos target = source.getBlockPos().offset(facing);
            World world = source.getWorld();
            for (net.minecraft.entity.EntityLivingBase entity : world.getEntitiesWithinAABB(
                    net.minecraft.entity.EntityLivingBase.class,
                    new net.minecraft.util.math.AxisAlignedBB(target))) {
                if (!entity.isEntityAlive() || !(entity instanceof net.minecraftforge.common.IShearable)) {
                    continue;
                }
                net.minecraftforge.common.IShearable shearable =
                        (net.minecraftforge.common.IShearable) entity;
                if (!shearable.isShearable(stack, world, target)) {
                    continue;
                }
                for (ItemStack drop : shearable.onSheared(stack, world, target,
                        net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                                net.minecraft.init.Enchantments.FORTUNE, stack))) {
                    entity.entityDropItem(drop, 1.0F);
                }
                damageDispensedShears(stack, world);
                world.playEvent(1000, source.getBlockPos(), 0);
                world.playEvent(2000, source.getBlockPos(), particleData(facing));
                return stack;
            }
            if (previous.getClass() != BehaviorDefaultDispenseItem.class) {
                return previous.dispense(source, stack);
            }
            world.playEvent(1001, source.getBlockPos(), 0);
            world.playEvent(2000, source.getBlockPos(), particleData(facing));
            return stack;
        });
    }

    private static void registerUnderwaterBonemeal() {
        IBehaviorDispenseItem previous = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.DYE);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DYE, (source, stack) -> {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos target = source.getBlockPos().offset(facing);
            World world = source.getWorld();
            if (stack.getMetadata() != 15 || !FFDUnderwaterBonemeal.grow(stack, world, target, null)) {
                return previous.dispense(source, stack);
            }
            world.playEvent(2005, target, 0);
            world.playEvent(1000, source.getBlockPos(), 0);
            world.playEvent(2000, source.getBlockPos(), particleData(facing));
            return stack;
        });
    }

    private static void registerFishBucket(final ItemFishBucket bucket) {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(bucket,
                new BehaviorDefaultDispenseItem() {
                    @Override
                    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                        BlockPos target = source.getBlockPos().offset(facing);
                        World world = source.getWorld();
                        ItemStack entityData = stack.copy();
                        if (!bucket.tryPlaceContainedLiquid(null, world, target)) {
                            return super.dispenseStack(source, stack);
                        }
                        if (!world.isRemote) {
                            bucket.spawnFish(world, target, entityData);
                        }
                        return consumeWithRemainder(source, stack, new ItemStack(Items.BUCKET));
                    }
                });
    }

    private static void registerBeehiveHarvest(Item item, final boolean shears) {
        final IBehaviorDispenseItem previous = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item,
                new IBehaviorDispenseItem() {
                    @Override
                    public ItemStack dispense(IBlockSource source, ItemStack stack) {
                        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                        BlockPos target = source.getBlockPos().offset(facing);
                        World world = source.getWorld();
                        TileEntity tile = world.getTileEntity(target);
                        if (!(world.getBlockState(target).getBlock() instanceof BlockBeehive)
                                || !(tile instanceof TileEntityBeehive)) {
                            return previous.dispense(source, stack);
                        }
                        TileEntityBeehive hive = (TileEntityBeehive) tile;
                        ItemStack harvested = FFDItems.effectiveStack(
                                shears ? FFDItems.HONEYCOMB : FFDItems.HONEY_BOTTLE);
                        boolean success = hive.getHoneyLevel() >= TileEntityBeehive.MAX_HONEY_LEVEL
                                && !harvested.isEmpty();
                        if (!success && !shears) {
                            return previous.dispense(source, stack);
                        }
                        if (success) {
                            hive.setHoneyLevel(0);
                            hive.releaseAll(TileEntityBeehive.ReleaseStatus.BEE_RELEASED, null);
                            if (shears) {
                                world.playSound(null, target, FFDSounds.BEEHIVE_SHEAR,
                                        SoundCategory.BLOCKS, 1.0F, 1.0F);
                                for (int count = 0; count < 3; count++) {
                                    Block.spawnAsEntity(world, target, harvested.copy());
                                }
                                damageDispensedShears(stack, world);
                            } else {
                                stack = consumeWithRemainder(source, stack, harvested);
                            }
                        }
                        world.playEvent(success ? 1000 : 1001, source.getBlockPos(), 0);
                        world.playEvent(2000, source.getBlockPos(), particleData(facing));
                        return stack;
                    }
                });
    }

    private static void damageDispensedShears(ItemStack stack, World world) {
        if (stack.attemptDamageItem(1, world.rand, null)
                || stack.getItemDamage() >= stack.getMaxDamage()) {
            stack.shrink(1);
            stack.setItemDamage(0);
        }
    }

    private static ItemStack consumeWithRemainder(IBlockSource source, ItemStack stack,
                                                   ItemStack remainder) {
        stack.shrink(1);
        if (stack.isEmpty()) {
            return remainder;
        }
        if (((TileEntityDispenser) source.getBlockTileEntity()).addItemStack(remainder) < 0) {
            BehaviorDefaultDispenseItem.doDispense(source.getWorld(), remainder, 6,
                    source.getBlockState().getValue(BlockDispenser.FACING),
                    BlockDispenser.getDispensePosition(source));
        }
        return stack;
    }

    private static int particleData(EnumFacing facing) {
        return facing.getFrontOffsetX() + 1 + (facing.getFrontOffsetZ() + 1) * 3;
    }
}
