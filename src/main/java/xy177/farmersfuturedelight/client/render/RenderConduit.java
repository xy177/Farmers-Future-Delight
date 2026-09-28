package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.tile.TileEntityConduit;

public class RenderConduit extends TileEntitySpecialRenderer<TileEntityConduit> {
    private static final ResourceLocation BASE = texture("base");
    private static final ResourceLocation CAGE = texture("cage");
    private static final ResourceLocation WIND = texture("wind");
    private static final ResourceLocation WIND_VERTICAL = texture("wind_vertical");
    private static final ResourceLocation OPEN_EYE = texture("open_eye");
    private static final ResourceLocation CLOSED_EYE = texture("closed_eye");
    private final ShellModel shell = new ShellModel();
    private final CageModel cage = new CageModel();
    private final WindModel wind = new WindModel();
    private final EyeModel eye = new EyeModel();

    @Override
    public void render(TileEntityConduit tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!tile.isActive()) {
            renderShell(tile, x, y, z, partialTicks);
        } else {
            renderActive(tile, x, y, z, partialTicks);
        }
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);
    }

    public void renderItem(float partialTicks) {
        bindTexture(BASE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        shell.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    private void renderShell(TileEntityConduit tile, double x, double y, double z,
                             float partialTicks) {
        bindTexture(BASE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.rotate(tile.getActiveRotation(0.0F), 0.0F, 1.0F, 0.0F);
        shell.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    private void renderActive(TileEntityConduit tile, double x, double y, double z,
                              float partialTicks) {
        float time = tile.getTickCount() + partialTicks;
        float rotation = tile.getActiveRotation(partialTicks) * 57.295776F;
        float bob = MathHelper.sin(time * 0.1F) / 2.0F + 0.5F;
        bob = bob * bob + bob;
        bindTexture(CAGE);
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.3D + bob * 0.2D, z + 0.5D);
        GlStateManager.rotate(rotation, 0.5F, 1.0F, 0.5F);
        cage.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();

        int frame = tile.getTickCount() / 3 % WindModel.FRAMES;
        wind.setFrame(frame);
        int direction = tile.getTickCount() / (3 * WindModel.FRAMES) % 3;
        bindTexture(direction == 1 ? WIND_VERTICAL : WIND);
        renderWind(x, y, z, direction, false);
        renderWind(x, y, z, direction, true);

        bindTexture(tile.isEyeOpen() ? OPEN_EYE : CLOSED_EYE);
        Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        float yaw = view == null ? 0.0F : view.rotationYaw;
        float pitch = view == null ? 0.0F : view.rotationPitch;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.3D + bob * 0.2D, z + 0.5D);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        eye.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.083333336F);
        GlStateManager.popMatrix();
        GlStateManager.enableCull();
    }

    private void renderWind(double x, double y, double z, int direction, boolean mirrored) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        if (mirrored) {
            GlStateManager.scale(0.875F, 0.875F, 0.875F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        }
        if (direction == 1) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        } else if (direction == 2) {
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        }
        wind.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID,
                "textures/entity/conduit/" + name + ".png");
    }

    private static final class CageModel extends ModelBase {
        private final ModelRenderer cube;

        private CageModel() {
            textureWidth = 32;
            textureHeight = 16;
            cube = new ModelRenderer(this, 0, 0);
            cube.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            cube.render(scale);
        }
    }

    private static final class ShellModel extends ModelBase {
        private final ModelRenderer cube;

        private ShellModel() {
            textureWidth = 32;
            textureHeight = 16;
            cube = new ModelRenderer(this, 0, 0);
            cube.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            cube.render(scale);
        }
    }

    private static final class EyeModel extends ModelBase {
        private final ModelRenderer plane;

        private EyeModel() {
            textureWidth = 8;
            textureHeight = 8;
            plane = new ModelRenderer(this, 0, 0);
            plane.addBox(-4.0F, -4.0F, 0.0F, 8, 8, 0, 0.01F);
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            plane.render(scale);
        }
    }

    private static final class WindModel extends ModelBase {
        private static final int FRAMES = 22;
        private final ModelRenderer[] cubes = new ModelRenderer[FRAMES];
        private int frame;

        private WindModel() {
            textureWidth = 64;
            textureHeight = 1024;
            for (int i = 0; i < cubes.length; i++) {
                cubes[i] = new ModelRenderer(this, 0, 32 * i);
                cubes[i].addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
            }
        }

        private void setFrame(int frame) {
            this.frame = frame;
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            cubes[frame].render(scale);
        }
    }
}
