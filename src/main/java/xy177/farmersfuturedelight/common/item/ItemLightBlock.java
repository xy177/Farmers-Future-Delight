package xy177.farmersfuturedelight.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.block.BlockLight;

/** Command-only item for placing and inspecting administrator light blocks. */
public class ItemLightBlock extends ItemBlock {
    public ItemLightBlock(BlockLight block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(64);
        // Modern light blocks are operator-only rather than ordinary creative items.
        setCreativeTab(null);
    }

    @Override
    public int getMetadata(int damage) {
        return Math.max(0, Math.min(15, damage));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        // Keep the item out of normal creative tabs; obtain it with /give, as in 1.17.
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        tooltip.add(I18n.format("item.farmers_future_delight.light.level", stack.getMetadata()));
    }
}
