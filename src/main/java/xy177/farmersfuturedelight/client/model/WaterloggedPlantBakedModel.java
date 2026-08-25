package xy177.farmersfuturedelight.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import xy177.farmersfuturedelight.client.AquaAcrobaticsWaterCompat;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;

/** Adds a water volume behind plants that replace a source-water block in 1.12. */
public final class WaterloggedPlantBakedModel implements IBakedModel {
    private static final float SOURCE_WATER_SURFACE = 16.0F * (8.0F / 9.0F - 0.001F);
    private static final float FULL_WATER_SURFACE = 16.0F * (1.0F - 0.001F);

    private final IBakedModel plantModel;
    private volatile WaterQuadCache waterQuadCache;
    private final TextureAtlasSprite stillWater;
    private final TextureAtlasSprite flowingWater;

    public WaterloggedPlantBakedModel(IBakedModel plantModel) {
        this.plantModel = plantModel;
        this.stillWater = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(AquaAcrobaticsWaterCompat.stillTexture());
        this.flowingWater = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(AquaAcrobaticsWaterCompat.flowingTexture());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == BlockRenderLayer.TRANSLUCENT) {
            if (state == null || !containsWater(state) || side == null) {
                return Collections.emptyList();
            }
            return getWaterQuads(state).get(side);
        }
        return plantModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return plantModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return plantModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return plantModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return plantModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return plantModel.getOverrides();
    }

    private boolean containsWater(IBlockState state) {
        return state.getMaterial() == Material.WATER
                || WaterloggedPlantFluid.isWaterlogged(state);
    }

    private Map<EnumFacing, List<BakedQuad>> getWaterQuads(IBlockState state) {
        VertexFormat format = DefaultVertexFormats.BLOCK;
        boolean waterAbove = hasWaterAbove(state);
        float fallback = waterAbove ? FULL_WATER_SURFACE / 16.0F
                : SOURCE_WATER_SURFACE / 16.0F;
        float northWest = getHeight(state, WaterloggedPlantFluid.WATER_NORTH_WEST, fallback);
        float southWest = getHeight(state, WaterloggedPlantFluid.WATER_SOUTH_WEST, fallback);
        float southEast = getHeight(state, WaterloggedPlantFluid.WATER_SOUTH_EAST, fallback);
        float northEast = getHeight(state, WaterloggedPlantFluid.WATER_NORTH_EAST, fallback);
        boolean northVisible = isVisible(state, WaterloggedPlantFluid.WATER_NORTH_VISIBLE);
        boolean southVisible = isVisible(state, WaterloggedPlantFluid.WATER_SOUTH_VISIBLE);
        boolean westVisible = isVisible(state, WaterloggedPlantFluid.WATER_WEST_VISIBLE);
        boolean eastVisible = isVisible(state, WaterloggedPlantFluid.WATER_EAST_VISIBLE);
        boolean downVisible = isVisible(state, WaterloggedPlantFluid.WATER_DOWN_VISIBLE);
        long key = heightKey(northWest, southWest, southEast, northEast);
        if (waterAbove) {
            key |= 1L << 32;
        }
        if (northVisible) {
            key |= 1L << 33;
        }
        if (southVisible) {
            key |= 1L << 34;
        }
        if (westVisible) {
            key |= 1L << 35;
        }
        if (eastVisible) {
            key |= 1L << 36;
        }
        if (downVisible) {
            key |= 1L << 37;
        }
        WaterQuadCache cache = waterQuadCache;
        if (cache == null || !cache.matches(format)) {
            synchronized (this) {
                cache = waterQuadCache;
                if (cache == null || !cache.matches(format)) {
                    cache = new WaterQuadCache(format);
                    waterQuadCache = cache;
                }
            }
        }
        Map<EnumFacing, List<BakedQuad>> cached = cache.quads.get(key);
        if (cached == null) {
            Map<EnumFacing, List<BakedQuad>> created = createWaterQuads(format,
                    northWest, southWest, southEast, northEast, waterAbove,
                    northVisible, southVisible, westVisible, eastVisible, downVisible);
            Map<EnumFacing, List<BakedQuad>> existing = cache.quads.putIfAbsent(key, created);
            cached = existing == null ? created : existing;
        }
        return cached;
    }

    private static float getHeight(IBlockState state,
                                   net.minecraftforge.common.property.IUnlistedProperty<Float> property,
                                   float fallback) {
        if (state instanceof IExtendedBlockState) {
            Float value = ((IExtendedBlockState) state).getValue(property);
            if (value != null) {
                return Math.max(0.0F, Math.min(1.0F, value - 0.001F));
            }
        }
        return fallback;
    }

    private static boolean hasWaterAbove(IBlockState state) {
        return state instanceof IExtendedBlockState
                && Boolean.TRUE.equals(((IExtendedBlockState) state)
                        .getValue(WaterloggedPlantFluid.WATER_ABOVE));
    }

    private static boolean isVisible(IBlockState state,
                                     net.minecraftforge.common.property.IUnlistedProperty<Boolean> property) {
        return !(state instanceof IExtendedBlockState)
                || !Boolean.FALSE.equals(((IExtendedBlockState) state).getValue(property));
    }

    private Map<EnumFacing, List<BakedQuad>> createWaterQuads(VertexFormat format,
                                                                float northWest, float southWest,
                                                                float southEast, float northEast,
                                                                boolean waterAbove,
                                                                boolean northVisible,
                                                                boolean southVisible,
                                                                boolean westVisible,
                                                                boolean eastVisible,
                                                                boolean downVisible) {
        Map<EnumFacing, List<BakedQuad>> quads = new EnumMap<>(EnumFacing.class);
        Vertex topNorthWest = vertex(0.0F, northWest, 0.0F, 0.0F, 0.0F);
        Vertex topSouthWest = vertex(0.0F, southWest, 1.0F, 0.0F, 16.0F);
        Vertex topSouthEast = vertex(1.0F, southEast, 1.0F, 16.0F, 16.0F);
        Vertex topNorthEast = vertex(1.0F, northEast, 0.0F, 16.0F, 0.0F);
        if (waterAbove) {
            quads.put(EnumFacing.UP, Collections.<BakedQuad>emptyList());
        } else {
            quads.put(EnumFacing.UP, Arrays.asList(
                    createQuad(format, EnumFacing.UP, stillWater,
                            topNorthWest, topSouthWest, topSouthEast, topNorthEast),
                    createQuad(format, EnumFacing.DOWN, stillWater,
                            topNorthWest, topNorthEast, topSouthEast, topSouthWest)));
        }
        if (downVisible) {
            quads.put(EnumFacing.DOWN, Collections.singletonList(createQuad(format, EnumFacing.DOWN, stillWater,
                    vertex(0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                    vertex(1.0F, 0.0F, 0.0F, 16.0F, 0.0F),
                    vertex(1.0F, 0.0F, 1.0F, 16.0F, 16.0F),
                    vertex(0.0F, 0.0F, 1.0F, 0.0F, 16.0F))));
        } else {
            quads.put(EnumFacing.DOWN, Collections.<BakedQuad>emptyList());
        }
        if (northVisible) {
            quads.put(EnumFacing.NORTH, Collections.singletonList(createQuad(format, EnumFacing.NORTH, flowingWater,
                    vertex(0.0F, 0.0F, 0.0F, 0.0F, 16.0F),
                    vertex(0.0F, northWest, 0.0F, 0.0F, 0.0F),
                    vertex(1.0F, northEast, 0.0F, 16.0F, 0.0F),
                    vertex(1.0F, 0.0F, 0.0F, 16.0F, 16.0F))));
        } else {
            quads.put(EnumFacing.NORTH, Collections.<BakedQuad>emptyList());
        }
        if (southVisible) {
            quads.put(EnumFacing.SOUTH, Collections.singletonList(createQuad(format, EnumFacing.SOUTH, flowingWater,
                    vertex(0.0F, 0.0F, 1.0F, 16.0F, 16.0F),
                    vertex(1.0F, 0.0F, 1.0F, 0.0F, 16.0F),
                    vertex(1.0F, southEast, 1.0F, 0.0F, 0.0F),
                    vertex(0.0F, southWest, 1.0F, 16.0F, 0.0F))));
        } else {
            quads.put(EnumFacing.SOUTH, Collections.<BakedQuad>emptyList());
        }
        if (westVisible) {
            quads.put(EnumFacing.WEST, Collections.singletonList(createQuad(format, EnumFacing.WEST, flowingWater,
                    vertex(0.0F, 0.0F, 0.0F, 16.0F, 16.0F),
                    vertex(0.0F, 0.0F, 1.0F, 0.0F, 16.0F),
                    vertex(0.0F, southWest, 1.0F, 0.0F, 0.0F),
                    vertex(0.0F, northWest, 0.0F, 16.0F, 0.0F))));
        } else {
            quads.put(EnumFacing.WEST, Collections.<BakedQuad>emptyList());
        }
        if (eastVisible) {
            quads.put(EnumFacing.EAST, Collections.singletonList(createQuad(format, EnumFacing.EAST, flowingWater,
                    vertex(1.0F, 0.0F, 0.0F, 0.0F, 16.0F),
                    vertex(1.0F, northEast, 0.0F, 0.0F, 0.0F),
                    vertex(1.0F, southEast, 1.0F, 16.0F, 0.0F),
                    vertex(1.0F, 0.0F, 1.0F, 16.0F, 16.0F))));
        } else {
            quads.put(EnumFacing.EAST, Collections.<BakedQuad>emptyList());
        }
        return quads;
    }

    private static Vertex vertex(float x, float y, float z, float u, float v) {
        return new Vertex(x, y, z, u, v);
    }

    private static BakedQuad createQuad(VertexFormat format, EnumFacing face, TextureAtlasSprite texture,
                                        Vertex first, Vertex second, Vertex third, Vertex fourth) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(face);
        builder.setQuadTint(1);
        builder.setTexture(texture);
        builder.setApplyDiffuseLighting(true);
        Vertex[] vertices = {first, second, third, fourth};
        for (Vertex vertex : vertices) {
            putVertex(builder, format, face, texture, vertex);
        }
        return builder.build();
    }

    private static void putVertex(UnpackedBakedQuad.Builder builder, VertexFormat format,
                                  EnumFacing face, TextureAtlasSprite texture, Vertex vertex) {
        for (int elementIndex = 0; elementIndex < format.getElementCount(); elementIndex++) {
            VertexFormatElement element = format.getElement(elementIndex);
            switch (element.getUsage()) {
                case POSITION:
                    builder.put(elementIndex, vertex.x, vertex.y, vertex.z, 1.0F);
                    break;
                case COLOR:
                    builder.put(elementIndex, 1.0F, 1.0F, 1.0F, 1.0F);
                    break;
                case NORMAL:
                    builder.put(elementIndex, face.getFrontOffsetX(), face.getFrontOffsetY(),
                            face.getFrontOffsetZ(), 0.0F);
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        builder.put(elementIndex, texture.getInterpolatedU(vertex.u),
                                texture.getInterpolatedV(vertex.v), 0.0F, 1.0F);
                    } else {
                        builder.put(elementIndex);
                    }
                    break;
                default:
                    builder.put(elementIndex);
                    break;
            }
        }
    }

    private static long heightKey(float northWest, float southWest, float southEast, float northEast) {
        long key = quantize(northWest);
        key |= (long) quantize(southWest) << 8;
        key |= (long) quantize(southEast) << 16;
        key |= (long) quantize(northEast) << 24;
        return key;
    }

    private static int quantize(float height) {
        return Math.max(0, Math.min(255, Math.round(height * 255.0F)));
    }

    private static final class WaterQuadCache {
        private final VertexFormat format;
        private final int elementCount;
        private final int integerSize;
        private final ConcurrentMap<Long, Map<EnumFacing, List<BakedQuad>>> quads =
                new ConcurrentHashMap<>();

        private WaterQuadCache(VertexFormat format) {
            this.format = format;
            this.elementCount = format.getElementCount();
            this.integerSize = format.getIntegerSize();
        }

        private boolean matches(VertexFormat current) {
            return format == current
                    && elementCount == current.getElementCount()
                    && integerSize == current.getIntegerSize();
        }
    }

    private static final class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private final float u;
        private final float v;

        private Vertex(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }
}
