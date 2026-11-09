package xy177.farmersfuturedelight.core;

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

public final class FFDAgeLockTransformer {
    private static final String HOOKS = "xy177/farmersfuturedelight/common/entity/FFDAgeLock";

    private FFDAgeLockTransformer() {
    }

    public static byte[] transformHorseFeeding(byte[] bytes) {
        return FFDHorseSoundTransformer.transform(transformFeeding(bytes, "handleEating", "func_190678_b"));
    }

    public static byte[] transformAnimalFeeding(byte[] bytes) {
        return transformFeeding(bytes, "processInteract", "func_184645_a");
    }

    private static byte[] transformFeeding(byte[] bytes, String readable, String mapped) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (!readable.equals(method.name) && !mapped.equals(method.name)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (("isChild".equals(call.name) || "func_70631_g_".equals(call.name)) && "()Z".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "canGrow", "(Lnet/minecraft/entity/EntityAgeable;)Z", false));
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (("onLivingUpdate".equals(method.name) || "func_70636_d".equals(method.name))
                    && "()V".equals(method.desc)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) instruction;
                        if (("setGrowingAge".equals(call.name) || "func_70873_a".equals(call.name))
                                && "(I)V".equals(call.desc)) {
                            method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    HOOKS, "advanceAge", "(Lnet/minecraft/entity/EntityAgeable;I)V", false));
                        }
                    }
                }
            }
            if (("ageUp".equals(method.name) || "func_175501_a".equals(method.name))
                    && "(IZ)V".equals(method.desc)) {
                InsnList guard = new InsnList();
                LabelNode proceed = new LabelNode();
                guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isLocked",
                        "(Lnet/minecraft/entity/EntityLivingBase;)Z", false));
                guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
                guard.add(new InsnNode(Opcodes.RETURN));
                guard.add(proceed);
                guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                method.instructions.insert(guard);
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
