package xy177.farmersfuturedelight.core;

import java.util.List;
import com.google.common.collect.MapMaker;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.world.WorldServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDPowderSnowEvents;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.block.BlockLightningRod;
import xy177.farmersfuturedelight.common.block.BlockAbstractCandle;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDPotions;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.item.INetheriteItem;

public final class FFDGameplayHooks {
    private static final Logger LOGGER = LogManager.getLogger("FFD Gameplay Hooks");
    public static final String PLAYERS_SLEEPING_PERCENTAGE_RULE = "playersSleepingPercentage";
    private static final int DEFAULT_SLEEPING_PERCENTAGE = 100;
    private static final float MODERN_CLOUD_HEIGHT = 192.0F;
    private static final int LIGHTNING_ROD_RANGE = 128;
    private static final String XP_MERGE_COUNT_TAG = "ffdExperienceOrbCount";
    private static final int MAX_BOOK_PAGES = 100;
    private static final int MAX_BOOK_PAGE_LENGTH = 1024;
    private static final Map<Entity, Boolean> SWEET_BERRY_SLOWDOWN =
            new MapMaker().weakKeys().makeMap();
    private static final DamageSource SWEET_BERRY_DAMAGE = new DamageSource("sweetBerryBush");
    private static Method shulkerTeleportMethod;
    private static DataParameter<Byte> entityFlags;
    private static boolean entityFlagsResolved;
    private static net.minecraft.block.Block blockFlowerChorus;
    private static final ThreadLocal<FFDSpawnGroupData> NATURAL_SPAWN_GROUP = new ThreadLocal<>();

    private FFDGameplayHooks() {
    }

    public static boolean isWritableBookEditValid(NBTTagCompound tag) {
        return ItemWritableBook.isNBTValid(tag) && isBookPageContentValid(tag);
    }

    public static boolean isWrittenBookSignValid(NBTTagCompound tag) {
        return ItemWrittenBook.validBookTagContents(tag) && isBookPageContentValid(tag);
    }

    private static boolean isBookPageContentValid(NBTTagCompound tag) {
        NBTTagList pages = tag.getTagList("pages", 8);
        if (pages.tagCount() > MAX_BOOK_PAGES) return false;
        for (int index = 0; index < pages.tagCount(); index++) {
            if (pages.getStringTagAt(index).length() > MAX_BOOK_PAGE_LENGTH) return false;
        }
        return true;
    }

    public static boolean canEatFood(boolean original, EntityPlayer player) {
        return original || player.capabilities.disableDamage;
    }

    public static boolean canPistonBlocksStick(IBlockState first, IBlockState second) {
        Block firstBlock = first.getBlock();
        Block secondBlock = second.getBlock();
        return !((firstBlock == FFDBlocks.HONEY_BLOCK && secondBlock == Blocks.SLIME_BLOCK)
                || (firstBlock == Blocks.SLIME_BLOCK && secondBlock == FFDBlocks.HONEY_BLOCK));
    }

    public static boolean pistonAcceleratesEntities(boolean sticky, IBlockState state) {
        return sticky && state.getBlock() != FFDBlocks.HONEY_BLOCK;
    }

    public static boolean canPistonPullBlock(boolean allowed, IBlockState first, IBlockState second) {
        return allowed && canPistonBlocksStick(first, second);
    }

