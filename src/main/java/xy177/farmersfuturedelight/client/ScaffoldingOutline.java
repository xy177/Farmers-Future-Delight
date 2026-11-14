package xy177.farmersfuturedelight.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockScaffolding;
import xy177.farmersfuturedelight.core.FFDSoulFireHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class ScaffoldingOutline {
    private static final Map<List<AxisAlignedBB>, List<double[]>> EDGES = new HashMap<>();

    private ScaffoldingOutline() {
    }

    @SubscribeEvent
    public static void draw(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        EntityPlayer player = event.getPlayer();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = target.getBlockPos();
        IBlockState state = player.world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockScaffolding)
                && !(state.getBlock() instanceof BlockFire)) {
            return;
        }
        event.setCanceled(true);
        if (!player.world.getWorldBorder().contains(pos)) {
            return;
        }
        List<AxisAlignedBB> boxes = state.getBlock() instanceof BlockFire
                ? FFDSoulFireHooks.fireOutline(state, player.world, pos)
                : ((BlockScaffolding) state.getBlock()).outline(state, player.world,
                        pos, FarmerFutureDelight.proxy.isHoldingScaffolding());
        List<double[]> edges = EDGES.computeIfAbsent(boxes, ScaffoldingOutline::edges);
        double partial = event.getPartialTicks();
        double cameraX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partial;
        double cameraY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partial;
        double cameraZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partial;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        for (double[] edge : edges) {
            for (int vertex = 0; vertex < 2; vertex++) {
                int offset = vertex * 3;
                buffer.pos(pos.getX() + edge[offset] - cameraX, pos.getY() + edge[offset + 1] - cameraY,
                        pos.getZ() + edge[offset + 2] - cameraZ).color(0, 0, 0, 0.4F).endVertex();
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static List<double[]> edges(List<AxisAlignedBB> boxes) {
        List<double[]> result = new ArrayList<>();
        List<Double[]> coordinates = new ArrayList<>();
        for (int axis = 0; axis < 3; axis++) {
            TreeSet<Double> values = new TreeSet<>();
            for (AxisAlignedBB box : boxes) {
                values.add(axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ);
                values.add(axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ);
            }
            coordinates.add(values.toArray(new Double[0]));
        }
        for (int axis = 0; axis < 3; axis++) {
            int across = (axis + 1) % 3;
            int around = (axis + 2) % 3;
            Double[] alongValues = coordinates.get(axis);
            for (int along = 1; along < alongValues.length; along++) {
                for (double cross : coordinates.get(across)) {
                    for (double depth : coordinates.get(around)) {
                        int mask = 0;
                        for (int corner = 0; corner < 4; corner++) {
                            double[] sample = new double[3];
                            sample[axis] = (alongValues[along - 1] + alongValues[along]) * 0.5D;
                            sample[across] = cross + ((corner & 1) == 0 ? -1 : 1) * 0.001D;
                            sample[around] = depth + ((corner & 2) == 0 ? -1 : 1) * 0.001D;
                            for (AxisAlignedBB box : boxes) {
                                if (box.minX < sample[0] && sample[0] < box.maxX
                                        && box.minY < sample[1] && sample[1] < box.maxY
                                        && box.minZ < sample[2] && sample[2] < box.maxZ) {
                                    mask |= 1 << corner;
                                    break;
                                }
                            }
                        }
                        if (mask == 0 || mask == 15 || mask == 3 || mask == 5 || mask == 10 || mask == 12) {
                            continue;
                        }
                        double[] edge = new double[6];
                        edge[axis] = alongValues[along - 1];
                        edge[axis + 3] = alongValues[along];
                        edge[across] = edge[across + 3] = cross;
                        edge[around] = edge[around + 3] = depth;
                        result.add(edge);
                    }
                }
            }
        }
        return result;
    }
}
