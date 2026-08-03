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
