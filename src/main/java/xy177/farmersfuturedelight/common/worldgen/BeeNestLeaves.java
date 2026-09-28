package xy177.farmersfuturedelight.common.worldgen;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class BeeNestLeaves {
    private static String[] loadedEntries = new String[0];
    private static Set<IBlockState> states = Collections.emptySet();

    private BeeNestLeaves() {
    }

    public static synchronized boolean matches(IBlockState state) {
        String[] configured = FFDConfig.beeNestAllowedLeaves;
        configured = configured == null ? new String[0] : configured;
        if (!Arrays.equals(loadedEntries, configured)) {
            Set<IBlockState> parsed = new HashSet<>();
            for (String entry : configured) {
                addStates(entry, parsed);
            }
            states = parsed;
            loadedEntries = configured.clone();
        }
        return states.contains(state);
    }

    private static void addStates(String configured, Set<IBlockState> parsed) {
        String entry = configured == null ? "" : configured.trim();
        if (entry.isEmpty()) {
            return;
        }
        try {
            String blockName = entry;
            String selector = "*";
            int bracket = entry.indexOf('[');
            int at = entry.lastIndexOf('@');
            if (bracket >= 0) {
                if (!entry.endsWith("]") || bracket == 0) {
                    throw new IllegalArgumentException("invalid property selector");
                }
                blockName = entry.substring(0, bracket).trim();
                selector = entry.substring(bracket + 1, entry.length() - 1).trim();
                if (selector.isEmpty() || selector.indexOf('=') < 1) {
                    throw new IllegalArgumentException("empty property selector");
                }
            } else if (at >= 0) {
                blockName = entry.substring(0, at).trim();
                int metadata = Integer.parseInt(entry.substring(at + 1).trim());
                if (metadata < 0 || metadata > 15) {
                    throw new IllegalArgumentException("metadata must be between 0 and 15");
                }
                selector = Integer.toString(metadata);
            }
            ResourceLocation id = new ResourceLocation(blockName);
            if (!ForgeRegistries.BLOCKS.containsKey(id)) {
                throw new IllegalArgumentException("block is not registered");
            }
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            Predicate<IBlockState> predicate = CommandBase.convertArgToBlockStatePredicate(block, selector);
            for (IBlockState candidate : block.getBlockState().getValidStates()) {
                if (predicate.apply(candidate)) {
                    parsed.add(candidate);
                }
            }
        } catch (IllegalArgumentException | InvalidBlockStateException ex) {
            LogManager.getLogger("FFD Bee Nest Leaves").warn(
                    "Ignoring invalid bee nest leaf selector '{}': {}", entry, ex.getMessage());
        }
    }
}
