package xy177.farmersfuturedelight.common.registry;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.zenscript.IBracketHandler;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionInt;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.natives.IJavaMethod;

@ZenRegister
@BracketHandler(priority = 50)
public final class FFDFutureItemBracket implements IBracketHandler {
    private static final Set<String> ITEMS = new HashSet<>(Arrays.asList(
            "sweet_berries", "honey_bottle", "honeycomb", "honey_block", "honeycomb_block",
            "bee_nest", "beehive", "bamboo", "scaffolding", "suspicious_stew", "crossbow", "trident",
            "nautilus_shell", "cod_bucket", "salmon_bucket", "pufferfish_bucket", "tropical_fish_bucket",
            "smoker", "blast_furnace", "campfire", "stonecutter", "smithing_table", "composter",
            "grindstone", "loom", "cartography_table", "fletching_table", "lectern", "barrel", "bell",
            "lantern", "blue_ice", "cornflower", "lily_of_the_valley", "wither_rose",
            "netherite_ingot", "netherite_scrap", "ancient_debris",
            "netherite_sword", "netherite_shovel", "netherite_pickaxe", "netherite_axe", "netherite_hoe",
            "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots"));

    private final IJavaMethod method = CraftTweakerAPI.getJavaMethod(
            FFDFutureItemBracket.class, "getItem", String.class, int.class);

    public static IItemStack getItem(String path, int metadata) {
        ItemStack stack = FFDModernItemLookup.resolve("minecraft:" + path);
        if (stack.isEmpty()) return null;
        stack.setItemDamage(metadata);
        return CraftTweakerMC.getIItemStackWildcardSize(stack);
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if (Loader.isModLoaded("futuremc")) return null;
        StringBuilder joined = new StringBuilder();
        for (Token token : tokens) joined.append(token.getValue());
        String value = joined.toString();
        if (value.startsWith("item:")) value = value.substring(5);
        if (!value.startsWith("futuremc:")) return null;
        String[] parts = value.substring(9).split(":", -1);
        if (parts.length < 1 || parts.length > 2 || !ITEMS.contains(parts[0])) return null;
        int metadata = 0;
        if (parts.length == 2) {
            if ("*".equals(parts[1])) metadata = Short.MAX_VALUE;
            else {
                try {
                    metadata = Integer.parseInt(parts[1]);
                } catch (NumberFormatException exception) {
                    return null;
                }
                if (metadata < 0 || metadata > Short.MAX_VALUE) return null;
            }
        }
        if (getItem(parts[0], metadata) == null) return null;
        String path = parts[0];
        int damage = metadata;
        return position -> new ExpressionCallStatic(position, environment, method,
                new Expression[] {new ExpressionString(position, path),
                        new ExpressionInt(position, damage, ZenType.INT)});
    }

    @Override
    public Class<?> getReturnedClass() {
        return IItemStack.class;
    }
}
