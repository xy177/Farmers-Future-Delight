package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDCreativeTab extends CreativeTabs {
    public static final FFDCreativeTab INSTANCE = new FFDCreativeTab();

    private FFDCreativeTab() {
        super(FarmerFutureDelight.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        ItemStack berries = FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
        return berries.isEmpty() ? new ItemStack(Items.APPLE) : berries;
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> items) {
        super.displayAllRelevantItems(items);
        items.removeIf(stack -> !FFDItems.shouldDisplayInCreativeTab(stack));
        regroupDeepslateOres(items);
        regroupRawOres(items);
    }

    private static void regroupDeepslateOres(NonNullList<ItemStack> items) {
        List<Item> order = new ArrayList<>();
        order.add(FFDItems.COPPER_ORE);
        order.add(FFDItems.DEEPSLATE_COAL_ORE);
        order.add(FFDItems.DEEPSLATE_IRON_ORE);
        order.add(FFDItems.DEEPSLATE_COPPER_ORE);
        order.add(FFDItems.DEEPSLATE_GOLD_ORE);
        order.add(FFDItems.DEEPSLATE_REDSTONE_ORE);
        order.add(FFDItems.DEEPSLATE_LAPIS_ORE);
        order.add(FFDItems.DEEPSLATE_DIAMOND_ORE);
        order.add(FFDItems.DEEPSLATE_EMERALD_ORE);
        order.addAll(FFDDeepslateOreCompat.items());

        List<ItemStack> grouped = new ArrayList<>();
        int firstIndex = -1;
        for (Item item : order) {
            for (int index = 0; index < items.size(); index++) {
                if (items.get(index).getItem() == item) {
                    if (firstIndex < 0 || index < firstIndex) {
                        firstIndex = index;
                    }
                    grouped.add(items.get(index));
                    break;
                }
            }
        }
        if (firstIndex < 0) {
            return;
        }
        items.removeIf(stack -> order.contains(stack.getItem()));
        items.addAll(firstIndex, grouped);
    }

    private static void regroupRawOres(NonNullList<ItemStack> items) {
        List<ItemStack> rawOres = new ArrayList<>();
        List<ItemStack> rawOreBlocks = new ArrayList<>();
        int firstRawIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i).getItem();
            if (isRawOre(item)) {
                if (firstRawIndex < 0) {
                    firstRawIndex = i;
                }
                rawOres.add(items.get(i));
            } else if (isRawOreBlock(item)) {
                if (firstRawIndex < 0) {
                    firstRawIndex = i;
                }
                rawOreBlocks.add(items.get(i));
            }
        }
        if (firstRawIndex < 0) {
            return;
        }
        items.removeIf(stack -> isRawOre(stack.getItem()) || isRawOreBlock(stack.getItem()));
        items.addAll(firstRawIndex, rawOres);
        items.addAll(firstRawIndex + rawOres.size(), rawOreBlocks);
    }

    private static boolean isRawOre(Item item) {
        for (Item rawOre : FFDItems.RAW_ORE_ITEMS) {
            if (item == rawOre) {
                return true;
            }
        }
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (item == entry.item()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRawOreBlock(Item item) {
        for (Item rawOreBlock : FFDItems.RAW_ORE_BLOCK_ITEMS) {
            if (item == rawOreBlock) {
                return true;
            }
        }
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (item == entry.blockItem()) {
                return true;
            }
        }
        return false;
    }
}
