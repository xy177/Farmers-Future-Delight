import mods.farmersfuturedelight.Workstations;

Workstations.addRecipe("example:smoking", "SMOKING", [<minecraft:beef>], <minecraft:cooked_beef>, 100, 0.35);
Workstations.removeRecipe("SMOKING", "example:smoking");

Workstations.addRecipe("example:blasting", "BLASTING", [<minecraft:iron_ore>], <minecraft:iron_ingot>, 100, 0.7);
Workstations.removeRecipe("BLASTING", "example:blasting");

Workstations.addRecipe("example:campfire_cooking", "CAMPFIRE_COOKING", [<minecraft:potato>], <minecraft:baked_potato>, 600, 0.0);
Workstations.removeRecipe("CAMPFIRE_COOKING", "example:campfire_cooking");

Workstations.addRecipe("example:stonecutting", "STONECUTTING", [<minecraft:stone>], <minecraft:stone_slab> * 2, 1, 0.0);
Workstations.removeRecipe("STONECUTTING", "example:stonecutting");

Workstations.addSmithingTransform("example:smithing", null, <minecraft:iron_sword>, <minecraft:diamond>, <minecraft:diamond_sword>);
Workstations.removeRecipe("SMITHING", "example:smithing");

Workstations.addRecipe("example:grinding_single", "GRINDING_SINGLE", [<minecraft:flint>], <minecraft:gravel>, 1, 1.0);
Workstations.removeRecipe("GRINDING_SINGLE", "example:grinding_single");
Workstations.addRecipe("example:grinding", "GRINDING", [<minecraft:gravel>, <minecraft:sand>], <minecraft:flint>, 1, 2.0);
Workstations.removeRecipe("GRINDING", "example:grinding");

Workstations.addCompostingRule("example:compost", <minecraft:rotten_flesh>, 0.65, false);
Workstations.removeCompostingRule("example:compost");
