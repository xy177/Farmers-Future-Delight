package xy177.farmersfuturedelight.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.particle.ParticleNetherSpore;
import xy177.farmersfuturedelight.client.sound.MovingSoundNetherBiome;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class NetherForestClientEffects {
    private static final int[] FOG_SAMPLE_OFFSETS = {-8, -4, 0, 4, 8};
    private static final int PARTICLE_SAMPLES = 667;
    private static final float CRIMSON_SPORE_CHANCE = 0.025F;
    private static final float WARPED_SPORE_CHANCE = 0.01428F;
    private static final double ADDITION_CHANCE = 0.0111D;
    private static final Map<Biome, MovingSoundNetherBiome> LOOP_SOUNDS = new HashMap<>();

    private static World lastWorld;
    private static Biome previousLoopBiome;
    private static float moodiness;

    private NetherForestClientEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || minecraft.isGamePaused()) {
            return;
        }
        if (lastWorld != minecraft.world) {
            lastWorld = minecraft.world;
            previousLoopBiome = null;
            LOOP_SOUNDS.clear();
            moodiness = 0.0F;
        }

        Biome biome = targetBiome(minecraft.world,
                new BlockPos(minecraft.player.posX, minecraft.player.posY,
                        minecraft.player.posZ));
        tickLoopSound(minecraft, biome);
        if (biome != null) {
            tickAdditionSound(minecraft, biome);
            tickMoodSound(minecraft, biome);
        }
        spawnAmbientParticles(minecraft);
    }

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        Material material = event.getState().getMaterial();
        if (material == Material.WATER || material == Material.LAVA) {
            return;
        }
        World world = event.getEntity().world;
        BlockPos center = new BlockPos(event.getEntity());
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        int crimsonSamples = 0;
        int warpedSamples = 0;
        for (int offsetX : FOG_SAMPLE_OFFSETS) {
            for (int offsetZ : FOG_SAMPLE_OFFSETS) {
                sample.setPos(center.getX() + offsetX, center.getY(), center.getZ() + offsetZ);
                Biome biome = targetBiome(world, sample);
                if (biome == FFDBiomes.CRIMSON_FOREST) {
                    crimsonSamples++;
                } else if (biome == FFDBiomes.WARPED_FOREST) {
                    warpedSamples++;
                }
            }
        }
        int sampleCount = FOG_SAMPLE_OFFSETS.length * FOG_SAMPLE_OFFSETS.length;
        float crimsonWeight = crimsonSamples / (float) sampleCount;
        float warpedWeight = warpedSamples / (float) sampleCount;
        float originalWeight = 1.0F - crimsonWeight - warpedWeight;
        if (originalWeight < 1.0F) {
            event.setRed(event.getRed() * originalWeight
                    + (0x33 / 255.0F) * crimsonWeight + (0x1A / 255.0F) * warpedWeight);
            event.setGreen(event.getGreen() * originalWeight
                    + (0x03 / 255.0F) * crimsonWeight + (0x05 / 255.0F) * warpedWeight);
            event.setBlue(event.getBlue() * originalWeight
                    + (0x03 / 255.0F) * crimsonWeight + (0x1A / 255.0F) * warpedWeight);
        }
    }

    private static void tickLoopSound(Minecraft minecraft, Biome biome) {
        Iterator<MovingSoundNetherBiome> iterator = LOOP_SOUNDS.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isDonePlaying()) {
                iterator.remove();
            }
        }
        if (biome == previousLoopBiome) {
            return;
        }
        previousLoopBiome = biome;
        for (MovingSoundNetherBiome sound : LOOP_SOUNDS.values()) {
            sound.fadeOut();
        }
        if (biome == null) {
            return;
        }
        MovingSoundNetherBiome sound = LOOP_SOUNDS.get(biome);
        if (sound == null) {
            sound = new MovingSoundNetherBiome(loopSound(biome));
            LOOP_SOUNDS.put(biome, sound);
            minecraft.getSoundHandler().playSound(sound);
        }
        sound.fadeIn();
    }

    private static void tickAdditionSound(Minecraft minecraft, Biome biome) {
        if (minecraft.world.rand.nextDouble() < ADDITION_CHANCE) {
            playRelative(minecraft, biome == FFDBiomes.CRIMSON_FOREST
                    ? FFDSounds.CRIMSON_FOREST_ADDITIONS
                    : FFDSounds.WARPED_FOREST_ADDITIONS);
        }
    }

    private static void tickMoodSound(Minecraft minecraft, Biome biome) {
        EntityPlayer player = minecraft.player;
        Random random = minecraft.world.rand;
        double eyeY = player.posY + player.getEyeHeight();
        BlockPos sample = new BlockPos(player.posX + random.nextInt(17) - 8,
                eyeY + random.nextInt(17) - 8,
                player.posZ + random.nextInt(17) - 8);
        int skyLight = minecraft.world.getLightFor(EnumSkyBlock.SKY, sample);
        if (skyLight > 0) {
            moodiness -= skyLight / 15.0F * 0.001F;
        } else {
            moodiness -= (minecraft.world.getLightFor(EnumSkyBlock.BLOCK, sample) - 1) / 6000.0F;
        }
        if (moodiness < 1.0F) {
            moodiness = Math.max(0.0F, moodiness);
            return;
        }

        double sampleX = sample.getX() + 0.5D;
        double sampleY = sample.getY() + 0.5D;
        double sampleZ = sample.getZ() + 0.5D;
        double dx = sampleX - player.posX;
        double dy = sampleY - eyeY;
        double dz = sampleZ - player.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 1.0E-4D) {
            distance = 1.0D;
        }
        double soundDistance = distance + 2.0D;
        SoundEvent sound = biome == FFDBiomes.CRIMSON_FOREST
                ? FFDSounds.CRIMSON_FOREST_MOOD : FFDSounds.WARPED_FOREST_MOOD;
        minecraft.getSoundHandler().playSound(new PositionedSoundRecord(sound.getSoundName(),
                SoundCategory.AMBIENT, 1.0F, 1.0F, false, 0,
                ISound.AttenuationType.LINEAR,
                (float) (player.posX + dx / distance * soundDistance),
                (float) (eyeY + dy / distance * soundDistance),
                (float) (player.posZ + dz / distance * soundDistance)));
        moodiness = 0.0F;
    }

    private static void spawnAmbientParticles(Minecraft minecraft) {
        if (minecraft.gameSettings.particleSetting == 2) {
            return;
        }
        BlockPos center = new BlockPos(minecraft.player);
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        Random random = minecraft.world.rand;
        for (int i = 0; i < PARTICLE_SAMPLES; i++) {
            trySpawnParticle(minecraft, center, sample, random, 16);
            trySpawnParticle(minecraft, center, sample, random, 32);
        }
    }

    private static void trySpawnParticle(Minecraft minecraft, BlockPos center,
                                         BlockPos.MutableBlockPos sample, Random random,
                                         int radius) {
        int x = center.getX() + random.nextInt(radius) - random.nextInt(radius);
        int y = center.getY() + random.nextInt(radius) - random.nextInt(radius);
        int z = center.getZ() + random.nextInt(radius) - random.nextInt(radius);
        if (y < 0 || y >= minecraft.world.getHeight()) {
            return;
        }
        sample.setPos(x, y, z);
        if (minecraft.world.getBlockState(sample).isFullCube()) {
            return;
        }
        Biome biome = targetBiome(minecraft.world, sample);
        float chance = biome == FFDBiomes.CRIMSON_FOREST ? CRIMSON_SPORE_CHANCE
                : biome == FFDBiomes.WARPED_FOREST ? WARPED_SPORE_CHANCE : 0.0F;
        if (random.nextFloat() > chance
                || minecraft.gameSettings.particleSetting == 1 && random.nextInt(3) == 0) {
            return;
        }
        minecraft.effectRenderer.addEffect(ParticleNetherSpore.create(minecraft.world,
                x + random.nextDouble(), y + random.nextDouble(), z + random.nextDouble(),
                biome == FFDBiomes.WARPED_FOREST));
    }

    private static Biome targetBiome(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        return biome == FFDBiomes.CRIMSON_FOREST || biome == FFDBiomes.WARPED_FOREST
                ? biome : null;
    }

    private static SoundEvent loopSound(Biome biome) {
        return biome == FFDBiomes.CRIMSON_FOREST ? FFDSounds.CRIMSON_FOREST_LOOP
                : FFDSounds.WARPED_FOREST_LOOP;
    }

    private static void playRelative(Minecraft minecraft, SoundEvent sound) {
        minecraft.getSoundHandler().playSound(new PositionedSoundRecord(sound.getSoundName(),
                SoundCategory.AMBIENT, 1.0F, 1.0F, false, 0,
                ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F));
    }
}
