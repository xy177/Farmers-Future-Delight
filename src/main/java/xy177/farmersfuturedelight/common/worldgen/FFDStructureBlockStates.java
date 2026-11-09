package xy177.farmersfuturedelight.common.worldgen;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDStoneDecor;

final class FFDStructureBlockStates {
    private FFDStructureBlockStates() {
    }

    static IBlockState parse(String encoded) {
        int opening = encoded.indexOf('[');
        ResourceLocation id = materialId(new ResourceLocation(opening < 0 ? encoded : encoded.substring(0, opening)));
        Block local = FFDItems.localBlock(id);
        boolean registered = Block.REGISTRY.containsKey(id);
        Block source = registered ? Block.REGISTRY.getObject(id) : local;
        if (source == null) throw new IllegalArgumentException("Unknown final block " + id);
        IBlockState result = registered ? source.getDefaultState() : FFDItems.effectiveBlockState(source);
        if (result == null) result = fallback(source);
        if (result == null) throw new IllegalArgumentException("No structure material for " + id);
        if (opening < 0) return result;
        if (!encoded.endsWith("]")) throw new IllegalArgumentException("Invalid final state " + encoded);
        String contents = encoded.substring(opening + 1, encoded.length() - 1);
        if (contents.isEmpty()) return result;
        Set<String> assigned = new HashSet<>();
        for (String assignment : contents.split(",", -1)) {
            String[] pair = assignment.split("=", -1);
            if (pair.length != 2 || !assigned.add(pair[0])) throw new IllegalArgumentException(encoded);
            IProperty<?> original = property(source.getDefaultState(), pair[0]);
            if (original == null && source instanceof BlockTrapDoor && "powered".equals(pair[0])
                    && ("false".equals(pair[1]) || "true".equals(pair[1]))) continue;
            if (original == null) throw new IllegalArgumentException("Unknown property " + assignment);
            String value = propertyValue(source.getDefaultState(), pair[0], pair[1]);
            withProperty(source.getDefaultState(), original, value);
            if (source != result.getBlock() && "axis".equals(pair[0])
                    && source.getRegistryName().getResourcePath().endsWith("_wood")
                    && (result.getBlock() == Blocks.LOG || result.getBlock() == Blocks.LOG2)) continue;
            IProperty<?> target = property(result, pair[0]);
            if (target != null && !(source != result.getBlock() && "variant".equals(pair[0])
                    && "default".equals(pair[1]))) {
                value = propertyValue(result, pair[0], pair[1]);
                if (target.parseValue(value).isPresent()) result = withProperty(result, target, value);
                else if (registered) throw new IllegalArgumentException("Invalid property " + assignment);
            }
        }
        return result;
    }

    static IProperty<?> property(IBlockState state, String name) {
        IProperty<?> property = state.getBlock().getBlockState().getProperty(name);
        if (property == null && "signal_fire".equals(name)) property = state.getBlock().getBlockState().getProperty("signal");
        if (property == null && "variant".equals(name)
                && (state.getBlock() instanceof BlockSandStone || state.getBlock() instanceof BlockRedSandstone)) {
            property = state.getBlock().getBlockState().getProperty("type");
        }
        return property;
    }

    private static String propertyValue(IBlockState state, String property, String value) {
        if ("variant".equals(property)
                && (state.getBlock() instanceof BlockSandStone || state.getBlock() instanceof BlockRedSandstone)) {
            String material = state.getBlock() instanceof BlockRedSandstone ? "red_sandstone" : "sandstone";
            if ("smooth".equals(value) || "chiseled".equals(value)) return value + "_" + material;
            if ("default".equals(value)) return material;
        }
        return value;
    }

    private static ResourceLocation materialId(ResourceLocation id) {
        if (!"minecraft".equals(id.getResourceDomain()) || Block.REGISTRY.containsKey(id)) return id;
        switch (id.getResourcePath()) {
            case "oak_trapdoor": return new ResourceLocation("minecraft:trapdoor");
            case "oak_door": return new ResourceLocation("minecraft:wooden_door");
            case "oak_fence": return new ResourceLocation("minecraft:fence");
            case "magma_block": return new ResourceLocation("minecraft:magma");
            default:
                ResourceLocation local = new ResourceLocation("farmers_future_delight", id.getResourcePath());
                return FFDItems.localBlock(local) == null ? id : local;
        }
    }

