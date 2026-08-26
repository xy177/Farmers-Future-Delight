package xy177.farmersfuturedelight.common.registry;

public final class FFDRawOres {
    public static final String[] NAMES = {
            "gold", "iron", "copper", "tin", "zinc", "lead", "silver", "cobalt",
            "osmium", "nickel", "iridium", "uranium", "gallium", "titanium", "platinum",
            "tungsten", "aluminium", "magnesium", "lithium", "thorium", "boron", "vanadium",
            "cadmium", "manganese", "germanium", "chromium", "arsenic", "beryllium", "irradium",
            "palladium", "plutonium", "niobium", "mithril", "rutile", "ardite", "cerulean",
            "moonstone", "octine", "syrmorite", "cinnabar", "vulcanite", "chasmium", "rosegold"
    };

    private static final String[][] PRODUCTS = {
            {"dustGold", "itemCinnabar", "dustCopper", "dustSilver"},
            {"dustIron", "dustNickel", "dustCobalt", "dustLithium"},
            {"dustCopper", "dustGold", "dustMolybdenum"},
            {"dustTin", "dustIron", "dustLithium"},
            {"dustZinc", "dustCadmium", "dustGallium"},
            {"dustLead", "dustSilver", "dustBismuth"},
            {"dustSilver", "dustLead", "dustArsenic"},
            {"dustCobalt", "dustNickel", "dustCopper", "dustIron"},
            {"dustOsmium", "dustIridium", "dustOsmium", "dustPlatinum"},
            {"dustNickel", "dustPlatinum", "dustCobalt"},
            {"dustIridium", "dustPlatinum", "dustOsmium", "dustRuthenium"},
            {"dustUranium", "dustThorium", "dustRareEarth"},
            {"dustGallium", "dustAluminium", "dustZinc"},
            {"dustTitanium", "dustVanadium", "dustIron"},
            {"dustPlatinum", "dustIridium", "dustPalladium"},
            {"dustTungsten", "dustTin", "dustMolybdenum"},
            {"dustAluminium", "dustIron", "dustTitanium", "dustLithium"},
            {"dustMagnesium", "dustCalcium", "dustLithium"},
            {"dustLithium", "dustAluminium", "dustBoron"},
            {"dustThorium", "dustRareEarth", "dustUranium"},
            {"dustBoron", "dustSodium", "dustCalcium"},
            {"dustVanadium", "dustTitanium", "dustIron"},
            {"dustCadmium", "dustZinc", "dustCopper"},
            {"dustManganese", "dustIron", "dustCobalt"},
            {"dustGermanium", "dustLead", "dustZinc"},
            {"dustChromium", "dustIron", "dustNickel"},
            {"dustArsenic", "dustSulfur", "dustAntimony"},
            {"dustBeryllium", "dustAluminium", "dustLithium"},
            {"dustIrradium", "dustIrradium"},
            {"dustPalladium", "dustNickel", "dustPlatinum"},
            {"dustPlutonium", "dustPlutonium", "dustUranium"},
            {"dustNiobium", "dustNiobium", "dustNiobium"},
            {"dustMithril", "dustSilver", "dustPlatinum"},
            {"dustRutile", "dustIron", "dustVanadium"},
            {"dustCinnabar", "dustArsenic", "itemCinnabar", "dustGold"},
            {"dustCerulean"}, {"dustMoonstone"}, {"dustOctine"}, {"dustSyrmorite"},
            {"quicksilver", "itemCinnabar"},
            {"dustVulcanite"}, {"dustChasmium"}, {"dustRosegold"}
    };

