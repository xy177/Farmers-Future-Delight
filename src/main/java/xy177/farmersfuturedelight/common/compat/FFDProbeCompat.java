package xy177.farmersfuturedelight.common.compat;

import java.lang.reflect.Method;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockComposter;
import xy177.farmersfuturedelight.common.block.BlockFutureCauldron;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRestocking;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

public final class FFDProbeCompat {
    private FFDProbeCompat() {
    }

    public static boolean legacyVillagerInfo(Entity entity, Object info) {
        if (!xy177.farmersfuturedelight.common.FFDConfig.modernVillagerSystem
                || !(entity instanceof EntityVillager)) return false;
        if (entity.getClass() != EntityVillager.class) return true;
        EntityVillager villager = (EntityVillager) entity;
        text(info, "{*ffd.probe.profession*}: {*" + FFDVillagerTypes.professionNameKey(villager) + "*}");
        if (!FFDProfessionWorkstations.isProfessionUnassigned(villager) && FFDVillagerRestocking.isManaged(villager)) {
            text(info, "{*ffd.probe.level*}: {*ffd.merchant.level."
                    + Math.max(1, Math.min(5, FFDVillagerRestocking.getLevel(villager))) + "*}");
        }
        return false;
    }

    public static java.util.List<String> wailaVillager(Entity entity, java.util.List<String> lines) {
        if (!xy177.farmersfuturedelight.common.FFDConfig.modernVillagerSystem
                || entity.getClass() != EntityVillager.class) return null;
        lines.add(new net.minecraft.util.text.TextComponentTranslation("hud.msg.career",
                new net.minecraft.util.text.TextComponentTranslation(
                        FFDVillagerTypes.professionNameKey((EntityVillager) entity))).getUnformattedText());
        return lines;
    }

    public static boolean cauldron(Object info, IBlockState state) {
        if (!(state.getBlock() instanceof BlockFutureCauldron)) return false;
        BlockFutureCauldron block = (BlockFutureCauldron) state.getBlock();
        ItemStack content = new ItemStack(block.getContent() == BlockFutureCauldron.Content.LAVA
                ? Items.LAVA_BUCKET : FFDItems.POWDER_SNOW_BUCKET);
        invoke(info, "item", ItemStack.class, content);
        text(info, "{*" + content.getUnlocalizedName() + ".name*}: "
                + state.getValue(BlockFutureCauldron.LEVEL) + "/3");
        return true;
    }

    public static void blockInfo(Object info, World world, IBlockState state, Object hit, boolean nativeCauldronInfo) {
        if (!nativeCauldronInfo) cauldron(info, state);
        if (state.getBlock() instanceof BlockComposter) {
            text(info, "{*ffd.probe.compost*}: " + state.getValue(BlockComposter.LEVEL) + "/8");
        }
        if (hit == null) return;
        try {
            BlockPos position = (BlockPos) hit.getClass().getMethod("getPos").invoke(hit);
            TileEntity tile = world.getTileEntity(position);
            if (tile instanceof TileEntityCookingFurnace) {
                TileEntityCookingFurnace furnace = (TileEntityCookingFurnace) tile;
                text(info, "{*ffd.probe.progress*}: " + furnace.getField(2) + "/" + furnace.getField(3));
                text(info, "{*ffd.probe.fuel*}: " + furnace.getField(0));
            }
        } catch (ReflectiveOperationException exception) {
            org.apache.logging.log4j.LogManager.getLogger("FFD Probe Compatibility").debug("Cannot read probe hit", exception);
        }
    }

    private static void text(Object target, String value) {
        invoke(target, "text", String.class, value);
    }

    private static void invoke(Object target, String name, Class<?> type, Object value) {
        try {
            Method method = target.getClass().getMethod(name, type);
            method.invoke(target, value);
        } catch (ReflectiveOperationException exception) {
            org.apache.logging.log4j.LogManager.getLogger("FFD Probe Compatibility").debug("Cannot append probe info", exception);
        }
    }
}
