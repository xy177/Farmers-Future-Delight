package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.util.NonNullList;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDRawOres;

public final class BlockCompatDeepslateOre extends BlockFutureStone {
    private final String material;
    private final IBlockState sourceState;
    private final ItemStack sourceStack;

    public BlockCompatDeepslateOre(String name, String material, IBlockState sourceState,
                                   ItemStack sourceStack, int harvestLevel) {
        super(name, 4.5F, 3.0F, SoundType.STONE, harvestLevel);
        this.material = material;
        this.sourceState = sourceState;
        this.sourceStack = sourceStack.copy();
        String harvestTool = sourceState.getBlock().getHarvestTool(sourceState);
        int sourceHarvestLevel = sourceState.getBlock().getHarvestLevel(sourceState);
        if (harvestTool != null && sourceHarvestLevel >= 0) {
            setHarvestLevel(harvestTool, sourceHarvestLevel);
        }
    }

    public IBlockState sourceState() {
        return sourceState;
    }

    public String material() {
        return material;
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        try {
            return sourceState.getBlock().getBlockHardness(sourceState, world, pos);
        } catch (RuntimeException ignored) {
            return super.getBlockHardness(state, world, pos);
        }
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
        try {
            return sourceState.getBlock().getSoundType(sourceState, world, pos, entity);
        } catch (RuntimeException ignored) {
            return super.getSoundType(state, world, pos, entity);
        }
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        try {
            return sourceState.getBlock().getExplosionResistance(exploder);
        } catch (RuntimeException ignored) {
            return super.getExplosionResistance(exploder);
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder,
                                        Explosion explosion) {
        try {
            return sourceState.getBlock().getExplosionResistance(world, pos, exploder, explosion);
        } catch (RuntimeException ignored) {
            return super.getExplosionResistance(world, pos, exploder, explosion);
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        try {
            return sourceState.getBlock().getLightValue(sourceState, world, pos);
        } catch (RuntimeException ignored) {
            return super.getLightValue(state, world, pos);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        ItemStack raw = rawStack();
        if (!raw.isEmpty()) {
            return raw.getItem();
        }
        Item item = sourceState.getBlock().getItemDropped(sourceState, random, fortune);
        return item == null ? sourceStack.getItem() : item;
    }

    @Override
    public int quantityDropped(Random random) {
        ItemStack raw = rawStack();
        return raw.isEmpty() ? sourceState.getBlock().quantityDropped(random)
                : FFDConfig.rawOreDropAmount;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        ItemStack raw = rawStack();
        if (raw.isEmpty()) {
            return sourceState.getBlock().quantityDroppedWithBonus(fortune, random);
        }
        int count = quantityDropped(random);
        if (fortune <= 0) {
            return count;
        }
        int multiplier = random.nextInt(fortune + 2) - 1;
        return count * (Math.max(0, multiplier) + 1);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        ItemStack raw = rawStack();
        return raw.isEmpty() ? sourceState.getBlock().quantityDropped(sourceState, fortune, random)
                : quantityDroppedWithBonus(fortune, random);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (rawStack().isEmpty()) {
            sourceState.getBlock().getDrops(drops, world, pos, sourceState, fortune);
        } else {
            super.getDrops(drops, world, pos, state, fortune);
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        ItemStack raw = rawStack();
        return raw.isEmpty() ? sourceState.getBlock().damageDropped(sourceState) : raw.getMetadata();
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return sourceState.getBlock().getExpDrop(sourceState, world, pos, fortune);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state,
                                  EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    private ItemStack rawStack() {
        for (int index = 0; index < FFDRawOres.NAMES.length; index++) {
            if (material.equals(FFDRawOres.NAMES[index])) {
                return FFDItems.effectiveStack(FFDItems.RAW_ORE_ITEMS[index]);
            }
        }
        FFDCustomRawOres.Entry custom = FFDCustomRawOres.get(material);
        if (custom != null) {
            return custom.rawStack();
        }
        return ItemStack.EMPTY;
    }
}
