package xy177.farmersfuturedelight.common.worldgen;

import net.minecraft.block.Block;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;
import xy177.farmersfuturedelight.common.world.ChunkGeneratorExtended;

public final class FFDModernMineshaftGenerator extends MapGenMineshaft {
    private final ChunkGeneratorExtended extendedGenerator;

    public FFDModernMineshaftGenerator(ChunkGeneratorExtended extendedGenerator) {
        this.extendedGenerator = extendedGenerator;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8));
        Type type = biome instanceof BiomeMesa ? Type.MESA : Type.NORMAL;
        StructureMineshaftStart start = new StructureMineshaftStart(
                world, rand, chunkX, chunkZ, type);
        FFDMineshaftHooks.adjustModernMineshaftHeight(start, world, rand, type, extendedGenerator);
        return start;
    }
}
