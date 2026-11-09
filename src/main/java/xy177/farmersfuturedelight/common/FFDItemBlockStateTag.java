package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDItemBlockStateTag {
    private FFDItemBlockStateTag() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.isCanceled() || event.getWorld().isRemote) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        if (!stack.getTagCompound().hasKey("BlockStateTag", 10)) {
            return;
        }
        List<BlockPos> positions = new ArrayList<>();
        if (event instanceof BlockEvent.MultiPlaceEvent) {
            for (net.minecraftforge.common.util.BlockSnapshot snapshot
                    : ((BlockEvent.MultiPlaceEvent) event).getReplacedBlockSnapshots()) {
                positions.add(snapshot.getPos());
            }
        } else {
            positions.add(event.getPos());
        }
        World world = event.getWorld();
        for (BlockPos pos : positions) {
            IBlockState state = world.getBlockState(pos);
            IBlockState updated = applyItemStateTag(stack, state);
            if (updated != state) {
                world.setBlockState(pos, updated, 3);
            }
        }
    }

    public static IBlockState applyItemStateTag(ItemStack stack, IBlockState state) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey("BlockStateTag", 10)) {
            return state;
        }
        return applyStateTag(state, stack.getTagCompound().getCompoundTag("BlockStateTag"));
    }

    private static IBlockState applyStateTag(IBlockState state, NBTTagCompound stateTag) {
        IBlockState updated = state;
        for (String key : stateTag.getKeySet()) {
            if (stateTag.getTagId(key) != 8) {
                continue;
            }
            IProperty<?> property = findProperty(updated, key);
            if (property != null) {
                updated = withPropertyValue(updated, property,
                        ((NBTTagString) stateTag.getTag(key)).getString());
            }
        }
        return updated;
    }

    private static IProperty<?> findProperty(IBlockState state, String name) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withPropertyValue(IBlockState state, IProperty property,
                                                 String value) {
        com.google.common.base.Optional parsed = property.parseValue(value);
        return parsed.isPresent()
                ? state.withProperty(property, (Comparable) parsed.get()) : state;
    }
}
