package xy177.farmersfuturedelight.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

final class FFDNetherUpdateTransformer {
    private static final String HOOKS = "xy177/farmersfuturedelight/common/FFDNetherUpdateGameplay";

    private FFDNetherUpdateTransformer() {
    }

    private static InsnList list(AbstractInsnNode... instructions) {
        InsnList result = new InsnList();
        for (AbstractInsnNode instruction : instructions) result.add(instruction);
        return result;
    }

    static void redstone(ClassNode node) {
        String hooks = "xy177/farmersfuturedelight/common/FFDRedstoneWire";
        node.interfaces.add("xy177/farmersfuturedelight/core/FFDRedstoneAccess");
        MethodNode wrapper = null;
        for (MethodNode method : node.methods) {
            if ((method.name.equals("getActualState") || method.name.equals("func_176221_a"))
                    && method.desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;")) {
                wrapper = new MethodNode(Opcodes.ACC_PUBLIC, method.name, method.desc, null, null);
                method.name = "ffd$getWireState";
                wrapper.instructions.add(list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1), new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, method.name, method.desc, false),
                        new VarInsnNode(Opcodes.ALOAD, 2), new VarInsnNode(Opcodes.ALOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "actual", method.desc, false),
                        new InsnNode(Opcodes.ARETURN)));
            } else if ((method.name.equals("getWeakPower") || method.name.equals("func_180656_a"))
                    && method.desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                if (method.localVariables != null) method.localVariables.clear();
                method.instructions.add(list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1), new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3), new VarInsnNode(Opcodes.ALOAD, 4),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "power",
                                "(Lnet/minecraft/block/BlockRedstoneWire;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", false),
                        new InsnNode(Opcodes.IRETURN)));
            } else if ((method.name.equals("breakBlock") || method.name.equals("func_180663_b"))
                    && method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V")) {
                method.instructions.insert(list(new VarInsnNode(Opcodes.ALOAD, 1), new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "removed",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false)));
            } else if ((method.name.equals("neighborChanged") || method.name.equals("func_189540_a"))
                    && method.desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V")) {
                method.instructions.insert(list(new VarInsnNode(Opcodes.ALOAD, 2), new VarInsnNode(Opcodes.ALOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "changed",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false)));
            }
        }
        if (wrapper == null) throw new IllegalStateException("Missing redstone connections method");
        node.methods.add(wrapper);
    }

    static void fishing(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("onUpdate") || method.name.equals("func_70071_h_"))
                    || !method.desc.equals("()V")) continue;
            method.instructions.insert(list(new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "xy177/farmersfuturedelight/common/FFDFishing", "update",
                            "(Lnet/minecraft/entity/projectile/EntityFishHook;)V", false)));
        }
    }

    static void item(ClassNode node) {
        for (MethodNode method : node.methods) {
            String hook = null;
            String descriptor = null;
            InsnList arguments = new InsnList();
            int opcode = Opcodes.IRETURN;
            if ((method.name.equals("getDestroySpeed") || method.name.equals("func_150893_a"))
                    && method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F")) {
                hook = "destroySpeed";
                descriptor = "(FLnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)F";
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 1));
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 2));
                opcode = Opcodes.FRETURN;
            } else if (method.name.equals("canApplyAtEnchantingTable")) {
                hook = "canEnchant";
                descriptor = "(ZLnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/Enchantment;)Z";
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 1));
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 2));
            } else if ((method.name.equals("getItemEnchantability") || method.name.equals("func_77619_b"))
                    && method.desc.equals("()I")) {
                hook = "enchantability";
                descriptor = "(ILnet/minecraft/item/Item;)I";
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 0));
            } else if ((method.name.equals("onBlockDestroyed") || method.name.equals("func_179218_a"))
                    && method.desc.endsWith("Lnet/minecraft/entity/EntityLivingBase;)Z")) {
                hook = "destroyed";
                descriptor = "(ZLnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/EntityLivingBase;)Z";
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 1));
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 3));
                arguments.add(new VarInsnNode(Opcodes.ALOAD, 5));
            }
            if (hook == null) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != opcode) continue;
                InsnList call = new InsnList();
                for (AbstractInsnNode argument : arguments.toArray()) call.add(argument.clone(null));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hook, descriptor, false));
                method.instructions.insertBefore(instruction, call);
            }
        }
    }

    static void containerLoot(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)V")
                    || !(method.name.equals("fillWithLoot") || method.name.equals("func_184281_d"))) continue;
            InsnList call = new InsnList();
            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
            call.add(new VarInsnNode(Opcodes.ALOAD, 1));
            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "xy177/farmersfuturedelight/common/FFDAdvancementEvents", "containerLoot",
                    "(Lnet/minecraft/tileentity/TileEntityLockableLoot;Lnet/minecraft/entity/player/EntityPlayer;)V", false));
            method.instructions.insert(call);
        }
    }

    static void knockBack(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!(method.name.equals("knockBack") || method.name.equals("func_70653_a"))
                    || !method.desc.equals("(Lnet/minecraft/entity/Entity;FDD)V")) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) method.localVariables.clear();
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
            method.instructions.add(new VarInsnNode(Opcodes.DLOAD, 3));
            method.instructions.add(new VarInsnNode(Opcodes.DLOAD, 5));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "knockBack",
                    "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;FDD)V", false));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }
}