    static NBTTagCompound tileData(World world, IBlockState state, NBTTagCompound data) {
        if (data == null) return null;
        TileEntity tile = state.getBlock().createTileEntity(world, state);
        if (tile == null) return null;
        NBTTagCompound result = data.copy();
        result.setString("id", tile.writeToNBT(new NBTTagCompound()).getString("id"));
        tile.readFromNBT(result);
        if (data.hasKey("Items", 9)) {
            NBTTagList items = data.getTagList("Items", 10);
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int index = 0; index < items.tagCount(); index++) {
                NBTTagCompound entry = items.getCompoundTagAt(index);
                int slot = entry.getByte("Slot") & 255;
                ItemStack stack = new ItemStack(entry);
                if (tile instanceof IInventory && slot < ((IInventory) tile).getSizeInventory()) {
                    ((IInventory) tile).setInventorySlotContents(slot, stack);
                } else if (handler instanceof IItemHandlerModifiable && slot < handler.getSlots()) {
                    ((IItemHandlerModifiable) handler).setStackInSlot(slot, stack);
                }
            }
        }
        return tile.writeToNBT(result);
    }

    static Set<IProperty<?>> matchedProperties(String encoded, IBlockState state) {
        Set<IProperty<?>> properties = new HashSet<>();
        int opening = encoded.indexOf('[');
        ResourceLocation id = new ResourceLocation(opening < 0 ? encoded : encoded.substring(0, opening));
        if (!id.equals(state.getBlock().getRegistryName())) {
            for (String name : new String[] {"variant", "type", "color"}) {
                IProperty<?> property = property(state, name);
                if (property != null) properties.add(property);
            }
        }
        if (opening >= 0) {
            for (String assignment : encoded.substring(opening + 1, encoded.length() - 1).split(",")) {
                if (assignment.isEmpty()) continue;
                IProperty<?> property = property(state, assignment.split("=", 2)[0]);
                if (property != null) properties.add(property);
            }
        }
        return properties;
    }

    private static <Value extends Comparable<Value>> IBlockState withProperty(
            IBlockState state, IProperty<Value> property, String value) {
        com.google.common.base.Optional<Value> parsed = property.parseValue(value);
        if (!parsed.isPresent()) throw new IllegalArgumentException("Invalid property " + property.getName() + "=" + value);
        return state.withProperty(property, parsed.get());
    }

    private static IBlockState fallback(Block block) {
        if (block == FFDStoneDecor.SMOOTH_STONE) return Blocks.DOUBLE_STONE_SLAB.getStateFromMeta(8);
        if (block == FFDStoneDecor.SMOOTH_SANDSTONE) return Blocks.SANDSTONE.getStateFromMeta(2);
        if (block == FFDStoneDecor.SMOOTH_RED_SANDSTONE) return Blocks.RED_SANDSTONE.getStateFromMeta(2);
        if (block == FFDStoneDecor.SMOOTH_QUARTZ) return Blocks.QUARTZ_BLOCK.getDefaultState();
        for (FFDStoneDecor.Entry entry : FFDStoneDecor.entries()) {
            if (block != entry.stairs && block != entry.slab && block != entry.doubleSlab) continue;
            Block material = entry.material;
            if (material == FFDStoneDecor.SMOOTH_SANDSTONE) material = Blocks.SANDSTONE;
            if (material == FFDStoneDecor.SMOOTH_RED_SANDSTONE) material = Blocks.RED_SANDSTONE;
            if (material == FFDStoneDecor.SMOOTH_QUARTZ) material = Blocks.QUARTZ_BLOCK;
            if (block == entry.stairs) {
                return (material == Blocks.SANDSTONE ? Blocks.SANDSTONE_STAIRS
                        : material == Blocks.RED_SANDSTONE ? Blocks.RED_SANDSTONE_STAIRS
                        : material == Blocks.QUARTZ_BLOCK ? Blocks.QUARTZ_STAIRS
                        : material == Blocks.RED_NETHER_BRICK ? Blocks.NETHER_BRICK_STAIRS
                        : material == Blocks.MOSSY_COBBLESTONE ? Blocks.STONE_STAIRS
                        : Blocks.STONE_BRICK_STAIRS).getDefaultState();
            }
            boolean doubled = block == entry.doubleSlab;
            if (material == Blocks.RED_SANDSTONE) return (doubled ? Blocks.DOUBLE_STONE_SLAB2 : Blocks.STONE_SLAB2).getDefaultState();
            int metadata = material == Blocks.SANDSTONE ? 1 : material == Blocks.QUARTZ_BLOCK ? 7
                    : material == Blocks.RED_NETHER_BRICK ? 6 : material == Blocks.MOSSY_COBBLESTONE ? 3
                    : material == Blocks.STONEBRICK || material == Blocks.END_BRICKS ? 5 : 0;
            return (doubled ? Blocks.DOUBLE_STONE_SLAB : Blocks.STONE_SLAB).getStateFromMeta(metadata);
        }
        if (FFDStoneDecor.contains(block)) return Blocks.COBBLESTONE_WALL.getDefaultState();
        if (block == FFDBlocks.BARREL) return Blocks.CHEST.getDefaultState();
        if (block == FFDBlocks.SMOKER || block == FFDBlocks.BLAST_FURNACE) return Blocks.FURNACE.getDefaultState();
        if (block == FFDBlocks.LANTERN) return Blocks.GLOWSTONE.getDefaultState();
        if (block == FFDBlocks.CAMPFIRE) return Blocks.LOG.getDefaultState();
        if (block == FFDBlocks.BELL) return Blocks.GOLD_BLOCK.getDefaultState();
        if (block == FFDBlocks.COMPOSTER) return Blocks.CAULDRON.getDefaultState();
        if (block == FFDBlocks.LECTERN) return Blocks.BOOKSHELF.getDefaultState();
        if (block == FFDBlocks.GRINDSTONE) return Blocks.ANVIL.getDefaultState();
        if (block == FFDBlocks.STONECUTTER || block == FFDBlocks.CARTOGRAPHY_TABLE
                || block == FFDBlocks.FLETCHING_TABLE || block == FFDBlocks.SMITHING_TABLE
                || block == FFDBlocks.LOOM) return Blocks.CRAFTING_TABLE.getDefaultState();
        if (block == FFDBlocks.BLUE_ICE) return Blocks.PACKED_ICE.getDefaultState();
        if (block == FFDBlocks.PUMPKIN) return Blocks.PUMPKIN.getDefaultState();
        if (block == FFDBlocks.SEA_PICKLE || block == FFDBlocks.SEAGRASS
                || block == FFDBlocks.TALL_SEAGRASS || block == FFDBlocks.KELP
                || block == FFDBlocks.KELP_PLANT || block == FFDBlocks.KELP_YOUNG) return Blocks.WATER.getDefaultState();
        if (block == FFDBlocks.CORNFLOWER || block == FFDBlocks.LILY_OF_THE_VALLEY
                || block == FFDBlocks.WITHER_ROSE) return Blocks.RED_FLOWER.getDefaultState();
        for (int index = 0; index < FFDBlocks.WOODS.length; index++) {
            if (block == FFDBlocks.WOODS[index] || block == FFDBlocks.STRIPPED_LOGS[index]
                    || block == FFDBlocks.STRIPPED_WOODS[index]) {
                int metadata = index < 4 ? index : index - 4;
                if (block != FFDBlocks.STRIPPED_LOGS[index]) metadata |= 12;
                return (index < 4 ? Blocks.LOG : Blocks.LOG2).getStateFromMeta(metadata);
            }
        }
        for (Block candidate : FFDBlocks.OVERWORLD_TRAPDOORS) {
            if (block == candidate) return Blocks.TRAPDOOR.getDefaultState();
        }
        for (Block candidate : FFDBlocks.OVERWORLD_BUTTONS) {
            if (block == candidate) return Blocks.WOODEN_BUTTON.getDefaultState();
        }
        for (Block candidate : FFDBlocks.OVERWORLD_PRESSURE_PLATES) {
            if (block == candidate) return Blocks.WOODEN_PRESSURE_PLATE.getDefaultState();
        }
        for (Block candidate : FFDBlocks.WOOD_STANDING_SIGNS) {
            if (block == candidate) return Blocks.STANDING_SIGN.getDefaultState();
        }
        for (Block candidate : FFDBlocks.WOOD_WALL_SIGNS) {
            if (block == candidate) return Blocks.WALL_SIGN.getDefaultState();
        }
        return null;
    }
}
