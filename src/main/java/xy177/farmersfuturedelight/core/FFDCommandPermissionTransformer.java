package xy177.farmersfuturedelight.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class FFDCommandPermissionTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FFD Command Permission Core");
    private static final String TARGET_CLASS = "net.minecraft.command.CommandReload";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        String className = transformedName == null ? name : transformedName;
        if (!TARGET_CLASS.equals(className)) {
            return basicClass;
        }
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"()I".equals(method.desc)
                    || !("getRequiredPermissionLevel".equals(method.name)
                    || "func_82362_a".equals(method.name))) {
                continue;
            }
            InsnList instructions = new InsnList();
            instructions.add(new InsnNode(Opcodes.ICONST_2));
            instructions.add(new InsnNode(Opcodes.IRETURN));
            method.instructions.clear();
            method.instructions.add(instructions);
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.maxStack = 0;
            method.maxLocals = 0;
            patched = true;
        }
        if (!patched) {
            LOGGER.warn("Could not find CommandReload permission method in {}", className);
            return basicClass;
        }
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
