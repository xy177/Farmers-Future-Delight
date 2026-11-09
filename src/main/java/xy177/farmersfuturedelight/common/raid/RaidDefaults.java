package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.BossBar;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Participant;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Timing;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Trigger;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Wave;

final class RaidDefaults {
    static final ResourceLocation ID = new ResourceLocation("farmers_future_delight:raid");
    static final ResourceLocation OMEN = new ResourceLocation("farmers_future_delight:raid_omen");
    private static final ResourceLocation[] TYPES = {
            new ResourceLocation("minecraft:vindication_illager"),
            new ResourceLocation("minecraft:evocation_illager"),
            new ResourceLocation("farmers_future_delight:pillager"),
            new ResourceLocation("minecraft:witch"),
            new ResourceLocation("farmers_future_delight:ravager")
    };
    private static final int[][] COUNTS = {
            {0, 0, 2, 0, 1, 4, 2, 5},
            {0, 0, 0, 0, 0, 1, 1, 2},
            {0, 4, 3, 3, 4, 4, 4, 2},
            {0, 0, 0, 0, 3, 0, 0, 1},
            {0, 0, 0, 1, 0, 1, 0, 2}
    };
    private static RaidEventDefinition definition;
    private static boolean initialized;

    private RaidDefaults() { }

    static synchronized RaidEventDefinition definition() {
        if (initialized) return definition;
        initialized = true;
        ResourceLocation pillager = resolve(FFDEntities.isLocalPillagerEnabled(), FFDConfig.pillagerMode,
                FFDEntities.PILLAGER_ID, FFDCompat.Feature.PILLAGER, "pillager");
        ResourceLocation ravager = resolve(FFDEntities.isLocalRavagerEnabled(), FFDConfig.ravagerMode,
                FFDEntities.RAVAGER_ID, FFDCompat.Feature.RAVAGER, "ravager");
        if (pillager == null || ravager == null) {
            FFDRaidManager.LOGGER.warn("Built-in raid disabled: no enabled registered pillager or ravager; external events remain available");
            return null;
        }
        TYPES[2] = pillager;
        TYPES[4] = ravager;
        definition = create();
        return definition;
    }

    private static ResourceLocation resolve(boolean local, FFDConfig.FeatureMode mode, ResourceLocation localId,
                                            FFDCompat.Feature feature, String path) {
        EntityEntry entry = local ? ForgeRegistries.ENTITIES.getValue(localId)
                : mode == FFDConfig.FeatureMode.AUTO ? FFDCompat.getExternalEntityEntry(feature, path) : null;
        return entry != null && entry.getEntityClass() != null && EntityLiving.class.isAssignableFrom(entry.getEntityClass())
                ? entry.getRegistryName() : null;
    }

    private static RaidEventDefinition create() {
        RaidEventDefinition.Builder builder = RaidEventDefinition.builder(ID)
                .trigger(new Trigger(OMEN, Collections.emptyList(), Collections.emptyList(), true, 1, 5))
                .radius(96.0D).timing(new Timing(300, 300, 40, 600, 48000))
                .bossBar(new BossBar(true, "event.minecraft.raid", "event.minecraft.raid.victory.full",
                        "event.minecraft.raid.defeat.full", BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10))
                .bonusOmenLevel(2);
        for (EnumDifficulty difficulty : new EnumDifficulty[] {EnumDifficulty.EASY, EnumDifficulty.NORMAL, EnumDifficulty.HARD}) {
            int count = waveCount(difficulty);
            List<Wave> waves = new ArrayList<>();
            for (int index = 1; index <= count; index++) waves.add(wave(difficulty, index, false, null));
            builder.waves(difficulty, waves);
            builder.bonusWaves(difficulty, Collections.singletonList(wave(difficulty, count + 1, true, null)));
        }
        return builder.build();
    }

    static int waveCount(EnumDifficulty difficulty) {
        return difficulty == EnumDifficulty.EASY ? 3 : difficulty == EnumDifficulty.HARD ? 7 : 5;
    }

    static boolean usesDefaultWaves(RaidEventDefinition candidate, EnumDifficulty difficulty) {
        if (!ID.equals(candidate.getId())) return false;
        RaidEventDefinition defaults = definition();
        return defaults != null && sameWaves(candidate.getWaves(difficulty), defaults.getWaves(difficulty))
                && sameWaves(candidate.getBonusWaves(difficulty), defaults.getBonusWaves(difficulty));
    }

    private static boolean sameWaves(List<Wave> first, List<Wave> second) {
        if (first.size() != second.size()) return false;
        for (int waveIndex = 0; waveIndex < first.size(); waveIndex++) {
            List<Participant> firstParticipants = first.get(waveIndex).getParticipants();
            List<Participant> secondParticipants = second.get(waveIndex).getParticipants();
            if (!sameParticipants(firstParticipants, secondParticipants)) return false;
        }
        return true;
    }

    private static boolean sameParticipants(List<Participant> first, List<Participant> second) {
        if (first.size() != second.size()) return false;
        for (int participantIndex = 0; participantIndex < first.size(); participantIndex++) {
            Participant left = first.get(participantIndex);
            Participant right = second.get(participantIndex);
            if (!left.getEntity().equals(right.getEntity()) || left.getCount() != right.getCount()
                    || left.isLeader() != right.isLeader() || !left.getNbt().equals(right.getNbt())
                    || !sameParticipants(left.getPassengers(), right.getPassengers())) return false;
        }
        return true;
    }

    static boolean isPillager(EntityLiving entity) { return TYPES[2].equals(net.minecraft.entity.EntityList.getKey(entity)); }

    static Wave wave(EnumDifficulty difficulty, int index, boolean bonus, Random random) {
        List<Participant> participants = new ArrayList<>();
        boolean leader = false;
        for (int type = 0; type < TYPES.length; type++) {
            int count = COUNTS[type][bonus ? waveCount(difficulty) : index];
            if (random != null) count += additional(type, difficulty, index, bonus, random);
            for (int entity = 0; entity < count; entity++) {
                List<Participant> passengers = Collections.emptyList();
                if (type == 4 && (index == 5 || index >= 7)) {
                    int rider = index == 5 ? 2 : entity == 0 ? 1 : 0;
                    passengers = Collections.singletonList(participant(TYPES[rider], false, Collections.emptyList()));
                }
                boolean captain = !leader && type < 3;
                participants.add(participant(TYPES[type], captain, passengers));
                leader |= captain;
            }
        }
        return new Wave(participants);
    }

    private static Participant participant(ResourceLocation type, boolean leader, List<Participant> passengers) {
        return new Participant(type, 1, leader, new NBTTagCompound(), passengers);
    }

    private static int additional(int type, EnumDifficulty difficulty, int wave, boolean bonus, Random random) {
        if (type == 3) return difficulty != EnumDifficulty.EASY && wave > 2 && wave != 4 ? random.nextInt(2) : 0;
        if (type == 1) return 0;
        int maximum;
        if (type == 4) maximum = difficulty != EnumDifficulty.EASY && bonus ? 1 : 0;
        else maximum = difficulty == EnumDifficulty.EASY ? random.nextInt(2) : difficulty == EnumDifficulty.HARD ? 2 : 1;
        return maximum == 0 ? 0 : random.nextInt(maximum + 1);
    }
}
