package xy177.farmersfuturedelight.common.registry;

import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.registries.IForgeRegistry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDEnchantments {
    private static final EnumEnchantmentType TRIDENT_TYPE = EnumHelper.addEnchantmentType(
            "ffd_trident", FFDEnchantments::isTridentItem);
    private static final EnumEnchantmentType CROSSBOW_TYPE = EnumHelper.addEnchantmentType(
            "ffd_crossbow", FFDEnchantments::isCrossbowItem);

    public static final Enchantment LOYALTY = new LoyaltyEnchantment();
    public static final Enchantment IMPALING = new ImpalingEnchantment();
    public static final Enchantment RIPTIDE = new RiptideEnchantment();
    public static final Enchantment CHANNELING = new ChannelingEnchantment();
    public static final Enchantment MULTISHOT = new CrossbowEnchantment(
            Enchantment.Rarity.RARE, "multishot", 1);
    public static final Enchantment PIERCING = new CrossbowEnchantment(
            Enchantment.Rarity.UNCOMMON, "piercing", 4);
    public static final Enchantment QUICK_CHARGE = new CrossbowEnchantment(
            Enchantment.Rarity.UNCOMMON, "quick_charge", 3);

    private FFDEnchantments() {
    }

    public static void register(IForgeRegistry<Enchantment> registry) {
        if (isLocalEnabled(LOYALTY)) {
            registry.register(LOYALTY);
        }
        if (isLocalEnabled(IMPALING, "is_watermob")) {
            registry.register(IMPALING);
        }
        if (isLocalEnabled(RIPTIDE)) {
            registry.register(RIPTIDE);
        }
        if (isLocalEnabled(CHANNELING)) {
            registry.register(CHANNELING);
        }
        if (isLocalCrossbowEnabled(MULTISHOT)) {
            registry.register(MULTISHOT);
        }
        if (isLocalCrossbowEnabled(PIERCING)) {
            registry.register(PIERCING);
        }
        if (isLocalCrossbowEnabled(QUICK_CHARGE)) {
            registry.register(QUICK_CHARGE);
        }
    }

    private static boolean isLocalEnabled(Enchantment enchantment, String... aliases) {
        return FFDCompat.isLocalEnchantmentEnabled(FFDConfig.tridentMode,
                FFDCompat.Feature.TRIDENT, enchantment, aliases);
    }

    private static boolean isLocalCrossbowEnabled(Enchantment enchantment) {
        return FFDCompat.isLocalEnchantmentEnabled(FFDConfig.crossbowMode,
                FFDCompat.Feature.CROSSBOW, enchantment,
                enchantment.getRegistryName().getResourcePath());
    }

    public static int getQuickCharge(ItemStack stack) {
        return getCrossbowLevel(stack, QUICK_CHARGE, "quick_charge");
    }

    public static int getMultishot(ItemStack stack) {
        return getCrossbowLevel(stack, MULTISHOT, "multishot");
    }

    public static int getPiercing(ItemStack stack) {
        return getCrossbowLevel(stack, PIERCING, "piercing");
    }

    private static int getCrossbowLevel(ItemStack stack, Enchantment local, String path) {
        if (stack.isEmpty()) {
            return 0;
        }
        int level = 0;
        for (Map.Entry<Enchantment, Integer> entry
                : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            ResourceLocation id = enchantment == null ? null : enchantment.getRegistryName();
            if (enchantment == local || id != null && path.equals(id.getResourcePath())
                    && (FarmerFutureDelight.MODID.equals(id.getResourceDomain())
                    || "futuremc".equals(id.getResourceDomain()))) {
                level = Math.max(level, entry.getValue());
            }
        }
        return level;
    }

    private static boolean isCrossbowItem(Item item) {
        return FFDItems.isCrossbowItem(item);
    }

    public static int getLoyalty(ItemStack stack) {
        return getLevel(stack, LOYALTY, "loyalty");
    }

    public static int getImpaling(ItemStack stack) {
        return Math.max(getLevel(stack, IMPALING, "impaling"),
                getLevel(stack, IMPALING, "is_watermob"));
    }

    public static int getRiptide(ItemStack stack) {
        return getLevel(stack, RIPTIDE, "riptide");
    }

    public static boolean hasChanneling(ItemStack stack) {
        return getLevel(stack, CHANNELING, "channeling") > 0;
    }

    public static float getImpalingDamage(ItemStack stack, EntityLivingBase target) {
        return isImpalingDamageTarget(target) ? getImpaling(stack) * 2.5F : 0.0F;
    }

    public static boolean isImpalingDamageTarget(EntityLivingBase entity) {
        return FFDConfig.isConfiguredEntity(FFDConfig.impalingDamageMobs, entity);
    }

    public static boolean isAquatic(EntityLivingBase entity) {
        return isImpalingDamageTarget(entity);
    }

    public static boolean isTridentEnchantment(Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }
        ResourceLocation id = enchantment.getRegistryName();
        if (id == null) {
            return false;
        }
        String path = id.getResourcePath();
        return "loyalty".equals(path) || "impaling".equals(path)
                || "is_watermob".equals(path) || "riptide".equals(path)
                || "channeling".equals(path);
    }

    private static int getLevel(ItemStack stack, Enchantment local, String path) {
        if (stack.isEmpty()) {
            return 0;
        }
        int result = 0;
        for (Map.Entry<Enchantment, Integer> entry
                : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment == null) {
                continue;
            }
            ResourceLocation id = enchantment.getRegistryName();
            if (enchantment == local || id != null && path.equals(id.getResourcePath())
                    && isTridentProvider(id.getResourceDomain())) {
                result = Math.max(result, entry.getValue());
            }
        }
        return result;
    }

    private static boolean isTridentProvider(String namespace) {
        return FarmerFutureDelight.MODID.equals(namespace) || "futuremc".equals(namespace)
                || "oe".equals(namespace);
    }

    private static boolean isTridentItem(Item item) {
        return FFDItems.isTridentItem(item);
    }

    private static boolean hasPath(Enchantment enchantment, String... paths) {
        ResourceLocation id = enchantment == null ? null : enchantment.getRegistryName();
        if (id == null || !isTridentProvider(id.getResourceDomain())) {
            return false;
        }
        for (String path : paths) {
            if (path.equals(id.getResourcePath())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDamageExclusive(Enchantment enchantment) {
        if (enchantment instanceof EnchantmentDamage
                || hasPath(enchantment, "impaling", "is_watermob", "density", "breach")) {
            return true;
        }
        ResourceLocation id = enchantment == null ? null : enchantment.getRegistryName();
        if (id == null || !"minecraft".equals(id.getResourceDomain())) {
            return false;
        }
        String path = id.getResourcePath();
        return "sharpness".equals(path) || "smite".equals(path)
                || "bane_of_arthropods".equals(path)
                || "density".equals(path) || "breach".equals(path);
    }

    private abstract static class TridentEnchantment extends Enchantment {
        private TridentEnchantment(Rarity rarity, String name) {
            super(rarity, TRIDENT_TYPE, new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND});
            setRegistryName(FarmerFutureDelight.MODID, name);
            setName(FarmerFutureDelight.MODID + "." + name);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return FFDItems.isTridentStack(stack);
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return FFDItems.isTridentStack(stack);
        }
    }

    private static final class LoyaltyEnchantment extends TridentEnchantment {
        private LoyaltyEnchantment() {
            super(Rarity.UNCOMMON, "loyalty");
        }

        @Override
        public int getMinEnchantability(int level) {
            return 5 + level * 7;
        }

        @Override
        public int getMaxEnchantability(int level) {
            return 50;
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }

        @Override
        protected boolean canApplyTogether(Enchantment enchantment) {
            return super.canApplyTogether(enchantment)
                    && !hasPath(enchantment, "loyalty", "riptide");
        }
    }

    private static final class ImpalingEnchantment extends TridentEnchantment {
        private ImpalingEnchantment() {
            super(Rarity.RARE, "impaling");
        }

        @Override
        public int getMinEnchantability(int level) {
            return 1 + (level - 1) * 8;
        }

        @Override
        public int getMaxEnchantability(int level) {
            return getMinEnchantability(level) + 20;
        }

        @Override
        public int getMaxLevel() {
            return 5;
        }

        @Override
        protected boolean canApplyTogether(Enchantment enchantment) {
            return super.canApplyTogether(enchantment) && !isDamageExclusive(enchantment);
        }
    }

    private static final class RiptideEnchantment extends TridentEnchantment {
        private RiptideEnchantment() {
            super(Rarity.RARE, "riptide");
        }

        @Override
        public int getMinEnchantability(int level) {
            return 10 + level * 7;
        }

        @Override
        public int getMaxEnchantability(int level) {
            return 50;
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }

        @Override
        protected boolean canApplyTogether(Enchantment enchantment) {
            return super.canApplyTogether(enchantment)
                    && !hasPath(enchantment, "loyalty", "riptide", "channeling");
        }
    }

    private static final class ChannelingEnchantment extends TridentEnchantment {
        private ChannelingEnchantment() {
            super(Rarity.VERY_RARE, "channeling");
        }

        @Override
        public int getMinEnchantability(int level) {
            return 25;
        }

        @Override
        public int getMaxEnchantability(int level) {
            return 50;
        }

        @Override
        protected boolean canApplyTogether(Enchantment enchantment) {
            return super.canApplyTogether(enchantment)
                    && !hasPath(enchantment, "riptide", "channeling");
        }
    }

    private static final class CrossbowEnchantment extends Enchantment {
        private final int maxLevel;
        private final String path;

        private CrossbowEnchantment(Rarity rarity, String path, int maxLevel) {
            super(rarity, CROSSBOW_TYPE,
                    new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND});
            this.path = path;
            this.maxLevel = maxLevel;
            setRegistryName(FarmerFutureDelight.MODID, path);
            setName(FarmerFutureDelight.MODID + "." + path);
        }

        @Override
        public int getMinEnchantability(int level) {
            if ("multishot".equals(path)) {
                return 20;
            }
            if ("quick_charge".equals(path)) {
                return 12 + (level - 1) * 20;
            }
            return 1 + (level - 1) * 10;
        }

        @Override
        public int getMaxEnchantability(int level) {
            return getMinEnchantability(level) + ("multishot".equals(path) ? 30 : 50);
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return FFDItems.isCrossbowItem(stack.getItem());
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return canApply(stack);
        }

        @Override
        protected boolean canApplyTogether(Enchantment enchantment) {
            return super.canApplyTogether(enchantment)
                    && !("multishot".equals(path)
                    && (enchantment == PIERCING || hasPath(enchantment, "piercing")))
                    && !("piercing".equals(path)
                    && (enchantment == MULTISHOT || hasPath(enchantment, "multishot")));
        }
    }
}
