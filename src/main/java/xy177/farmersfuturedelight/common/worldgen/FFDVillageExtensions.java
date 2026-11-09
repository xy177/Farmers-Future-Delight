package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

final class FFDVillageExtensions {
    private final StructureVillagePieces.Start start;
    private final List<StructureVillagePieces.PieceWeight> weights;

    FFDVillageExtensions(WorldServer world, BlockPos origin, Random random) {
        weights = StructureVillagePieces.getStructureVillageWeightedPieceList(random, 0);
        weights.removeIf(weight -> weight.villagePieceClass.getName().startsWith("net.minecraft."));
        start = weights.isEmpty() ? null : new StructureVillagePieces.Start(world.getBiomeProvider(), 0, random,
                origin.getX(), origin.getZ(), weights, 0);
    }

    List<StructureVillagePieces.PieceWeight> available() {
        List<StructureVillagePieces.PieceWeight> result = new ArrayList<>();
        for (StructureVillagePieces.PieceWeight weight : weights) {
            if (weight.villagePieceWeight > 0 && weight.canSpawnMoreVillagePieces()) result.add(weight);
        }
        return result;
    }

    List<StructureComponent> build(StructureVillagePieces.PieceWeight weight, BlockPos origin, EnumFacing facing,
                                   int depth, List<StructureBoundingBox> occupied,
                                   StructureBoundingBox enclosing, Random random) {
        if (start == null || facing.getAxis() == EnumFacing.Axis.Y || !weight.canSpawnMoreVillagePiecesOfType(depth)) {
            return Collections.emptyList();
        }
        List<StructureComponent> components = new ArrayList<>();
        for (StructureBoundingBox bounds : occupied) components.add(new ReservedSpace(bounds));
        int first = components.size();
        StructureVillagePieces.Village piece = VillagerRegistry.getVillageComponent(weight, start, components, random,
                origin.getX(), origin.getY(), origin.getZ(), facing, depth);
        if (piece == null) return Collections.emptyList();
        StructureBoundingBox bounds = piece.getBoundingBox();
        if (enclosing != null && (bounds.minX < enclosing.minX || bounds.maxX > enclosing.maxX
                || bounds.minY < enclosing.minY || bounds.maxY > enclosing.maxY
                || bounds.minZ < enclosing.minZ || bounds.maxZ > enclosing.maxZ)) return Collections.emptyList();
        components.add(piece);
        weight.villagePiecesSpawned++;
        start.lastPlaced = weight;
        start.pendingHouses.clear();
        start.pendingRoads.clear();
        piece.buildComponent(start, components, random);
        int built = 0;
        while (!start.pendingHouses.isEmpty() || !start.pendingRoads.isEmpty()) {
            if (++built > 512 || components.size() - first > 512) {
                throw new IllegalArgumentException("External village component limit exceeded");
            }
            List<StructureComponent> pending = start.pendingRoads.isEmpty() ? start.pendingHouses : start.pendingRoads;
            StructureComponent next = pending.remove(random.nextInt(pending.size()));
            next.buildComponent(start, components, random);
        }
        return new ArrayList<>(components.subList(first, components.size()));
    }

    private static final class ReservedSpace extends StructureComponent {
        private ReservedSpace(StructureBoundingBox bounds) {
            super(0);
            boundingBox = new StructureBoundingBox(bounds);
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
        }

        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            return true;
        }
    }
}
