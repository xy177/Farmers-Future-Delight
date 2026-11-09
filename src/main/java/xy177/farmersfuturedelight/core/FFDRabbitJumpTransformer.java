package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FFDRabbitJumpTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FFD Rabbit Jump Core");
    private static final String TARGET_CLASS = "net.minecraft.entity.passive.EntityRabbit";
    private static final String HOOK_CLASS = "xy177/farmersfuturedelight/core/FFDRabbitJumpTransformer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET_CLASS.equals(transformedName == null ? name : transformedName)) return basicClass;
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            boolean startsJump = "()V".equals(method.desc)
                    && ("startJumping".equals(method.name) || "func_184770_cZ".equals(method.name));
            boolean receivesJump = "(B)V".equals(method.desc)
                    && ("handleStatusUpdate".equals(method.name) || "func_70103_a".equals(method.name));
            if (!startsJump && !receivesJump) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof FieldInsnNode) || instruction.getOpcode() != Opcodes.PUTFIELD) continue;
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (!node.name.equals(field.owner) || !"I".equals(field.desc)
                        || !("jumpDuration".equals(field.name) || "field_175535_bn".equals(field.name))) continue;
                AbstractInsnNode value = instruction.getPrevious();
                if (!(value instanceof IntInsnNode) || value.getOpcode() != Opcodes.BIPUSH
                        || ((IntInsnNode) value).operand != 10) continue;
                InsnList replacement = new InsnList();
                replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                replacement.add(new IntInsnNode(Opcodes.BIPUSH, 10));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_CLASS,
                        "jumpDuration", "(Ljava/lang/Object;I)I", false));
                method.instructions.insertBefore(value, replacement);
                method.instructions.remove(value);
                patched++;
            }
        }
        if (patched == 0) {
            LOGGER.warn("Could not find vanilla rabbit jump duration assignments");
            return basicClass;
        }
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        LOGGER.info("Updated {} rabbit jump duration assignments", patched);
        return writer.toByteArray();
    }

    public static int jumpDuration(Object rabbit, int original) {
        return TARGET_CLASS.equals(rabbit.getClass().getName()) ? 15 : original;
    }
}
