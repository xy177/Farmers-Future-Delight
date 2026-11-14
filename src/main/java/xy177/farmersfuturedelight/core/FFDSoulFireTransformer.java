package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDSoulFireTransformer implements IClassTransformer {
    private static final String HOOK = "xy177/farmersfuturedelight/core/FFDSoulFireHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !("net.minecraft.block.BlockFire".equals(transformedName)
                || "net.minecraft.block.BlockSkull".equals(transformedName)
                || "net.minecraft.world.World".equals(transformedName)
                || "net.minecraft.entity.Entity".equals(transformedName))) return bytes;
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        boolean changed = false;
        boolean mapped = node.methods.stream().anyMatch(method -> method.name.startsWith("func_"));
        for (MethodNode method : node.methods) {
            if ("net.minecraft.block.BlockFire".equals(transformedName)) {
                if ("isCollidable".equals(method.name) || "func_149703_v".equals(method.name)) {
                    method.instructions.clear();
                    method.tryCatchBlocks.clear();
                    if (method.localVariables != null) method.localVariables.clear();
                    method.instructions.add(new InsnNode(Opcodes.ICONST_1));
                    method.instructions.add(new InsnNode(Opcodes.IRETURN));
                    changed = true;
                    continue;
                }
                boolean placement = "canPlaceBlockAt".equals(method.name) || "func_176196_c".equals(method.name);
                boolean added = "onBlockAdded".equals(method.name) || "func_176213_c".equals(method.name);
                boolean neighbor = "neighborChanged".equals(method.name) || "func_189540_a".equals(method.name);
                boolean tick = "updateTick".equals(method.name) || "func_180650_b".equals(method.name);
                if (!placement && !added && !neighbor && !tick) continue;
                int world = neighbor ? 2 : 1;
                int position = neighbor ? 3 : 2;
                LabelNode original = new LabelNode();
                InsnList entry = new InsnList();
                entry.add(new VarInsnNode(Opcodes.ALOAD, world));
                entry.add(new VarInsnNode(Opcodes.ALOAD, position));
                entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK,
                        placement ? "canPlaceFire" : "convertFire",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
                entry.add(new JumpInsnNode(Opcodes.IFEQ, original));
                if (placement) entry.add(new InsnNode(Opcodes.ICONST_1));
                entry.add(new InsnNode(placement ? Opcodes.IRETURN : Opcodes.RETURN));
                entry.add(original);
                entry.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                method.instructions.insert(entry);
                changed = true;
            } else if ("net.minecraft.block.BlockSkull".equals(transformedName)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!"net/minecraft/block/state/pattern/BlockStateMatcher".equals(call.owner)
                            || !("forBlock".equals(call.name) || "func_177638_a".equals(call.name))) continue;
                    method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK,
                            "witherBase", "(Lcom/google/common/base/Predicate;)Lcom/google/common/base/Predicate;", false));
                    changed = true;
                }
            } else if ("net.minecraft.world.World".equals(transformedName)
                    && ("extinguishFire".equals(method.name) || "func_175719_a".equals(method.name))) {
                LabelNode original = new LabelNode();
                InsnList entry = new InsnList();
                for (int local = 0; local <= 3; local++) entry.add(new VarInsnNode(Opcodes.ALOAD, local));
                entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "extinguishFire",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;"
                                + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", false));
                entry.add(new JumpInsnNode(Opcodes.IFEQ, original));
                entry.add(new InsnNode(Opcodes.ICONST_1));
                entry.add(new InsnNode(Opcodes.IRETURN));
                entry.add(original);
                entry.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                method.instructions.insert(entry);
                changed = true;
            } else if ("net.minecraft.entity.Entity".equals(transformedName)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!("dealFireDamage".equals(call.name) || "func_70081_e".equals(call.name))
                            || !"(I)V".equals(call.desc)) continue;
                    InsnList damage = new InsnList();
                    damage.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    damage.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "fireDamage",
                            "(ILnet/minecraft/entity/Entity;)I", false));
                    method.instructions.insertBefore(call, damage);
                    changed = true;
                }
            }
        }
        if ("net.minecraft.block.BlockFire".equals(transformedName)) {
            addShapeMethod(node, mapped ? "func_185496_a" : "getBoundingBox", "fireBounds",
                    "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;"
                            + "Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;", 3);
            addShapeMethod(node, mapped ? "func_180636_a" : "collisionRayTrace", "fireRayTrace",
                    "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;"
                            + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Vec3d;"
                            + "Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;", 5);
            changed = true;
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static void addShapeMethod(ClassNode node, String name, String hook,
                                       String descriptor, int arguments) {
        if (node.methods.stream().anyMatch(method -> method.name.equals(name)
                && method.desc.equals(descriptor))) return;
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        for (int argument = 1; argument <= arguments; argument++) {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, argument));
        }
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, hook,
                descriptor, false));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        node.methods.add(method);
    }
}