    public static void moveHoneyPistonEntities(net.minecraft.tileentity.TileEntityPiston piston,
                                               float progress, float newProgress,
                                               ThreadLocal<EnumFacing> movingEntity) {
        if (piston.getPistonState().getBlock() != FFDBlocks.HONEY_BLOCK) return;
        EnumFacing facing = piston.getFacing();
        EnumFacing movement = piston.isExtending() ? facing : facing.getOpposite();
        if (movement.getAxis() == EnumFacing.Axis.Y) return;
        World world = piston.getWorld();
        BlockPos pos = piston.getPos();
        double offset = piston.isExtending() ? progress - 1.0D : 1.0D - progress;
        AxisAlignedBB box = new AxisAlignedBB(0.0D, 0.9375D, 0.0D,
                1.0D, 1.500001D, 1.0D).offset(pos)
                .offset(offset * facing.getFrontOffsetX(), 0.0D,
                        offset * facing.getFrontOffsetZ());
        double distance = newProgress - progress;
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, box)) {
            boolean supported = new BlockPos(entity.posX,
                    entity.getEntityBoundingBox().minY - 0.2D, entity.posZ).equals(pos);
            if (entity.getPushReaction() != net.minecraft.block.material.EnumPushReaction.NORMAL
                    || !entity.onGround || !supported && (entity.posX < box.minX
                    || entity.posX > box.maxX || entity.posZ < box.minZ || entity.posZ > box.maxZ)) {
                continue;
            }
            EnumFacing previous = movingEntity.get();
            movingEntity.set(movement);
            try {
                entity.move(MoverType.PISTON, distance * movement.getFrontOffsetX(), 0.0D,
                        distance * movement.getFrontOffsetZ());
            } finally {
                movingEntity.set(previous);
            }
        }
    }

    public static IEntityLivingData prepareNaturalSpawnGroupData(IEntityLivingData data) {
        FFDSpawnGroupData group = data instanceof FFDSpawnGroupData
                ? (FFDSpawnGroupData) data : new FFDSpawnGroupData(data);
        NATURAL_SPAWN_GROUP.set(group);
        return group.delegate;
    }

    public static IEntityLivingData finishNaturalSpawn(EntityLiving entity,
                                                        IEntityLivingData data) {
        FFDSpawnGroupData group = NATURAL_SPAWN_GROUP.get();
        NATURAL_SPAWN_GROUP.remove();
        if (group == null) {
            return data;
        }
        group.delegate = data;
        if (entity instanceof EntityAgeable && group.groupSize > 0
                && entity.world.rand.nextFloat() < 0.05F) {
            ((EntityAgeable) entity).setGrowingAge(-24000);
        }
        group.groupSize++;
        return group;
    }

    private static final class FFDSpawnGroupData implements IEntityLivingData {
        private IEntityLivingData delegate;
        private int groupSize;

        private FFDSpawnGroupData(IEntityLivingData delegate) {
            this.delegate = delegate;
        }
    }

    public static boolean holdClimbingPosition(boolean sneaking, EntityLivingBase entity) {
        return sneaking && entity.world.getBlockState(new BlockPos(entity.posX, entity.posY, entity.posZ))
                .getBlock() != FFDBlocks.SCAFFOLDING;
    }

    public static float explosionDropChance(float original, Entity source, World world) {
        if (!(source instanceof EntityTNTPrimed) && !(source instanceof EntityMinecartTNT)) {
            return original;
        }
        return world.getGameRules().getBoolean("tntExplosionDropDecay") ? original : 1.0F;
    }

    public static void ensurePortingGameRules(GameRules rules) {
        if (!rules.hasRule("tntExplosionDropDecay")) {
            rules.addGameRule("tntExplosionDropDecay", "false", GameRules.ValueType.BOOLEAN_VALUE);
        }
        if (!rules.hasRule("projectilesCanBreakBlocks")) {
            rules.addGameRule("projectilesCanBreakBlocks", "true", GameRules.ValueType.BOOLEAN_VALUE);
        }
    }

    public static boolean breakChorusFlowerOnProjectile(Entity projectile, RayTraceResult hit) {
        if (projectile.world.isRemote || !projectile.world.getGameRules().getBoolean("projectilesCanBreakBlocks")
                || !(projectile instanceof EntityArrow || projectile instanceof EntityFireball
                || projectile instanceof net.minecraft.entity.projectile.EntitySnowball
                || projectile instanceof net.minecraft.entity.projectile.EntityEgg
                || projectile instanceof net.minecraft.entity.item.EntityFireworkRocket) || hit == null
                || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        if (blockFlowerChorus == null) {
            blockFlowerChorus = Block.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(
                    "minecraft:chorus_flower"));
        }
        if (projectile.world.getBlockState(pos).getBlock() != blockFlowerChorus) {
            return false;
        }
        Entity owner = projectile instanceof EntityArrow ? ((EntityArrow) projectile).shootingEntity
                : projectile instanceof EntityFireball ? ((EntityFireball) projectile).shootingEntity
                : projectile instanceof net.minecraft.entity.item.EntityFireworkRocket
                ? FFDFireworkHooks.getShooter(
                        (net.minecraft.entity.item.EntityFireworkRocket) projectile)
                : projectile instanceof EntityThrowable ? ((EntityThrowable) projectile).getThrower() : null;
        if (owner != null && !(owner instanceof EntityPlayer)
                && !projectile.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
        }
        Block block = projectile.world.getBlockState(pos).getBlock();
        if (owner == null && projectile.world.getWorldInfo().getGameType()
                == net.minecraft.world.GameType.ADVENTURE
                && !canProjectileBreak(projectile, block)) {
            return false;
        }
        if (owner instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) owner;
            boolean adventure = player instanceof EntityPlayerMP
                    ? ((EntityPlayerMP) player).interactionManager.getGameType() == net.minecraft.world.GameType.ADVENTURE
                    : !player.capabilities.allowEdit;
            if (adventure && !canProjectileBreak(projectile, block)
                    || !projectile.world.isBlockModifiable(player, pos)) {
                return false;
            }
        }
        return projectile.world.destroyBlock(pos, true);
    }

    public static void captureProjectileCanDestroy(Entity projectile, ItemStack ammunition) {
        if (projectile == null || ammunition == null || !ammunition.hasTagCompound()
                || !ammunition.getTagCompound().hasKey("CanDestroy", 9)) {
            return;
        }
        NBTTagList canDestroy = ammunition.getTagCompound().getTagList("CanDestroy", 8);
        projectile.getEntityData().setTag("ffdCanDestroy", canDestroy.copy());
    }

    private static boolean canProjectileBreak(Entity projectile, Block block) {
        NBTTagCompound data = projectile.getEntityData();
        if (!data.hasKey("ffdCanDestroy", 9)) {
            return false;
        }
        ItemStack permissions = new ItemStack(Items.ARROW);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("CanDestroy", data.getTagList("ffdCanDestroy", 8).copy());
        permissions.setTagCompound(tag);
        return permissions.canDestroy(block);
    }

    public static void dropCreeperDiscs(LivingDropsEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();
        if (!(victim instanceof EntityCreeper) || !(attacker instanceof AbstractSkeleton
                || attacker instanceof net.minecraft.entity.passive.EntitySkeletonHorse)
                || attacker instanceof EntitySkeleton
                || !victim.world.getGameRules().getBoolean("doMobLoot")) {
            return;
        }
        WorldServer world = (WorldServer) victim.world;
        net.minecraft.world.storage.loot.LootContext context =
                xy177.farmersfuturedelight.common.FFDEntityLootTables.deathContext(
                        world, victim, event.getSource(), event.isRecentlyHit());
        for (ItemStack stack : world.getLootTableManager().getLootTableFromLocation(
                xy177.farmersfuturedelight.common.FFDEntityLootTables.CREEPER_MUSIC_DISCS)
                .generateLootForPools(victim.getRNG(), context)) {
            event.getDrops().add(new EntityItem(world, victim.posX, victim.posY, victim.posZ, stack));
        }
    }

    public static boolean dryPlacedSponge(World world, BlockPos pos, IBlockState state) {
        if (!state.getValue(net.minecraft.block.BlockSponge.WET)
                || !world.provider.doesWaterVaporize()) {
            return false;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, Blocks.SPONGE.getDefaultState(), 3);
            if (world instanceof WorldServer) {
                for (int particle = 0; particle < 8; particle++) {
                    ((WorldServer) world).spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD,
                            pos.getX() + world.rand.nextDouble(), pos.getY() + 1.2D,
                            pos.getZ() + world.rand.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            world.playSound(null, pos, FFDSounds.WET_SPONGE_DRIES, SoundCategory.BLOCKS,
                    1.0F, (1.0F + world.rand.nextFloat() * 0.2F) * 0.7F);
        }
        return true;
    }

    public static void consumeFood(ItemStack stack, int count, EntityLivingBase consumer) {
        if (!(consumer instanceof EntityPlayer)
                || !((EntityPlayer) consumer).capabilities.isCreativeMode) {
            stack.shrink(count);
        }
    }

    public static ItemStack foodRemainder(ItemStack remainder, ItemStack original,
                                          EntityLivingBase consumer) {
        if (consumer instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) consumer;
            if (player.capabilities.isCreativeMode) {
                return original;
            }
            if (!original.isEmpty()) {
                if (!player.inventory.addItemStackToInventory(remainder)) {
                    player.dropItem(remainder, false);
                }
                return original;
            }
        }
        return remainder;
    }

    public static boolean cannotBePickedUpByBoat(Entity entity) {
        return entity instanceof net.minecraft.entity.passive.EntityWaterMob
                || entity instanceof xy177.farmersfuturedelight.common.entity.EntityDolphin;
    }

    public static int biomeCreatureTypeSwitch(int[] switchTable, int ordinal) {
        return ordinal < switchTable.length ? switchTable[ordinal] : 0;
    }

    public static void onArrowImpact(EntityArrow arrow, RayTraceResult hit) {
        if (arrow.world.isRemote || !FFDItems.isCandleEnabled() || !arrow.isBurning()
                || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        IBlockState state = arrow.world.getBlockState(hit.getBlockPos());
        if (state.getBlock() instanceof BlockAbstractCandle) {
            BlockAbstractCandle candle = (BlockAbstractCandle) state.getBlock();
            if (candle.canLight(state)) {
                candle.setLit(arrow.world, hit.getBlockPos(), state, true);
            }
        }
    }

    public static SoundEvent playerHurtSound(SoundEvent original, DamageSource source) {
        return "sweetBerryBush".equals(source.damageType)
                ? FFDSounds.PLAYER_HURT_SWEET_BERRY_BUSH
                : original;
    }

    public static SoundEvent playerSwimSound(SoundEvent original) {
        return FFDSounds.PLAYER_SWIM;
    }

    public static SoundEvent playerSplashSound(SoundEvent original, EntityPlayer player) {
        Entity passenger = player.getControllingPassenger();
        Entity moving = player.isBeingRidden() && passenger != null ? passenger : player;
        float multiplier = moving == player ? 0.2F : 0.9F;
        float volume = Math.min(1.0F, MathHelper.sqrt(moving.motionX * moving.motionX * (double) 0.2F
                + moving.motionY * moving.motionY
                + moving.motionZ * moving.motionZ * (double) 0.2F) * multiplier);
        return volume < 0.25F ? FFDSounds.PLAYER_SPLASH : FFDSounds.PLAYER_SPLASH_HIGH_SPEED;
    }

    public static byte sweetBerryHurtStatus(byte status, EntityLivingBase entity,
                                           DamageSource source) {
        return status == 2 && entity instanceof EntityPlayer
                && "sweetBerryBush".equals(source.damageType) ? (byte) 44 : status;
    }

    public static byte normalizeSweetBerryHurtStatus(byte status) {
        return status == 44 ? (byte) 2 : status;
    }

    public static DamageSource sweetBerryHurtSource(DamageSource original, byte status) {
        return status == 44 ? SWEET_BERRY_DAMAGE : original;
    }

    public static void markSweetBerrySlowdown(Entity entity) {
        entity.fallDistance = 0.0F;
        SWEET_BERRY_SLOWDOWN.put(entity, Boolean.TRUE);
    }

    public static double consumeSweetBerrySlowdown(Entity entity, MoverType type) {
        if (!SWEET_BERRY_SLOWDOWN.containsKey(entity) || SWEET_BERRY_SLOWDOWN.remove(entity) == null) {
            return 1.0D;
        }
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        return type != MoverType.PISTON
                && !(entity instanceof EntityPlayerMP && type == MoverType.PLAYER)
                ? (double) 0.8F : 1.0D;
    }

    public static double sweetBerryVerticalMultiplier(double horizontal) {
        return horizontal < 1.0D ? 0.75D : 1.0D;
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
        if (record.isEmpty()) return original;
        if (record.getItem() == FFDItems.MUSIC_DISC_OTHERSIDE) return 14;
        return record.getItem() == FFDItems.MUSIC_DISC_PIGSTEP ? 13 : original;
    }

    public static boolean updateBeaconSounds(TileEntityBeacon beacon, boolean active,
                                               boolean wasActive) {
        if (beacon == null || beacon.getWorld() == null || beacon.getWorld().isRemote) {
            return active;
        }
        World world = beacon.getWorld();
        BlockPos pos = beacon.getPos();
        if (active && world.getTotalWorldTime() % 80L == 0L) {
            world.playSound(null, pos, FFDSounds.BEACON_AMBIENT,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        if (active != wasActive) {
            world.playSound(null, pos, active ? FFDSounds.BEACON_ACTIVATE
                            : FFDSounds.BEACON_DEACTIVATE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return active;
    }

    public static void handleBeaconFieldUpdate(TileEntityBeacon beacon, int fieldId,
                                                boolean active) {
        if (fieldId != 1 || !active || beacon == null || beacon.getWorld() == null
                || beacon.getWorld().isRemote) {
            return;
        }
        beacon.getWorld().playSound(null, beacon.getPos(), FFDSounds.BEACON_POWER_SELECT,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public static void applyModernBlockProperties() {
        Blocks.WATER.setLightOpacity(1);
        Blocks.FLOWING_WATER.setLightOpacity(1);
        modernSound(Blocks.NETHERRACK, FFDSounds.NETHERRACK);
        modernSound(Blocks.QUARTZ_ORE, FFDSounds.NETHER_ORE);
        modernSound(Blocks.SOUL_SAND, FFDSounds.SOUL_SAND);
        modernSound(Blocks.NETHER_BRICK, FFDSounds.NETHER_BRICKS);
        modernSound(Blocks.NETHER_BRICK_FENCE, FFDSounds.NETHER_BRICKS);
        modernSound(Blocks.NETHER_BRICK_STAIRS, FFDSounds.NETHER_BRICKS);
        modernSound(Blocks.RED_NETHER_BRICK, FFDSounds.NETHER_BRICKS);
        modernSound(Blocks.NETHER_WART, FFDSounds.NETHER_WART);
        if (Blocks.ENCHANTING_TABLE != null) {
            Blocks.ENCHANTING_TABLE.setLightLevel(FFDConfig.enchantingTableEmitsLight
                    ? 7.0F / 15.0F : 0.0F);
        }
        if (FFDItems.isCrimsonEnabled() && Blocks.NETHER_WART_BLOCK != null) {
            ObfuscationReflectionHelper.setPrivateValue(Block.class, Blocks.NETHER_WART_BLOCK,
                    FFDSounds.WART_BLOCK, "blockSoundType", "field_149762_H");
        }
    }

    private static void modernSound(Block block, net.minecraft.block.SoundType sound) {
        ObfuscationReflectionHelper.setPrivateValue(Block.class, block, sound,
                "blockSoundType", "field_149762_H");
    }

    public static boolean canTickGrass(boolean areaLoaded, World world, BlockPos pos) {
        if (!areaLoaded) {
            return false;
        }
        if (grassNeedsDecay(world, pos)) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            return false;
        }
        return true;
    }

    public static boolean grassNeedsDecay(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != Blocks.GRASS) {
            return false;
        }
        BlockPos abovePos = pos.up();
        IBlockState above = world.getBlockState(abovePos);
        if (WaterloggedBlockApi.isWaterlogged(above)
                || WaterloggedBlockApi.isFluidSource(world, abovePos)
                || above.getBlock() instanceof BlockLiquid
                        && above.getValue(BlockLiquid.LEVEL) >= 8) {
            return true;
        }
        return false;
    }

    public static boolean setGrassTickState(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() == Blocks.GRASS
                && !canGrassSpreadAt(world, pos.getX(), pos.getY(), pos.getZ())) {
            return false;
        }
        return world.setBlockState(pos, state);
    }

    public static boolean canGrassSpreadAt(World world, int x, int y, int z) {
        IBlockState above = world.getBlockState(new BlockPos(x, y + 1, z));
        return !above.getMaterial().isLiquid() && !WaterloggedBlockApi.isWaterlogged(above)
                && WaterloggedBlockApi.getFluidForBlock(above.getBlock()) == null;
    }

    public static boolean blocksFlowingWater(Block liquid, IBlockState targetState,
                                              World world, BlockPos pos) {
        if (targetState.getBlock() instanceof net.minecraft.block.BlockSign
                || WaterloggedBlockApi.isWaterlogged(world, pos)
                || FFDConfig.isAdditionalWaterloggingBlock(world, targetState.getBlock())) return true;
        return liquid == Blocks.FLOWING_WATER && targetState != null
                && !WaterloggedBlockApi.isWaterlogged(targetState)
                && WaterloggedBlockApi.canBeWaterlogged(targetState);
    }

    public static IBlockState waterlogPlacedBlockState(IBlockState placedState, World world,
                                                        BlockPos pos) {
        Fluid fluid = world == null || pos == null ? null
                : WaterloggedBlockApi.getSourceFluid(world, pos);
        if (placedState == null || fluid == null || !FFDConfig.isFluidAllowed(world, fluid)) {
            return placedState;
        }
        IBlockState wetState = WaterloggedBlockApi.withWaterlogged(placedState, true);
        if (wetState != null && !world.isRemote) {
            world.scheduleUpdate(pos, wetState.getBlock(),
                    WaterloggedBlockApi.fluidTickRate(world, fluid));
        }
        return wetState == null ? placedState : wetState;
    }

    public static boolean removeWaterloggedBlockByPlayer(World world, BlockPos pos,
                                                         IBlockState removedState) {
        return setBlockStateAfterRemoval(world, pos, Blocks.AIR.getDefaultState(),
                world.isRemote ? 11 : 3, removedState);
    }

    public static boolean setBlockStateAfterRemoval(World world, BlockPos pos,
                                                     IBlockState replacement, int flags,
                                                     IBlockState removedState) {
        IBlockState next = WaterloggedBlockApi.isWaterlogged(world, pos)
                ? WaterloggedBlockApi.getReplacementState(world, pos, removedState) : replacement;
        boolean changed = world.setBlockState(pos, next, flags);
        if (changed) {
            WaterloggedBlockApi.recordContainedFluid(world, pos, null);
        }
        return changed;
    }

    public static boolean setBlockStateAfterShearing(World world, BlockPos pos,
                                                     IBlockState replacement, int flags) {
        return setBlockStateAfterRemoval(world, pos, replacement, flags, world.getBlockState(pos));
    }

    public static boolean setBlockToAirAfterExplosion(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        IBlockState replacement = WaterloggedBlockApi.getReplacementState(world, pos, state);
        boolean changed = world.setBlockState(pos, replacement, 3);
        if (changed) {
            WaterloggedBlockApi.recordContainedFluid(world, pos, null);
        }
        return changed;
    }

    public static void handleUniversalWaterloggedNeighborChanged(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (WaterloggedBlockApi.hasUniversalWaterloggedProperty(state)
                || xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.has(world, pos)) {
            WaterloggedBlockApi.onNeighborChanged(world, pos, state.getBlock());
        }
    }

    public static void updateBlockTickWithWaterlogging(Block block, World world, BlockPos pos,
                                                        IBlockState state, Random random) {
        if (!world.isRemote && WaterloggedBlockApi.getFluidForBlock(block) != null) {
            if (xy177.farmersfuturedelight.common.world.modern.ModernGenerationLifecycle
                    .deferFluidTick(world, pos, block)) return;
            boolean mixed = xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid
                    .tryMixFluidNeighbors(world, pos, state);
            if (mixed && WaterloggedBlockApi.getFluidForBlock(world.getBlockState(pos).getBlock())
                    == null) {
                return;
            }
        }
        block.updateTick(world, pos, state, random);
        IBlockState current = world.getBlockState(pos);
        if (pos.getY() == 0 && FFDHeightHooks.isExtended(world)
                && FFDHeightHooks.minY(world) == 0) {
            xy177.farmersfuturedelight.common.world.FFDWorldgenFluidUpdates.queue(world, pos);
        }
        if (WaterloggedBlockApi.isWaterlogged(world, pos)) {
            WaterloggedBlockApi.updateTick(world, pos, current);
        } else if (!WaterloggedBlockApi.isWaterlogged(current)) {
            xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid
                    .tryFormWaterSource(world, pos, current);
        }
    }

    public static Comparable<?> getLiquidLevelValue(IBlockState state, IProperty<?> property) {
        if (state == null || property == null) {
            return Integer.valueOf(0);
        }
        if (property == BlockLiquid.LEVEL
                && !state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return Integer.valueOf(0);
        }
        return state.getValue(property);
    }

    public static IBlockState fluidloggedApiFluidState(IBlockState state) {
        if (state != null && state.getMaterial() == Material.WATER
                && state.getBlock() instanceof xy177.farmersfuturedelight.api.IWaterloggableBlock
                && ((xy177.farmersfuturedelight.api.IWaterloggableBlock) state.getBlock())
                        .isWaterloggedState(state)) {
            return Blocks.WATER.getDefaultState();
        }
        return state;
    }

    public static Block snowCoverBlock(Block block) {
        return block instanceof BlockPowderSnow ? Blocks.SNOW : block;
    }

    public static IBlockState farmlandFluidState(IBlockAccess world, BlockPos pos, IBlockState state) {
        if (world instanceof World && !((World) world).isBlockLoaded(pos)) {
            return state;
        }
        IBlockState stored = FFDStoredFluidStates.get(world, pos);
        if (stored != null) {
            return stored;
        }
        return WaterloggedBlockApi.isWaterlogged(state)
                ? WaterloggedBlockApi.getFluidState(world, pos) : state;
    }

    public static boolean farmlandShouldTurnToDirt(Material aboveMaterial, World world,
                                                    BlockPos farmlandPos) {
        if (!aboveMaterial.isSolid()) {
            return false;
        }
        return !farmlandHasMaintainingBlock(world, farmlandPos);
    }

    public static boolean farmlandHasMaintainingBlock(World world, BlockPos farmlandPos) {
        Block above = world.getBlockState(farmlandPos.up()).getBlock();
        return above == Blocks.PISTON_EXTENSION || above instanceof BlockFenceGate;
    }

    public static boolean isWaterloggedStateForLiquid(IBlockState state) {
        return WaterloggedBlockApi.isWaterlogged(state);
    }

    public static int waterloggedLiquidDepth(Block liquid) {
        return liquid.getDefaultState().getMaterial() == Material.WATER ? 0 : -1;
    }

    public static int waterloggedLiquidDepth(int depth, Block liquid, IBlockAccess world,
            BlockPos pos) {
        if (liquid == null || world == null || pos == null
                || !WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return depth;
        }
        Fluid liquidFluid = WaterloggedBlockApi.getFluidForBlock(liquid);
        return liquidFluid != null
                && WaterloggedBlockApi.getContainedFluid(world, pos) == liquidFluid
                ? xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.level(world, pos) : -1;
    }

    public static boolean isContainedFluidSource(Block liquid, IBlockAccess world, BlockPos pos) {
        if (liquid == null || world == null || pos == null
                || !WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return false;
        }
        Fluid liquidFluid = WaterloggedBlockApi.getFluidForBlock(liquid);
        return liquidFluid != null
                && WaterloggedBlockApi.getSourceFluid(world, pos) == liquidFluid;
    }

    public static boolean hasContainedFluidAlongDensity(Block liquid, IBlockAccess world,
            BlockPos pos, int densityDirection) {
        return pos != null && WaterloggedBlockApi.isWaterlogged(world, pos.down(densityDirection))
                && WaterloggedBlockApi.getContainedFluid(world, pos.down(densityDirection))
                    == WaterloggedBlockApi.getFluidForBlock(liquid);
    }

    public static int containedFluidQuanta(int original, Block liquid, IBlockAccess world,
                                          BlockPos pos, int quanta) {
        if (!WaterloggedBlockApi.isWaterlogged(world, pos)) return original;
        return WaterloggedBlockApi.getContainedFluid(world, pos) == WaterloggedBlockApi.getFluidForBlock(liquid)
                ? Math.max(0, quanta - xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.level(world, pos))
                : original;
    }

    public static boolean protectContainedHost(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.isWaterlogged(world, pos)
                || FFDConfig.isAdditionalWaterloggingBlock(world, world.getBlockState(pos).getBlock());
    }

    public static boolean isWaterloggedFlowOccluded(Block liquid, World world,
            BlockPos source, BlockPos neighbor) {
        Fluid liquidFluid = liquid == null ? null : WaterloggedBlockApi.getFluidForBlock(liquid);
        if (liquidFluid == null || !WaterloggedBlockApi.isWaterlogged(world, neighbor)
                || WaterloggedBlockApi.getContainedFluid(world, neighbor) != liquidFluid) {
            return false;
        }
        EnumFacing side = EnumFacing.getFacingFromVector(neighbor.getX() - source.getX(),
                neighbor.getY() - source.getY(), neighbor.getZ() - source.getZ());
        return !xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid
                .canPassThrough(world, source, neighbor, side);
    }

    public static int waterloggedDepthAbove(int depth, Block liquid, World world, BlockPos pos) {
        depth = waterloggedLiquidDepth(depth, liquid, world, pos.up());
        return depth >= 0 && isWaterloggedFlowOccluded(liquid, world, pos, pos.up()) ? -1 : depth;
    }

    public static int getFluidloggedLightValue(Block block, IBlockState state,
                                                IBlockAccess world, BlockPos pos) {
        return getFluidloggedLightValue(block.getLightValue(state, world, pos), world, pos);
    }

    public static int getFluidloggedLightValue(int light, IBlockAccess world, BlockPos pos) {
        int prepared = xy177.farmersfuturedelight.common.world.modern.ModernLightAccess
                .containedLight(light, world, pos);
        if (prepared >= 0) return prepared;
        if (!WaterloggedBlockApi.isWaterlogged(world, pos) || !(world instanceof World)) {
            return light;
        }
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        return fluid == null ? light
                : Math.max(light, fluid.getLuminosity((World) world, pos));
    }

    public static Boolean isEntityInsideWaterloggedMaterial(Boolean original,
                                                             IBlockState state,
                                                             World world,
                                                             BlockPos pos,
                                                             Material material) {
        return original != null ? original
                : isContainedMaterial(state, world, pos, material) ? Boolean.TRUE : null;
    }

    public static Boolean isAABBInsideFluidloggedMaterial(Boolean original,
                                                           IBlockState state,
                                                           World world,
                                                           BlockPos pos,
                                                           Material material) {
        return original != null ? original
                : isContainedMaterial(state, world, pos, material) ? Boolean.TRUE : null;
    }

    private static boolean isContainedMaterial(IBlockState state, World world, BlockPos pos,
                                               Material material) {
        if (!WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return false;
        }
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        return WaterloggedBlockApi.sourceState(fluid).getMaterial() == material;
    }

    public static boolean hasWaterBreathingOrConduit(EntityLivingBase entity, Potion potion) {
        return entity != null && (entity.isPotionActive(potion)
                || potion == MobEffects.WATER_BREATHING
                        && (FFDPotions.hasConduitPower(entity)
                                || FFDConfig.isConfiguredEntity(FFDConfig.waterDrowningImmuneMobs,
                                        entity)));
    }

    public static boolean isSubmergedForBreathing(EntityLivingBase entity, Material material) {
        return entity.isInsideOfMaterial(material)
                && (material != Material.WATER || !isInBubbleColumn(entity));
    }

    private static boolean isInBubbleColumn(EntityLivingBase entity) {
        return entity.world.getBlockState(new BlockPos(entity.posX,
                entity.posY + entity.getEyeHeight(), entity.posZ)).getBlock() == FFDBlocks.BUBBLE_COLUMN;
    }

    public static int recoverAirSupply(int original, EntityLivingBase entity) {
        return entity instanceof EntityPlayer && (isSwimmingSystemEnabled() || isInBubbleColumn(entity))
                ? Math.min(original, entity.getAir() + 4) : original;
    }

    public static boolean updateBreathingInWater(boolean submerged, EntityLivingBase entity) {
        if (submerged && isInBubbleColumn(entity)) {
            return false;
        }
        if (submerged && entity instanceof EntityPlayer && entity.isEntityAlive() && entity.getAir() < 300
                && (((EntityPlayer) entity).capabilities.disableDamage
                        || hasWaterBreathingOrConduit(entity, MobEffects.WATER_BREATHING))) {
            entity.setAir(Math.min(300, entity.getAir() + 4));
        }
        return submerged;
    }

    public static Material blockReplacementMaterial(IBlockState state) {
        IBlockState dry = WaterloggedBlockApi.isWaterlogged(state)
                ? WaterloggedBlockApi.withWaterlogged(state, false) : null;
        return dry == null ? state.getMaterial() : dry.getMaterial();
    }

    public static boolean canReplaceFluidloggedBlock(boolean allowed, World world, BlockPos pos) {
        if (!allowed) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        return !WaterloggedBlockApi.isWaterlogged(world, pos)
                || state.getBlock().isReplaceable(world, pos);
    }

    public static float dolphinsGraceWaterDrag(EntityLivingBase entity, float drag) {
        return FFDPotions.hasDolphinsGrace(entity) ? 0.96F : drag;
    }

    public static boolean shouldSinkInWater(EntityLivingBase entity) {
        return entity != null && entity.world != null
                && !entity.world.isRemote
                && entity.isInWater()
                && FFDConfig.isConfiguredEntity(FFDConfig.waterSinkingMobs, entity);
    }

    public static float waterSinkingVertical(EntityLivingBase entity, float vertical) {
        if (!shouldSinkInWater(entity)) {
            return vertical;
        }
        return 0.0F;
    }

    public static int waterSinkingPathY(PathPoint point, WalkNodeProcessor processor,
                                        IBlockAccess access, EntityLiving entity) {
        if (point == null) {
            return 0;
        }
        if (point.nodeType != PathNodeType.WATER || processor.getCanSwim()
                || !shouldSinkInWater(entity)) {
            return point.y;
        }
        int lowestY = point.y;
        int minimumY = FFDHeightHooks.minY(entity);
        int width = MathHelper.floor(entity.width + 1.0F);
        int height = MathHelper.floor(entity.height + 1.0F);
        while (lowestY - 1 > minimumY
                && processor.getPathNodeType(access, point.x, lowestY - 1, point.z,
                        entity, width, height, width, processor.getCanOpenDoors(),
                        processor.getCanEnterDoors()) == PathNodeType.WATER) {
            lowestY--;
        }
        return lowestY;
    }

    public static void advanceWaterSinkingPath(EntityLiving entity) {
        if (!shouldSinkInWater(entity) || !entity.onGround
                || entity.getNavigator().getNodeProcessor().getCanSwim()) {
            return;
        }
        Path path = entity.getNavigator().getPath();
        if (path == null || path.getCurrentPathIndex() + 1 >= path.getCurrentPathLength()) {
            return;
        }
        PathPoint current = path.getPathPointFromIndex(path.getCurrentPathIndex());
        PathPoint next = path.getPathPointFromIndex(path.getCurrentPathIndex() + 1);
        if (current.nodeType != PathNodeType.WATER || next.nodeType != PathNodeType.WATER) {
            return;
        }
        Vec3d towardCurrent = new Vec3d(current.x + 0.5D - entity.posX,
                current.y - entity.posY, current.z + 0.5D - entity.posZ);
        double currentDistance = towardCurrent.lengthSquared();
        if (currentDistance >= 4.0D) {
            return;
        }
        Vec3d towardNext = new Vec3d(next.x + 0.5D - entity.posX,
                next.y - entity.posY, next.z + 0.5D - entity.posZ);
        if ((currentDistance < 0.5D || towardNext.lengthSquared() < currentDistance)
                && towardNext.normalize().dotProduct(towardCurrent.normalize()) < 0.0D) {
            path.incrementPathIndex();
        }
    }

    public static Block pumpkinStemFruit(Block original) {
        return original == Blocks.PUMPKIN && FFDItems.isPumpkinEnabled()
                && FFDItems.isItemRegistered(FFDItems.PUMPKIN) ? FFDBlocks.PUMPKIN : original;
    }

    public static boolean generateUncarvedPumpkin(World world, BlockPos pos,
                                                 IBlockState state, int flags) {
        Block fruit = pumpkinStemFruit(state.getBlock());
        return world.setBlockState(pos, fruit == state.getBlock() ? state : fruit.getDefaultState(), flags);
    }

    public static double waterJumpImpulse(double original, EntityLivingBase entity) {
        return shouldSinkInWater(entity) ? 0.0D : original;
    }

    public static double waterSinkingGravity(double original, EntityLivingBase entity) {
        return shouldSinkInWater(entity) ? 0.005D : original;
    }

    public static boolean itemWaterBuoyancy(boolean noGravity,
                                            net.minecraft.entity.item.EntityItem item) {
        if (isNetheriteItemEntity(item) && isItemSubmerged(item, Material.LAVA)) {
            applyItemFluidMovement(item, (double) 0.95F);
            return true;
        }
        if (Loader.isModLoaded("aquaacrobatics")) {
            return noGravity;
        }
        if (!isItemSubmerged(item, Material.WATER)) {
            return noGravity;
        }
        if (FFDConfig.isConfiguredItem(FFDConfig.waterSinkingItems, item.getItem())) {
            return noGravity;
        }
        applyItemFluidMovement(item, (double) 0.99F);
        return true;
    }

    private static boolean isItemSubmerged(EntityItem item, Material material) {
        double y = item.getEntityBoundingBox().minY + (double) 0.1F;
        BlockPos pos = new BlockPos(item.posX, y, item.posZ);
        IBlockState state = item.world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLiquid && state.getMaterial() == Material.LAVA
                && material == Material.LAVA) {
            float height = item.world.getBlockState(pos.up()).getMaterial() == Material.LAVA
                    ? 1.0F : 1.0F - BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));
            return y < pos.getY() + height;
        }
        if (state.getBlock() instanceof BlockLiquid && state.getMaterial() == material
                && y >= pos.getY() + BlockLiquid.getBlockLiquidHeight(state, item.world, pos)) {
            return false;
        }
        AxisAlignedBB slice = new AxisAlignedBB(item.posX - 0.001D, y, item.posZ - 0.001D,
                item.posX + 0.001D, y + 0.001D, item.posZ + 0.001D);
        return item.world.isMaterialInBB(slice, material);
    }

    private static void applyItemFluidMovement(EntityItem item, double drag) {
        if (item.motionY < (double) 0.06F) {
            item.motionY += (double) 0.0005F;
        }
        item.motionX *= drag;
        item.motionZ *= drag;
    }

    public static void updateNetheriteItemFireImmunity(EntityItem item) {
        if (isNetheriteItemEntity(item)) {
            if (!item.isImmuneToFire()) {
                ObfuscationReflectionHelper.setPrivateValue(Entity.class, item, true,
                        "field_70178_ae", "isImmuneToFire");
            }
            item.extinguish();
        }
    }

    public static boolean cancelNetheriteItemFireDamage(EntityItem entityItem,
                                                        DamageSource source) {
        return source.isFireDamage() && isNetheriteItemEntity(entityItem);
    }

    public static boolean isNetheriteItemEntity(EntityItem entityItem) {
        if (entityItem == null) {
            return false;
        }
        ItemStack stack = entityItem.getItem();
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof INetheriteItem;
    }

    public static boolean isClimbableStep(Block block, Block ladder, Entity entity) {
        BlockPos pos = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.2D,
                entity.posZ);
        return block == ladder || entity instanceof EntityLivingBase
                && block.isLadder(entity.world.getBlockState(pos), entity.world, pos,
                        (EntityLivingBase) entity);
    }

    public static SoundEvent climbingStepSound(SoundEvent original, Block block) {
        return block == Blocks.VINE ? FFDSounds.VINE_STEP : original;
    }

    public static Vec3d modifyWaterloggedAcceleration(Block block, World world, BlockPos pos,
                                                       Entity entity, Vec3d motion,
                                                       Material material) {
        if (!WaterloggedBlockApi.isWaterlogged(world, pos)) {
            return block.modifyAcceleration(world, pos, entity, motion);
        }
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(world, pos);
        if (WaterloggedBlockApi.sourceState(fluid).getMaterial() != material) {
            return block.modifyAcceleration(world, pos, entity, motion);
        }
        return material == Material.WATER
                ? WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion) : motion;
    }

    public static IProperty<?>[] appendUniversalWaterloggedProperty(
            Block block, IProperty<?>[] properties) {
        return WaterloggedBlockApi.appendUniversalWaterloggedProperty(block, properties);
    }

    public static boolean hasOnlyUniversalWaterloggedProperty(IBlockState state) {
        return state.getPropertyKeys().size() == 1
                && WaterloggedBlockApi.hasUniversalWaterloggedProperty(state);
    }

    public static int encodeUniversalWaterloggedStateId(int stateId, IBlockState state) {
        return WaterloggedBlockApi.encodeUniversalStateId(stateId, state);
    }

    public static IBlockState decodeUniversalWaterloggedStateId(IBlockState state, int stateId) {
        return WaterloggedBlockApi.decodeUniversalStateId(state, stateId);
    }

    public static Map<IProperty<?>, Comparable<?>> withoutUniversalWaterloggedProperty(
            Map<IProperty<?>, Comparable<?>> properties) {
        return WaterloggedBlockApi.withoutUniversalWaterloggedProperty(properties);
    }

    public static boolean isSwimmingSystemEnabled() {
        return !FFDConfig.isAquaAcrobaticsCompatibilityEnabled();
    }

    public static boolean isSwimming(EntityPlayer player) {
        if (!isSwimmingSystemEnabled() || player == null || player.capabilities.isFlying
                || player.isSpectator()) {
            return false;
        }
        DataParameter<Byte> flags = entityFlags();
        return flags != null && (player.getDataManager().get(flags).byteValue() & 16) != 0;
    }

    public static void updateSwimmingState(EntityPlayer player) {
        if (!isSwimmingSystemEnabled() || player == null) {
            return;
        }
        boolean swimming = isSwimming(player);
        boolean next;
        if (player.capabilities.isFlying || player.isSpectator() || player.isRiding()
                || player.isPlayerSleeping()) {
            next = false;
        } else if (player.isInWater()) {
            next = swimming ? player.isSprinting()
                    : player.isSprinting() && player.isInsideOfMaterial(Material.WATER);
        } else {
            next = false;
        }
        setSwimming(player, next);
    }

    public static boolean useSwimmingSize(EntityPlayer player) {
        if (player == null || !isSwimmingSystemEnabled() || player.isPlayerSleeping()
                || player.isSpectator() || player.isRiding()) {
            return false;
        }
        if (!canPlayerFitAtHeight(player, 0.6D)) {
            return hasSwimmingPose(player);
        }
        return isSwimming(player) || !player.isElytraFlying()
                && !canPlayerFitAtHeight(player, 1.8D)
                && !canPlayerFitAtHeight(player, 1.5D);
    }

    public static boolean hasSwimmingPose(EntityPlayer player) {
        return isSwimmingSystemEnabled() && player.height == 0.6F
                && !player.isElytraFlying() && !player.isPlayerSleeping();
    }

    public static float crouchingHeight(float original) {
        return isSwimmingSystemEnabled() ? 1.5F : original;
    }

    public static float playerEyeHeight(float original, EntityPlayer player) {
        if (player == null || !isSwimmingSystemEnabled()
                || player.isPlayerSleeping() || player.isRiding()) {
            return original;
        }
        if (player.isElytraFlying() || hasSwimmingPose(player)) {
            return 0.4F;
        }
        return player.height == 1.5F ? 1.27F : original;
    }

    public static boolean shouldUseCrouchingPose(boolean sneaking, EntityPlayer player) {
        if (player == null || !isSwimmingSystemEnabled()) {
            return sneaking;
        }
        return sneaking && !player.capabilities.isFlying
                || !player.isElytraFlying() && !player.isPlayerSleeping()
                && !player.isSpectator() && !player.isRiding()
                && !canPlayerFitAtHeight(player, 1.8D)
                && canPlayerFitAtHeight(player, 1.5D);
    }

    public static boolean shouldUseCrouchingModelPose(boolean sneaking, Entity entity) {
        return entity instanceof EntityPlayer
                && isSwimmingSystemEnabled() ? entity.height == 1.5F : sneaking;
    }

    private static boolean canPlayerFitAtHeight(EntityPlayer player, double height) {
        AxisAlignedBB bounds = player.getEntityBoundingBox();
        AxisAlignedBB candidate = new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ,
                bounds.maxX, bounds.minY + height, bounds.maxZ).shrink(1.0E-7D);
        return player.world.getCollisionBoxes(player, candidate).isEmpty();
    }

    public static boolean handleSwimmingTravel(EntityPlayer player, float strafe,
                                                 float vertical, float forward) {
        if (!isSwimmingSystemEnabled() || player == null) {
            return false;
        }
        updateSwimmingState(player);
        if (!isSwimming(player) || player.isRiding() || player.capabilities.isFlying
                || !player.isInWater()
                || !player.isServerWorld() && !player.canPassengerSteer()) {
            return false;
        }

        double startX = player.posX;
        double startY = player.posY;
        double startZ = player.posZ;
        Vec3d look = player.getLookVec();
        double lookY = look.y;
        double verticalResponse = lookY < -0.2D ? 0.085D : 0.06D;
        BlockPos upperPos = new BlockPos(player.posX, player.posY + 0.9D, player.posZ);
        if (lookY <= 0.0D || player.motionY > 0.0D
                || WaterloggedBlockApi.containsWater(player.world, upperPos)) {
            player.motionY += (lookY - player.motionY) * verticalResponse;
        }

        double waterStartY = player.posY;
        float drag = player.isSprinting() ? 0.9F : 0.8F;
        float acceleration = 0.02F;
        float depthStrider = (float) EnchantmentHelper.getDepthStriderModifier(player);
        depthStrider = Math.min(depthStrider, 3.0F);
        if (!player.onGround) {
            depthStrider *= 0.5F;
        }
        if (depthStrider > 0.0F) {
            drag += (0.54600006F - drag) * depthStrider / 3.0F;
            acceleration += (player.getAIMoveSpeed() - acceleration) * depthStrider / 3.0F;
        }
        drag = dolphinsGraceWaterDrag(player, drag);

        player.moveRelative(strafe, vertical, forward, acceleration);
        player.move(MoverType.SELF, player.motionX, player.motionY, player.motionZ);
        if (player.collidedHorizontally && player.isOnLadder()) {
            player.motionY = 0.2D;
        }
        player.motionX *= drag;
        player.motionY *= 0.8D;
        player.motionZ *= drag;
        if (!player.hasNoGravity() && !player.isSprinting()) {
            player.motionY -= 0.005D;
        }
        if (player.collidedHorizontally && player.isOffsetPositionInLiquid(
                player.motionX, player.motionY + 0.6D - player.posY + waterStartY,
                player.motionZ)) {
            player.motionY = 0.3D;
        }

        player.prevLimbSwingAmount = player.limbSwingAmount;
        double movedX = player.posX - player.prevPosX;
        double movedZ = player.posZ - player.prevPosZ;
        float limbSpeed = MathHelper.sqrt(movedX * movedX + movedZ * movedZ) * 4.0F;
        limbSpeed = Math.min(limbSpeed, 1.0F);
        player.limbSwingAmount += (limbSpeed - player.limbSwingAmount) * 0.4F;
        player.limbSwing += player.limbSwingAmount;
        player.addMovementStat(player.posX - startX, player.posY - startY,
                player.posZ - startZ);
        return true;
    }

    private static void setSwimming(EntityPlayer player, boolean swimming) {
        DataParameter<Byte> flags = entityFlags();
        if (flags == null) {
            return;
        }
        byte value = player.getDataManager().get(flags).byteValue();
        byte updated = swimming ? (byte) (value | 16) : (byte) (value & ~16);
        if (value != updated) {
            player.getDataManager().set(flags, Byte.valueOf(updated));
        }
    }

    @SuppressWarnings("unchecked")
    private static DataParameter<Byte> entityFlags() {
        if (entityFlagsResolved) {
            return entityFlags;
        }
        entityFlagsResolved = true;
        String[] names = {"FLAGS", "field_184240_ax"};
        for (String name : names) {
            try {
                Field field = Entity.class.getDeclaredField(name);
                field.setAccessible(true);
                entityFlags = (DataParameter<Byte>) field.get(null);
                break;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return entityFlags;
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