    private static final float[][] PRODUCT_CHANCES = {
            {2.0F, 0.2F, 0.2F, 0.15F}, {2.0F, 0.1F, 0.05F, 0.03F},
            {2.0F, 0.12F, 0.08F}, {2.0F, 0.1F, 0.05F},
            {2.0F, 0.15F, 0.1F}, {2.0F, 0.1F, 0.07F},
            {2.0F, 0.1F, 0.05F}, {2.0F, 0.3F, 0.2F, 0.15F},
            {2.0F, 0.25F, 0.1F, 0.1F}, {2.0F, 0.2F, 0.2F},
            {2.0F, 0.2F, 0.2F, 0.15F}, {2.0F, 0.15F, 0.1F},
            {2.0F, 0.3F, 0.1F}, {2.0F, 0.15F, 0.1F},
            {2.0F, 0.2F, 0.2F}, {2.0F, 0.1F, 0.1F},
            {2.0F, 0.1F, 0.1F, 0.03F}, {2.0F, 0.2F, 0.05F},
            {2.0F, 0.1F, 0.05F}, {2.0F, 0.25F, 0.15F},
            {2.0F, 0.2F, 0.2F}, {2.0F, 0.3F, 0.25F},
            {2.0F, 0.4F, 0.1F}, {2.0F, 0.3F, 0.15F},
            {2.0F, 0.2F, 0.2F}, {2.0F, 0.3F, 0.1F},
            {2.0F, 0.4F, 0.15F}, {2.0F, 0.2F, 0.1F},
            {1.0F, 0.03F}, {2.0F, 0.3F, 0.1F},
            {1.0F, 0.03F, 0.01F}, {2.0F, 0.03F, 0.01F},
            {2.0F, 0.15F, 0.05F}, {2.0F, 0.25F, 0.25F},
            {2.0F, 0.25F, 0.1F, 0.05F},
            {2.0F}, {2.0F}, {2.0F}, {2.0F}, {1.0F, 0.25F},
            {2.0F}, {2.0F}, {2.0F}
    };

    private FFDRawOres() {
    }

    public static boolean isCopper(String name) {
        return "copper".equals(name);
    }

    public static String rawItemName(String name) {
        return "raw_" + name;
    }

    public static String rawBlockName(String name) {
        return "raw_" + name + "_block";
    }

    public static String externalRawBlockName(String name) {
        return "raw_block_" + name;
    }

    public static String rawOreName(String name) {
        return "raw" + capitalize(name);
    }

    public static String rawBlockOreName(String name) {
        return "blockRaw" + capitalize(name);
    }

    public static String ingotOreName(String name) {
        return "ingot" + capitalize(name);
    }

    public static String[] productOreNames(int index) {
        return PRODUCTS[index];
    }

    public static float[] productChances(int index) {
        return PRODUCT_CHANCES[index];
    }

    public static String[] refinedOreNames(String name) {
        if ("aluminium".equals(name)) {
            return new String[] {"ingotAluminium", "ingotAluminum"};
        }
        if ("chromium".equals(name)) {
            return new String[] {"ingotChromium", "ingotChrome"};
        }
        if ("rosegold".equals(name)) {
            return new String[] {"ingotRosegold", "ingotRoseGold"};
        }
        if ("cinnabar".equals(name)) {
            return new String[] {"ingotCinnabar", "quicksilver", "itemCinnabar"};
        }
        return new String[] {ingotOreName(name)};
    }

    public static String[] fluidNames(String name) {
        if ("aluminium".equals(name)) {
            return new String[] {"aluminium", "aluminum", "molten_aluminium", "molten_aluminum"};
        }
        if ("gallium".equals(name)) {
            return new String[] {"liquidgallium", "gallium", "molten_gallium"};
        }
        if ("irradium".equals(name)) {
            return new String[] {"liquidirradium", "irradium", "molten_irradium"};
        }
        if ("palladium".equals(name)) {
            return new String[] {"palladium_fluid", "palladium", "molten_palladium"};
        }
        if ("cerulean".equals(name)) {
            return new String[] {"tamoltencerulean", "cerulean", "molten_cerulean"};
        }
        if ("moonstone".equals(name)) {
            return new String[] {"tamoltenmoonstone", "moonstone", "molten_moonstone"};
        }
        if ("octine".equals(name)) {
            return new String[] {"liquidoctine", "octine", "molten_octine"};
        }
        if ("syrmorite".equals(name)) {
            return new String[] {"liquidsyrmorite", "syrmorite", "molten_syrmorite"};
        }
        if ("rosegold".equals(name)) {
            return new String[] {"rosegold", "rose_gold", "molten_rosegold", "molten_rose_gold"};
        }
        return new String[] {name, "molten_" + name, "molten" + name};
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
