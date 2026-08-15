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

    public static final SoundEvent GLOW_SQUID_AMBIENT = sound("entity.glow_squid.ambient");
    public static final SoundEvent GLOW_SQUID_HURT = sound("entity.glow_squid.hurt");
    public static final SoundEvent GLOW_SQUID_DEATH = sound("entity.glow_squid.death");
    public static final SoundEvent GLOW_SQUID_SQUIRT = sound("entity.glow_squid.squirt");
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
    public static final SoundEvent AXOLOTL_ATTACK = sound("entity.axolotl.attack");
    public static final SoundEvent AXOLOTL_DEATH = sound("entity.axolotl.death");
    public static final SoundEvent AXOLOTL_HURT = sound("entity.axolotl.hurt");
    public static final SoundEvent AXOLOTL_IDLE_AIR = sound("entity.axolotl.idle_air");
    public static final SoundEvent AXOLOTL_IDLE_WATER = sound("entity.axolotl.idle_water");
    public static final SoundEvent AXOLOTL_SPLASH = sound("entity.axolotl.splash");
    public static final SoundEvent AXOLOTL_SWIM = sound("entity.axolotl.swim");
    public static final SoundEvent BUCKET_FILL_AXOLOTL = sound("item.bucket.fill_axolotl");
    public static final SoundEvent BUCKET_EMPTY_AXOLOTL = sound("item.bucket.empty_axolotl");
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
