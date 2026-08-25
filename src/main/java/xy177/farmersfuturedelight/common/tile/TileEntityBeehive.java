package xy177.farmersfuturedelight.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.ITickable;

import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityBeehive extends TileEntity implements ITickable {
    public static final int MAX_BEES = 3;
    public static final int MAX_HONEY_LEVEL = 5;

    private final List<BeeOccupant> occupants = new ArrayList<>();
    private int honeyLevel;
    @Nullable
    private BlockPos flowerPos;
    private boolean preserveOnBreak;
    private boolean suppressReleaseOnBreak;
    private boolean pendingHoneyStateSync;

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (isFireNearby()) {
            releaseAll(ReleaseStatus.EMERGENCY, null);
            return;
        }
        if (pendingHoneyStateSync) {
            syncHoneyState();
            pendingHoneyStateSync = false;
        }
        if (occupants.isEmpty()) {
            return;
        }
        boolean changed = false;
        Iterator<BeeOccupant> iterator = occupants.iterator();
        while (iterator.hasNext()) {
            BeeOccupant occupant = iterator.next();
            changed = true;
            if (occupant.ticksInHive++ > occupant.minOccupationTicks
                    && releaseBee(occupant, occupant.hasNectar()
                            ? ReleaseStatus.HONEY_DELIVERED : ReleaseStatus.BEE_RELEASED, null)) {
                iterator.remove();
            }
        }
        if (!occupants.isEmpty() && world.rand.nextDouble() < 0.005D) {
            world.playSound(null, pos, FFDSounds.BEEHIVE_WORK,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        if (changed) {
            markDirty();
        }
    }

    public boolean tryEnterHive(EntityBee bee, boolean hasNectar) {
        if (world == null || world.isRemote || isFullOfBees() || isFireNearby()) {
            return false;
        }
        bee.dismountRidingEntity();
        bee.removePassengers();
        if (bee.getLeashed()) {
            bee.clearLeashed(true, false);
        }
        NBTTagCompound entityData = new NBTTagCompound();
        if (!bee.writeToNBTAtomically(entityData)) {
            return false;
        }
        cleanStoredBeeData(entityData);
        occupants.add(new BeeOccupant(entityData, 0, hasNectar ? 2400 : 600));
        if (bee.getFlowerPos() != null && (flowerPos == null || world.rand.nextBoolean())) {
            flowerPos = bee.getFlowerPos();
        }
        bee.setDead();
        world.playSound(null, pos, FFDSounds.BEE_ENTER_HIVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        markDirty();
        return true;
    }

    public void addNewBees(int count) {
        if (world == null || world.isRemote || !FFDEntities.isLocalBeeEnabled()) {
            return;
        }
        for (int i = 0; i < count && !isFullOfBees(); i++) {
            EntityBee bee = new EntityBee(world);
            bee.setHivePos(pos);
            NBTTagCompound entityData = new NBTTagCompound();
            if (bee.writeToNBTAtomically(entityData)) {
                cleanStoredBeeData(entityData);
                occupants.add(new BeeOccupant(entityData, world.rand.nextInt(599), 600));
            }
        }
        markDirty();
    }

    public boolean isFullOfBees() {
        return occupants.size() >= MAX_BEES;
    }

    public boolean isEmpty() {
        return occupants.isEmpty();
    }

    public int getOccupantCount() {
        return occupants.size();
    }

    public int getHoneyLevel() {
        return honeyLevel;
    }

    public void setHoneyLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(MAX_HONEY_LEVEL, level));
        if (honeyLevel == clampedLevel) {
            return;
        }
        honeyLevel = clampedLevel;
        syncHoneyState();
        markDirty();
    }

    public boolean harvest(EntityPlayer player, EnumHand hand) {
        if (honeyLevel < MAX_HONEY_LEVEL) {
            return false;
        }
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.SHEARS) {
            if (!world.isRemote) {
                held.damageItem(1, player);
                world.playSound(null, pos, FFDSounds.BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                for (int i = 0; i < 3; i++) {
                    Block.spawnAsEntity(world, pos,
                            FFDItems.effectiveStack(FFDItems.HONEYCOMB));
                }
                player.addStat(StatList.getObjectUseStats(Items.SHEARS));
                setHoneyLevel(0);
                if (!isSedated() && !isEmpty()) {
                    angerNearbyBees();
                    releaseAll(ReleaseStatus.EMERGENCY, player);
                }
            }
            return true;
        }
        if (held.getItem() == Items.GLASS_BOTTLE) {
            if (!world.isRemote) {
                held.shrink(1);
                ItemStack honeyBottle = FFDItems.effectiveStack(FFDItems.HONEY_BOTTLE);
                if (held.isEmpty()) {
                    player.setHeldItem(hand, honeyBottle);
                } else if (!player.inventory.addItemStackToInventory(honeyBottle)) {
                    player.dropItem(honeyBottle, false);
                }
                world.playSound(null, player.posX, player.posY, player.posZ,
                        net.minecraft.init.SoundEvents.ITEM_BOTTLE_FILL,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.addStat(StatList.getObjectUseStats(Items.GLASS_BOTTLE));
                setHoneyLevel(0);
                if (!isSedated() && !isEmpty()) {
                    angerNearbyBees();
                    releaseAll(ReleaseStatus.EMERGENCY, player);
                }
            }
            return true;
        }
        return false;
    }

    public void angerBees(@Nullable EntityPlayer player) {
        releaseAll(ReleaseStatus.EMERGENCY, player);
        angerNearbyBees();
    }

    public void angerNearbyBees() {
        if (world == null) {
            return;
        }
        AxisAlignedBB area = new AxisAlignedBB(pos).grow(8.0D, 6.0D, 8.0D);
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, area);
        if (players.isEmpty()) {
            return;
        }
        for (EntityBee bee : world.getEntitiesWithinAABB(EntityBee.class, area)) {
            if (bee.getAttackTarget() == null) {
                EntityPlayer target = players.get(world.rand.nextInt(players.size()));
                bee.setBeeAttacker(target, 400 + world.rand.nextInt(380));
            }
        }
    }

    public boolean isFireNearby() {
        if (world == null) {
            return false;
        }
        for (BlockPos.MutableBlockPos cursor : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1),
                pos.add(1, 1, 1))) {
            IBlockState state = world.getBlockState(cursor);
            if (state.getBlock() == Blocks.FIRE || state.getMaterial() == Material.FIRE) {
                return true;
            }
        }
        return false;
    }

    public boolean isSedated() {
        if (world == null) {
            return false;
        }
        for (int distance = 1; distance <= 5; distance++) {
            BlockPos smokePos = pos.down(distance);
            IBlockState state = world.getBlockState(smokePos);
            if (isLitSmokeSource(state)) {
                return true;
            }
            if (blocksSmoke(state, smokePos)) {
                return isLitSmokeSource(world.getBlockState(smokePos.down()));
            }
        }
        return false;
    }

    private boolean blocksSmoke(IBlockState state, BlockPos smokePos) {
        AxisAlignedBB box = state.getCollisionBoundingBox(world, smokePos);
        if (box == null) {
            return false;
        }
        double centerX = box.maxX > 1.0D ? smokePos.getX() + 0.5D : 0.5D;
        double centerZ = box.maxZ > 1.0D ? smokePos.getZ() + 0.5D : 0.5D;
        return box.minX <= centerX && box.maxX >= centerX
                && box.minZ <= centerZ && box.maxZ >= centerZ;
    }

    private static boolean isLitSmokeSource(IBlockState state) {
        if (state.getBlock().getRegistryName() == null) {
            return false;
        }
        String path = state.getBlock().getRegistryName().getResourcePath();
        if (!path.equals("campfire") && !path.endsWith("_campfire")
                && !path.equals("brazier") && !path.endsWith("_brazier")
                && !path.equals("stove") && !path.endsWith("_stove")) {
            return false;
        }
        for (IProperty<?> property : state.getPropertyKeys()) {
            if ("lit".equals(property.getName())) {
                Comparable<?> value = state.getValue(property);
                return Boolean.TRUE.equals(value);
            }
        }
        return false;
    }

    public List<EntityBee> releaseAll(ReleaseStatus status, @Nullable EntityPlayer angryAt) {
        List<EntityBee> released = new ArrayList<>();
        if (world == null || world.isRemote) {
            return released;
        }
        Iterator<BeeOccupant> iterator = occupants.iterator();
        while (iterator.hasNext()) {
            BeeOccupant occupant = iterator.next();
            if (releaseBee(occupant, status, angryAt, released)) {
                iterator.remove();
            }
        }
        if (angryAt != null) {
            boolean sedated = isSedated();
            for (EntityBee bee : released) {
                if (bee.getDistanceSq(angryAt) > 16.0D) {
                    continue;
                }
                if (sedated) {
                    bee.setCannotEnterHiveTicks(400);
                } else {
                    bee.setBeeAttacker(angryAt, 400 + world.rand.nextInt(380));
                }
            }
        }
        markDirty();
        return released;
    }

    public void preserveForSilkTouch() {
        preserveOnBreak = true;
    }

    public boolean consumePreserveOnBreak() {
        boolean preserved = preserveOnBreak;
        preserveOnBreak = false;
        return preserved;
    }

    public void suppressReleaseOnBreak() {
        suppressReleaseOnBreak = true;
    }

    public boolean consumeSuppressReleaseOnBreak() {
        boolean suppressed = suppressReleaseOnBreak;
        suppressReleaseOnBreak = false;
        return suppressed;
    }

    public ItemStack createItemStack() {
        IBlockState state = world.getBlockState(pos);
        ItemStack stack = new ItemStack(state.getBlock());
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        tag.removeTag("id");
        tag.removeTag("x");
        tag.removeTag("y");
        tag.removeTag("z");
        stack.setTagInfo("BlockEntityTag", tag);
        return stack;
    }

    public void loadFromItem(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            return;
        }
        NBTTagCompound data = stack.getTagCompound().getCompoundTag("BlockEntityTag").copy();
        data.setInteger("x", pos.getX());
        data.setInteger("y", pos.getY());
        data.setInteger("z", pos.getZ());
        readFromNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        honeyLevel = Math.max(0, Math.min(MAX_HONEY_LEVEL, compound.getInteger("HoneyLevel")));
        flowerPos = readPos(compound, "FlowerPos");
        pendingHoneyStateSync = true;
        occupants.clear();
        NBTTagList bees = compound.getTagList("Bees", 10);
        for (int i = 0; i < bees.tagCount(); i++) {
            NBTTagCompound beeTag = bees.getCompoundTagAt(i);
            if (!beeTag.hasKey("EntityData", 10)) {
                continue;
            }
            occupants.add(new BeeOccupant(beeTag.getCompoundTag("EntityData"),
                    beeTag.getInteger("TicksInHive"), beeTag.getInteger("MinOccupationTicks")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("HoneyLevel", honeyLevel);
        writePos(compound, "FlowerPos", flowerPos);
        NBTTagList bees = new NBTTagList();
        for (BeeOccupant occupant : occupants) {
            NBTTagCompound beeTag = new NBTTagCompound();
            beeTag.setTag("EntityData", occupant.entityData.copy());
            beeTag.setInteger("TicksInHive", occupant.ticksInHive);
            beeTag.setInteger("MinOccupationTicks", occupant.minOccupationTicks);
            bees.appendTag(beeTag);
        }
        compound.setTag("Bees", bees);
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 11, writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    private boolean releaseBee(BeeOccupant occupant, ReleaseStatus status,
                               @Nullable EntityPlayer angryAt) {
        return releaseBee(occupant, status, angryAt, null);
    }

    private boolean releaseBee(BeeOccupant occupant, ReleaseStatus status,
                               @Nullable EntityPlayer angryAt, @Nullable List<EntityBee> released) {
        if (world == null || (!world.isDaytime() || world.isRaining())
                && status != ReleaseStatus.EMERGENCY) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockBeehive)) {
            return false;
        }
        BlockPos exit = pos.offset(state.getValue(BlockBeehive.FACING));
        boolean frontBlocked = world.getBlockState(exit).getCollisionBoundingBox(world, exit) != null;
        if (frontBlocked && status != ReleaseStatus.EMERGENCY) {
            return false;
        }
        NBTTagCompound entityData = occupant.entityData.copy();
        cleanStoredBeeData(entityData);
        advanceBeeTimers(entityData, occupant.ticksInHive);
        Entity entity = EntityList.createEntityFromNBT(entityData, world);
        if (!(entity instanceof EntityBee)) {
            return false;
        }
        EntityBee bee = (EntityBee) entity;
        bee.setNoGravity(true);
        double offset = frontBlocked ? 0.0D : 0.55D + bee.width / 2.0D;
        bee.setPosition(pos.getX() + 0.5D + state.getValue(BlockBeehive.FACING).getFrontOffsetX() * offset,
                pos.getY() + 0.5D - bee.height / 2.0D, pos.getZ() + 0.5D
                        + state.getValue(BlockBeehive.FACING).getFrontOffsetZ() * offset);
        bee.setHivePos(pos);
        if (flowerPos != null && bee.getFlowerPos() == null && world.rand.nextFloat() < 0.9F) {
            bee.setFlowerPos(flowerPos);
        }
        boolean deliveredHoney = status == ReleaseStatus.HONEY_DELIVERED;
        if (deliveredHoney) {
            bee.onHoneyDelivered();
        }
        if (!world.spawnEntity(bee)) {
            return false;
        }
        if (deliveredHoney) {
            setHoneyLevel(honeyLevel + (world.rand.nextInt(100) == 0 ? 2 : 1));
        }
        world.playSound(null, pos, FFDSounds.BEE_EXIT_HIVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (released != null) {
            released.add(bee);
        }
        return true;
    }

    private static void cleanStoredBeeData(NBTTagCompound data) {
        String[] ignored = {
                "Air", "ArmorDropChances", "ArmorItems", "CanPickUpLoot", "DeathTime",
                "Dimension", "FallDistance", "FallFlying", "Fire", "HandDropChances",
                "HandItems", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround",
                "PortalCooldown", "Pos", "Rotation", "CannotEnterHiveTicks",
                "TicksSincePollination", "CropsGrownSincePollination", "HivePos", "Passengers",
                "Leash", "Leashed", "UUID", "UUIDMost", "UUIDLeast"
        };
        for (String key : ignored) {
            data.removeTag(key);
        }
    }

    private static void advanceBeeTimers(NBTTagCompound entityData, int ticksInHive) {
        int age = entityData.getInteger("Age");
        if (age < 0) {
            entityData.setInteger("Age", Math.min(0, age + ticksInHive));
        } else if (age > 0) {
            entityData.setInteger("Age", Math.max(0, age - ticksInHive));
        }
        if (entityData.hasKey("InLove", 99)) {
            entityData.setInteger("InLove", Math.max(0,
                    entityData.getInteger("InLove") - ticksInHive));
        }
    }

    private void syncHoneyState() {
        if (world == null || world.isRemote) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockBeehive) {
            world.notifyBlockUpdate(pos, state, state, 3);
            world.updateComparatorOutputLevel(pos, state.getBlock());
        }
    }

    private static void writePos(NBTTagCompound compound, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        NBTTagCompound posTag = new NBTTagCompound();
        posTag.setInteger("X", pos.getX());
        posTag.setInteger("Y", pos.getY());
        posTag.setInteger("Z", pos.getZ());
        compound.setTag(key, posTag);
    }

    @Nullable
    private static BlockPos readPos(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 10)) {
            return null;
        }
        NBTTagCompound posTag = compound.getCompoundTag(key);
        return new BlockPos(posTag.getInteger("X"), posTag.getInteger("Y"), posTag.getInteger("Z"));
    }

    private static final class BeeOccupant {
        private final NBTTagCompound entityData;
        private int ticksInHive;
        private final int minOccupationTicks;

        private BeeOccupant(NBTTagCompound entityData, int ticksInHive, int minOccupationTicks) {
            this.entityData = entityData;
            this.ticksInHive = ticksInHive;
            this.minOccupationTicks = minOccupationTicks;
        }

        private boolean hasNectar() {
            return entityData.getBoolean("HasNectar");
        }
    }

    public enum ReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY
    }
}
