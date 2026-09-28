package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
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
        addRegisteredTridentEnchantmentBooks(items);
        regroupDeepslateOres(items);
        regroupRawOres(items);
        regroupWoodItems(items);
    }

    private static void addRegisteredTridentEnchantmentBooks(NonNullList<ItemStack> items) {
        addRegisteredEnchantmentBooks(items, FFDEnchantments.LOYALTY);
        addRegisteredEnchantmentBooks(items, FFDEnchantments.IMPALING);
        addRegisteredEnchantmentBooks(items, FFDEnchantments.RIPTIDE);
        addRegisteredEnchantmentBooks(items, FFDEnchantments.CHANNELING);
    }

    private static void addRegisteredEnchantmentBooks(NonNullList<ItemStack> items,
                                                      Enchantment enchantment) {
        if (enchantment.getRegistryName() == null
                || ForgeRegistries.ENCHANTMENTS.getValue(enchantment.getRegistryName())
                != enchantment) {
            return;
        }
        for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
            items.add(ItemEnchantedBook.getEnchantedItemStack(
                    new EnchantmentData(enchantment, level)));
        }
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
        regroupItems(items, order);
    }

    private static void regroupWoodItems(NonNullList<ItemStack> items) {
        List<Item> order = new ArrayList<>();
        Collections.addAll(order, FFDItems.CRIMSON_STEM, FFDItems.WARPED_STEM);
        Collections.addAll(order, FFDItems.STRIPPED_LOG_ITEMS);
        Collections.addAll(order, FFDItems.STRIPPED_CRIMSON_STEM, FFDItems.STRIPPED_WARPED_STEM,
                FFDItems.CRIMSON_HYPHAE, FFDItems.WARPED_HYPHAE);
        Collections.addAll(order, FFDItems.STRIPPED_WOOD_ITEMS);
        Collections.addAll(order, FFDItems.STRIPPED_CRIMSON_HYPHAE, FFDItems.STRIPPED_WARPED_HYPHAE,
                FFDItems.CRIMSON_PLANKS, FFDItems.WARPED_PLANKS,
                FFDItems.CRIMSON_STAIRS, FFDItems.WARPED_STAIRS,
                FFDItems.CRIMSON_SLAB, FFDItems.WARPED_SLAB,
                FFDItems.CRIMSON_FENCE, FFDItems.WARPED_FENCE,
                FFDItems.CRIMSON_FENCE_GATE, FFDItems.WARPED_FENCE_GATE,
                FFDItems.CRIMSON_DOOR, FFDItems.WARPED_DOOR);
        Collections.addAll(order, FFDItems.OVERWORLD_TRAPDOOR_ITEMS);
        Collections.addAll(order, FFDItems.CRIMSON_TRAPDOOR, FFDItems.WARPED_TRAPDOOR);
        Collections.addAll(order, FFDItems.OVERWORLD_BUTTON_ITEMS);
        Collections.addAll(order, FFDItems.CRIMSON_BUTTON, FFDItems.WARPED_BUTTON);
        Collections.addAll(order, FFDItems.OVERWORLD_PRESSURE_PLATE_ITEMS);
        Collections.addAll(order, FFDItems.CRIMSON_PRESSURE_PLATE, FFDItems.WARPED_PRESSURE_PLATE);
        regroupItems(items, order);
    }

    private static void regroupItems(NonNullList<ItemStack> items, List<Item> order) {
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
