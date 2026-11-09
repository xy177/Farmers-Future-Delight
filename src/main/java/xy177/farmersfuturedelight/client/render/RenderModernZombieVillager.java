package xy177.farmersfuturedelight.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombieVillager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDZombieVillagerState;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class RenderModernZombieVillager extends RenderLiving<EntityZombieVillager> {
    private static final String[] BADGES = {"stone", "iron", "gold", "emerald", "diamond"};
    private static final Map<ResourceLocation, String> HATS = new HashMap<>();
    private static RenderModernZombieVillager adultRenderer;
    private static RenderModernZombieVillager babyRenderer;
    private static boolean reloadRegistered;
    private final ModelModernZombieVillager model;
    private final FFDRenderLayerBridge<EntityZombieVillager> externalLayers = new FFDRenderLayerBridge<>();

    private RenderModernZombieVillager(RenderManager manager, boolean baby) {
        super(manager, new ModelModernZombieVillager(baby), 0.5F);
        model = (ModelModernZombieVillager) getMainModel();
        addLayer(new ProfessionLayer());
        addLayer(new HeadLayer());
        addLayer(new WingsLayer());
        addLayer(new HeldItemLayer());
        addLayer(new ArmorLayer(baby));
        if (!reloadRegistered && Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(resourceManager -> HATS.clear());
            reloadRegistered = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderModern(RenderLivingEvent.Pre<?> event) {
        if (event.getEntity().getClass() != EntityZombieVillager.class
                || event.getRenderer().getClass() != RenderZombieVillager.class
                || event.getRenderer().getMainModel().getClass() != ModelZombieVillager.class) return;
        EntityZombieVillager zombie = (EntityZombieVillager) event.getEntity();
        if (zombie.isChild() && !xy177.farmersfuturedelight.common.FFDConfig.modernBabyModels) return;
        if (!FFDZombieVillagerState.canRender(zombie) || !hasVanillaLayers(event.getRenderer())) return;
        if (zombie.isChild()) {
            for (EntityEquipmentSlot slot : new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
                ItemStack stack = zombie.getItemStackFromSlot(slot);
                if (stack.getItem() instanceof ItemArmor && babyArmorMaterial(stack).isEmpty()) return;
            }
        }
        if (adultRenderer == null) {
            adultRenderer = new RenderModernZombieVillager(Minecraft.getMinecraft().getRenderManager(), false);
            babyRenderer = new RenderModernZombieVillager(Minecraft.getMinecraft().getRenderManager(), true);
        }
        RenderModernZombieVillager renderer = zombie.isChild() ? babyRenderer : adultRenderer;
        List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "field_177097_h");
        if (!renderer.externalLayers.synchronize(renderer, layers, renderer.layerRenderers)) return;
        event.setCanceled(true);
        boolean outlines = ObfuscationReflectionHelper.getPrivateValue(Render.class,
                event.getRenderer(), "field_188301_f");
        renderer.setRenderOutlines(outlines);
        try {
            renderer.doRender(zombie, event.getX(), event.getY(), event.getZ(),
                    zombie.rotationYaw, event.getPartialRenderTick());
        } finally {
            renderer.setRenderOutlines(false);
        }
    }

    private static boolean hasVanillaLayers(RenderLivingBase<?> renderer) {
        List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class,
                renderer, "field_177097_h");
        Set<Class<?>> expected = new HashSet<>(Arrays.asList(LayerCustomHead.class, LayerElytra.class,
                LayerHeldItem.class, LayerVillagerArmor.class));
        for (LayerRenderer<?> layer : layers) {
            if (FFDRenderLayerBridge.supports(layer)) continue;
            if (!expected.remove(layer.getClass())) return false;
        }
        return expected.isEmpty();
    }

    private static NBTTagCompound state(EntityZombieVillager zombie) {
        return zombie.getEntityData().getCompoundTag(FFDZombieVillagerState.CLIENT_STATE);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/zombie_villager/" + path + ".png");
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityZombieVillager zombie) {
        return texture(zombie.isChild() ? "zombie_villager_baby" : "zombie_villager");
    }

    @Override
    protected void applyRotations(EntityZombieVillager zombie, float age, float yaw, float partialTicks) {
        if (zombie.isConverting()) {
            yaw += (float) (Math.cos(MathHelper.floor(age) * 3.25F) * Math.PI * 0.4F);
        }
        super.applyRotations(zombie, age, yaw, partialTicks);
    }

    private static String hat(ResourceLocation location) {
        return HATS.computeIfAbsent(location, texture -> {
            ResourceLocation metadata = new ResourceLocation(texture.getResourceDomain(),
                    texture.getResourcePath() + ".mcmeta");
            try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(metadata);
                 InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
                return root.has("villager") && root.getAsJsonObject("villager").has("hat")
                        ? root.getAsJsonObject("villager").get("hat").getAsString() : "none";
            } catch (Exception exception) {
                return "none";
            }
        });
    }

    static String babyArmorMaterial(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemArmor)) return "";
        ItemArmor armor = (ItemArmor) stack.getItem();
        if (armor == FFDItems.NETHERITE_HELMET || armor == FFDItems.NETHERITE_CHESTPLATE
                || armor == FFDItems.NETHERITE_LEGGINGS || armor == FFDItems.NETHERITE_BOOTS) return "netherite";
        if (armor == FFDItems.TURTLE_HELMET) return "turtle_scute";
        ResourceLocation id = armor.getRegistryName();
        if (armor.getClass() != ItemArmor.class || id == null || !"minecraft".equals(id.getResourceDomain())) return "";
        switch (armor.getArmorMaterial()) {
            case LEATHER: return "leather";
            case CHAIN: return "chainmail";
            case IRON: return "iron";
            case GOLD: return "gold";
            case DIAMOND: return "diamond";
            default: return "";
        }
    }

    private final class ProfessionLayer implements LayerRenderer<EntityZombieVillager> {
        @Override
        public void doRenderLayer(EntityZombieVillager zombie, float limbSwing, float limbAmount,
                                  float partialTicks, float age, float yaw, float pitch, float scale) {
            if (zombie.isInvisible()) return;
            NBTTagCompound state = state(zombie);
            String type = state.getString("type");
            String profession = state.getString("profession");
            ResourceLocation typeTexture = texture((zombie.isChild() ? "baby/" : "type/") + type);
            ResourceLocation professionTexture = texture("profession/" + profession);
            String professionHat = "none".equals(profession) ? "none" : hat(professionTexture);
            boolean showTypeHead = "none".equals(professionHat)
                    || "partial".equals(professionHat) && !"full".equals(hat(texture("type/" + type)));
            model.bipedHead.showModel = showTypeHead;
            try {
                bindTexture(typeTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                model.render(zombie, limbSwing, limbAmount, age, yaw, pitch, scale);
            } finally {
                model.bipedHead.showModel = true;
            }
            if (zombie.isChild() || "none".equals(profession)) return;
            bindTexture(professionTexture);
            model.render(zombie, limbSwing, limbAmount, age, yaw, pitch, scale);
            if ("nitwit".equals(profession)) return;
            int level = Math.max(1, Math.min(5, state.getInteger("level")));
            bindTexture(texture("profession_level/" + BADGES[level - 1]));
            model.render(zombie, limbSwing, limbAmount, age, yaw, pitch, scale);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    private final class HeldItemLayer implements LayerRenderer<EntityZombieVillager> {
        @Override
        public void doRenderLayer(EntityZombieVillager zombie, float limbSwing, float limbAmount,
                                  float partialTicks, float age, float yaw, float pitch, float scale) {
            boolean right = zombie.getPrimaryHand() == EnumHandSide.RIGHT;
            renderItem(zombie, right ? zombie.getHeldItemMainhand() : zombie.getHeldItemOffhand(),
                    EnumHandSide.RIGHT, scale);
            renderItem(zombie, right ? zombie.getHeldItemOffhand() : zombie.getHeldItemMainhand(),
                    EnumHandSide.LEFT, scale);
        }

        private void renderItem(EntityZombieVillager zombie, ItemStack stack, EnumHandSide side, float scale) {
            if (stack.isEmpty()) return;
            GlStateManager.pushMatrix();
            model.postRenderArm(scale, side);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            boolean left = side == EnumHandSide.LEFT;
            GlStateManager.translate(zombie.isChild() ? 0.0F : (left ? -1.0F : 1.0F) / 16.0F,
                    zombie.isChild() ? 1.0F / 16.0F : 2.0F / 16.0F,
                    zombie.isChild() ? -4.5F / 16.0F : -10.0F / 16.0F);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(zombie, stack,
                    left ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                            : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, left);
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private final class HeadLayer implements LayerRenderer<EntityZombieVillager> {
        @Override
        public void doRenderLayer(EntityZombieVillager zombie, float limbSwing, float limbAmount,
                                  float partialTicks, float age, float yaw, float pitch, float scale) {
            ItemStack stack = zombie.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (stack.isEmpty() || stack.getItem() instanceof ItemArmor
                    && ((ItemArmor) stack.getItem()).getEquipmentSlot() == EntityEquipmentSlot.HEAD) return;
            GlStateManager.pushMatrix();
            model.bipedHead.postRender(scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (stack.getItem() == Items.SKULL) {
                GlStateManager.translate(0.0F, -0.07421875F, 0.0F);
                GlStateManager.scale(1.1875F, -1.1875F, -1.1875F);
                GameProfile profile = null;
                NBTTagCompound data = stack.getTagCompound();
                if (data != null && data.hasKey("SkullOwner", 10)) {
                    profile = NBTUtil.readGameProfileFromNBT(data.getCompoundTag("SkullOwner"));
                } else if (data != null && data.hasKey("SkullOwner", 8)
                        && !data.getString("SkullOwner").trim().isEmpty()) {
                    profile = TileEntitySkull.updateGameprofile(new GameProfile((UUID) null,
                            data.getString("SkullOwner")));
                    data.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
                }
                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F,
                        stack.getMetadata(), profile, -1, limbSwing);
            } else {
                GlStateManager.translate(0.0F, -0.25F - 0.1171875F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(0.625F, -0.625F, -0.625F);
                Minecraft.getMinecraft().getItemRenderer().renderItem(zombie, stack,
                        ItemCameraTransforms.TransformType.HEAD);
            }
            GlStateManager.popMatrix();
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }

    private final class WingsLayer extends LayerElytra {
        private WingsLayer() {
            super(RenderModernZombieVillager.this);
        }

        @Override
        public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbAmount,
                                  float partialTicks, float age, float yaw, float pitch, float scale) {
            GlStateManager.pushMatrix();
            try {
                if (entity.isChild()) GlStateManager.translate(0.0F, 0.0005F, 0.05F);
                super.doRenderLayer(entity, limbSwing, limbAmount, partialTicks, age, yaw, pitch, scale);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    private final class ArmorLayer extends LayerBipedArmor {
        private final Map<EntityEquipmentSlot, ModelBiped> models = new EnumMap<>(EntityEquipmentSlot.class);
        private final boolean baby;

        private ArmorLayer(boolean baby) {
            super(RenderModernZombieVillager.this);
            this.baby = baby;
            for (EntityEquipmentSlot slot : new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
                models.put(slot, new ModelModernZombieVillagerArmor(baby, slot));
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
        public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EntityEquipmentSlot slot,
                                                 String type) {
            if (baby) {
                String material = babyArmorMaterial(stack);
                if (!material.isEmpty()) return texture("armor/baby/" + material
                        + (type == null ? "" : "_" + type));
            }
            return super.getArmorResource(entity, stack, slot, type);
        }
    }
}
