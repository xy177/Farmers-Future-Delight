package xy177.farmersfuturedelight.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.math.MathHelper;

public class ModelModernBaby extends ModelBiped {
    private final Map<String, ModelRenderer> parts = new LinkedHashMap<>();
    private final Map<ModelRenderer, float[]> poses = new LinkedHashMap<>();
    private final ModelRenderer empty = new ModelRenderer(this);
    private final ModelRenderer root;
    private float partialTicks;
    private final BabyKeyframeAnimation hopAnimation;
    private final BabyKeyframeAnimation idleAnimation;
    private final boolean adultRabbit;
    private final Map<EntityRabbit, RabbitAnimationState> rabbitStates = new WeakHashMap<>();

    public ModelModernBaby(String name) {
        adultRabbit = "rabbit_adult".equals(name);
        hopAnimation = adultRabbit ? new BabyKeyframeAnimation("rabbit", "hop")
                : "rabbit".equals(name) ? new BabyKeyframeAnimation("rabbit_hop") : null;
        idleAnimation = adultRabbit ? new BabyKeyframeAnimation("rabbit", "idle_head_tilt")
                : "rabbit".equals(name) ? new BabyKeyframeAnimation("rabbit_idle_head_tilt") : null;
        String modelPath = adultRabbit ? "rabbit/adult" : "baby/" + name;
        try (InputStream stream = ModelModernBaby.class.getResourceAsStream(
                "/assets/farmers_future_delight/models/entity/" + modelPath + ".json")) {
            if (stream == null) throw new IllegalArgumentException("Missing baby model: " + name);
            JsonObject data = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            root = load("root", data);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to load baby model: " + name, exception);
        }
        bipedHead = part("head");
        bipedHeadwear = part("hat");
        bipedBody = part("body");
        bipedRightArm = part("right_arm");
        bipedLeftArm = part("left_arm");
        bipedRightLeg = part("right_leg");
        bipedLeftLeg = part("left_leg");
    }

