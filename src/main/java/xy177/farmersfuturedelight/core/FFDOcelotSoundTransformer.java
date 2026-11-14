package xy177.farmersfuturedelight.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDOcelotSoundTransformer {
    private static final String HOOKS = "xy177/farmersfuturedelight/common/entity/FFDOcelotSounds";
    private static final String OCELOT = "Lnet/minecraft/entity/passive/EntityOcelot;";

    private FFDOcelotSoundTransformer() {
    }

    public static byte[] transform(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean mapped = false;
        boolean hasInterval = false;
        for (MethodNode method : node.methods) {
            if ("func_184639_G".equals(method.name)) mapped = true;
            if (("getTalkInterval".equals(method.name) || "func_70627_aG".equals(method.name))
                    && "()I".equals(method.desc)) {
                hasInterval = true;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() != Opcodes.IRETURN) continue;
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "interval", "(I" + OCELOT + ")I", false));
                    method.instructions.insertBefore(instruction, hook);
                }
            }
            int type = "getAmbientSound".equals(method.name) || "func_184639_G".equals(method.name)
                    ? 0 : "getHurtSound".equals(method.name) || "func_184601_bQ".equals(method.name)
                    ? 1 : "getDeathSound".equals(method.name) || "func_184615_bR".equals(method.name)
                    ? 2 : -1;
            boolean volume = "getSoundVolume".equals(method.name) || "func_70599_aP".equals(method.name);
            if (type < 0 && !volume) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != (volume ? Opcodes.FRETURN : Opcodes.ARETURN)) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                if (volume) {
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "volume",
                            "(F" + OCELOT + ")F", false));
                } else {
                    hook.add(new InsnNode(Opcodes.ICONST_0 + type));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sound",
                            "(Lnet/minecraft/util/SoundEvent;" + OCELOT
                                    + "I)Lnet/minecraft/util/SoundEvent;", false));
                }
                method.instructions.insertBefore(instruction, hook);
            }
        }
        String name = mapped ? "func_70627_aG" : "getTalkInterval";
        MethodNode interval = new MethodNode(Opcodes.ACC_PUBLIC, name, "()I", null, null);
        interval.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        interval.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                node.superName, name, "()I", false));
        interval.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        interval.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                "interval", "(I" + OCELOT + ")I", false));
        interval.instructions.add(new InsnNode(Opcodes.IRETURN));
        if (!hasInterval) node.methods.add(interval);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
