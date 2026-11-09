package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Participant;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition.Wave;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;

final class RaidWaveSpawner {
    private RaidWaveSpawner() { }

    static final class ObstructedSpawn extends IllegalStateException {
        private ObstructedSpawn() { super("Raid spawn position is obstructed"); }
    }

    @Nullable
    static BlockPos findPosition(RaidInstance instance, int attempts) {
        WorldServer world = instance.world();
        xy177.farmersfuturedelight.api.raid.RaidScriptRules rules = instance.definition.getScriptRules();
        if (rules != null && rules.hasGeometry()) {
            for (int attempt = 0; attempt < attempts; attempt++) {
                double radius = rules.size() / 2;
                int horizontalX = instance.center.getX() + MathHelper.floor((world.rand.nextDouble() * 2 - 1) * radius);
                int horizontalZ = instance.center.getZ() + MathHelper.floor((world.rand.nextDouble() * 2 - 1) * radius);
                if (!world.isBlockLoaded(new BlockPos(horizontalX, instance.center.getY(), horizontalZ))) continue;
                BlockPos position = new BlockPos(horizontalX, world.getHeight(horizontalX, horizontalZ), horizontalZ);
                if (!instance.contains(position) || !world.getWorldBorder().contains(position)
                        || !world.isAreaLoaded(position.add(-1, 0, -1), position.add(1, 1, 1))
                        || !FFDRaidManager.isTicking(world, position)) continue;
                if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, position)) return position;
            }
            return null;
        }
        int seconds = instance.cooldown() / 20;
        float distance = 0.22F * seconds - 0.24F;
        float start = world.rand.nextFloat() * ((float) Math.PI * 2.0F);
        for (int attempt = 0; attempt < attempts; attempt++) {
            float angle = start + (float) Math.PI * attempt / 8.0F;
            int horizontalX = instance.center.getX() + MathHelper.floor(MathHelper.cos(angle) * 32.0F * distance)
                    + world.rand.nextInt(3) * MathHelper.floor(distance);
            int horizontalZ = instance.center.getZ() + MathHelper.floor(MathHelper.sin(angle) * 32.0F * distance)
                    + world.rand.nextInt(3) * MathHelper.floor(distance);
            BlockPos column = new BlockPos(horizontalX, instance.center.getY(), horizontalZ);
            if (!world.isAreaLoaded(column.add(-10, 0, -10), column.add(10, 0, 10))) continue;
            BlockPos position = new BlockPos(horizontalX, world.getHeight(horizontalX, horizontalZ), horizontalZ);
            if (Math.abs(position.getY() - instance.center.getY()) > 96 || !world.getWorldBorder().contains(position)
                    || !FFDRaidManager.isTicking(world, position)
                    || seconds > 7 && RaidVillages.isVillage(world, position)) continue;
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, position)
                    || world.getBlockState(position.down()).getBlock() == Blocks.SNOW_LAYER && world.isAirBlock(position)) return position;
        }
        return null;
    }

    static List<Spawned> spawn(RaidInstance instance, Wave wave, BlockPos position) {
        List<Spawned> entities = new ArrayList<>();
        WorldServer world = instance.world();
        try {
            for (Participant participant : wave.getParticipants()) create(instance, participant, position, null, entities);
            for (Spawned spawned : entities) {
                if (instance.manager.owns(spawned.entity.getUniqueID()) || !world.spawnEntity(spawned.entity)) {
                    throw new IllegalStateException("Raid wave entity spawn was rejected");
                }
            }
            return entities;
        } catch (RuntimeException exception) {
            for (Spawned spawned : entities) {
                spawned.entity.dismountRidingEntity();
                spawned.entity.setDead();
                world.removeEntity(spawned.entity);
            }
            throw exception;
        }
    }

    private static void create(RaidInstance instance, Participant participant, BlockPos position,
                               @Nullable EntityLiving vehicle, List<Spawned> entities) {
        WorldServer world = instance.world();
        for (int count = 0; count < participant.getCount(); count++) {
            Entity created = EntityList.createEntityByIDFromName(participant.getEntity(), world);
            if (!(created instanceof EntityLiving)) throw new IllegalArgumentException("Cannot instantiate raid mob " + participant.getEntity());
            EntityLiving entity = (EntityLiving) created;
            entities.add(new Spawned(entity, participant.isLeader()));
            entity.setLocationAndAngles(position.getX() + 0.5D, position.getY() + 1.0D, position.getZ() + 0.5D, 0.0F, 0.0F);
            FFDRaidCaptains.prepareSpawn(entity, "event");
            Event.Result allowed = ForgeEventFactory.canEntitySpawn(entity, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ, null);
            if (allowed == Event.Result.DENY) {
                throw new IllegalStateException("Raid spawn rejected by spawn rules");
            }
            if (!ForgeEventFactory.doSpecialSpawn(entity, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ, null)) {
                entity.onInitialSpawn(world.getDifficultyForLocation(position), null);
            }
            if (instance.builtIn) buffs(instance, entity);
            NBTTagCompound nbt = entity.writeToNBT(new NBTTagCompound());
            nbt.merge(participant.getNbt());
            entity.readFromNBT(nbt);
            if (!entity.isEntityAlive() || !Float.isFinite(entity.getHealth())) throw new IllegalArgumentException("Raid NBT produced invalid health " + participant.getEntity());
            if (!entity.isNotColliding()) throw new ObstructedSpawn();
            entity.onGround = true;
            if (participant.isLeader()) FFDRaidCaptains.setLeader(entity, true);
            if (vehicle != null && !entity.startRiding(vehicle, true)) throw new IllegalArgumentException("Raid passenger could not mount its vehicle");
            for (Participant passenger : participant.getPassengers()) create(instance, passenger, position, entity, entities);
        }
    }

    private static void buffs(RaidInstance instance, EntityLiving entity) {
        float odds = instance.omenLevel >= 5 ? 0.75F : instance.omenLevel == 4 ? 0.5F
                : instance.omenLevel == 3 ? 0.25F : instance.omenLevel == 2 ? 0.1F : 0.0F;
        int wave = instance.wave + 1;
        if (entity instanceof EntityPillager || RaidDefaults.isPillager(entity)) {
            if (entity.getRNG().nextFloat() <= odds && wave > 3) {
                ItemStack crossbow = FFDItems.effectiveStack(FFDItems.CROSSBOW);
                if (crossbow.isEmpty()) return;
                net.minecraft.enchantment.Enchantment enchantment =
                        net.minecraftforge.fml.common.registry.ForgeRegistries.ENCHANTMENTS.getValue(FFDEnchantments.QUICK_CHARGE.getRegistryName());
                if (enchantment == null) enchantment = net.minecraftforge.fml.common.registry.ForgeRegistries.ENCHANTMENTS
                        .getValue(new net.minecraft.util.ResourceLocation("futuremc:quick_charge"));
                if (enchantment != null) crossbow.addEnchantment(enchantment, wave > 5 ? 2 : 1);
                entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, crossbow);
            }
        } else if (entity instanceof EntityVindicator) {
            ItemStack axe = new ItemStack(Items.IRON_AXE);
            if (entity.getRNG().nextFloat() <= odds) axe.addEnchantment(Enchantments.SHARPNESS, wave > 5 ? 2 : 1);
            entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, axe);
        }
    }

    static void horn(RaidInstance instance, BlockPos position) {
        WorldServer world = instance.world();
        for (EntityPlayerMP player : world.getPlayers(EntityPlayerMP.class, candidate -> true)) {
            double horizontalX = position.getX() + 0.5D - player.posX;
            double horizontalZ = position.getZ() + 0.5D - player.posZ;
            double distance = Math.sqrt(horizontalX * horizontalX + horizontalZ * horizontalZ);
            if (distance > 64.0D && instance.manager.nearby(player.getPosition(), null, true) != instance) continue;
            double soundX = distance == 0.0D ? player.posX : player.posX + 13.0D * horizontalX / distance;
            double soundZ = distance == 0.0D ? player.posZ : player.posZ + 13.0D * horizontalZ / distance;
            player.connection.sendPacket(new SPacketSoundEffect(FFDRaidContent.RAID_HORN, SoundCategory.NEUTRAL,
                    soundX, player.posY, soundZ, 64.0F, 1.0F));
        }
    }

    static final class Spawned {
        final EntityLiving entity;
        final boolean leader;

        Spawned(EntityLiving entity, boolean leader) {
            this.entity = entity;
            this.leader = leader;
        }
    }
}
