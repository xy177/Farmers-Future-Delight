package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.BlockButtonStone;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public class BlockBlackstone extends BlockFutureStone {
    public BlockBlackstone(String name, float hardness, SoundType sound) {
        super(name, hardness, 6.0F, sound);
        setMapColor(MapColor.BLACK);
    }

    public static final class Gilded extends BlockBlackstone {
        public Gilded(SoundType sound) {
            super("gilded_blackstone", 1.5F, sound);
        }

        @Override
        public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                             IBlockState state, int fortune) {
            Random random = world instanceof World ? ((World) world).rand : new Random();
            EntityPlayer player = harvesters.get();
            if (player != null && EnchantmentHelper.getEnchantmentLevel(
                    Enchantments.SILK_TOUCH, player.getHeldItemMainhand()) > 0) {
                drops.add(new ItemStack(this));
                return;
            }
            float chance = fortune >= 3 ? 1.0F : fortune == 2 ? 0.25F
                    : fortune == 1 ? 1.0F / 7.0F : 0.1F;
            drops.add(random.nextFloat() < chance
                    ? new ItemStack(Items.GOLD_NUGGET, 2 + random.nextInt(4))
                    : new ItemStack(this));
        }

        @Override
        public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state,
                                      EntityPlayer player) {
            return true;
        }
    }

    public static final class Button extends BlockButtonStone {
        public Button() {
            setRegistryName(FarmerFutureDelight.MODID, "polished_blackstone_button");
            setUnlocalizedName(FarmerFutureDelight.MODID + ".polished_blackstone_button");
            setCreativeTab(FFDCreativeTab.INSTANCE);
            setHardness(0.5F);
        }
    }

    public static final class PressurePlate extends BlockPressurePlate {
        public PressurePlate() {
            super(Material.ROCK, Sensitivity.MOBS);
            setRegistryName(FarmerFutureDelight.MODID, "polished_blackstone_pressure_plate");
            setUnlocalizedName(FarmerFutureDelight.MODID + ".polished_blackstone_pressure_plate");
            setCreativeTab(FFDCreativeTab.INSTANCE);
            setHardness(0.5F);
        }

        @Override
        public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
            return MapColor.BLACK;
        }
    }
}
