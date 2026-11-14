package xy177.farmersfuturedelight.client;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.common.FFDLodestoneCompass;

public final class FFDLodestoneNeedle {
    private FFDLodestoneNeedle() {
    }

    public static void install() {
        ResourceLocation angle = new ResourceLocation("angle");
        IItemPropertyGetter original = Items.COMPASS.getPropertyGetter(angle);
        Wobbler tracked = new Wobbler();
        Wobbler untracked = new Wobbler();
        Random random = new Random();
        Items.COMPASS.addPropertyOverride(angle, (stack, world, holder) -> {
            if (!FFDLodestoneCompass.isBound(stack)) return original.apply(stack, world, holder);
            Entity entity = holder != null ? holder : stack.getItemFrame();
            if (entity == null) return 0.0F;
            if (world == null) world = entity.world;
            BlockPos pos = FFDLodestoneCompass.target(stack, world);
            long time = world.getTotalWorldTime();
            if (pos == null || pos.distanceSqToCenter(entity.posX, entity.posY, entity.posZ) < 1.0E-5D) {
                if (untracked.lastTick != time) untracked.update(time, random.nextFloat());
                int seed = System.identityHashCode(stack) * 1327217883;
                return MathHelper.positiveModulo(untracked.angle + seed / 2147483648.0F, 1.0F);
            }
            double yaw = holder != null ? holder.rotationYaw : 180 + entity.getHorizontalFacing().getHorizontalIndex() * 90;
            double direction = Math.atan2(pos.getZ() + 0.5D - entity.posZ, pos.getX() + 0.5D - entity.posX);
            float heading = MathHelper.positiveModulo((float) (.75D - yaw / 360.0D), 1.0F);
            if (holder != null && holder == Minecraft.getMinecraft().player) {
                if (tracked.lastTick != time) tracked.update(time, heading);
                heading = tracked.angle;
            }
            return MathHelper.positiveModulo(heading + (float) (direction / (2.0D * Math.PI)), 1.0F);
        });
    }

    private static final class Wobbler {
        private long lastTick = Long.MIN_VALUE;
        private float angle;
        private float velocity;

        private void update(long time, float target) {
            lastTick = time;
            float difference = MathHelper.positiveModulo(target - angle + .5F, 1.0F) - .5F;
            velocity = (velocity + difference * .1F) * .8F;
            angle = MathHelper.positiveModulo(angle + velocity, 1.0F);
        }
    }
}
