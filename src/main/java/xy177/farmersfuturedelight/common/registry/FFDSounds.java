package xy177.farmersfuturedelight.common.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDSounds {
    private static final List<SoundEvent> ALL = new ArrayList<>();

    public static final SoundEvent GLOW_INK_SAC_USE = sound("item.glow_ink_sac.use");
    public static final SoundEvent INK_SAC_USE = sound("item.ink_sac.use");
    public static final SoundEvent DYE_USE = sound("item.dye.use");
    public static final SoundEvent GOLDEN_DANDELION_USE = sound("item.golden_dandelion.use");
    public static final SoundEvent GOLDEN_DANDELION_UNUSE = sound("item.golden_dandelion.unuse");
    public static final SoundEvent IRON_GOLEM_REPAIR = sound("entity.iron_golem.repair");
    public static final SoundEvent IRON_GOLEM_DAMAGE = sound("entity.iron_golem.damage");
    public static final SoundEvent CROP_PLANT = sound("item.crop.plant");
    public static final SoundEvent VILLAGER_WORK_ARMORER = sound("entity.villager.work_armorer");
    public static final SoundEvent VILLAGER_WORK_BUTCHER = sound("entity.villager.work_butcher");
    public static final SoundEvent VILLAGER_WORK_CARTOGRAPHER = sound("entity.villager.work_cartographer");
    public static final SoundEvent VILLAGER_WORK_CLERIC = sound("entity.villager.work_cleric");
    public static final SoundEvent VILLAGER_WORK_FARMER = sound("entity.villager.work_farmer");
    public static final SoundEvent VILLAGER_WORK_FISHERMAN = sound("entity.villager.work_fisherman");
    public static final SoundEvent VILLAGER_WORK_FLETCHER = sound("entity.villager.work_fletcher");
    public static final SoundEvent VILLAGER_WORK_LEATHERWORKER = sound("entity.villager.work_leatherworker");
    public static final SoundEvent VILLAGER_WORK_LIBRARIAN = sound("entity.villager.work_librarian");
    public static final SoundEvent VILLAGER_WORK_MASON = sound("entity.villager.work_mason");
    public static final SoundEvent VILLAGER_WORK_SHEPHERD = sound("entity.villager.work_shepherd");
    public static final SoundEvent VILLAGER_WORK_TOOLSMITH = sound("entity.villager.work_toolsmith");
    public static final SoundEvent VILLAGER_WORK_WEAPONSMITH = sound("entity.villager.work_weaponsmith");
    public static final SoundEvent WANDERING_TRADER_AMBIENT = sound("entity.wandering_trader.ambient");
    public static final SoundEvent WANDERING_TRADER_TRADE = sound("entity.wandering_trader.trade");
    public static final SoundEvent WANDERING_TRADER_HURT = sound("entity.wandering_trader.hurt");
    public static final SoundEvent WANDERING_TRADER_DEATH = sound("entity.wandering_trader.death");
    public static final SoundEvent WANDERING_TRADER_YES = sound("entity.wandering_trader.yes");
    public static final SoundEvent WANDERING_TRADER_NO = sound("entity.wandering_trader.no");
    public static final SoundEvent WANDERING_TRADER_DRINK_MILK = sound("entity.wandering_trader.drink_milk");
    public static final SoundEvent WANDERING_TRADER_DRINK_POTION = sound("entity.wandering_trader.drink_potion");
    public static final SoundEvent WANDERING_TRADER_DISAPPEARED = sound("entity.wandering_trader.disappeared");
    public static final SoundEvent WANDERING_TRADER_REAPPEARED = sound("entity.wandering_trader.reappeared");
    public static final SoundEvent BARREL_OPEN = sound("block.barrel.open");
    public static final SoundEvent SMOKER_SMOKE = sound("block.smoker.smoke");
    public static final SoundEvent BLAST_FURNACE_FIRE_CRACKLE = sound("block.blastfurnace.fire_crackle");
    public static final SoundEvent CAMPFIRE_CRACKLE = sound("block.campfire.crackle");
    public static final SoundEvent BARREL_CLOSE = sound("block.barrel.close");
    public static final SoundEvent COMPOSTER_FILL = sound("block.composter.fill");
    public static final SoundEvent COMPOSTER_FILL_SUCCESS = sound("block.composter.fill_success");
    public static final SoundEvent COMPOSTER_EMPTY = sound("block.composter.empty");
    public static final SoundEvent COMPOSTER_READY = sound("block.composter.ready");
    public static final SoundEvent STONECUTTER_TAKE_RESULT = sound("ui.stonecutter.take_result");
    public static final SoundEvent GRINDSTONE_USE = sound("block.grindstone.use");
    public static final SoundEvent CARTOGRAPHY_TABLE_TAKE_RESULT =
            sound("ui.cartography_table.take_result");
    public static final SoundEvent BELL_USE = sound("block.bell.use");
    public static final SoundEvent LOOM_TAKE_RESULT = sound("ui.loom.take_result");
    public static final SoundEvent BOOK_PAGE_TURN = sound("item.book.page_turn");
    public static final SoundEvent BOOK_PUT = sound("item.book.put");
    public static final SoundEvent WET_SPONGE_DRIES = sound("block.wet_sponge.dries");
    public static final SoundEvent CROSSBOW_LOADING_START = sound("item.crossbow.loading_start");
    public static final SoundEvent CROSSBOW_LOADING_MIDDLE = sound("item.crossbow.loading_middle");
    public static final SoundEvent CROSSBOW_LOADING_END = sound("item.crossbow.loading_end");
    public static final SoundEvent CROSSBOW_SHOOT = sound("item.crossbow.shoot");
    public static final SoundEvent CROSSBOW_HIT = sound("item.crossbow.hit");
    public static final SoundEvent CROSSBOW_QUICK_CHARGE_1 = sound("item.crossbow.quick_charge_1");
    public static final SoundEvent CROSSBOW_QUICK_CHARGE_2 = sound("item.crossbow.quick_charge_2");
    public static final SoundEvent CROSSBOW_QUICK_CHARGE_3 = sound("item.crossbow.quick_charge_3");
    public static final SoundEvent PILLAGER_AMBIENT = sound("entity.pillager.ambient");
    public static final SoundEvent PILLAGER_DEATH = sound("entity.pillager.death");
    public static final SoundEvent PILLAGER_HURT = sound("entity.pillager.hurt");
    public static final SoundEvent RAVAGER_AMBIENT = sound("entity.ravager.ambient");
    public static final SoundEvent RAVAGER_ATTACK = sound("entity.ravager.attack");
    public static final SoundEvent RAVAGER_CELEBRATE = sound("entity.ravager.celebrate");
    public static final SoundEvent RAVAGER_DEATH = sound("entity.ravager.death");
    public static final SoundEvent RAVAGER_HURT = sound("entity.ravager.hurt");
    public static final SoundEvent RAVAGER_ROAR = sound("entity.ravager.roar");
    public static final SoundEvent RAVAGER_STEP = sound("entity.ravager.step");
    public static final SoundEvent RAVAGER_STUNNED = sound("entity.ravager.stunned");
    public static final SoundEvent NOTE_IRON_XYLOPHONE = sound("block.note_block.iron_xylophone");
    public static final SoundEvent NOTE_COW_BELL = sound("block.note_block.cow_bell");
    public static final SoundEvent NOTE_DIDGERIDOO = sound("block.note_block.didgeridoo");
    public static final SoundEvent NOTE_BIT = sound("block.note_block.bit");
    public static final SoundEvent NOTE_BANJO = sound("block.note_block.banjo");
    public static final SoundEvent NOTE_PLING = sound("block.note_block.pling");
    public static final SoundEvent GLOW_SQUID_AMBIENT = sound("entity.glow_squid.ambient");
    public static final SoundEvent GLOW_SQUID_HURT = sound("entity.glow_squid.hurt");
    public static final SoundEvent GLOW_SQUID_DEATH = sound("entity.glow_squid.death");
    public static final SoundEvent GLOW_SQUID_SQUIRT = sound("entity.glow_squid.squirt");
    public static final SoundEvent SQUID_SQUIRT = sound("entity.squid.squirt");
    public static final SoundEvent PHANTOM_AMBIENT = sound("entity.phantom.ambient");
    public static final SoundEvent PHANTOM_BITE = sound("entity.phantom.bite");
    public static final SoundEvent PHANTOM_DEATH = sound("entity.phantom.death");
    public static final SoundEvent PHANTOM_FLAP = sound("entity.phantom.flap");
    public static final SoundEvent PHANTOM_HURT = sound("entity.phantom.hurt");
    public static final SoundEvent PHANTOM_SWOOP = sound("entity.phantom.swoop");
    public static final SoundEvent MUSIC_DISC_OTHERSIDE = sound("music_disc.otherside");
    public static final SoundEvent CAVES_AND_CLIFFS_MUSIC = sound("music.caves_and_cliffs");
    public static final SoundEvent HONEY_BLOCK_BREAK = sound("block.honey_block.break");
    public static final SoundEvent HONEY_BLOCK_STEP = sound("block.honey_block.step");
    public static final SoundEvent HONEY_BLOCK_PLACE = sound("block.honey_block.place");
    public static final SoundEvent HONEY_BLOCK_HIT = sound("block.honey_block.hit");
    public static final SoundEvent HONEY_BLOCK_FALL = sound("block.honey_block.fall");
    public static final SoundEvent HONEY_BLOCK_SLIDE = sound("block.honey_block.slide");
    public static final SoundEvent BIG_DRIPLEAF_TILT_DOWN = sound("block.big_dripleaf.tilt_down");
    public static final SoundEvent BIG_DRIPLEAF_TILT_UP = sound("block.big_dripleaf.tilt_up");
    public static final SoundEvent SWEET_BERRY_BUSH_BREAK = sound("block.sweet_berry_bush.break");
    public static final SoundEvent SWEET_BERRY_BUSH_PLACE = sound("block.sweet_berry_bush.place");
    public static final SoundEvent SWEET_BERRY_BUSH_PICK_BERRIES =
            sound("block.sweet_berry_bush.pick_berries");
    public static final SoundEvent PLAYER_HURT_SWEET_BERRY_BUSH =
            sound("entity.player.hurt_sweet_berry_bush");
    public static final SoundEvent FOX_AGGRO = sound("entity.fox.aggro");
    public static final SoundEvent FOX_AMBIENT = sound("entity.fox.ambient");
    public static final SoundEvent FOX_BITE = sound("entity.fox.bite");
    public static final SoundEvent FOX_DEATH = sound("entity.fox.death");
    public static final SoundEvent FOX_EAT = sound("entity.fox.eat");
    public static final SoundEvent FOX_HURT = sound("entity.fox.hurt");
    public static final SoundEvent FOX_SCREECH = sound("entity.fox.screech");
    public static final SoundEvent FOX_SLEEP = sound("entity.fox.sleep");
    public static final SoundEvent FOX_SNIFF = sound("entity.fox.sniff");
    public static final SoundEvent FOX_SPIT = sound("entity.fox.spit");
    public static final SoundEvent PANDA_AGGRESSIVE_AMBIENT = sound("entity.panda.aggressive_ambient");
    public static final SoundEvent PANDA_AMBIENT = sound("entity.panda.ambient");
    public static final SoundEvent PANDA_BITE = sound("entity.panda.bite");
    public static final SoundEvent PANDA_CANT_BREED = sound("entity.panda.cant_breed");
    public static final SoundEvent PANDA_DEATH = sound("entity.panda.death");
    public static final SoundEvent PANDA_EAT = sound("entity.panda.eat");
    public static final SoundEvent PANDA_HURT = sound("entity.panda.hurt");
    public static final SoundEvent PANDA_PRE_SNEEZE = sound("entity.panda.pre_sneeze");
    public static final SoundEvent PANDA_SNEEZE = sound("entity.panda.sneeze");
    public static final SoundEvent PANDA_STEP = sound("entity.panda.step");
    public static final SoundEvent PANDA_WORRIED_AMBIENT = sound("entity.panda.worried_ambient");
    public static final SoundEvent CAVE_VINES_BREAK = sound("block.cave_vines.break");
    public static final SoundEvent CAVE_VINES_STEP = sound("block.cave_vines.step");
    public static final SoundEvent CAVE_VINES_PLACE = sound("block.cave_vines.place");
    public static final SoundEvent CAVE_VINES_HIT = sound("block.cave_vines.hit");
    public static final SoundEvent CAVE_VINES_FALL = sound("block.cave_vines.fall");
    public static final SoundEvent CAVE_VINES_PICK_BERRIES = sound("block.cave_vines.pick_berries");
    public static final SoundEvent AMETHYST_BLOCK_BREAK = sound("block.amethyst_block.break");
    public static final SoundEvent AMETHYST_BLOCK_STEP = sound("block.amethyst_block.step");
    public static final SoundEvent AMETHYST_BLOCK_PLACE = sound("block.amethyst_block.place");
    public static final SoundEvent AMETHYST_BLOCK_HIT = sound("block.amethyst_block.hit");
    public static final SoundEvent AMETHYST_BLOCK_FALL = sound("block.amethyst_block.fall");
    public static final SoundEvent AMETHYST_BLOCK_CHIME = sound("block.amethyst_block.chime");
    public static final SoundEvent AMETHYST_CLUSTER_BREAK = sound("block.amethyst_cluster.break");
    public static final SoundEvent AMETHYST_CLUSTER_STEP = sound("block.amethyst_cluster.step");
    public static final SoundEvent AMETHYST_CLUSTER_PLACE = sound("block.amethyst_cluster.place");
    public static final SoundEvent AMETHYST_CLUSTER_HIT = sound("block.amethyst_cluster.hit");
    public static final SoundEvent AMETHYST_CLUSTER_FALL = sound("block.amethyst_cluster.fall");
    public static final SoundEvent SMALL_AMETHYST_BUD_BREAK = sound("block.small_amethyst_bud.break");
    public static final SoundEvent SMALL_AMETHYST_BUD_PLACE = sound("block.small_amethyst_bud.place");
    public static final SoundEvent MEDIUM_AMETHYST_BUD_BREAK = sound("block.medium_amethyst_bud.break");
    public static final SoundEvent MEDIUM_AMETHYST_BUD_PLACE = sound("block.medium_amethyst_bud.place");
    public static final SoundEvent LARGE_AMETHYST_BUD_BREAK = sound("block.large_amethyst_bud.break");
    public static final SoundEvent LARGE_AMETHYST_BUD_PLACE = sound("block.large_amethyst_bud.place");
    public static final SoundEvent CALCITE_BREAK = sound("block.calcite.break");
    public static final SoundEvent CALCITE_STEP = sound("block.calcite.step");
    public static final SoundEvent CALCITE_PLACE = sound("block.calcite.place");
    public static final SoundEvent CALCITE_HIT = sound("block.calcite.hit");
    public static final SoundEvent CALCITE_FALL = sound("block.calcite.fall");
    public static final SoundEvent BASALT_BREAK = sound("block.basalt.break");
    public static final SoundEvent BASALT_STEP = sound("block.basalt.step");
    public static final SoundEvent BASALT_PLACE = sound("block.basalt.place");
    public static final SoundEvent BASALT_HIT = sound("block.basalt.hit");
    public static final SoundEvent BASALT_FALL = sound("block.basalt.fall");
    public static final SoundType SWEET_BERRY_BUSH = new SoundType(1.0F, 1.0F,
            SWEET_BERRY_BUSH_BREAK, SoundEvents.BLOCK_GRASS_STEP, SWEET_BERRY_BUSH_PLACE,
            SoundEvents.BLOCK_GRASS_HIT, SoundEvents.BLOCK_GRASS_FALL);
    public static final SoundType CAVE_VINES = new SoundType(1.0F, 1.0F,
            CAVE_VINES_BREAK, CAVE_VINES_STEP, CAVE_VINES_PLACE, CAVE_VINES_HIT, CAVE_VINES_FALL);
    public static final SoundEvent BEE_AMBIENT = sound("entity.bee.ambient");
    public static final SoundEvent BEE_AGGRESSIVE = sound("entity.bee.aggressive");
    public static final SoundEvent BEE_LOOP = sound("entity.bee.loop");
    public static final SoundEvent BEE_LOOP_AGGRESSIVE = sound("entity.bee.loop_aggressive");
    public static final SoundEvent BEE_HURT = sound("entity.bee.hurt");
    public static final SoundEvent BEE_DEATH = sound("entity.bee.death");
    public static final SoundEvent BEE_POLLINATE = sound("entity.bee.pollinate");
    public static final SoundEvent BEE_STING = sound("entity.bee.sting");
    public static final SoundEvent BEE_ENTER_HIVE = sound("block.beehive.enter");
    public static final SoundEvent BEE_EXIT_HIVE = sound("block.beehive.exit");
    public static final SoundEvent BEEHIVE_WORK = sound("block.beehive.work");
    public static final SoundEvent BEEHIVE_SHEAR = sound("block.beehive.shear");
    public static final SoundEvent BEEHIVE_DRIP = sound("block.beehive.drip");
    public static final SoundEvent HONEY_BOTTLE_DRINK = sound("item.honey_bottle.drink");
    public static final SoundType CORAL = blockSoundType("coral_block");
    public static final SoundType WET_GRASS = blockSoundType("wet_grass");
    public static final SoundEvent TURTLE_AMBIENT = sound("entity.turtle.ambient_land");
    public static final SoundEvent TURTLE_HURT = sound("entity.turtle.hurt");
    public static final SoundEvent TURTLE_BABY_HURT = sound("entity.turtle.hurt_baby");
    public static final SoundEvent TURTLE_DEATH = sound("entity.turtle.death");
    public static final SoundEvent TURTLE_BABY_DEATH = sound("entity.turtle.death_baby");
    public static final SoundEvent TURTLE_STEP = sound("entity.turtle.shamble");
    public static final SoundEvent TURTLE_BABY_STEP = sound("entity.turtle.shamble_baby");
    public static final SoundEvent TURTLE_SWIM = sound("entity.turtle.swim");
    public static final SoundEvent TURTLE_LAY_EGG = sound("entity.turtle.lay_egg");
    public static final SoundEvent TURTLE_EGG_CRACK = sound("entity.turtle.egg_crack");
    public static final SoundEvent TURTLE_EGG_BREAK = sound("entity.turtle.egg_break");
    public static final SoundEvent TURTLE_EGG_HATCH = sound("entity.turtle.egg_hatch");
    public static final SoundEvent ZOMBIE_DESTROY_EGG = sound("entity.zombie.destroy_egg");
    public static final SoundEvent TURTLE_ARMOR_EQUIP = sound("item.armor.equip_turtle");
    public static final SoundEvent NETHERITE_ARMOR_EQUIP = sound("item.armor.equip_netherite");
    public static final SoundEvent ANCIENT_DEBRIS_BREAK = sound("block.ancient_debris.break");
    public static final SoundEvent HORSE_ARMOR = sound("entity.horse.armor");
    public static final SoundEvent HORSE_SADDLE = sound("entity.horse.saddle");
    public static final SoundEvent HORSE_EAT = sound("entity.horse.eat");
    public static final SoundEvent HORSE_AMBIENT = sound("entity.horse.ambient");
    public static final SoundEvent HORSE_ANGRY = sound("entity.horse.angry");
    public static final SoundEvent HORSE_BREATHE = sound("entity.horse.breathe");
    public static final SoundEvent HORSE_DEATH = sound("entity.horse.death");
    public static final SoundEvent HORSE_GALLOP = sound("entity.horse.gallop");
    public static final SoundEvent HORSE_HURT = sound("entity.horse.hurt");
    public static final SoundEvent HORSE_JUMP = sound("entity.horse.jump");
    public static final SoundEvent HORSE_LAND = sound("entity.horse.land");
    public static final SoundEvent HORSE_STEP = sound("entity.horse.step");
    public static final SoundEvent HORSE_STEP_WOOD = sound("entity.horse.step_wood");
    public static final SoundEvent HORSE_ARMOR_UNEQUIP = sound("item.horse_armor.unequip");
    public static final SoundEvent BABY_HORSE_AMBIENT = sound("entity.baby_horse.ambient");
    public static final SoundEvent BABY_HORSE_ANGRY = sound("entity.baby_horse.angry");
    public static final SoundEvent BABY_HORSE_BREATHE = sound("entity.baby_horse.breathe");
    public static final SoundEvent BABY_HORSE_DEATH = sound("entity.baby_horse.death");
    public static final SoundEvent BABY_HORSE_EAT = sound("entity.baby_horse.eat");
    public static final SoundEvent BABY_HORSE_HURT = sound("entity.baby_horse.hurt");
    public static final SoundEvent BABY_HORSE_LAND = sound("entity.baby_horse.land");
    public static final SoundEvent BABY_HORSE_STEP = sound("entity.baby_horse.step");
    public static final SoundEvent SKELETON_HORSE_AMBIENT_WATER = sound("entity.skeleton_horse.ambient_water");
    public static final SoundEvent SKELETON_HORSE_AMBIENT = sound("entity.skeleton_horse.ambient");
    public static final SoundEvent SKELETON_HORSE_DEATH = sound("entity.skeleton_horse.death");
    public static final SoundEvent SKELETON_HORSE_HURT = sound("entity.skeleton_horse.hurt");
    public static final SoundEvent SKELETON_HORSE_GALLOP_WATER = sound("entity.skeleton_horse.gallop_water");
    public static final SoundEvent SKELETON_HORSE_JUMP_WATER = sound("entity.skeleton_horse.jump_water");
    public static final SoundEvent SKELETON_HORSE_STEP_WATER = sound("entity.skeleton_horse.step_water");
    public static final SoundEvent SKELETON_HORSE_SWIM = sound("entity.skeleton_horse.swim");
    public static final SoundEvent ZOMBIE_HORSE_EAT = sound("entity.zombie_horse.eat");
    public static final SoundEvent ZOMBIE_HORSE_ANGRY = sound("entity.zombie_horse.angry");
    public static final SoundEvent ZOMBIE_HORSE_AMBIENT = sound("entity.zombie_horse.ambient");
    public static final SoundEvent ZOMBIE_HORSE_DEATH = sound("entity.zombie_horse.death");
    public static final SoundEvent ZOMBIE_HORSE_HURT = sound("entity.zombie_horse.hurt");
    public static final SoundEvent ANCIENT_DEBRIS_STEP = sound("block.ancient_debris.step");
    public static final SoundEvent ANCIENT_DEBRIS_PLACE = sound("block.ancient_debris.place");
    public static final SoundEvent ANCIENT_DEBRIS_HIT = sound("block.ancient_debris.hit");
    public static final SoundEvent ANCIENT_DEBRIS_FALL = sound("block.ancient_debris.fall");
    public static final SoundType ANCIENT_DEBRIS = new SoundType(1.0F, 1.0F,
            ANCIENT_DEBRIS_BREAK, ANCIENT_DEBRIS_STEP, ANCIENT_DEBRIS_PLACE,
            ANCIENT_DEBRIS_HIT, ANCIENT_DEBRIS_FALL);
    public static final SoundEvent AXOLOTL_ATTACK = sound("entity.axolotl.attack");
    public static final SoundEvent AXOLOTL_DEATH = sound("entity.axolotl.death");
    public static final SoundEvent AXOLOTL_HURT = sound("entity.axolotl.hurt");
    public static final SoundEvent AXOLOTL_IDLE_AIR = sound("entity.axolotl.idle_air");
    public static final SoundEvent AXOLOTL_IDLE_WATER = sound("entity.axolotl.idle_water");
    public static final SoundEvent AXOLOTL_SPLASH = sound("entity.axolotl.splash");
    public static final SoundEvent AXOLOTL_SWIM = sound("entity.axolotl.swim");
    public static final SoundEvent BUCKET_FILL_AXOLOTL = sound("item.bucket.fill_axolotl");
    public static final SoundEvent BUCKET_EMPTY_AXOLOTL = sound("item.bucket.empty_axolotl");
    public static final SoundEvent BUCKET_FILL_FISH = sound("item.bucket.fill_fish");
    public static final SoundEvent BUCKET_EMPTY_FISH = sound("item.bucket.empty_fish");
    public static final SoundEvent COD_AMBIENT = sound("entity.cod.ambient");
    public static final SoundEvent COD_DEATH = sound("entity.cod.death");
    public static final SoundEvent COD_FLOP = sound("entity.cod.flop");
    public static final SoundEvent COD_HURT = sound("entity.cod.hurt");
    public static final SoundEvent FISH_SWIM = sound("entity.fish.swim");
    public static final SoundEvent SALMON_AMBIENT = sound("entity.salmon.ambient");
    public static final SoundEvent SALMON_DEATH = sound("entity.salmon.death");
    public static final SoundEvent SALMON_FLOP = sound("entity.salmon.flop");
    public static final SoundEvent SALMON_HURT = sound("entity.salmon.hurt");
    public static final SoundEvent PUFFERFISH_AMBIENT = sound("entity.puffer_fish.ambient");
    public static final SoundEvent PUFFERFISH_BLOW_OUT = sound("entity.puffer_fish.blow_out");
    public static final SoundEvent PUFFERFISH_BLOW_UP = sound("entity.puffer_fish.blow_up");
    public static final SoundEvent PUFFERFISH_DEATH = sound("entity.puffer_fish.death");
    public static final SoundEvent PUFFERFISH_FLOP = sound("entity.puffer_fish.flop");
    public static final SoundEvent PUFFERFISH_HURT = sound("entity.puffer_fish.hurt");
    public static final SoundEvent PUFFERFISH_STING = sound("entity.puffer_fish.sting");
    public static final SoundEvent TROPICAL_FISH_AMBIENT = sound("entity.tropical_fish.ambient");
    public static final SoundEvent TROPICAL_FISH_DEATH = sound("entity.tropical_fish.death");
    public static final SoundEvent TROPICAL_FISH_FLOP = sound("entity.tropical_fish.flop");
    public static final SoundEvent TROPICAL_FISH_HURT = sound("entity.tropical_fish.hurt");
    public static final SoundEvent DOLPHIN_AMBIENT = sound("entity.dolphin.ambient");
    public static final SoundEvent DOLPHIN_AMBIENT_WATER = sound("entity.dolphin.ambient_water");
    public static final SoundEvent DOLPHIN_ATTACK = sound("entity.dolphin.attack");
    public static final SoundEvent DOLPHIN_DEATH = sound("entity.dolphin.death");
    public static final SoundEvent DOLPHIN_EAT = sound("entity.dolphin.eat");
    public static final SoundEvent DOLPHIN_HURT = sound("entity.dolphin.hurt");
    public static final SoundEvent DOLPHIN_JUMP = sound("entity.dolphin.jump");
    public static final SoundEvent DOLPHIN_PLAY = sound("entity.dolphin.play");
    public static final SoundEvent DOLPHIN_SPLASH = sound("entity.dolphin.splash");
    public static final SoundEvent DOLPHIN_SWIM = sound("entity.dolphin.swim");
    public static final SoundEvent DROWNED_AMBIENT = sound("entity.drowned.ambient");
    public static final SoundEvent DROWNED_AMBIENT_WATER = sound("entity.drowned.ambient_water");
    public static final SoundEvent DROWNED_DEATH = sound("entity.drowned.death");
    public static final SoundEvent DROWNED_DEATH_WATER = sound("entity.drowned.death_water");
    public static final SoundEvent DROWNED_HURT = sound("entity.drowned.hurt");
    public static final SoundEvent DROWNED_HURT_WATER = sound("entity.drowned.hurt_water");
    public static final SoundEvent DROWNED_SHOOT = sound("entity.drowned.shoot");
    public static final SoundEvent DROWNED_STEP = sound("entity.drowned.step");
    public static final SoundEvent DROWNED_SWIM = sound("entity.drowned.swim");
    public static final SoundEvent PLAYER_SWIM = sound("entity.player.swim");
    public static final SoundEvent PLAYER_SPLASH = sound("entity.player.splash");
    public static final SoundEvent PLAYER_SPLASH_HIGH_SPEED = sound("entity.player.splash.high_speed");
    public static final SoundEvent UNDERWATER_ENTER = sound("ambient.underwater.enter");
    public static final SoundEvent UNDERWATER_EXIT = sound("ambient.underwater.exit");
    public static final SoundEvent UNDERWATER_LOOP = sound("ambient.underwater.loop");
    public static final SoundEvent UNDERWATER_ADDITIONS = sound("ambient.underwater.loop.additions");
    public static final SoundEvent UNDERWATER_ADDITIONS_RARE =
            sound("ambient.underwater.loop.additions.rare");
    public static final SoundEvent UNDERWATER_ADDITIONS_ULTRA_RARE =
            sound("ambient.underwater.loop.additions.ultra_rare");
    public static final SoundEvent ZOMBIE_CONVERTED_TO_DROWNED =
            sound("entity.zombie.converted_to_drowned");
    public static final SoundEvent TRIDENT_THROW = sound("item.trident.throw");
    public static final SoundEvent TRIDENT_HIT = sound("item.trident.hit");
    public static final SoundEvent TRIDENT_HIT_GROUND = sound("item.trident.hit_ground");
    public static final SoundEvent TRIDENT_RETURN = sound("item.trident.return");
    public static final SoundEvent TRIDENT_RIPTIDE_1 = sound("item.trident.riptide_1");
    public static final SoundEvent TRIDENT_RIPTIDE_2 = sound("item.trident.riptide_2");
    public static final SoundEvent TRIDENT_RIPTIDE_3 = sound("item.trident.riptide_3");
    public static final SoundEvent TRIDENT_THUNDER = sound("item.trident.thunder");
    public static final SoundEvent MOOSHROOM_CONVERT = sound("entity.mooshroom.convert");
    public static final SoundEvent MOOSHROOM_EAT = sound("entity.mooshroom.eat");
    public static final SoundEvent MOOSHROOM_SUSPICIOUS_MILK =
            sound("entity.mooshroom.suspicious_milk");
    public static final SoundEvent GOAT_AMBIENT = sound("entity.goat.ambient");
    public static final SoundEvent GOAT_DEATH = sound("entity.goat.death");
    public static final SoundEvent GOAT_EAT = sound("entity.goat.eat");
    public static final SoundEvent GOAT_HORN_BREAK = sound("entity.goat.horn_break");
    public static final SoundEvent GOAT_HURT = sound("entity.goat.hurt");
    public static final SoundEvent GOAT_LONG_JUMP = sound("entity.goat.long_jump");
    public static final SoundEvent GOAT_MILK = sound("entity.goat.milk");
    public static final SoundEvent GOAT_PREPARE_RAM = sound("entity.goat.prepare_ram");
    public static final SoundEvent GOAT_RAM_IMPACT = sound("entity.goat.ram_impact");
    public static final SoundEvent GOAT_SCREAMING_AMBIENT = sound("entity.goat.screaming.ambient");
    public static final SoundEvent GOAT_SCREAMING_DEATH = sound("entity.goat.screaming.death");
    public static final SoundEvent GOAT_SCREAMING_EAT = sound("entity.goat.screaming.eat");
    public static final SoundEvent GOAT_SCREAMING_HURT = sound("entity.goat.screaming.hurt");
    public static final SoundEvent GOAT_SCREAMING_LONG_JUMP = sound("entity.goat.screaming.long_jump");
    public static final SoundEvent GOAT_SCREAMING_MILK = sound("entity.goat.screaming.milk");
    public static final SoundEvent GOAT_SCREAMING_PREPARE_RAM = sound("entity.goat.screaming.prepare_ram");
    public static final SoundEvent GOAT_SCREAMING_RAM_IMPACT = sound("entity.goat.screaming.ram_impact");
    public static final SoundEvent GOAT_STEP = sound("entity.goat.step");
    public static final SoundEvent[] GOAT_HORN_SOUNDS = {
            sound("item.goat_horn.sound.0"),
            sound("item.goat_horn.sound.1"),
            sound("item.goat_horn.sound.2"),
            sound("item.goat_horn.sound.3"),
            sound("item.goat_horn.sound.4"),
            sound("item.goat_horn.sound.5"),
            sound("item.goat_horn.sound.6"),
            sound("item.goat_horn.sound.7")
    };
    public static final SoundType GILDED_BLACKSTONE = blockSoundType("gilded_blackstone");
    public static final SoundType AZALEA = blockSoundType("azalea");
    public static final SoundType FLOWERING_AZALEA = blockSoundType("flowering_azalea");
    public static final SoundType AZALEA_LEAVES = blockSoundType("azalea_leaves");
    public static final SoundType MOSS = blockSoundType("moss");
    public static final SoundType MOSS_CARPET = blockSoundType("moss_carpet");
    public static final SoundType BIG_DRIPLEAF = blockSoundType("big_dripleaf");
    public static final SoundType SMALL_DRIPLEAF = blockSoundType("small_dripleaf");
    public static final SoundType ROOTED_DIRT = blockSoundType("rooted_dirt");
    public static final SoundType HANGING_ROOTS = blockSoundType("hanging_roots");
    public static final SoundType SPORE_BLOSSOM = blockSoundType("spore_blossom");
    public static final SoundType AMETHYST = new SoundType(1.0F, 1.0F,
            AMETHYST_BLOCK_BREAK, AMETHYST_BLOCK_STEP, AMETHYST_BLOCK_PLACE,
            AMETHYST_BLOCK_HIT, AMETHYST_BLOCK_FALL);
    public static final SoundType AMETHYST_CLUSTER = new SoundType(1.0F, 1.0F,
            AMETHYST_CLUSTER_BREAK, AMETHYST_CLUSTER_STEP, AMETHYST_CLUSTER_PLACE,
            AMETHYST_CLUSTER_HIT, AMETHYST_CLUSTER_FALL);
    public static final SoundType SMALL_AMETHYST_BUD = new SoundType(1.0F, 1.0F,
            SMALL_AMETHYST_BUD_BREAK, AMETHYST_CLUSTER_STEP, SMALL_AMETHYST_BUD_PLACE,
            AMETHYST_CLUSTER_HIT, AMETHYST_CLUSTER_FALL);
    public static final SoundType MEDIUM_AMETHYST_BUD = new SoundType(1.0F, 1.0F,
            MEDIUM_AMETHYST_BUD_BREAK, AMETHYST_CLUSTER_STEP, MEDIUM_AMETHYST_BUD_PLACE,
            AMETHYST_CLUSTER_HIT, AMETHYST_CLUSTER_FALL);
    public static final SoundType LARGE_AMETHYST_BUD = new SoundType(1.0F, 1.0F,
            LARGE_AMETHYST_BUD_BREAK, AMETHYST_CLUSTER_STEP, LARGE_AMETHYST_BUD_PLACE,
            AMETHYST_CLUSTER_HIT, AMETHYST_CLUSTER_FALL);
    public static final SoundType CALCITE = new SoundType(1.0F, 1.0F,
            CALCITE_BREAK, CALCITE_STEP, CALCITE_PLACE, CALCITE_HIT, CALCITE_FALL);
    public static final SoundType BASALT = new SoundType(1.0F, 1.0F,
            BASALT_BREAK, BASALT_STEP, BASALT_PLACE, BASALT_HIT, BASALT_FALL);
    public static final SoundType DEEPSLATE = blockSoundType("deepslate");
    public static final SoundType DEEPSLATE_BRICKS = blockSoundType("deepslate_bricks");
    public static final SoundType TUFF = blockSoundType("tuff");
    public static final SoundType COPPER = blockSoundType("copper");
    public static final SoundType NYLIUM = blockSoundType("nylium");
    public static final SoundType FUNGUS = blockSoundType("fungus");
    public static final SoundType ROOTS = blockSoundType("roots");
    public static final SoundType NETHER_SPROUTS = blockSoundType("nether_sprouts");
    public static final SoundType WEEPING_VINES = blockSoundType("weeping_vines");
    public static final SoundType TWISTING_VINES = new SoundType(1.0F, 0.5F,
            WEEPING_VINES.getBreakSound(), WEEPING_VINES.getStepSound(),
            WEEPING_VINES.getPlaceSound(), WEEPING_VINES.getHitSound(),
            WEEPING_VINES.getFallSound());
    public static final SoundType STEM = blockSoundType("stem");
    public static final SoundType WART_BLOCK = blockSoundType("wart_block");
    public static final SoundType SHROOMLIGHT = blockSoundType("shroomlight");
    public static final SoundType NETHER_WOOD = blockSoundType("nether_wood");
    public static final SoundEvent NETHER_WOOD_BUTTON_CLICK_OFF =
            sound("block.nether_wood_button.click_off");
    public static final SoundEvent NETHER_WOOD_BUTTON_CLICK_ON =
            sound("block.nether_wood_button.click_on");
    public static final SoundEvent NETHER_WOOD_DOOR_CLOSE = sound("block.nether_wood_door.close");
    public static final SoundEvent NETHER_WOOD_DOOR_OPEN = sound("block.nether_wood_door.open");
    public static final SoundEvent NETHER_WOOD_FENCE_GATE_CLOSE =
            sound("block.nether_wood_fence_gate.close");
    public static final SoundEvent NETHER_WOOD_FENCE_GATE_OPEN =
            sound("block.nether_wood_fence_gate.open");
    public static final SoundEvent NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF =
            sound("block.nether_wood_pressure_plate.click_off");
    public static final SoundEvent NETHER_WOOD_PRESSURE_PLATE_CLICK_ON =
            sound("block.nether_wood_pressure_plate.click_on");
    public static final SoundEvent NETHER_WOOD_TRAPDOOR_CLOSE =
            sound("block.nether_wood_trapdoor.close");
    public static final SoundEvent NETHER_WOOD_TRAPDOOR_OPEN =
            sound("block.nether_wood_trapdoor.open");
    public static final SoundEvent CRIMSON_FOREST_ADDITIONS =
            sound("ambient.crimson_forest.additions");
    public static final SoundEvent CRIMSON_FOREST_LOOP =
            sound("ambient.crimson_forest.loop");
    public static final SoundEvent CRIMSON_FOREST_MOOD =
            sound("ambient.crimson_forest.mood");
    public static final SoundEvent WARPED_FOREST_ADDITIONS =
            sound("ambient.warped_forest.additions");
    public static final SoundEvent WARPED_FOREST_LOOP =
            sound("ambient.warped_forest.loop");
    public static final SoundEvent WARPED_FOREST_MOOD =
            sound("ambient.warped_forest.mood");
    public static final SoundEvent AXE_STRIP = sound("item.axe.strip");
    public static final SoundEvent AXE_SCRAPE = sound("item.axe.scrape");
    public static final SoundEvent AXE_WAX_OFF = sound("item.axe.wax_off");
    public static final SoundEvent HONEYCOMB_WAX_ON = sound("item.honeycomb.wax_on");
    public static final SoundEvent SPYGLASS_USE = sound("item.spyglass.use");
    public static final SoundEvent SPYGLASS_STOP_USING = sound("item.spyglass.stop_using");
    public static final SoundEvent CAKE_ADD_CANDLE = sound("block.cake.add_candle");
    public static final SoundEvent CANDLE_AMBIENT = sound("block.candle.ambient");
    public static final SoundEvent CANDLE_EXTINGUISH = sound("block.candle.extinguish");
    public static final SoundType DRIPSTONE_BLOCK = blockSoundType("dripstone_block");
    public static final SoundType POINTED_DRIPSTONE = blockSoundType("pointed_dripstone");
    public static final SoundType POWDER_SNOW = blockSoundType("powder_snow");
    public static final SoundType CHAIN = blockSoundType("chain");
    public static final SoundType LANTERN = blockSoundType("lantern");
    public static final SoundType BAMBOO = blockSoundType("bamboo");
    public static final SoundType SCAFFOLDING = blockSoundType("scaffolding");
    public static final SoundType BAMBOO_SAPLING = new SoundType(1.0F, 1.0F,
            sound("block.bamboo_sapling.break"), BAMBOO.getStepSound(),
            sound("block.bamboo_sapling.place"), sound("block.bamboo_sapling.hit"), BAMBOO.getFallSound());
    public static final SoundType CANDLE = blockSoundType("candle");
    public static final SoundEvent POINTED_DRIPSTONE_DRIP_WATER =
            sound("block.pointed_dripstone.drip_water");
    public static final SoundEvent POINTED_DRIPSTONE_DRIP_LAVA =
            sound("block.pointed_dripstone.drip_lava");
    public static final SoundEvent POINTED_DRIPSTONE_DRIP_WATER_CAULDRON =
            sound("block.pointed_dripstone.drip_water_into_cauldron");
    public static final SoundEvent POINTED_DRIPSTONE_DRIP_LAVA_CAULDRON =
            sound("block.pointed_dripstone.drip_lava_into_cauldron");
    public static final SoundEvent POINTED_DRIPSTONE_LAND =
            sound("block.pointed_dripstone.land");
    public static final SoundEvent BUCKET_FILL_POWDER_SNOW =
            sound("item.bucket.fill_powder_snow");
    public static final SoundEvent BUCKET_EMPTY_POWDER_SNOW =
            sound("item.bucket.empty_powder_snow");
    public static final SoundEvent SKELETON_CONVERTED_TO_STRAY =
            sound("entity.skeleton.converted_to_stray");
    public static final SoundEvent PLAYER_HURT_FREEZE =
            sound("entity.player.hurt_freeze");
    public static final SoundEvent BEACON_ACTIVATE = sound("block.beacon.activate");
    public static final SoundEvent BEACON_AMBIENT = sound("block.beacon.ambient");
    public static final SoundEvent BEACON_DEACTIVATE = sound("block.beacon.deactivate");
    public static final SoundEvent BEACON_POWER_SELECT = sound("block.beacon.power_select");
    public static final SoundEvent CONDUIT_ACTIVATE = sound("block.conduit.activate");
    public static final SoundEvent CONDUIT_AMBIENT = sound("block.conduit.ambient");
    public static final SoundEvent CONDUIT_AMBIENT_SHORT = sound("block.conduit.ambient_short");
    public static final SoundEvent CONDUIT_ATTACK_TARGET = sound("block.conduit.attack_target");
    public static final SoundEvent CONDUIT_DEACTIVATE = sound("block.conduit.deactivate");
    public static final SoundEvent BUBBLE_COLUMN_UPWARDS_AMBIENT =
            sound("block.bubble_column.upwards_ambient");
    public static final SoundEvent BUBBLE_COLUMN_WHIRLPOOL_AMBIENT =
            sound("block.bubble_column.whirlpool_ambient");
    public static final SoundEvent BUBBLE_COLUMN_UPWARDS_INSIDE =
            sound("block.bubble_column.upwards_inside");
    public static final SoundEvent BUBBLE_COLUMN_WHIRLPOOL_INSIDE =
            sound("block.bubble_column.whirlpool_inside");
    public static final SoundEvent VINE_STEP = sound("block.vine.step");
    public static final SoundEvent PUMPKIN_CARVE = sound("block.pumpkin.carve");

    private FFDSounds() {
    }

    public static SoundEvent[] all() {
        return ALL.toArray(new SoundEvent[0]);
    }

    private static SoundType blockSoundType(String name) {
        String prefix = "block." + name + ".";
        return new SoundType(1.0F, 1.0F, sound(prefix + "break"), sound(prefix + "step"),
                sound(prefix + "place"), sound(prefix + "hit"), sound(prefix + "fall"));
    }

    private static SoundEvent sound(String name) {
        ResourceLocation id = new ResourceLocation(FarmerFutureDelight.MODID, name);
        SoundEvent event = new SoundEvent(id).setRegistryName(id);
        ALL.add(event);
        return event;
    }
}
