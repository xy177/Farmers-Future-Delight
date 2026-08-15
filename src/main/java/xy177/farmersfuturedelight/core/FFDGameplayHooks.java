package xy177.farmersfuturedelight.core;

import java.util.List;
import java.lang.reflect.Method;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDPowderSnowEvents;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.block.BlockLightningRod;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public final class FFDGameplayHooks {
    public static final String PLAYERS_SLEEPING_PERCENTAGE_RULE = "playersSleepingPercentage";
    private static final int DEFAULT_SLEEPING_PERCENTAGE = 100;
    private static final float MODERN_CLOUD_HEIGHT = 192.0F;
    private static final int LIGHTNING_ROD_RANGE = 128;
    private static final String XP_MERGE_COUNT_TAG = "ffdExperienceOrbCount";
    private static Method shulkerTeleportMethod;

    private FFDGameplayHooks() {
    }

    public static boolean hasEnoughFullySleepingPlayers(WorldServer world) {
        if (world == null || world.isRemote) {
            return false;
        }
        GameRules rules = world.getGameRules();
        ensureSleepingPercentageRule(rules);
        int eligiblePlayers = 0;
        int sleepingPlayers = 0;
        for (EntityPlayer player : world.playerEntities) {
            if (player.isSpectator()) {
                continue;
            }
            eligiblePlayers++;
            if (player.isPlayerFullyAsleep()) {
                sleepingPlayers++;
            }
        }
        if (eligiblePlayers == 0 || sleepingPlayers == 0) {
            return false;
        }
        int percentage = Math.max(0, Math.min(100,
                rules.getInt(PLAYERS_SLEEPING_PERCENTAGE_RULE)));
        int requiredPlayers = Math.max(1,
                (eligiblePlayers * percentage + 99) / 100);
        return sleepingPlayers >= requiredPlayers;
    }

    public static void ensureSleepingPercentageRule(GameRules rules) {
        if (!rules.hasRule(PLAYERS_SLEEPING_PERCENTAGE_RULE)) {
            rules.addGameRule(PLAYERS_SLEEPING_PERCENTAGE_RULE,
                    Integer.toString(DEFAULT_SLEEPING_PERCENTAGE),
                    GameRules.ValueType.NUMERICAL_VALUE);
        }
    }

    public static float adjustCloudHeight(float originalHeight, World world) {
        if (world == null || world.provider == null || world.provider.getDimension() != 0) {
            return originalHeight;
        }
        FFDConfig.CloudHeightMode mode = FFDConfig.cloudHeightMode;
        if (mode == FFDConfig.CloudHeightMode.ALL_WORLDS
                || mode != FFDConfig.CloudHeightMode.DISABLED && FFDHeightHooks.isExtended(world)) {
            return MODERN_CLOUD_HEIGHT;
        }
        return originalHeight;
    }

    public static BlockPos findLightningTarget(WorldServer world, BlockPos pos,
                                                int emptyPrecipitationHeight) {
        BlockPos surface = world.getPrecipitationHeight(pos);
        BlockPos rod = findClosestLightningRod(world, surface);
        if (rod != null) {
            return rod.up();
        }

        AxisAlignedBB search = new AxisAlignedBB(surface,
                new BlockPos(surface.getX(), FFDHeightHooks.maxYExclusive(world), surface.getZ()))
                .grow(3.0D);
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                search, entity -> entity != null && entity.isEntityAlive()
                        && world.canSeeSky(entity.getPosition()));
        if (!entities.isEmpty()) {
            return entities.get(world.rand.nextInt(entities.size())).getPosition();
        }
        int sentinel = FFDHeightHooks.isExtended(world)
                ? FFDHeightHooks.minY(world) - 1 : emptyPrecipitationHeight;
        return surface.getY() == sentinel ? surface.up(2) : surface;
    }

    public static boolean isLightningRodStrikeTarget(WorldServer world, BlockPos strikePos) {
        return world != null && strikePos != null && FFDItems.isCopperEnabled()
                && world.getBlockState(strikePos.down()).getBlock() instanceof BlockLightningRod;
    }

    public static boolean handleCauldronPrecipitation(BlockCauldron cauldron, World world,
                                                       BlockPos pos) {
        if (!FFDConfig.modernCauldronFeatures) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != Blocks.CAULDRON) {
            return true;
        }
        int level = state.getValue(BlockCauldron.LEVEL);
        float temperature = world.getBiome(pos).getTemperature(pos);
        temperature = world.getBiomeProvider().getTemperatureAtHeight(temperature, pos.getY());
        if (temperature >= 0.15F) {
            if (level < 3 && world.rand.nextFloat() < FFDConfig.cauldronRainFillChance) {
                world.setBlockState(pos, state.withProperty(BlockCauldron.LEVEL, level + 1), 2);
                world.updateComparatorOutputLevel(pos, cauldron);
            }
        } else if (level == 0 && FFDItems.isPowderSnowEnabled()
                && world.rand.nextFloat() < FFDConfig.cauldronSnowFillChance) {
            world.setBlockState(pos, FFDBlocks.POWDER_SNOW_CAULDRON.getDefaultState()
                    .withProperty(BlockCauldron.LEVEL, 1), 2);
            world.updateComparatorOutputLevel(pos, FFDBlocks.POWDER_SNOW_CAULDRON);
        }
        return true;
    }

    public static int adjustJukeboxComparatorOutput(int original, World world, BlockPos pos) {
        if (world == null || pos == null) {
            return original;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof BlockJukebox.TileEntityJukebox)) {
            return original;
        }
        ItemStack record = ((BlockJukebox.TileEntityJukebox) tile).getRecord();
        return !record.isEmpty() && record.getItem() == FFDItems.MUSIC_DISC_OTHERSIDE
                ? 14 : original;
    }

    public static void applyModernBlockProperties() {
        if (Blocks.ENCHANTING_TABLE != null) {
            Blocks.ENCHANTING_TABLE.setLightLevel(FFDConfig.enchantingTableEmitsLight
                    ? 7.0F / 15.0F : 0.0F);
        }
    }

    public static boolean blocksFlowingWater(Block liquid, IBlockState targetState) {
        return liquid == Blocks.FLOWING_WATER && targetState != null
                && !WaterloggedPlantFluid.isWaterlogged(targetState)
                && WaterloggedPlantFluid.withWaterlogged(targetState, true) != null;
    }

    public static double powderSnowHorizontalMovementMultiplier(Entity entity, MoverType type) {
        if (entity == null || type == MoverType.PISTON || entity.world == null
                || !FFDItems.isPowderSnowEnabled()) {
            return 1.0D;
        }
        return entity.world.getBlockState(new BlockPos(entity.posX, entity.posY, entity.posZ))
                .getBlock() == FFDBlocks.POWDER_SNOW ? 0.9D : 1.0D;
    }

    public static void finishPowderSnowMovement(Entity entity, double horizontalMultiplier) {
        if (horizontalMultiplier >= 1.0D) {
            return;
        }
        entity.fallDistance = 0.0F;
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
    }

    public static void handlePowderSnowJump(EntityLivingBase entity) {
        if (entity == null || !FFDItems.isPowderSnowEnabled()
                || !BlockPowderSnow.canEntityWalkOnPowderSnow(entity)
                || !FFDPowderSnowEvents.isInsidePowderSnow(entity)) {
            return;
        }
        entity.motionY = Math.max(entity.motionY, 0.2D);
    }

    public static float adjustInfestedBlockBreakProgress(float original, IBlockState state,
                                                          EntityPlayer player, World world,
                                                          BlockPos pos) {
        if (!FFDConfig.infestedBlocksHalfBreakTime || state == null
                || state.getBlock() != Blocks.MONSTER_EGG || player == null || world == null) {
            return original;
        }
        BlockSilverfish.EnumType variant = state.getValue(BlockSilverfish.VARIANT);
        IBlockState hostState = variant.getModelBlock();
        return ForgeHooks.blockStrength(hostState, player, world, pos) * 2.0F;
    }

    public static void spawnPistonDestroyParticles(World world, BlockPos pos, IBlockState state) {
        if (!FFDConfig.pistonBreakParticles || world == null || !world.isRemote
                || pos == null || state == null || state.getBlock() == Blocks.FIRE) {
            return;
        }
        world.playEvent(2001, pos, Block.getStateId(state));
    }

    public static void handleShulkerBulletHit(EntityShulker shulker, DamageSource source) {
        if (!FFDConfig.shulkerDuplication || shulker == null || source == null
                || shulker.world == null || shulker.world.isRemote
                || !(source.getImmediateSource() instanceof EntityShulkerBullet)
                || shulker.getPeekTick() == 0) {
            return;
        }

        double oldX = shulker.posX;
        double oldY = shulker.posY;
        double oldZ = shulker.posZ;
        AxisAlignedBB oldBox = shulker.getEntityBoundingBox();
        if (!tryShulkerTeleport(shulker)) {
            return;
        }

        int nearby = shulker.world.getEntitiesWithinAABB(EntityShulker.class,
                oldBox.grow(8.0D), candidate -> candidate != null && candidate.isEntityAlive()).size();
        float chance = (nearby - 1) / 5.0F;
        if (shulker.world.rand.nextFloat() < chance) {
            return;
        }

        EntityShulker copy = new EntityShulker(shulker.world);
        copyShulkerColor(shulker, copy);
        copy.setPosition(oldX, oldY, oldZ);
        shulker.world.spawnEntity(copy);
    }

    private static void copyShulkerColor(EntityShulker source, EntityShulker target) {
        // EntityShulker#getColor is client-only in 1.12, so copy its server-safe NBT value.
        NBTTagCompound sourceData = new NBTTagCompound();
        NBTTagCompound targetData = new NBTTagCompound();
        source.writeEntityToNBT(sourceData);
        target.writeEntityToNBT(targetData);
        targetData.setByte("Color", sourceData.getByte("Color"));
        target.readEntityFromNBT(targetData);
    }

    private static boolean tryShulkerTeleport(EntityShulker shulker) {
        if (shulkerTeleportMethod == null) {
            String[] names = {"tryTeleportToNewPosition", "func_184689_o"};
            for (String name : names) {
                try {
                    shulkerTeleportMethod = EntityShulker.class.getDeclaredMethod(name);
                    shulkerTeleportMethod.setAccessible(true);
                    break;
                } catch (ReflectiveOperationException ignored) {
                    // The next name covers the alternate Forge mapping namespace.
                }
            }
        }
        if (shulkerTeleportMethod == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(shulkerTeleportMethod.invoke(shulker));
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    public static void mergeNearbyExperienceOrbs(EntityXPOrb orb) {
        if (!FFDConfig.experienceOrbMerging || orb == null || orb.world == null
                || orb.world.isRemote || !orb.isEntityAlive() || orb.ticksExisted % 20 != 1) {
            return;
        }
        final int value = orb.xpValue;
        List<EntityXPOrb> nearby = orb.world.getEntitiesWithinAABB(EntityXPOrb.class,
                orb.getEntityBoundingBox().grow(0.5D), candidate -> candidate != null
                        && candidate != orb && candidate.isEntityAlive()
                        && candidate.xpValue == value
                        && (candidate.getEntityId() - orb.getEntityId()) % 40 == 0);
        for (EntityXPOrb candidate : nearby) {
            setExperienceOrbCount(orb, getExperienceOrbCount(orb)
                    + getExperienceOrbCount(candidate));
            orb.xpOrbAge = Math.min(orb.xpOrbAge, candidate.xpOrbAge);
            candidate.setDead();
        }
    }

    public static void finishExperienceOrbPickup(EntityXPOrb orb) {
        if (orb == null) {
            return;
        }
        int count = getExperienceOrbCount(orb);
        if (count > 1) {
            setExperienceOrbCount(orb, count - 1);
        } else {
            orb.setDead();
        }
    }

    private static int getExperienceOrbCount(EntityXPOrb orb) {
        int count = orb.getEntityData().getInteger(XP_MERGE_COUNT_TAG);
        return count > 0 ? count : 1;
    }

    private static void setExperienceOrbCount(EntityXPOrb orb, int count) {
        orb.getEntityData().setInteger(XP_MERGE_COUNT_TAG, Math.max(1, count));
    }

    private static BlockPos findClosestLightningRod(WorldServer world, BlockPos center) {
        if (!FFDItems.isCopperEnabled()) {
            return null;
        }

        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        int minX = center.getX() - LIGHTNING_ROD_RANGE;
        int maxX = center.getX() + LIGHTNING_ROD_RANGE;
        int minZ = center.getZ() - LIGHTNING_ROD_RANGE;
        int maxZ = center.getZ() + LIGHTNING_ROD_RANGE;
        BlockPos.MutableBlockPos column = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                column.setPos(x, center.getY(), z);
                if (!world.isBlockLoaded(column, false)) {
                    continue;
                }
                BlockPos candidate = world.getHeight(column).down();
                if (!(world.getBlockState(candidate).getBlock() instanceof BlockLightningRod)) {
                    continue;
                }
                double distance = candidate.distanceSq(center);
                if (distance <= (double) LIGHTNING_ROD_RANGE * LIGHTNING_ROD_RANGE
                        && distance < closestDistance) {
                    closestDistance = distance;
                    closest = candidate;
                }
            }
        }
        return closest;
    }
}
