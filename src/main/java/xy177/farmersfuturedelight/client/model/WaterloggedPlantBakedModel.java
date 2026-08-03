package xy177.farmersfuturedelight.client.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.util.vector.Vector3f;

/** Adds a water volume behind plants that replace a source-water block in 1.12. */
public final class WaterloggedPlantBakedModel implements IBakedModel {
    private static final Vector3f MIN = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final float SOURCE_WATER_SURFACE = 16.0F * (8.0F / 9.0F - 0.001F);
    private static final Vector3f MAX = new Vector3f(16.0F, SOURCE_WATER_SURFACE, 16.0F);

    private final IBakedModel plantModel;
    private final Map<EnumFacing, List<BakedQuad>> waterQuads;

    public WaterloggedPlantBakedModel(IBakedModel plantModel) {
        this.plantModel = plantModel;
        this.waterQuads = createWaterQuads();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer == BlockRenderLayer.TRANSLUCENT) {
            if (state == null || !containsWater(state) || side == null) {
                return Collections.emptyList();
            }
            return waterQuads.get(side);
        }
        if (layer == null || layer == BlockRenderLayer.CUTOUT) {
            return plantModel.getQuads(state, side, rand);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return plantModel.isAmbientOcclusion();
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
        return state.getMaterial() == Material.WATER;
    }

    private static Map<EnumFacing, List<BakedQuad>> createWaterQuads() {
        TextureAtlasSprite still = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/water_still");
        TextureAtlasSprite flowing = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/water_flow");
        FaceBakery bakery = new FaceBakery();
        Map<EnumFacing, List<BakedQuad>> quads = new EnumMap<>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.values()) {
            TextureAtlasSprite texture = face == EnumFacing.UP || face == EnumFacing.DOWN ? still : flowing;
            BlockPartFace partFace = new BlockPartFace(null, 0, "",
                    new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));
            quads.put(face, Collections.singletonList(bakery.makeBakedQuad(MIN, MAX, partFace, texture,
                    face, ModelRotation.X0_Y0, null, true, true)));
        }
        return quads;
    }
}
