package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModelModernZombieVillagerArmor extends ModelModernZombieVillager {
    private final ModelRenderer waist;

    public ModelModernZombieVillagerArmor(boolean baby, EntityEquipmentSlot slot) {
        super(baby);
        textureHeight = baby ? 64 : 32;
        waist = baby && slot == EntityEquipmentSlot.LEGS ? new ModelRenderer(this) : null;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        if (baby) {
            float inflationY = slot == EntityEquipmentSlot.LEGS ? 0.3F : 0.5F;
            bipedHead.setRotationPoint(0.0F, 15.0F, 0.0F);
            bipedBody.setRotationPoint(0.0F, 18.0F, 0.0F);
            bipedRightArm.setRotationPoint(-3.5F, 15.5F, 0.0F);
            bipedLeftArm.setRotationPoint(3.5F, 15.5F, 0.0F);
            bipedRightLeg.setRotationPoint(-1.5F, 20.0F, 0.5F);
            bipedLeftLeg.setRotationPoint(1.5F, 20.0F, 0.5F);
            if (slot == EntityEquipmentSlot.HEAD) {
                box(bipedHead, 0, 0, -4.5F, -7.0F, -4.5F, 9, 8, 8,
                        -0.1F, inflationY, 0.3F, false);
            } else if (slot == EntityEquipmentSlot.CHEST) {
                box(bipedBody, 0, 17, -3.0F, -3.0F, -1.5F, 6, 5, 3,
                        -0.1F, inflationY, 0.3F, false);
                box(bipedRightArm, 30, 25, -1.0F, 0.0F, -1.53F, 2, 5, 3,
                        -0.1F, inflationY, 0.3F, false);
                box(bipedLeftArm, 30, 17, -1.0F, 0.0F, -1.53F, 2, 5, 3,
                        -0.1F, inflationY, 0.3F, false);
            } else if (slot == EntityEquipmentSlot.LEGS) {
                waist.setRotationPoint(0.0F, 19.0F, 0.0F);
                box(waist, 0, 36, -3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F,
                        -0.2F, inflationY - 0.1F, 0.2F, false);
                box(bipedRightLeg, 18, 17, -1.0F, -0.2F, -2.0F, 3, 4, 3,
                        -0.2F, inflationY - 0.1F, 0.2F, false);
                box(bipedLeftLeg, 18, 24, -2.0F, -0.2F, -2.0F, 3, 4, 3,
                        -0.2F, inflationY - 0.1F, 0.2F, false);
            } else if (slot == EntityEquipmentSlot.FEET) {
                box(bipedRightLeg, 0, 29, -1.0F, 2.9F, -2.0F, 3, 1, 3,
                        -0.1F, inflationY, 0.3F, true);
                box(bipedLeftLeg, 0, 25, -2.0F, 2.9F, -2.0F, 3, 1, 3,
                        -0.1F, inflationY, 0.3F, false);
            }
        } else {
            float inflation = slot == EntityEquipmentSlot.LEGS ? 0.5F : 1.0F;
            bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 8, 8, inflation);
            bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, inflation + 0.5F);
            bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, inflation + 0.1F);
            bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, inflation);
            bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            bipedLeftArm.mirror = true;
            bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, inflation);
            bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, inflation + 0.1F);
            bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
            bipedLeftLeg.mirror = true;
            bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, inflation + 0.1F);
            bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        }
        rememberPivots();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbAmount, float age,
                       float yaw, float pitch, float scale) {
        super.render(entity, limbSwing, limbAmount, age, yaw, pitch, scale);
        if (waist != null && bipedBody.showModel && !bipedBody.isHidden) waist.render(scale);
    }

    private void box(ModelRenderer parent, int textureX, int textureY, float minX, float minY, float minZ,
                     float width, float height, float depth, float inflateX, float inflateY, float inflateZ,
                     boolean mirror) {
        parent.cubeList.add(new FloatBox(parent, textureX, textureY, minX, minY, minZ,
                width, height, depth, inflateX, inflateY, inflateZ, mirror));
    }

    private static final class FloatBox extends ModelBox {
        private final TexturedQuad[] faces;

        private FloatBox(ModelRenderer parent, int textureX, int textureY, float minX, float minY,
                         float minZ, float width, float height, float depth, float inflateX,
                         float inflateY, float inflateZ, boolean mirror) {
            super(parent, textureX, textureY, minX, minY, minZ,
                    (int) Math.ceil(width), (int) Math.ceil(height), (int) Math.ceil(depth), 0.0F, mirror);
            float maxX = minX + width + inflateX;
            float maxY = minY + height + inflateY;
            float maxZ = minZ + depth + inflateZ;
            minX -= inflateX;
            minY -= inflateY;
            minZ -= inflateZ;
            if (mirror) {
                float previous = maxX;
                maxX = minX;
                minX = previous;
            }
            PositionTextureVertex frontLeftTop = new PositionTextureVertex(minX, minY, minZ, 0, 0);
            PositionTextureVertex frontRightTop = new PositionTextureVertex(maxX, minY, minZ, 0, 0);
            PositionTextureVertex frontRightBottom = new PositionTextureVertex(maxX, maxY, minZ, 0, 0);
            PositionTextureVertex frontLeftBottom = new PositionTextureVertex(minX, maxY, minZ, 0, 0);
            PositionTextureVertex backLeftTop = new PositionTextureVertex(minX, minY, maxZ, 0, 0);
            PositionTextureVertex backRightTop = new PositionTextureVertex(maxX, minY, maxZ, 0, 0);
            PositionTextureVertex backRightBottom = new PositionTextureVertex(maxX, maxY, maxZ, 0, 0);
            PositionTextureVertex backLeftBottom = new PositionTextureVertex(minX, maxY, maxZ, 0, 0);
            float first = textureX + depth;
            float second = first + width;
            float third = second + depth;
            float top = textureY + depth;
            float bottom = top + height;
            faces = new TexturedQuad[] {
                    face(parent, new PositionTextureVertex[] {backRightTop, frontRightTop,
                            frontRightBottom, backRightBottom}, second, top, third, bottom),
                    face(parent, new PositionTextureVertex[] {frontLeftTop, backLeftTop,
                            backLeftBottom, frontLeftBottom}, textureX, top, first, bottom),
                    face(parent, new PositionTextureVertex[] {backRightTop, backLeftTop,
                            frontLeftTop, frontRightTop}, first, textureY, second, top),
                    face(parent, new PositionTextureVertex[] {frontRightBottom, frontLeftBottom,
                            backLeftBottom, backRightBottom}, second, top, second + width, textureY),
                    face(parent, new PositionTextureVertex[] {frontRightTop, frontLeftTop,
                            frontLeftBottom, frontRightBottom}, first, top, second, bottom),
                    face(parent, new PositionTextureVertex[] {backLeftTop, backRightTop,
                            backRightBottom, backLeftBottom}, third, top, third + width, bottom)
            };
            if (mirror) for (TexturedQuad face : faces) face.flipFace();
        }

        private static TexturedQuad face(ModelRenderer parent, PositionTextureVertex[] vertices,
                                         float minU, float minV, float maxU, float maxV) {
            vertices[0] = vertices[0].setTexturePosition(maxU / parent.textureWidth, minV / parent.textureHeight);
            vertices[1] = vertices[1].setTexturePosition(minU / parent.textureWidth, minV / parent.textureHeight);
            vertices[2] = vertices[2].setTexturePosition(minU / parent.textureWidth, maxV / parent.textureHeight);
            vertices[3] = vertices[3].setTexturePosition(maxU / parent.textureWidth, maxV / parent.textureHeight);
            return new TexturedQuad(vertices);
        }

        @Override
        public void render(BufferBuilder renderer, float scale) {
            for (TexturedQuad face : faces) face.draw(renderer, scale);
        }
    }
}
