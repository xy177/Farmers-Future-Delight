package xy177.farmersfuturedelight.common.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TileEntityCaveVines extends TileEntity {
    private static final String AGE_KEY = "Age";
    private int age = -1;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = MathHelper.clamp(age, 0, 25);
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (age >= 0) {
            compound.setInteger(AGE_KEY, age);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        age = compound.hasKey(AGE_KEY, 3)
                ? MathHelper.clamp(compound.getInteger(AGE_KEY), 0, 25) : -1;
    }
}
