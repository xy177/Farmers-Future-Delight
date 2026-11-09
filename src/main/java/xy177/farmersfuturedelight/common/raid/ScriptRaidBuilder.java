package xy177.farmersfuturedelight.common.raid;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.entity.IEntityLiving;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.EnumDifficulty;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;
import xy177.farmersfuturedelight.api.raid.RaidEventRegistry;
import xy177.farmersfuturedelight.api.raid.RaidScriptRules;

@ZenRegister
@ZenClass("mods.farmersfuturedelight.RaidBuilder")
public final class ScriptRaidBuilder {
    private final ResourceLocation id;
    private final boolean existing;
    private final List<Consumer<NBTTagCompound>> edits = new ArrayList<>();
    private boolean registered;

    ScriptRaidBuilder(String id, boolean existing) {
        this.id = RaidEventDefinition.identifier(id);
        this.existing = existing;
    }

    private ScriptRaidBuilder edit(Consumer<NBTTagCompound> operation) {
        if (registered) throw new IllegalStateException("Raid builder has already been registered");
        edits.add(operation);
        return this;
    }

    private static NBTTagCompound wave(NBTTagCompound data, int number) {
        if (number <= 0) throw new IllegalArgumentException("Wave number must be positive");
        NBTTagList waves = data.getTagList("Waves", 10);
        for (int index = 0; index < waves.tagCount(); index++) {
            if (waves.getCompoundTagAt(index).getInteger("Number") == number) return waves.getCompoundTagAt(index);
        }
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("Number", number);
        waves.appendTag(result);
        data.setTag("Waves", waves);
        return result;
    }

    @ZenMethod
    public ScriptRaidBuilder setName(String translationKey) {
        String key = RaidScriptRules.nameText(translationKey);
        return edit(data -> data.setString("NameKey", key));
    }

    @ZenMethod
    public ScriptRaidBuilder setName(String language, String text) {
        String code = RaidScriptRules.languageCode(language);
        String name = RaidScriptRules.nameText(text);
        return edit(data -> {
            NBTTagCompound names = data.getCompoundTag("Names");
            names.setString(code, name);
            data.setTag("Names", names);
        });
    }

    @ZenMethod
    public ScriptRaidBuilder setRestTime(int number, int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("Rest time must be nonnegative");
        return edit(data -> wave(data, number).setInteger("Delay", ticks));
    }

    @ZenMethod
    public ScriptRaidBuilder setWaveMobCount(int number, int count) { return setWaveMobCount(number, count, count); }

