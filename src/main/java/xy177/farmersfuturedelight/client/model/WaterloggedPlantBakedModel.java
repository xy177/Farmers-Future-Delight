package xy177.farmersfuturedelight.client.model;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import xy177.farmersfuturedelight.client.AquaAcrobaticsWaterCompat;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;

public final class WaterloggedPlantBakedModel implements IBakedModel {
    public static final int FLUID_TINT_INDEX = 0x464644;
    private static final float SOURCE_WATER_SURFACE = 16.0F * (8.0F / 9.0F - 0.001F);
    private static final float FULL_WATER_SURFACE = 16.0F * (1.0F - 0.001F);
    private static final float SIDE_INSET = 0.002F;

    private final IBakedModel plantModel;
    private volatile WaterQuadCache waterQuadCache;

    public WaterloggedPlantBakedModel(IBakedModel plantModel) {
        this.plantModel = plantModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == BlockRenderLayer.TRANSLUCENT) {
            if (state == null || !containsWater(state)) {
                return plantModel.getQuads(state, side, rand);
            }
            if (side != null) {
                return Collections.emptyList();
            }
            List<BakedQuad> quads = new ArrayList<>();
            for (List<BakedQuad> face : getWaterQuads(state).values()) {
                quads.addAll(face);
            }
            if (state.getBlock().getBlockLayer() == BlockRenderLayer.TRANSLUCENT) {
                quads.addAll(plantModel.getQuads(state, null, rand));
                for (EnumFacing face : EnumFacing.values()) {
                    quads.addAll(plantModel.getQuads(state, face, rand));
                }
            }
            return quads;
        }
        return plantModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return MinecraftForgeClient.getRenderLayer() != BlockRenderLayer.TRANSLUCENT
                && plantModel.isAmbientOcclusion();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return !(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT
                && state != null && containsWater(state))
                && plantModel.isAmbientOcclusion(state);
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
        return WaterloggedBlockApi.containsWater(state);
    }

    private Map<EnumFacing, List<BakedQuad>> getWaterQuads(IBlockState state) {
        VertexFormat format = DefaultVertexFormats.BLOCK;
        String fluidName = getFluidName(state);
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            fluid = FluidRegistry.WATER;
            fluidName = fluid.getName();
        }
        TextureAtlasSprite stillFluid = texture(fluid, false);
        TextureAtlasSprite flowingFluid = texture(fluid, true);
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
        float offsetX = getOffset(state, WaterloggedPlantFluid.WATER_MODEL_OFFSET_X);
        float offsetY = getOffset(state, WaterloggedPlantFluid.WATER_MODEL_OFFSET_Y);
        float offsetZ = getOffset(state, WaterloggedPlantFluid.WATER_MODEL_OFFSET_Z);
        long shapeKey = 0L;
        if (waterAbove) {
            shapeKey |= 1L << 32;
        }
        if (northVisible) {
            shapeKey |= 1L << 33;
        }
        if (southVisible) {
            shapeKey |= 1L << 34;
        }
        if (westVisible) {
            shapeKey |= 1L << 35;
        }
        if (eastVisible) {
            shapeKey |= 1L << 36;
        }
        if (downVisible) {
            shapeKey |= 1L << 37;
        }
        WaterQuadKey key = new WaterQuadKey(fluidName, shapeKey, northWest, southWest, southEast,
                northEast, offsetX, offsetY, offsetZ);
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
                    northVisible, southVisible, westVisible, eastVisible, downVisible,
                    offsetX, offsetY, offsetZ, stillFluid, flowingFluid);
            Map<EnumFacing, List<BakedQuad>> existing = cache.quads.putIfAbsent(key, created);
            cached = existing == null ? created : existing;
        }
        return cached;
    }

    private static String getFluidName(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            String value = ((IExtendedBlockState) state)
                    .getValue(WaterloggedPlantFluid.CONTAINED_FLUID);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return FluidRegistry.WATER.getName();
    }

    private static TextureAtlasSprite texture(Fluid fluid, boolean flowing) {
        String location;
        if (fluid == FluidRegistry.WATER) {
            location = flowing ? AquaAcrobaticsWaterCompat.flowingTexture()
                    : AquaAcrobaticsWaterCompat.stillTexture();
        } else {
            FluidStack stack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
            net.minecraft.util.ResourceLocation resource = flowing
                    ? fluid.getFlowing(stack) : fluid.getStill(stack);
            location = resource == null ? (flowing
                    ? AquaAcrobaticsWaterCompat.flowingTexture()
                    : AquaAcrobaticsWaterCompat.stillTexture()) : resource.toString();
        }
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location);
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

    private static float getOffset(IBlockState state,
                                   net.minecraftforge.common.property.IUnlistedProperty<Float> property) {
        if (state instanceof IExtendedBlockState) {
            Float value = ((IExtendedBlockState) state).getValue(property);
            if (value != null) {
                return value;
            }
        }
        return 0.0F;
    }

    private Map<EnumFacing, List<BakedQuad>> createWaterQuads(VertexFormat format,
                                                                float northWest, float southWest,
                                                                float southEast, float northEast,
                                                                boolean waterAbove,
                                                                boolean northVisible,
                                                                boolean southVisible,
                                                                boolean westVisible,
                                                                boolean eastVisible,
                                                                boolean downVisible,
                                                                float offsetX,
                                                                float offsetY,
                                                                float offsetZ,
                                                                TextureAtlasSprite stillFluid,
                                                                TextureAtlasSprite flowingFluid) {
        Map<EnumFacing, List<BakedQuad>> quads = new EnumMap<>(EnumFacing.class);
        float x0 = -offsetX;
        float x1 = 1.0F - offsetX;
        float y0 = -offsetY;
        float z0 = -offsetZ;
        float z1 = 1.0F - offsetZ;
        float westX = x0 + SIDE_INSET;
        float eastX = x1 - SIDE_INSET;
        float northZ = z0 + SIDE_INSET;
        float southZ = z1 - SIDE_INSET;
        Vertex topNorthWest = vertex(x0, northWest - offsetY, z0, 0.0F, 0.0F);
        Vertex topSouthWest = vertex(x0, southWest - offsetY, z1, 0.0F, 16.0F);
        Vertex topSouthEast = vertex(x1, southEast - offsetY, z1, 16.0F, 16.0F);
        Vertex topNorthEast = vertex(x1, northEast - offsetY, z0, 16.0F, 0.0F);
        if (waterAbove) {
            quads.put(EnumFacing.UP, Collections.<BakedQuad>emptyList());
        } else {
            quads.put(EnumFacing.UP, Arrays.asList(
                    createQuad(format, EnumFacing.UP, stillFluid,
                            topNorthWest, topSouthWest, topSouthEast, topNorthEast),
                    createQuad(format, EnumFacing.DOWN, stillFluid, false,
                            topNorthWest, topNorthEast, topSouthEast, topSouthWest)));
        }
        if (downVisible) {
            quads.put(EnumFacing.DOWN, Collections.singletonList(createQuad(format, EnumFacing.DOWN, stillFluid,
                    vertex(x0, y0, z0, 0.0F, 0.0F),
                    vertex(x1, y0, z0, 16.0F, 0.0F),
                    vertex(x1, y0, z1, 16.0F, 16.0F),
                    vertex(x0, y0, z1, 0.0F, 16.0F))));
        } else {
            quads.put(EnumFacing.DOWN, Collections.<BakedQuad>emptyList());
        }
        if (northVisible) {
            quads.put(EnumFacing.NORTH, Collections.singletonList(createQuad(format, EnumFacing.NORTH, flowingFluid,
                    vertex(x0, y0, northZ, 0.0F, 16.0F),
                    vertex(x0, northWest - offsetY, northZ, 0.0F, 0.0F),
                    vertex(x1, northEast - offsetY, northZ, 16.0F, 0.0F),
                    vertex(x1, y0, northZ, 16.0F, 16.0F))));
        } else {
            quads.put(EnumFacing.NORTH, Collections.<BakedQuad>emptyList());
        }
        if (southVisible) {
            quads.put(EnumFacing.SOUTH, Collections.singletonList(createQuad(format, EnumFacing.SOUTH, flowingFluid,
                    vertex(x0, y0, southZ, 16.0F, 16.0F),
                    vertex(x1, y0, southZ, 0.0F, 16.0F),
                    vertex(x1, southEast - offsetY, southZ, 0.0F, 0.0F),
                    vertex(x0, southWest - offsetY, southZ, 16.0F, 0.0F))));
        } else {
            quads.put(EnumFacing.SOUTH, Collections.<BakedQuad>emptyList());
        }
        if (westVisible) {
            quads.put(EnumFacing.WEST, Collections.singletonList(createQuad(format, EnumFacing.WEST, flowingFluid,
                    vertex(westX, y0, z0, 16.0F, 16.0F),
                    vertex(westX, y0, z1, 0.0F, 16.0F),
                    vertex(westX, southWest - offsetY, z1, 0.0F, 0.0F),
                    vertex(westX, northWest - offsetY, z0, 16.0F, 0.0F))));
        } else {
            quads.put(EnumFacing.WEST, Collections.<BakedQuad>emptyList());
        }
        if (eastVisible) {
            quads.put(EnumFacing.EAST, Collections.singletonList(createQuad(format, EnumFacing.EAST, flowingFluid,
                    vertex(eastX, y0, z0, 0.0F, 16.0F),
                    vertex(eastX, northEast - offsetY, z0, 0.0F, 0.0F),
                    vertex(eastX, southEast - offsetY, z1, 16.0F, 0.0F),
                    vertex(eastX, y0, z1, 16.0F, 16.0F))));
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
        return createQuad(format, face, texture, true, first, second, third, fourth);
    }

    private static BakedQuad createQuad(VertexFormat format, EnumFacing face, TextureAtlasSprite texture,
                                        boolean diffuseLighting,
                                        Vertex first, Vertex second, Vertex third, Vertex fourth) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(face);
        builder.setQuadTint(FLUID_TINT_INDEX);
        builder.setTexture(texture);
        builder.setApplyDiffuseLighting(diffuseLighting);
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

    private static final class WaterQuadCache {
        private final VertexFormat format;
        private final int elementCount;
        private final int integerSize;
        private final ConcurrentMap<WaterQuadKey, Map<EnumFacing, List<BakedQuad>>> quads =
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

    private static final class WaterQuadKey {
        private final String fluid;
        private final long shape;
        private final int northWest;
        private final int southWest;
        private final int southEast;
        private final int northEast;
        private final int offsetX;
        private final int offsetY;
        private final int offsetZ;

        private WaterQuadKey(String fluid, long shape, float northWest, float southWest, float southEast,
                float northEast, float offsetX, float offsetY, float offsetZ) {
            this.fluid = fluid;
            this.shape = shape;
            this.northWest = Float.floatToIntBits(northWest);
            this.southWest = Float.floatToIntBits(southWest);
            this.southEast = Float.floatToIntBits(southEast);
            this.northEast = Float.floatToIntBits(northEast);
            this.offsetX = Float.floatToIntBits(offsetX);
            this.offsetY = Float.floatToIntBits(offsetY);
            this.offsetZ = Float.floatToIntBits(offsetZ);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof WaterQuadKey)) {
                return false;
            }
            WaterQuadKey other = (WaterQuadKey) object;
            return fluid.equals(other.fluid) && shape == other.shape && northWest == other.northWest
                    && southWest == other.southWest && southEast == other.southEast
                    && northEast == other.northEast && offsetX == other.offsetX
                    && offsetY == other.offsetY && offsetZ == other.offsetZ;
        }

        @Override
        public int hashCode() {
            int result = fluid.hashCode();
            result = 31 * result + (int) (shape ^ shape >>> 32);
            result = 31 * result + northWest;
            result = 31 * result + southWest;
            result = 31 * result + southEast;
            result = 31 * result + northEast;
            result = 31 * result + offsetX;
            result = 31 * result + offsetY;
            return 31 * result + offsetZ;
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
