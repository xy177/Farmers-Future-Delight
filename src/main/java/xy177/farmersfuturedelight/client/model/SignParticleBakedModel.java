package xy177.farmersfuturedelight.client.model;

import java.util.List;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

public final class SignParticleBakedModel implements IBakedModel {
    private final IBakedModel original;
    private final TextureAtlasSprite particle;

    public SignParticleBakedModel(IBakedModel original, TextureAtlasSprite particle) {
        this.original = original;
        this.particle = particle;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return original.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return original.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particle;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return original.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return original.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        return original.handlePerspective(type);
    }
}