    private ModelRenderer load(String name, JsonObject data) {
        ModelRenderer result = new ModelRenderer(this);
        parts.put(name, result);
        float[] pose = new float[6];
        for (int index = 0; index < pose.length; index++) pose[index] = data.getAsJsonArray("pose").get(index).getAsFloat();
        poses.put(result, pose);
        for (JsonElement cube : data.getAsJsonArray("cubes")) result.cubeList.add(new BakedBox(result, cube.getAsJsonArray()));
        for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("children").entrySet()) {
            result.addChild(load(entry.getKey(), entry.getValue().getAsJsonObject()));
        }
        reset(result, pose);
        return result;
    }

    public ModelRenderer part(String name) {
        ModelRenderer result = parts.get(name);
        return result == null ? empty : result;
    }

    private void reset(ModelRenderer part, float[] pose) {
        part.setRotationPoint(pose[0], pose[1], pose[2]);
        part.rotateAngleX = pose[3];
        part.rotateAngleY = pose[4];
        part.rotateAngleZ = pose[5];
        part.showModel = true;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float swing, float amount, float partial) {
        partialTicks = partial;
    }

    @Override
    public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(swing, amount, age, yaw, pitch, scale, entity);
        renderPrepared(scale);
    }

    protected void renderPrepared(float scale) {
        root.render(scale);
    }

    protected void copyPose(ModelModernBaby source) {
        for (Map.Entry<String, ModelRenderer> entry : parts.entrySet()) {
            ModelRenderer original = source.parts.get(entry.getKey());
            if (original == null) continue;
            ModelRenderer destination = entry.getValue();
            reset(destination, poses.get(destination));
            float[] origin = source.poses.get(original);
            destination.rotationPointX += original.rotationPointX - origin[0];
            destination.rotationPointY += original.rotationPointY - origin[1];
            destination.rotationPointZ += original.rotationPointZ - origin[2];
            destination.rotateAngleX += original.rotateAngleX - origin[3];
            destination.rotateAngleY += original.rotateAngleY - origin[4];
            destination.rotateAngleZ += original.rotateAngleZ - origin[5];
        }
    }

    @Override
    public void setRotationAngles(float swing, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
        for (Map.Entry<ModelRenderer, float[]> entry : poses.entrySet()) reset(entry.getKey(), entry.getValue());
        part("head").rotateAngleX += pitch * 0.017453292F;
        part("head").rotateAngleY += yaw * 0.017453292F;
        float walk = MathHelper.cos(swing * 0.6662F) * 1.4F * amount;
        float opposite = MathHelper.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
        if (!(entity instanceof EntityRabbit) && !(entity instanceof AbstractHorse)) {
            part("right_hind_leg").rotateAngleX += walk;
            part("left_hind_leg").rotateAngleX += opposite;
            part("right_front_leg").rotateAngleX += opposite;
            part("left_front_leg").rotateAngleX += walk;
        }
        if (entity instanceof net.minecraft.entity.passive.EntityLlama) {
            part("right_hind_leg").rotateAngleX = walk;
            part("left_hind_leg").rotateAngleX = opposite;
            part("right_front_leg").rotateAngleX = opposite;
            part("left_front_leg").rotateAngleX = walk;
        } else if (entity instanceof AbstractHorse) animateHorse((AbstractHorse) entity, swing, amount, age, yaw, pitch);
        if (entity instanceof EntityChicken) {
            part("right_leg").rotateAngleX = walk;
            part("left_leg").rotateAngleX = opposite;
            part("right_wing").rotateAngleZ = age;
            part("left_wing").rotateAngleZ = -age;
        }
        if (entity instanceof EntitySheep) {
            EntitySheep sheep = (EntitySheep) entity;
            part("head").rotationPointY += sheep.getHeadRotationPointY(partialTicks) * 4.5F;
            part("head").rotateAngleX = sheep.getHeadRotationAngleX(partialTicks);
        }
        if (entity instanceof EntityWolf) animateWolf((EntityWolf) entity, swing, amount);
        if (entity instanceof EntityOcelot) animateOcelot((EntityOcelot) entity, swing, amount);
        if (entity instanceof EntityPolarBear) animateBear((EntityPolarBear) entity);
        if (entity instanceof EntityRabbit) animateRabbit((EntityRabbit) entity);
        if (entity instanceof EntityZombie) animateZombie((EntityZombie) entity, swing, amount, age);
        part("left_chest").showModel = false;
        part("right_chest").showModel = false;
    }

    private void animateWolf(EntityWolf wolf, float swing, float amount) {
        part("tail").rotateAngleY = wolf.isAngry() ? 0.0F : MathHelper.cos(swing * 0.6662F) * 1.4F * amount;
        part("tail").rotateAngleX = wolf.getTailRotation();
        if (wolf.isSitting()) {
            part("body").rotationPointY += 2.0F;
            part("body").rotationPointZ -= 1.0F;
            part("body").rotateAngleX = -0.7853982F;
            part("tail").rotationPointY += 4.5F;
            part("tail").rotationPointZ -= 1.0F;
            for (String leg : new String[]{"right_hind_leg", "left_hind_leg"}) {
                part(leg).rotationPointY += 3.35F;
                part(leg).rotationPointZ -= 2.5F;
                part(leg).rotateAngleX = 4.712389F;
            }
            for (String leg : new String[]{"right_front_leg", "left_front_leg"}) {
                part(leg).rotateAngleX = 5.811947F;
                part(leg).rotationPointY += 0.5F;
            }
            part("right_front_leg").rotationPointX += 0.005F;
            part("left_front_leg").rotationPointX -= 0.005F;
        }
        part("body").rotateAngleZ = wolf.getShakeAngle(partialTicks, -0.16F);
        part("head").rotateAngleZ = wolf.getInterestedAngle(partialTicks) + wolf.getShakeAngle(partialTicks, 0.0F);
        part("tail").rotateAngleZ = wolf.getShakeAngle(partialTicks, -0.2F);
    }

    private void animateOcelot(EntityOcelot cat, float swing, float amount) {
        if (cat.isSneaking()) {
            part("body").rotationPointY += 0.5F;
            part("head").rotationPointY += 1.0F;
            part("tail1").rotationPointY += 0.5F;
            part("tail2").rotationPointY -= 2.0F;
            part("tail2").rotationPointZ += 1.0F;
            part("tail1").rotateAngleX = 1.5707964F;
            part("tail2").rotateAngleX = 1.5707964F;
        } else if (cat.isSprinting()) {
            part("tail2").rotationPointY = part("tail1").rotationPointY;
            part("tail2").rotationPointZ += 1.0F;
            part("tail1").rotateAngleX = 1.5707964F;
            part("tail2").rotateAngleX = 1.5707964F;
        }
        if (cat.isSitting()) {
            part("body").rotateAngleX -= 0.43633232F;
            part("body").rotationPointY += 1.25F;
            part("head").rotationPointZ += 0.75F;
            part("tail1").rotateAngleX += 0.5454154F;
            part("tail1").rotationPointY += 4.0F;
            part("tail1").rotationPointZ -= 0.9F;
            part("left_hind_leg").rotationPointZ -= 0.9F;
            part("right_hind_leg").rotationPointZ -= 0.9F;
        } else {
            float sprint = cat.isSprinting() ? 0.3F : (float) Math.PI;
            part("left_hind_leg").rotateAngleX = MathHelper.cos(swing * 0.6662F) * amount;
            part("right_hind_leg").rotateAngleX = MathHelper.cos(swing * 0.6662F + sprint) * amount;
            part("left_front_leg").rotateAngleX = MathHelper.cos(swing * 0.6662F + (float) Math.PI
                    + (cat.isSprinting() ? 0.3F : 0.0F)) * amount;
            part("right_front_leg").rotateAngleX = MathHelper.cos(swing * 0.6662F
                    + (cat.isSprinting() ? (float) Math.PI : 0.0F)) * amount;
            part("tail2").rotateAngleX = 1.7278761F + (cat.isSprinting() ? 0.31415927F
                    : cat.isSneaking() ? 0.47123894F : 0.7853982F) * MathHelper.cos(swing) * amount;
        }
    }

    private void animateBear(EntityPolarBear bear) {
        float standing = bear.getStandingAnimationScale(partialTicks);
        standing *= standing;
        part("body").rotateAngleX -= standing * (float) Math.PI * 0.35F;
        part("body").rotationPointY += standing;
        for (String leg : new String[]{"right_front_leg", "left_front_leg"}) {
            part(leg).rotationPointY -= standing * 10.0F;
            part(leg).rotationPointZ += standing * 2.0F;
            part(leg).rotateAngleX -= standing * (float) Math.PI * 0.45F;
        }
        part("head").rotationPointY -= standing * 24.0F;
        part("head").rotationPointZ += standing * 13.0F;
        part("head").rotateAngleX += standing * (float) Math.PI * 0.15F;
    }

    private void animateRabbit(EntityRabbit rabbit) {
        if (adultRabbit) {
            part("head").rotateAngleX = part("head").rotateAngleX - poses.get(part("head"))[3];
            part("head").rotateAngleY = part("head").rotateAngleY - poses.get(part("head"))[4];
            ModernRabbitAnimationState state = ModernRabbitAnimationState.get(rabbit);
            if (state.idleStart >= 0) {
                part("head").rotateAngleX = poses.get(part("head"))[3];
                part("head").rotateAngleY = poses.get(part("head"))[4];
                idleAnimation.apply(this, rabbit.ticksExisted + partialTicks - state.idleStart);
            }
            if (state.hopping) hopAnimation.apply(this,
                    MathHelper.clamp(rabbit.setJumpCompletion(partialTicks), 0.0F, 0.99999F) * 15.0F);
            return;
        }
        RabbitAnimationState state = rabbitStates.computeIfAbsent(rabbit, ignored -> new RabbitAnimationState());
        float jump = rabbit.setJumpCompletion(0.0F);
        if (state.lastTick != rabbit.ticksExisted) {
            int elapsed = state.lastTick < 0 ? 1 : Math.max(1, rabbit.ticksExisted - state.lastTick);
            if (state.idleTimeout <= 0 && !rabbit.getLeashed() && !rabbit.isAIDisabled()) {
                state.idleTimeout = rabbit.getRNG().nextInt(40) + 180;
                state.idleStart = rabbit.ticksExisted;
            } else if (jump > 0.0F) {
                if (state.hopStart < 0 || jump < state.previousJump) state.hopStart = rabbit.ticksExisted;
                state.idleStart = -1;
            } else {
                state.idleTimeout -= elapsed;
                state.hopStart = -1;
            }
            state.lastTick = rabbit.ticksExisted;
            state.previousJump = jump;
        }
        if (rabbit.getLeashed()) state.idleStart = -1;
        if (state.idleStart >= 0) {
            part("head").rotateAngleX = poses.get(part("head"))[3];
            part("head").rotateAngleY = poses.get(part("head"))[4];
            idleAnimation.apply(this, rabbit.ticksExisted + partialTicks - state.idleStart);
        }
        if (state.hopStart >= 0) hopAnimation.apply(this, rabbit.ticksExisted + partialTicks - state.hopStart);
    }

    private static final class RabbitAnimationState {
        private int lastTick = -1;
        private int idleTimeout;
        private int idleStart = -1;
        private int hopStart = -1;
        private float previousJump;
    }

    private void animateHorse(AbstractHorse horse, float swing, float amount, float age, float yaw, float pitch) {
        boolean donkey = horse instanceof EntityDonkey || horse instanceof EntityMule;
        float eating = horse.getGrassEatingAmount(partialTicks);
        float standing = horse.getRearingAmount(partialTicks);
        float feeding = horse.getMouthOpennessAngle(partialTicks);
        float inverse = 1.0F - standing;
        float headPitch = donkey ? -0.5235988F : pitch * 0.017453292F;
        if (!donkey && amount > 0.2F) headPitch += MathHelper.cos(swing * 0.8F) * 0.15F * amount;
        ModelRenderer neck = part("head_parts");
        part("head").rotateAngleX = poses.get(part("head"))[3];
        part("head").rotateAngleY = poses.get(part("head"))[4];
        neck.rotateAngleX = standing * (0.2617994F + headPitch)
                + eating * ((donkey ? 1.5707964F : 2.1816616F) + MathHelper.sin(age) * 0.05F)
                + (1.0F - Math.max(standing, eating)) * (0.5235988F + headPitch + feeding * MathHelper.sin(age) * 0.05F);
        neck.rotateAngleY = (standing + 1.0F - Math.max(standing, eating)) * MathHelper.clamp(yaw, -20.0F, 20.0F) * 0.017453292F;
        if (donkey) {
            neck.rotationPointY += eating * (-1.2F - neck.rotationPointY);
            neck.rotationPointZ += standing * (-3.6F - neck.rotationPointZ);
        } else {
            neck.rotationPointY += -2.0F * standing * (1.0F - eating) + 2.0F * eating;
            neck.rotationPointZ += standing * (-4.0F - neck.rotationPointZ);
        }
        part("body").rotateAngleX = -0.7853982F * standing + part("body").rotateAngleX * inverse;
        part("left_front_leg").rotationPointY -= (donkey ? 1.0F : 4.0F) * standing;
        part("left_front_leg").rotationPointZ += (donkey ? 0.5F : 0.0F) * standing;
        part("right_front_leg").rotationPointY = part("left_front_leg").rotationPointY;
        part("right_front_leg").rotationPointZ = part("left_front_leg").rotationPointZ;
        float leg = MathHelper.cos((horse.isInWater() ? 0.2F : 1.0F) * swing * 0.6662F + (float) Math.PI);
        float bob = MathHelper.cos(age * 0.6F + (float) Math.PI);
        float offset = donkey ? 0.0F : -1.0471976F;
        float standAngle = (donkey ? 1.0471976F : 0.2617994F) * standing;
        part("left_hind_leg").rotateAngleX = standAngle - leg * 0.5F * amount * inverse;
        part("right_hind_leg").rotateAngleX = standAngle + leg * 0.5F * amount * inverse;
        part("left_front_leg").rotateAngleX = (offset + bob) * standing + leg * 0.8F * amount * inverse;
        part("right_front_leg").rotateAngleX = (offset - bob) * standing - leg * 0.8F * amount * inverse;
        if (donkey) {
            part("left_hind_leg").rotationPointY += standing * (-0.3F - part("left_hind_leg").rotationPointY);
            part("right_hind_leg").rotationPointY = part("left_hind_leg").rotationPointY
                    + standing * (-0.3F - part("left_hind_leg").rotationPointY);
        }
        part("tail").rotateAngleX = (donkey ? -0.7853982F : -1.5707964F) + 0.5235988F + amount * 0.75F;
        part("tail").rotationPointY += amount * 0.5F;
        part("tail").rotationPointZ += amount;
        part("tail").rotateAngleY = horse.tailCounter > 0 ? MathHelper.cos(age * 0.7F) : 0.0F;
    }

    private void animateZombie(EntityZombie zombie, float swing, float amount, float age) {
        bipedRightLeg.rotateAngleX = MathHelper.cos(swing * 0.6662F) * 1.4F * amount;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
        boolean raisedArms = zombie.getHeldItemMainhand().isEmpty();
        float attack = (raisedArms ? 1.0F : -1.0F) * MathHelper.sin(swingProgress * (float) Math.PI);
        float recovery = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float) Math.PI);
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightArm.rotateAngleY = (raisedArms ? -1.0F : 1.0F) * (0.1F - attack * 0.6F);
        bipedLeftArm.rotateAngleY = -bipedRightArm.rotateAngleY;
        float raised = raisedArms ? -(float) Math.PI / (zombie.isArmsRaised() ? 1.5F : 2.25F) : 0.0F;
        bipedRightArm.rotateAngleX = raised + attack * 1.2F - recovery * 0.4F;
        bipedLeftArm.rotateAngleX = raised + attack * 1.2F - recovery * 0.4F;
        bipedRightArm.rotateAngleZ += MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(age * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(age * 0.067F) * 0.05F;
        if (zombie instanceof EntityPigZombie) {
            float frequency = age * 0.1F + swing * 0.5F;
            float amplitude = 0.08F + amount * 0.4F;
            part("left_ear").rotateAngleZ = -0.08726646F - MathHelper.cos(frequency * 1.2F) * amplitude;
            part("right_ear").rotateAngleZ = 0.08726646F + MathHelper.cos(frequency) * amplitude;
        }
    }

    private static final class BakedBox extends ModelBox {
        private final TexturedQuad[] faces;

        private BakedBox(ModelRenderer renderer, JsonArray data) {
            super(renderer, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            faces = new TexturedQuad[data.size()];
            for (int faceIndex = 0; faceIndex < data.size(); faceIndex++) {
                JsonArray vertices = data.get(faceIndex).getAsJsonObject().getAsJsonArray("vertices");
                PositionTextureVertex[] quad = new PositionTextureVertex[4];
                for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                    JsonArray vertex = vertices.get(vertexIndex).getAsJsonArray();
                    quad[vertexIndex] = new PositionTextureVertex(vertex.get(0).getAsFloat(), vertex.get(1).getAsFloat(),
                            vertex.get(2).getAsFloat(), vertex.get(3).getAsFloat(), vertex.get(4).getAsFloat());
                }
                faces[faceIndex] = new TexturedQuad(quad);
            }
        }

        @Override
        public void render(BufferBuilder buffer, float scale) {
            for (TexturedQuad face : faces) face.draw(buffer, scale);
        }
    }
}
