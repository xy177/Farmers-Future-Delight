package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockFutureOre extends BlockFutureStone {
    public enum Drop {
        COAL,
        IRON,
        COPPER,
        GOLD,
        REDSTONE,
        LAPIS,
        DIAMOND,
        EMERALD
    }

    private final Drop drop;

    public BlockFutureOre(String name, Drop drop, int harvestLevel) {
        this(name, drop, harvestLevel, 4.5F, FFDSounds.DEEPSLATE);
    }

    public BlockFutureOre(String name, Drop drop, int harvestLevel, float hardness,
                          SoundType soundType) {
        super(name, hardness, 3.0F, soundType, harvestLevel);
        this.drop = drop;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        if (!rawDropEnabled()) {
            return Item.getItemFromBlock(this);
        }
        switch (drop) {
            case COAL:
                return Items.COAL;
            case IRON:
                return FFDItems.RAW_IRON;
            case COPPER:
                return FFDItems.RAW_COPPER;
            case GOLD:
                return FFDItems.RAW_GOLD;
            case REDSTONE:
                return Items.REDSTONE;
            case LAPIS:
                return Items.DYE;
            case DIAMOND:
                return Items.DIAMOND;
            case EMERALD:
                return Items.EMERALD;
            default:
                return Item.getItemFromBlock(this);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        if (!rawDropEnabled()) {
            return 1;
        }
        if (drop == Drop.COPPER) {
            return 2 + random.nextInt(4);
        }
        if (drop == Drop.REDSTONE) {
            return 4 + random.nextInt(2);
        }
        if (drop == Drop.LAPIS) {
            return 4 + random.nextInt(6);
        }
        return 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        int count = quantityDropped(random);
        if (fortune <= 0 || getItemDropped(getDefaultState(), random, fortune)
                == Item.getItemFromBlock(this)) {
            return count;
        }
        if (drop == Drop.REDSTONE) {
            return count + random.nextInt(fortune + 1);
        }
        int multiplier = random.nextInt(fortune + 2) - 1;
        return count * (Math.max(0, multiplier) + 1);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return drop == Drop.LAPIS ? EnumDyeColor.BLUE.getDyeDamage() : 0;
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Random random = world instanceof World ? ((World) world).rand : new Random();
        if (getItemDropped(state, random, fortune) == Item.getItemFromBlock(this)) {
            return 0;
        }
        switch (drop) {
            case COAL:
                return MathHelper.getInt(random, 0, 2);
            case IRON:
            case COPPER:
            case GOLD:
                return 0;
            case REDSTONE:
                return MathHelper.getInt(random, 1, 5);
            case LAPIS:
                return MathHelper.getInt(random, 2, 5);
            case DIAMOND:
            case EMERALD:
                return MathHelper.getInt(random, 3, 7);
            default:
                return 0;
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    private boolean rawDropEnabled() {
        if (!FFDConfig.oresDropRawMaterials) {
            return drop != Drop.IRON && drop != Drop.COPPER && drop != Drop.GOLD;
        }
        if (drop == Drop.IRON || drop == Drop.GOLD) {
            return FFDItems.isRawOreEnabled();
        }
        return drop != Drop.COPPER || FFDItems.isCopperEnabled();
    }
}
