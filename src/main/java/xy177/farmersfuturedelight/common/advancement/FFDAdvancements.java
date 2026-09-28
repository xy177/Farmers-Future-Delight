package xy177.farmersfuturedelight.common.advancement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;

public final class FFDAdvancements {
    public static final SimpleTrigger WAX_ON = register("wax_on");
    public static final SimpleTrigger WAX_OFF = register("wax_off");
    public static final SimpleTrigger SPYGLASS_AT_PARROT = register("spyglass_at_parrot");
    public static final SimpleTrigger SPYGLASS_AT_GHAST = register("spyglass_at_ghast");
    public static final SimpleTrigger SPYGLASS_AT_DRAGON = register("spyglass_at_dragon");
    public static final SimpleTrigger LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE =
            register("lightning_rod_with_villager_no_fire");
    public static final SimpleTrigger AXOLOTL_IN_A_BUCKET = register("axolotl_in_a_bucket");
    public static final SimpleTrigger KILL_AXOLOTL_TARGET = register("kill_axolotl_target");
    public static final SimpleTrigger WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS =
            register("walk_on_powder_snow_with_leather_boots");
    public static final SimpleTrigger RIDE_A_BOAT_WITH_A_GOAT =
            register("ride_a_boat_with_a_goat");
    public static final SimpleTrigger FALL_FROM_WORLD_HEIGHT =
            register("fall_from_world_height");
    public static final SimpleTrigger PLAY_JUKEBOX_IN_MEADOWS =
            register("play_jukebox_in_meadows");
    public static final SimpleTrigger TRADE_AT_WORLD_HEIGHT =
            register("trade_at_world_height");
    public static final SimpleTrigger CONSUME_SWEET_BERRIES =
            register("consume_sweet_berries");
    public static final SimpleTrigger CONSUME_GLOW_BERRIES =
            register("consume_glow_berries");
    public static final SimpleTrigger CONSUME_HONEY_BOTTLE =
            register("consume_honey_bottle");
    public static final SimpleTrigger CONSUME_DRIED_KELP =
            register("consume_dried_kelp");
    public static final SimpleTrigger BRED_BEE = register("bred_bee");
    public static final SimpleTrigger BRED_TURTLE = register("bred_turtle");
    public static final SimpleTrigger BRED_GOAT = register("bred_goat");
    public static final SimpleTrigger BRED_AXOLOTL = register("bred_axolotl");
    public static final SimpleTrigger KILL_PHANTOM = register("kill_phantom");
    public static final SimpleTrigger VISIT_LUSH_CAVES = register("visit_lush_caves");
    public static final SimpleTrigger VISIT_DRIPSTONE_CAVES =
            register("visit_dripstone_caves");
    public static final SimpleTrigger VISIT_MEADOW = register("visit_meadow");
    public static final SimpleTrigger VISIT_GROVE = register("visit_grove");
    public static final SimpleTrigger VISIT_SNOWY_SLOPES = register("visit_snowy_slopes");
    public static final SimpleTrigger VISIT_JAGGED_PEAKS = register("visit_jagged_peaks");
    public static final SimpleTrigger VISIT_FROZEN_PEAKS = register("visit_frozen_peaks");
    public static final SimpleTrigger VISIT_STONY_PEAKS = register("visit_stony_peaks");
    public static final SimpleTrigger FISHY_BUSINESS = register("fishy_business");
    public static final SimpleTrigger TACTICAL_FISHING = register("tactical_fishing");
    public static final SimpleTrigger THROW_TRIDENT = register("throw_trident");
    public static final SimpleTrigger VERY_VERY_FRIGHTENING =
            register("very_very_frightening");

    private FFDAdvancements() {
    }

    public static void init() {
    }

    public static void triggerVerticalBiome(EntityPlayerMP player, FFDVerticalBiome biome) {
        switch (biome) {
            case LUSH_CAVES:
                VISIT_LUSH_CAVES.trigger(player);
                break;
            case DRIPSTONE_CAVES:
                VISIT_DRIPSTONE_CAVES.trigger(player);
                break;
            case MEADOW:
                VISIT_MEADOW.trigger(player);
                break;
            case GROVE:
                VISIT_GROVE.trigger(player);
                break;
            case SNOWY_SLOPES:
                VISIT_SNOWY_SLOPES.trigger(player);
                break;
            case JAGGED_PEAKS:
                VISIT_JAGGED_PEAKS.trigger(player);
                break;
            case FROZEN_PEAKS:
                VISIT_FROZEN_PEAKS.trigger(player);
                break;
            case STONY_PEAKS:
                VISIT_STONY_PEAKS.trigger(player);
                break;
            default:
                break;
        }
    }

