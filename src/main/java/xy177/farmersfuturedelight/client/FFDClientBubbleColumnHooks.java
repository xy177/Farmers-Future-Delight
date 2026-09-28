package xy177.farmersfuturedelight.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDBubbleColumnEvents;
import xy177.farmersfuturedelight.common.block.BlockBubbleColumn;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class FFDClientBubbleColumnHooks {
    private static EntityPlayerSP lastPlayer;
    private static boolean wasInBubbleColumn;

    private FFDClientBubbleColumnHooks() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.isGamePaused()) {
            return;
        }
        EntityPlayerSP player = mc.player;
        boolean firstTick = player != lastPlayer;
        if (firstTick || player == null) {
            wasInBubbleColumn = false;
            lastPlayer = player;
        }
        if (player == null || mc.world == null) {
            return;
        }
        AxisAlignedBB box = player.getEntityBoundingBox().grow(0.0D, -0.4F, 0.0D).shrink(1.0E-6D);
        IBlockState column = null;
        BlockPos min = new BlockPos(box.minX, box.minY, box.minZ);
        BlockPos max = new BlockPos(box.maxX, box.maxY, box.maxZ);
        if (player.world.isAreaLoaded(min, max)) {
            for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
                IBlockState state = player.world.getBlockState(pos);
                if (state.getBlock() instanceof BlockBubbleColumn) {
                    column = state;
                    break;
                }
            }
        }
        if (column != null && !wasInBubbleColumn && !firstTick && !player.isSpectator()) {
            player.playSound(column.getValue(BlockBubbleColumn.DRAG)
                    ? FFDSounds.BUBBLE_COLUMN_WHIRLPOOL_INSIDE
                    : FFDSounds.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
        }
        wasInBubbleColumn = column != null;
    }

    public static void applyBoatRotation(EntityBoat boat, float partialTicks) {
        float angle = FFDBubbleColumnEvents.getBoatBubbleAngle(boat, partialTicks);
        if (angle != 0.0F) {
            GlStateManager.rotate(angle, 1.0F, 0.0F, 1.0F);
        }
    }
}
