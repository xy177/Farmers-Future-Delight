package xy177.farmersfuturedelight.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;

final class FFDStructureProcessors {
    private FFDStructureProcessors() {
    }

    interface Processor {
        Template.BlockInfo apply(World world, BlockPos position, Template.BlockInfo block);
    }

    static List<Processor> read(WorldServer world, JsonElement definition) {
        List<Processor> result = new ArrayList<>();
        if (definition == null) return result;
        JsonArray processors;
        if (definition.isJsonArray()) processors = definition.getAsJsonArray();
        else {
            JsonObject object;
            if (definition.isJsonPrimitive()) {
                ResourceLocation id = new ResourceLocation(definition.getAsString());
                if ("minecraft:empty".equals(id.toString())) return result;
                String path = id.getResourcePath();
                if (path.startsWith("/") || path.contains("..") || path.contains("\\")
                        || ".".equals(id.getResourceDomain()) || "..".equals(id.getResourceDomain())) {
                    throw new IllegalArgumentException("Invalid processor list " + id);
                }
                File root = new File(world.getSaveHandler().getWorldDirectory(), "data/ffd_processor_lists");
                File file = new File(root, id.getResourceDomain() + "/" + path + ".json");
                try (InputStream input = file.isFile() ? new FileInputStream(file)
                        : FFDStructureProcessors.class.getResourceAsStream("/assets/" + id.getResourceDomain()
                                + "/processor_lists/" + path + ".json")) {
                    if (input == null) throw new IllegalArgumentException("Unknown processor list " + id);
                    object = FFDStructureResources.readJson(input, "Oversized processor list " + id);
                } catch (IOException exception) {
                    throw new IllegalArgumentException("Cannot load processor list " + id, exception);
                }
            } else object = definition.getAsJsonObject();
            processors = object.getAsJsonArray("processors");
        }
        if (processors == null || processors.size() > 128) throw new IllegalArgumentException("Invalid processor count");
        for (JsonElement entry : processors) {
            JsonObject processor = entry.getAsJsonObject();
            String type = processor.get("processor_type").getAsString();
            if ("minecraft:block_rot".equals(type)) {
                float integrity = processor.get("integrity").getAsFloat();
                if (!Float.isFinite(integrity) || integrity < 0.0F || integrity > 1.0F
                        || processor.has("rottable_blocks")) throw new IllegalArgumentException("Unsupported block rot definition");
                result.add((level, position, block) -> random(position).nextFloat() <= integrity ? block : null);
            } else if ("minecraft:rule".equals(type)) {
                JsonArray rules = processor.getAsJsonArray("rules");
                if (rules == null || rules.size() > 1024) throw new IllegalArgumentException("Invalid structure rules");
                List<Rule> parsed = new ArrayList<>();
                for (JsonElement rule : rules) parsed.add(new Rule(rule.getAsJsonObject()));
                result.add((level, position, block) -> {
                    Random random = random(position);
                    IBlockState location = level.getBlockState(position);
                    for (Rule rule : parsed) {
                        if (rule.input.test(block.blockState, random) && rule.location.test(location, random)
                                && rule.position.test(block.pos, random)) {
                            return new Template.BlockInfo(block.pos, rule.output,
                                    rule.output.getBlock() == block.blockState.getBlock() ? block.tileentityData
                                            : FFDStructureBlockStates.tileData(level, rule.output, block.tileentityData));
                        }
                    }
                    return block;
                });
            } else throw new IllegalArgumentException("Unsupported structure processor " + type);
        }
        return result;
    }

    private static Random random(BlockPos position) {
        long seed = (long) (position.getX() * 3129871) ^ (long) position.getZ() * 116129781L ^ position.getY();
        seed = seed * seed * 42317861L + seed * 11L;
        return new Random(seed >> 16);
    }

