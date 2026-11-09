import mods.farmersfuturedelight.Smoker;
import mods.farmersfuturedelight.BlastFurnace;
import mods.farmersfuturedelight.Campfire;
import mods.farmersfuturedelight.Stonecutter;
import mods.farmersfuturedelight.SmithingTable;
import mods.farmersfuturedelight.Grindstone;
import mods.farmersfuturedelight.Composter;

Smoker.addRecipe(<minecraft:beef>, <minecraft:cooked_beef>, 100, 0.35);
Smoker.removeRecipe(<minecraft:beef>);

BlastFurnace.addRecipe(<minecraft:iron_ore>, <minecraft:iron_ingot>, 100, 0.7);
BlastFurnace.removeRecipe(<minecraft:iron_ore>);

Campfire.addRecipe(<minecraft:potato>, <minecraft:baked_potato>, 600);
Campfire.removeRecipe(<minecraft:potato>);

Stonecutter.addOutput(<minecraft:stone>, <minecraft:stone_slab> * 2);
Stonecutter.addOutputs(<minecraft:stone>, <minecraft:stone_stairs>, <minecraft:stone_slab> * 2);
Stonecutter.removeOutputs(<minecraft:stone>, <minecraft:stone_stairs>);
Stonecutter.removeAllOutputsForInput(<minecraft:stone>);

SmithingTable.addRecipe(<minecraft:iron_sword>, <minecraft:diamond>, <minecraft:diamond_sword>);
SmithingTable.removeRecipe(<minecraft:iron_sword>, <minecraft:diamond>);
SmithingTable.addRecipe(<minecraft:paper>, <minecraft:iron_axe>, null, <minecraft:diamond_axe>);
SmithingTable.removeRecipe(<minecraft:paper>, <minecraft:iron_axe>, null);
SmithingTable.addRecipe(null, <minecraft:iron_shovel>, null, <minecraft:diamond_shovel>);
SmithingTable.removeRecipe(null, <minecraft:iron_shovel>, null);

Grindstone.addRecipe(<minecraft:flint>, <minecraft:gravel>, 1.0);
Grindstone.removeRecipe(<minecraft:flint>);
Grindstone.addRecipe(<minecraft:gravel>, <minecraft:sand>, <minecraft:flint>, 2.0);
Grindstone.removeRecipe(<minecraft:gravel>, <minecraft:sand>);

Composter.addRecipe(<minecraft:rotten_flesh>, 0.65, false);
Composter.removeRecipe(<minecraft:rotten_flesh>);
