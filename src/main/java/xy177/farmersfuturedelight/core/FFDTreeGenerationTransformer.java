package xy177.farmersfuturedelight.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

final class FFDTreeGenerationTransformer {
    private static final String HOOKS =
            "xy177/farmersfuturedelight/common/worldgen/BeeNestTreeGeneration";
    private static final String TREE_ARGUMENTS =
            "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z";

    private FFDTreeGenerationTransformer() {
    }

    static boolean handles(String name) {
        return "net.minecraft.world.gen.feature.WorldGenerator".equals(name)
                || "net.minecraft.world.biome.BiomeDecorator".equals(name)
                || "net.minecraft.world.biome.BiomeForest".equals(name)
                || "net.minecraft.block.BlockSapling".equals(name);
    }

    static byte[] transform(String name, byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if ("net.minecraft.world.gen.feature.WorldGenerator".equals(name)) {
                if (("setBlockAndNotifyAdequately".equals(method.name)
                        || "func_175903_a".equals(method.name))
                        && method.desc.equals("(Lnet/minecraft/world/World;"
                                + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V")) {
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (instruction.getOpcode() != Opcodes.RETURN) continue;
                        InsnList record = new InsnList();
                        record.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        record.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        record.add(new VarInsnNode(Opcodes.ALOAD, 3));
                        record.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "recordPlacement", method.desc, false));
                        method.instructions.insertBefore(instruction, record);
                        changed = true;
                    }
                }
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL || !TREE_ARGUMENTS.equals(call.desc)
                        || !("generate".equals(call.name) || "func_180709_b".equals(call.name))) continue;
                boolean sapling = "net.minecraft.block.BlockSapling".equals(name);
                if (!("net/minecraft/world/gen/feature/WorldGenAbstractTree".equals(call.owner)
                        || sapling && "net/minecraft/world/gen/feature/WorldGenerator".equals(call.owner))) continue;
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        sapling ? "generateSapling" : "generateNatural",
                        "(Lnet/minecraft/world/gen/feature/WorldGenerator;" + TREE_ARGUMENTS.substring(1), false));
                changed = true;
            }
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
