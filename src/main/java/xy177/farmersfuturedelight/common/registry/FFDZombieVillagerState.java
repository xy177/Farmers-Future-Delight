package xy177.farmersfuturedelight.common.registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDZombieVillagerState {
    public static final String STATE_TAG = "farmers_future_delight:zombie_villager";
    public static final String CLIENT_STATE = "farmers_future_delight:zombie_villager_client_state";
    private static final String UNASSIGNED_TAG = "farmers_future_delight:workstation_unassigned";
    private static final Map<EntityZombieVillager, NBTTagCompound> LAST_STATE = new WeakHashMap<>();

    private FFDZombieVillagerState() {
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().getClass() != EntityZombieVillager.class
                || event.getEntityLiving().world.isRemote || !FFDConfig.modernVillagerSystem) return;
        EntityZombieVillager zombie = (EntityZombieVillager) event.getEntityLiving();
        if (Math.floorMod(zombie.ticksExisted + zombie.getEntityId(), 20) != 0) return;
        NBTTagCompound current = clientState(zombie);
        if (current.equals(LAST_STATE.get(zombie))) return;
        LAST_STATE.put(zombie, current);
        FFDNetwork.sendVillagerState(zombie);
    }

    public static NBTTagCompound ensureState(EntityZombieVillager zombie) {
        if (!FFDConfig.modernVillagerSystem) {
            zombie.getEntityData().removeTag(STATE_TAG);
            return new NBTTagCompound();
        }
        NBTTagCompound state = zombie.getEntityData().getCompoundTag(STATE_TAG);
        if (zombie.getClass() != EntityZombieVillager.class || zombie.world.isRemote) return state;
        VillagerProfession profession = zombie.getForgeProfession();
        String source = professionId(profession);
        NBTTagCompound snapshot = FFDVillagerConversion.getMatchingSnapshot(zombie);
        if (!source.equals(state.getString("sourceProfession"))) {
            NBTTagCompound previous = state;
            state = new NBTTagCompound();
            if (FFDVillagerTypes.isType(previous.getString("type"))) {
                state.setString("type", previous.getString("type"));
            }
        }
        if (!FFDVillagerTypes.isType(state.getString("type"))) {
            String type = snapshot == null ? "" : snapshot.getCompoundTag("ForgeData")
                    .getString(FFDModernVillagerTrades.VARIANT_TAG);
            if (!FFDVillagerTypes.isType(type)) {
                type = zombie.getEntityData().getString(FFDModernVillagerTrades.VARIANT_TAG);
            }
            state.setString("type", FFDVillagerTypes.isType(type) ? type
                    : FFDVillagerTypes.byBiome(zombie.world, zombie.getPosition()));
        }
        if (!state.hasKey("career", 99)) {
            boolean unassigned = (snapshot == null ? zombie.getEntityData()
                    : snapshot.getCompoundTag("ForgeData")).getBoolean(UNASSIGNED_TAG);
            int career = snapshot == null ? 0 : snapshot.getInteger("Career");
            career = chooseCareer(profession, career, unassigned, zombie.getRNG());
            state.setInteger("career", career);
            state.setString("careerName", careerName(profession, career));
            state.setBoolean("unassigned", unassigned);
            NBTTagCompound trades = snapshot == null ? new NBTTagCompound()
                    : snapshot.getCompoundTag(FFDVillagerRestocking.STATE_TAG);
            state.setInteger("level", Math.max(1, Math.min(5, trades.getInteger("level"))));
        }
        state.setString("sourceProfession", source);
        String modern = modernProfession(profession, state.getInteger("career"), state.getString("careerName"),
                state.getBoolean("unassigned"));
        state.setBoolean("modern", modern != null);
        state.setString("profession", modern == null ? "none" : modern);
        zombie.getEntityData().setTag(STATE_TAG, state);
        return state;
    }

    public static void initializeStructure(EntityZombieVillager zombie, NBTTagCompound data) {
        if (!FFDConfig.modernVillagerSystem || zombie.getClass() != EntityZombieVillager.class
                || zombie.world.isRemote) return;
        if (!zombie.getEntityData().hasKey(STATE_TAG, 10)) {
            VillagerProfession profession = zombie.getForgeProfession();
            NBTTagCompound modern = data.getCompoundTag("VillagerData");
            boolean unassigned = zombie.getEntityData().getBoolean(UNASSIGNED_TAG);
            int career = chooseCareer(profession, data.getInteger("Career"), unassigned, zombie.getRNG());
            NBTTagCompound state = new NBTTagCompound();
            state.setString("sourceProfession", professionId(profession));
            state.setInteger("career", career);
            state.setString("careerName", careerName(profession, career));
            state.setBoolean("unassigned", unassigned);
            state.setInteger("level", Math.max(1, Math.min(5, modern.getInteger("level"))));
            zombie.getEntityData().setTag(STATE_TAG, state);
        }
        ensureState(zombie);
    }

    static void capture(EntityVillager villager, EntityZombieVillager zombie) {
        if (!FFDConfig.modernVillagerSystem) return;
        VillagerProfession profession = villager.getProfessionForge();
        boolean unassigned = FFDProfessionWorkstations.isProfessionUnassigned(villager);
        int previous = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager,
                "field_175563_bv");
        int career = chooseCareer(profession, previous, unassigned, villager.getRNG());
        if (career != previous) ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class,
                villager, career, "field_175563_bv");
        NBTTagCompound state = new NBTTagCompound();
        state.setString("type", FFDVillagerTypes.ensureType(villager));
        state.setString("sourceProfession", professionId(profession));
        state.setInteger("career", career);
        state.setString("careerName", careerName(profession, career));
        state.setBoolean("unassigned", unassigned);
        state.setInteger("level", FFDVillagerRestocking.getLevel(villager));
        zombie.getEntityData().setTag(STATE_TAG, state);
        ensureState(zombie);
    }

    static void prepareCure(EntityVillager villager, EntityZombieVillager zombie) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound state = ensureState(zombie);
        villager.getEntityData().setString(FFDModernVillagerTrades.VARIANT_TAG, state.getString("type"));
        NBTTagCompound snapshot = FFDVillagerConversion.getMatchingSnapshot(zombie);
        int career = snapshot == null ? state.getInteger("career")
                : snapshot.getInteger("Career");
        if (career > 0) ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, villager,
                career, "field_175563_bv");
        if (state.getBoolean("unassigned")) villager.getEntityData().setBoolean(UNASSIGNED_TAG, true);
    }

    public static NBTTagCompound clientState(EntityZombieVillager zombie) {
        if (!FFDConfig.modernVillagerSystem) return new NBTTagCompound();
        NBTTagCompound saved = ensureState(zombie);
        NBTTagCompound state = new NBTTagCompound();
        state.setString("type", saved.getString("type"));
        state.setString("profession", saved.getString("profession"));
        state.setString("sourceProfession", saved.getString("sourceProfession"));
        state.setInteger("level", saved.getInteger("level"));
        state.setBoolean("modern", saved.getBoolean("modern"));
        return state;
    }

    public static void receiveClientState(EntityZombieVillager zombie, NBTTagCompound state) {
        if (!FFDConfig.modernVillagerSystem) {
            zombie.getEntityData().removeTag(CLIENT_STATE);
        } else if (zombie.getClass() == EntityZombieVillager.class) {
            zombie.getEntityData().setTag(CLIENT_STATE, state.copy());
        }
    }

    public static boolean canRender(EntityZombieVillager zombie) {
        if (!FFDConfig.modernVillagerSystem) return false;
        NBTTagCompound state = zombie.getEntityData().getCompoundTag(CLIENT_STATE);
        return zombie.getClass() == EntityZombieVillager.class && state.getBoolean("modern")
                && isSupportedProfession(zombie.getForgeProfession())
                && professionId(zombie.getForgeProfession()).equals(state.getString("sourceProfession"))
                && FFDVillagerTypes.isType(state.getString("type"))
                && isModernProfession(state.getString("profession"));
    }

    private static boolean isModernProfession(String profession) {
        switch (profession) {
            case "none":
            case "nitwit":
            case "farmer":
            case "fisherman":
            case "shepherd":
            case "fletcher":
            case "librarian":
            case "cartographer":
            case "cleric":
            case "armorer":
            case "weaponsmith":
            case "toolsmith":
            case "butcher":
            case "leatherworker":
            case "mason": return true;
            default: return false;
        }
    }

    private static String professionId(VillagerProfession profession) {
        return profession == null || profession.getRegistryName() == null ? ""
                : profession.getRegistryName().toString();
    }

    private static boolean isSupportedProfession(VillagerProfession profession) {
        if (profession == null || profession.getClass() != VillagerProfession.class) return false;
        switch (professionId(profession)) {
            case "minecraft:farmer":
            case "minecraft:librarian":
            case "minecraft:priest":
            case "minecraft:smith":
            case "minecraft:butcher":
            case "minecraft:nitwit":
            case "farmers_future_delight:mason": return true;
            default: return false;
        }
    }

    private static int chooseCareer(VillagerProfession profession, int career, boolean unassigned,
                                    Random random) {
        if (career > 0 || unassigned || !isSupportedProfession(profession)
                || "minecraft:nitwit".equals(professionId(profession)) || careers(profession).isEmpty()) return career;
        return profession.getRandomCareer(random) + 1;
    }

    private static List<VillagerCareer> careers(VillagerProfession profession) {
        if (profession == null) return Collections.emptyList();
        return ObfuscationReflectionHelper.getPrivateValue(VillagerProfession.class, profession, "careers");
    }

    private static String careerName(VillagerProfession profession, int career) {
        List<VillagerCareer> careers = careers(profession);
        return career <= 0 || career > careers.size() ? "" : careers.get(career - 1).getName();
    }

    private static String modernProfession(VillagerProfession profession, int index, String career,
                                           boolean unassigned) {
        if (!isSupportedProfession(profession)) return null;
        if (unassigned) return "none";
        if ("minecraft:nitwit".equals(professionId(profession)) && index == 0) return "nitwit";
        List<VillagerCareer> careers = careers(profession);
        if (index <= 0 || index > careers.size() || !career.equals(careers.get(index - 1).getName())
                || careers.get(index - 1).getClass() != VillagerCareer.class) return null;
        String key = professionId(profession) + "/" + career;
        switch (key) {
            case "minecraft:farmer/farmer": return index == 1 ? "farmer" : null;
            case "minecraft:farmer/fisherman": return index == 2 ? "fisherman" : null;
            case "minecraft:farmer/shepherd": return index == 3 ? "shepherd" : null;
            case "minecraft:farmer/fletcher": return index == 4 ? "fletcher" : null;
            case "minecraft:librarian/librarian": return index == 1 ? "librarian" : null;
            case "minecraft:librarian/cartographer": return index == 2 ? "cartographer" : null;
            case "minecraft:priest/cleric": return index == 1 ? "cleric" : null;
            case "minecraft:smith/armor": return index == 1 ? "armorer" : null;
            case "minecraft:smith/weapon": return index == 2 ? "weaponsmith" : null;
            case "minecraft:smith/tool": return index == 3 ? "toolsmith" : null;
            case "minecraft:butcher/butcher": return index == 1 ? "butcher" : null;
            case "minecraft:butcher/leather": return index == 2 ? "leatherworker" : null;
            case "farmers_future_delight:mason/mason": return index == 1 ? "mason" : null;
            case "minecraft:nitwit/nitwit": return index == 1 ? "nitwit" : null;
            default: return null;
        }
    }
}
