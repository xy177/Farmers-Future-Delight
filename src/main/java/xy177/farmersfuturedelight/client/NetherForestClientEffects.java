package xy177.farmersfuturedelight.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.particle.ParticleNetherAsh;
import xy177.farmersfuturedelight.client.particle.ParticleNetherSpore;
import xy177.farmersfuturedelight.client.sound.MovingSoundNetherBiome;
import xy177.farmersfuturedelight.common.registry.FFDBiomes;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class NetherForestClientEffects {
    private static final int[] FOG_SAMPLE_OFFSETS = {-8, -4, 0, 4, 8};
    private static final int PARTICLE_SAMPLES = 667;
    private static final double ADDITION_CHANCE = 0.0111D;
    private static final Map<Environment, MovingSoundNetherBiome> LOOP_SOUNDS = new HashMap<>();
    private static World lastWorld;
    private static Environment previousLoopBiome;
    private static float moodiness;

    private NetherForestClientEffects() { }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || minecraft.isGamePaused()) return;
        if (lastWorld != minecraft.world) {
            for (MovingSoundNetherBiome sound : LOOP_SOUNDS.values()) sound.fadeOut();
            lastWorld = minecraft.world;
            previousLoopBiome = null;
            LOOP_SOUNDS.clear();
            moodiness = 0;
        }
        Environment biome = environment(minecraft.world, new BlockPos(minecraft.player));
        tickLoopSound(minecraft, biome);
        if (biome != null) {
            if (minecraft.world.rand.nextDouble() < ADDITION_CHANCE) playRelative(minecraft, biome.additions);
            tickMoodSound(minecraft, biome);
        }
        if (minecraft.world.provider.getDimension() == -1) spawnAmbientParticles(minecraft);
    }

    @SubscribeEvent
    public static void onMusic(PlaySoundEvent event) {
        ISound sound = event.getResultSound();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (sound == null || minecraft.world == null || minecraft.player == null
                || sound.getCategory() != SoundCategory.MUSIC
                || !sound.getSoundLocation().equals(SoundEvents.MUSIC_NETHER.getSoundName())) return;
        Environment biome = environment(minecraft.world, new BlockPos(minecraft.player));
        if (biome == null) return;
        if (biome == Environment.WARPED) {
            event.setResultSound(null);
            return;
        }
        SoundEventAccessor accessor = minecraft.getSoundHandler().getAccessor(biome.music.getSoundName());
        if (accessor != null && accessor.getWeight() > 0)
            event.setResultSound(PositionedSoundRecord.getMusicRecord(biome.music));
    }

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        Material material = event.getState().getMaterial();
        World world = event.getEntity().world;
        if (world.provider.getDimension() != -1 || material == Material.WATER || material == Material.LAVA) return;
        BlockPos center = new BlockPos(event.getEntity().posX,
                event.getEntity().posY + event.getEntity().getEyeHeight(), event.getEntity().posZ);
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        float red = 0;
        float green = 0;
        float blue = 0;
        int known = 0;
        for (int offsetX : FOG_SAMPLE_OFFSETS) for (int offsetY : FOG_SAMPLE_OFFSETS) for (int offsetZ : FOG_SAMPLE_OFFSETS) {
            sample.setPos(center.getX() + offsetX, center.getY() + offsetY, center.getZ() + offsetZ);
            Environment biome = environment(world, sample);
            if (biome == null) continue;
            red += (biome.color >> 16 & 255) / 255.0F;
            green += (biome.color >> 8 & 255) / 255.0F;
            blue += (biome.color & 255) / 255.0F;
            known++;
        }
        int count = FOG_SAMPLE_OFFSETS.length * FOG_SAMPLE_OFFSETS.length * FOG_SAMPLE_OFFSETS.length;
        float original = (count - known) / (float) count;
        if (known > 0) {
            event.setRed(event.getRed() * original + red / count);
            event.setGreen(event.getGreen() * original + green / count);
            event.setBlue(event.getBlue() * original + blue / count);
        }
    }

    @SubscribeEvent
    public static void onFogDistance(EntityViewRenderEvent.RenderFogEvent event) {
        net.minecraft.entity.Entity entity = event.getEntity();
        Material material = event.getState().getMaterial();
        if (entity.world.provider.getDimension() != -1 || material == Material.WATER || material == Material.LAVA
                || entity instanceof net.minecraft.entity.EntityLivingBase
                && ((net.minecraft.entity.EntityLivingBase) entity).isPotionActive(MobEffects.BLINDNESS)
                || environment(entity.world, new BlockPos(entity)) == null) return;
        float end = Math.min(96, event.getFarPlaneDistance());
        GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        GlStateManager.setFogStart(Math.min(10, end * 0.75F));
        GlStateManager.setFogEnd(end);
    }

    private static void tickLoopSound(Minecraft minecraft, Environment biome) {
        Iterator<MovingSoundNetherBiome> iterator = LOOP_SOUNDS.values().iterator();
        while (iterator.hasNext()) if (iterator.next().isDonePlaying()) iterator.remove();
        if (previousLoopBiome != biome) {
            MovingSoundNetherBiome previous = LOOP_SOUNDS.get(previousLoopBiome);
            if (previous != null) previous.fadeOut();
            previousLoopBiome = biome;
        }
        if (biome == null) return;
        MovingSoundNetherBiome sound = LOOP_SOUNDS.get(biome);
        if (sound == null) {
            sound = new MovingSoundNetherBiome(biome.loop);
            LOOP_SOUNDS.put(biome, sound);
            minecraft.getSoundHandler().playSound(sound);
        }
        sound.fadeIn();
    }

    private static void tickMoodSound(Minecraft minecraft, Environment biome) {
        EntityPlayer player = minecraft.player;
        Random random = minecraft.world.rand;
        double eyeY = player.posY + player.getEyeHeight();
        BlockPos sample = new BlockPos(player.posX + random.nextInt(17) - 8,
                eyeY + random.nextInt(17) - 8, player.posZ + random.nextInt(17) - 8);
        int skyLight = minecraft.world.getLightFor(EnumSkyBlock.SKY, sample);
        moodiness -= skyLight > 0 ? skyLight / 15.0F * 0.001F
                : (minecraft.world.getLightFor(EnumSkyBlock.BLOCK, sample) - 1) / 6000.0F;
        if (moodiness < 1) { moodiness = Math.max(0, moodiness); return; }
        double dx = sample.getX() + 0.5 - player.posX;
        double dy = sample.getY() + 0.5 - eyeY;
        double dz = sample.getZ() + 0.5 - player.posZ;
        double distance = Math.max(1.0E-4, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double offset = (distance + 2) / distance;
        minecraft.getSoundHandler().playSound(new PositionedSoundRecord(biome.mood.getSoundName(),
                SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.LINEAR,
                (float) (player.posX + dx * offset), (float) (eyeY + dy * offset), (float) (player.posZ + dz * offset)));
        moodiness = 0;
    }

    private static void spawnAmbientParticles(Minecraft minecraft) {
        if (minecraft.gameSettings.particleSetting == 2) return;
        BlockPos center = new BlockPos(minecraft.player);
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        Random random = minecraft.world.rand;
        for (int attempt = 0; attempt < PARTICLE_SAMPLES; attempt++) {
            trySpawnParticle(minecraft, center, sample, random, 16);
            trySpawnParticle(minecraft, center, sample, random, 32);
        }
    }

    private static void trySpawnParticle(Minecraft minecraft, BlockPos center, BlockPos.MutableBlockPos sample,
                                         Random random, int radius) {
        int x = center.getX() + random.nextInt(radius) - random.nextInt(radius);
        int y = center.getY() + random.nextInt(radius) - random.nextInt(radius);
        int z = center.getZ() + random.nextInt(radius) - random.nextInt(radius);
        if (y < 0 || y >= minecraft.world.getHeight()) return;
        sample.setPos(x, y, z);
        if (minecraft.world.getBlockState(sample).isFullCube()) return;
        Environment biome = environment(minecraft.world, sample);
        if (biome == null || random.nextFloat() >= biome.particleChance
                || minecraft.gameSettings.particleSetting == 1 && random.nextInt(3) == 0) return;
        double particleX = x + random.nextDouble();
        double particleY = y + random.nextDouble();
        double particleZ = z + random.nextDouble();
        minecraft.effectRenderer.addEffect(biome == Environment.CRIMSON || biome == Environment.WARPED
                ? ParticleNetherSpore.create(minecraft.world, particleX, particleY, particleZ, biome == Environment.WARPED)
                : ParticleNetherAsh.create(minecraft.world, particleX, particleY, particleZ, biome == Environment.DELTAS));
    }

    private static Environment environment(World world, BlockPos pos) {
        if (NetherGeneration.isModern(world)) {
            FFDVerticalBiome biome = FFDVerticalBiomeManager.getBiome(world, pos);
            for (Environment candidate : Environment.values()) if (candidate.biome == biome) return candidate;
            return null;
        }
        Biome biome = world.getBiome(pos);
        return biome == FFDBiomes.CRIMSON_FOREST ? Environment.CRIMSON
                : biome == FFDBiomes.WARPED_FOREST ? Environment.WARPED : null;
    }

    private static void playRelative(Minecraft minecraft, SoundEvent sound) {
        minecraft.getSoundHandler().playSound(new PositionedSoundRecord(sound.getSoundName(),
                SoundCategory.AMBIENT, 1, 1, false, 0, ISound.AttenuationType.NONE, 0, 0, 0));
    }

    private enum Environment {
        WASTES(FFDVerticalBiome.NETHER_WASTES, 0x330808, 0, FFDSounds.NETHER_WASTES_LOOP,
                FFDSounds.NETHER_WASTES_ADDITIONS, FFDSounds.NETHER_WASTES_MOOD, FFDSounds.NETHER_WASTES_MUSIC),
        CRIMSON(FFDVerticalBiome.CRIMSON_FOREST, 0x330303, .025F, FFDSounds.CRIMSON_FOREST_LOOP,
                FFDSounds.CRIMSON_FOREST_ADDITIONS, FFDSounds.CRIMSON_FOREST_MOOD, FFDSounds.CRIMSON_FOREST_MUSIC),
        WARPED(FFDVerticalBiome.WARPED_FOREST, 0x1A051A, .01428F, FFDSounds.WARPED_FOREST_LOOP,
                FFDSounds.WARPED_FOREST_ADDITIONS, FFDSounds.WARPED_FOREST_MOOD, FFDSounds.WARPED_FOREST_MUSIC),
        VALLEY(FFDVerticalBiome.SOUL_SAND_VALLEY, 0x1B4745, .00625F, FFDSounds.SOUL_SAND_VALLEY_LOOP,
                FFDSounds.SOUL_SAND_VALLEY_ADDITIONS, FFDSounds.SOUL_SAND_VALLEY_MOOD, FFDSounds.SOUL_SAND_VALLEY_MUSIC),
        DELTAS(FFDVerticalBiome.BASALT_DELTAS, 0x685F70, .118093334F, FFDSounds.BASALT_DELTAS_LOOP,
                FFDSounds.BASALT_DELTAS_ADDITIONS, FFDSounds.BASALT_DELTAS_MOOD, FFDSounds.BASALT_DELTAS_MUSIC);

        final FFDVerticalBiome biome;
        final int color;
        final float particleChance;
        final SoundEvent loop;
        final SoundEvent additions;
        final SoundEvent mood;
        final SoundEvent music;
        Environment(FFDVerticalBiome biome, int color, float particleChance, SoundEvent loop,
                    SoundEvent additions, SoundEvent mood, SoundEvent music) {
            this.biome = biome;
            this.color = color;
            this.particleChance = particleChance;
            this.loop = loop;
            this.additions = additions;
            this.mood = mood;
            this.music = music;
        }
    }
}