    private static BiPredicate<IBlockState, Random> predicate(JsonObject definition) {
        String type = definition.get("predicate_type").getAsString();
        if ("minecraft:always_true".equals(type)) return (state, random) -> true;
        if ("minecraft:tag_match".equals(type)) {
            if (!"minecraft:doors".equals(definition.get("tag").getAsString())) {
                throw new IllegalArgumentException("Unsupported structure block tag " + definition.get("tag"));
            }
            return (state, random) -> state.getBlock() instanceof BlockDoor;
        }
        if (!("minecraft:block_match".equals(type) || "minecraft:random_block_match".equals(type)
                || "minecraft:blockstate_match".equals(type))) throw new IllegalArgumentException("Unsupported structure predicate " + type);
        String encoded = definition.get("minecraft:blockstate_match".equals(type) ? "block_state" : "block").getAsString();
        IBlockState expected = FFDJigsawPlacement.parseFinalState(encoded);
        java.util.Set<IProperty<?>> properties = FFDStructureBlockStates.matchedProperties(encoded, expected);
        boolean randomized = "minecraft:random_block_match".equals(type);
        float chance = randomized ? definition.get("probability").getAsFloat() : 1.0F;
        if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) throw new IllegalArgumentException("Invalid structure predicate probability");
        return (state, random) -> {
            if (state.getBlock() != expected.getBlock()) {
                if (!(state.getBlock() == net.minecraft.init.Blocks.FLOWING_WATER
                        && expected.getBlock() == net.minecraft.init.Blocks.WATER)) return false;
            }
            for (IProperty<?> property : properties) {
                if (!state.getValue(property).equals(expected.getValue(property))) return false;
            }
            return !randomized || random.nextFloat() < chance;
        };
    }

    private static final class Rule {
        private final BiPredicate<IBlockState, Random> input;
        private final BiPredicate<IBlockState, Random> location;
        private final BiPredicate<BlockPos, Random> position;
        private final IBlockState output;

        private Rule(JsonObject definition) {
            if (definition.has("block_entity_modifier")) {
                throw new IllegalArgumentException("Unsupported structure rule extension");
            }
            input = predicate(definition.getAsJsonObject("input_predicate"));
            location = predicate(definition.getAsJsonObject("location_predicate"));
            position = positionPredicate(definition.getAsJsonObject("position_predicate"));
            output = FFDJigsawPlacement.parseFinalState(definition.get("output_state").getAsString());
        }
    }

    private static BiPredicate<BlockPos, Random> positionPredicate(JsonObject definition) {
        if (definition == null || "minecraft:always_true".equals(definition.get("predicate_type").getAsString())) {
            return (position, random) -> true;
        }
        if (!"minecraft:axis_aligned_linear_pos".equals(definition.get("predicate_type").getAsString())) {
            throw new IllegalArgumentException("Unsupported structure position predicate");
        }
        net.minecraft.util.EnumFacing.Axis axis = definition.has("axis")
                ? net.minecraft.util.EnumFacing.Axis.byName(definition.get("axis").getAsString()) : net.minecraft.util.EnumFacing.Axis.Y;
        int minimum = definition.has("min_dist") ? definition.get("min_dist").getAsInt() : 0;
        int maximum = definition.has("max_dist") ? definition.get("max_dist").getAsInt() : 0;
        float low = definition.has("min_chance") ? definition.get("min_chance").getAsFloat() : 0.0F;
        float high = definition.has("max_chance") ? definition.get("max_chance").getAsFloat() : 0.0F;
        if (axis == null || minimum >= maximum || !Float.isFinite(low) || !Float.isFinite(high)
                || low < 0 || high < 0 || low > 1 || high > 1) {
            throw new IllegalArgumentException("Invalid structure position predicate");
        }
        return (position, random) -> {
            int distance = Math.abs(axis == net.minecraft.util.EnumFacing.Axis.X ? position.getX()
                    : axis == net.minecraft.util.EnumFacing.Axis.Z ? position.getZ() : position.getY());
            float progress = Math.max(0.0F, Math.min(1.0F, (float) (distance - minimum) / (maximum - minimum)));
            return random.nextFloat() <= low + (high - low) * progress;
        };
    }
}
