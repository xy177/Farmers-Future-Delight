package xy177.farmersfuturedelight.common.worldgen;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDJigsawStructureStart extends StructureStart {
    private final Set<Long> processed = new HashSet<>();
    private String storage;

    public FFDJigsawStructureStart() {
    }

    FFDJigsawStructureStart(WorldServer world, Random random, int chunkX, int chunkZ,
                           ResourceLocation pool, int depth, String storage, IntBinaryOperator heights,
                           boolean village) {
        super(chunkX, chunkZ);
        this.storage = storage;
        BlockPos origin = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        FFDVillageExtensions extensions = village ? new FFDVillageExtensions(world, origin,
                new Random(world.getSeed() ^ (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L)) : null;
        FFDJigsawPlacement.Plan plan = FFDJigsawPlacement.plan(world, pool, null, depth, origin,
                random, heights, true, extensions);
        components.addAll(plan.components(random));
        updateBoundingBox();
    }

    @Override
    public boolean isSizeableStructure() {
        return !components.isEmpty();
    }

    FFDJigsawStructureStart(WorldServer world, Random random, int chunkX, int chunkZ,
                           ResourceLocation pool, int depth, String storage, int height, int maxDistance) {
        super(chunkX, chunkZ);
        this.storage = storage;
        FFDJigsawPlacement.Plan plan = FFDJigsawPlacement.plan(world, pool, null, depth,
                new BlockPos(chunkX * 16, height, chunkZ * 16), random,
                (blockX, blockZ) -> height, false, null, maxDistance);
        components.addAll(plan.components(random));
        updateBoundingBox();
    }

    @Override
    public void generateStructure(World world, Random random, StructureBoundingBox bounds) {
        super.generateStructure(world, random, new StructureBoundingBox(bounds.minX, FFDHeightHooks.minY(world),
                bounds.minZ, bounds.maxX, FFDHeightHooks.maxYExclusive(world) - 1, bounds.maxZ));
        if (hasPending()) FFDModernStructures.defer(world, this);
    }

    @Override
    public boolean isValidForPostProcess(ChunkPos chunk) {
        return !processed.contains(ChunkPos.asLong(chunk.x, chunk.z));
    }

    @Override
    public void notifyPostProcessAt(ChunkPos chunk) {
        processed.add(ChunkPos.asLong(chunk.x, chunk.z));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("Storage", storage);
        NBTTagList chunks = new NBTTagList();
        for (long chunk : processed) chunks.appendTag(new NBTTagLong(chunk));
        tag.setTag("Processed", chunks);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        storage = tag.getString("Storage");
        processed.clear();
        NBTTagList chunks = tag.getTagList("Processed", 4);
        for (int index = 0; index < chunks.tagCount(); index++) processed.add(((NBTTagLong) chunks.get(index)).getLong());
    }

    @Override
    public void readStructureComponentsFromNBT(World world, NBTTagCompound tag) {
        super.readStructureComponentsFromNBT(world, tag);
        if (hasPending()) FFDModernStructures.defer(world, this);
    }

    boolean hasPending() {
        for (StructureComponent component : components) {
            if (component instanceof FFDJigsawPlacement.SavedPiece
                    && ((FFDJigsawPlacement.SavedPiece) component).hasPending()) return true;
        }
        return false;
    }

    boolean resume(WorldServer world) {
        boolean changed = false;
        for (StructureComponent component : components) {
            if (component instanceof FFDJigsawPlacement.SavedPiece) changed |= ((FFDJigsawPlacement.SavedPiece) component).resume(world);
        }
        return changed;
    }

    String storage() {
        return storage;
    }

    void addTerrain(FFDStructureBeard.Builder builder) {
        for (StructureComponent component : components) {
            if (component instanceof FFDJigsawPlacement.SavedPiece) ((FFDJigsawPlacement.SavedPiece) component).addTerrain(builder);
            else builder.piece(component.getBoundingBox(), 0);
        }
    }
}