    @ZenMethod
    public ScriptRaidBuilder setWaveMobCount(int number, int minimum, int maximum) {
        if (minimum < 0 || maximum < minimum || maximum > 1024) throw new IllegalArgumentException("Expected a wave count range in 0..1024");
        return edit(data -> {
            NBTTagCompound rule = wave(data, number);
            rule.setInteger("Min", minimum);
            rule.setInteger("Max", maximum);
        });
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, String entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        ResourceLocation type = RaidEventDefinition.identifier(entity);
        if (maximum != null && (maximum < 0 || maximum > 1024) || minimum != null && (minimum < 0 || minimum > 1024)
                || maximum != null && minimum != null && minimum > maximum || !Double.isFinite(weight) || weight <= 0 || weight > 1000000000) {
            throw new IllegalArgumentException("Invalid mob bounds or weight");
        }
        return edit(data -> {
            NBTTagCompound rule = wave(data, number);
            NBTTagList pools = rule.getTagList("Pools", 10);
            for (int index = pools.tagCount() - 1; index >= 0; index--) {
                if (pools.getCompoundTagAt(index).getString("Entity").equals(type.toString())) pools.removeTag(index);
            }
            NBTTagCompound pool = new NBTTagCompound();
            pool.setString("Entity", type.toString());
            if (maximum != null) pool.setInteger("Max", maximum);
            if (minimum != null) pool.setInteger("Min", minimum);
            pool.setDouble("Weight", weight);
            pool.setBoolean("Adjust", adjust);
            pools.appendTag(pool);
            rule.setTag("Pools", pools);
        });
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, String entity, double weight, boolean adjust) {
        return addWaveMob(number, entity, null, null, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, IEntityDefinition entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        return addWaveMob(number, entity.getId(), maximum, minimum, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, IEntityLiving entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        return addWaveMob(number, entity.getDefinition().getId(), maximum, minimum, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, IEntityDefinition entity, double weight, boolean adjust) {
        return addWaveMob(number, entity, null, null, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder addWaveMob(int number, IEntityLiving entity, double weight, boolean adjust) {
        return addWaveMob(number, entity, null, null, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder setWaveMob(int number, String entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        edit(data -> {
            wave(data, number).setTag("Pools", new NBTTagList());
            wave(data, number).setBoolean("ReplacePool", true);
        });
        return addWaveMob(number, entity, maximum, minimum, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder setWaveMob(int number, IEntityDefinition entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        return setWaveMob(number, entity.getId(), maximum, minimum, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder setWaveMob(int number, IEntityLiving entity, Integer maximum, Integer minimum, double weight, boolean adjust) {
        return setWaveMob(number, entity.getDefinition().getId(), maximum, minimum, weight, adjust);
    }

    @ZenMethod
    public ScriptRaidBuilder setLoop(int number, int cycles) {
        if (cycles < 0) throw new IllegalArgumentException("Cycle count must be nonnegative");
        return edit(data -> wave(data, number).setInteger("Cycles", cycles));
    }

    @ZenMethod
    public ScriptRaidBuilder setRange(String shape, double size) { return setRange(shape, size, 96); }

    @ZenMethod
    public ScriptRaidBuilder setRange(String shape, double size, double height) {
        if (!"circle".equals(shape) && !"square".equals(shape)) throw new IllegalArgumentException("Shape must be circle or square");
        if (!Double.isFinite(size) || size <= 0 || size > 1024 || !Double.isFinite(height) || height <= 0 || height > 2048) {
            throw new IllegalArgumentException("Invalid raid size or height");
        }
        return edit(data -> {
            data.setString("Shape", shape);
            data.setDouble("Size", size);
            data.setDouble("Height", height);
        });
    }

    @ZenMethod
    public ScriptRaidBuilder setCenter(IBlockPos position) {
        long location = CraftTweakerMC.getBlockPos(position).toLong();
        return edit(data -> {
            data.setLong("Center", location);
            data.setBoolean("Follow", false);
            data.removeTag("Dimension");
        });
    }

    @ZenMethod
    public ScriptRaidBuilder setCenter(IBlockPos position, int dimension) {
        setCenter(position);
        return edit(data -> data.setInteger("Dimension", dimension));
    }

    @ZenMethod
    public ScriptRaidBuilder setCenter(String source) {
        if (!"triggerer".equals(source)) throw new IllegalArgumentException("Center source must be triggerer");
        return edit(data -> {
            data.setBoolean("Follow", true);
            data.removeTag("Center");
            data.removeTag("Dimension");
        });
    }

    private ScriptRaidBuilder target(String list, String selector) {
        if (!selector.startsWith("uuid:")) RaidEventDefinition.identifier(selector);
        return edit(data -> {
            NBTTagList targets = data.getTagList(list, 8);
            boolean found = false;
            for (int index = 0; index < targets.tagCount(); index++) if (targets.getStringTagAt(index).equals(selector)) found = true;
            if (!found) targets.appendTag(new NBTTagString(selector));
            data.setTag(list, targets);
        });
    }

    @ZenMethod
    public ScriptRaidBuilder setRaidTarget(String entity) { return target("Targets", entity); }
    @ZenMethod
    public ScriptRaidBuilder setRaidTarget(IEntityDefinition entity) { return setRaidTarget(entity.getId()); }
    @ZenMethod
    public ScriptRaidBuilder setRaidTarget(IEntityLiving entity) { return target("Targets", "uuid:" + CraftTweakerMC.getEntity(entity).getUniqueID()); }
    @ZenMethod
    public ScriptRaidBuilder setHostileTarget(String entity) { return target("Hostiles", entity); }
    @ZenMethod
    public ScriptRaidBuilder setHostileTarget(IEntityDefinition entity) { return setHostileTarget(entity.getId()); }
    @ZenMethod
    public ScriptRaidBuilder setHostileTarget(IEntityLiving entity) { return target("Hostiles", "uuid:" + CraftTweakerMC.getEntity(entity).getUniqueID()); }

    @ZenMethod
    public ScriptRaidBuilder setPlayerAttitude(String attitude) {
        if (!"normal".equals(attitude) && !"attack".equals(attitude) && !"hostile".equals(attitude)) {
            throw new IllegalArgumentException("Attitude must be normal, attack or hostile");
        }
        return edit(data -> data.setString("Players", attitude));
    }

    @ZenMethod
    public void register() {
        if (registered) throw new IllegalStateException("Raid builder has already been registered");
        registered = true;
        List<Consumer<NBTTagCompound>> operations = new ArrayList<>(edits);
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                RaidEventConfiguration.applyScriptAction(() -> {
                    try {
                        publish(operations);
                    } catch (RuntimeException exception) {
                        RaidScriptValidation.record(id.toString(), exception.getMessage());
                        CraftTweakerAPI.logError("FFD raid " + id + ": " + exception.getMessage());
                    }
                });
            }

            @Override
            public String describe() { return (existing ? "Edit" : "Create") + " raid event " + id; }
        });
    }

    private void publish(List<Consumer<NBTTagCompound>> operations) {
        RaidEventDefinition previous = RaidEventRegistry.get(id);
        if (existing && previous == null) throw new IllegalArgumentException("Unknown raid event " + id);
        if (!existing && previous != null) throw new IllegalArgumentException("Raid event already exists; use Raids.get: " + id);
        NBTTagCompound data = existing && previous.getScriptRules() != null ? previous.getScriptRules().write() : new NBTTagCompound();
        if (!existing) {
            data.setBoolean("ReplaceWaves", true);
            data.setBoolean("Follow", true);
            data.setString("Shape", "circle");
            data.setDouble("Size", 96);
            data.setDouble("Height", 96);
        }
        for (Consumer<NBTTagCompound> operation : operations) operation.accept(data);
        RaidScriptRules rules = new RaidScriptRules(data);
        RaidEventDefinition.Builder builder;
        if (existing) builder = previous.toBuilder();
        else {
            builder = RaidEventDefinition.builder(id)
                    .trigger(new RaidEventDefinition.Trigger(new ResourceLocation("farmers_future_delight:manual"), Collections.emptyList(), Collections.emptyList(), false, 0, 255))
                    .radius(48).timing(new RaidEventDefinition.Timing(0, 0, 0, 200, Integer.MAX_VALUE))
                    .bossBar(new RaidEventDefinition.BossBar(true, id.toString(), id + ".victory", id + ".defeat", BossInfo.Color.RED, BossInfo.Overlay.PROGRESS));
            List<RaidEventDefinition.Wave> templates = new ArrayList<>();
            for (RaidScriptRules.WaveRule rule : rules.waves().values()) {
                if (!rule.pools.isEmpty()) templates.add(new RaidEventDefinition.Wave(Collections.singletonList(
                        new RaidEventDefinition.Participant(rule.pools.get(0).entity, 1, false, new NBTTagCompound(), Collections.emptyList()))));
            }
            if (templates.isEmpty()) throw new IllegalArgumentException("At least one wave needs mobs");
            for (EnumDifficulty difficulty : new EnumDifficulty[] {EnumDifficulty.EASY, EnumDifficulty.NORMAL, EnumDifficulty.HARD}) builder.waves(difficulty, templates);
        }
        builder.scriptRules(rules);
        if (rules.hasGeometry()) builder.radius(rules.size() / 2);
        RaidEventDefinition definition = builder.build();
        if (existing) RaidEventRegistry.registerData(definition);
        else RaidEventRegistry.register(definition);
    }
}
