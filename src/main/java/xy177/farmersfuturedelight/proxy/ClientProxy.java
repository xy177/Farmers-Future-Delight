package xy177.farmersfuturedelight.proxy;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.particle.ParticleGlowSquid;
import xy177.farmersfuturedelight.client.particle.ParticleHoneyDrip;
import xy177.farmersfuturedelight.client.particle.ParticleSporeBlossom;
import xy177.farmersfuturedelight.client.render.RenderBee;
import xy177.farmersfuturedelight.client.render.RenderGlowSquid;
import xy177.farmersfuturedelight.client.render.RenderTurtle;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.block.BlockKelp;
import xy177.farmersfuturedelight.common.block.BlockKelpYoung;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (FFDItems.isSweetBerryEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.SWEET_BERRIES, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":sweet_berries", "inventory"));
        }
        if (FFDItems.isMossEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.MOSS_BLOCK, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":moss_block", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.MOSS_CARPET, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":moss_carpet", "inventory"));
        }
        if (FFDItems.isGlowBerryEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.GLOW_BERRIES, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":glow_berries", "inventory"));
        }
        if (FFDItems.isAzaleaEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.AZALEA, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":azalea", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.FLOWERING_AZALEA, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":flowering_azalea", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.AZALEA_LEAVES, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":azalea_leaves", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.FLOWERING_AZALEA_LEAVES, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":flowering_azalea_leaves", "inventory"));
        }
        if (FFDItems.isDripleafEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.SMALL_DRIPLEAF, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":small_dripleaf", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.BIG_DRIPLEAF, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":big_dripleaf", "inventory"));
        }
        if (FFDItems.isHangingRootsEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.HANGING_ROOTS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockHangingRoots.WATERLOGGED).build());
        }
        if (FFDItems.isRootedDirtEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.ROOTED_DIRT, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":rooted_dirt", "inventory"));
        }
        if (FFDItems.isHangingRootsEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.HANGING_ROOTS, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":hanging_roots", "inventory"));
        }
        if (FFDItems.isSporeBlossomEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.SPORE_BLOSSOM, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":spore_blossom", "inventory"));
        }
        if (FFDItems.isGlowLichenEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.GLOW_LICHEN, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":glow_lichen", "inventory"));
            for (net.minecraft.block.Block block : FFDBlocks.GLOW_LICHEN_VARIANTS) {
                ModelLoader.setCustomStateMapper(block,
                        new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            }
        }
        if (FFDEntities.isGlowSquidEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.GLOW_INK_SAC, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":glow_ink_sac", "inventory"));
        }
        if (FFDItems.isKelpEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.KELP, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":kelp", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.DRIED_KELP, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":dried_kelp", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.DRIED_KELP_BLOCK, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":dried_kelp_block", "inventory"));
        }
        if (FFDItems.isSeagrassEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.SEAGRASS, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":seagrass", "inventory"));
        }
        if (FFDItems.isSeaPickleEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.SEA_PICKLE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":sea_pickle", "inventory"));
        }
        if (FFDItems.isTurtleEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.TURTLE_EGG, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":turtle_egg", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.TURTLE_SCUTE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":turtle_scute", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.TURTLE_HELMET, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":turtle_helmet", "inventory"));
        }
        if (FFDItems.isHoneyEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.HONEY_BOTTLE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":honey_bottle", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.HONEYCOMB, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":honeycomb", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.HONEY_BLOCK, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":honey_block", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.HONEYCOMB_BLOCK, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":honeycomb_block", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.BEE_NEST, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":bee_nest", "inventory"));
            ModelLoader.setCustomModelResourceLocation(FFDItems.BEEHIVE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":beehive", "inventory"));
        }
        if (FFDItems.isCrimsonEnabled()) {
            registerModel(FFDItems.CRIMSON_NYLIUM, "crimson_nylium");
            registerModel(FFDItems.CRIMSON_FUNGUS, "crimson_fungus");
            registerModel(FFDItems.CRIMSON_ROOTS, "crimson_roots");
            registerModel(FFDItems.WEEPING_VINES, "weeping_vines");
        }
        if (FFDItems.isWarpedEnabled()) {
            registerModel(FFDItems.WARPED_NYLIUM, "warped_nylium");
            registerModel(FFDItems.WARPED_FUNGUS, "warped_fungus");
            registerModel(FFDItems.WARPED_ROOTS, "warped_roots");
            registerModel(FFDItems.NETHER_SPROUTS, "nether_sprouts");
            registerModel(FFDItems.TWISTING_VINES, "twisting_vines");
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            registerModel(FFDItems.CRIMSON_STEM, "crimson_stem");
            registerModel(FFDItems.STRIPPED_CRIMSON_STEM, "stripped_crimson_stem");
            registerModel(FFDItems.CRIMSON_HYPHAE, "crimson_hyphae");
            registerModel(FFDItems.STRIPPED_CRIMSON_HYPHAE, "stripped_crimson_hyphae");
            registerModel(FFDItems.NETHER_WART_BLOCK, "nether_wart_block");
            registerModel(FFDItems.CRIMSON_PLANKS, "crimson_planks");
            registerModel(FFDItems.CRIMSON_STAIRS, "crimson_stairs");
            registerModel(FFDItems.CRIMSON_SLAB, "crimson_slab");
            registerModel(FFDItems.CRIMSON_FENCE, "crimson_fence");
            registerModel(FFDItems.CRIMSON_FENCE_GATE, "crimson_fence_gate");
            registerModel(FFDItems.CRIMSON_DOOR, "crimson_door");
            registerModel(FFDItems.CRIMSON_TRAPDOOR, "crimson_trapdoor");
            registerModel(FFDItems.CRIMSON_BUTTON, "crimson_button");
            registerModel(FFDItems.CRIMSON_PRESSURE_PLATE, "crimson_pressure_plate");
            ModelLoader.setCustomStateMapper(FFDBlocks.CRIMSON_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.CRIMSON_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.WARPED_STEM, "warped_stem");
            registerModel(FFDItems.STRIPPED_WARPED_STEM, "stripped_warped_stem");
            registerModel(FFDItems.WARPED_HYPHAE, "warped_hyphae");
            registerModel(FFDItems.STRIPPED_WARPED_HYPHAE, "stripped_warped_hyphae");
            registerModel(FFDItems.WARPED_WART_BLOCK, "warped_wart_block");
            registerModel(FFDItems.WARPED_PLANKS, "warped_planks");
            registerModel(FFDItems.WARPED_STAIRS, "warped_stairs");
            registerModel(FFDItems.WARPED_SLAB, "warped_slab");
            registerModel(FFDItems.WARPED_FENCE, "warped_fence");
            registerModel(FFDItems.WARPED_FENCE_GATE, "warped_fence_gate");
            registerModel(FFDItems.WARPED_DOOR, "warped_door");
            registerModel(FFDItems.WARPED_TRAPDOOR, "warped_trapdoor");
            registerModel(FFDItems.WARPED_BUTTON, "warped_button");
            registerModel(FFDItems.WARPED_PRESSURE_PLATE, "warped_pressure_plate");
            ModelLoader.setCustomStateMapper(FFDBlocks.WARPED_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.WARPED_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isCrimsonWoodEnabled() || FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.SHROOMLIGHT, "shroomlight");
        }
        if (FFDItems.isAzaleaEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.AZALEA_LEAVES,
                    new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.FLOWERING_AZALEA_LEAVES,
                    new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY).build());
        }
        if (FFDItems.isKelpEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.KELP,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelp.AGE).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.KELP_YOUNG,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelpYoung.AGE).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.KELP_PLANT,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeagrassEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.TALL_SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeaPickleEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.SEA_PICKLE,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isDripleafEnabled()) {
            ModelLoader.setCustomStateMapper(FFDBlocks.SMALL_DRIPLEAF,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockSmallDripleaf.WATERLOGGED).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.BIG_DRIPLEAF_STEM,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBigDripleafStem.WATERLOGGED).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.BIG_DRIPLEAF,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            ModelLoader.setCustomStateMapper(FFDBlocks.BIG_DRIPLEAF_WATERLOGGED,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
    }

    private static void registerModel(net.minecraft.item.Item item, String name) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(FarmerFutureDelight.MODID + ":" + name, "inventory"));
    }

    @Override
    public void preInit() {
        if (FFDEntities.isGlowSquidEnabled()) {
            RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        }
        if (FFDItems.isTurtleEnabled()) {
            RenderingRegistry.registerEntityRenderingHandler(EntityTurtle.class, RenderTurtle::new);
        }
        if (FFDItems.isHoneyEnabled()) {
            RenderingRegistry.registerEntityRenderingHandler(EntityBee.class, RenderBee::new);
        }
    }

    @Override
    public void spawnGlowParticle(EntityGlowSquid squid) {
        double x = squid.posX + (squid.getRNG().nextDouble() - 0.5D) * 0.6D;
        double y = squid.posY + squid.getRNG().nextDouble() * squid.height;
        double z = squid.posZ + (squid.getRNG().nextDouble() - 0.5D) * 0.6D;
        Minecraft.getMinecraft().effectRenderer.addEffect(ParticleGlowSquid.ambient(squid.world, x, y, z));
    }

    @Override
    public void spawnGlowInkParticles(EntityGlowSquid squid) {
        float pitch = squid.prevSquidPitch * ((float) Math.PI / 180.0F);
        float yaw = -squid.prevRenderYawOffset * ((float) Math.PI / 180.0F);
        Vec3d originOffset = new Vec3d(0.0D, -1.0D, 0.0D).rotatePitch(pitch).rotateYaw(yaw);
        Vec3d origin = originOffset.addVector(squid.posX, squid.posY + 0.5D, squid.posZ);

        for (int i = 0; i < 30; i++) {
            Vec3d direction = new Vec3d(
                    squid.getRNG().nextFloat() * 0.6F - 0.3F,
                    -1.0D,
                    squid.getRNG().nextFloat() * 0.6F - 0.3F)
                    .rotatePitch(pitch).rotateYaw(yaw);
            double scale = (squid.isChild() ? 0.1D : 0.3D) + squid.getRNG().nextFloat() * 2.0D;
            direction = direction.scale(scale * 0.1D);
            Minecraft.getMinecraft().effectRenderer.addEffect(
                    ParticleGlowSquid.ink(squid.world, origin.x, origin.y, origin.z,
                            direction.x, direction.y, direction.z));
        }
    }

    @Override
    public void spawnSporeBlossomParticle(net.minecraft.world.World world, double x, double y,
                                          double z, boolean ambient) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleSporeBlossom.create(world, x, y, z, ambient));
    }

    @Override
    public void spawnHoneyDripParticle(net.minecraft.world.World world, double x, double y,
                                       double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleHoneyDrip.create(world, x, y, z));
    }
}
