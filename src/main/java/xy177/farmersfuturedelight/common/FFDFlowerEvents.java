package xy177.farmersfuturedelight.common;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDFlowerEvents {
    private FFDFlowerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onWitherKill(LivingDropsEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        World world = victim.world;
        if (world.isRemote || !FFDItems.isBlockRegistered(FFDBlocks.WITHER_ROSE)
                || !(event.getSource().getTrueSource() instanceof EntityWither)) {
            return;
        }
        BlockPos pos = victim.getPosition();
        if (ForgeEventFactory.getMobGriefingEvent(world, event.getSource().getTrueSource())
                && world.isAirBlock(pos) && FFDBlocks.WITHER_ROSE.canBlockStay(world, pos, FFDBlocks.WITHER_ROSE.getDefaultState())
                && world.setBlockState(pos, FFDBlocks.WITHER_ROSE.getDefaultState(), 3)) {
            return;
        }
        world.spawnEntity(new EntityItem(world, victim.posX, victim.posY, victim.posZ,
                new ItemStack(FFDItems.WITHER_ROSE)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExternalFlowerPotted(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().isEmpty() || event.getUseBlock() == Event.Result.DENY
                || event.getUseItem() == Event.Result.DENY
                || event.getWorld().getBlockState(event.getPos()).getBlock() != Blocks.FLOWER_POT) {
            return;
        }
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!(tile instanceof TileEntityFlowerPot)
                || !((TileEntityFlowerPot) tile).getFlowerItemStack().isEmpty()
                || !event.getEntityPlayer().canPlayerEdit(event.getPos(), face, event.getItemStack())) {
            return;
        }
        Item[] flowers = {FFDItems.CORNFLOWER, FFDItems.LILY_OF_THE_VALLEY, FFDItems.WITHER_ROSE,
                FFDItems.GOLDEN_DANDELION};
        Block[] pots = {FFDBlocks.POTTED_CORNFLOWER, FFDBlocks.POTTED_LILY_OF_THE_VALLEY, FFDBlocks.POTTED_WITHER_ROSE,
                FFDBlocks.POTTED_GOLDEN_DANDELION};
        for (int index = 0; index < flowers.length; index++) {
            Item local = flowers[index];
            ItemStack effective = FFDItems.effectiveStack(local);
            if (FFDItems.isBlockRegistered(pots[index]) && event.getItemStack().getItem() != local
                    && !effective.isEmpty() && ItemStack.areItemsEqual(effective, event.getItemStack())) {
                if (!event.getWorld().isRemote) {
                    local.onItemUse(event.getEntityPlayer(), event.getWorld(), event.getPos(),
                            event.getHand(), face, 0.5F, 0.5F, 0.5F);
                }
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
                return;
            }
        }
    }
}
