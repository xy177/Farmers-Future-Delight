package xy177.farmersfuturedelight.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.EnumMap;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.*;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelModernBaby;
import xy177.farmersfuturedelight.client.model.ModelModernBabyArmor;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.FFDMooshroomData;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class RenderModernBaby extends RenderLiving<EntityLiving> {
    private static final Map<String, RenderModernBaby> RENDERERS = new HashMap<>();
    private final String kind;
    private final ModelModernBaby babyModel;
    private final FFDRenderLayerBridge<EntityLiving> externalLayers = new FFDRenderLayerBridge<>();

    private RenderModernBaby(RenderManager manager, String kind) {
        super(manager, new ModelModernBaby(kind), "rabbit_adult".equals(kind) ? 0.3F : 0.25F);
        this.kind = kind;
        babyModel = (ModelModernBaby) mainModel;
        if ("sheep".equals(kind)) addLayer(new BabyOverlay(this, "sheep/sheep_wool_baby", new ModelModernBaby("sheep_wool")));
        if ("wolf".equals(kind)) addLayer(new BabyOverlay(this, "wolf/wolf_collar_baby", babyModel));
        if ("zombie".equals(kind) || "zombie_pigman".equals(kind)) {
            addLayer(new BabyEquipment());
            addLayer(new BabyArmor());
            addLayer(new LayerElytra(this));
        }
    }

    public static String modelKind(EntityLiving entity) {
        Class<?> type = entity.getClass();
        if (type == EntityCow.class || type == EntityMooshroom.class) return "cow";
        if (type == EntitySheep.class) return "sheep";
        if (type == EntityPig.class) return "pig";
        if (type == EntityWolf.class) return "wolf";
        if (type == EntityOcelot.class) return "ocelot";
        if (type == EntityChicken.class) return "chicken";
        if (type == EntityRabbit.class) return entity.isChild() ? "rabbit" : "rabbit_adult";
        if (type == EntityPolarBear.class) return "polar_bear";
        if (type == EntityHorse.class || type == EntityZombieHorse.class || type == EntitySkeletonHorse.class) return "horse";
        if (type == EntityDonkey.class || type == EntityMule.class) return "donkey";
        if (type == EntityLlama.class) return "llama";
        if (type == EntityZombie.class || type == EntityHusk.class) return "zombie";
        if (type == EntityPigZombie.class) return "zombie_pigman";
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderBaby(RenderLivingEvent.Pre<?> event) {
        boolean adultRabbit = event.getEntity().getClass() == EntityRabbit.class && !event.getEntity().isChild();
        if (event.getEntity().getClass() == EntityOcelot.class && !event.getEntity().isChild()) {
            RenderModernOcelot.renderOcelot(event);
            return;
        }
        if ((!adultRabbit && (!FFDConfig.modernBabyModels || !event.getEntity().isChild()))
                || !(event.getEntity() instanceof EntityLiving)
                || event.getRenderer() instanceof RenderModernBaby
                || !event.getRenderer().getClass().getName().startsWith("net.minecraft.client.renderer.entity.")) return;
        EntityLiving entity = (EntityLiving) event.getEntity();
        String kind = modelKind(entity);
        if (kind == null) return;
        List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "field_177097_h");
        for (LayerRenderer<?> layer : layers) {
            String name = layer.getClass().getName();
            if (!name.startsWith("net.minecraft.client.renderer.entity.")
                    && layer.getClass() != LayerLeatherHorseArmor.class
                    && !FFDRenderLayerBridge.supports(layer)) return;
        }
        RenderModernBaby renderer = RENDERERS.get(kind);
        if (renderer == null) {
            renderer = new RenderModernBaby(Minecraft.getMinecraft().getRenderManager(), kind);
            RENDERERS.put(kind, renderer);
        }
        if (entity instanceof EntityZombie) {
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                ItemStack stack = entity.getItemStackFromSlot(slot);
                if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && stack.getItem() instanceof ItemArmor
                        && (RenderModernZombieVillager.babyArmorMaterial(stack).isEmpty()
                        || ForgeHooksClient.getArmorModel(entity, stack, slot, renderer.babyModel) != renderer.babyModel)) return;
            }
        }
        if (!renderer.externalLayers.synchronize(renderer, layers, renderer.layerRenderers)) return;
        event.setCanceled(true);
        boolean outlines = ObfuscationReflectionHelper.getPrivateValue(Render.class, event.getRenderer(), "field_188301_f");
        renderer.setRenderOutlines(outlines);
        try {
            renderer.doRender(entity, event.getX(), event.getY(), event.getZ(), entity.rotationYaw, event.getPartialRenderTick());
        } finally {
            renderer.setRenderOutlines(false);
        }
    }

    @Override
    public void doRender(EntityLiving entity, double positionX, double positionY, double positionZ, float yaw, float partialTicks) {
        if (entity instanceof EntityWolf && ((EntityWolf) entity).isWolfWet()) {
            float shading = entity.getBrightness() * ((EntityWolf) entity).getShadingWhileWet(partialTicks);
            GlStateManager.color(shading, shading, shading);
        }
        super.doRender(entity, positionX, positionY, positionZ, yaw, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void preRenderCallback(EntityLiving entity, float partialTicks) {
        if (entity instanceof EntityHusk) GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
    }

    @Override
    protected float handleRotationFloat(EntityLiving entity, float partialTicks) {
        if (entity instanceof EntityChicken) {
            EntityChicken chicken = (EntityChicken) entity;
            float flap = chicken.oFlap + (chicken.wingRotation - chicken.oFlap) * partialTicks;
            float speed = chicken.oFlapSpeed + (chicken.destPos - chicken.oFlapSpeed) * partialTicks;
            return (MathHelper.sin(flap) + 1.0F) * speed;
        }
        return super.handleRotationFloat(entity, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLiving entity) {
        String path;
        if (entity instanceof EntityMooshroom) {
            path = "cow/mooshroom_" + (FFDMooshroomData.isBrown((EntityMooshroom) entity) ? "brown" : "red") + "_baby";
        } else if (entity instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) entity;
            path = "wolf/" + (wolf.isTamed() ? "wolf_tame_baby" : wolf.isAngry() ? "wolf_angry_baby" : "wolf_baby");
        } else if (entity instanceof EntityRabbit) {
            EntityRabbit rabbit = (EntityRabbit) entity;
            String[] variants = {"brown", "white", "black", "white_splotched", "gold", "salt"};
            String variant = "Toast".equals(net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(rabbit.getName()))
                    ? "toast" : rabbit.getRabbitType() == 99 ? "caerbannog" : variants[MathHelper.clamp(rabbit.getRabbitType(), 0, 5)];
            path = "rabbit/rabbit_" + variant + (rabbit.isChild() ? "_baby" : "");
        } else if (entity instanceof EntityOcelot) {
            int variant = ((EntityOcelot) entity).getTameSkin();
            path = variant == 0 ? "cat/ocelot_baby" : "cat/cat_" + (variant == 1 ? "black" : variant == 2 ? "red" : "siamese") + "_baby";
        } else if (entity instanceof EntityHorse) {
            return FFDModernHorseTextures.get((EntityHorse) entity, true);
        } else if (entity instanceof EntityZombieHorse) path = "horse/horse_zombie_baby";
        else if (entity instanceof EntitySkeletonHorse) path = "horse/horse_skeleton_baby";
        else if (entity instanceof EntityDonkey) path = "horse/donkey_baby";
        else if (entity instanceof EntityMule) path = "horse/mule_baby";
        else if (entity instanceof EntityLlama) {
            String[] variants = {"creamy", "white", "brown", "gray"};
            path = "llama/llama_" + variants[MathHelper.clamp(((EntityLlama) entity).getVariant(), 0, 3)] + "_baby";
        } else if (entity instanceof EntityPigZombie) path = "piglin/zombified_piglin_baby";
        else if (entity instanceof EntityHusk) path = "zombie/husk_baby";
        else if (entity instanceof EntityZombie) path = "zombie/zombie_baby";
        else if ("cow".equals(kind)) path = "cow/cow_temperate_baby";
        else if ("pig".equals(kind)) path = "pig/pig_temperate_baby";
        else if ("chicken".equals(kind)) path = "chicken/chicken_temperate_baby";
        else if ("polar_bear".equals(kind)) path = "bear/polarbear_baby";
        else path = "sheep/sheep_baby";
        return texture(path);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/" + path + ".png");
    }

    private final class BabyArmor extends LayerBipedArmor {
        private final Map<EntityEquipmentSlot, ModelBiped> models = new EnumMap<>(EntityEquipmentSlot.class);

        private BabyArmor() {
            super(RenderModernBaby.this);
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                    models.put(slot, new ModelModernBabyArmor(kind, slot, babyModel));
                }
            }
        }

        @Override
        protected void initArmor() {
        }

        @Override
        public ModelBiped getModelFromSlot(EntityEquipmentSlot slot) {
            return models.get(slot);
        }

        @Override
        public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EntityEquipmentSlot slot, String overlay) {
            return texture("equipment/humanoid_baby/" + RenderModernZombieVillager.babyArmorMaterial(stack)
                    + (overlay == null ? "" : "_" + overlay));
        }
    }

    private final class BabyEquipment implements LayerRenderer<EntityLiving> {
        @Override
        public void doRenderLayer(EntityLiving entity, float swing, float amount, float partial, float age, float yaw, float pitch, float scale) {
            boolean right = entity.getPrimaryHand() == EnumHandSide.RIGHT;
            renderHeld(entity, right ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand(), EnumHandSide.RIGHT, scale);
            renderHeld(entity, right ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand(), EnumHandSide.LEFT, scale);
            ItemStack head = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (head.isEmpty() || head.getItem() instanceof ItemArmor) return;
            GlStateManager.pushMatrix();
            babyModel.bipedHead.postRender(scale);
            if (head.getItem() == Items.SKULL) {
                GlStateManager.scale(1.1875F, -1.1875F, -1.1875F);
                GameProfile profile = null;
                NBTTagCompound data = head.getTagCompound();
                if (data != null && data.hasKey("SkullOwner", 10)) profile = NBTUtil.readGameProfileFromNBT(data.getCompoundTag("SkullOwner"));
                else if (data != null && data.hasKey("SkullOwner", 8) && !data.getString("SkullOwner").trim().isEmpty()) {
                    profile = TileEntitySkull.updateGameprofile(new GameProfile(null, data.getString("SkullOwner")));
                    data.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
                }
                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, head.getMetadata(), profile, -1, swing);
            } else {
                GlStateManager.translate(0.0F, -0.25F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(0.625F, -0.625F, -0.625F);
                Minecraft.getMinecraft().getItemRenderer().renderItem(entity, head, ItemCameraTransforms.TransformType.HEAD);
            }
            GlStateManager.popMatrix();
        }

        private void renderHeld(EntityLiving entity, ItemStack stack, EnumHandSide hand, float scale) {
            if (stack.isEmpty()) return;
            GlStateManager.pushMatrix();
            babyModel.postRenderArm(scale, hand);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.0625F, -0.28125F);
            boolean left = hand == EnumHandSide.LEFT;
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack,
                    left ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, left);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private static final class BabyOverlay implements LayerRenderer<EntityLiving> {
        private final RenderModernBaby renderer;
        private final ResourceLocation texture;
        private final ModelModernBaby model;

        private BabyOverlay(RenderModernBaby renderer, String texture, ModelModernBaby model) {
            this.renderer = renderer;
            this.texture = texture(texture);
            this.model = model;
        }

        @Override
        public void doRenderLayer(EntityLiving entity, float swing, float amount, float partial, float age, float yaw, float pitch, float scale) {
            if (entity.isInvisible()) return;
            float[] color;
            if (entity instanceof EntitySheep) {
                EntitySheep sheep = (EntitySheep) entity;
                if (sheep.getSheared()) return;
                if ("jeb_".equals(sheep.getCustomNameTag())) {
                    int cycle = sheep.ticksExisted / 25 + sheep.getEntityId();
                    float blend = (sheep.ticksExisted % 25 + partial) / 25.0F;
                    float[] first = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(cycle % 16));
                    float[] second = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata((cycle + 1) % 16));
                    color = new float[]{first[0] * (1 - blend) + second[0] * blend,
                            first[1] * (1 - blend) + second[1] * blend, first[2] * (1 - blend) + second[2] * blend};
                } else color = EntitySheep.getDyeRgb(sheep.getFleeceColor());
            } else {
                EntityWolf wolf = (EntityWolf) entity;
                if (!wolf.isTamed()) return;
                color = wolf.getCollarColor().getColorComponentValues();
            }
            renderer.bindTexture(texture);
            GlStateManager.color(color[0], color[1], color[2], 1.0F);
            model.setModelAttributes(renderer.mainModel);
            model.setLivingAnimations(entity, swing, amount, partial);
            model.render(entity, swing, amount, age, yaw, pitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
