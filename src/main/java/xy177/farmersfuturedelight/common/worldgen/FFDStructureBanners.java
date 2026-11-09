package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public final class FFDStructureBanners {
    private static final String COMPONENTS = "farmers_future_delight:structure_components";
    private static final String NAME = "block.farmers_future_delight.ominous_banner";

    private FFDStructureBanners() {
    }

    private static NBTTagCompound components(ItemStack stack) {
        NBTTagCompound block = stack.getSubCompound("BlockEntityTag");
        return block == null ? new NBTTagCompound() : block.getCompoundTag("ForgeData").getCompoundTag(COMPONENTS);
    }

    private static boolean ominous(NBTTagCompound components) {
        return "block.minecraft.ominous_banner".equals(
                components.getCompoundTag("minecraft:item_name").getString("translate"));
    }

    public static String itemName(String original, ItemStack stack) {
        return !stack.hasDisplayName() && ominous(components(stack))
                ? new TextComponentTranslation(NAME).getUnformattedText() : original;
    }

    public static EnumRarity rarity(EnumRarity original, ItemStack stack) {
        String rarity = components(stack).getString("minecraft:rarity");
        if ("uncommon".equals(rarity)) return EnumRarity.UNCOMMON;
        if ("rare".equals(rarity)) return EnumRarity.RARE;
        if ("epic".equals(rarity)) return EnumRarity.EPIC;
        if ("common".equals(rarity)) return EnumRarity.COMMON;
        return original;
    }

    public static ITextComponent blockName(ITextComponent original, TileEntityBanner banner) {
        return !banner.hasCustomName() && ominous(banner.getTileData().getCompoundTag(COMPONENTS))
                ? new TextComponentTranslation(NAME) : original;
    }

    public static ItemStack dropped(ItemStack stack, TileEntityBanner banner) {
        NBTTagCompound components = banner.getTileData().getCompoundTag(COMPONENTS);
        if (!components.hasNoTags()) {
            NBTTagCompound block = stack.getOrCreateSubCompound("BlockEntityTag");
            NBTTagCompound custom = block.getCompoundTag("ForgeData");
            custom.setTag(COMPONENTS, components.copy());
            block.setTag("ForgeData", custom);
        }
        return stack;
    }

    public static void placed(TileEntityBanner banner, ItemStack stack) {
        NBTTagCompound components = components(stack);
        if (components.hasNoTags()) banner.getTileData().removeTag(COMPONENTS);
        else banner.getTileData().setTag(COMPONENTS, components.copy());
        banner.markDirty();
    }
}
