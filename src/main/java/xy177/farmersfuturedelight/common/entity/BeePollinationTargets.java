package xy177.farmersfuturedelight.common.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class BeePollinationTargets {
    private static final Logger LOGGER = LogManager.getLogger("FFD Bee Pollination Targets");
    private static String[] loadedEntries = new String[0];
    private static List<Target> targets = Collections.emptyList();

    private BeePollinationTargets() {
    }

    public static boolean matches(IBlockState state) {
        reloadIfNeeded();
        for (Target target : targets) {
            if (target.matches(state)) {
                return true;
            }
        }
        return false;
    }

    private static void reloadIfNeeded() {
        String[] configured = FFDConfig.beeAdditionalPollinationTargets;
        configured = configured == null ? new String[0] : configured;
        if (Arrays.equals(loadedEntries, configured)) {
            return;
        }
        synchronized (BeePollinationTargets.class) {
            configured = FFDConfig.beeAdditionalPollinationTargets;
            configured = configured == null ? new String[0] : configured;
            if (Arrays.equals(loadedEntries, configured)) {
                return;
            }
            loadedEntries = configured.clone();
            List<Target> parsed = new ArrayList<>();
            for (String entry : loadedEntries) {
                Target target = parse(entry);
                if (target != null) {
                    parsed.add(target);
                }
            }
            targets = Collections.unmodifiableList(parsed);
        }
    }

    private static Target parse(String configured) {
        String entry = configured == null ? "" : configured.trim();
        if (entry.isEmpty()) {
            return null;
        }
        try {
            String blockName = entry;
            Integer metadata = null;
            Map<String, String> propertyValues = new LinkedHashMap<>();
            int bracket = entry.indexOf('[');
            if (bracket >= 0) {
                if (!entry.endsWith("]") || bracket == 0) {
                    throw new IllegalArgumentException("invalid property selector");
                }
                blockName = entry.substring(0, bracket).trim();
                String properties = entry.substring(bracket + 1, entry.length() - 1).trim();
                if (properties.isEmpty()) {
                    throw new IllegalArgumentException("empty property selector");
                }
                for (String propertyEntry : properties.split(",")) {
                    int equals = propertyEntry.indexOf('=');
                    if (equals <= 0 || equals == propertyEntry.length() - 1) {
                        throw new IllegalArgumentException("invalid property selector");
                    }
                    propertyValues.put(propertyEntry.substring(0, equals).trim(),
                            propertyEntry.substring(equals + 1).trim());
                }
            } else {
                int metadataSeparator = entry.lastIndexOf('@');
                if (metadataSeparator > entry.indexOf(':')) {
                    blockName = entry.substring(0, metadataSeparator).trim();
                    metadata = Integer.parseInt(entry.substring(metadataSeparator + 1).trim());
                    if (metadata < 0 || metadata > 15) {
                        throw new IllegalArgumentException("metadata must be between 0 and 15");
                    }
                }
            }
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block == null) {
                throw new IllegalArgumentException("block is not registered");
            }
            Map<IProperty<?>, Comparable<?>> expectedProperties = new LinkedHashMap<>();
            for (Map.Entry<String, String> propertyEntry : propertyValues.entrySet()) {
                IProperty<?> property = findProperty(block, propertyEntry.getKey());
                if (property == null) {
                    throw new IllegalArgumentException("unknown property " + propertyEntry.getKey());
                }
                Comparable<?> value = parsePropertyValue(property, propertyEntry.getValue());
                if (value == null) {
                    throw new IllegalArgumentException("invalid value " + propertyEntry.getValue()
                            + " for property " + propertyEntry.getKey());
                }
                expectedProperties.put(property, value);
            }
            return new Target(block, metadata, expectedProperties);
        } catch (RuntimeException ex) {
            LOGGER.warn("Ignoring invalid bee pollination target '{}': {}", entry, ex.getMessage());
            return null;
        }
    }

    private static IProperty<?> findProperty(Block block, String name) {
        for (IProperty<?> property : block.getBlockState().getProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Comparable<?> parsePropertyValue(IProperty<?> property, String value) {
        return (Comparable<?>) ((IProperty) property).parseValue(value).orNull();
    }

    private static final class Target {
        private final Block block;
        private final Integer metadata;
        private final Map<IProperty<?>, Comparable<?>> properties;

        private Target(Block block, Integer metadata,
                       Map<IProperty<?>, Comparable<?>> properties) {
            this.block = block;
            this.metadata = metadata;
            this.properties = properties;
        }

        private boolean matches(IBlockState state) {
            if (state.getBlock() != block) {
                return false;
            }
            if (metadata != null && block.getMetaFromState(state) != metadata) {
                return false;
            }
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                if (!entry.getValue().equals(state.getProperties().get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }
    }
}
