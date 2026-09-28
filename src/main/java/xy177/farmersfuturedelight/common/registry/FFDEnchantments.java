package xy177.farmersfuturedelight.common.registry;

import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.registries.IForgeRegistry;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;

public final class FFDEnchantments {
    private static final EnumEnchantmentType TRIDENT_TYPE = EnumHelper.addEnchantmentType(
            "ffd_trident", FFDEnchantments::isTridentItem);

    public static final Enchantment LOYALTY = new LoyaltyEnchantment();
    public static final Enchantment IMPALING = new ImpalingEnchantment();
    public static final Enchantment RIPTIDE = new RiptideEnchantment();
    public static final Enchantment CHANNELING = new ChannelingEnchantment();

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
    }

    private static boolean isLocalEnabled(Enchantment enchantment, String... aliases) {
        return FFDCompat.isLocalEnchantmentEnabled(FFDConfig.tridentMode,
                FFDCompat.Feature.TRIDENT, enchantment, aliases);
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
        return isAquatic(target) ? getImpaling(stack) * 2.5F : 0.0F;
    }

    public static boolean isAquatic(EntityLivingBase entity) {
        if (entity instanceof EntityWaterMob || entity instanceof EntityGuardian
                || entity instanceof EntityDolphin || entity instanceof EntityTurtle
                || entity instanceof EntityAxolotl) {
            return true;
        }
        ResourceLocation id = net.minecraft.entity.EntityList.getKey(entity);
        if (id == null) {
            return false;
        }
        String path = id.getResourcePath();
        return "guardian".equals(path) || "elder_guardian".equals(path)
                || "squid".equals(path) || "glow_squid".equals(path)
                || "cod".equals(path) || "salmon".equals(path)
                || "pufferfish".equals(path) || "puffer_fish".equals(path)
                || "tropical_fish".equals(path) || "dolphin".equals(path)
                || "turtle".equals(path) || "axolotl".equals(path);
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
}
