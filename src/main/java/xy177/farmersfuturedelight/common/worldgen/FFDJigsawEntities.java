package xy177.farmersfuturedelight.common.worldgen;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDZombieVillagerState;

final class FFDJigsawEntities {
    private FFDJigsawEntities() {
    }

    static void place(World world, Template template, BlockPos origin, PlacementSettings settings) {
        if (settings.getIgnoreEntities()) return;
        List<Template.EntityInfo> entities = ObfuscationReflectionHelper.getPrivateValue(Template.class, template, "field_186271_b");
        StructureBoundingBox bounds = settings.getBoundingBox();
        for (Template.EntityInfo info : entities) {
            BlockPos blockPosition = origin.add(Template.transformedBlockPos(settings, info.blockPos));
            if (bounds != null && !bounds.isVecInside(blockPosition)) continue;
            Vec3d local = transform(info.pos, settings.getMirror(), settings.getRotation());
            Vec3d position = local.addVector(origin.getX(), origin.getY(), origin.getZ());
            spawn(world, info.entityData.copy(), position, settings.getMirror(), settings.getRotation(), null, 0);
        }
    }

    private static void spawn(World world, NBTTagCompound data, Vec3d position, Mirror mirror,
                              Rotation rotation, Entity mount, int depth) {
        if (depth > 16) throw new IllegalArgumentException("Structure entity passenger limit exceeded");
        String identifier = data.getString("id");
        if (FFDEntities.CAT_ID.toString().equals(identifier) && !FFDEntities.isLocalCatEnabled()) {
            EntityEntry external = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.CAT, "cat");
            if (external == null) return;
            data.setString("id", external.getRegistryName().toString());
        }
        data.removeTag("UUIDMost");
        data.removeTag("UUIDLeast");
        data.removeTag("UUID");
        NBTTagList coordinates = new NBTTagList();
        coordinates.appendTag(new NBTTagDouble(position.x));
        coordinates.appendTag(new NBTTagDouble(position.y));
        coordinates.appendTag(new NBTTagDouble(position.z));
        data.setTag("Pos", coordinates);
        Entity entity = EntityList.createEntityFromNBT(data, world);
        if (entity == null) throw new IllegalArgumentException("Unknown structure entity " + identifier);
        entity.setUniqueId(UUID.randomUUID());
        float yaw = entity.getMirroredYaw(mirror) + entity.rotationYaw - entity.getRotatedYaw(rotation);
        entity.setLocationAndAngles(position.x, position.y, position.z, yaw, entity.rotationPitch);
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.renderYawOffset = yaw;
            living.rotationYawHead = yaw;
            if (entity instanceof xy177.farmersfuturedelight.common.entity.EntityPillager) {
                xy177.farmersfuturedelight.common.raid.FFDRaidCaptains.prepareSpawn(living, "structure");
            }
            if (entity instanceof EntityVillager) {
                ((EntityVillager) entity).finalizeMobSpawn(world.getDifficultyForLocation(entity.getPosition()), null, false);
            } else if (entity.getClass() == EntityZombieVillager.class) {
                EntityZombieVillager zombie = (EntityZombieVillager) entity;
                net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession profession =
                        zombie.getForgeProfession();
                living.onInitialSpawn(world.getDifficultyForLocation(entity.getPosition()), null);
                if (data.hasKey("Profession", 99) || data.hasKey("ProfessionName", 8)) {
                    zombie.setForgeProfession(profession);
                }
                FFDZombieVillagerState.initializeStructure(zombie, data);
            } else living.onInitialSpawn(world.getDifficultyForLocation(entity.getPosition()), null);
        }
        if (!world.spawnEntity(entity)) return;
        if (mount != null) entity.startRiding(mount, true);
        NBTTagList passengers = data.getTagList("Passengers", 10);
        for (int index = 0; index < passengers.tagCount(); index++) {
            spawn(world, passengers.getCompoundTagAt(index).copy(), position, mirror, rotation, entity, depth + 1);
        }
    }

    private static Vec3d transform(Vec3d vector, Mirror mirror, Rotation rotation) {
        double horizontal = mirror == Mirror.FRONT_BACK ? 1.0D - vector.x : vector.x;
        double depth = mirror == Mirror.LEFT_RIGHT ? 1.0D - vector.z : vector.z;
        switch (rotation) {
            case CLOCKWISE_90: return new Vec3d(1.0D - depth, vector.y, horizontal);
            case COUNTERCLOCKWISE_90: return new Vec3d(depth, vector.y, 1.0D - horizontal);
            case CLOCKWISE_180: return new Vec3d(1.0D - horizontal, vector.y, 1.0D - depth);
            default: return new Vec3d(horizontal, vector.y, depth);
        }
    }
}
