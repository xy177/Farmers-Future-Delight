package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.common.block.BlockBell;
import xy177.farmersfuturedelight.common.raid.FFDRaidManager;
import xy177.farmersfuturedelight.common.raid.FFDRaidVillagers;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;

public final class EntityAIVillagerRaidResponse extends EntityAIBase {
    private final EntityVillager villager;
    private BlockPos target;
    private int phase;
    private int nextPath;
    private long nextWander;

    public EntityAIVillagerRaidResponse(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    private int currentPhase() {
        WorldServer world = (WorldServer) villager.world;
        BlockPos position = villager.getPosition();
        if (FFDRaidManager.hasWonRaid(world, position)) return 3;
        if (FFDRaidManager.isPreparingRaid(world, position)) return 1;
        if (FFDRaidManager.hasActiveRaid(world, position)) return 2;
        return FFDRaidVillagers.hasHeardBell(villager) ? 4 : 0;
    }

    @Override
    public boolean shouldExecute() {
        if (villager.isAIDisabled() || villager.isTrading() || villager.isRiding()
                || !villager.isEntityAlive()) return false;
        phase = currentPhase();
        return phase != 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return villager.isEntityAlive() && !villager.isAIDisabled() && !villager.isTrading()
                && !villager.isRiding() && phase == currentPhase();
    }

    @Override
    public void startExecuting() {
        FFDVillagerHomes.wake(villager);
        nextPath = 0;
        nextWander = villager.world.getTotalWorldTime() + 20L;
        target = phase == 1 ? FFDVillagerRoutines.getMeeting(villager)
                : phase == 2 || phase == 4 ? FFDRaidVillagers.findHidingPlace(villager,
                phase == 2 ? 24 : 32, phase == 2 ? 1 : 2) : null;
        if (phase == 1 && (target == null || villager.getRNG().nextInt(8) >= 6)) {
            target = randomTarget(false);
        }
    }

    @Override
    public void updateTask() {
        if (phase == 3) {
            celebrate();
        }
        long now = villager.world.getTotalWorldTime();
        if ((phase == 1 || phase == 3) && now >= nextWander
                && (target == null || villager.getDistanceSqToCenter(target) < 1.0D
                || villager.getNavigator().noPath() && nextPath <= 0)) {
            nextWander = now + 20L;
            target = phase == 1 && villager.getRNG().nextInt(8) < 6
                    ? FFDVillagerRoutines.getMeeting(villager)
                    : randomTarget(phase == 3 && villager.getRNG().nextInt(7) < 5);
        }
        BlockPos meeting = phase == 1 ? FFDVillagerRoutines.getMeeting(villager) : null;
        if (meeting != null && meeting.distanceSq(villager.getPosition()) < 9.0D
                && villager.getRNG().nextFloat() > 0.95F && villager.world.isBlockLoaded(meeting)
                && villager.world.getBlockState(meeting).getBlock() instanceof BlockBell) {
            ((BlockBell) villager.world.getBlockState(meeting).getBlock()).ring(villager.world, meeting, null);
        }
        if (target == null || !villager.world.isBlockLoaded(target)) return;
        double distance = villager.getDistanceSqToCenter(target);
        double stop = phase == 1 || phase == 4 ? 4.0D : 1.0D;
        if (distance < stop) {
            villager.getNavigator().clearPath();
            return;
        }
        if (--nextPath > 0 || !FFDVillagerHomes.canNavigate(villager, target)) return;
        nextPath = 20;
        villager.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(),
                target.getZ() + 0.5D, phase == 1 ? 0.75D : phase == 2 ? 0.7D : phase == 3 ? 0.5D : 0.625D);
    }

    private BlockPos randomTarget(boolean outdoors) {
        if (outdoors && villager.world.canSeeSky(villager.getPosition())) return null;
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos candidate = villager.getPosition().add(villager.getRNG().nextInt(20) - 10,
                    villager.getRNG().nextInt(6) - 3, villager.getRNG().nextInt(20) - 10);
            if (!villager.world.isBlockLoaded(candidate) || !villager.world.isBlockLoaded(candidate.down())
                    || outdoors && (!villager.world.canSeeSky(candidate)
                    || villager.world.getHeight(candidate).getY() > villager.posY)
                    || !villager.world.getBlockState(candidate.down()).isTopSolid()
                    || !villager.world.isAirBlock(candidate) || !villager.world.isAirBlock(candidate.up())) continue;
            return candidate;
        }
        return null;
    }

    private void celebrate() {
        if (!villager.world.canSeeSky(villager.getPosition())) return;
        if (villager.getRNG().nextInt(100) == 0) {
            villager.playSound(FFDRaidContent.VILLAGER_CELEBRATE, 1.0F, 1.0F);
        }
        if (villager.getRNG().nextInt(200) != 0) return;
        EnumDyeColor color = EnumDyeColor.values()[villager.getRNG().nextInt(EnumDyeColor.values().length)];
        ItemStack rocket = new ItemStack(Items.FIREWORKS);
        NBTTagCompound fireworks = rocket.getOrCreateSubCompound("Fireworks");
        fireworks.setByte("Flight", (byte) villager.getRNG().nextInt(3));
        NBTTagCompound explosion = new NBTTagCompound();
        explosion.setByte("Type", (byte) 4);
        explosion.setIntArray("Colors", new int[] {ItemDye.DYE_COLORS[color.getDyeDamage()]});
        NBTTagList explosions = new NBTTagList();
        explosions.appendTag(explosion);
        fireworks.setTag("Explosions", explosions);
        EntityFireworkRocket entity = new EntityFireworkRocket(villager.world, villager.posX,
                villager.posY + villager.getEyeHeight(), villager.posZ, rocket);
        xy177.farmersfuturedelight.core.FFDFireworkHooks.setShooter(entity, villager);
        villager.world.spawnEntity(entity);
    }

    @Override
    public void resetTask() {
        target = null;
        villager.getNavigator().clearPath();
    }
}
