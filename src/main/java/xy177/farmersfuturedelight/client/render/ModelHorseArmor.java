package xy177.farmersfuturedelight.client.render;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class ModelHorseArmor extends ModelHorse {
    private final Set<ModelRenderer> armoredParts = Collections.newSetFromMap(
            new IdentityHashMap<ModelRenderer, Boolean>());

    public ModelHorseArmor() {
        remap("field_110715_k", source(0, 32, -5, -8, -17, 10, 10, 22, 0.05F, false), 0, 1);
        remap("field_110716_h", source(0, 35, -2.05F, -6, -2, 4, 12, 7, 0, false), 0, 1);
        remap("field_110709_a", source(0, 13, -3, -11, -2, 6, 5, 7, 0.1F, false), 0, 1);
        ModelBox mouth = source(0, 25, -2, -11, -7, 4, 5, 5, 0.1F, false);
        remap("field_178711_b", mouth, 0, 0.6F);
        remap("field_178712_c", mouth, 0.6F, 1);
        remap("field_110705_d", source(19, 16, 0.55F, -13, 4, 2, 3, 1, -0.001F, false), 0, 1);
        remap("field_110706_e", source(19, 16, -2.55F, -13, 4, 2, 3, 1, -0.001F, false), 0, 1);
        remap("field_110714_j", source(56, 36, -1, -11, 5.01F, 2, 16, 2, 0.1F, false), 0, 1);
        leg("field_110688_A", "field_110689_B", "field_110690_C", true, true);
        leg("field_110684_D", "field_110685_E", "field_110686_F", true, false);
        leg("field_110711_o", "field_110719_v", "field_110718_w", false, true);
        leg("field_110722_x", "field_110721_y", "field_110720_z", false, false);
        for (ModelRenderer part : boxList) {
            if (!armoredParts.contains(part)) part.cubeList.clear();
        }
    }

    private void leg(String upper, String shin, String hoof, boolean front, boolean left) {
        ModelBox template = source(48, 21, left ? -3 : -1, -1.01F, front ? -1.9F : -1,
                4, 11, 4, 0.1F, left);
        float upperLength = front ? 8 : 9;
        float length = upperLength + 8.1F;
        remap(upper, template, 0, upperLength / length);
        remap(shin, template, upperLength / length, (upperLength + 5) / length);
        remap(hoof, template, (upperLength + 5.1F) / length, 1);
    }

    private static ModelBox source(int horizontal, int vertical, float positionX, float positionY,
                                   float positionZ, int width, int height, int depth,
                                   float deformation, boolean mirrored) {
        ModelBase atlas = new ModelBase() {};
        atlas.textureWidth = atlas.textureHeight = 64;
        ModelRenderer part = new ModelRenderer(atlas, horizontal, vertical);
        return new ModelBox(part, horizontal, vertical, positionX, positionY, positionZ,
                width, height, depth, deformation, mirrored);
    }

    private void remap(String field, ModelBox template, float start, float end) {
        ModelRenderer part = ObfuscationReflectionHelper.getPrivateValue(ModelHorse.class, this, field);
        armoredParts.add(part);
        ModelBox original = part.cubeList.get(0);
        ModelBox replacement = new ModelBox(part, 0, 0, original.posX1, original.posY1,
                original.posZ1, Math.round(original.posX2 - original.posX1),
                Math.round(original.posY2 - original.posY1), Math.round(original.posZ2 - original.posZ1), 0.04F);
        TexturedQuad[] targetFaces = faces(replacement);
        TexturedQuad[] sourceFaces = faces(template);
        double[][] targetBounds = bounds(targetFaces);
        double[][] sourceBounds = bounds(sourceFaces);
        for (TexturedQuad face : targetFaces) {
            Vec3d normal = normal(face);
            TexturedQuad sourceFace = null;
            for (TexturedQuad candidate : sourceFaces) {
                if (normal.dotProduct(normal(candidate)) > 0.99D) sourceFace = candidate;
            }
            if (sourceFace == null) throw new IllegalStateException("Missing horse armor face: " + field);
            PositionTextureVertex first = sourceFace.vertexPositions[0];
            Vec3d across = sourceFace.vertexPositions[1].vector3D.subtract(first.vector3D);
            Vec3d down = sourceFace.vertexPositions[3].vector3D.subtract(first.vector3D);
            for (int index = 0; index < face.vertexPositions.length; index++) {
                PositionTextureVertex vertex = face.vertexPositions[index];
                double[] values = {vertex.vector3D.x, vertex.vector3D.y, vertex.vector3D.z};
                for (int axis = 0; axis < 3; axis++) {
                    double fraction = (values[axis] - targetBounds[0][axis])
                            / (targetBounds[1][axis] - targetBounds[0][axis]);
                    if (axis == 1) fraction = start + fraction * (end - start);
                    values[axis] = sourceBounds[0][axis]
                            + fraction * (sourceBounds[1][axis] - sourceBounds[0][axis]);
                }
                Vec3d offset = new Vec3d(values[0], values[1], values[2]).subtract(first.vector3D);
                double horizontal = offset.dotProduct(across) / across.lengthSquared();
                double vertical = offset.dotProduct(down) / down.lengthSquared();
                float textureX = (float) (first.texturePositionX
                        + horizontal * (sourceFace.vertexPositions[1].texturePositionX - first.texturePositionX)
                        + vertical * (sourceFace.vertexPositions[3].texturePositionX - first.texturePositionX));
                float textureY = (float) (first.texturePositionY
                        + horizontal * (sourceFace.vertexPositions[1].texturePositionY - first.texturePositionY)
                        + vertical * (sourceFace.vertexPositions[3].texturePositionY - first.texturePositionY));
                face.vertexPositions[index] = vertex.setTexturePosition(textureX, textureY);
            }
        }
        part.cubeList.set(0, replacement);
    }

    private static TexturedQuad[] faces(ModelBox box) {
        return ObfuscationReflectionHelper.getPrivateValue(ModelBox.class, box, "field_78254_i");
    }

    private static Vec3d normal(TexturedQuad face) {
        Vec3d origin = face.vertexPositions[0].vector3D;
        return face.vertexPositions[1].vector3D.subtract(origin)
                .crossProduct(face.vertexPositions[3].vector3D.subtract(origin)).normalize();
    }

    private static double[][] bounds(TexturedQuad[] faces) {
        double[][] result = {{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY}};
        for (TexturedQuad face : faces) {
            for (PositionTextureVertex vertex : face.vertexPositions) {
                double[] values = {vertex.vector3D.x, vertex.vector3D.y, vertex.vector3D.z};
                for (int axis = 0; axis < 3; axis++) {
                    result[0][axis] = Math.min(result[0][axis], values[axis]);
                    result[1][axis] = Math.max(result[1][axis], values[axis]);
                }
            }
        }
        return result;
    }
}
