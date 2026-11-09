package xy177.farmersfuturedelight.api.raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class RaidScriptRules {
    private final NBTTagCompound data;
    private final Map<Integer, WaveRule> waves;
    private final List<String> targets;
    private final List<String> hostiles;
    private final Map<String, String> names;

    public RaidScriptRules(NBTTagCompound source) {
        data = source.copy();
        if (data.hasKey("NameKey")) {
            if (!data.hasKey("NameKey", 8)) throw new IllegalArgumentException("Raid name key must be a string");
            nameText(data.getString("NameKey"));
        }
        if (data.hasKey("Names") && !data.hasKey("Names", 10)) throw new IllegalArgumentException("Raid names must be a compound");
        Map<String, String> localized = new TreeMap<>();
        NBTTagCompound entries = data.getCompoundTag("Names");
        for (String language : entries.getKeySet()) {
            if (!entries.hasKey(language, 8)) throw new IllegalArgumentException("Raid name must be a string");
            localized.put(languageCode(language), nameText(entries.getString(language)));
        }
        names = Collections.unmodifiableMap(localized);
        if (data.hasKey("Shape") && !"circle".equals(data.getString("Shape")) && !"square".equals(data.getString("Shape"))) {
            throw new IllegalArgumentException("Raid shape must be circle or square");
        }
        if (hasGeometry() && (!Double.isFinite(size()) || size() <= 0 || size() > 1024
                || !Double.isFinite(height()) || height() <= 0 || height() > 2048)) {
            throw new IllegalArgumentException("Raid size must be in (0,1024], height in (0,2048]");
        }
        if (!"normal".equals(playerAttitude()) && !"hostile".equals(playerAttitude()) && !"attack".equals(playerAttitude())) {
            throw new IllegalArgumentException("Player attitude must be normal, hostile or attack");
        }
        Map<Integer, WaveRule> copy = new TreeMap<>();
        NBTTagList list = data.getTagList("Waves", 10);
        for (int index = 0; index < list.tagCount(); index++) {
            WaveRule rule = new WaveRule(list.getCompoundTagAt(index));
            if (copy.put(rule.number, rule) != null) throw new IllegalArgumentException("Duplicate raid wave " + rule.number);
        }
        if (copy.size() > RaidEventDefinition.MAX_WAVES) throw new IllegalArgumentException("Too many raid waves");
        waves = Collections.unmodifiableMap(copy);
        targets = selectors("Targets");
        hostiles = selectors("Hostiles");
    }

    private List<String> selectors(String key) {
        List<String> result = new ArrayList<>();
        NBTTagList list = data.getTagList(key, 8);
        for (int index = 0; index < list.tagCount(); index++) {
            String value = list.getStringTagAt(index);
            if (value.startsWith("uuid:")) UUID.fromString(value.substring(5));
            else RaidEventDefinition.identifier(value);
            result.add(value);
        }
        return Collections.unmodifiableList(result);
    }

    public NBTTagCompound write() { return data.copy(); }

    public static String languageCode(String language) {
        if (language == null) throw new IllegalArgumentException("Expected a language code");
        String code = language.toLowerCase(Locale.ROOT).replace('-', '_');
        if (code.length() > 32 || !code.matches("[a-z0-9]+(?:_[a-z0-9]+)*")) {
            throw new IllegalArgumentException("Invalid language code " + language);
        }
        return code;
    }

    public static String nameText(String text) {
        if (text == null || text.trim().isEmpty() || text.length() > 256) {
            throw new IllegalArgumentException("Expected a nonempty raid name or translation key of at most 256 characters");
        }
        return text;
    }

    public ITextComponent displayName(String language, String fallbackKey) {
        String code = language == null ? "en_us" : language.toLowerCase(Locale.ROOT).replace('-', '_');
        String text = names.get(code);
        if (text != null) return new TextComponentString(text);
        if (data.hasKey("NameKey", 8)) return new TextComponentTranslation(data.getString("NameKey"));
        text = names.get("en_us");
        if (text == null && !names.isEmpty()) text = names.values().iterator().next();
        return text == null ? new TextComponentTranslation(fallbackKey) : new TextComponentString(text);
    }

    public boolean hasGeometry() { return data.hasKey("Shape"); }
    public boolean followsPlayer() { return data.getBoolean("Follow"); }
    public boolean replacesWaves() { return data.getBoolean("ReplaceWaves"); }
    public boolean hasFixedCenter() { return data.hasKey("Center"); }
    public BlockPos fixedCenter() { return BlockPos.fromLong(data.getLong("Center")); }
    public boolean hasDimension() { return data.hasKey("Dimension"); }
    public int dimension() { return data.getInteger("Dimension"); }
    public double size() { return data.hasKey("Size") ? data.getDouble("Size") : 96; }
    public double height() { return data.hasKey("Height") ? data.getDouble("Height") : 96; }
    public String playerAttitude() { return data.hasKey("Players") ? data.getString("Players") : "normal"; }
    public WaveRule rule(int number) { return waves.get(number); }
    public Map<Integer, WaveRule> waves() { return waves; }

    public List<Integer> numbers(RaidEventDefinition definition, EnumDifficulty difficulty, int omen) {
        Set<Integer> numbers = new java.util.TreeSet<>();
        if (!replacesWaves()) {
            int count = definition.getWaves(difficulty).size();
            if (definition.getBonusOmenLevel() > 0 && omen >= definition.getBonusOmenLevel()) count += definition.getBonusWaves(difficulty).size();
            for (int index = 1; index <= count; index++) numbers.add(index);
        }
        for (WaveRule rule : waves.values()) if (!rule.pools.isEmpty()) numbers.add(rule.number);
        return Collections.unmodifiableList(new ArrayList<>(numbers));
    }

    public boolean contains(BlockPos center, BlockPos position, double legacyRadius) {
        if (!hasGeometry()) return center.distanceSq(position) < legacyRadius * legacyRadius;
        double horizontalX = (double) position.getX() - center.getX();
        double horizontalZ = (double) position.getZ() - center.getZ();
        double radius = size() / 2;
        return Math.abs((double) position.getY() - center.getY()) <= height()
                && ("square".equals(data.getString("Shape"))
                ? Math.abs(horizontalX) <= radius && Math.abs(horizontalZ) <= radius
                : horizontalX * horizontalX + horizontalZ * horizontalZ <= radius * radius);
    }

    public boolean hasTargets() { return !targets.isEmpty() || "attack".equals(playerAttitude()); }
    public boolean hasTargeting() { return hasTargets() || !hostiles.isEmpty() || "hostile".equals(playerAttitude()); }
    public boolean isTarget(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && "attack".equals(playerAttitude()) || matches(targets, entity);
    }
    public boolean isHostile(EntityLivingBase entity) {
        return isTarget(entity) || entity instanceof EntityPlayer && "hostile".equals(playerAttitude()) || matches(hostiles, entity);
    }

    private static boolean matches(List<String> values, EntityLivingBase entity) {
        ResourceLocation type = EntityList.getKey(entity);
        for (String value : values) {
            if (value.equals("uuid:" + entity.getUniqueID()) || type != null && value.equals(type.toString())) return true;
        }
        return false;
    }

    public void validateReferences(RaidEventDefinition definition) {
        for (WaveRule rule : waves.values()) for (Pool pool : rule.pools) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(pool.entity);
            if (entry == null || entry.getEntityClass() == null || !net.minecraft.entity.EntityLiving.class.isAssignableFrom(entry.getEntityClass())) {
                throw new IllegalArgumentException("Expected registered living mob " + pool.entity);
            }
        }
        for (String selector : concat(targets, hostiles)) {
            if (!selector.startsWith("uuid:") && !ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(selector))) {
                throw new IllegalArgumentException("Unknown raid target " + selector);
            }
        }
        for (WaveRule rule : waves.values()) {
            boolean found = false;
            for (EnumDifficulty difficulty : new EnumDifficulty[] {EnumDifficulty.EASY, EnumDifficulty.NORMAL, EnumDifficulty.HARD}) {
                if (numbers(definition, difficulty, 255).contains(rule.number)) found = true;
            }
            if (!found) throw new IllegalArgumentException("Undefined raid wave " + rule.number);
        }
        for (EnumDifficulty difficulty : definition.getWaves().keySet()) {
            if (numbers(definition, difficulty, 255).size() > RaidEventDefinition.MAX_WAVES) {
                throw new IllegalArgumentException("Too many combined raid waves");
            }
        }
    }

    private static List<String> concat(List<String> first, List<String> second) {
        List<String> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    public static final class WaveRule {
        public final int number;
        public final int delay;
        public final int cycles;
        public final int minimum;
        public final int maximum;
        public final List<Pool> pools;
        private final boolean replacePool;

        private WaveRule(NBTTagCompound value) {
            number = value.getInteger("Number");
            delay = value.hasKey("Delay") ? value.getInteger("Delay") : -1;
            cycles = value.hasKey("Cycles") ? value.getInteger("Cycles") : 1;
            minimum = value.hasKey("Min") ? value.getInteger("Min") : -1;
            maximum = value.hasKey("Max") ? value.getInteger("Max") : minimum;
            replacePool = value.getBoolean("ReplacePool");
            if (number <= 0 || delay < -1 || cycles < 0 || minimum < -1 || maximum < minimum || maximum > 1024) {
                throw new IllegalArgumentException("Invalid wave number, delay, cycles or count range");
            }
            List<Pool> entries = new ArrayList<>();
            Set<ResourceLocation> seen = new LinkedHashSet<>();
            NBTTagList list = value.getTagList("Pools", 10);
            long sumMin = 0;
            for (int index = 0; index < list.tagCount(); index++) {
                Pool entry = new Pool(list.getCompoundTagAt(index));
                if (!seen.add(entry.entity)) throw new IllegalArgumentException("Duplicate wave mob " + entry.entity);
                if (entry.adjust) sumMin += entry.minimum;
                entries.add(entry);
            }
            if (entries.size() > 1024 || sumMin > 1024) throw new IllegalArgumentException("Wave exceeds entity limit");
            pools = Collections.unmodifiableList(entries);
        }

        public List<RaidEventDefinition.Participant> draw(Random random, List<RaidEventDefinition.Participant> original, int budget) {
            List<Pool> entries = pools;
            if (entries.isEmpty() && minimum < 0) return original;
            if (!replacePool && !original.isEmpty()) {
                entries = new ArrayList<>();
                for (RaidEventDefinition.Participant participant : original) {
                    boolean overridden = false;
                    for (Pool configured : pools) if (configured.entity.equals(participant.getEntity())) overridden = true;
                    if (!overridden) entries.add(new Pool(participant));
                }
                entries.addAll(pools);
            }
            if (entries.isEmpty()) return Collections.emptyList();
            int count = minimum < 0 ? original.isEmpty() ? entries.size() : count(original)
                    : minimum + random.nextInt(maximum - minimum + 1);
            int[] quantities = new int[entries.size()];
            double[] weights = new double[entries.size()];
            for (int index = 0; index < weights.length; index++) weights[index] = entries.get(index).weight;
            for (int slot = 0; slot < count; slot++) {
                int selected = pick(random, weights);
                while (selected >= 0 && quantities[selected] >= entries.get(selected).maximum && entries.get(selected).adjust) {
                    weights[selected] = 0;
                    selected = pick(random, weights);
                }
                if (selected < 0) break;
                if (quantities[selected] < entries.get(selected).maximum) quantities[selected]++;
            }
            for (int index = 0; index < quantities.length; index++) {
                if (entries.get(index).adjust) quantities[index] = Math.max(quantities[index], entries.get(index).minimum);
            }
            List<RaidEventDefinition.Participant> result = new ArrayList<>();
            for (int index = 0; index < quantities.length; index++) {
                Pool entry = entries.get(index);
                for (int quantity = 0; quantity < quantities[index] && budget >= entry.cost; quantity++) {
                    result.add(entry.participant);
                    budget -= entry.cost;
                }
            }
            return result;
        }

        private static int count(List<RaidEventDefinition.Participant> participants) {
            int count = 0;
            for (RaidEventDefinition.Participant participant : participants) count += participant.getCount();
            return count;
        }

        private static int pick(Random random, double[] weights) {
            double total = 0;
            for (double weight : weights) total += weight;
            if (total <= 0) return -1;
            double value = random.nextDouble() * total;
            for (int index = 0; index < weights.length; index++) {
                value -= weights[index];
                if (value < 0) return index;
            }
            return -1;
        }
    }

    public static final class Pool {
        public final ResourceLocation entity;
        public final int minimum;
        public final int maximum;
        public final double weight;
        public final boolean adjust;
        private final RaidEventDefinition.Participant participant;
        private final int cost;

        private Pool(NBTTagCompound value) {
            entity = RaidEventDefinition.identifier(value.getString("Entity"));
            minimum = value.hasKey("Min") ? value.getInteger("Min") : 0;
            maximum = value.hasKey("Max") ? value.getInteger("Max") : 1024;
            weight = value.getDouble("Weight");
            adjust = value.getBoolean("Adjust");
            if (minimum < 0 || maximum < minimum || maximum > 1024 || !Double.isFinite(weight) || weight <= 0 || weight > 1000000000) {
                throw new IllegalArgumentException("Invalid mob bounds or weight");
            }
            participant = new RaidEventDefinition.Participant(entity, 1, false, new NBTTagCompound(), Collections.emptyList());
            cost = 1;
        }

        private Pool(RaidEventDefinition.Participant source) {
            entity = source.getEntity();
            minimum = 0;
            maximum = 1024;
            weight = source.getCount();
            adjust = true;
            participant = new RaidEventDefinition.Participant(entity, 1, false, source.getNbt(), source.getPassengers());
            cost = new RaidEventDefinition.Wave(Collections.singletonList(participant)).getEntityCount();
        }
    }
}
