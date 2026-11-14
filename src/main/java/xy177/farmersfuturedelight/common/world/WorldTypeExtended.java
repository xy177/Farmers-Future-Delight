package xy177.farmersfuturedelight.common.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.common.world.biome.FFDModernBiomeProvider;
import xy177.farmersfuturedelight.common.world.modern.ModernGenerationSettings;

public final class WorldTypeExtended extends WorldType {
    WorldTypeExtended() {
        super(FFDHeightHooks.WORLD_TYPE_NAME);
    }

    @Override
    public net.minecraft.world.biome.BiomeProvider getBiomeProvider(World world) {
        return new FFDModernBiomeProvider(world);
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        ModernGenerationSettings modern = ModernGenerationSettings.read(generatorOptions);
        if (modern != null) {
            return new ChunkGeneratorModern(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(),
                    generatorOptions, modern);
        }
        return new ChunkGeneratorExtended(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(),
                generatorOptions);
    }
}