    public static void checkSpyglassTarget(EntityPlayerMP player) {
        if (!SPYGLASS_AT_PARROT.hasListeners(player)
                && !SPYGLASS_AT_GHAST.hasListeners(player)
                && !SPYGLASS_AT_DRAGON.hasListeners(player)) {
            return;
        }
        Entity target = findLookTarget(player, 100.0D);
        if (target instanceof EntityParrot) {
            SPYGLASS_AT_PARROT.trigger(player);
        } else if (target instanceof EntityGhast) {
            SPYGLASS_AT_GHAST.trigger(player);
        } else if (target instanceof EntityDragon) {
            SPYGLASS_AT_DRAGON.trigger(player);
        }
    }

    private static Entity findLookTarget(EntityPlayerMP player, double range) {
        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d end = start.addVector(look.x * range, look.y * range, look.z * range);
        RayTraceResult blockHit = player.world.rayTraceBlocks(start, end, false, true, false);
        double closestDistance = blockHit == null ? range * range
                : start.squareDistanceTo(blockHit.hitVec);
        Entity closest = null;
        AxisAlignedBB searchBox = player.getEntityBoundingBox()
                .expand(look.x * range, look.y * range, look.z * range).grow(1.0D);
        List<Entity> candidates = player.world.getEntitiesInAABBexcluding(player, searchBox,
                FFDAdvancements::isSpyglassTarget);
        for (Entity candidate : candidates) {
            AxisAlignedBB bounds = candidate.getEntityBoundingBox()
                    .grow(candidate.getCollisionBorderSize());
            RayTraceResult intercept = bounds.calculateIntercept(start, end);
            if (bounds.contains(start)) {
                if (closestDistance >= 0.0D) {
                    closest = candidate;
                    closestDistance = 0.0D;
                }
            } else if (intercept != null) {
                double distance = start.squareDistanceTo(intercept.hitVec);
                if (distance < closestDistance) {
                    closest = candidate;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    private static boolean isSpyglassTarget(Entity entity) {
        return entity.isEntityAlive() && (entity instanceof EntityParrot
                || entity instanceof EntityGhast || entity instanceof EntityDragon);
    }

    private static SimpleTrigger register(String name) {
        return CriteriaTriggers.register(new SimpleTrigger(
                new ResourceLocation(FarmerFutureDelight.MODID, name)));
    }

    public static final class SimpleTrigger implements ICriterionTrigger<SimpleTrigger.Instance> {
        private final ResourceLocation id;
        private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

        private SimpleTrigger(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public void addListener(PlayerAdvancements advancements,
                                ICriterionTrigger.Listener<Instance> listener) {
            listeners.computeIfAbsent(advancements, Listeners::new).add(listener);
        }

        @Override
        public void removeListener(PlayerAdvancements advancements,
                                   ICriterionTrigger.Listener<Instance> listener) {
            Listeners playerListeners = listeners.get(advancements);
            if (playerListeners != null) {
                playerListeners.remove(listener);
                if (playerListeners.isEmpty()) {
                    listeners.remove(advancements);
                }
            }
        }

        @Override
        public void removeAllListeners(PlayerAdvancements advancements) {
            listeners.remove(advancements);
        }

        @Override
        public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
            return new Instance(id);
        }

        public boolean hasListeners(EntityPlayerMP player) {
            return listeners.containsKey(player.getAdvancements());
        }

        public void trigger(EntityPlayerMP player) {
            Listeners playerListeners = listeners.get(player.getAdvancements());
            if (playerListeners != null) {
                playerListeners.trigger();
            }
        }

        public static final class Instance extends AbstractCriterionInstance {
            private Instance(ResourceLocation id) {
                super(id);
            }
        }

        private static final class Listeners {
            private final PlayerAdvancements advancements;
            private final Set<ICriterionTrigger.Listener<Instance>> listeners = Sets.newHashSet();

            private Listeners(PlayerAdvancements advancements) {
                this.advancements = advancements;
            }

            private void add(ICriterionTrigger.Listener<Instance> listener) {
                listeners.add(listener);
            }

            private void remove(ICriterionTrigger.Listener<Instance> listener) {
                listeners.remove(listener);
            }

            private boolean isEmpty() {
                return listeners.isEmpty();
            }

            private void trigger() {
                for (ICriterionTrigger.Listener<Instance> listener
                        : Lists.newArrayList(listeners)) {
                    listener.grantCriterion(advancements);
                }
            }
        }
    }
}
